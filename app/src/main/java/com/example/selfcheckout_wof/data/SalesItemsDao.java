package com.example.selfcheckout_wof.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SalesItemsDao {
    @Query("SELECT * FROM salesitems")
    List<SalesItems> getAll();

    @Query("SELECT * FROM salesitems WHERE parent_category=-1")
    List<SalesItems> loadTopCategories();

    @Query("SELECT * FROM salesitems WHERE parent_category=:parentId")
    List<SalesItems> loadSubCategory(int parentId);

    @Insert
    void insertAll(SalesItems... salesItems);

    @Delete
    void delete(SalesItems salesItem);
}