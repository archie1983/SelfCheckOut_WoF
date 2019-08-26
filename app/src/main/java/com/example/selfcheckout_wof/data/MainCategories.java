package com.example.selfcheckout_wof.data;

import com.example.selfcheckout_wof.R;

import java.util.Arrays;
import java.util.List;

/**
 * Enum of the main categories that can be initially selected
 * from the left side of the screen.
 */
public enum MainCategories {
    /*
     * For meal bases we will only want one item selected (hence 1 item selectable).
     */
    BASES(R.drawable.noodle_base, "Pick Your Base", "nbase", MealBases.values(), 1, MealBases.class),
    TOPPINGS(R.drawable.noodle_toppings, "With Topping", "ntop", MealToppings.values(), MealToppings.values().length, MealToppings.class),
    SAUCES(R.drawable.noodle_sauces, "And Sauce", "nsauce", null,0,null),
    EXTRAS(R.drawable.noodle_extras, "Add Some Extras", "nextra", null,0,null),
    DRINKS(R.drawable.bubble_drinks, "Choose Drink", "bdrink", null,0,null);

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

    Class subcategory_type = null;

    int numberOfMultiSelectableItems = 0;

    MainCategories(int image_resource,
                   String label,
                   String id,
                   PurchasableGoods[] category_content,
                   int numberOfMultiSelectableItems,
                   Class subcategory_type) {
        this.image_resource = image_resource;
        this.id = id;
        this.label = label;
        this.category_content = category_content;
        this.subcategory_type = subcategory_type;
        this.numberOfMultiSelectableItems = numberOfMultiSelectableItems;
    }

    public static List<MainCategories> getMainCategoriesAsList() {
        return Arrays.asList(MainCategories.values());
    }

    public int getImage_resource() {
        return image_resource;
    }

    public PurchasableGoods[] getCategory_content() {
        return category_content;
    }

    /**
     * Returns a number of how many purchasable goods items can be selected
     * alongside other purchasable goods items in the same category.
     *
     * @return
     */
    public int numberOfMultiSelectableItems() {
        /*
         * A number of how many items can be selected alongside other items in subcategory of this
         * category.
         */
        return numberOfMultiSelectableItems;
    }

    public static MainCategories itemBelongsToThisMainCategory(PurchasableGoods pg) {
        for (MainCategories mc : MainCategories.values()) {
            if (pg.getClass() == mc.subcategory_type) {
                return mc;
            }
        }
        return null;
    }
}
