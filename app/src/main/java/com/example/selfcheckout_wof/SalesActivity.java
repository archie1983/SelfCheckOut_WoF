package com.example.selfcheckout_wof;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.example.selfcheckout_wof.custom_components.SalesProcessNavigationFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sales);

        /*
         * When we create this activity, we start with the base page
         */
        displayAvailableSalesItemsForEdit(0, 0, false);

        contentView = findViewById(R.id.frmSalesItemsListBrowse);
    }

    /**
     * Creates (or reloads) the two fragments, that I have for displaying sales items
     * navigation buttons and sales items selection page.
     * @param pageNumber page number to load
     * @param parentID parent, whose page to load
     * @param seeMeal a flag of whether user needs to be shown the actual meal instead of a
     *                page with items to select.
     */
    private void displayAvailableSalesItemsForEdit(int pageNumber, int parentID, boolean seeMeal){
        final FragmentManager fm = getSupportFragmentManager();

        final SalesProcessNavigationFragment navigationFragment =
                SalesProcessNavigationFragment.newInstance(pageNumber, parentID, true, seeMeal);

        final SalesProcessNavigationFragment dataFragment =
                SalesProcessNavigationFragment.newInstance(pageNumber, parentID, false, seeMeal);

        fm.beginTransaction()
                .replace(R.id.frmSalesItemsListNavigation, navigationFragment, "si_nav")
                .commit();

        fm.beginTransaction()
                .replace(R.id.frmSalesItemsListBrowse, dataFragment, "si_list")
                .commit();
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
        if (process == SalesProcessNavigationFragment.SalesProcesses.LOAD_PAGE) {
            displayAvailableSalesItemsForEdit(pageNumber, parentId, false);
        } else if (process == SalesProcessNavigationFragment.SalesProcesses.SEE_MEAL) {
            displayAvailableSalesItemsForEdit(pageNumber, parentId, true);
        }
    }
}
