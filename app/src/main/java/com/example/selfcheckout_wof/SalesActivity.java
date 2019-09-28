package com.example.selfcheckout_wof;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.SelectionGUIForOrder;
import com.example.selfcheckout_wof.custom_components.UsersSelectedChoice;
import com.example.selfcheckout_wof.custom_components.componentActions.ActionForSelectionGUI;
import com.example.selfcheckout_wof.data.AppDatabase;
import com.example.selfcheckout_wof.data.DBThread;
import com.example.selfcheckout_wof.data.PurchasableGoods;
import com.example.selfcheckout_wof.data.SalesItems;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SalesActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private AppCompatActivity thisActivity = null;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mShowContentRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowControlsRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sales);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.vContentLayout);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.btnGoToAdmin).setOnTouchListener(mDelayHideTouchListener);

//        findViewById(R.id.btnGoToAdmin).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent showAdminActivity = new Intent(thisActivity, AdminActivity.class);
//                startActivity(showAdminActivity);
//            }
//        });
    }

    public void onGoToAdminClick(View view) {
        Intent showAdminActivity = new Intent(this, AdminActivity.class);
        startActivity(showAdminActivity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

        displayPage(1);

        thisActivity = this;
    }

    /**
     * Displays the requrested page of sales items. It tries
     * to create as uniform grid as possible.
     *
     * If page_number < 1, then displaying main categories.
     *
     * @param page_number
     */
    private void displayPage(final int page_number) {
        /**
         * Updating the admin list of sales items in a separate thread because
         * Room doesn't allow running db stuff on the main thread.
         */
        DBThread.addTask(new Runnable() {
            @Override
            public void run() {
                final AppDatabase db = AdminActivity.getDBInstance(getApplicationContext());
                List<SalesItems> salesItemsList;
                if (db != null) {

                    /*
                     * if page number is less than one, then we want the top categories
                     * otherwise we want the specified page.
                     */
                    if (page_number < 1) {
                        salesItemsList = db.salesItemsDao().loadTopCategories();
                    } else {
                        salesItemsList = db.salesItemsDao().loadPage(page_number);
                    }

                    int item_count = salesItemsList.size();
                    /*
                     * making sure that the row size will be as close as possible to the column count
                     */
                    int number_of_items_per_row = (int) Math.ceil(Math.sqrt(item_count));

                    /*
                     * We'll find the layout where we want to put the items and start
                     * putting in there horizontal layouts and adding a calculated number
                     * of items in each of the horizontal layouts (rows)
                     */
                    final LinearLayout vContentLayout = (LinearLayout)findViewById(R.id.vContentLayout);
                    int items_displayed = 0;

                    while (item_count > items_displayed) {
                        final LinearLayout hItemsRow = new LinearLayout(getApplicationContext());
                        hItemsRow.setOrientation(LinearLayout.HORIZONTAL);

                        for (int cnt = 0; cnt < number_of_items_per_row; cnt++) {
                            /*
                             * Because each SalesItems item is a PurchasableGoods (implements the interface),
                             * we can use it as PurchasableGoods and pass into the constructor
                             * of SelectionGUIForOrder.
                             */
                            final PurchasableGoods pg = salesItemsList.get(items_displayed);
                            hItemsRow.addView(
                                    new SelectionGUIForOrder(
                                            pg,
                                            new ActionForSelectionGUI(pg),
                                            false,
                                            false,
                                            getApplicationContext()
                                    )
                            );
                            items_displayed++;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                vContentLayout.addView(hItemsRow);
                            }
                        });
                    }
                }
            }
        });
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowControlsRunnable);
        mHideHandler.postDelayed(mShowContentRunnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mShowContentRunnable);
        mHideHandler.postDelayed(mShowControlsRunnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
