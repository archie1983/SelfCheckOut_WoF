package com.example.selfcheckout_wof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum of the main categories that can be initially selected
 * from the left side of the screen.
 */
public enum MainCategories {
    BASES(R.drawable.noodle_base, "Pick Your Base", "nbase"),
    TOPPINGS(R.drawable.noodle_toppings, "With Topping", "ntop"),
    SAUCES(R.drawable.noodle_sauces, "And Sauce", "nsauce"),
    EXTRAS(R.drawable.noodle_extras, "Add Some Extras", "nextra"),
    DRINKS(R.drawable.bubble_drinks, "Choose Drink", "bdrink");

    public String id = "AE";
    public String label = "AE2";

    int image_resource = 0;

    MainCategories(int image_resource, String label, String id) {
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
