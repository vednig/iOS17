package foundation.e.blisslauncher.features.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import foundation.e.blisslauncher.core.Preferences
import foundation.e.blisslauncher.core.database.LauncherDB
import foundation.e.blisslauncher.features.widgets.DefaultWidgets.privacyWidget
import timber.log.Timber

object WidgetMigration {
    @JvmStatic
    fun migrateAdvancedPrivacy(context: Context) {
        Timber.tag(TAG).d("Starting widget migration for Advanced Privacy")
        val addedAp = Preferences.getAddedPrivacyWidget(context)
        val oldComponent =
            ComponentName("foundation.e.advancedprivacy", "foundation.e.privacycentralapp.Widget")

        val shouldMigrate = migrate(context, addedAp, oldComponent)
        Timber.tag(TAG).d("Should migrate?: $shouldMigrate")

        if (shouldMigrate && !Preferences.getApMigration1Status(context)) {
            Preferences.setRemovedPrivacyWidget(context)
            Preferences.setApMigration1Status(context, true)
        }
    }

    private fun migrate(context: Context, isAdded: Boolean, oldComponent: ComponentName): Boolean {
        val widgetDao = LauncherDB.getDatabase(context).widgetDao()
        var containsInDB = false

        for (widget in widgetDao.all) {
            val info = AppWidgetManager.getInstance(context).getAppWidgetInfo(widget.id)
            Timber.tag(TAG).d("Widget Id: ${widget.id} | Widget info: $info")

            if (info != null && (info.provider == oldComponent || info.provider == privacyWidget)) {
                Timber.tag(TAG).d("Migrating widget ${info.provider}")
                containsInDB = true
            }
        }

        if (containsInDB && isAdded) {
            Preferences.setApMigration1Status(context, true)
        }

        return !containsInDB && isAdded
    }

    private const val TAG = "WidgetMigration"
}
