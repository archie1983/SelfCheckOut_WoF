package com.example.selfcheckout_wof.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SalesItems.class, StoredBTDevices.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SalesItemsDao salesItemsDao();
    public abstract StoredBTDevicesDao storedBTDevicesDao();
}