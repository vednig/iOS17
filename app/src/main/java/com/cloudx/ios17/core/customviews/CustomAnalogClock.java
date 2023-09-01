package com.cloudx.ios17.core.customviews;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;

import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.R;
import com.cloudx.ios17.BlissLauncher;

import java.util.Calendar;

/** Created by falcon on 8/3/18. */
public class CustomAnalogClock extends View {

    public static boolean is24;
    public static boolean hourOnTop;
    private Calendar mCalendar;
    private Drawable mFace;
    private int mDialWidth;
    private float sizeScale = 1f;
    private int mDialHeight;
    private int mBottom;
    private int mTop;
    private int mLeft;
    private int mRight;
    private boolean mSizeChanged;
    private HandsOverlay mHandsOverlay;
    private boolean autoUpdate;
    private static final String TAG = "CustomAnalogClock";
    private Context mContext;

    public CustomAnalogClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        handleAttrs(context, attrs);
    }

    public CustomAnalogClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        handleAttrs(context, attrs);
    }

    public CustomAnalogClock(Context context) {
        super(context);
        init(context);
    }

    private void handleAttrs(Context context, AttributeSet attrs) {
        init(context);
    }

    public void init(Context context) {
        init(context, R.drawable.clock, R.drawable.hours, R.drawable.minutes, R.drawable.seconds, 0, false, false);
    }

    /**
     * Will set the scale of the view, for example 0.5f will draw the clock with
     * half of its radius
     *
     * @param scale
     *            the scale to render the view in
     */
    public void setScale(float scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be bigger than 0");
        }
        this.sizeScale = scale;
        mHandsOverlay.withScale(sizeScale);
        invalidate();
    }

    public void setFace(int drawableRes) {
        final Resources r = getResources();
        setFace(r.getDrawable(drawableRes));
    }

    public void init(Context context, int watchFace, int hourHand, int minuteHand, int secHand, int alpha, boolean is24,
            boolean hourOnTop) {
        this.mContext = context;
        CustomAnalogClock.is24 = is24;

        CustomAnalogClock.hourOnTop = hourOnTop;
        setFace(watchFace);
        Drawable Hhand = ContextCompat.getDrawable(context, hourHand);
        assert Hhand != null;
        if (alpha > 0) {
            Hhand.setAlpha(alpha);
        }

        Drawable Mhand = ContextCompat.getDrawable(context, minuteHand);

        Drawable SHand = ContextCompat.getDrawable(context, secHand);

        mCalendar = Calendar.getInstance();

        mHandsOverlay = new HandsOverlay(Hhand, Mhand, SHand).withScale(sizeScale);
        mHandsOverlay.setShowSeconds(true);
        setScale((float) BlissLauncher.getApplication(mContext).getDeviceProfile().iconSizePx / mDialWidth);
    }

    public void setFace(Drawable face) {
        mFace = face;
        mDialHeight = mFace.getIntrinsicHeight();
        mDialWidth = mFace.getIntrinsicWidth();
        mSizeChanged = true;
        invalidate();
    }

    /**
     * Sets the currently displayed time in {@link System#currentTimeMillis()} time.
     *
     * @param time
     *            the time to display on the clock
     */
    public void setTime(long time) {
        mCalendar.setTimeInMillis(time);
        invalidate();
    }

    /**
     * Sets the currently displayed time.
     *
     * @param calendar
     *            The time to display on the clock
     */
    public void setTime(Calendar calendar) {
        mCalendar = calendar;
        invalidate();
        if (autoUpdate) {
            new Handler().postDelayed(() -> setTime(Calendar.getInstance()), 1000);
        }
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        setTime(Calendar.getInstance());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mSizeChanged = true;
    }

    // some parts from AnalogClock.java
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final boolean sizeChanged = mSizeChanged;
        if (sizeChanged) {
            mSizeChanged = false;
        }

        final int availW = mRight - mLeft;
        final int availH = mBottom - mTop;

        final int cX = availW / 2;
        final int cY = availH / 2;

        final int w = (int) (mDialWidth * sizeScale);
        final int h = (int) (mDialHeight * sizeScale);

        boolean scaled = false;

        if (availW < w || availH < h) {
            scaled = true;
            final float scale = Math.min((float) availW / (float) w, (float) availH / (float) h);
            canvas.save();
            canvas.scale(scale, scale, cX, cY);
        }

        if (sizeChanged) {
            // Extend bottom by 1 and top by -1 for proper bounds
            mFace.setBounds(cX - (w / 2), cY - (h / 2) - 1, cX + (w / 2), cY + (h / 2) + 1);
        }

        mFace.draw(canvas);
        mHandsOverlay.onDraw(canvas, cX, cY, w, h, mCalendar, sizeChanged);

        if (scaled) {
            canvas.restore();
        }
    }

    // from AnalogClock.java
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float) heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSizeAndState((int) (mDialWidth * scale), widthMeasureSpec, 0),
                resolveSizeAndState((int) (mDialHeight * scale), heightMeasureSpec, 0));
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return (int) (mDialHeight * sizeScale);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return (int) (mDialWidth * sizeScale);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mRight = right;
        mLeft = left;
        mTop = top;
        mBottom = bottom;
    }
}
