package com.example.selfcheckout_wof.data;

import com.example.selfcheckout_wof.R;

import java.util.Arrays;
import java.util.List;

public enum MealBases implements PurchasableGoods{
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

    /*
     * Price of the meal base in PENNIES (hence it's an int and not a double)
     */
    public long price = 0;

    int image_resource = 0;

    MealBases(int image_resource, String label, String description, long price) {
        this.image_resource = image_resource;
        this.description = description;
        this.label = label;
        this.price = price;
    }

    public static List<MealBases> getItemsAsList() {
        return Arrays.asList(MealBases.values());
    }

    @Override
    public long getPrice() {
        return price;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getImage_resource() {
        return image_resource;
    }

    /**
     * Returns the image URI path that we want displayed for this purchasable goods item.
     *
     * @return
     */
    @Override
    public String getImage_path() {
        return null;
    }

    /**
     * ID of the purchasable goods.
     *
     * @return
     */
    @Override
    public int getID() {
        return 0;
    }
}
