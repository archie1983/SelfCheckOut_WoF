package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.componentActions.AdmSalesItemAction;
import com.example.selfcheckout_wof.custom_components.utils.Formatting;
import com.example.selfcheckout_wof.data.SalesItems;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

/**
 * A custom view for displaying a sales item for admin purposes (view, update, delete)
 */
public class AdmSalesItemView extends LinearLayout {
    /**
     * A flag of whether the item represented by this component is selected or not
     */
    private boolean selected;

    /**
     * In order to mark the selected admin list item, we'll need to keep
     * track of all of them and null this collection upon creating a header,
     * because that's where we normally start creating the admin list items.
     */
    private static ArrayList<AdmSalesItemView> allItems = new ArrayList<>();

    /**
     * We'll have unified formatting for the fields (and the headings)
     */
    private static LayoutParams layoutParams_label
            , layoutParams_btn
            , layoutParams_price
            , layoutParams_page
            , layoutParams_image
            , layoutParams_image_hdr
            , layoutParams_btn_hdr;

    /**
     * Initialising the layout parameters for components (formatting)
     */
    static {
        layoutParams_label = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );

        // Set margins for text view
        layoutParams_label.bottomMargin = 8;
        layoutParams_label.setMarginEnd(8);
        layoutParams_label.topMargin = 20;
        layoutParams_label.setMarginStart(8);
        layoutParams_label.gravity = Gravity.CENTER;
        //layoutParams_label.height = 200;
        layoutParams_label.width = 250;

        /*
         * for price
         */
        layoutParams_price = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );

        layoutParams_price.bottomMargin = 8;
        layoutParams_price.setMarginEnd(8);
        layoutParams_price.topMargin = 20;
        layoutParams_price.setMarginStart(8);
        layoutParams_price.gravity = Gravity.CENTER;
        //layoutParams_price.height = 200;
        layoutParams_price.width = 75;

        /*
         * for page
         */
        layoutParams_page = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );

        layoutParams_page.bottomMargin = 8;
        layoutParams_page.setMarginEnd(8);
        layoutParams_page.topMargin = 20;
        layoutParams_page.setMarginStart(8);
        layoutParams_page.gravity = Gravity.CENTER;
        //layoutParams_page.height = 200;
        layoutParams_page.width = 75;

        /*
         * for image
         */
        layoutParams_image = new LayoutParams(
                LayoutParams.MATCH_PARENT, // imageView width
                LayoutParams.WRAP_CONTENT // imageView height
        );

        layoutParams_image.bottomMargin = 8;
        layoutParams_image.setMarginEnd(8);
        layoutParams_image.topMargin = 20;
        layoutParams_image.setMarginStart(8);
        layoutParams_image.gravity = Gravity.CENTER;
        layoutParams_image.height = 100;
        layoutParams_image.width = 125;

        /*
         * for image
         */
        layoutParams_image_hdr = new LayoutParams(
                LayoutParams.MATCH_PARENT, // imageView width
                LayoutParams.WRAP_CONTENT // imageView height
        );

        layoutParams_image_hdr.bottomMargin = 8;
        layoutParams_image_hdr.setMarginEnd(8);
        layoutParams_image_hdr.topMargin = 20;
        layoutParams_image_hdr.setMarginStart(8);
        layoutParams_image_hdr.gravity = Gravity.CENTER;
        //layoutParams_image_hdr.height = 200;
        layoutParams_image_hdr.width = 250;

        /*
         * And a separate formatting for the buttons
         */
        layoutParams_btn = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );

        // Set margins for button
        layoutParams_btn.bottomMargin = 8;
        layoutParams_btn.setMarginEnd(8);
        layoutParams_btn.topMargin = 20;
        layoutParams_btn.setMarginStart(8);
        layoutParams_btn.gravity = Gravity.CENTER;

        /*
         * And a separate formatting for the buttons
         */
        layoutParams_btn_hdr = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );

        // Set margins for button
        layoutParams_btn_hdr.bottomMargin = 8;
        layoutParams_btn_hdr.setMarginEnd(8);
        layoutParams_btn_hdr.topMargin = 20;
        layoutParams_btn_hdr.setMarginStart(8);
        layoutParams_btn_hdr.gravity = Gravity.CENTER;
        layoutParams_btn_hdr.width = 150;
    }

    public AdmSalesItemView(Context context) {
        super(context);
        init(null,null, null, 0);
    }

    public AdmSalesItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null, null, attrs, 0);
    }

    public AdmSalesItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(null, null,attrs, defStyle);
    }

    public AdmSalesItemView(SalesItems si, AdmSalesItemAction action, Context context) {
        super(context);
        init(si, action,null, 0);
    }

    /**
     * Creates a header for the table of admin list items
     * @return
     */
    private void createHeader() {
        /*
         * since we're creating a header, we'll be creating ordinary AdmSalesItemView
         * objects soon and we'll need a clear collection for them.
         */
        allItems = new ArrayList<>();

        removeAllViews();
        setOrientation(LinearLayout.HORIZONTAL);

        /*
         * Now label
         */
        TextView tvLabel = new TextView(getContext());
        tvLabel.setText("Label");
        addView(tvLabel);
        tvLabel.setLayoutParams(layoutParams_label);
        tvLabel.setPadding(5,5,5,5);

        /*
         * Now page
         */
        TextView tvPage = new TextView(getContext());
        tvPage.setText("Page");
        addView(tvPage);
        tvPage.setLayoutParams(layoutParams_page);
        tvPage.setPadding(5,5,5,5);

        /*
         * Now price
         */
        TextView tvPrice = new TextView(getContext());
        tvPrice.setTypeface(null, Typeface.BOLD);
        tvPrice.setText("Price");
        addView(tvPrice);
        tvPrice.setLayoutParams(layoutParams_price);
        tvPrice.setPadding(5,5,5,5);

        /*
         * Now image
         */
        TextView tvImage = new TextView(getContext());
        tvImage.setText("Image");
        addView(tvImage);
        tvImage.setLayoutParams(layoutParams_image_hdr);
        tvImage.setPadding(5,5,5,5);

        /*
         * Now a button to allow edit or delete.
         */
        TextView tvButton = new TextView(getContext());
        tvButton.setText(" ");
        addView(tvButton);
        tvButton.setLayoutParams(layoutParams_btn_hdr);
        tvButton.setPadding(5,5,5,5);
    }

    /**
     * Initialises the component.
     *
     * @param si SalesItems data to display
     * @param action Behaviour of what to do on actions pertaining to this sales item admin entry (update, delete, load)
     * @param attrs
     * @param defStyle
     */
    private void init(SalesItems si, AdmSalesItemAction action, AttributeSet attrs, int defStyle) {
        /*
         * Populate the view with the SalesItem data
         */
        if (si != null) {
            setSalesItem(si, action);
            allItems.add(this);
        } else {
            createHeader();
        }

        /*
         * Load attributes and set the "selected" aspect of this component
         */
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.AdmSalesItemView, defStyle, 0);

        setSelected(a.getBoolean(R.styleable.AdmSalesItemView_selected, false));

        a.recycle();
    }

    /**
     * Populates the component with the SalesItem data
     *
     * @param salesItem
     */
    public void setSalesItem(SalesItems salesItem, final AdmSalesItemAction action) {
        removeAllViews();
        setOrientation(LinearLayout.HORIZONTAL);

        /*
         * If this is a subcategory, then add an indent
         */
        if (salesItem.parentCategoryId > -1) {
            TextView tvLabel = new TextView(getContext());
            tvLabel.setText("          ");
            addView(tvLabel);
            ImageView ivIndent = new ImageView(getContext());
            ivIndent.setImageResource(R.drawable.hierarchy_arrow);
            addView(ivIndent);
        }

        /*
         * Now label
         */
        TextView tvLabel = new TextView(getContext());
        tvLabel.setText(salesItem.label);
        addView(tvLabel);

        /*
         * Now page
         */
        TextView tvPage = new TextView(getContext());
        tvPage.setText(salesItem.page + "");
        addView(tvPage);

        /*
         * Now price
         */
        TextView tvPrice = new TextView(getContext());
        tvPrice.setTypeface(null, Typeface.BOLD);
        tvPrice.setText(Formatting.formatCash(salesItem.price));
        addView(tvPrice);

//        TextView tvUri = new TextView(getContext());
//        tvUri.setText(salesItem.pictureUrl);
//        addView(tvUri);

        /*
         * Now image
         */
        addView(createImgFromFile(salesItem.pictureUrl));
        setSelected(this.selected);

        /*
         * Now a button to allow edit or delete.
         */
        Button btnEditThisItem = new Button(getContext());
        btnEditThisItem.setText(R.string.btnEditSalesItem);
        addView(btnEditThisItem);

        /*
         * When we click the edit button, we want to load
         * the info into the edit fields and make the item
         * ready for editing.
         */
        btnEditThisItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                action.selectSalesItemForEdit();

                /*
                 * First un-select all items, then select this one
                 */
                unselectAllSelectedItems();
                setSelected(true);
            }
        });

        /*
         * Lastly formatting of the text views- just visual formatting below this line
         */
        tvLabel.setLayoutParams(layoutParams_label);
        // Set the text view content padding
        tvLabel.setPadding(5,5,5,5);

        tvPage.setLayoutParams(layoutParams_page);
        tvPage.setPadding(5,5,5,5);

        tvPrice.setLayoutParams(layoutParams_price);
        tvPrice.setPadding(5,5,5,5);

//        tvUri.setLayoutParams(layoutParams_label);
//        // Set the text view content padding
//        tvUri.setPadding(5,5,5,5);

        btnEditThisItem.setLayoutParams(layoutParams_btn);
        // Set the button padding
        btnEditThisItem.setPadding(5,5,5,5);
    }

    /**
     * Unselects all selected items (should only be 1 max, but it makes sure
     * that nothing is selected)
     */
    public static void unselectAllSelectedItems() {
        for (AdmSalesItemView av : allItems) {
            av.setSelected(false);
        }
    }

    /**
     * Creates an ImageView object and loads it with an image
     * from a given path in the filesystem.
     *
     * @param path
     * @return
     */
    private ImageView createImgFromFile(String path) {
        ImageView iv = new ImageView(getContext());

        /*
         * If not image given, then putting in the default thumbnail,
         * otherwise use the exact picture.
         */
        if (path != null && !path.equals("")) {
            Uri uri = Uri.parse(path);
            iv.setImageURI(uri);
        } else {
            iv.setImageResource(R.drawable.dragndrop);
        }

        iv.setLayoutParams(layoutParams_image);

        return iv;
    }

    /**
     * Gets the "selected" attribute value.
     *
     * @return The "selected" attribute value.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets the view's "selected" attribute value. It's a flag
     * of whether we paint the view as selected or not.
     *
     * @param selected The "selected" attribute value to use.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;

        if (selected) {
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.selected_goods));
        } else {
            setBackgroundColor(Color.WHITE);
        }
    }
}
