package com.example.selfcheckout_wof.custom_components.utils;

import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.selfcheckout_wof.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Class that helps formatting text and possibly other GUI aspects.
 */
public class Formatting {
    /*
     * Variables for cash formatting
     */
    private static DecimalFormatSymbols symbols;
    private static DecimalFormat decimalFormat;

    static {
        symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("£ #,##0.00", symbols);
    }

    /**
     * Formats cash
     *
     * @param amount
     * @return
     */
    public static String formatCash(long amount) {
        return decimalFormat.format(amount / 100.0);
    }

//    public static void formatImageFromUri(Uri uri) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decod
//
//        BitmapFactory.decodeResource(getResources(), R.id.dr, options);
//        int imageHeight = options.outHeight;
//        int imageWidth = options.outWidth;
//        String imageType = options.outMimeType;
//    }
}
