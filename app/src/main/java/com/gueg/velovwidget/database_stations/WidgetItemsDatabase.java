package com.gueg.velovwidget.database_stations;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;

import com.gueg.velovwidget.WidgetItem;

import java.util.ArrayList;
import java.util.List;

import static com.gueg.velovwidget.WidgetItem.DATABASE_NAME;

@Database(entities = {WidgetItem.class}, version = 2, exportSchema = false)
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

        public static class AllItems extends AsyncTask<Object, Void, ArrayList<WidgetItem>> {
            @Override
            protected final ArrayList<WidgetItem> doInBackground(Object... objs) {
                return new ArrayList<>(WidgetItemsDatabase.getDatabase((Context)objs[0]).widgetItemsDao().getAll((String)objs[1]));
            }
        }

        public static class PinnedItems extends AsyncTask<Object, Void, ArrayList<WidgetItem>> {
            @Override
            protected final ArrayList<WidgetItem> doInBackground(Object... objs){
                return new ArrayList<>(WidgetItemsDatabase.getDatabase((Context)objs[0]).widgetItemsDao().getAllPinned((String)objs[1]));
            }
        }

        public static class WriteItems extends Thread {
            Context c;
            List<WidgetItem> items;

            public WriteItems(Context c, List<WidgetItem> items) {
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

            public TogglePinnedItem(Context c, WidgetItem item) {
                this.c = c;
                this.item = item;
            }

            @Override
            public void run() {
                item.isPinned = !item.isPinned;
                WidgetItemsDatabase.getDatabase(c.getApplicationContext()).widgetItemsDao().updateItems(item);
            }
        }


        public static class UpdateItems extends Thread {
            Context c;
            ArrayList<WidgetItem> items;

            public UpdateItems(Context c, ArrayList<WidgetItem> items) {
                this.c = c;
                this.items = items;
            }

            @Override
            public void run() {
                WidgetItemsDatabase.getDatabase(c.getApplicationContext()).widgetItemsDao().updateItems(items.toArray(new WidgetItem[items.size()]));
            }
        }
    }

}