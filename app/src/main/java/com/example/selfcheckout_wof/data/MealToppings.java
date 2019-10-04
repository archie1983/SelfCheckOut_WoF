package com.example.selfcheckout_wof.data;

import com.example.selfcheckout_wof.R;

import java.util.Arrays;
import java.util.List;

public enum MealToppings implements PurchasableGoods{
    CHICKEN(R.drawable.dragndrop, "Chicken", "", 210),
    BEEF(R.drawable.dragndrop, "Beef", "", 220),
    SHRIMPS(R.drawable.dragndrop, "Shrimps", "", 210),
    DUCK(R.drawable.dragndrop, "Duck", "", 220),
    FISH_BALLS(R.drawable.dragndrop, "Fish Balls", "", 210),
    MUSSLES(R.drawable.dragndrop, "Mussles", "", 210),
    SHITAKE_MUSHROOMS(R.drawable.dragndrop, "Shitake Mushrooms", "", 150),
    CUP_MUSHROOMS(R.drawable.dragndrop, "Cup Mushrooms", "", 120),
    PEPPER_MIX(R.drawable.dragndrop, "Pepper Mix", "", 110),
    CASHEW_NUTS(R.drawable.dragndrop, "Cashew Nuts", "", 100),
    BROCOLI(R.drawable.dragndrop, "Brocoli", "", 150),
    TOFU(R.drawable.dragndrop, "Tofu", "", 150),
    PINEAPPLE(R.drawable.dragndrop, "Pineaple", "", 100),
    BAMBOO_SHOOTS(R.drawable.dragndrop, "Bamboo Shoots", "", 100),
    RED_ONIONS(R.drawable.dragndrop, "Red Onions", "", 90),
    PAK_CHOI(R.drawable.dragndrop, "Pak Choi", "", 140),
    BABY_CORN(R.drawable.dragndrop, "Baby Corn", "", 125),
    SWEETCORN(R.drawable.dragndrop, "Sweetcorn", "", 50);

    public String description = "AE";
    public String label = "AE2";

    /*
     * Price of the meal base in PENNIES (hence it's an int and not a double)
     */
    public long price = 0;

    int image_resource = 0;

    MealToppings(int image_resource, String label, String description, long price) {
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