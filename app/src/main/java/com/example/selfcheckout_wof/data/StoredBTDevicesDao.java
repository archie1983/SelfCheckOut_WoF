package com.example.selfcheckout_wof.data;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StoredBTDevicesDao {
    @Query("SELECT * FROM StoredBTDevices")
    List<SalesItems> getAll();

    @Query("SELECT * FROM StoredBTDevices WHERE dev_type=1")
    StoredBTDevices getZJPrinter();

    @Query("SELECT * FROM StoredBTDevices WHERE dev_type=2")
    StoredBTDevices getPPHCardReader();

    @Insert
    void insertAll(StoredBTDevices... devices);

    @Delete
    void delete(StoredBTDevices device);

    @Update
    void update(StoredBTDevices device);
}
