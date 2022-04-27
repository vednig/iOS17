package foundation.e.blisslauncher.features.widgets

import android.content.ComponentName

object DefaultWidgets {
    val ecloudWidget = ComponentName("foundation.e.drive", "foundation.e.drive.widgets.EDriveWidget")
    val privacyWidget = ComponentName("foundation.e.advancedprivacy", "foundation.e.privacycentralapp.Widget")

    val widgets = setOf(ecloudWidget, privacyWidget)
}
