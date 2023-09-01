package com.cloudx.ios17.core.customviews;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

import timber.log.Timber;

/** Created by falcon on 15/2/18. */
public class BlissDragShadowBuilder extends View.DragShadowBuilder {

    private final int mX;
    private final int mY;

    private static final String TAG = "BlissDragShadowBuilder";
    public final float yOffset;
    public final float xOffset;

    private Point mScaleFactor;

    // Defines the constructor for myDragShadowBuilder
    public BlissDragShadowBuilder(View v, float x, float y) {

        // Stores the View parameter passed to DragShadowBuilder.
        super(v);

        mX = (int) x;
        mY = (int) y;

        Timber.tag(TAG).i("Touchpoint: " + mX + " " + mY);

        xOffset = mX - v.getWidth() / 2;
        yOffset = (mY - v.getHeight() / 2);

        Timber.tag(TAG).i("Offset: " + xOffset + " " + yOffset);
    }

    // Defines a callback that sends the drag shadow dimensions and touch point back
    // to the
    // system.
    @Override
    public void onProvideShadowMetrics(Point size, Point touch) {
        // Defines local variables
        int width;
        int height;

        // Sets the width of the shadow to half the width of the original View
        width = getView().getWidth();

        // Sets the height of the shadow to half the height of the original View
        height = getView().getHeight();

        // Sets the size parameter's width and height values. These get back to the
        // system
        // through the size parameter.
        size.set(width, height);
        // Sets size parameter to member that will be used for scaling shadow image.
        mScaleFactor = size;

        // Sets the touch point's position to be in the middle of the drag shadow
        touch.set(mX, mY);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        canvas.save();
        // Draws the ColorDrawable in the Canvas passed in from the system.
        canvas.scale(mScaleFactor.x / (float) getView().getWidth(), mScaleFactor.y / (float) getView().getHeight());
        getView().draw(canvas);
        canvas.restore();
    }
}
