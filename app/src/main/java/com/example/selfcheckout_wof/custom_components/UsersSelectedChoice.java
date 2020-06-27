package com.example.selfcheckout_wof.custom_components;

import com.example.selfcheckout_wof.custom_components.componentActions.ConfiguredMeal;
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
     * (glorified arraylists of PurchasableGoods).
     */
    private static ArrayList<ConfiguredMeal> currentOrder = new ArrayList<>();

    /**
     * When we're editing the current meal, we'll want to know to not add it as a new one
     * when we're done editing. For that it will be useful to know what meal we are editing
     * if any.
     */
    private static String nameOfMealBeingEdited = "";

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
    public static synchronized ArrayList<PurchasableGoods> getCurrentlySelectedItems() {
        return currentMeal;
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
        nameOfMealBeingEdited = "";
    }

    /**
     * Load a meal for editing.
     */
    public static synchronized void startEditingMeal(String nameOfMealToEdit) {
        for (ConfiguredMeal cm : currentOrder) {
            if (cm.getMealName().equals(nameOfMealToEdit)) {
                currentMeal = cm.getCurrentMealItems();
                nameOfMealBeingEdited = cm.getMealName();
                break;
            }
        }
    }

    /**
     * Remove a meal from order.
     */
    public static synchronized void removeMealFromOrder(String nameOfMealToRemove) {
        ConfiguredMeal mealToRemove = null;
        for (ConfiguredMeal cm : currentOrder) {
            if (cm.getMealName().equals(nameOfMealToRemove)) {
                mealToRemove = cm;
                break;
            }
        }
        if (mealToRemove != null) {
            currentOrder.remove(mealToRemove);
        }
    }

    /**
     * Is the current meal being edited?
     *
     * @return
     */
    public static boolean isCurrentMealBeingEdited() {
        return (nameOfMealBeingEdited != "");
    }

    public static String getNameOfMealBeingEdited() {
        return nameOfMealBeingEdited;
    }

    /**
     * Adds the current meal to order.
     *
     * @param nameOfParentItemInThisMeal the verbal part of name that we want the meal to be named
     *                                   (e.g. "Packet" in name "Packet1")
     * @param idOfParentItemInThisMeal the DB id of the parent item, from which this meal was
     *                                 generated.
     */
    public static synchronized void addCurrentMealToOrder(String nameOfParentItemInThisMeal, int idOfParentItemInThisMeal) {

        int mealNumber = 1;

        /*
         * Only add if there is anything to add.
         */
        if (currentMeal.size() > 0) {
            /*
             * Now find out the meal number for this parent ID.
             */
            if (currentOrder.size() > 0) {
                for (ConfiguredMeal cm : currentOrder) {
                    if (cm.getMainCategoryID() == idOfParentItemInThisMeal) {
                        mealNumber++;
                    }
                }
            }

            currentOrder.add(new ConfiguredMeal(currentMeal, nameOfParentItemInThisMeal + " " + mealNumber, idOfParentItemInThisMeal));
            clearCurrentMeal();
        }
    }

    /**
     * Adds the current meal to order.
     */
    public static synchronized void clearOrder() {
        currentOrder = new ArrayList<>();
        nameOfMealBeingEdited = "";
    }

    /**
     * Returns an iterator for currently added meals.
     * @return
     */
    public static synchronized Iterator<ConfiguredMeal> getCurrentOrder() {
        return currentOrder.iterator();
    }
}
