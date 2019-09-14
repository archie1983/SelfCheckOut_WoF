package com.example.selfcheckout_wof.data;

import androidx.annotation.Nullable;
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
    public long parentCategoryId;

    @ColumnInfo(name = "price")
    public int price;

    /**
     * A copy constructor - useful when updating a sales item.
     *
     * @param salesItem
     */
    public SalesItems(SalesItems salesItem) {
        this.pictureUrl = salesItem.pictureUrl;
        this.label = salesItem.label;
        this.si_id = salesItem.si_id;
        this.price = salesItem.price;
        this.parentCategoryId = salesItem.parentCategoryId;
        this.description = salesItem.description;
        this.numberOfMultiSelectableItems = salesItem.numberOfMultiSelectableItems;
    }

    /**
     * Ordinary constructor to discern it from the copy constructor
     */
    public SalesItems() {
    }

    /**
     * A static method for creating a top level category
     *
     * @param label
     * @return
     */
    public static SalesItems createCategory(String label, String pictureUrl, long parentCategoryId) {
        SalesItems s = new SalesItems();
        s.label = label;
        s.description = "";
        s.numberOfMultiSelectableItems = 1;
        s.parentCategoryId = parentCategoryId;
        s.price = 0;
        s.pictureUrl = pictureUrl;

        return s;
    }

    /**
     * The ID coming from DB is already guaranteed unique
     * @return
     */
    @Override
    public int hashCode() {
        return si_id;
    }

    /**
     * When comparing two SalesItems objects, the only thing that
     * matters is their ID (at least at the time of writing)
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        SalesItems other = (SalesItems) obj;

        if (si_id == other.si_id) {
            return true;
        } else {
            return false;
        }
    }
}
