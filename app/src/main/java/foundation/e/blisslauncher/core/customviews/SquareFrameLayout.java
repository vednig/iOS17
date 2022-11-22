package foundation.e.blisslauncher.core.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.DeviceProfile;
import foundation.e.blisslauncher.core.blur.BlurViewDelegate;
import foundation.e.blisslauncher.core.blur.BlurWallpaperProvider;

/** Created by falcon on 9/3/18. */
public class SquareFrameLayout extends FrameLayout {

    private BlurViewDelegate mBlurDelegate = null;

    public SquareFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public SquareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    public void enableBlur() {
        mBlurDelegate = new BlurViewDelegate(this, BlurWallpaperProvider.Companion.getBlurConfigAppGroup(), null);
        mBlurDelegate.setOverlayColor(getContext().getColor(R.color.blur_overlay));
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBlurDelegate != null) {
            int count = canvas.save();
            canvas.clipPath(DeviceProfile.path);
            mBlurDelegate.draw(canvas);
            canvas.restoreToCount(count);
        }
        super.onDraw(canvas);
    }
}
