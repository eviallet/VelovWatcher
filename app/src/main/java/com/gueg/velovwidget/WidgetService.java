package com.gueg.velovwidget;

import android.appwidget.AppWidgetManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(getApplicationContext(), intent);
    }
}

class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
    private int mCount = 3;
    private ArrayList<WidgetItem> mWidgetItems = new ArrayList<>();
    private Context mContext;
    private int mAppWidgetId;


    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

    }
    public void onCreate() {
        Room.databaseBuilder(mContext, WidgetItemsDatabase.class, "widget_items").build();
        // In onCreate() you setup any connections / cursors to your data source.
        for (int i = 0; i < mCount; i++) {
            mWidgetItems.add(new WidgetItem(i + "!"));
        }
    }
    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetItems.clear();
    }
    public int getCount() {
        return mCount;
    }
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        rv.setTextViewText(R.id.widget_item_title, mWidgetItems.get(position).name);

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in WidgetProvider.
        rv.setOnClickFillInIntent(R.id.widget_item_title, new Intent().putExtra(WidgetProvider.STATION_INDEX_EXTRA, position));

        // You can do heaving lifting in here, synchronously.

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

    }
}