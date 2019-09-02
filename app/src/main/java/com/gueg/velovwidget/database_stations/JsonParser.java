package com.gueg.velovwidget.database_stations;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gueg.velovwidget.Item;

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
import java.util.concurrent.ExecutionException;

public class JsonParser {

    private static final String API_URL_CONTRACT_LIST = "https://api.jcdecaux.com/vls/v3/contracts?&apiKey={api_key}";
    private static final String API_URL_UPDATE_STATION = "https://api.jcdecaux.com/vls/v3/stations/{station_number}?contract={contract_name}&apiKey={api_key}";
    private static final String API_URL_GET_STATIONS = "https://api.jcdecaux.com/vls/v3/stations?contract={contract_name}&apiKey={api_key}";
    private static final String API_SCHEME_STATION_NUMBER = "{station_number}";
    private static final String API_SCHEME_CONTRACT_NAME = "{contract_name}";
    private static final String API_SCHEME_API_KEY = "{api_key}";
    private static String API_KEY;



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

        } catch(IOException e) {
            e.printStackTrace();
        }
    }






    private static ArrayList<String> loadContractsFromJson(String json) {
        ArrayList<Contract> contracts = new Gson().fromJson(json, new TypeToken<Collection<Contract>>(){}.getType());
        ArrayList<String> res = new ArrayList<>(contracts.size());
        for(Contract c : contracts)
            res.add(c.name);
        return res;
    }

    public static class GetContractList extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        public ArrayList<String> doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(API_URL_CONTRACT_LIST.replace(API_SCHEME_API_KEY, API_KEY));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");

                return loadContractsFromJson(buffer.toString());


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
            return null;
        }
    }


    private class Contract {
        public String name;
    }










    public static ArrayList<Item> loadStationsFromContract(String contract) {
        ArrayList<Item> items = null;
        try {
            items = new Gson().fromJson(
                    new GetStationsJsonFromContract().execute(contract).get(),
                    new TypeToken<Collection<Item>>() {
                    }.getType());

            for (Item item : items) {
                if(item.name.contains("-"))
                    item.name = item.name.substring(item.name.indexOf("-") + 1);
                item.rank = -1;
            }
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static class GetStationsJsonFromContract extends AsyncTask<String, Void, String> {
        @Override
        public String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(API_URL_GET_STATIONS.replace(API_SCHEME_API_KEY, API_KEY).replace(API_SCHEME_CONTRACT_NAME, strings[0]));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");


                return buffer.toString();


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
            return null;
        }
    }






    public static Item updateDynamicDataFromApi(Item item) {
        try {
            item = new UpdateItemTask().execute(item).get();
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
        }
        return item;
    }

    // https://stackoverflow.com/a/37525989/8308507
    private static class UpdateItemTask extends AsyncTask<Item, Void, Item> {
        @Override
        public Item doInBackground(Item... items) {
            Item item = items[0];

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

                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");

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

    private static DynamicData loadDynamicDataFromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONObject mainStands = json.getJSONObject("mainStands");
            JSONObject availabilities = mainStands.getJSONObject("availabilities");
            return new DynamicData(
                    json.getString("status"),
                    mainStands.getInt("capacity"),
                    availabilities.getInt("bikes"),
                    availabilities.getInt("stands"),
                    json.getString("lastUpdate"),
                    json.getBoolean("connected")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String completeFields(Item item) {
        return API_URL_UPDATE_STATION
                .replace(API_SCHEME_STATION_NUMBER,Integer.toString(item.number))
                .replace(API_SCHEME_CONTRACT_NAME,item.contractName)
                .replace(API_SCHEME_API_KEY,API_KEY);
    }

}
