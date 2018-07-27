package com.gueg.velovwidget;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import static com.gueg.velovwidget.WidgetItem.DATABASE_NAME;

@Database(entities = {WidgetItem.class}, version = 1)
public abstract class WidgetItemsDatabase extends RoomDatabase {

    public abstract WidgetItemsDao widgetItemsDao();

    private static WidgetItemsDatabase INSTANCE;


    public static WidgetItemsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WidgetItemsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WidgetItemsDatabase.class, DATABASE_NAME)
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}