package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.componentActions.ConfiguredMeal;
import com.example.selfcheckout_wof.custom_components.utils.Formatting;
import com.example.selfcheckout_wof.data.PurchasableGoods;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A component to represent the current order for user. This will be useful
 * to visually show the contents of order.
 */
public class FinalOrderView extends LinearLayout {
    /**
     * Price of this order.
     */
    int orderTotal = 0;

    /**
     *
     * @param context
     * @param itemsInOrder
     */
    public FinalOrderView(Context context, Iterator<ConfiguredMeal> itemsInOrder) {
        super(context);
        init(itemsInOrder);
    }

    /**
     * Builds the view.
     * @param itemsInOrder
     */
    private void init(Iterator<ConfiguredMeal> itemsInOrder) {
        this.setOrientation(VERTICAL);
        this.setShowDividers(SHOW_DIVIDER_MIDDLE);
        this.setDividerDrawable(getResources().getDrawable(R.drawable.horizontal_divider, null));

        while (itemsInOrder.hasNext()) {
            SelectedMealView mealView = new SelectedMealView(getContext(), itemsInOrder.next());
            this.addView(mealView);

            orderTotal += mealView.getMealTotal();
        }

        final TextView vOrderTotal = new TextView(getContext());
        vOrderTotal.setText("Total: " +
                Formatting.formatCash(orderTotal)
        );
        vOrderTotal.setTextAppearance(R.style.mealTotal);
        this.addView(vOrderTotal);
    }

    public int getOrderTotal() {
        return orderTotal;
    }
}
