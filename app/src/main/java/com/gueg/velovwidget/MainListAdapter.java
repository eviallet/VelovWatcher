package com.gueg.velovwidget;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gueg.velovwidget.database_stations.JsonParser;
import com.gueg.velovwidget.database_stations.WidgetItemsDatabase;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainListAdapter extends ArrayAdapter<Item> {
    private boolean _init = false;
    private RefreshListener _listener;

    public MainListAdapter(final Context context) {
        super(context, 0);
    }


    public void refresh() {
        _listener.onRefreshStarted();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!_init) {
                    Room.databaseBuilder(getContext(), WidgetItemsDatabase.class, "widget_items").build();
                    _init = true;
                }
                try {
                    final ArrayList<Item> items =
                            Item.sort(
                                    new WidgetItemsDatabase.DatabaseLoader.PinnedItems().
                                            execute(getContext(), Item.getSelectedContract(getContext())).
                                            get());
                    final ArrayList<Item> cp = (ArrayList<Item>)items.clone();
                    ((MainListActivity)getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            clear();
                            MainListAdapter.this.addAll(cp);
                        }
                    });
                    for(int i=0; i<items.size(); i++) {
                        final int pos = i;
                        Item item = items.get(i);
                        items.remove(i);
                        items.add(i,JsonParser.updateDynamicDataFromApi(item));
                        ((MainListActivity)getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainListAdapter.this.getItem(pos).setData(items.get(pos).data);
                                MainListAdapter.this.notifyDataSetChanged();
                                _listener.onProgressChanged(pos, items.size());
                            }
                        });
                    }
                    ((MainListActivity)getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _listener.onRefreshFinished();
                        }
                    });
                } catch (ExecutionException |InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Item item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_item, parent, false);
        }

        ((TextView)convertView.findViewById(R.id.widget_item_title)).setText(item.name);

        if(item.data!=null&&!item.isOpen())
            ((TextView)convertView.findViewById(R.id.widget_item_title)).setTextColor(getContext().getResources().getColor(R.color.colorLow));

        if(item.data!=null) {
            // Available bikes
            ((TextView) convertView.findViewById(R.id.widget_item_available_bikes)).setText(Integer.toString(item.data.available_bikes));
            if (item.data.available_bikes < item.data.bike_stands * 0.15)
                ((TextView) convertView.findViewById(R.id.widget_item_available_bikes)).setTextColor(getContext().getResources().getColor(R.color.colorLow));
            else if (item.data.available_bikes < item.data.bike_stands * 0.3)
                ((TextView) convertView.findViewById(R.id.widget_item_available_bikes)).setTextColor(getContext().getResources().getColor(R.color.colorMed));
            else
                ((TextView) convertView.findViewById(R.id.widget_item_available_bikes)).setTextColor(getContext().getResources().getColor(R.color.colorHig));

            // Available bike stands
            ((TextView) convertView.findViewById(R.id.widget_item_available_bike_stands)).setText(Integer.toString(item.data.available_bike_stands));
            if (item.data.available_bike_stands < item.data.bike_stands * 0.15)
                ((TextView) convertView.findViewById(R.id.widget_item_available_bike_stands)).setTextColor(getContext().getResources().getColor(R.color.colorLow));
            else if (item.data.available_bike_stands < item.data.bike_stands * 0.3)
                ((TextView) convertView.findViewById(R.id.widget_item_available_bike_stands)).setTextColor(getContext().getResources().getColor(R.color.colorMed));
            else
                ((TextView) convertView.findViewById(R.id.widget_item_available_bike_stands)).setTextColor(getContext().getResources().getColor(R.color.colorHig));
        }

        return convertView;
    }

    public void setListener(RefreshListener l) {
        _listener = l;
    }

    public interface RefreshListener {
        void onRefreshStarted();
        void onProgressChanged(int progress, int max);
        void onRefreshFinished();
    }
}