package foundation.e.blisslauncher.features.weather;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.Preferences;
import java.util.HashSet;
import java.util.List;
import lineageos.weather.LineageWeatherManager;
import lineageos.weather.WeatherLocation;

public class CustomLocationPreference extends EditTextPreference
        implements
            LineageWeatherManager.LookupCityRequestListener {
    public CustomLocationPreference(Context context) {
        super(context);
    }

    public CustomLocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLocationPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private ProgressDialog mProgressDialog;
    private int mCustomLocationRequestId;
    private Handler mHandler;

    private static final String TAG = "CustomLocationPreferenc";

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        mHandler = new Handler(getContext().getMainLooper());

        final AlertDialog d = (AlertDialog) getDialog();
        final Button okButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
        okButton.setOnClickListener(v -> {
            CustomLocationPreference.this.onClick(d, DialogInterface.BUTTON_POSITIVE);
            final String customLocationToLookUp = getEditText().getText().toString();
            if (TextUtils.equals(customLocationToLookUp, ""))
                return;
            final LineageWeatherManager weatherManager = LineageWeatherManager.getInstance(getContext());
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(getContext().getString(R.string.weather_progress_title));
            mProgressDialog.setOnCancelListener(dialog -> weatherManager.cancelRequest(mCustomLocationRequestId));
            mCustomLocationRequestId = weatherManager.lookupCity(customLocationToLookUp, CustomLocationPreference.this);
            mProgressDialog.show();
        });
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        String location = Preferences.getCustomWeatherLocationCity(getContext());
        if (location != null) {
            getEditText().setText(location);
            getEditText().setSelection(location.length());
        } else {
            getEditText().setText("");
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // we handle persisting the selected location below, so pretend cancel
        super.onDialogClosed(false);
    }

    private void handleResultDisambiguation(final List<WeatherLocation> results) {
        CharSequence[] items = buildItemList(results);
        new AlertDialog.Builder(getContext()).setSingleChoiceItems(items, -1, (dialog, which) -> {
            applyLocation(results.get(which));
            dialog.dismiss();
        }).setNegativeButton(android.R.string.cancel, null).setTitle(R.string.weather_select_location).show();
    }

    private CharSequence[] buildItemList(List<WeatherLocation> results) {
        boolean needCountry = false, needPostal = false;
        String firstCountry = results.get(0).getCountry();
        HashSet<String> postalIds = new HashSet<>();

        for (WeatherLocation result : results) {
            if (!TextUtils.equals(result.getCountry(), firstCountry)) {
                needCountry = true;
            }
            String postalId = result.getCountryId() + "##" + result.getCity();
            if (postalIds.contains(postalId)) {
                needPostal = true;
            }
            postalIds.add(postalId);
            if (needPostal && needCountry) {
                break;
            }
        }

        int count = results.size();
        CharSequence[] items = new CharSequence[count];
        for (int i = 0; i < count; i++) {
            WeatherLocation result = results.get(i);
            StringBuilder builder = new StringBuilder();
            if (needPostal && result.getPostalCode() != null) {
                builder.append(result.getPostalCode()).append(" ");
            }
            builder.append(result.getCity());
            if (needCountry) {
                String country = result.getCountry() != null ? result.getCountry() : result.getCountryId();
                builder.append(" (").append(country).append(")");
            }
            items[i] = builder.toString();
        }
        return items;
    }

    private void applyLocation(final WeatherLocation result) {
        if (Preferences.setCustomWeatherLocation(getContext(), result)) {
            String cityName = result.getCity();
            String state = result.getState();
            String country = result.getCountry();
            setText(cityName + "," + state + "/" + country);
        }
        final AlertDialog d = (AlertDialog) getDialog();
        d.dismiss();
    }

    @Override
    public void onLookupCityRequestCompleted(int status, final List<WeatherLocation> locations) {
        mHandler.post(() -> {
            final Context context = getContext();
            Log.i(TAG, "onLookupCityRequestCompleted: " + status + " " + (locations == null));
            if (locations == null || locations.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.weather_retrieve_location_dialog_title),
                        Toast.LENGTH_SHORT).show();
            } else if (locations.size() > 1) {
                handleResultDisambiguation(locations);
            } else {
                applyLocation(locations.get(0));
            }
            mProgressDialog.dismiss();
        });
    }
}
