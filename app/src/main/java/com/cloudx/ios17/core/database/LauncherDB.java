package com.cloudx.ios17.core.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.cloudx.ios17.core.database.converters.CharSequenceConverter;
import com.cloudx.ios17.core.database.daos.LauncherDao;
import com.cloudx.ios17.core.database.daos.WidgetDao;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.database.model.WidgetItem;

@Database(entities = {LauncherItem.class, WidgetItem.class}, version = 5, exportSchema = false)
@TypeConverters({CharSequenceConverter.class})
public abstract class LauncherDB extends RoomDatabase {

    public abstract LauncherDao launcherDao();

    public abstract WidgetDao widgetDao();

    private static volatile LauncherDB INSTANCE;

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `widget_items` (`id` INTEGER NOT NULL, `height` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `widget_items` RENAME TO `widget_items_old`");
            database.execSQL("CREATE TABLE IF NOT EXISTS `widget_items` (" + "`id` INTEGER NOT NULL, "
                    + "`height` INTEGER NOT NULL DEFAULT 0, " + "`order` INTEGER NOT NULL DEFAULT 99999, "
                    + "PRIMARY KEY(`id`))");
            database.execSQL(
                    "INSERT INTO `widget_items` (`id`, `height`) SELECT `id`, `height` FROM `widget_items_old`");
            database.execSQL("DROP TABLE `widget_items_old`");
        }
    };

    public static LauncherDB getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (LauncherDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), LauncherDB.class, "launcher_db")
                            .addMigrations(MIGRATION_3_4, MIGRATION_4_5).build();
                }
            }
        }
        return INSTANCE;
    }

    private static final class UserHandleMigration extends Migration {
        private long userSerialNumber;

        public UserHandleMigration(int startVersion, int endVersion, long userSerialNumber) {
            super(startVersion, endVersion);
            this.userSerialNumber = userSerialNumber;
        }

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String suffix = "\"/" + userSerialNumber + "\"";
            String query = "UPDATE launcher_items set item_id=item_id || " + suffix + "WHERE item_type <> 2";
            database.execSQL(query);
        }
    }
}
