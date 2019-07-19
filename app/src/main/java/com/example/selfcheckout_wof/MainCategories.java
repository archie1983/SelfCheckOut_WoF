package com.example.selfcheckout_wof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum of the main categories that can be initially selected
 * from the left side of the screen.
 */
public enum MainCategories {
    BASES(R.drawable.noodle_base, "Pick Your Base", "nbase", MealBases.values()),
    TOPPINGS(R.drawable.noodle_toppings, "With Topping", "ntop", null),
    SAUCES(R.drawable.noodle_sauces, "And Sauce", "nsauce", null),
    EXTRAS(R.drawable.noodle_extras, "Add Some Extras", "nextra", null),
    DRINKS(R.drawable.bubble_drinks, "Choose Drink", "bdrink", null);

    public String id = "AE";
    public String label = "AE2";

    /**
     * An image resource (typically available at R.drawable.xxx)
     */
    int image_resource = 0;

    /**
     * An enum of items that are available for purchase under this category.
     */
    PurchasableGoods[] category_content = null;

    MainCategories(int image_resource, String label, String id, PurchasableGoods[] category_content) {
        this.image_resource = image_resource;
        this.id = id;
        this.label = label;
    }

    public static List<MainCategories> getMainCategoriesAsList() {
        return Arrays.asList(MainCategories.values());
    }

    public int getImage_resource() {
        return image_resource;
    }
}
