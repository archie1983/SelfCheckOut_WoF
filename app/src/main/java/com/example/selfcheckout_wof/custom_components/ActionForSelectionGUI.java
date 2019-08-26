package com.example.selfcheckout_wof.custom_components;

import android.os.Bundle;

import com.example.selfcheckout_wof.ItemListActivity;
import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.data.MainCategories;
import com.example.selfcheckout_wof.data.PurchasableGoods;

import java.util.ArrayList;

/**
 * A class (with overridable methods) to define and change functionality of
 * selection GUI actions (selection / de-selection)
 */
public class ActionForSelectionGUI {

    /*
     * Here we will store what user has selected.
     */
    private static ArrayList<PurchasableGoods> currentlySelectedGoods = new ArrayList<PurchasableGoods>();

    /**
     * The item that we want to define the "selected" / "de-selected" actions for.
     */
    PurchasableGoods pgForTheseActions;

    public ActionForSelectionGUI(PurchasableGoods pg) {
        pgForTheseActions = pg;
    }

    /**
     * Checks if we can remove a goods item, that has been selected
     * and if we can, then we remove it.
     *
     * @param pgSelectedItem
     * @return
     */
    private boolean removeSelectedItem(PurchasableGoods pgSelectedItem) {
        return currentlySelectedGoods.remove(pgSelectedItem);
    }

    /**
     * Checks if we can still add a goods item belonging to its parent category
     * and adds it if it's possible.
     *
     * @param pgSelectedItem
     * @return
     */
    private boolean addSelectedItem(PurchasableGoods pgSelectedItem) {
        /*
         * First figuring out what main cactegory does this goods item belong to.
         */
        MainCategories mc = MainCategories.itemBelongsToThisMainCategory(pgSelectedItem);

        /*
         * How many of this category items can we have selected?
         */
        int maxSelectedItemsInThisCategory = mc.numberOfMultiSelectableItems();

        /*
         * How many do we have selected?
         */
        int currentSelectedItemsInThisCategory = 0;

        for (PurchasableGoods pg : currentlySelectedGoods) {
            if (pg.getClass() == pgSelectedItem.getClass()) {
                currentSelectedItemsInThisCategory++;
            }
        }

        /*
         * If more can be selected, then return true, otherwise false
         */
        if (currentSelectedItemsInThisCategory < maxSelectedItemsInThisCategory) {
            currentlySelectedGoods.add(pgSelectedItem);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Action to perform when an item is selected
     *
     * @return a flag if the action was successful
     */
    boolean onSelected() {
        /*
         * Fragment support manager is acquired from ItemListActivity.getInstance()
         * as that is where we got it from for the first fragment. We will use it
         * again for the users choice fragment.
         */
        if (ItemListActivity.getInstance() == null) {
            return false;
        } else if (addSelectedItem(pgForTheseActions)) {
            /*
             * We've added the goods item successfully.
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

            ItemListActivity.getInstance().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.users_choice_container, fragment)
                    .commit();

            return true;
        }

        return false;
    }

    /**
     * Action to perform when an item is de-selected
     *
     * @return a flag if the action was successful
     */
    boolean onDeSelected() {
        return removeSelectedItem(pgForTheseActions);
    }
}
