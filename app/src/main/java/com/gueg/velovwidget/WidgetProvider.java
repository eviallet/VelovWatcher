package com.gueg.velovwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.gueg.velovwidget.database_stations.JsonParser;
import com.gueg.velovwidget.map.PinsActivity;
import com.gueg.velovwidget.sorting.SortActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WidgetProvider extends AppWidgetProvider {

    public static final String STATION_CLICK_ACTION = "com.gueg.velovwatcher.widgetprovider.station_click_action";
    public static final String STATION_INDEX_EXTRA = "com.gueg.velovwatcher.widgetprovider.station_index_extra";

    private boolean _isConfig = false;
    private int[] _appWidgetIds;
    private AppWidgetManager _appWidgetManager;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);
        if(intent.getAction()==null)
            return;
        switch(intent.getAction()) {
            case STATION_CLICK_ACTION:
                /*
                // TODO show station on oficial app
                final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                final int viewIndex = intent.getIntExtra(STATION_INDEX_EXTRA, 0);
                Toast.makeText(context, "Touched " + viewIndex, Toast.LENGTH_SHORT).show();
                */
                break;
            case Intent.ACTION_BOOT_COMPLETED:
            case ConnectivityManager.CONNECTIVITY_ACTION:
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                break;
            default:
                break;
        }
        super.onReceive(context,intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(":-:","WProvider - onUpdate");

        if(!_isConfig) {
            _appWidgetIds = appWidgetIds;
            _appWidgetManager = appWidgetManager;
            init(context.getApplicationContext());
        }

        boolean isConnected = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm!=null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        if(!JsonParser.IS_API_KEY_LOADED)
            JsonParser.loadApiKey(context);

        Log.d(":-:","WProvider - isConnected = "+isConnected);

        if(isConnected) {

            for (int appWidgetId : appWidgetIds) {
                // Parsing views from current widget
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list);

                // Preparing list adapter
                Intent intent = new Intent(context, WidgetService.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
                views.setRemoteAdapter(R.id.widget_list_stations, intent);
                views.setEmptyView(R.id.widget_list_stations, R.id.widget_list_empty);

                // Handling header buttons clicks
                views.setOnClickPendingIntent(R.id.widget_header_sort, PendingIntent.getActivity(context, 0, new Intent(context, SortActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
                views.setOnClickPendingIntent(R.id.widget_header_configure, PendingIntent.getActivity(context, 0, new Intent(context, PinsActivity.class).setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId), PendingIntent.FLAG_UPDATE_CURRENT));
                views.setOnClickPendingIntent(R.id.widget_header_update, PendingIntent.getBroadcast(context, 0, new Intent().setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds), PendingIntent.FLAG_UPDATE_CURRENT));
                views.setOnClickPendingIntent(R.id.widget_header_title, PendingIntent.getBroadcast(context, 0, new Intent().setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds), PendingIntent.FLAG_UPDATE_CURRENT));
                views.setOnClickPendingIntent(R.id.widget_update_text, PendingIntent.getBroadcast(context, 0, new Intent().setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds), PendingIntent.FLAG_UPDATE_CURRENT));

                // Handling list clicks
                Intent stationIntent = new Intent(context, WidgetProvider.class);
                stationIntent.setAction(STATION_CLICK_ACTION);
                stationIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                stationIntent.setData(Uri.parse(stationIntent.toUri(Intent.URI_INTENT_SCHEME)));
                views.setPendingIntentTemplate(R.id.widget_list_stations, PendingIntent.getBroadcast(context, 0, stationIntent, PendingIntent.FLAG_UPDATE_CURRENT));


                views.setTextViewText(R.id.widget_update_text,
                        context.getResources().getString(R.string.widget_last_udpate_time) + " " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime()));

                // Update stations info (trigger WidgetDataProvider.onDataSetChanged)
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_stations);

                // Updating widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
            super.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }


    private void init(Context context) {
        Log.d(":-:","WProvider - init");
        _isConfig = true;

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,false)) {
                    Log.d(":-:", "WProvider - connectivity changed");
                    onUpdate(context, _appWidgetManager, _appWidgetIds);
                }
            }
        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

}
