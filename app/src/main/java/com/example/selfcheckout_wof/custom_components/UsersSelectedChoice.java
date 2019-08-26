package com.example.selfcheckout_wof.custom_components;

import com.example.selfcheckout_wof.data.PurchasableGoods;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * In this class I will store what user has selected in each category.
 * This will also be useful for generating invoice and controlling how
 * many items in a category can be selected.
 */
public class UsersSelectedChoice {
    /*
     * Here we will store what user has selected.
     */
    private static ArrayList<PurchasableGoods> currentlySelectedGoods = new ArrayList<PurchasableGoods>();

    /*
     * Adding items to the list in a synchronised, tightly controlled way
     */
    public static synchronized boolean addGoodsItem(PurchasableGoods purchasableGoods) {
        return currentlySelectedGoods.add(purchasableGoods);
    }

    /*
     * Removing items from the list in a synchronised, tightly controlled way
     */
    public static synchronized boolean removeGoodsItem(PurchasableGoods purchasableGoods) {
        return currentlySelectedGoods.remove(purchasableGoods);
    }

    /*
     * Returns an iterator for currently selected items.
     */
    public static synchronized Iterator<PurchasableGoods> getCurrentlySelectedItems() {
        return currentlySelectedGoods.iterator();
    }

    /*
     * Returns a flag of whether the given purchasable goods item has been selected or not
     */
    public static synchronized boolean itemIsSelected(PurchasableGoods purchasableGoods) {
        return currentlySelectedGoods.contains(purchasableGoods);
    }
}
