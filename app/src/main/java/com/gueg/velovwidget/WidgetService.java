package com.gueg.velovwidget;

import android.appwidget.AppWidgetManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.gueg.velovwidget.database_stations.JsonParser;
import com.gueg.velovwidget.database_stations.WidgetItemsDatabase;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(getApplicationContext(), intent);
    }
}

class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<WidgetItem> mWidgetItems = new ArrayList<>();
    private Context mContext;
    private int progress;
    private boolean progressShowing = false;
    private int mAppWidgetId;


    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

    }
    public void onCreate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Room.databaseBuilder(mContext, WidgetItemsDatabase.class, "widget_items").build();
                mWidgetItems.clear();
                try {
                    mWidgetItems.addAll(new WidgetItemsDatabase.DatabaseLoader.PinnedItems().execute(mContext, WidgetItem.getSelectedContract(mContext)).get());
                    mWidgetItems = WidgetItem.sort(mWidgetItems);
                    Log.d(":-:","onCreate - loaded "+mWidgetItems.size());
                } catch (ExecutionException|InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetItems.clear();
    }
    public int getCount() {
        return mWidgetItems.size();
    }
    public RemoteViews getViewAt(int position) {
        Log.d(":-:","WService - getViewAt "+position);


        WidgetItem item = JsonParser.updateDynamicDataFromApi(mWidgetItems.get(position));
        mWidgetItems.remove(position);
        mWidgetItems.add(position, item);

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        rv.setTextViewText(R.id.widget_item_title, mWidgetItems.get(position).name);
        if(!item.isOpen())
            rv.setTextColor(R.id.widget_item_title, mContext.getResources().getColor(R.color.colorLow));

        // Available bikes
        rv.setTextViewText(R.id.widget_item_available_bikes, ""+mWidgetItems.get(position).data.available_bikes);
        if(mWidgetItems.get(position).data.available_bikes<mWidgetItems.get(position).data.bike_stands*0.15)
            rv.setTextColor(R.id.widget_item_available_bikes, mContext.getResources().getColor(R.color.colorLow));
        else if(mWidgetItems.get(position).data.available_bikes<mWidgetItems.get(position).data.bike_stands*0.3)
            rv.setTextColor(R.id.widget_item_available_bikes, mContext.getResources().getColor(R.color.colorMed));
        else
            rv.setTextColor(R.id.widget_item_available_bikes, mContext.getResources().getColor(R.color.colorHig));

        // Available bike stands
        rv.setTextViewText(R.id.widget_item_available_bike_stands, ""+mWidgetItems.get(position).data.available_bike_stands);
        if(mWidgetItems.get(position).data.available_bike_stands<mWidgetItems.get(position).data.bike_stands*0.15)
            rv.setTextColor(R.id.widget_item_available_bike_stands, mContext.getResources().getColor(R.color.colorLow));
        else if(mWidgetItems.get(position).data.available_bike_stands<mWidgetItems.get(position).data.bike_stands*0.3)
            rv.setTextColor(R.id.widget_item_available_bike_stands, mContext.getResources().getColor(R.color.colorMed));
        else
            rv.setTextColor(R.id.widget_item_available_bike_stands, mContext.getResources().getColor(R.color.colorHig));

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in WidgetProvider.
        rv.setOnClickFillInIntent(R.id.widget_item_title, new Intent().putExtra(WidgetProvider.STATION_INDEX_EXTRA, position));


        // Progress animation
        progress++;
        onProgressChanged();


        return rv;
    }
    public RemoteViews getLoadingView() {
        return null;
    }
    public int getViewTypeCount() {
        return 1;
    }
    public long getItemId(int position) {
        return position;
    }
    public boolean hasStableIds() {
        return true;
    }
    public void onDataSetChanged() {
        mWidgetItems.clear();
        try {
            progress = 0;
            mWidgetItems.addAll(new WidgetItemsDatabase.DatabaseLoader.PinnedItems().execute(mContext, WidgetItem.getSelectedContract(mContext)).get());
            mWidgetItems = WidgetItem.sort(mWidgetItems);
            Log.d(":-:","WDataprovider - onDataSetChanged - new mWidgetItems size = "+mWidgetItems.size());
            onProgressChanged();
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void onProgressChanged() {
        boolean changeMade = false;
        RemoteViews widget = new RemoteViews(mContext.getPackageName(), R.layout.widget_list);
        if(progress==mWidgetItems.size()) {
            if(!progressShowing) {
                widget.setViewVisibility(R.id.widget_header_update, View.INVISIBLE);
                widget.setViewVisibility(R.id.widget_header_update_animation, View.VISIBLE);
                progressShowing = true;
                changeMade = true;
                Log.d(":-:","onProgressChanged - showing progress");
            }
        } else {
            if(progressShowing) {
                widget.setViewVisibility(R.id.widget_header_update, View.VISIBLE);
                widget.setViewVisibility(R.id.widget_header_update_animation, View.INVISIBLE);
                progressShowing = false;
                changeMade = true;
                Log.d(":-:","onProgressChanged - hiding progress");
            }
        }
        if(changeMade)
            AppWidgetManager.getInstance(mContext).updateAppWidget(mAppWidgetId, widget);
    }



}