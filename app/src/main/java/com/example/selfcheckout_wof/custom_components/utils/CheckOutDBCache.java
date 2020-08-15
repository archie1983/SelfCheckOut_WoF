/**
 * This class is intended to be used for querying database for sales items and maintaining a cached
 * list of them. It extends Application, because we want the application context for Room database
 * access.
 *
 * This cached list of sales items will ensure that we always have a list to display in the
 * AdmSalesItemsListFragment class.
 */
package com.example.selfcheckout_wof.custom_components.utils;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.database.Cursor;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.selfcheckout_wof.data.AppDatabase;
import com.example.selfcheckout_wof.data.SalesItems;
import com.example.selfcheckout_wof.data.StoredBTDevices;

import java.util.ArrayList;
import java.util.List;

public class CheckOutDBCache extends Application {
    /**
     * A reference to the database. We'll initialise it in the getDBInstance(...) function
     */
    private static AppDatabase db_instance = null;

    /**
     * A collection of the sales items that we have at any given time.
     */
    private static List<SalesItems> salesItemsList = null;

    /**
     * A collection of the sales items parents (e.g. the top categories like: Food, drinks, etc)
     * that we have at any given time.
     */
    private static List<SalesItems> salesItemsParents = null;

    /**
     * Largely the same as the salesItemsParents, but it also includes a top category with
     * value of -1 and label of "Please select" or similar.
     */
    private static Cursor salesItemsParentsForDropDownBox = null;

    /**
     * Migration for when I added a new table for the BT devices.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE StoredBTDevices (" +
                    "dev_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "device_addr TEXT, " +
                    "device_name TEXT, " +
                    "dev_type INTEGER NOT NULL" +
                    ")");
        }
    };

    /**
     * Returns the database instance. This function has to be public,
     * because we will be accessing AdminActivity in a static context
     * and then using this function to get db instance so that we can
     * use db functionality elsewhere in application.
     *
     * @return
     */
    public static AppDatabase getDBInstance() {
        if (db_instance == null) {
            db_instance = Room.databaseBuilder(getContext(),
                    AppDatabase.class, "sales-items")
                    //.fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }

        return db_instance;
    }

    /**
     * Cached version of last used ZJ BT printer. To avoid extra DB accesses.
     */
    private static StoredBTDevices lastUsedZJ_BTPrinter;

    /**
     * Last used ZJ BT printer.
     */
    public StoredBTDevices getLastUsedZJ_BTPrinter() {
        if (lastUsedZJ_BTPrinter == null) {
            final AppDatabase db = getDBInstance();
            if (db != null) {
                lastUsedZJ_BTPrinter = db.storedBTDevicesDao().getZJPrinter();

                /**
                 * If this is null now, then that means we haven't got this record in the DB
                 * and need to create a dummy value now to avoid further lookups in the DB
                 * when we can't have them on the same thread as GUI.
                 */
                if (lastUsedZJ_BTPrinter == null) {
                    lastUsedZJ_BTPrinter = StoredBTDevices.createDevice("", "", StoredBTDevices.BTDeviceType.ZJ_PRINTER);
                }
            }
            return lastUsedZJ_BTPrinter;
        } else {
            return lastUsedZJ_BTPrinter;
        }
    }

    /**
     * Stores (if none registered before) or updates (if one exists) last used ZJ BT printer
     * device in the DB.
     *
     * @param device
     */
    public void storeLastUsedZJ_BTPrinter(BluetoothDevice device) {
        final AppDatabase db = getDBInstance();
        if (db != null) {
            if (lastUsedZJ_BTPrinter == null) {
                /*
                 * If it doesn't exist at all, then put in this one.
                 */
                lastUsedZJ_BTPrinter = StoredBTDevices.createDevice(device.getName(), device.getAddress(), StoredBTDevices.BTDeviceType.ZJ_PRINTER);
                db.storedBTDevicesDao().insertAll(lastUsedZJ_BTPrinter);
            } else {
                /*
                 * If one already exists, then update it if it's different.
                 */
                if (!lastUsedZJ_BTPrinter.deviceName.equals(device.getName()) ||
                        !lastUsedZJ_BTPrinter.deviceAddr.equals(device.getAddress())) {
                    lastUsedZJ_BTPrinter.deviceAddr = device.getAddress();
                    lastUsedZJ_BTPrinter.deviceName = device.getName();
                    db.storedBTDevicesDao().update(lastUsedZJ_BTPrinter);
                }
            }
        }
    }

    /**
     * Forces loading the sales items list from the database.
     */
    public void forceLoadSalesItemsList() {
        final AppDatabase db = getDBInstance();
        if (db != null) {
            //salesItemsList = db.salesItemsDao().getAll();
            salesItemsList = db.salesItemsDao().loadTopCategories();
            salesItemsParents = db.salesItemsDao().loadTopCategories();
            salesItemsParentsForDropDownBox = db.salesItemsDao().loadTopCategoriesForDropDownBox();
            /*
             * Here unfortunately we have no choice but to launch a DBThread and wait for it
             * to complete, becaues there is no point in continuing if items have not been loaded
             * from the database. To be able to pass a kind of a final boolean and then change its
             * value from the inside of the thread, I've used a boolean array of 1 item.
             */
//            final boolean[] db_loaded = {false};
//            DBThread.addTask(new Runnable() {
//                @Override
//                public void run() {
//                    salesItemsList = db.salesItemsDao().loadTopCategories();
//                    salesItemsParents = db.salesItemsDao().loadTopCategories();
//                    salesItemsParentsForDropDownBox = db.salesItemsDao().loadTopCategoriesForDropDownBox();
//                    db_loaded[0] = true;
//                }
//            });
//
//            while (!db_loaded[0]) {
//                //Thread.yield();
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException exc) {
//                    /*
//                     * If this thread is interrupted, then we probably want it to carry on
//                     * and eventually finish even if we have no database data.
//                     */
//                    db_loaded[0] = true;
//                }
//            }

            /*
             * Now we'll combine the top categories with the sub categories
             * in a depth-first style hierarchical structure.
             */
            ArrayList<SalesItems> tmp = new ArrayList<>(salesItemsList);
            for (SalesItems si : salesItemsList) {
                List<SalesItems> subCategories = db.salesItemsDao().loadSubCategory(si.si_id);
                int indexOfTopCategory = tmp.indexOf(si);
                if (indexOfTopCategory < tmp.size() - 1) {
                    tmp.addAll(indexOfTopCategory + 1, subCategories);
                } else {
                    tmp.addAll(subCategories);
                }
            }

            salesItemsList = tmp;
        }
    }

    /**
     * Returns a cached version of sales items list if it's available.
     * If not, it first reads it and then returns.
     * @return
     */
    public List<SalesItems> getSalesItemsList() {
        if (salesItemsList == null) {
            forceLoadSalesItemsList();
        }
        return salesItemsList;
    }

    /**
     * Returns a cached version of sales items list if it's available.
     * If not, it returns null. This is useful when we can't create
     * a new DBThread task because it's already nested into one.
     * @return
     */
    public List<SalesItems> getCachedSalesItemsList() {
        return salesItemsList;
    }

    /**
     * Returns a cached version of sales items parents if it's available.
     * If not, then it first reads the list and then returns it.
     * @return
     */
    public List<SalesItems> getSalesItemsParents() {
        if (salesItemsParents == null) {
            forceLoadSalesItemsList();
        }
        return salesItemsParents;
    }

    /**
     * Returns a cached version of sales items parents for drop down box if it's available.
     * If not, then it first reads the list and then returns it.
     * @return
     */
    public Cursor getSalesItemsParentsForDropDownBox() {
        if (salesItemsParentsForDropDownBox == null) {
            forceLoadSalesItemsList();
        }
        return salesItemsParentsForDropDownBox;
    }

    /**
     * Returns a page of sales items. Pages are set up in the admin section.
     * This is typically to display in user section (selecting items for sale).
     *
     * @param page_number page we want to load.
     * @param parent_id parent ID, which pages we want to load.
     * @return
     */
    public List<SalesItems> getSalesItemsPage(int page_number, int parent_id) {
        List<SalesItems> result = null;
        final AppDatabase db = getDBInstance();
        if (db != null) {
            /*
             * if page number is less than one, then we want the top categories
             * otherwise we want the specified page.
             */
            if (page_number < 1) {
                result = db.salesItemsDao().loadTopCategories();
            } else {
                result = db.salesItemsDao().loadPage(page_number, parent_id);
            }
        }
        return result;
    }

    /**
     * Returns number of salesitems on a given page. This is useful when deciding
     * whether next step is to show user the created meal (if returned 0) or the
     * next page (if returned more than 0).
     *
     * If databse is not accessible, this returns -1.
     *
     * @param page_number
     * @param parent_id
     * @return
     */
    public int getNumberOfItemsInPage(int page_number, int parent_id) {
        int result = -1;
        final AppDatabase db = getDBInstance();
        if (db != null) {
            result = db.salesItemsDao().numberOfItemsInPage(page_number, parent_id);
        }
        return result;
    }

    public SalesItems getSalesItemByID(int sid) {
        SalesItems salesItem = null;
        final AppDatabase db = getDBInstance();
        if (db != null) {
            salesItem = db.salesItemsDao().getSalesItem(sid);
        }
        return salesItem;
    }

    /**
     * Ensuring that we have an application context always available for
     * Room database access.
     */
    private static CheckOutDBCache instance;

    public static CheckOutDBCache getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}