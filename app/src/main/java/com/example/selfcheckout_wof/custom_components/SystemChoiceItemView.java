package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.utils.Formatting;
import com.example.selfcheckout_wof.data.SystemChoices;

/**
 * A clickable CardView for choosing one of the System choices (e.g. Start Sales, Import DB, etc.).
 */
public class SystemChoiceItemView extends LinearLayout {
    private SystemChoices systemChoiceToRepresent;

    public SystemChoiceItemView(Context context) {
        super(context);
        init(null, 0);
    }

    public SystemChoiceItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SystemChoiceItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SystemChoiceItemView, defStyle, 0);

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SystemChoiceItemView);
        systemChoiceToRepresent = SystemChoices.values()[ta.getInt(R.styleable.SystemChoiceItemView_systemChoiceType,0)];

        a.recycle();

        // Update the choice item view from attributes
        setUpChoiceItem();
        displayChoice();
    }

    final CardView cvThisGUI = new CardView(getContext());
    final LinearLayout vlCardViewContent = new LinearLayout(getContext());
    TextView tvDescription = new TextView(getContext());

    /**
     * Initialise the view by setting up the skeleton of it (e.g. the CardView element inside,
     * etc.).
     */
    private void setUpChoiceItem() {
        /**
         * First make the layout vertical
         */
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        /*
         * Now create and add a CardView to show the picture of the item that we're
         * representing. The CardView will need to contain its own LinearLayout
         * because that's how I like to add images to CardView- by setting an image to
         * be a background of a LinearLayout inside the Cardview.
         */
        cvThisGUI.addView(vlCardViewContent);

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
        layoutParams_cv.topMargin = 20;
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
//        tvDescription.setLines(2);
//        tvDescription.setMaxLines(2);

        LayoutParams layoutParams_desc = new LayoutParams(
                LayoutParams.MATCH_PARENT, // width
                LayoutParams.WRAP_CONTENT // height
        );

        //layoutParams_desc.bottomMargin = 8;
        layoutParams_desc.setMarginEnd(16);
        //layoutParams_desc.topMargin = 8;
        layoutParams_desc.setMarginStart(16);

        tvDescription.setLayoutParams(layoutParams_desc);
        tvDescription.setGravity(Gravity.CENTER);
        this.addView(tvDescription);
    }

    /**
     * Display the required choice to the user.
     */
    private void displayChoice() {
        //vlCardViewContent.setBackgroundResource(pgItemToDisplay.getImage_resource());
        /*
         * Loading the image using Glide.
         */
        if (systemChoiceToRepresent != null) {
            Glide.with(this)
                    .load(systemChoiceToRepresent.getImage())
                    .into(new CustomTarget<Drawable>() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            vlCardViewContent.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });

            tvDescription.setText(systemChoiceToRepresent.getCaption());

            /*
             * Adding clickhandler for this cardView object to mark the checkbox checked
             */
            cvThisGUI.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    getContext().sendBroadcast(systemChoiceToRepresent.getIntent());
                }
            });
        }
    }

    /**
     * Sets the system choice type, which then defines the look and feel of
     * this selector item.
     *
     * @param systemChoiceToRepresent
     */
    public void setSystemChoiceType(SystemChoices systemChoiceToRepresent) {
        this.systemChoiceToRepresent = systemChoiceToRepresent;
        displayChoice();
    }
}
