package com.example.selfcheckout_wof.custom_components.componentActions;

import com.example.selfcheckout_wof.custom_components.UsersSelectedChoice;
import com.example.selfcheckout_wof.data.PurchasableGoods;

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
     * on the current page and adds it if it's possible.
     *
     * @param pgSelectedItem
     * @return
     */
    private boolean addSelectedItem(PurchasableGoods pgSelectedItem) {
        /*
         * How many of this category items can we have selected?
         */
        int maxSelectedItemsInThisCategory = pgSelectedItem.getNumberOfMultiSelectableItems();

        /*
         * How many do we have selected on this page?
         */
        int currentSelectedItemsInThisCategory = 0;

        for (PurchasableGoods pg: UsersSelectedChoice.getCurrentlySelectedItems()) {
            if (pg.getParentID() == pgSelectedItem.getParentID() &&
                    pg.getPage() == pgSelectedItem.getPage()) {
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
