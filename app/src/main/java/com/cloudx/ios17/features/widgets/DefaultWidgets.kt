package com.cloudx.ios17.features.widgets

import android.content.ComponentName
import com.cloudx.ios17.features.weather.WeatherAppWidgetProvider

object DefaultWidgets {
    val ecloudWidget =
        ComponentName("foundation.e.drive", "foundation.e.drive.widgets.EDriveWidget")
    val privacyWidget =
        ComponentName("foundation.e.advancedprivacy", "foundation.e.advancedprivacy.Widget")
    val weatherWidget = com.cloudx.ios17.features.weather.WeatherAppWidgetProvider.COMPONENT_NAME

    val widgets = setOf(ecloudWidget, privacyWidget)
}
