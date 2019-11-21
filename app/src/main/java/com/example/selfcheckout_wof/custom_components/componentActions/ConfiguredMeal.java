package com.example.selfcheckout_wof.custom_components.componentActions;

import com.example.selfcheckout_wof.data.PurchasableGoods;

import java.util.ArrayList;

/**
 * This is a container class (POJO) for meals. For example a user may choose to have
 * udon noodles + chicken + beansprouts + chilli. Well tha choice then is a ConfiguredMeal,
 * which needs to have a collection of those items and some sort of identification.
 *
 * The same user may then also choose a jasmine tea + watermelon flavour + lychee jelly. That
 * will be another ConfiguredMeal. Both of those ConfiguredMeal objects need to be part of
 * the final order.
 *
 * Maybe the ConfiguredMeal as a name is not the most successful one, but I'm struggling to find
 * something more appropriate.
 */
public class ConfiguredMeal {
    /**
     * Here we will store what user has selected in the current selection (meal).
     */
    private ArrayList<PurchasableGoods> currentMealItems;

    /**
     * Simple text ID for this meal.
     */
    private String mealName = "";

    /**
     * The ID of the main category (in the DB) that was used to create this meal
     */
    private int mainCategoryID = -1;

    public ConfiguredMeal(ArrayList<PurchasableGoods> currentMealItems,
                          String mealName,
                          int mainCategoryID) {
        this.currentMealItems = currentMealItems;
        this.mealName = mealName;
        this.mainCategoryID = mainCategoryID;
    }

    public ArrayList<PurchasableGoods> getCurrentMealItems() {
        return currentMealItems;
    }

    public String getMealName() {
        return mealName;
    }

    public int getMainCategoryID() {
        return mainCategoryID;
    }
}
