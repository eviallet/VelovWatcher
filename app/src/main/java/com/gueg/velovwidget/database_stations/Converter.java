package com.gueg.velovwidget.database_stations;


import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gueg.velovwidget.Item;

public class Converter {

    @TypeConverter
    public static Item.Position fromJsonToPosition(String positionJson) {
        return new Gson().fromJson(positionJson, new TypeToken<Item.Position>() {}.getType());
    }

    @TypeConverter
    public static String fromPositionToJson(Item.Position position) {
        return new Gson().toJson(position);
    }

}
