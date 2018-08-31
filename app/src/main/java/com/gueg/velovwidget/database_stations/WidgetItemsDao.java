package com.gueg.velovwidget.database_stations;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.gueg.velovwidget.WidgetItem;

import java.util.List;

import static com.gueg.velovwidget.WidgetItem.DATABASE_NAME;

@Dao
public interface WidgetItemsDao {
    @Query("SELECT * FROM "+DATABASE_NAME+" WHERE contract_name LIKE :contract")
    List<WidgetItem> getAll(String contract);

    @Query("SELECT * FROM "+DATABASE_NAME+" WHERE isPinned LIKE 1 AND contract_name LIKE :contract")
    List<WidgetItem> getAllPinned(String contract);

    @Insert
    void insertAll(WidgetItem... items);

    @Update
    void updateItems(WidgetItem... items);

    @Delete
    void delete(WidgetItem items);

    @Query("DELETE FROM "+DATABASE_NAME)
    void deleteAll();
}
