package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.componentActions.ConfiguredMeal;
import com.example.selfcheckout_wof.custom_components.utils.Formatting;
import com.example.selfcheckout_wof.custom_components.utils.IntentFactory;
import com.example.selfcheckout_wof.data.PurchasableGoods;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A component to represent a selected meal by user. This will be useful
 * to visually show the contents of order.
 */
public class SelectedMealView extends LinearLayout {
    /**
     * Price of this meal.
     */
    int mealTotal = 0;

    /**
     * Are we editing meal displayed here?
     */
    boolean mealBeingEdited = false;

    /**
     * Edit button
     */
    Button btnEdit = null;

    /**
     * Cardview of this element - the main part of the GUI
     */
    CardView cvThisGUI;

    /**
     * Inside the CardView will be this vertical panel where we will add all the meal items.
     */
    LinearLayout vlContent;

    /**
     *
     * @param context
     * @param meal
     */
    public SelectedMealView(Context context, ConfiguredMeal meal) {
        super(context);
        init(meal);
    }

    /**
     * Builds the view.
     * @param meal
     */
    private void init(final ConfiguredMeal meal) {
        this.setOrientation(VERTICAL);
        Iterator<PurchasableGoods> itemsInMeal = null;

        /**
         * If we have anything to display, then we want to start setting up the card view and the
         * content panel.
         */
        if (meal != null && meal.getCurrentMealItems() != null && meal.getCurrentMealItems().size() > 0) {
            itemsInMeal = meal.getCurrentMealItems().iterator();
            if (itemsInMeal != null && itemsInMeal.hasNext()) {
                /*
                 * CardView setup
                 */
                cvThisGUI = new CardView(getContext());
                vlContent = new LinearLayout(getContext());
                vlContent.setOrientation(VERTICAL);

                vlContent.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.meal_not_being_edited));

                this.addView(cvThisGUI);
                cvThisGUI.addView(vlContent);

                LayoutParams layoutParams_cv = new LayoutParams(
                        LayoutParams.MATCH_PARENT, // CardView width
                        LayoutParams.WRAP_CONTENT // CardView height
                );
                // Set the card view content padding
                cvThisGUI.setContentPadding(5,5,5,5);
                // Set margins for card view
                layoutParams_cv.bottomMargin = 8;
                layoutParams_cv.setMarginEnd(8);
                layoutParams_cv.topMargin = 0;
                layoutParams_cv.setMarginStart(8);
                layoutParams_cv.gravity = Gravity.CENTER;
                //layoutParams_cv.height = 200;
                layoutParams_cv.width = 270;
                // Set the card view layout params
                cvThisGUI.setLayoutParams(layoutParams_cv);
                // Set the card view corner radius
                cvThisGUI.setRadius(8F);
                // Set card view elevation
                cvThisGUI.setCardElevation(8F);

                /*
                 * We'll be adding a new horizontal layout with the header label of the meal
                 * and a button or icon to allow editing that meal.
                 */
                final LinearLayout mealHdr = new LinearLayout(getContext());
                mealHdr.setOrientation(HORIZONTAL);

                final TextView header = new TextView(getContext());
                header.setText(meal.getMealName() + ":");
                header.setTextAppearance(R.style.mealTotal);
                mealHdr.addView(header);

                btnEdit = new Button(getContext());

                if (UsersSelectedChoice.isCurrentMealBeingEdited()
                        && UsersSelectedChoice.getNameOfMealBeingEdited().equals(meal.getMealName())) {
                    setMealEditStatus(true);
                } else {
                    setMealEditStatus(false);
                }

                btnEdit.setLayoutParams(new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT)
                );
                btnEdit.setGravity(Gravity.RIGHT);

                /*
                 * Functionality for the Edit button. We basically want to clear current selection
                 * in the UsersSelectedChoice (or maybe add it to the order), then load the current
                 * selection with the items in the current ConfiguredMeal object (meal) and finally
                 * take user to the beginning of the parent, of this meal.
                 */
                btnEdit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UsersSelectedChoice.clearCurrentMeal();

                        if (mealBeingEdited) {
                            setMealEditStatus(false);
                        } else {
                            setMealEditStatus(true);
                            UsersSelectedChoice.startEditingMeal(meal.getMealName());
                        }

                        Intent intent = IntentFactory.create_GOTO_FIRST_PAGE_OF_GIVEN_PARENT_Intent(meal.getMainCategoryID());

                        getContext().sendBroadcast(intent);
                    }
                });

                /*
                 * We only want the "Edit" button on the meals, that are possible to edit (not the
                 * current meal, that we haven't yet added to the order).
                 */
                if (!meal.getMealName().equals(getResources().getString(R.string.current_meal_name))) {
                    mealHdr.addView(btnEdit);
                }

                vlContent.addView(mealHdr);

                while (itemsInMeal.hasNext()) {
                    PurchasableGoods pg = itemsInMeal.next();

                    final TextView newItem = new TextView(getContext());
                    newItem.setText(pg.getLabel() +
                            " " +
                            Formatting.formatCash(pg.getPrice())
                    );
                    mealTotal += pg.getPrice();
                    vlContent.addView(newItem);
                }
//        final TextView newItem = new TextView(getContext());
//        newItem.setText("Total: " +
//                Formatting.formatCash(mealTotal)
//        );
//        newItem.setTextAppearance(R.style.mealTotal);
//        vlContent.addView(newItem);
            }
        }
    }

    /**
     * Sets the status of this meal being edited or not.
     * @param beingEdited
     */
    private void setMealEditStatus(boolean beingEdited) {
        if (beingEdited) {
            if (btnEdit != null) {
                btnEdit.setText(R.string.btnStopEditMeal);
            }

            mealBeingEdited = true;
            cvThisGUI.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.meal_being_edited));
        } else {
            if (btnEdit != null) {
                btnEdit.setText(R.string.btnEditMeal);
            }

            mealBeingEdited = false;
            cvThisGUI.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.meal_not_being_edited));
        }
    }

    public int getMealTotal() {
        return mealTotal;
    }
}
