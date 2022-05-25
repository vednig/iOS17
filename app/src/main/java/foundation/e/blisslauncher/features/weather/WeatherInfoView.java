package foundation.e.blisslauncher.features.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.Preferences;
import foundation.e.blisslauncher.features.launcher.LauncherActivity;

public class WeatherInfoView extends LinearLayout {

    private View mWeatherPanel;
    private View mWeatherSetupTextView;

    private final BroadcastReceiver mWeatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getBooleanExtra(WeatherUpdateService.EXTRA_UPDATE_CANCELLED, false)) {
                updateWeatherPanel();
            }
        }
    };
    private final BroadcastReceiver mResumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateWeatherPanel();
        }
    };

    public WeatherInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mWeatherSetupTextView = findViewById(R.id.weather_setup_textview);
        mWeatherPanel = findViewById(R.id.weather_panel);
        mWeatherPanel.setOnClickListener(v -> {
            Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage(
                    "foundation.e.weather");
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(launchIntent);
            }
        });
        findViewById(R.id.weather_setting_imageview).setOnClickListener(v -> startWeatherPreferences());
        findViewById(R.id.weather_refresh_imageview).setOnClickListener(v -> WeatherUpdateService.scheduleNextUpdate(getContext(), true));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        broadcastManager.registerReceiver(mWeatherReceiver, new IntentFilter(
                WeatherUpdateService.ACTION_UPDATE_FINISHED));
        broadcastManager.registerReceiver(mResumeReceiver, new IntentFilter(
                LauncherActivity.ACTION_LAUNCHER_RESUME));
        updateWeatherPanel();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        broadcastManager.unregisterReceiver(mWeatherReceiver);
        broadcastManager.unregisterReceiver(mResumeReceiver);
    }

    private void updateWeatherPanel() {
        if (Preferences.getCachedWeatherInfo(getContext()) == null) {
            mWeatherSetupTextView.setVisibility(VISIBLE);
            mWeatherPanel.setVisibility(GONE);
            mWeatherSetupTextView.setOnClickListener(v -> startWeatherPreferences());
            return;
        }
        mWeatherSetupTextView.setVisibility(GONE);
        mWeatherPanel.setVisibility(VISIBLE);
        ForecastBuilder.buildLargePanel(getContext(), mWeatherPanel,
                Preferences.getCachedWeatherInfo(getContext()));
    }

    private void startWeatherPreferences() {
        final Intent intent = new Intent(getContext(), WeatherPreferences.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }
}
