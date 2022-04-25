package foundation.e.blisslauncher.core.utils

import java.util.concurrent.CopyOnWriteArrayList

interface OffsetParent {

    val offsetX: Float
    val offsetY: Float

    fun addOnOffsetChangeListener(listener: OnOffsetChangeListener)
    fun removeOnOffsetChangeListener(listener: OnOffsetChangeListener)

    interface OnOffsetChangeListener {
        fun onOffsetChange()
    }

    class OffsetParentDelegate() {
        private val listeners = CopyOnWriteArrayList<OnOffsetChangeListener>()

        fun notifyOffsetChanged() {
            listeners.forEach { it.onOffsetChange() }
        }

        fun addOnOffsetChangeListener(listener: OnOffsetChangeListener) {
            listeners.add(listener)
        }

        fun removeOnOffsetChangeListener(listener: OnOffsetChangeListener) {
            listeners.remove(listener)
        }
    }
}
