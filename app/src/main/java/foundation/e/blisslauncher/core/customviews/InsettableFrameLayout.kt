package foundation.e.blisslauncher.core.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout
import foundation.e.blisslauncher.BlissLauncher

class InsettableFrameLayout(private val mContext: Context, attrs: AttributeSet?) : FrameLayout(
    mContext, attrs
), Insettable {

    override fun setInsets(insets: WindowInsets?) {
        if (insets == null) return
        val deviceProfile = BlissLauncher.getApplication(mContext).deviceProfile
        setPadding(
            paddingLeft, paddingTop,
            paddingRight, insets.systemWindowInsetBottom
        )
    }
}