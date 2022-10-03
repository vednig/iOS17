package foundation.e.blisslauncher.core.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "widget_items")
public class WidgetItem {

    public static final int DEFAULT_ORDER = 99999;

    @PrimaryKey
    public int id;

    @ColumnInfo(defaultValue = "0")
    public int height = 0;

    @ColumnInfo(defaultValue = "99999")
    public int order = DEFAULT_ORDER;

    public WidgetItem() {
    }

    public WidgetItem(int id) {
        this.id = id;
    }
}
