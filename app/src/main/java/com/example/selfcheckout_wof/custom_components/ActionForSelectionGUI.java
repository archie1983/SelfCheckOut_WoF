package com.example.selfcheckout_wof.custom_components;

import android.os.Bundle;

import com.example.selfcheckout_wof.ItemListActivity;
import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.data.PurchasableGoods;

/**
 * A class (with overridable methods) to define and change functionality of
 * selection GUI actions (selection / de-selection)
 */
public class ActionForSelectionGUI {

    PurchasableGoods pgForTheseActions;

    public ActionForSelectionGUI(PurchasableGoods pg) {
        pgForTheseActions = pg;
    }

    void onSelected() {
        /*
         * Now we'll update the user's selected choice on the right hand side.
         * The selected choice will eventually form the invoice and will be sent
         * to the chef for cooking.
         */
        Bundle arguments = new Bundle();
        /*
         * ID here will be the name of the enum entry of the MainCategories enum
         */
        arguments.putString(UsersChoiceFragment.ARG_PARAM3, pgForTheseActions.getLabel() + " " + pgForTheseActions.getDescription());
        UsersChoiceFragment fragment = new UsersChoiceFragment();
        fragment.setArguments(arguments);

        /*
         * Fragment support manager is acquired from ItemListActivity.getInstance()
         * as that is where we got it from for the first fragment. We will use it
         * again for the users choice fragment.
         */
        ItemListActivity.getInstance().getSupportFragmentManager().beginTransaction()
                .replace(R.id.users_choice_container, fragment)
                .commit();
    }

    void onDeSelected() {

    }
}
