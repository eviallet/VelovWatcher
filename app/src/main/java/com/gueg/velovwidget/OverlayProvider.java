package com.gueg.velovwidget;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;
import org.osmdroid.views.util.constants.OverlayConstants;

import java.util.ArrayList;
import java.util.List;

public class OverlayProvider {


    /**
     * Pros :
     * - Easy popup integration when marker clicked
     * - Customizable popup
     * - Customizable icon on map
     * Cons :
     * - Slow loading (~2.5 s)
     */
    public static ArrayList<Overlay> setMarkersOverlay(final MapView map, ArrayList<WidgetItem> items) {
        ArrayList<Overlay> overlays = new ArrayList<>();
        for(WidgetItem item : items)
            overlays.add(getMarkerFromWidgetItem(map, item, true));

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Overlay m : map.getOverlays())
                    if(m instanceof Marker)
                        ((Marker) m).closeInfoWindow();
            }
        });
        return overlays;
    }

    private static Marker getMarkerFromWidgetItem(final MapView map, WidgetItem item, boolean shouldShowIcon) {
        final Marker m = new Marker(map);
        m.setRelatedObject(item);
        m.setTitle(item.name);
        m.setSubDescription(item.address);
        if(shouldShowIcon)
            m.setIcon(map.getContext().getResources().getDrawable(R.drawable.marker));
        else if(!item.isPinned)
            m.setIcon(map.getContext().getResources().getDrawable(R.drawable.marker_hidden));
        else
            m.setIcon(map.getContext().getResources().getDrawable(R.drawable.marker_hidden_pinned));

        m.setPosition(new GeoPoint(item.latitude, item.longitude));
        m.setInfoWindow(new MarkerInfoWindow(R.layout.map_info_window, map));
        m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
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


    /**
     * Pros :
     * - Easy click/long click actions
     * Cons :
     * - Non customizable popup
     */
    public static Overlay setItemizedOverlayWithFocus(final MapView map, final ArrayList<WidgetItem> items) {
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<>(
                WidgetItem.toOverlayItems(items),
                map.getContext().getResources().getDrawable(R.drawable.marker),
                map.getContext().getResources().getDrawable(R.drawable.marker_selected),
                OverlayConstants.NOT_SET,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        WidgetItem widget = WidgetItem.getWidgetItemFromOverlayItem(items, item);
                        if(widget!=null) {
                            new WidgetItemsDatabase.DatabaseLoader.TogglePinnedItem(map.getContext().getApplicationContext(), widget).start();
                            return true;
                        }
                        return false;
                    }
                },
                map.getContext()
        );
        mOverlay.setFocusItemsOnTap(true);

        return mOverlay;
    }


    /**
     * Pros :
     * - Very fast to load
     * Cons :
     * - No popup integrated
     * - No icons
     */
    public static ArrayList<Overlay> setFastOverlay(final MapView map, final ArrayList<WidgetItem> items) {
        ArrayList<Overlay> overlays = new ArrayList<>();

        ArrayList<IGeoPoint> points = new ArrayList<>();
        ArrayList<IGeoPoint> pinned = new ArrayList<>();
        for (WidgetItem item : items) {
            if(!item.isPinned)
                points.add(new LabelledGeoPoint(item.latitude, item.longitude, item.name));
            else
                pinned.add(new LabelledGeoPoint(item.latitude, item.longitude, item.name));
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
                    final Marker m = getMarkerFromWidgetItem(map, item, false);
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
