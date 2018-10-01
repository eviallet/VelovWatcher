package com.gueg.velovwidget.map;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.gueg.velovwidget.R;
import com.gueg.velovwidget.Item;
import com.gueg.velovwidget.database_stations.JsonParser;
import com.gueg.velovwidget.database_stations.WidgetItemsDatabase;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

// TODO warn on low bikes/stands available on chosen hours ?
// TODO collect stats about available bikes/stands on chosen stations and show results on a small graph on popup ?

public class PinsActivity extends AppCompatActivity {


    private static final int PERMISSION_REQUEST_WRITE = 0;
    private static final int PERMISSION_REQUEST_LOCALISATION = 1;
    private MapView map;
    private ArrayList<Overlay> currentMarkersOverlays;



    private ArrayList<Item> items = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);


        if(Item.getSelectedContract(this).equals(Item.NO_CONTRACT_NAME)) {
            final ListView contractListView = new ListView(this);
            try {
                final ArrayList<String> contracts = new JsonParser.GetContractList().execute().get();
                contractListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contracts));
                contractListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        showConfirmation(contracts.get(i));
                    }
                });
                setContentView(contractListView);
            } catch (InterruptedException|ExecutionException e) {
                e.printStackTrace();
            }
        } else
            checkExternalStoragePermission();
    }

    private void checkExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE);
        } else
            checkLocalisationPermission();
    }

    private void checkLocalisationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCALISATION);
        } else
            loadActivity();
    }

    private void loadActivity() {
        setContentView(R.layout.activity_pins);


        map = findViewById(R.id.activity_pins_map);

        try {
            items.addAll(new WidgetItemsDatabase.DatabaseLoader.AllItems().execute(this, Item.getSelectedContract(this)).get());
        } catch (ExecutionException |InterruptedException e) {
            e.printStackTrace();
        }

        if(items.size()==0) {
            items.addAll(JsonParser.loadStationsFromContract(Item.getSelectedContract(this)));
            new WidgetItemsDatabase.DatabaseLoader.WriteItems(this, items).start();
        }

        if(items.size()==0){
            Toast.makeText(this, getResources().getString(R.string.toast_no_station_found), Toast.LENGTH_SHORT).show();
            resetCurrentContract();
        }


        loadMap();
    }


    private void loadMap() {
        // Map configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // Position overlay
        MyLocationNewOverlay mapOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        mapOverlay.enableMyLocation();
        GeoPoint myLocation = mapOverlay.getMyLocation();
        map.getOverlays().add(mapOverlay);

        // Add overlay
        currentMarkersOverlays = OverlayProvider.setFastOverlay(map, items);
        map.getOverlays().addAll(currentMarkersOverlays);
        OverlayProvider.setOnPinsChangedListener(new OverlayProvider.OnPinsChanged() {
            @Override
            public void onPinToggled() {
                map.getOverlays().removeAll(currentMarkersOverlays);
                currentMarkersOverlays = OverlayProvider.setFastOverlay(map, items);
                map.getOverlays().addAll(currentMarkersOverlays);
            }
        });


        // Limit scrollable area
        map.setScrollableAreaLimitDouble(Item.getBoundaries(items));

        // Set initial position
        IMapController mapController = map.getController();
        if(myLocation!=null)
            mapController.setCenter(myLocation);
        else
            mapController.setCenter(Item.getBoundaries(items).getCenter());
        mapController.setZoom(18.0d);

    }





    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocalisationPermission();
                } else
                    finish();
                break;
            case PERMISSION_REQUEST_LOCALISATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadActivity();
                }
                break;
        }
    }





    public void onResume(){
        super.onResume();
        if(map!=null) {
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
            map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        }
    }

    public void onPause(){
        super.onPause();
        if(map!=null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Configuration.getInstance().save(this, prefs);
            map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
        }
    }




    private void showConfirmation(final String choice) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(Item.CONTRACT_NAME, choice).apply();
                        checkExternalStoragePermission();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.activity_pins_choose)+" "+choice+" ?").setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
    }




    private void resetCurrentContract() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().remove(Item.CONTRACT_NAME).apply();
        recreate();
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.activity_pins_confirm:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.activity_pins_change_city:
                resetCurrentContract();
                break;
        }
    }



}

