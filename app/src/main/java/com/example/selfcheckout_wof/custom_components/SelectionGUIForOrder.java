package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.cardview.widget.CardView;

import com.example.selfcheckout_wof.data.PurchasableGoods;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

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
        final AppCompatCheckBox chkThisSelected = new AppCompatCheckBox(context);
        LayoutParams layoutParams_chk = new LayoutParams(
                LayoutParams.MATCH_PARENT, // width
                LayoutParams.WRAP_CONTENT // height
        );

        layoutParams_chk.bottomMargin = 0;
        //layoutParams_chk.setMarginEnd(16);
        layoutParams_chk.topMargin = 20;
        //layoutParams_chk.setMarginStart(16);
        chkThisSelected.setLayoutParams(layoutParams_chk);
        this.addView(chkThisSelected);

        /*
         * Now create and add a CardView to show the picture of the item that we're
         * adding to the order. The VardView will need to contain its own LinearLayout
         * because that's how I like to add images to CardView- by setting an image to
         * be a background of a LinearLayout inside the Cardview.
         */
        CardView cvThisGUI = new CardView(context);
        LinearLayout vlCardViewContent = new LinearLayout(context);
        cvThisGUI.addView(vlCardViewContent);
        vlCardViewContent.setBackgroundResource(pgItemToDisplay.getImage_resource());

        /*
         * Adding clickhandler for this cardView object to mark the checkbox checked
         */
        cvThisGUI.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chkThisSelected.isChecked()) {
                    chkThisSelected.setChecked(false);
                } else {
                    chkThisSelected.setChecked(true);
                }
            }
        });

        this.addView(cvThisGUI);

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
        layoutParams_cv.height = 200;
        layoutParams_cv.width = 250;
        // Set the card view layout params
        cvThisGUI.setLayoutParams(layoutParams_cv);
        // Set the card view corner radius
        cvThisGUI.setRadius(4F);
        // Set card view elevation
        cvThisGUI.setCardElevation(8F);

        /**
         * Now a TextView for description
         */
        TextView tvDescription = new TextView(context);
        tvDescription.setText(pgItemToDisplay.getLabel() + " " + pgItemToDisplay.getDescription());
        //tvDescription.setSingleLine(false);
        //tvDescription.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        /**
         * On two lines
         */
        tvDescription.setLines(2);
        tvDescription.setMaxLines(2);

        LayoutParams layoutParams_desc = new LayoutParams(
                LayoutParams.MATCH_PARENT, // width
                LayoutParams.WRAP_CONTENT // height
        );

        //layoutParams_desc.bottomMargin = 8;
        layoutParams_desc.setMarginEnd(16);
        //layoutParams_desc.topMargin = 8;
        layoutParams_desc.setMarginStart(16);

        tvDescription.setLayoutParams(layoutParams_desc);
        this.addView(tvDescription);

        /**
         * Now a TextView for price
         */
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        /**
         * Money format
         */
        DecimalFormat decimalFormat = new DecimalFormat("£ #,###.00", symbols);
        String price = decimalFormat.format(pgItemToDisplay.getPrice() / 100);

        TextView tvPrice = new TextView(context);
        tvPrice.setTypeface(null, Typeface.BOLD);
        tvPrice.setLayoutParams(layoutParams_desc);
        tvPrice.setText(price);
        this.addView(tvPrice);
    }
}
