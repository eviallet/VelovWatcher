package com.gueg.velovwidget;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.util.ArrayList;

public class OverlayProvider {

    private static Marker CURRENT_INFOWINDOW = null;

    private static Marker getMarkerFromWidgetItem(final MapView map, final WidgetItem item) {
        final Marker m = new Marker(map);
        m.setRelatedObject(item);
        m.setTitle(item.name);
        m.setSubDescription(item.address);
        if(!item.isPinned)
            m.setIcon(map.getContext().getResources().getDrawable(R.drawable.marker_hidden));
        else
            m.setIcon(map.getContext().getResources().getDrawable(R.drawable.marker_hidden_pinned));

        m.setPosition(new GeoPoint(item.position.lat, item.position.lng));
        m.setInfoWindow(new MarkerInfoWindow(R.layout.map_info_window, map));
        m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                marker.getInfoWindow().getView().findViewById(R.id.bubble_favorite).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        WidgetItem wi = (WidgetItem)m.getRelatedObject();
                        new WidgetItemsDatabase.DatabaseLoader.TogglePinnedItem(map.getContext().getApplicationContext(), wi).start();
                        Toast.makeText(map.getContext().getApplicationContext(), "Station "+wi.name.toLowerCase()+(wi.isPinned?" supprimée.":" suivie."), Toast.LENGTH_SHORT).show();

                        if(wi.isPinned)
                            ((ImageView)m.getInfoWindow().getView().findViewById(R.id.bubble_favorite)).setImageDrawable(map.getContext().getResources().getDrawable(R.drawable.ic_favorite));
                        else
                            ((ImageView)m.getInfoWindow().getView().findViewById(R.id.bubble_favorite)).setImageDrawable(map.getContext().getResources().getDrawable(R.drawable.ic_favorite_border));
                    }
                });
                marker.showInfoWindow();
                return true;
            }
        });
        if(item.isPinned)
            ((ImageView)m.getInfoWindow().getView().findViewById(R.id.bubble_favorite)).setImageDrawable(map.getContext().getResources().getDrawable(R.drawable.ic_favorite));
        return m;
    }

    public static ArrayList<Overlay> setFastOverlay(final MapView map, final ArrayList<WidgetItem> items) {
        ArrayList<Overlay> overlays = new ArrayList<>();

        ArrayList<IGeoPoint> points = new ArrayList<>();
        ArrayList<IGeoPoint> pinned = new ArrayList<>();
        for (WidgetItem item : items) {
            if(!item.isPinned)
                points.add(new LabelledGeoPoint(item.position.lat, item.position.lng, item.name));
            else
                pinned.add(new LabelledGeoPoint(item.position.lat, item.position.lng, item.name));
        }

        // wrap them in a theme
        SimplePointTheme pt = new SimplePointTheme(points, true);
        SimplePointTheme pnd = new SimplePointTheme(pinned, true);

        // create label style
        Paint textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);
        textStyle.setColor(map.getContext().getResources().getColor(R.color.mapStationsColor));
        textStyle.setTextAlign(Paint.Align.CENTER);
        textStyle.setTextSize(30);
        textStyle.setFakeBoldText(true);


        Paint pinnedTextStyle = new Paint();
        pinnedTextStyle.setStyle(Paint.Style.FILL);
        pinnedTextStyle.setColor(map.getContext().getResources().getColor(R.color.mapStationsPinnedColor));
        pinnedTextStyle.setTextAlign(Paint.Align.CENTER);
        pinnedTextStyle.setTextSize(30);
        pinnedTextStyle.setFakeBoldText(true);

        // set some visual options for the overlay
        // we use here MAXIMUM_OPTIMIZATION algorithm, which works well with >100k points
        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(13).setIsClickable(true).setTextStyle(textStyle)
                .setLabelPolicy(SimpleFastPointOverlayOptions.LabelPolicy.DENSITY_THRESHOLD).setMaxNShownLabels(20)
                .setPointStyle(textStyle);
        SimpleFastPointOverlayOptions optpnd = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(13).setIsClickable(true).setTextStyle(pinnedTextStyle)
                .setLabelPolicy(SimpleFastPointOverlayOptions.LabelPolicy.DENSITY_THRESHOLD).setMaxNShownLabels(20)
                .setPointStyle(pinnedTextStyle);

        // create the overlay with the theme
        final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pt, opt);
        final SimpleFastPointOverlay sfpopnd = new SimpleFastPointOverlay(pnd, optpnd);

        SimpleFastPointOverlay.OnClickListener listener = new SimpleFastPointOverlay.OnClickListener() {
            @Override
            public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {
                final WidgetItem item = WidgetItem.findByName(items, ((LabelledGeoPoint) points.get(point)).getLabel());
                if(item!=null) {
                    final Marker m = getMarkerFromWidgetItem(map, item);
                    map.getOverlays().add(m);
                    m.getInfoWindow().getView().findViewById(R.id.bubble_favorite).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            WidgetItem wi = (WidgetItem) m.getRelatedObject();
                            new WidgetItemsDatabase.DatabaseLoader.TogglePinnedItem(map.getContext().getApplicationContext(), wi).start();
                            Toast.makeText(map.getContext().getApplicationContext(), "Station " + wi.name.toLowerCase() + (wi.isPinned ? " supprimée." : " suivie."), Toast.LENGTH_SHORT).show();

                            if (wi.isPinned)
                                ((ImageView) m.getInfoWindow().getView().findViewById(R.id.bubble_favorite)).setImageDrawable(map.getContext().getResources().getDrawable(R.drawable.ic_favorite));
                            else
                                ((ImageView) m.getInfoWindow().getView().findViewById(R.id.bubble_favorite)).setImageDrawable(map.getContext().getResources().getDrawable(R.drawable.ic_favorite_border));

                            _listener.onPinToggled();
                        }
                    });
                    if (item.isPinned)
                        ((ImageView) m.getInfoWindow().getView().findViewById(R.id.bubble_favorite)).setImageDrawable(map.getContext().getResources().getDrawable(R.drawable.ic_favorite));
                    m.showInfoWindow();

                    if(CURRENT_INFOWINDOW!=null)
                        CURRENT_INFOWINDOW.closeInfoWindow();
                    CURRENT_INFOWINDOW = m;
                }
            }
        };

        // onClick callback
        sfpo.setOnClickListener(listener);
        sfpopnd.setOnClickListener(listener);

        overlays.add(sfpo);
        overlays.add(sfpopnd);

        return overlays;
    }

    private static OnPinsChanged _listener;

    public static void setOnPinsChangedListener(OnPinsChanged listener) {
        _listener = listener;
    }

    public interface OnPinsChanged {
        void onPinToggled();
    }


}
