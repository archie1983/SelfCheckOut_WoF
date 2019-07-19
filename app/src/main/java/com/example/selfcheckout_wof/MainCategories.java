package com.example.selfcheckout_wof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum MainCategories {
    NOODLES_BASE(R.drawable.noodle_base, "nbase"),
    TOPPINGS(R.drawable.noodle_toppings, "ntop"),
    SAUCES(R.drawable.noodle_sauces, "nsauce"),
    EXTRAS(R.drawable.noodle_extras, "nextra"),
    DRINKS(R.drawable.bubble_drinks, "bdrink");

    public String id = "AE";
    public final String content = "AE2";

    int image_resource = 0;

    MainCategories(int image_resource, String id) {
        this.image_resource = image_resource;
        this.id = id;
    }

    public static List<MainCategories> getMainCategoriesAsList() {
        return Arrays.asList(MainCategories.values());
    }

    public int getImage_resource() {
        return image_resource;
    }
}
