package com.example.selfcheckout_wof;

import java.util.Arrays;
import java.util.List;

public enum MealBases {
    EGG_NOODLES(R.drawable.dragndrop, "Egg Noodles", "With Fresh Vegetable & Egg", 425),
    WHOLE_WHEAT_NOODLES(R.drawable.dragndrop, "Whole-Wheat Noodles", "With Fresh Vegetable & Egg", 425),
    RICE_NOODLES(R.drawable.dragndrop, "Rice Noodles", "With Fresh Vegetable & Egg", 425),
    UDON_NOODLES(R.drawable.dragndrop, "Udon Noodles", "With Fresh Vegetable & Egg", 425),
    VERMICELLI_NOODLES(R.drawable.dragndrop, "Vermicelli Noodles", "With Fresh Vegetable & Egg", 425),
    JASMINE_RICE(R.drawable.dragndrop, "Jasmine Rice", "With Fresh Vegetable & Egg", 425),
    WHOLE_GRAIN_RICE(R.drawable.dragndrop, "Whole-Grain Rice", "With Fresh Vegetable & Egg", 425),
    THE_VEGGIE_DISH(R.drawable.dragndrop, "The Veggie Dish", "Brocoli, Mushroom and Fresh Vegetable", 425);

    public String description = "AE";
    public String label = "AE2";

    /**
     * Price of the meal base in PENNIES (hence it's an int and not a double)
     */
    public int price = 0;

    int image_resource = 0;

    MealBases(int image_resource, String label, String description, int price) {
        this.image_resource = image_resource;
        this.description = description;
        this.label = label;
        this.price = price;
    }

    public static List<MainCategories> getMealBasesAsList() {
        return Arrays.asList(MainCategories.values());
    }

    public int getImage_resource() {
        return image_resource;
    }
}
