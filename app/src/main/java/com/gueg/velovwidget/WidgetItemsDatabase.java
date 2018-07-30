package com.gueg.velovwidget;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gueg.velovwidget.WidgetItem.DATABASE_NAME;

@Database(entities = {WidgetItem.class}, version = 2)
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

        public static class AllItems extends AsyncTask<Context, Void, ArrayList<WidgetItem>> {
            @Override
            protected ArrayList<WidgetItem> doInBackground(Context... contexts) {
                return new ArrayList<>(WidgetItemsDatabase.getDatabase(contexts[0]).widgetItemsDao().getAll());
            }
        }

        public static class PinnedItems extends AsyncTask<Context, Void, ArrayList<WidgetItem>> {
            @Override
            protected ArrayList<WidgetItem> doInBackground(Context... contexts) {
                return new ArrayList<>(WidgetItemsDatabase.getDatabase(contexts[0]).widgetItemsDao().getAllPinned());
            }
        }

        public static class WriteItems extends Thread {
            Context c;
            List<WidgetItem> items;

            WriteItems(Context c, List<WidgetItem> items) {
                this.c = c;
                this.items = items;
            }

            @Override
            public void run() {
                WidgetItemsDatabase.getDatabase(c).widgetItemsDao().insertAll(items.toArray(new WidgetItem[items.size()]));
            }
        }

        public static class TogglePinnedItem extends Thread {
            Context c;
            WidgetItem item;

            TogglePinnedItem(Context c, WidgetItem item) {
                this.c = c;
                this.item = item;
            }

            @Override
            public void run() {
                item.isPinned = !item.isPinned;
                WidgetItemsDatabase.getDatabase(c.getApplicationContext()).widgetItemsDao().updateItems(item);
            }
        }
    }
}