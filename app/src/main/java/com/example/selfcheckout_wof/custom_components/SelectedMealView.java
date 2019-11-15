package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.utils.Formatting;
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
     *
     * @param context
     * @param itemsInMeal
     */
    public SelectedMealView(Context context, Iterator<PurchasableGoods> itemsInMeal) {
        super(context);
        init(itemsInMeal);
    }

    /**
     *
     * @param context
     * @param itemsInMeal
     */
    public SelectedMealView(Context context, ArrayList<PurchasableGoods> itemsInMeal) {
        super(context);
        init(itemsInMeal.iterator());
    }

    /**
     * Builds the view.
     * @param itemsInMeal
     */
    private void init(Iterator<PurchasableGoods> itemsInMeal) {
        this.setOrientation(VERTICAL);

        if (itemsInMeal.hasNext()) {
            final TextView header = new TextView(getContext());
            header.setText("Selection: ");
            header.setTextAppearance(R.style.mealTotal);
            this.addView(header);

            while (itemsInMeal.hasNext()) {
                PurchasableGoods pg = itemsInMeal.next();
                final TextView newItem = new TextView(getContext());
                newItem.setText(pg.getLabel() +
                        " " +
                        Formatting.formatCash(pg.getPrice())
                );
                mealTotal += pg.getPrice();
                this.addView(newItem);
            }
        }

//        final TextView newItem = new TextView(getContext());
//        newItem.setText("Total: " +
//                Formatting.formatCash(mealTotal)
//        );
//        newItem.setTextAppearance(R.style.mealTotal);
//        this.addView(newItem);
    }

    public int getMealTotal() {
        return mealTotal;
    }
}
