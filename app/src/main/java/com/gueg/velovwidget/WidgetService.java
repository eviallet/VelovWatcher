package com.gueg.velovwidget;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

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
    //private int mAppWidgetId;


    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
        //mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
        //        AppWidgetManager.INVALID_APPWIDGET_ID);

    }
    public void onCreate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Room.databaseBuilder(mContext, WidgetItemsDatabase.class, "widget_items").build();
            }
        }).start();
        // In onCreate() you setup any connections / cursors to your data source.
        //for (int i = 0; i < mCount; i++)
        //    mWidgetItems.add(new WidgetItem(i + "!"));
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
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        rv.setTextViewText(R.id.widget_item_title, mWidgetItems.get(position).name);

        // Available bikes
        rv.setTextViewText(R.id.widget_item_available_bikes, ""+mWidgetItems.get(position).available_bikes);
        if(mWidgetItems.get(position).available_bikes<mWidgetItems.get(position).bike_stands*0.15)
            rv.setTextColor(R.id.widget_item_available_bikes, mContext.getResources().getColor(R.color.colorLow));
        else if(mWidgetItems.get(position).available_bikes<mWidgetItems.get(position).bike_stands*0.3)
            rv.setTextColor(R.id.widget_item_available_bikes, mContext.getResources().getColor(R.color.colorMed));
        else
            rv.setTextColor(R.id.widget_item_available_bikes, mContext.getResources().getColor(R.color.colorHig));

        // Available bike stands
        rv.setTextViewText(R.id.widget_item_available_bike_stands, ""+mWidgetItems.get(position).available_bike_stands);
        if(mWidgetItems.get(position).available_bike_stands<mWidgetItems.get(position).bike_stands*0.15)
            rv.setTextColor(R.id.widget_item_available_bike_stands, mContext.getResources().getColor(R.color.colorLow));
        else if(mWidgetItems.get(position).available_bike_stands<mWidgetItems.get(position).bike_stands*0.3)
            rv.setTextColor(R.id.widget_item_available_bike_stands, mContext.getResources().getColor(R.color.colorMed));
        else
            rv.setTextColor(R.id.widget_item_available_bike_stands, mContext.getResources().getColor(R.color.colorHig));

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in WidgetProvider.
        rv.setOnClickFillInIntent(R.id.widget_item_title, new Intent().putExtra(WidgetProvider.STATION_INDEX_EXTRA, position));


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
            mWidgetItems.addAll(new WidgetItemsDatabase.DatabaseLoader.PinnedItems().execute(mContext).get());
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
        }
    }



}