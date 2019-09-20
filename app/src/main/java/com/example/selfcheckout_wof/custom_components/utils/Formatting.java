package com.example.selfcheckout_wof.custom_components.utils;

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
}
