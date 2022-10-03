package foundation.e.blisslauncher.core.broadcast;

import static android.content.Context.WALLPAPER_SERVICE;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.View;
import foundation.e.blisslauncher.core.blur.BlurWallpaperProvider;

public class WallpaperChangeReceiver extends BroadcastReceiver {
    private final Context mContext;
    private IBinder mWindowToken;
    private boolean mRegistered;
    private View mWorkspace;

    public WallpaperChangeReceiver(View workspace) {
        this.mWorkspace = workspace;
        this.mContext = mWorkspace.getContext();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BlurWallpaperProvider.Companion.getInstance(context).updateAsync();
        updateOffset();
    }

    public void setWindowToken(IBinder token) {
        mWindowToken = token;
        if (mWindowToken == null && mRegistered) {
            mWorkspace.getContext().unregisterReceiver(this);
            mRegistered = false;
        } else if (mWindowToken != null && !mRegistered) {
            mWorkspace.getContext().registerReceiver(this, new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));
            onReceive(mWorkspace.getContext(), null);
            mRegistered = true;
        }
    }

    private void updateOffset() {
        WallpaperManager wm = (WallpaperManager) mContext.getSystemService(WALLPAPER_SERVICE);
        wm.setWallpaperOffsets(mWindowToken, 0f, 0.5f);
        wm.setWallpaperOffsetSteps(0.0f, 0.0f);
    }
}
