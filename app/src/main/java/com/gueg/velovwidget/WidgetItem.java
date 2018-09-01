package com.gueg.velovwidget;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.gueg.velovwidget.database_stations.Converter;
import com.gueg.velovwidget.database_stations.DynamicData;

import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.gueg.velovwidget.WidgetItem.DATABASE_NAME;


@Entity(tableName = DATABASE_NAME, primaryKeys = {"number", "name"})
public class WidgetItem {
    public static final String DATABASE_NAME = "widget_items";
    public static final String CONTRACT_NAME = "com.gueg.velovwatcher.sharedprefs.contract_name";
    public static final String NO_CONTRACT_NAME = "com.gueg.velovwatcher.sharedprefs.contract_name.none";

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSED";

    @NonNull
    public Integer number;
    public String contract_name;
    @NonNull
    public String name;
    public String address;
    @TypeConverters(Converter.class)
    public Position position;

    public boolean isPinned;
    public int rank;


    @Ignore
    public DynamicData data;

    public WidgetItem(@NonNull Integer number, String contract_name, @NonNull String name, String address, Position position, boolean isPinned, int rank) {
        this.number = number;
        this.contract_name = contract_name;
        this.name = name;
        this.address = address;
        this.position = position;
        this.isPinned = isPinned;
        this.rank = rank;
    }

    public boolean isOpen() {
        return data!=null&&data.status.equals(STATUS_OPEN);
    }



    @Override
    public String toString() {
        return "number = "+number+" - name = "+name+" - address = "+address+" - latitude = "+position.lat+" - longitude = "+position.lng+" - isPinned = "+isPinned+" - rank = "+rank;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof WidgetItem && ((WidgetItem) other).name.equals(name) && ((WidgetItem) other).number.equals(number);
    }

    public static BoundingBox getBoundaries(ArrayList<WidgetItem> items) {
        double latMin = items.get(0).position.lat;
        double latMax = items.get(0).position.lat;
        double lngMin = items.get(0).position.lng;
        double lngMax = items.get(0).position.lng;

        for(WidgetItem item : items) {
            if(item.position.lat<latMin)
                latMin = item.position.lat;
            else if(item.position.lat>latMax)
                latMax = item.position.lat;
            if(item.position.lng<lngMin)
                lngMin = item.position.lng;
            else if(item.position.lng>lngMax)
                lngMax = item.position.lng;
        }
        return new BoundingBox(latMax, lngMax, latMin, lngMin);
    }

    // https://stackoverflow.com/a/36595905/8308507
    public static ArrayList<WidgetItem> sort(ArrayList<WidgetItem> items) {
        items = removeDuplicates(items);
        Collections.sort(items, new Comparator<WidgetItem>() {
            @Override public int compare(WidgetItem w1, WidgetItem w2) {
                if(w1.rank==-1&&w2.rank!=-1)
                    return 1;
                return w1.rank - w2.rank; // Ascending
            }
        });
        return items;
    }


    public static ArrayList<WidgetItem> removeDuplicates(ArrayList<WidgetItem> items) {
		// TODO why are there duplicates in a first place?
        for(int i=0; i<items.size(); i++) {
            for(int j=0; j<items.size(); j++){
                if(i!=j && items.get(i).equals(items.get(j))) {
                    items.remove(i);
                    i=0;
                    j=0;
                }
            }
        }
        return items;
    }


    public static WidgetItem findByName(ArrayList<WidgetItem> items, String name) {
        for(WidgetItem item : items)
            if(item.name.equals(name))
                return item;
        return null;
    }

    public static String getSelectedContract(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(CONTRACT_NAME, NO_CONTRACT_NAME);
    }

    public static class Position {
        public double lat;
        public double lng;
    }
}
