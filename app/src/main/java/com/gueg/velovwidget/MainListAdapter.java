package com.gueg.velovwidget;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gueg.velovwidget.database_stations.JsonParser;
import com.gueg.velovwidget.database_stations.WidgetItemsDatabase;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.gueg.velovwidget.MainListActivity.IS_DATABASE_BUSY;

public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.ViewHolder> {
    private boolean _init = false;
    private RefreshListener _listener;


    private ArrayList<Item> _list;
    private Context c;


    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout bkg, dynamicDataLayout;
        TextView title, bikes, stands;
        ViewHolder(View v) {
            super(v);
            bkg = v.findViewById(R.id.widget_item_bkg);
            dynamicDataLayout = v.findViewById(R.id.widget_item_dynamic_data_layout);
            title = v.findViewById(R.id.widget_item_title);
            bikes = v.findViewById(R.id.widget_item_available_bikes);
            stands = v.findViewById(R.id.widget_item_available_bike_stands);
        }
    }

    public MainListAdapter(Context c) {
        this.c = c;
        _list = new ArrayList<>();
    }

    @NonNull
    @Override
    public MainListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.row_item, parent, false);
        return new MainListAdapter.ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MainListAdapter.ViewHolder holder, final int position) {
        if(position<_list.size()) {
            Item item = _list.get(position);

            holder.title.setText(item.name);

            if(item.isSeparator()) {
                holder.dynamicDataLayout.setVisibility(View.GONE);
                holder.title.setTextColor(c.getResources().getColor(R.color.colorTextWhite));
                holder.bkg.setBackgroundColor(c.getResources().getColor(R.color.colorPrimary));
            } else {
                holder.dynamicDataLayout.setVisibility(View.VISIBLE);
                holder.title.setTextColor(c.getResources().getColor(R.color.colorTextBlack));
                holder.bkg.setBackgroundColor(c.getResources().getColor(R.color.colorTextWhite));

                if (item.data != null && !item.isOpen())
                    holder.title.setTextColor(c.getResources().getColor(R.color.colorLow));

                if (item.data != null) {
                    // Available bikes
                    holder.bikes.setText(Integer.toString(item.data.available_bikes));
                    if (item.data.available_bikes < item.data.bike_stands * 0.15)
                        holder.bikes.setTextColor(c.getResources().getColor(R.color.colorLow));
                    else if (item.data.available_bikes < item.data.bike_stands * 0.3)
                        holder.bikes.setTextColor(c.getResources().getColor(R.color.colorMed));
                    else
                        holder.bikes.setTextColor(c.getResources().getColor(R.color.colorHig));

                    // Available bike stands
                    holder.stands.setText(Integer.toString(item.data.available_bike_stands));
                    if (item.data.available_bike_stands < item.data.bike_stands * 0.15)
                        holder.stands.setTextColor(c.getResources().getColor(R.color.colorLow));
                    else if (item.data.available_bike_stands < item.data.bike_stands * 0.3)
                        holder.stands.setTextColor(c.getResources().getColor(R.color.colorMed));
                    else
                        holder.stands.setTextColor(c.getResources().getColor(R.color.colorHig));
                }
            }
        }
    }


    public void refresh() {
        _listener.onRefreshStarted();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!_init) {
                    Room.databaseBuilder(c, WidgetItemsDatabase.class, "widget_items").build();
                    _init = true;
                }
                try {
                    while(PreferenceManager.getDefaultSharedPreferences(c).getBoolean(IS_DATABASE_BUSY,false));

                    _list.clear();
                    _list.addAll(
                            Item.sort(
                                    new WidgetItemsDatabase.DatabaseLoader.PinnedItems().
                                            execute(c, Item.getSelectedContract(c)).
                                            get()));
                    ((MainListActivity)c).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainListAdapter.this.notifyDataSetChanged();
                        }
                    });
                    for(int i=0; i<_list.size(); i++) {
                        final int pos = i;
                        if(!_list.get(i).isSeparator())
                            _list.get(i).setData(JsonParser.updateDynamicDataFromApi(_list.get(i)).data);
                        ((MainListActivity)c).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainListAdapter.this.notifyItemChanged(pos);
                                _listener.onProgressChanged(pos, _list.size());
                            }
                        });
                    }
                    ((MainListActivity)c).runOnUiThread(new Runnable() {
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
    public int getItemCount() {
        return _list.size();
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
