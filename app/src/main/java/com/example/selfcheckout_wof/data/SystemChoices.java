package com.example.selfcheckout_wof.data;

import android.content.Intent;

import com.example.selfcheckout_wof.R;

/**
 * Enum that defines system choices, that user can select when application starts.
 */
public enum SystemChoices {
    UNKNOWN(R.drawable.dragndrop, "UNKNOWN"),
    MANAGE_DATA(R.drawable.dragndrop, "Manage Data"),
    EXPORT_DB(R.drawable.dragndrop, "Export Data"),
    LOGIN_PAYPAL(R.drawable.dragndrop, "Connect Card reader and Printer"),
    START_SALES(R.drawable.dragndrop, "Start Sales"),
    IMPORT_DB(R.drawable.dragndrop, "Import Data");

    int image = 0;
    String caption = "";
    Intent intent = null;

    SystemChoices(int image, String caption) {
        this.image = image;
        this.caption = caption;
        intent = new Intent();
        intent.setAction("com.ae." + this.name());
    }

    public int getImage() {
        return image;
    }

    public String getCaption() {
        return caption;
    }

    public Intent getIntent() {
        return intent;
    }

    public static SystemChoices lookUpByIntent(Intent intentToLookUp) {
        for (SystemChoices sc : SystemChoices.values()) {
            if (sc.getIntent().getAction().equals(intentToLookUp.getAction())) {
                return sc;
            }
        }
        return UNKNOWN;
    }
}
