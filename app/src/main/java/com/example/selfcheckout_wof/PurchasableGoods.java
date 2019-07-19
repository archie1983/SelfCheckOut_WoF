package com.example.selfcheckout_wof;

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
    public int getImage_resource();
}
