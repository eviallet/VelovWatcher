package com.gueg.velovwidget;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class JsonParser {

    public static final String CONTRACT_NAME_LYON = "Lyon";
    private static final String API_URL_UPDATE_STATION = "https://api.jcdecaux.com/vls/v1/stations/{station_number}?contract={contract_name}&apiKey={api_key}";
    private static final String API_SCHEME_STATION_NUMBER = "{station_number}";
    private static final String API_SCHEME_CONTRACT_NAME = "{contract_name}";
    private static final String API_SCHEME_API_KEY = "{api_key}";
    private static String API_KEY;
    public static boolean IS_API_KEY_LOADED = false;

    public static void loadApiKey(Context context) {
        try {
            InputStream in = context.getAssets().open("api_key.txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            API_KEY = total.toString();
            IS_API_KEY_LOADED = true;

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static WidgetItem updateDynamicalDataFromApi(WidgetItem item) {
        return new UpdateItemTask().doInBackground(item);
    }

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

            for(WidgetItem item : items)
                item.name = item.name.substring(item.name.indexOf("-")+1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    private static DynamicData loadDynamicDataFromJson(String data) {
        return new Gson().fromJson(data, new TypeToken<DynamicData>(){}.getType());
    }

    // https://stackoverflow.com/a/37525989/8308507
    private static class UpdateItemTask extends AsyncTask<WidgetItem, Void, WidgetItem> {
        @Override
        public WidgetItem doInBackground(WidgetItem... items) {
            WidgetItem item = items[0];

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(completeFields(item));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("Response: ", "> " + line);
                }

                item.data = loadDynamicDataFromJson(buffer.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return item;
        }
    }

    private static String completeFields(WidgetItem item) {
        return API_URL_UPDATE_STATION
                .replace(API_SCHEME_STATION_NUMBER,Integer.toString(item.number))
                .replace(API_SCHEME_CONTRACT_NAME,CONTRACT_NAME_LYON)
                .replace(API_SCHEME_API_KEY,API_KEY);
    }

}
