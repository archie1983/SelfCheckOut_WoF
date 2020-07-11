package com.example.selfcheckout_wof.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SalesItems.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SalesItemsDao salesItemsDao();
    public abstract StoredBTDevicesDao storedBTDevicesDao();
}