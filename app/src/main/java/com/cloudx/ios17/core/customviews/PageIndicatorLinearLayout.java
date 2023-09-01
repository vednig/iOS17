package com.cloudx.ios17.core.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;

import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.DeviceProfile;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.DeviceProfile;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.DeviceProfile;
import com.cloudx.ios17.BlissLauncher;
import com.cloudx.ios17.core.DeviceProfile;

public class PageIndicatorLinearLayout extends LinearLayout {
    private Context mContext;

    public PageIndicatorLinearLayout(Context context) {
        this(context, null);
    }

    public PageIndicatorLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        DeviceProfile deviceProfile = BlissLauncher.getApplication(mContext).getDeviceProfile();
        setMeasuredDimension(deviceProfile.getAvailableWidthPx(), deviceProfile.getPageIndicatorHeight());
    }
}
