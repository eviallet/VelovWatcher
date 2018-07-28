package com.gueg.velovwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class WidgetProvider extends AppWidgetProvider {

    public static final String STATION_CLICK_ACTION = "com.gueg.velovwatcher.widgetprovider.station_click_action";
    public static final String STATION_INDEX_EXTRA = "com.gueg.velovwatcher.widgetprovider.station_index_extra";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction()!=null&&intent.getAction().equals(STATION_CLICK_ACTION)) {
            final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            final int viewIndex = intent.getIntExtra(STATION_INDEX_EXTRA, 0);
            Toast.makeText(context, "Touched " + viewIndex, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(":-:","WProvider - onUpdate");
        for(int appWidgetId : appWidgetIds) {
            // Parsing views from current widget
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list);

            // Preparing list adapter
            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(R.id.widget_list_stations, intent);
            views.setEmptyView(R.id.widget_list_stations, R.id.widget_list_empty);

            // Handling header buttons clicks
            views.setOnClickPendingIntent(R.id.widget_header_configure, PendingIntent.getActivity(context, 0, new Intent(context, PinsActivity.class).setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId), PendingIntent.FLAG_UPDATE_CURRENT));
            views.setOnClickPendingIntent(R.id.widget_header_update, PendingIntent.getBroadcast(context, 0, new Intent().setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds), PendingIntent.FLAG_UPDATE_CURRENT));
            views.setOnClickPendingIntent(R.id.widget_header_title, PendingIntent.getBroadcast(context, 0, new Intent().setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds), PendingIntent.FLAG_UPDATE_CURRENT));
            views.setOnClickPendingIntent(R.id.widget_header_update_text, PendingIntent.getBroadcast(context, 0, new Intent().setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds), PendingIntent.FLAG_UPDATE_CURRENT));

            // Handling list clicks
            Intent stationIntent = new Intent(context, WidgetProvider.class);
            stationIntent.setAction(STATION_CLICK_ACTION);
            stationIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            stationIntent.setData(Uri.parse(stationIntent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setPendingIntentTemplate(R.id.widget_list_stations, PendingIntent.getBroadcast(context, 0, stationIntent, PendingIntent.FLAG_UPDATE_CURRENT));


            views.setTextViewText(R.id.widget_header_update_text,
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime()));

            // Updating widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

}
