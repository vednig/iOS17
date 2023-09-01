/*
 * Copyright 2018 /e/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudx.ios17.core.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.R;
import com.cloudx.ios17.core.DeviceProfile;
import com.cloudx.ios17.core.database.model.FolderItem;
import com.cloudx.ios17.BlissLauncher;
import timber.log.Timber;

import java.util.HashMap;
import java.util.Map;

public class GraphicsUtil {

    private static final String TAG = "BLISS_GRAPHICS";
    private final Context mContext;
    private final int appIconWidth;
    private final int mFolderPreviewBackgroundColor;

    public GraphicsUtil(Context context) {
        this.mContext = context;
        DeviceProfile deviceProfile = BlissLauncher.getApplication(context).getDeviceProfile();
        this.appIconWidth = deviceProfile.iconSizePx;
        mFolderPreviewBackgroundColor = mContext.getColor(R.color.folder_preview_background_color);
    }

    /**
     * Takes 1 or more drawables and merges them to form a single Drawable. However,
     * if more than 4 drawables are provided, only the first 4 are used.
     */
    public Drawable generateFolderIcon(Context context, Drawable... sources) {
        int width = appIconWidth;
        int height = width; // Square icons

        Timber.tag(TAG).i("generateFolderIcon: " + width + "*" + height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int xOrigin = bitmap.getWidth() / 10;
        int yOrigin = bitmap.getHeight() / 10;
        int x = xOrigin;
        int y = yOrigin;
        int xIncrement = bitmap.getWidth() / 10;
        int yIncrement = bitmap.getHeight() / 10;
        int count = 0;
        int total = 0;
        for (Drawable d : sources) {
            d.setBounds(x, y, (int) (x + width / 5f), (int) (y + height / 5f));
            d.draw(canvas);
            x += (int) (width / 5f + xIncrement);
            count++;
            total++;
            if (count == 3) {
                count = 0;
                y += (int) (height / 5f + yIncrement);
                x = xOrigin;
            }
            if (total > 8) {
                break;
            }
        }

        Drawable convertedBitmap = convertToRoundedCorner(context, addBackground(bitmap, true));
        return convertedBitmap;
    }

    /**
     * A utility method that simplifies calls to the generateFolderIcon() method
     * that expects an array of Drawables.
     */
    public Drawable generateFolderIcon(Context context, FolderItem app) {
        Drawable[] drawables = new Drawable[app.items.size()];
        for (int i = 0; i < app.items.size(); i++) {
            drawables[i] = app.items.get(i).icon;
        }
        return generateFolderIcon(context, drawables);
    }

    /** Scales icons to match the icon pack */
    public Drawable scaleImage(Context context, Drawable image, float scaleFactor) {
        if ((image == null) || !(image instanceof BitmapDrawable)) {
            return image;
        }
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        int sizeX = Math.round(image.getIntrinsicWidth() * scaleFactor);
        int sizeY = Math.round(image.getIntrinsicHeight() * scaleFactor);
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, sizeX, sizeY, false);
        image = new BitmapDrawable(context.getResources(), bitmapResized);
        return image;
    }

    public Bitmap addBackground(Bitmap bitmap, boolean isFolder) {
        if (!hasTransparency(bitmap)) {
            bitmap = Bitmap.createScaledBitmap(bitmap, appIconWidth,
                    (appIconWidth * bitmap.getHeight() / bitmap.getWidth()), true);
            return bitmap;
        }
        if (bitmap.getWidth() >= appIconWidth) {
            bitmap = Bitmap.createScaledBitmap(bitmap, appIconWidth,
                    (appIconWidth * bitmap.getHeight() / bitmap.getWidth()), true);
        }

        int width = appIconWidth;
        int height = width;
        Bitmap mergedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mergedBitmap);
        canvas.drawColor(isFolder ? mFolderPreviewBackgroundColor : getDominantColor(bitmap));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, (width - bitmap.getWidth()) / 2, (height - bitmap.getHeight()) / 2, paint);
        return mergedBitmap;
    }

    public Bitmap addBackground(Drawable appIcon, boolean isFolder) {
        Bitmap bitmap;
        if (appIcon instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) appIcon).getBitmap();
        } else {
            bitmap = Bitmap.createBitmap(appIcon.getIntrinsicWidth(), appIcon.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            appIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            appIcon.draw(canvas);
        }
        return addBackground(bitmap, isFolder);
    }

    public BitmapDrawable convertToRoundedCorner(Context context, Bitmap src) {
        return new BitmapDrawable(context.getResources(), BitmapUtils.getCroppedBitmap(src, DeviceProfile.path));
    }

    public boolean hasTransparency(Bitmap bitmap) {
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                if (Color.alpha(bitmap.getPixel(x, y)) < 255) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Finds the color with the most occurrences inside of a bitmap.
     *
     * @param drawable
     *            to get the dominant color of
     * @return the dominant color
     */
    public int getDominantColor(Drawable drawable) {
        return getDominantColor(((BitmapDrawable) drawable).getBitmap());
    }

    /**
     * Finds the color with the most occurrences inside of a bitmap.
     *
     * @param bitmap
     *            the bitmap to get the dominant color of
     * @return the dominant color
     */
    private int getDominantColor(Bitmap bitmap) {
        @SuppressLint("UseSparseArrays")
        Map<Integer, Integer> colors = new HashMap<>();

        int count = 0;

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (Color.alpha(bitmap.getPixel(x, y)) == 255) {
                    int color = bitmap.getPixel(x, y);
                    colors.put(color, (colors.containsKey(color) ? colors.get(color) : 0) + 1);
                }
            }
        }

        int color = Color.TRANSPARENT;
        int occurrences = 0;
        if (colors.keySet().size() > 1) {
            for (Integer key : colors.keySet()) {
                if (colors.get(key) > occurrences) {
                    occurrences = colors.get(key);
                    color = key;
                }
            }

            return color;
        } else {
            Timber.tag(TAG).i("getDominantColor: white");
            return Color.WHITE;
        }
    }
}
