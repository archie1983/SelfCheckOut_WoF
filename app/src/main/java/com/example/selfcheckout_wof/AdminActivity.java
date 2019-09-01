package com.example.selfcheckout_wof;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.selfcheckout_wof.custom_components.AdmSalesItemsListFragment;
import com.example.selfcheckout_wof.custom_components.EditSalesItemFragment;
import com.example.selfcheckout_wof.data.AppDatabase;
import com.example.selfcheckout_wof.data.SalesItems;

import java.util.List;

/**
 * Activity for managing sales items (adding, removing, listing).
 *
 * It implements AdmSalesItemsListFragment.OnFragmentInteractionListener as per
 * default fragment contract, that Android Studio introduces, when adding a new
 * fragment.
 */
public class AdminActivity extends AppCompatActivity
        implements AdmSalesItemsListFragment.OnFragmentInteractionListener,
        EditSalesItemFragment.OnFragmentInteractionListener {

    /**
     * A static reference to the database. We'll initialise it in the getDBInstance(...) function
     */
    private static AppDatabase db_instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
    }

    /**
     * This is what happens when we press the "Add main category" button
     *
     * @param view
     */
    public void onAddMainCategory(View view) {
        final String mainCatText = ((EditText)findViewById(R.id.txtCatLabel)).getText().toString();
        final AppDatabase db = getDBInstance(getApplicationContext());

        /**
         * Have to do it in a separate thread because Room doesn't allow running
         * db stuff on the main thread.
         */
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                db.salesItemsDao().insertAll(SalesItems.createTopCategory(mainCatText));
                updateSalesItemsListView();
            }
        });

        t.start();
    }

    private static List<SalesItems> salesItemsList;

    public static List<SalesItems> getCurrentSalesItemsList() {
        return salesItemsList;
    }

    /**
     * After we've added or removed sales items, we'll want to update
     * the fragment, where we're showing them. This function will do that.
     */
    private void updateSalesItemsListView() {
        final AppDatabase db = getDBInstance(getApplicationContext());
        if (db != null) {
            salesItemsList = db.salesItemsDao().getAll();
        }

        AdmSalesItemsListFragment fragment = new AdmSalesItemsListFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.adm_sales_items_list_container, fragment)
                .commit();
    }

    /**
     * Returns the database instance in a static way so that we can
     * use this method elsewhere in application to get db functionality.
     *
     * @param context
     * @return
     */
    public static AppDatabase getDBInstance(Context context) {
        if (db_instance == null) {
            db_instance = Room.databaseBuilder(context,
                    AppDatabase.class, "sales-items").build();
        }

        return db_instance;
    }

    /**
     * Implementation of a function to ensure interaction with this activity
     * from the loaded fragment.
     *
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {
        // Nothing to do yet. Functionality not required atm.
    }
}
