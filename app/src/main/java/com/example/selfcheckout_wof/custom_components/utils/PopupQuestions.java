package com.example.selfcheckout_wof.custom_components.utils;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

/**
 * A utility class for creating pop-up questions for different scenarious.
 * Typically it will be a static function taking in one or more Runnables,
 * that will define the behaviour of the buttons on the pop-up question.
 * After that it will just create the pop-up and show it.
 */
public class PopupQuestions {
    public static void doYouWantToContinueShoppingOrCheckout(Context context,
                                                             final Runnable rOrderCompleteBehaviour,
                                                             final Runnable rAddAnotherBehaviour) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Complete Order?");
        builder.setMessage("The Item was added to order. " +
                "Is your order complete or would you " +
                "like to add another meal or drink?");

        builder.setPositiveButton("Order Complete", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                rOrderCompleteBehaviour.run();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Add Another", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                rAddAnotherBehaviour.run();
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
