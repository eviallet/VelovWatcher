package com.gueg.velovwidget.database_stations;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.gueg.velovwidget.Item;

import java.util.List;

import static com.gueg.velovwidget.Item.DATABASE_NAME;

@Dao
public interface WidgetItemsDao {
    @Query("SELECT * FROM "+DATABASE_NAME+" WHERE contractName LIKE :contract")
    List<Item> getAll(String contract);

    @Query("SELECT * FROM "+DATABASE_NAME+" WHERE isPinned LIKE 1 AND contractName LIKE :contract")
    List<Item> getAllPinned(String contract);

    @Insert
    void insertAll(Item... items);

    @Update
    void updateItems(Item... items);

    @Delete
    void delete(Item items);

    @Query("DELETE FROM "+DATABASE_NAME)
    void deleteAll();
}
