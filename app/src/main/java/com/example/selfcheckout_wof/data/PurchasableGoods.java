package com.example.selfcheckout_wof.data;

/**
 * An interface for all the enums that I'll use to specify goods, that can be added
 * to invoice and purchased.
 */
public interface PurchasableGoods {
    //public static List<PurchasableGoods> getItemsAsList();
    public long getPrice();
    public String getDescription();
    public String getLabel();

    /**
     * Returns the image that we want displayed for this purchasable goods item.
     *
     * @return
     */
    public int getImage_resource();

    /**
     * Returns the image URI path that we want displayed for this purchasable goods item.
     *
     * @return
     */
    public String getImage_path();

    /**
     * ID of the purchasable goods.
     * @return
     */
    public int getID();

    /**
     * Parent ID of the purchasable goods.
     * @return
     */
    public int getParentID();

    /**
     * How many items simultaneously (with the same parent) can be selected with this one
     * @return
     */
    public int getNumberOfMultiSelectableItems();

    /**
     * The page that this item belongs to
     * @return
     */
    public int getPage();

    /**
     * The bard code of the item.
     * @return
     */
    public String getBarCode();
}
