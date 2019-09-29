package com.example.selfcheckout_wof;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.room.Room;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.example.selfcheckout_wof.custom_components.AdmSalesItemView;
import com.example.selfcheckout_wof.custom_components.AdmSalesItemsListFragment;
import com.example.selfcheckout_wof.custom_components.EditSalesItemFragment;
import com.example.selfcheckout_wof.custom_components.componentActions.AdmSalesItemAction;
import com.example.selfcheckout_wof.custom_components.exceptions.AdminActivityNotReady;
import com.example.selfcheckout_wof.custom_components.utils.SalesItemsCache;
import com.example.selfcheckout_wof.data.AppDatabase;
import com.example.selfcheckout_wof.data.DBThread;
import com.example.selfcheckout_wof.data.SalesItems;

import java.util.ArrayList;
import java.util.Iterator;
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
        EditSalesItemFragment.OnFragmentInteractionListener, AdapterView.OnItemSelectedListener {

    /*
     * A static reference to itself so that we can access it from elsewhere
     * in the application and call functions like updateSalesItemsListView().
     */
    private static AdminActivity self = null;

    public static AdminActivity getInstance() throws AdminActivityNotReady {
        if (self == null) {
            throw new AdminActivityNotReady();
        }
        return self;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        displayAvailableSalesItemsForEdit();

        /*
         * We could initialise the self reference in a constructor- the singelton
         * way, but it's probably ok at least for now to set it here instead.
         */
        self = this;
        setContentView(R.layout.activity_admin);

        /*
         * Setting up listener for a spinner that we'll later use to select parent categories.
         */
        Spinner spnParentCategories = (Spinner)findViewById(R.id.spnParentCategories);
        spnParentCategories.setOnItemSelectedListener(this);
    }

    /**
     * When the admin activity loads, we want to show all the current
     * categories.
     */
    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Updating the admin list of sales items in a separate thread because
         * Room doesn't allow running db stuff on the main thread.
         */
        DBThread.addTask(new Runnable() {
            @Override
            public void run() {
                updateSalesItemsListView();
            }
        });
    }

    /**
     * When we select the image for the category, we'll want
     * its uri. This is where we'll store a string representation
     * of the URI. This will be initialised in the "onActivityResult(...)"
     * method.
     */
    String selected_image_uri = "";

    /**
     * ID of the selected parent category for this entry.
     */
    long selected_parent_category = -1;

    /**
     * Selected SalesItems object
     */
    SalesItems selectedSalesItem = null;

    /**
     * This is what happens when we press the "Add main category" button
     *
     * @param view
     */
    public void onAddOrEditMainCategory(View view) {
        String mainCatText = ((EditText)findViewById(R.id.txtCatLabel)).getText().toString();
        String itemDescription = ((EditText)findViewById(R.id.txtDescription)).getText().toString();

        long price;
        try {
            price = Long.parseLong(((EditText)findViewById(R.id.txtPrice)).getText().toString());
        } catch (NumberFormatException exc) {
            price = 0;
        }

        int page;
        try {
            page = Integer.parseInt(((EditText)findViewById(R.id.txtPage)).getText().toString());
        } catch (NumberFormatException exc) {
            page = 1;
        }

        int multi_choice_number;
        try {
            multi_choice_number = Integer.parseInt(((EditText)findViewById(R.id.txtMultiChoiceNumber)).getText().toString());
        } catch (NumberFormatException exc) {
            multi_choice_number = 1;
        }

        final AppDatabase db = SalesItemsCache.getDBInstance();

        /*
         * If we have a loaded sales item, then we want to update it with the new
         * values, otherwise we want to create a new one.
         */
        if (selectedSalesItem != null) {
            /**
             * Saving changes to a sales item and updating list in a separate thread because
             * Room doesn't allow running db stuff on the main thread.
             */
            selectedSalesItem.label = mainCatText;
            selectedSalesItem.pictureUrl = selected_image_uri;
            selectedSalesItem.parentCategoryId = selected_parent_category;
            selectedSalesItem.numberOfMultiSelectableItems = multi_choice_number;
            selectedSalesItem.price = price;
            selectedSalesItem.page = page;
            selectedSalesItem.description = itemDescription;

            /*
             * Creating a copy of the modified sales item, because we're passing
             * it to a thread, which may well execute after the modified sales item
             * has already been nulled
             */
            final SalesItems tmp_salesItem = new SalesItems(selectedSalesItem);

            DBThread.addTask(new Runnable() {
                @Override
                public void run() {
                db.salesItemsDao().update(tmp_salesItem);
                updateSalesItemsListView();
                }
            });
        } else {
            final SalesItems newSalesItem = SalesItems.createCategory(
                    mainCatText,
                    selected_image_uri,
                    selected_parent_category,
                    itemDescription,
                    multi_choice_number,
                    price,
                    page);
            /**
             * Adding a sales item and updating list in a separate thread because
             * Room doesn't allow running db stuff on the main thread.
             */
            DBThread.addTask(new Runnable() {
                @Override
                public void run() {
                db.salesItemsDao().insertAll(
                    newSalesItem
                );
                updateSalesItemsListView();
                }
            });
        }

        clearSalesItemEditFields();
    }

    public void onCancelCategoryEdit(View view) {
        clearSalesItemEditFields();
    }

    /**
     * Clears the Sales Item edit fields.
     */
    private void clearSalesItemEditFields() {
        /*
         * Category name and other text fields
         */
        ((EditText)findViewById(R.id.txtCatLabel)).setText("");
        ((EditText)findViewById(R.id.txtMultiChoiceNumber)).setText("");
        ((EditText)findViewById(R.id.txtPage)).setText("");
        ((EditText)findViewById(R.id.txtPrice)).setText("");
        ((EditText)findViewById(R.id.txtDescription)).setText("");

        /*
         * Parent category
         */
        final Spinner spnParentCategories = (Spinner)findViewById(R.id.spnParentCategories);
        spnParentCategories.setSelection(0);
        selected_parent_category = -1;

        /*
         * Selected sales item (category)
         */
        selectedSalesItem = null;

        /*
         * Image
         */
        selected_image_uri = "";
        ImageView iv = ((ImageView)findViewById(R.id.imgCategoryPicture));
        iv.setImageResource(R.drawable.dragndrop);

        /*
         * The "save sales item (or category)" button now needs to be called "Add category"
         * and not "Save category"
         */
        final Button btnAddOrEditCategory = (Button)findViewById(R.id.btnAddOrEditCategory);
        btnAddOrEditCategory.setText(R.string.btnAddMainCategory);

        /*
         * In the end make the delete button disappear if there's nothing to delete
         */
        final Button btnDeleteCategory = (Button)findViewById(R.id.btnDeleteCategory);
        btnDeleteCategory.setEnabled(false);
        btnDeleteCategory.setVisibility(View.INVISIBLE);

        /*
         * Now makes sure that all items are un-selected
         */
        AdmSalesItemView.unselectAllSelectedItems();
    }

    /**
     * This is what happens when we press the "Delete category" button
     *
     * @param view
     */
    public void onDeleteCategory(View view) {
        AdmSalesItemAction action = new AdmSalesItemAction(selectedSalesItem, this);
        action.deleteSalesItem();

        clearSalesItemEditFields();
    }

    /**
     * Loads an existing sales item into the edit fields of the AdminActivity
     * to make it ready for editing.
     *
     * @param salesItem
     */
    public void loadExistingSalesItemForEdit(SalesItems salesItem) {
        selectedSalesItem = salesItem;

        /*
         * Label
         */
        EditText txtCatLabel = findViewById(R.id.txtCatLabel);
        txtCatLabel.setText(salesItem.label);

        /*
         * price
         */
        EditText txtPrice = findViewById(R.id.txtPrice);
        txtPrice.setText(salesItem.price + "");

        /*
         * page
         */
        EditText txtPage = findViewById(R.id.txtPage);
        txtPage.setText(salesItem.page + "");

        /*
         * description
         */
        EditText txtDescription = ((EditText)findViewById(R.id.txtDescription));
        txtDescription.setText(salesItem.description);

        /*
         * number of multi selectable items on the page
         */
        EditText txtMultiChoiceNumber = ((EditText)findViewById(R.id.txtMultiChoiceNumber));
        txtMultiChoiceNumber.setText(salesItem.numberOfMultiSelectableItems + "");

        /*
         * Image
         */
        selected_image_uri = salesItem.pictureUrl;
        ImageView iv = ((ImageView)findViewById(R.id.imgCategoryPicture));

        if (selected_image_uri != null && !selected_image_uri.equals("")) {
            Uri uri = Uri.parse(salesItem.pictureUrl);
            //iv.setImageURI(uri);
            /*
             * Using Glide to save memory for image loads
             */
            Glide.with(this).load(uri).into(iv);
        } else {
            /*
             * Using Glide to save memory for image loads
             */
            Glide.with(this).load(R.drawable.dragndrop).into(iv);
            //iv.setImageResource(R.drawable.dragndrop);
        }

        /*
         * Parent category
         */
        final Spinner spnParentCategories = (Spinner)findViewById(R.id.spnParentCategories);

        /*
         * First we'll find the parent sales item that we need to display in the spinner
         */
        SalesItems siToDisplayInSpinner = null;

        for (SalesItems si : SalesItemsCache.getInstance().getSalesItemsList()) {
            if (si.si_id == salesItem.parentCategoryId) {
                siToDisplayInSpinner = si;
            }
        }

        /*
         * Now we want its index. The index in the spinner will be almost the same
         * as in the salesItemsParents collection, because they're populated in a
         * similar way.
         *
         * "Similar way" here means, that the cursor for the spinner gets the
         * categories, that don't have parents AND one extra dummy category called
         * "NO PARENT", which is the first one (so index 0).
         *
         * The salesItemsParents list gets just the categories that don't have parents.
         * So if we get here: salesItemsParents.indexOf(siToDisplayInSpinner) == -1, then
         * we actually want the 0 index category (the dummy one) displayed. If we get
         * any other index, then we want to display here the category, whose index is
         * 1 higher.
         */
        spnParentCategories.setSelection(SalesItemsCache.getInstance().getSalesItemsParents().indexOf(siToDisplayInSpinner) + 1);

        /*
         * We'll also want the "delete category" button available
         */
        final Button btnDeleteCategory = (Button)findViewById(R.id.btnDeleteCategory);
        btnDeleteCategory.setEnabled(true);
        btnDeleteCategory.setVisibility(View.VISIBLE);

        /*
         * The "Add category" button needs to now be called "Save category" as we're
         * in edit mode
         */
        final Button btnAddOrEditCategory = (Button)findViewById(R.id.btnAddOrEditCategory);
        btnAddOrEditCategory.setText(R.string.btnSaveCategory);
    }

    /**
     * After we've added or removed sales items, we'll want to update
     * the fragment, where we're showing them. This function will do that.
     *
     * It is also public, because we'll want to use it in AdmSalesItemAction
     * (when deleting a sales item), which we'll create in AdmSalesItemsListFragment
     * and pass to AdmSalesItemView.
     *
     * NOTE: This method must be wrappped in a DBThread task, because it uses DB access.
     * The reason why it is not done so in the body of this method is to give more
     * flexibility in scheduling this task.
     */
    public void updateSalesItemsListView() {
        SalesItemsCache.getInstance().forceLoadSalesItemsList();

        /*
         * First re-populate the drop down box of main categories to select.
         */
        final Spinner spnParentCategories = (Spinner)findViewById(R.id.spnParentCategories);

        String[] adapterCols=new String[]{"item_label"};
        int[] adapterRowViews=new int[]{android.R.id.text1};

        final SimpleCursorAdapter scaParentCategories = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,//android.R.layout.simple_spinner_item
                SalesItemsCache.getInstance().getSalesItemsParentsForDropDownBox(),
                adapterCols,
                adapterRowViews,
                0);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spnParentCategories.setAdapter(scaParentCategories);
            }
        });
        displayAvailableSalesItemsForEdit();
    }

    /**
     * Creates (or reloads) the two fragments, that I have for displaying existing sales items
     * in a list that can be used to pick items for edit.
     */
    private void displayAvailableSalesItemsForEdit(){
        /*
         * Populate the admin fragment with the list of available sales items.
         * If we have already have an existing data fragment, then take that and reload its data.
         * If not, then we have to create a new data fragment and also a new header fragment.
         */
        FragmentManager fm = getSupportFragmentManager();
        AdmSalesItemsListFragment data_fragment = (AdmSalesItemsListFragment)fm.findFragmentByTag("adm_si_list");

        if (data_fragment != null) {
            data_fragment.loadData();
        } else {
            AdmSalesItemsListFragment header_fragment = AdmSalesItemsListFragment.newInstance(true);
            data_fragment = AdmSalesItemsListFragment.newInstance(false);

            fm.beginTransaction()
                .add(R.id.adm_sales_items_list_hdr_container, header_fragment, "adm_si_hdr")
                .commit();

            fm.beginTransaction()
                .add(R.id.adm_sales_items_list_container, data_fragment, "adm_si_list")
                .commit();
        }
    }

    /**
     * Upon selecting a parent category, we need to remember its id
     *
     * @param parent
     * @param view
     * @param pos
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selected_parent_category = id;
    }

    /**
     * When nothing is selected, we want the parent category to be -1
     *
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selected_parent_category = -1;
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
                //iv.setImageURI(uri);
                /*
                 * Using Glide to save memory for image loads
                 */
                Glide.with(this).load(uri).into(iv);
                selected_image_uri = uri.toString();
            }
        }
    }
}
