package com.gueg.velovwidget.database_stations;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.gueg.velovwidget.Item;

import java.util.ArrayList;
import java.util.List;

import static com.gueg.velovwidget.Item.DATABASE_NAME;
import static com.gueg.velovwidget.MainListActivity.IS_DATABASE_BUSY;

@Database(entities = {Item.class}, version = 2, exportSchema = false)
public abstract class WidgetItemsDatabase extends RoomDatabase {

    public abstract WidgetItemsDao widgetItemsDao();

    private static WidgetItemsDatabase INSTANCE;


    public static WidgetItemsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WidgetItemsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WidgetItemsDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();

                }
            }
        }
        return INSTANCE;
    }



    public static class DatabaseLoader {

        public static class AllItems extends AsyncTask<Object, Void, ArrayList<Item>> {
            @Override
            protected final ArrayList<Item> doInBackground(Object... objs) {
                return new ArrayList<>(WidgetItemsDatabase.getDatabase((Context)objs[0]).widgetItemsDao().getAll((String)objs[1]));
            }
        }

        public static class PinnedItems extends AsyncTask<Object, Void, ArrayList<Item>> {
            @Override
            protected final ArrayList<Item> doInBackground(Object... objs){
                return new ArrayList<>(WidgetItemsDatabase.getDatabase((Context)objs[0]).widgetItemsDao().getAllPinned((String)objs[1]));
            }
        }

        public static class WriteItems extends Thread {
            Context c;
            List<Item> items;

            public WriteItems(Context c, List<Item> items) {
                this.c = c;
                this.items = items;
            }

            @Override
            public void run() {
                PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(IS_DATABASE_BUSY, true).apply();
                WidgetItemsDatabase.getDatabase(c).widgetItemsDao().insertAll(items.toArray(new Item[items.size()]));
                PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(IS_DATABASE_BUSY, false).apply();
            }
        }

        public static class TogglePinnedItem extends Thread {
            Context c;
            Item item;

            public TogglePinnedItem(Context c, Item item) {
                this.c = c;
                this.item = item;
            }

            @Override
            public void run() {
                PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(IS_DATABASE_BUSY, true).apply();
                item.isPinned = !item.isPinned;
                WidgetItemsDatabase.getDatabase(c.getApplicationContext()).widgetItemsDao().updateItems(item);
                PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(IS_DATABASE_BUSY, false).apply();
            }
        }


        public static class UpdateItems extends Thread {
            Context c;
            ArrayList<Item> items;
            boolean sort = false;

            public UpdateItems(Context c, ArrayList<Item> items) {
                this.c = c;
                this.items = items;
            }

            public UpdateItems(Context c, ArrayList<Item> items, boolean sort) {
                this.c = c;
                this.items = items;
                this.sort = sort;
            }

            @Override
            public void run() {
                PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(IS_DATABASE_BUSY, true).apply();
                List<Item> pinned = WidgetItemsDatabase.getDatabase(c.getApplicationContext()).widgetItemsDao().getAllPinned(Item.getSelectedContract(c));
                if(!sort) {
                    for (Item i : pinned)
                        if (items.contains(i))
                            items.remove(i);
                }
                WidgetItemsDatabase.getDatabase(c.getApplicationContext()).widgetItemsDao().updateItems(items.toArray(new Item[items.size()]));
                PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(IS_DATABASE_BUSY, false).apply();
            }
        }
    }

}