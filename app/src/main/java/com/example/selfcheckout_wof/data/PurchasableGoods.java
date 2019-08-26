package com.example.selfcheckout_wof.data;

import java.util.Arrays;
import java.util.List;

/**
 * An interface for all the enums that I'll use to specify goods, that can be added
 * to invoice and purchased.
 */
public interface PurchasableGoods {
    //public static List<PurchasableGoods> getItemsAsList();
    public int getPrice();
    public String getDescription();
    public String getLabel();

    /**
     * Returns the image that we want displayed for this purchasable goods item.
     *
     * @return
     */
    public int getImage_resource();

    /**
     * Returns a number of how many purchasable goods items can be selected
     * alongside other purchasable goods items in the same category.
     *
     * @return
     */
    public int numberOfMultiSelectableItems();
}
