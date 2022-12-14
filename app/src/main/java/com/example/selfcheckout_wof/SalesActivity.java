package com.example.selfcheckout_wof;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.selfcheckout_wof.custom_components.SalesProcessNavigationFragment;
import com.example.selfcheckout_wof.custom_components.UsersSelectedChoice;
import com.example.selfcheckout_wof.custom_components.componentActions.ActionForSelectionGUI;
import com.example.selfcheckout_wof.custom_components.componentActions.ConfiguredMeal;
import com.example.selfcheckout_wof.custom_components.utils.CheckOutDBCache;
import com.example.selfcheckout_wof.data.DBThread;
import com.example.selfcheckout_wof.data.PurchasableGoods;
import com.example.selfcheckout_wof.data.SalesItems;

import java.util.Iterator;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SalesActivity extends AppCompatActivity
        implements SalesProcessNavigationFragment.OnFragmentInteractionListener {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler sysControlsHideHandler = new Handler();

    /**
     * Parent ID for items that have no parent (the top level items e.g. Food, Drink, etc.).
     */
    public static final int TOP_LEVEL_ITEMS = 0;

    /**
     * A return value for the intent that launches payment activity.
     */
    public static final int INVOICE_PAYMENT = 1;

    private View contentView;
    private final Runnable mShowContentRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    public void onPay(View view) {
        Intent serverIntent = new Intent(SalesActivity.this, SelfCheckoutChargeActivity.class);
        serverIntent.putExtra(SelfCheckoutChargeActivity.INTENT_TRANX_TOTAL_AMOUNT, getOrderTotalAsDouble());
        startActivityForResult(serverIntent, INVOICE_PAYMENT);
    }

    /**
     * Finds out the price of the current order and returns it as a double, which we need in the
     * SelfCheckoutChargeActivity.
     * @return
     */
    private double getOrderTotalAsDouble() {
        Iterator<ConfiguredMeal> mealsInCurrentOrder = UsersSelectedChoice.getCurrentOrder();

        /**
         * If there's not meals in the order, then we don't need to print anything.
         */
        if (!mealsInCurrentOrder.hasNext()) {
            return 0.0;
        }

        /*
         * Going through the current order and preparing items one line at a time for each
         * meal.
         */
        int orderTotal = 0;
        int mealTotal = 0;

        /*
         * Looking at the each meal in the order.
         */
        while (mealsInCurrentOrder.hasNext()) {
            ConfiguredMeal currentMealInOrder = mealsInCurrentOrder.next();
            if (currentMealInOrder.getCurrentMealItems() != null) {
                /*
                 * Now let's go through the items in each meal.
                 */
                for (PurchasableGoods pg : currentMealInOrder.getCurrentMealItems()) {
                    mealTotal += pg.getPrice();
                }

                orderTotal += mealTotal;
                mealTotal = 0;
            }
        }

        return orderTotal / 100.0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sales);

        /*
         * When we create this activity, we start with the base page
         */
        displayAvailableSalesItemsOrCurrentOrder(0, TOP_LEVEL_ITEMS, SalesProcessNavigationFragment.SalesProcesses.LOAD_PAGE);

        contentView = findViewById(R.id.frmSalesItemsListBrowse);
    }

    /**
     * Creates (or reloads) the two fragments, that I have for displaying sales items
     * navigation buttons and sales items selection page.
     * @param pageNumber page number to load
     * @param parentID parent, whose page to load
     * @param process indicator of whether user needs to be shown the actual meal instead of a
     *                page with items to select or the final order.
     */
    private void displayAvailableSalesItemsOrCurrentOrder(int pageNumber,
                                                   int parentID,
                                                   SalesProcessNavigationFragment.SalesProcesses process){
        final FragmentManager fm = getSupportFragmentManager();

        if (process == SalesProcessNavigationFragment.SalesProcesses.SEE_ORDER) {
            /**
             * The fragment that shows the current selection and allows to check out at any time.
             */
            final SalesProcessNavigationFragment seeMealFragment =
                    SalesProcessNavigationFragment.newInstance(pageNumber, parentID, true, process);

            fm.beginTransaction()
                    .replace(R.id.frmSalesItemsListSeeMeal, seeMealFragment, "si_see_meal")
                    .commit();
        } else if (process == SalesProcessNavigationFragment.SalesProcesses.LOAD_PAGE) {
            /**
             * The fragment that allows to make choices for the meal.
             */
            final SalesProcessNavigationFragment dataFragment =
                    SalesProcessNavigationFragment.newInstance(pageNumber, parentID, false, process);

            fm.beginTransaction()
                    .replace(R.id.frmSalesItemsListBrowse, dataFragment, "si_list")
                    .commit();
        } else if (process == SalesProcessNavigationFragment.SalesProcesses.GO_TO_CHECKOUT) {
            /**
             * The fragment that allows to make choices for the meal.
             */
            final SalesProcessNavigationFragment dataFragment =
                    SalesProcessNavigationFragment.newInstance(pageNumber, parentID, false, process);

            /*
             * Now we will load the part where we normally put items that are available on menu,
             * now we will load there the final order that the user has chosen and offer a checkout
             * option there.
             */
            fm.beginTransaction()
                    .replace(R.id.frmSalesItemsListBrowse, dataFragment, "si_list")
                    .commit();
        }
    }

    public void onGoToAdminClick(View view) {
        Intent showAdminActivity = new Intent(this, AdminActivity.class);
        startActivity(showAdminActivity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Hide the system controls because we want user to use only our interface.
        hideSystemControls();
    }

    /**
     * Hides the normal system navigation controls (back, home, show processes)
     * in a way that's most optimal to the Android versions covered by the app.
     */
    private void hideSystemControls() {
//        sysControlsHideHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                ActionBar actionBar = getSupportActionBar();
//                if (actionBar != null) {
//                    actionBar.hide();
//                }
//
//                sysControlsHideHandler.postDelayed(mShowContentRunnable, UI_ANIMATION_DELAY);
//            }
//        }, 100);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        sysControlsHideHandler.postDelayed(mShowContentRunnable, UI_ANIMATION_DELAY);
    }

    @Override
    public void onFragmentInteraction(SalesProcessNavigationFragment.SalesProcesses process, int pageNumber, int parentId) {
        displayAvailableSalesItemsOrCurrentOrder(pageNumber, parentId, process);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i("key pressed", String.valueOf(event.getKeyCode()));
        return super.dispatchKeyEvent(event);
    }

    private static String scannedCode = "";
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                scannedCode += "1";
                return true;
            case KeyEvent.KEYCODE_2:
                scannedCode += "2";
                return true;
            case KeyEvent.KEYCODE_3:
                scannedCode += "3";
                return true;
            case KeyEvent.KEYCODE_4:
                scannedCode += "4";
                return true;
            case KeyEvent.KEYCODE_5:
                scannedCode += "5";
                return true;
            case KeyEvent.KEYCODE_6:
                scannedCode += "6";
                return true;
            case KeyEvent.KEYCODE_7:
                scannedCode += "7";
                return true;
            case KeyEvent.KEYCODE_8:
                scannedCode += "8";
                return true;
            case KeyEvent.KEYCODE_9:
                scannedCode += "9";
                return true;
            case KeyEvent.KEYCODE_0:
                scannedCode += "0";
                return true;
            case KeyEvent.KEYCODE_ENTER:
                Log.i("scanned code", scannedCode);
                Toast.makeText(getApplicationContext(), scannedCode, Toast.LENGTH_SHORT).show();

                DBThread.addTask(new Runnable() {
                     @Override
                     public void run() {
                         SalesItems sales_item = CheckOutDBCache.getInstance().getSalesItemByID(scannedCode);
                         ActionForSelectionGUI.addSelectedItem(sales_item);
                         scannedCode = "";

                         displayAvailableSalesItemsOrCurrentOrder(0, SalesActivity.TOP_LEVEL_ITEMS, SalesProcessNavigationFragment.SalesProcesses.SEE_ORDER);
                         displayAvailableSalesItemsOrCurrentOrder(0, SalesActivity.TOP_LEVEL_ITEMS, SalesProcessNavigationFragment.SalesProcesses.LOAD_PAGE);
                     }
                 });

                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
}
