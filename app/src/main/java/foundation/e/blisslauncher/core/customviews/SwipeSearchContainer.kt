package foundation.e.blisslauncher.core.customviews

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import foundation.e.blisslauncher.core.utils.OffsetParent

class SwipeSearchContainer
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), OffsetParent {

    private val offsetParentDelegate = OffsetParent.OffsetParentDelegate()

    override val offsetX: Float
        get() = translationX
    override val offsetY: Float
        get() = translationY

    override fun setTranslationX(translationX: Float) {
        super.setTranslationX(translationX)
        offsetParentDelegate.notifyOffsetChanged()
    }

    override fun setTranslationY(translationY: Float) {
        super.setTranslationY(translationY)
        offsetParentDelegate.notifyOffsetChanged()
    }

    override fun addOnOffsetChangeListener(listener: OffsetParent.OnOffsetChangeListener) {
        offsetParentDelegate.addOnOffsetChangeListener(listener)
    }

    override fun removeOnOffsetChangeListener(listener: OffsetParent.OnOffsetChangeListener) {
        offsetParentDelegate.removeOnOffsetChangeListener(listener)
    }
}
