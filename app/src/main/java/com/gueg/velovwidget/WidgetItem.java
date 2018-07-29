package com.gueg.velovwidget;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import static com.gueg.velovwidget.WidgetItem.DATABASE_NAME;

@Entity(tableName = DATABASE_NAME)
public class WidgetItem {
    public static final String DATABASE_NAME = "widget_items";

    @PrimaryKey @NonNull
    public Integer number;
    public String name;
    public String address;
    public double latitude;
    public double longitude;

    public boolean isPinned;
    public int rank;


    @Ignore
    public DynamicData data;

    @Ignore
    public WidgetItem(@NonNull Integer number, String name, String address, double latitude, double longitude) {
        this(number, name, address, latitude, longitude, false, -1);
    }

    public WidgetItem(@NonNull Integer number, String name, String address, double latitude, double longitude, boolean isPinned, int rank) {
        this.number = number;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isPinned = isPinned;
        this.rank = rank;
    }


    public OverlayItem toOverlayItem() {
        return new OverlayItem(Integer.toString(number), name, address, new GeoPoint(latitude, longitude));
    }

    public boolean isEqual(OverlayItem other) {
        return number.equals(Integer.decode(other.getUid())) && name.equals(other.getTitle());
    }

    public static ArrayList<OverlayItem> toOverlayItems(ArrayList<WidgetItem> items) {
        ArrayList<OverlayItem> res = new ArrayList<>();
        for(WidgetItem wi : items)
            res.add(wi.toOverlayItem());
        return res;
    }

    public static WidgetItem getWidgetItemFromOverlayItem(ArrayList<WidgetItem> items, OverlayItem oi) {
        for(WidgetItem item : items)
            if(item.isEqual(oi))
                return item;
        return null;
    }

    @Override
    public String toString() {
        return "number = "+number+" - name = "+name+" - address = "+address+" - latitude = "+latitude+" - longitude = "+longitude+" - isPinned = "+isPinned+" - rank = "+rank;
    }

    public static BoundingBox getBoundaries(ArrayList<WidgetItem> items) {
        double latMin = items.get(0).latitude;
        double latMax = items.get(0).latitude;
        double lngMin = items.get(0).longitude;
        double lngMax = items.get(0).longitude;

        for(WidgetItem item : items) {
            if(item.latitude<latMin)
                latMin = item.latitude;
            else if(item.latitude>latMax)
                latMax = item.latitude;
            if(item.longitude<lngMin)
                lngMin = item.longitude;
            else if(item.longitude>lngMax)
                lngMax = item.longitude;
        }
        return new BoundingBox(latMax, lngMax, latMin, lngMin);
    }

}
