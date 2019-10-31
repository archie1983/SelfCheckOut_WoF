package com.example.selfcheckout_wof.custom_components.componentActions;

import android.content.Context;

import com.example.selfcheckout_wof.AdminActivity;
import com.example.selfcheckout_wof.DataAdminActivity;
import com.example.selfcheckout_wof.custom_components.exceptions.AdminActivityNotReady;
import com.example.selfcheckout_wof.custom_components.utils.SalesItemsCache;
import com.example.selfcheckout_wof.data.AppDatabase;
import com.example.selfcheckout_wof.data.DBThread;
import com.example.selfcheckout_wof.data.SalesItems;

/**
 * A class with overridable methods to define functionality for admininstration
 * of sales items in the db- to fill the editable area and update and delete
 * the record.
 */
public class AdmSalesItemAction {

    SalesItems salesItem;
    Context context;

    public AdmSalesItemAction(SalesItems salesItem, Context context) {
        this.salesItem = salesItem;
        this.context = context;
    }

    /**
     * Loads the selected sales item into the appropriate fields in the AdminActvity form.
     */
    public void selectSalesItemForEdit() {
        final DataAdminActivity adminActivity;

        /*
         * First check that we even have an instance of the AdminActivity
         * active right now because we'll need it for everything else.
         */
        try {
            adminActivity = DataAdminActivity.getInstance();
        } catch (AdminActivityNotReady exc) {
            /*
             * If Admin activity has not yet been opened, then we have no business
             * deleting anything and should not continue (we won't have DB instance
             * anyway).
             */
            return;
        }

        adminActivity.loadExistingSalesItemForEdit(salesItem);
    }

    /**
     * Deletes an admin sales item from the db and reloads the sales items admin view.
     */
    public void deleteSalesItem() {
        final DataAdminActivity adminActivity;

        /*
         * First check that we even have an instance of the AdminActivity
         * active right now because we'll need it for everything else.
         */
        try {
            adminActivity = DataAdminActivity.getInstance();
        } catch (AdminActivityNotReady exc) {
            /*
             * If Admin activity has not yet been opened, then we have no business
             * deleting anything and should not continue (we won't have DB instance
             * anyway).
             */
            return;
        }

        final AppDatabase db = SalesItemsCache.getDBInstance();
        /**
         * Deleting a sales item and updating the sales item list in the admin
         * section in a separate thread because Room doesn't allow running
         * db stuff on the main thread.
         */
        DBThread.addTask(new Runnable() {
            @Override
            public void run() {
                db.salesItemsDao().deleteByID(salesItem.si_id);
                adminActivity.updateSalesItemsListView();
            }
        });
    }
}
