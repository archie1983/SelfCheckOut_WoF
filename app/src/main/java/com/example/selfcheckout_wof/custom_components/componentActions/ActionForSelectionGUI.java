package com.example.selfcheckout_wof.custom_components.componentActions;

import android.os.Bundle;

import com.example.selfcheckout_wof.ItemListActivity;
import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.UsersChoiceFragment;
import com.example.selfcheckout_wof.custom_components.UsersSelectedChoice;
import com.example.selfcheckout_wof.data.MainCategories;
import com.example.selfcheckout_wof.data.PurchasableGoods;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A class (with overridable methods) to define and change functionality of
 * selection GUI actions (selection / de-selection)
 */
public class ActionForSelectionGUI {

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
        return UsersSelectedChoice.removeGoodsItem(pgSelectedItem);
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
         * First finding out what parent category does this goods item belong to.
         */
        int parentId = pgSelectedItem.getParentID();

        /*
         * How many of this category items can we have selected?
         */
        int maxSelectedItemsInThisCategory = pgSelectedItem.getNumberOfMultiSelectableItems();

        /*
         * How many do we have selected?
         */
        int currentSelectedItemsInThisCategory = 0;

        Iterator<PurchasableGoods> it = UsersSelectedChoice.getCurrentlySelectedItems();
        while (it.hasNext()) {
            PurchasableGoods pg = it.next();
            if (pg.getParentID() == parentId) {
                currentSelectedItemsInThisCategory++;
            }
        }

        /*
         * If more can be selected, then return true, otherwise false
         */
        if (currentSelectedItemsInThisCategory < maxSelectedItemsInThisCategory) {
            return UsersSelectedChoice.addGoodsItem(pgSelectedItem);
        } else {
            return false;
        }
    }

    /**
     * Action to perform when an item is selected
     *
     * @return a flag if the action was successful
     */
    public boolean onSelected() {
        /*
         * Fragment support manager is acquired from ItemListActivity.getInstance()
         * as that is where we got it from for the first fragment. We will use it
         * again for the users choice fragment.
         */
        if (addSelectedItem(pgForTheseActions)) {
            //updateInvoice();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Action to perform when an item is de-selected
     *
     * @return a flag if the action was successful
     */
    public boolean onDeSelected() {
        if (removeSelectedItem(pgForTheseActions)) {
            //updateInvoice();
            return true;
        } else {
            return false;
        }
    }

//    /**
//     * After we've added or removed the goods item successfully,
//     * we'll update the user's selected choice on the right hand side.
//     * The selected choices will eventually form the invoice and will be sent
//     * to the chef for cooking.
//     */
//    private void updateInvoice() {
//        UsersChoiceFragment fragment = new UsersChoiceFragment();
//
//        ItemListActivity.getInstance().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.users_choice_container, fragment)
//                .commit();
//    }
}
