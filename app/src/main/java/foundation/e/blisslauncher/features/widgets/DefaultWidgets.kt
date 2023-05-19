package foundation.e.blisslauncher.features.widgets

import android.content.ComponentName
import foundation.e.blisslauncher.features.weather.WeatherAppWidgetProvider

object DefaultWidgets {
    val ecloudWidget =
        ComponentName("foundation.e.drive", "foundation.e.drive.widgets.EDriveWidget")
    val privacyWidget =
        ComponentName("foundation.e.advancedprivacy", "foundation.e.advancedprivacy.Widget")
    val weatherWidget = WeatherAppWidgetProvider.COMPONENT_NAME

    val widgets = setOf(ecloudWidget, privacyWidget)
}
