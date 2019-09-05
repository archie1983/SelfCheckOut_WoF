package com.example.selfcheckout_wof.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Representation of a row in SalesItems table in the db.
 * This is mostly a container class to represent a sales item,
 * and it may seem confusing that it's called "sales items" and
 * not "sales item", but that is because the name of the db
 * entity is called SalesItems (as it contains many sales items)
 * and just so happens that the corresponding POJO (if annotated)
 * with the db annotations, is called the same as the entity.
 *
 * This class also contains a static function to create a new
 * SalesItems entry, that can then be stored into the db.
 */
@Entity
public class SalesItems {
    @PrimaryKey(autoGenerate = true)
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
    public static SalesItems createTopCategory(String label, String pictureUrl) {
        SalesItems s = new SalesItems();
        s.label = label;
        s.description = "";
        s.numberOfMultiSelectableItems = 1;
        s.parentCategoryId = -1;
        s.price = 0;
        s.pictureUrl = pictureUrl;

        return s;
    }
}
