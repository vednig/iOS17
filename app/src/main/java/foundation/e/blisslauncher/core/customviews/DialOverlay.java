package foundation.e.blisslauncher.core.customviews;


import android.graphics.Canvas;

import java.util.Calendar;

/**
 * An overlay for a clock dial.
 */
public interface DialOverlay {

    /**
     * Subclasses should implement this to draw the overlay.
     *
     * @param canvas   the canvas onto which you must draw
     * @param cX       the x coordinate of the center
     * @param cY       the y coordinate of the center
     * @param w        the width of the canvas
     * @param h        the height of the canvas
     * @param calendar the desired date/time
     */
    void onDraw(Canvas canvas, float cX, float cY, int w, int h, Calendar calendar,
            boolean sizeChanged);

}
