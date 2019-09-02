package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.data.SalesItems;

import java.io.File;

/**
 * A custom view for displaying a sales item for admin purposes (view, update, delete)
 */
public class AdmSalesItemView extends LinearLayout {
    /**
     * A flag of whether the item represented by this component is selected or not
     */
    private boolean selected;

    public AdmSalesItemView(Context context) {
        super(context);
        init(null,null, 0);
    }

    public AdmSalesItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null, attrs, 0);
    }

    public AdmSalesItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(null, attrs, defStyle);
    }

    public AdmSalesItemView(SalesItems si, Context context) {
        super(context);
        init(si,null, 0);
    }

    /**
     * Initialises the component.
     *
     * @param si SalesItems data to display
     * @param attrs
     * @param defStyle
     */
    private void init(SalesItems si, AttributeSet attrs, int defStyle) {
        /*
         * Populate the view with the SalesItem data
         */
        if (si != null) {
            setSalesItem(si);
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
    public void setSalesItem(SalesItems salesItem) {
        removeAllViews();
        setOrientation(LinearLayout.HORIZONTAL);

        TextView tvLabel = new TextView(getContext());
        tvLabel.setText(salesItem.label);
        addView(tvLabel);

        TextView tvUri = new TextView(getContext());
        tvUri.setText(salesItem.pictureUrl);
        addView(tvUri);

        addView(createImgFromFile(salesItem.pictureUrl));

        setSelected(this.selected);
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
        Uri uri = Uri.parse(path);
        iv.setImageURI(uri);

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
