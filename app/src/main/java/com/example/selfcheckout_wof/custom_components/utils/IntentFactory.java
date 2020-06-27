package com.example.selfcheckout_wof.custom_components.utils;

import android.content.Intent;

import com.example.selfcheckout_wof.data.SystemChoices;

/**
 * This class is for generating intents, that we use across the app. This is somewhat similar to
 * how SystemChoices enum works, but that is for system choices, where each choice has a picture
 * and other attributes, whereas this class is wider and is meant for more ad-hock intents as they
 * become necessary.
 */
public class IntentFactory {

    /**
     * Declared and defined in one place - tha param names.
     */
    public static final String PARENT_ID_PARAM_NAME = "PARENT_ID";

    /**
     * All intents, that our factory can create and serve.
     */
    public enum IntentType {
        GOTO_FIRST_PAGE_OF_GIVEN_PARENT,
        GOTO_BEGINNING_OF_SALES_PROCESS,
        UNKNOWN;

        Intent intent = null;

        IntentType() {
            intent = new Intent();
            intent.setAction("com.ae." + this.name());
        }

        public Intent getIntent() {
            return intent;
        }
    }

    /**
     * This class will be accessed singleton style. There's no need for multiple instances.
     */
    IntentFactory intentFactory = null;
    private IntentFactory() {

    }

    /**
     * A singleton style access. There's no need for multiple instances of this.
     *
     * @return
     */
    public IntentFactory getIntentFactory() {
        if (intentFactory == null) {
            intentFactory = new IntentFactory();
        }

        return intentFactory;
    }

    public static IntentType lookUpByIntent(Intent intentToLookUp) {
        for (IntentType it : IntentType.values()) {
            if (it.getIntent().getAction().equals(intentToLookUp.getAction())) {
                return it;
            }
        }
        return IntentType.UNKNOWN;
    }

    public static Intent create_GOTO_FIRST_PAGE_OF_GIVEN_PARENT_Intent(int parentID) {
        Intent intent = IntentType.GOTO_FIRST_PAGE_OF_GIVEN_PARENT.getIntent();

        intent.putExtra(PARENT_ID_PARAM_NAME, parentID);
        return intent;
    }

    public static Intent create_GOTO_BEGINNING_OF_SALES_PROCESS_Intent() {
        Intent intent = IntentType.GOTO_BEGINNING_OF_SALES_PROCESS.getIntent();

        return intent;
    }
}
