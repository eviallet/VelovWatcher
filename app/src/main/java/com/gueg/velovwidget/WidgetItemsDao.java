package com.gueg.velovwidget;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static com.gueg.velovwidget.WidgetItem.DATABASE_NAME;

@Dao
public interface WidgetItemsDao {
    @Query("SELECT * FROM "+DATABASE_NAME)
    List<WidgetItem> getAll();

    @Query("SELECT * FROM "+DATABASE_NAME+" WHERE name LIKE :search LIMIT 1")
    WidgetItem findByName(String search);

    @Insert
    void insertAll(WidgetItem... items);

    @Update
    void updateItems(WidgetItem... items);

    @Delete
    void delete(WidgetItem items);
}
