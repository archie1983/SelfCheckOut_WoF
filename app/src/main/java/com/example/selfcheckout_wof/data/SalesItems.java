package com.example.selfcheckout_wof.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SalesItems {
    @PrimaryKey
    public int si_id;

    @ColumnInfo(name = "item_label")
    public String label;

    @ColumnInfo(name = "item_description")
    public String description;

    @ColumnInfo(name = "picture_url")
    public String pictureUrl;

    @ColumnInfo(name = "multi_selectable_items")
    public int numberOfMultiSelectableItems;

    @ColumnInfo(name = "parent_category")
    public int parentCategoryId;

    @ColumnInfo(name = "price")
    public int price;

    /**
     * A static method for creating a top level category
     *
     * @param label
     * @return
     */
    public static SalesItems createTopCategory(String label) {
        SalesItems s = new SalesItems();
        s.label = label;
        s.description = "";
        s.numberOfMultiSelectableItems = 1;
        s.parentCategoryId = -1;
        s.price = 0;
        s.pictureUrl = "";

        return s;
    }
}
