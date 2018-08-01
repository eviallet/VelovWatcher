package com.gueg.velovwidget;


import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Converter {

    @TypeConverter
    public static WidgetItem.Position fromJsonToPosition(String positionJson) {
        return new Gson().fromJson(positionJson, new TypeToken<WidgetItem.Position>() {}.getType());
    }

    @TypeConverter
    public static String fromPositionToJson(WidgetItem.Position position) {
        return new Gson().toJson(position);
    }

}
