package com.gueg.velovwidget;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.gueg.velovwidget.database_stations.Converter;
import com.gueg.velovwidget.database_stations.DynamicData;

import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.gueg.velovwidget.Item.DATABASE_NAME;


@Entity(tableName = DATABASE_NAME, primaryKeys = {"number", "name"})
public class Item {
    public static final String DATABASE_NAME = "widget_items";
    public static final String CONTRACT_NAME = "com.gueg.velovwatcher.sharedprefs.contractName";
    public static final String NO_CONTRACT_NAME = "com.gueg.velovwatcher.sharedprefs.contractName.none";

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSED";

    public static final Position POSITION_SEPARATOR = new Position(0,0);

    @NonNull
    public Integer number;
    public String contractName;
    @NonNull
    public String name;
    public String address;
    @TypeConverters(Converter.class)
    public Position position;

    public boolean isPinned;
    public int rank;


    @Ignore
    public DynamicData data;

    public Item(@NonNull Integer number, String contractName, @NonNull String name, String address, Position position, boolean isPinned, int rank) {
        this.number = number;
        this.contractName = contractName;
        this.name = name;
        this.address = address;
        this.position = position;
        this.isPinned = isPinned;
        this.rank = rank;
    }

    public boolean isOpen() {
        return data != null && data.status.equals(STATUS_OPEN);
    }
    public boolean isConnected() {
        return data != null && data.connected;
    }

    public boolean isSeparator() {
        return number==0&&position.latitude==POSITION_SEPARATOR.latitude&&position.longitude==POSITION_SEPARATOR.longitude;
    }

    public void setData(DynamicData d) {
        data = d;
    }


    @Override
    public String toString() {
        return "number = "+number+" - name = "+name+" - address = "+address+" - latitude = "+position.latitude+" - longitude = "+position.longitude+" - isPinned = "+isPinned+" - rank = "+rank;
    }

    public void toDebug() {
        Log.d(":-:", "name = "+name+" - data = "+(data==null?"null":Integer.toString(data.available_bikes))+" - isSeparator = "+isSeparator());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Item && ((Item) other).name.equals(name) && ((Item) other).number.equals(number);
    }

    public static BoundingBox getBoundaries(ArrayList<Item> items) {
        int i=0;
        double latMin=0;
        while(latMin==0&&i<items.size())
            latMin=items.get(i).position.latitude;
        i=0;
        double latMax=0;
        while(latMax==0&&i<items.size())
            latMax = items.get(0).position.latitude;
        i=0;
        double lngMin=0;
        while(lngMin==0&&i<items.size())
            lngMin = items.get(0).position.longitude;
        i=0;
        double lngMax=0;
        while(lngMax==0&&i<items.size())
            lngMax = items.get(0).position.longitude;

        for(Item item : items) {
            if (!item.isSeparator()) {
                if (item.position.latitude < latMin)
                    latMin = item.position.latitude;
                else if (item.position.latitude > latMax)
                    latMax = item.position.latitude;
                if (item.position.longitude < lngMin)
                    lngMin = item.position.longitude;
                else if (item.position.longitude > lngMax)
                    lngMax = item.position.longitude;
            }
        }
        return new BoundingBox(latMax, lngMax, latMin, lngMin);
    }

    // https://stackoverflow.com/a/36595905/8308507
    public static ArrayList<Item> sort(ArrayList<Item> items) {
        items = removeDuplicates(items);
        Collections.sort(items, new Comparator<Item>() {
            @Override public int compare(Item w1, Item w2) {
                if(w1.rank==-1&&w2.rank!=-1)
                    return 1;
                return w1.rank - w2.rank; // Ascending
            }
        });
        return items;
    }


    public static ArrayList<Item> removeDuplicates(ArrayList<Item> items) {
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


    public static Item findByName(ArrayList<Item> items, String name) {
        for(Item item : items)
            if(item.name.equals(name))
                return item;
        return null;
    }

    public static String getSelectedContract(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(CONTRACT_NAME, NO_CONTRACT_NAME);
    }

    public static class Position {
        public double latitude;
        public double longitude;

        Position(int latitude, int longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
