package foundation.e.blisslauncher.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import foundation.e.blisslauncher.BlissLauncher;
import foundation.e.blisslauncher.core.customviews.AdaptiveIconDrawableCompat;
import foundation.e.blisslauncher.core.utils.GraphicsUtil;
import foundation.e.blisslauncher.core.utils.UserHandle;

/**
 * Inspired from
 * http://stackoverflow.com/questions/31490630/how-to-load-icon-from-icon-pack
 */
public class IconsHandler {

    private static final String TAG = "IconsHandler";
    // map with available icons packs
    private final HashMap<String, String> iconsPacks = new HashMap<>();
    // map with available drawable for an icons pack
    private final Map<String, String> packagesDrawables = new HashMap<>();
    // graphics util to manipulate bitmaps.
    private final GraphicsUtil graphicsUtil;
    private final int mIconDpi;
    // instance of a resource object of an icon pack
    private Resources iconPackres;
    // package name of the icons pack
    private String iconsPackPackageName;
    // bitmap mask of an icons pack
    private Bitmap maskImage = null;
    // front image of an icons pack
    private Bitmap frontImage = null;
    // scale factor of an icons pack
    private float factor = 1.0f;
    private final PackageManager pm;
    private final Context ctx;

    public IconsHandler(Context ctx) {
        super();
        this.ctx = ctx;
        this.pm = ctx.getPackageManager();
        graphicsUtil = new GraphicsUtil(ctx);
        mIconDpi = BlissLauncher.getApplication(ctx).getDeviceProfile().fillResIconDpi;
        loadIconsPack("foundation.e.blissiconpack");
    }

    private boolean iconPackExists(PackageManager packageManager) {
        try {
            packageManager.getPackageInfo("foundation.e.blissiconpack", PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * Parse icons pack metadata
     *
     * @param packageName
     *            Android package ID of the package to parse
     */
    public void loadIconsPack(String packageName) {

        // clear icons pack
        if (iconPackExists(pm)) {
            iconsPackPackageName = packageName;
        } else {
            iconsPackPackageName = "default";
        }

        packagesDrawables.clear();

        // system icons, nothing to do
        if (iconsPackPackageName.equalsIgnoreCase("default")) {
            return;
        }

        XmlResourceParser xpp = null;

        try {
            // search appfilter.xml into icons pack apk resource folder
            iconPackres = pm.getResourcesForApplication(iconsPackPackageName);
            Context iconPackContext = ctx.createPackageContext(iconsPackPackageName, 0);
            InputStream stream = iconPackContext.getAssets().open("appfilter.xml");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
            NodeList items = doc.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                String componentName = item.getAttributes().getNamedItem("component").getTextContent();
                String drawableName = item.getAttributes().getNamedItem("drawable").getTextContent();

                if (!packagesDrawables.containsKey(componentName)) {
                    packagesDrawables.put(componentName, drawableName);
                }
            }
            Log.i(TAG, "Cached " + packagesDrawables.size() + " icons");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error parsing appfilter.xml " + e);
        }

    }

    private Drawable getDefaultAppDrawable(LauncherActivityInfo activityInfo, UserHandle userHandle) {
        return activityInfo.getIcon(0);
    }

    public boolean isClock(String componentName) {
        return packagesDrawables.get(componentName) != null && packagesDrawables.get(componentName).equals("clock");
    }

    public boolean isCalendar(String componentName) {
        return packagesDrawables.get(componentName) != null && packagesDrawables.get(componentName).equals("calendar");
    }

    /**
     * Get or generate icon for an app
     */
    public Drawable getDrawableIconForPackage(LauncherActivityInfo activityInfo, UserHandle userHandle) {
        /*
         * // system icons, nothing to do if
         * (iconsPackPackageName.equalsIgnoreCase("default")) { return
         * this.getDefaultAppDrawable(componentName, userHandle); }
         */

        ComponentName componentName = activityInfo.getComponentName();
        String drawable = packagesDrawables.get(activityInfo.getComponentName().toString());
        if (drawable != null) { // there is a custom icon
            int id = iconPackres.getIdentifier(drawable, "drawable", iconsPackPackageName);
            if (id > 0) {
                // noinspection deprecation: Resources.getDrawable(int, Theme) requires SDK 21+
                try {
                    return getBadgedIcon(iconPackres.getDrawable(id), activityInfo.getUser());
                } catch (Resources.NotFoundException e) {
                    // Unable to load icon, keep going.
                    e.printStackTrace();
                }
            }
        }

        String key = userHandle.addUserSuffixToString(componentName.flattenToString(), '/');

        // Search first in cache
        Drawable systemIcon = cacheGetDrawable(key);
        if (systemIcon != null) {
            return systemIcon;
        }

        systemIcon = this.getDefaultAppDrawable(activityInfo, userHandle);
        if (Utilities.ATLEAST_OREO && systemIcon instanceof AdaptiveIconDrawable) {
            systemIcon = new AdaptiveIconDrawableCompat(((AdaptiveIconDrawable) systemIcon).getBackground(),
                    ((AdaptiveIconDrawable) systemIcon).getForeground());
            return systemIcon;
        } else {
            // Icon is not adaptive, try to load using reflection.
            Drawable adaptiveIcon = new AdaptiveIconProvider().load(ctx, componentName.getPackageName());
            if (adaptiveIcon != null) {
                systemIcon = adaptiveIcon;
            } else {
                // Failed to load adaptive icon, Generate an adaptive icon from app default
                // icon.
                systemIcon = new AdaptiveIconGenerator(ctx, getDefaultAppDrawable(activityInfo, userHandle))
                        .getResult();
            }
        }

        Drawable badgedIcon = getBadgedIcon(systemIcon, activityInfo.getUser());
        cacheStoreDrawable(key, badgedIcon);
        return badgedIcon;
    }

    public void resetIconDrawableForPackage(ComponentName componentName, UserHandle userHandle) {
        if (!packagesDrawables.containsKey(componentName.toString())) {
            LauncherApps launcherApps = (LauncherApps) ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            for (LauncherActivityInfo launcherActivityInfo : launcherApps
                    .getActivityList(componentName.getPackageName(), userHandle.getRealHandle())) {
                if (launcherActivityInfo.getComponentName().flattenToString().equals(componentName.flattenToString())) {
                    Drawable icon = this.getDefaultAppDrawable(launcherActivityInfo, userHandle);
                    if (Utilities.ATLEAST_OREO && icon instanceof AdaptiveIconDrawable) {
                        icon = new AdaptiveIconDrawableCompat(((AdaptiveIconDrawable) icon).getBackground(),
                                ((AdaptiveIconDrawable) icon).getForeground());
                    } else {
                        Drawable adaptiveIcon = new AdaptiveIconProvider().load(ctx, componentName.getPackageName());
                        if (adaptiveIcon != null) {
                            icon = adaptiveIcon;
                        } else {
                            icon = graphicsUtil.convertToRoundedCorner(ctx, graphicsUtil.addBackground(icon, false));
                        }
                    }

                    Drawable badgedIcon = getBadgedIcon(icon, launcherActivityInfo.getUser());
                    cacheStoreDrawable(userHandle.addUserSuffixToString(componentName.flattenToString(), '/'),
                            badgedIcon);
                }
            }
        }
    }

    private Drawable getBadgedIcon(Drawable icon, android.os.UserHandle userHandle) {
        return ctx.getApplicationContext().getPackageManager().getUserBadgedIcon(icon, userHandle);
    }

    private boolean isDrawableInCache(String key) {
        File drawableFile = cacheGetFileName(key);
        return drawableFile.isFile();
    }

    private void cacheStoreDrawable(String key, Drawable drawable) {
        Bitmap bitmap = getBitmapFromDrawable(drawable);
        File drawableFile = cacheGetFileName(key);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(drawableFile);
            bitmap.compress(CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Unable to store drawable in cache " + e);
        }
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    private Drawable cacheGetDrawable(String key) {

        if (!isDrawableInCache(key)) {
            return null;
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(cacheGetFileName(key));
            BitmapDrawable drawable = new BitmapDrawable(this.ctx.getResources(), BitmapFactory.decodeStream(fis));
            fis.close();
            return drawable;
        } catch (Exception e) {
            Log.e(TAG, "Unable to get drawable from cache " + e);
        }

        return null;
    }

    /**
     * create path for icons cache like this
     * {cacheDir}/icons/{icons_pack_package_name}_{key_hash}.png
     */
    private File cacheGetFileName(String key) {
        return new File(getIconsCacheDir() + File.separator + iconsPackPackageName + "_" + key.hashCode() + ".png");
    }

    /**
     * returns icons cache directory.
     */
    private File getIconsCacheDir() {
        File file = new File(this.ctx.getCacheDir().getPath() + "/icons/");
        file.mkdir();
        return file;
    }

    /**
     * Clear cache
     */
    private void cacheClear() {
        File cacheDir = this.getIconsCacheDir();

        if (!cacheDir.isDirectory()) {
            return;
        }

        for (File item : cacheDir.listFiles()) {
            if (!item.delete()) {
                Log.w(TAG, "Failed to delete file: " + item.getAbsolutePath());
            }
        }
    }

    public Drawable convertIcon(Drawable icon) {
        return new AdaptiveIconGenerator(ctx, icon).getResult();
    }

    /**
     * Returns a drawable suitable for the all apps view. If the package or the
     * resource do not exist, it returns null.
     */
    public static Drawable createIconDrawable(Intent.ShortcutIconResource iconRes, Context context) {
        PackageManager packageManager = context.getPackageManager();
        // the resource
        try {
            Resources resources = packageManager.getResourcesForApplication(iconRes.packageName);
            if (resources != null) {
                final int id = resources.getIdentifier(iconRes.resourceName, null, null);
                return resources.getDrawableForDensity(id,
                        BlissLauncher.getApplication(context).getDeviceProfile().fillResIconDpi);
            }
        } catch (Exception e) {
            // Icon not found.
        }
        return null;
    }

    /**
     * Returns a drawable which is of the appropriate size to be displayed as an
     * icon
     */
    public static Drawable createIconDrawable(Bitmap icon, Context context) {
        return new BitmapDrawable(icon);
    }

    public Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(),
                Utilities.ATLEAST_OREO ? android.R.drawable.sym_def_app_icon : android.R.mipmap.sym_def_app_icon);
    }

    private Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            d = resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }

    public void clearAll() {
        packagesDrawables.clear();
        cacheClear();
    }
}
