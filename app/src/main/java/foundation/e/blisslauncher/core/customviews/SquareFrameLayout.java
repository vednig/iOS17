package foundation.e.blisslauncher.core.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.e.blisslauncher.core.DeviceProfile;
import foundation.e.blisslauncher.core.blur.BlurViewDelegate;

/**
 * Created by falcon on 9/3/18.
 */

public class SquareFrameLayout extends FrameLayout {

    private BlurViewDelegate mBlurDelegate = null;

    public SquareFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public SquareFrameLayout(@NonNull Context context,
            @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int size = width < height ? width : height;
        setMeasuredDimension(size, size);
    }

    public void enableBlur() {
        mBlurDelegate = new BlurViewDelegate(this, null);
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
