package com.gueg.velovwidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;

import java.util.ArrayList;

public class PinsActivity extends AppCompatActivity {

    private int id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_pins);

        if(getIntent().getExtras()==null||
                (id=getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID))==AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
            finish();
        }
        setResult(RESULT_CANCELED, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                Log.d(":-:","Config activity confirmed");
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list);
                ArrayList<String> res = new ArrayList<>();
                res.add("Station1");
                res.add("Station2");

                WidgetProvider.updateItems(getApplicationContext(), appWidgetManager, id, res);

                Log.d(":-:","Updating from AWM");
                appWidgetManager.updateAppWidget(id, views);

                setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id));
                finish();
                break;
            default:
                break;
        }
    }
}

