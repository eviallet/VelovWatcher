package com.gueg.velovwidget.database_stations;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gueg.velovwidget.WidgetItem;

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

    private static final String API_URL_CONTRACT_LIST = "https://api.jcdecaux.com/vls/v1/contracts?&apiKey={api_key}";
    private static final String API_URL_UPDATE_STATION = "https://api.jcdecaux.com/vls/v1/stations/{station_number}?contract={contract_name}&apiKey={api_key}";
    private static final String API_URL_GET_STATIONS = "https://api.jcdecaux.com/vls/v1/stations?contract={contract_name}&apiKey={api_key}";
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

                Log.d(":-:","GetContractsList - Loaded "+loadContractsFromJson(buffer.toString()).size()+" contracts");

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










    public static ArrayList<WidgetItem> loadStationsFromContract(String contract) {
        ArrayList<WidgetItem> items = null;
        try {
            items = new Gson().fromJson(
                    new GetStationsJsonFromContract().execute(contract).get(),
                    new TypeToken<Collection<WidgetItem>>() {
                    }.getType());

            for (WidgetItem item : items) {
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






    public static WidgetItem updateDynamicDataFromApi(WidgetItem item) {
        return new UpdateItemTask().doInBackground(item);
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

    private static DynamicData loadDynamicDataFromJson(String data) {
        return new Gson().fromJson(data, new TypeToken<DynamicData>(){}.getType());
    }

    private static String completeFields(WidgetItem item) {
        return API_URL_UPDATE_STATION
                .replace(API_SCHEME_STATION_NUMBER,Integer.toString(item.number))
                .replace(API_SCHEME_CONTRACT_NAME,item.contract_name)
                .replace(API_SCHEME_API_KEY,API_KEY);
    }

}
