package com.cloudx.ios17.core.database.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.utils.Constants;

import java.util.List;

@Dao
public interface LauncherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LauncherItem launcherItem);

    @Query("SELECT * FROM launcher_items ORDER BY container, screen_id, cell")
    List<LauncherItem> getAllItems();

    @Query("SELECT item_id FROM launcher_items WHERE package = :packageName")
    String getComponentName(String packageName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LauncherItem> launcherItems);

    @Query("DELETE FROM launcher_items WHERE item_id = :id")
    void delete(String id);

    @Query("DELETE FROM launcher_items WHERE title = :name and item_type = " + Constants.ITEM_TYPE_SHORTCUT)
    void deleteShortcut(String name);

    @Query("UPDATE launcher_items SET item_id = :newComponentName WHERE item_id = :id")
    int updateComponent(String id, String newComponentName);
}
