package foundation.e.blisslauncher.core.database.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import foundation.e.blisslauncher.core.database.model.WidgetItem;

@Dao
public interface WidgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WidgetItem widgetItem);

    @Query("UPDATE widget_items SET height = :height WHERE id = :id")
    void updateHeight(int id, int height);

    @Query("SELECT height FROM widget_items WHERE id = :id")
    int getHeight(int id);

    @Query("SELECT * FROM widget_items")
    List<WidgetItem> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WidgetItem> widgetItems);

    @Query("DELETE FROM widget_items WHERE id = :id")
    void delete(int id);
}
