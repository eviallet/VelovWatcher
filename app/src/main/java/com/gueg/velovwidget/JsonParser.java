package com.gueg.velovwidget;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

public class JsonParser {
    public static ArrayList<WidgetItem> loadFromJson(Context context) {
        ArrayList<WidgetItem> items = new ArrayList<>();
        try {
            InputStream in = context.getAssets().open("Lyon.json");
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            items.addAll((ArrayList<WidgetItem>)new Gson().fromJson(total.toString(), new TypeToken<Collection<WidgetItem>>(){}.getType()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

}
