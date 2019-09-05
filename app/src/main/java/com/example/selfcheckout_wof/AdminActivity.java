package com.example.selfcheckout_wof;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.selfcheckout_wof.custom_components.AdmSalesItemsListFragment;
import com.example.selfcheckout_wof.custom_components.EditSalesItemFragment;
import com.example.selfcheckout_wof.custom_components.exceptions.AdminActivityNotReady;
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

    /*
     * A static reference to itself so that we can access it from elsewhere
     * in the application and call functions like getDBInstance(...)
     * and updateSalesItemsListView().
     */
    private static AdminActivity self = null;

    public static AdminActivity getInstance() throws AdminActivityNotReady {
        if (self == null) {
            throw new AdminActivityNotReady();
        }
        return self;
    }

    /**
     * A reference to the database. We'll initialise it in the getDBInstance(...) function
     */
    private AppDatabase db_instance = null;

    /**
     * Returns the database instance. This function has to be public,
     * because we will be accessing AdminActivity in a static context
     * and then using this function to get db instance so that we can
     * use db functionality elsewhere in application.
     *
     * @param context
     * @return
     */
    public AppDatabase getDBInstance(Context context) {
        if (db_instance == null) {
            db_instance = Room.databaseBuilder(context,
                    AppDatabase.class, "sales-items").build();
        }

        return db_instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * We could initialise the self reference in a constructor- the singelton
         * way, but it's probably ok at least for now to set it here instead.
         */
        self = this;
        setContentView(R.layout.activity_admin);
    }

    /**
     * When the admin activity loads, we want to show all the current
     * categories.
     */
    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Have to do it in a separate thread because Room doesn't allow running
         * db stuff on the main thread.
         */
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                updateSalesItemsListView();
            }
        });

        t.start();
    }

    /**
     * When we select the image for the category, we'll want
     * its uri. This is where we'll store a string representation
     * of the URI. This will be initialised in the "onActivityResult(...)"
     * method.
     */
    String selected_image_uri = "";
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
                db.salesItemsDao().insertAll(SalesItems.createTopCategory(mainCatText, selected_image_uri));
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
     *
     * It is also public, because we'll want to include in AdmSalesItemAction,
     * which we'll create in AdmSalesItemsListFragment and pass to AdmSalesItemView.
     */
    public void updateSalesItemsListView() {
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
     * Implementation of a function to ensure interaction with this activity
     * from the loaded fragment.
     *
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {
        // Nothing to do yet. Functionality not required atm.
    }

    /**
     * What happens when we press the button to select a picture for the category.
     *
     * @param v
     */
    public void onCategoryPictureSelect(View v) {
        performFileSearch();
    }

    /**
     * A constant for image opening intent
     */
    private static final int READ_REQUEST_CODE = 42;

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * Catches the callback from the file picker and gives us the URI of
     * the file selected.
     *
     * @param requestCode
     * @param resultCode
     * @param resultData
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                //Log.i(TAG, "Uri: " + uri.toString());
                //showImage(uri);
                ImageView iv = ((ImageView)findViewById(R.id.imgCategoryPicture));
                iv.setImageURI(uri);
                selected_image_uri = uri.toString();
            }
        }
    }
}
