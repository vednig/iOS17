package foundation.e.blisslauncher.core;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseIntArray;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import foundation.e.blisslauncher.FixedScaleDrawable;
import foundation.e.blisslauncher.core.customviews.AdaptiveIconDrawableCompat;

public class AdaptiveIconGenerator {

    // Average number of derived colors (based on averages with ~100 icons and
    // performance testing)
    private static final int NUMBER_OF_COLORS_GUESSTIMATE = 45;

    // Found after some experimenting, might be improved with some more testing
    private static final float FULL_BLEED_ICON_SCALE = 1.44f;
    // Found after some experimenting, might be improved with some more testing
    private static final float NO_MIXIN_ICON_SCALE = 1.40f;
    // Icons with less than 5 colors are considered as "single color"
    private static final int SINGLE_COLOR_LIMIT = 5;
    // Minimal alpha to be considered opaque
    private static final int MIN_VISIBLE_ALPHA = 0xEF;

    private Context context;
    private Drawable icon;

    private boolean ranLoop;
    private boolean shouldWrap;
    private int backgroundColor = Color.WHITE;
    private boolean useWhiteBackground = true;
    private boolean isFullBleed;
    private boolean noMixinNeeded;
    private boolean fullBleedChecked;
    private boolean matchesMaskShape;
    private boolean isBackgroundWhite;
    private float scale;
    private int height;
    private float aHeight;
    private int width;
    private float aWidth;
    private Drawable result;

    public AdaptiveIconGenerator(Context context, @NonNull Drawable icon) {
        this.context = context;
        this.icon = icon;
    }

    private void loop() {
        Drawable extractee = icon;

        if (extractee == null) {
            Log.e("AdaptiveIconGenerator", "extractee is null, skipping.");
            onExitLoop();
            return;
        }

        RectF bounds = new RectF();

        scale = 1.0f;

        if (extractee instanceof ColorDrawable) {
            isFullBleed = true;
            fullBleedChecked = true;
        }

        width = extractee.getIntrinsicWidth();
        height = extractee.getIntrinsicHeight();
        aWidth = width * (1 - (bounds.left + bounds.right));
        aHeight = height * (1 - (bounds.top + bounds.bottom));

        // Check if the icon is squarish
        final float ratio = aHeight / aWidth;
        boolean isSquarish = 0.999 < ratio && ratio < 1.0001;
        boolean almostSquarish = isSquarish || (0.97 < ratio && ratio < 1.005);
        if (!isSquarish) {
            isFullBleed = false;
            fullBleedChecked = true;
        }

        final Bitmap bitmap = Utilities.drawableToBitmap(extractee);
        if (bitmap == null) {
            onExitLoop();
            return;
        }

        if (!bitmap.hasAlpha()) {
            isFullBleed = true;
            fullBleedChecked = true;
        }

        final int size = height * width;
        SparseIntArray rgbScoreHistogram = new SparseIntArray(NUMBER_OF_COLORS_GUESSTIMATE);
        final int[] pixels = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        /*
         * Calculate the number of padding pixels around the actual icon (i)
         * +----------------+ | top | +---+--------+---+ | | | | | l | i | r | | | | |
         * +---+--------+---+ | bottom | +----------------+
         */
        float adjHeight = height - bounds.top - bounds.bottom;
        float l = bounds.left * width * adjHeight;
        float top = bounds.top * height * width;
        float r = bounds.right * width * adjHeight;
        float bottom = bounds.bottom * height * width;
        int addPixels = round(l + top + r + bottom);

        // Any icon with less than 10% transparent pixels (padding excluded) is
        // considered
        // "full-bleed-ish"
        final int maxTransparent = (int) (round(size * .10) + addPixels);
        // Any icon with less than 27% transparent pixels (padding excluded) doesn't
        // need a color mix-in
        final int noMixinScore = (int) (round(size * .27) + addPixels);

        int highScore = 0;
        int bestRGB = 0;
        int transparentScore = 0;
        for (int pixel : pixels) {
            int alpha = 0xFF & (pixel >> 24);
            if (alpha < MIN_VISIBLE_ALPHA) {
                // Drop mostly-transparent pixels.
                transparentScore++;
                if (transparentScore > maxTransparent) {
                    isFullBleed = false;
                    fullBleedChecked = true;
                }
                continue;
            }
            // Reduce color complexity.
            int rgb = ColorExtractor.posterize(pixel);
            if (rgb < 0) {
                // Defensively avoid array bounds violations.
                continue;
            }
            int currentScore = rgbScoreHistogram.get(rgb) + 1;
            rgbScoreHistogram.append(rgb, currentScore);
            if (currentScore > highScore) {
                highScore = currentScore;
                bestRGB = rgb;
            }
        }
        // add back the alpha channel
        bestRGB |= 0xff << 24;

        // not yet checked = not set to false = has to be full bleed, isBackgroundWhite
        // = true = is
        // adaptive
        isFullBleed |= !fullBleedChecked && !isBackgroundWhite;

        // return early if a mix-in isnt needed
        noMixinNeeded = !isFullBleed && !isBackgroundWhite && almostSquarish && transparentScore <= noMixinScore;

        // Currently, it's set to true so a white background is used for all the icons.
        if (useWhiteBackground) {
            // backgroundColor = Color.WHITE;
            backgroundColor = Color.WHITE & 0x80FFFFFF;
            onExitLoop();
            return;
        }

        if (isFullBleed || noMixinNeeded) {
            backgroundColor = bestRGB;
            onExitLoop();
            return;
        }

        // "single color"
        final int numColors = rgbScoreHistogram.size();
        boolean singleColor = numColors <= SINGLE_COLOR_LIMIT;

        // Convert to HSL to get the lightness and adjust the color
        final float[] hsl = new float[3];
        ColorUtils.colorToHSL(bestRGB, hsl);
        float lightness = hsl[2];

        boolean light = lightness > .5;
        // Apply dark background to mostly white icons
        boolean veryLight = lightness > .75 && singleColor;
        // Apply light background to mostly dark icons
        boolean veryDark = lightness < .35 && singleColor;

        // Adjust color to reach suitable contrast depending on the relationship between
        // the colors
        final int opaqueSize = size - transparentScore;
        final float pxPerColor = opaqueSize / (float) numColors;
        float mixRatio = min(max(pxPerColor / highScore, .15f), .7f);

        // Vary color mix-in based on lightness and amount of colors
        int fill = (light && !veryLight) || veryDark ? 0xFFFFFFFF : 0xFF333333;
        backgroundColor = ColorUtils.blendARGB(bestRGB, fill, mixRatio);
        onExitLoop();
    }

    private void onExitLoop() {
        ranLoop = true;
        result = genResult();
    }

    private Drawable genResult() {
        AdaptiveIconDrawableCompat tmp = new AdaptiveIconDrawableCompat(new ColorDrawable(), new FixedScaleDrawable());
        ((FixedScaleDrawable) tmp.getForeground()).setDrawable(icon);
        if (isFullBleed || noMixinNeeded) {
            float scale;
            if (noMixinNeeded) {
                float upScale = min(width / aWidth, height / aHeight);
                scale = NO_MIXIN_ICON_SCALE * upScale;
            } else {
                float upScale = max(width / aWidth, height / aHeight);
                scale = FULL_BLEED_ICON_SCALE * upScale;
            }
            ((FixedScaleDrawable) tmp.getForeground()).setScale(scale);
        } else {
            ((FixedScaleDrawable) tmp.getForeground()).setScale(scale);
        }
        ((ColorDrawable) tmp.getBackground()).setColor(backgroundColor);
        return tmp;
    }

    public Drawable getResult() {
        if (!ranLoop) {
            loop();
        }
        return result;
    }
}
