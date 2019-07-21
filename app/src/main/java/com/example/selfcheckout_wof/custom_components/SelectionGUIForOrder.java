package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.cardview.widget.CardView;

import com.example.selfcheckout_wof.data.PurchasableGoods;

/**
 * A component to allow user to make a selection of an item that they want
 * to add to the order. This will probably end up being a GUI with a checkbox,
 * a CardView and two text boxes-- one for price and one for description.
 *
 * Upon clicking the cardview, the checkbox should get checked and the whole
 * component should become highlighted.
 */
public class SelectionGUIForOrder extends LinearLayout {
    public SelectionGUIForOrder(PurchasableGoods pgItemToDisplay, Context context) {
        super(context);
        setUpGUI(pgItemToDisplay, context);
    }

    public SelectionGUIForOrder(PurchasableGoods pgItemToDisplay, Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpGUI(pgItemToDisplay, context);
    }

    public SelectionGUIForOrder(PurchasableGoods pgItemToDisplay, Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUpGUI(pgItemToDisplay, context);
    }

    /**
     * Creating the required components and adding them to this SelectionGUIForOrder,
     * which at the start is just a linear layout.
     */
    private void setUpGUI(PurchasableGoods pgItemToDisplay, Context context) {
        /**
         * First make the layout vertical
         */
        setOrientation(VERTICAL);

        /**
         * Now create and add a checkbox
         */
        AppCompatCheckBox chkThisSelected = new AppCompatCheckBox(context);
        this.addView(chkThisSelected);

        /**
         * Now create and add a CardView to show the picture of the item that we're
         * adding to the order. The VardView will need to contain its own LinearLayout
         * because that's how I like to add images to CardView- by setting an image to
         * be a background of a LinearLayout inside the Cardview.
         */
        CardView cvThisGUI = new CardView(context);
        LinearLayout vlCardViewContent = new LinearLayout(context);
        cvThisGUI.addView(vlCardViewContent);
        vlCardViewContent.setBackgroundResource(pgItemToDisplay.getImage_resource());
        this.addView(cvThisGUI);

        /**
         * Now a TextView for description
         */
        TextView tvDescription = new TextView(context);
        tvDescription.setText(pgItemToDisplay.getLabel() + " " + pgItemToDisplay.getDescription());
        this.addView(tvDescription);

        /**
         * Now a TextView for price
         */
        TextView tvPrice = new TextView(context);
        tvPrice.setText("£" + pgItemToDisplay.getPrice() / 100);
        this.addView(tvPrice);
    }
}
