package com.cloudx.ios17.features.weather;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.R;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.Constants;
import timber.log.Timber;

public class WeatherIconUtils {
    private static final String TAG = "WeatherIconUtils";
    private static boolean D = Constants.DEBUG;

    public static int getWeatherIconResource(Context context, String iconSet, int conditionCode) {
        if (iconSet.startsWith("ext:") || iconSet.equals(Constants.MONOCHROME)) {
            return 0;
        }

        final Resources res = context.getResources();
        final int resId = res.getIdentifier(
                "weather_" + iconSet + "_" + WeatherUtils.addOffsetToConditionCodeFromWeatherContract(conditionCode),
                "drawable", context.getPackageName());

        if (resId != 0) {
            return resId;
        }

        // Use the default color set unknown icon
        return R.drawable.weather_color_na;
    }

    public static Bitmap getWeatherIconBitmap(Context context, String iconSet, int color, int conditionCode) {
        return getWeatherIconBitmap(context, iconSet, color, conditionCode, 0);
    }

    public static Bitmap getWeatherIconBitmap(Context context, String iconSet, int color, int conditionCode,
            int density) {
        boolean isMonoSet = Constants.MONOCHROME.equals(iconSet);
        Resources res = null;
        int resId = 0;
        int fixedConditionCode = WeatherUtils.addOffsetToConditionCodeFromWeatherContract(conditionCode);

        if (iconSet.startsWith("ext:")) {
            String packageName = iconSet.substring(4);
            try {
                res = context.getPackageManager().getResourcesForApplication(packageName);
                resId = res.getIdentifier("weather_" + fixedConditionCode, "drawable", packageName);
            } catch (PackageManager.NameNotFoundException e) {
                // fall back to colored icons
                iconSet = Constants.COLOR_STD;
            }
        }
        if (resId == 0) {
            String identifier = isMonoSet
                    ? "weather_" + fixedConditionCode
                    : "weather_" + iconSet + "_" + fixedConditionCode;
            res = context.getResources();
            resId = res.getIdentifier(identifier, "drawable", context.getPackageName());
        }

        if (resId == 0) {
            resId = isMonoSet ? R.drawable.weather_na : R.drawable.weather_color_na;
        }

        return getOverlaidBitmap(res, resId, isMonoSet ? color : 0, density);
    }

    public static Bitmap getOverlaidBitmap(Resources res, int resId, int color) {
        return getOverlaidBitmap(res, resId, color, 0);
    }

    public static Bitmap getOverlaidBitmap(Resources res, int resId, int color, int density) {
        Bitmap src = getBitmapFromResource(res, resId, density);
        if (color == 0 || src == null) {
            return src;
        }

        final Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(dest);
        final Paint paint = new Paint();

        // Overlay the selected color and set the imageview
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        c.drawBitmap(src, 0, 0, paint);
        return dest;
    }

    public static Bitmap getBitmapFromResource(Resources res, int resId, int density) {
        if (density == 0) {
            if (D)
                Timber.tag(TAG).d("Decoding resource id = " + resId + " for default density");
            return BitmapFactory.decodeResource(res, resId);
        }

        if (D)
            Timber.tag(TAG).d("Decoding resource id = " + resId + " for density = " + density);
        Drawable d = res.getDrawableForDensity(resId, density);
        if (d instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) d;
            return bd.getBitmap();
        }

        Bitmap result = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        d.setBounds(0, 0, result.getWidth(), result.getHeight());
        d.draw(canvas);
        canvas.setBitmap(null);

        return result;
    }

    public static int getNextHigherDensity(Context context) {
        Resources res = context.getResources();
        int density = res.getDisplayMetrics().densityDpi;

        if (density == DisplayMetrics.DENSITY_LOW) {
            return DisplayMetrics.DENSITY_MEDIUM;
        } else if (density == DisplayMetrics.DENSITY_MEDIUM) {
            return DisplayMetrics.DENSITY_HIGH;
        } else if (density == DisplayMetrics.DENSITY_HIGH) {
            return DisplayMetrics.DENSITY_XHIGH;
        } else if (density == DisplayMetrics.DENSITY_XHIGH) {
            return DisplayMetrics.DENSITY_XXHIGH;
        } else if (density == DisplayMetrics.DENSITY_XXHIGH) {
            return DisplayMetrics.DENSITY_XXXHIGH;
        }

        // fallback: use current density
        return density;
    }
}
