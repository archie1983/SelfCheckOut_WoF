package com.example.selfcheckout_wof.custom_components.componentActions;

import android.content.Context;

import com.example.selfcheckout_wof.AdminActivity;
import com.example.selfcheckout_wof.custom_components.exceptions.AdminActivityNotReady;
import com.example.selfcheckout_wof.data.AppDatabase;
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

    public void deleteSalesItem() {
        final AdminActivity adminActivity;

        try {
            adminActivity = AdminActivity.getInstance();
        } catch (AdminActivityNotReady exc) {
            /*
             * If Admin activity has not yet been opened, then we have no business
             * deleting anything and should not continue (we won't have DB instance
             * anyway).
             */
            return;
        }

        final AppDatabase db = adminActivity.getDBInstance(context);
        /**
         * Have to do it in a separate thread because Room doesn't allow running
         * db stuff on the main thread.
         */
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                db.salesItemsDao().deleteByID(salesItem.si_id);
                adminActivity.updateSalesItemsListView();
            }
        });

        t.start();
    }
}
