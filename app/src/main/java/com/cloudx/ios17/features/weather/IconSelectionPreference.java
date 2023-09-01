package com.cloudx.ios17.features.weather;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.cloudx.ios17.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class IconSelectionPreference extends DialogPreference implements AdapterView.OnItemClickListener {
    private static final String INTENT_CATEGORY_ICONPACK = "com.dvtonder.chronus.ICON_PACK";

    private static final String SEARCH_URI = "https://market.android.com/search?q=%s&c=apps";

    private static class IconSetDescriptor {
        String name;
        CharSequence description;
        int descriptionResId;
        Drawable previewDrawable;
        int previewResId;

        IconSetDescriptor(String name, int descriptionResId, int previewResId) {
            this.name = name;
            this.descriptionResId = descriptionResId;
            this.previewResId = previewResId;
        }

        IconSetDescriptor(String packageName, CharSequence description, Drawable preview) {
            this.name = "ext:" + packageName;
            this.description = description;
            this.previewDrawable = preview;
        }

        public CharSequence getDescription(Context context) {
            if (description != null) {
                return description;
            }
            return context.getString(descriptionResId);
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof IconSetDescriptor) {
                IconSetDescriptor o = (IconSetDescriptor) other;
                return name.equals(o.name);
            }
            return false;
        }
    }

    private static final IconSetDescriptor ICON_SETS[] = new IconSetDescriptor[]{
            new IconSetDescriptor("color", R.string.weather_icons_standard, R.drawable.weather_color_28),
            new IconSetDescriptor("mono", R.string.weather_icons_monochrome, R.drawable.weather_28),
            new IconSetDescriptor("vclouds", R.string.weather_icons_vclouds, R.drawable.weather_vclouds_28)};

    private static final IntentFilter PACKAGE_CHANGE_FILTER = new IntentFilter();

    static {
        PACKAGE_CHANGE_FILTER.addAction(Intent.ACTION_PACKAGE_ADDED);
        PACKAGE_CHANGE_FILTER.addAction(Intent.ACTION_PACKAGE_REMOVED);
        PACKAGE_CHANGE_FILTER.addDataScheme("package");
    }

    private IconSetAdapter mAdapter;
    private GridView mGrid;
    private String mValue;
    private String mSelectedValue;

    private BroadcastReceiver mPackageChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.reenumerateIconSets();
            if (getValueIndex(mSelectedValue) == GridView.INVALID_POSITION) {
                selectValue(mAdapter.getItem(0).name);
            } else {
                // index might have changed
                selectValue(mSelectedValue);
            }
        }
    };

    public IconSelectionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAdapter = new IconSetAdapter(getContext());
    }

    public CharSequence getEntry() {
        int index = getValueIndex(mValue);
        if (index != GridView.INVALID_POSITION) {
            return mAdapter.getItem(index).getDescription(getContext());
        }
        return null;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNeutralButton(R.string.icon_set_selection_get_more, null);
    }

    @Override
    protected void showDialog(Bundle state) {
        getContext().registerReceiver(mPackageChangeReceiver, PACKAGE_CHANGE_FILTER);
        super.showDialog(state);

        AlertDialog d = (AlertDialog) getDialog();
        d.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
            String uri = String.format(Locale.US, SEARCH_URI, getContext().getString(R.string.icon_set_store_filter));
            viewUri(getContext(), uri);
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        int selected = mGrid.getCheckedItemPosition();
        if (positiveResult && selected != GridView.INVALID_POSITION) {
            IconSetDescriptor descriptor = mAdapter.getItem(selected);
            if (callChangeListener(descriptor.name)) {
                mValue = descriptor.name;
                persistString(descriptor.name);
            }
        }

        getContext().unregisterReceiver(mPackageChangeReceiver);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String defValue = (String) defaultValue;
        mValue = restorePersistedValue ? getPersistedString(defValue) : defValue;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        IconSetDescriptor descriptor = mAdapter.getItem(mGrid.getCheckedItemPosition());
        mSelectedValue = descriptor.name;
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.icon_style_selection, null);

        mGrid = view.findViewById(R.id.icon_list);
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(this);

        selectValue(mValue);

        return view;
    }

    private void selectValue(String value) {
        int index = getValueIndex(value);
        if (index == GridView.INVALID_POSITION) {
            index = 0;
        }
        mGrid.setItemChecked(index, true);
        mSelectedValue = mAdapter.getItem(index).name;
    }

    private int getValueIndex(String value) {
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            if (mAdapter.getItem(i).name.equals(value)) {
                return i;
            }
        }
        return GridView.INVALID_POSITION;
    }

    private static void viewUri(Context context, String uri) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    private static class IconSetAdapter extends ArrayAdapter<IconSetDescriptor> {
        private LayoutInflater mInflater;

        public IconSetAdapter(Context context) {
            super(context, R.layout.icon_item, 0, populateIconSets(context));
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void reenumerateIconSets() {
            ArrayList<IconSetDescriptor> newSets = populateIconSets(getContext());
            boolean changed = false;

            if (newSets.size() != getCount()) {
                changed = true;
            } else {
                for (int i = 0; i < getCount(); i++) {
                    if (!newSets.get(i).equals(getItem(i))) {
                        changed = true;
                        break;
                    }
                }
            }

            if (changed) {
                setNotifyOnChange(false);
                clear();
                addAll(newSets);
                notifyDataSetChanged();
            }
        }

        private static ArrayList<IconSetDescriptor> populateIconSets(Context context) {
            ArrayList<IconSetDescriptor> result = new ArrayList<>(Arrays.asList(ICON_SETS));

            PackageManager pm = context.getPackageManager();
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(INTENT_CATEGORY_ICONPACK);

            for (ResolveInfo info : pm.queryIntentActivities(i, 0)) {
                ApplicationInfo appInfo = info.activityInfo.applicationInfo;
                try {
                    Resources res = pm.getResourcesForApplication(appInfo);
                    int previewResId = res.getIdentifier("weather_28", "drawable", appInfo.packageName);
                    Drawable preview = previewResId != 0 ? res.getDrawable(previewResId) : null;
                    result.add(new IconSetDescriptor(appInfo.packageName, appInfo.loadLabel(pm), preview));
                } catch (PackageManager.NameNotFoundException e) {
                    // shouldn't happen, ignore package
                }
            }
            return result;
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.icon_item, parent, false);
            }

            IconSetDescriptor descriptor = getItem(position);
            ImageView preview = convertView.findViewById(R.id.preview);
            TextView name = convertView.findViewById(R.id.name);

            if (descriptor.previewDrawable != null) {
                preview.setImageDrawable(descriptor.previewDrawable);
            } else {
                preview.setImageResource(descriptor.previewResId);
            }
            name.setText(descriptor.getDescription(getContext()));
            return convertView;
        }
    }
}
