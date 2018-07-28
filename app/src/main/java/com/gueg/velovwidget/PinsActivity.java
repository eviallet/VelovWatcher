package com.gueg.velovwidget;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.arch.persistence.room.RoomDatabase;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PinsActivity extends AppCompatActivity {


    private static final int PERMISSION_REQUEST_WRITE = 0;
    private static final int PERMISSION_REQUEST_LOCALISATION = 1;

    private boolean storagePermissionGranted = true;
    private boolean localisationPermissionGranted = true;
    private int id;

    private MapView map;

    private ArrayList<WidgetItem> items = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_pins);

        // Delete db and load from json
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                WidgetItemsDatabase.getDatabase(getApplicationContext()).widgetItemsDao().deleteAll();
                Log.d(":-:","Activity - DB deleted");
            }
        }).start();
        */

        map = findViewById(R.id.activity_pins_map);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE);
            storagePermissionGranted = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCALISATION);
            localisationPermissionGranted = false;
        }

        if(getIntent().getExtras()==null||
                (id=getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID))==AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
            finish();
        }
        setResult(RESULT_CANCELED, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id));

        try {
            items.addAll(new WidgetItemsDatabase.DatabaseLoader.AllItems().execute(this).get());
            Log.d(":-:","Activity - Loaded "+items.size()+" stations from database");
        } catch (ExecutionException |InterruptedException e) {
            e.printStackTrace();
        }

        if(items.size()==0) {
            items.addAll(JsonParser.loadFromJson(this));
            new WidgetItemsDatabase.DatabaseLoader.WriteItems(this, items).start();
            Log.d(":-:", "Activity - Loaded " + items.size() + " stations from json");
        }

        if(storagePermissionGranted&&localisationPermissionGranted)
            loadMap();
    }

    private void loadMap() {
        // Map configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // Position overlay
        MyLocationNewOverlay mapOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        mapOverlay.enableMyLocation();
        map.getOverlays().add(mapOverlay);

        // Enable rotation
        /*
        RotationGestureOverlay rotation = new RotationGestureOverlay(map);
        rotation.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(rotation);*/

        //the overlay
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<>(
                this,
                WidgetItem.toOverlayItems(items),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        WidgetItem widget = WidgetItem.getWidgetItemFromOverlayItem(items, item);
                        if(widget!=null) {
                            new WidgetItemsDatabase.DatabaseLoader.TogglePinnedItem(getApplicationContext(), widget).start();
                            return true;
                        }
                        return false;
                    }
        });
        mOverlay.setFocusItemsOnTap(true);

        map.getOverlays().add(mOverlay);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_pins_confirm:
                Log.d(":-:","Activity - Confirmed");

                setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id));
                finish();
                break;
            default:
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    storagePermissionGranted = true;
                    if(localisationPermissionGranted)
                        loadMap();
                }
                break;
            case PERMISSION_REQUEST_LOCALISATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    localisationPermissionGranted = true;
                    if(storagePermissionGranted)
                        loadMap();
                }
                break;
        }
    }



    public void onResume(){
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}

