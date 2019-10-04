package com.example.selfcheckout_wof.data;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SalesItemsDao {
    @Query("SELECT * FROM salesitems")
    List<SalesItems> getAll();

    @Query("SELECT * FROM salesitems WHERE parent_category=-1")
    List<SalesItems> loadTopCategories();

    @Query("SELECT * FROM salesitems WHERE page=:page AND parent_category=:parent")
    List<SalesItems> loadPage(int page, int parent);

    @Query("SELECT count(si_id) FROM salesitems WHERE page=:page AND parent_category=:parent")
    int numberOfItemsInPage(int page, int parent);

    /**
     * This cursor contains one extra dummy category called "NO PARENT". This is important as the rest
     * of the code expects that.
     * @return
     */
    @Query("select -1 as _id, 'NO PARENT' as item_label union all SELECT si_id as _id, item_label FROM salesitems WHERE parent_category=-1")
    Cursor loadTopCategoriesForDropDownBox();

    @Query("SELECT * FROM salesitems WHERE parent_category=:parentId order by page")
    List<SalesItems> loadSubCategory(int parentId);

    @Insert
    void insertAll(SalesItems... salesItems);

    @Delete
    void delete(SalesItems salesItem);

    @Update
    void update(SalesItems salesItem);

    @Query("DELETE FROM salesitems WHERE si_id=:salesItem_id")
    void deleteByID(int salesItem_id);

    /**
     * This is how it should be done, when we have a hierarchical entity and we need
     * a hierarchical query. Unfortunately though Room doesn't support it for some
     * reason- it just paints part of it red and complains. Therefore we'll need to
     * use multiple ordinary queries.
     */
//    @Query("with RECURSIVE r_sales_items(si_id, item_label, item_description, picture_url, multi_selectable_items, parent_category, price, r_level) as " +
//            "(" +
//            "  SELECT *, 0 as r_level from sales_items where parent_category=-1 " +
//            " UNION ALL " +
//            " SELECT si.*, " +
//            " rsi.r_level + 1 as r_level " +
//            " from r_sales_items rsi, sales_items si " +
//            " where rsi.si_id = si.parent_category " +
//            "  order by r_level desc " +
//            ") " +
//            "select si_id, item_label, item_description, picture_url, multi_selectable_items, parent_category, price from r_sales_items")
//    List<SalesItems> loadHierarchicalCategories();
}