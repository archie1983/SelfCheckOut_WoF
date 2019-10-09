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
    /**
     * Here we will store what user has selected in the current selection.
     */
    private static ArrayList<PurchasableGoods> currentMeal = new ArrayList<>();

    /**
     * Here we will store the whole order, that consists of user's selected meals
     * (arraylists of PurchasableGoods)- so really an arraylist of arralists.
     */
    private static ArrayList<ArrayList<PurchasableGoods>> currentOrder = new ArrayList<>();

    /**
     * Adding items to the list in a synchronised, tightly controlled way
     * @param purchasableGoods
     * @return
     */
    public static synchronized boolean addGoodsItem(PurchasableGoods purchasableGoods) {
        return currentMeal.add(purchasableGoods);
    }

    /**
     * Removing items from the list in a synchronised, tightly controlled way
     * @param purchasableGoods
     * @return
     */
    public static synchronized boolean removeGoodsItem(PurchasableGoods purchasableGoods) {
        return currentMeal.remove(purchasableGoods);
    }

    /**
     * Returns an iterator for currently selected items.
     * @return
     */
    public static synchronized Iterator<PurchasableGoods> getCurrentlySelectedItems() {
        return currentMeal.iterator();
    }

    /**
     * Returns a flag of whether the given purchasable goods item has been selected or not
     * @param purchasableGoods
     * @return
     */
    public static synchronized boolean itemIsSelected(PurchasableGoods purchasableGoods) {
        return currentMeal.contains(purchasableGoods);
    }

    /**
     * Clears user's selection.
     */
    public static synchronized void clearCurrentMeal() {
        /*
         * We're creating a new ArrayList instead of just currentMeal.clear(),
         * because we may still have this arraylist in the order and therefore
         * we don't want to clear it, but just simply forget about it in terms
         * of meal.
         */
        currentMeal = new ArrayList<>();
    }

    /**
     * Adds the current meal to order.
     */
    public static synchronized void addCurrentMealToOrder() {
        currentOrder.add(currentMeal);
    }

    /**
     * Adds the current meal to order.
     */
    public static synchronized void clearOrder() {
        currentOrder = new ArrayList<>();
    }

    /**
     * Returns an iterator for currently selected items.
     * @return
     */
    public static synchronized Iterator<ArrayList<PurchasableGoods>> getCurrentOrder() {
        return currentOrder.iterator();
    }
}
