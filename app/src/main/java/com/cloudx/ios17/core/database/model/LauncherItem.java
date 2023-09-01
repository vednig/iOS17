package com.cloudx.ios17.core.database.model;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.UserHandle;

import java.net.URISyntaxException;

@Entity(tableName = "launcher_items", indices = {@Index(value = {"item_id"}, unique = true)})
public class LauncherItem {

    @Ignore
    public static final int NO_ID = -1;

    @Ignore
    public static final int INVALID_CELL = -1;

    @PrimaryKey(autoGenerate = true)
    public int keyId;

    /**
     * The id in the database for this item. For
     * {@link Constants#ITEM_TYPE_APPLICATION} this would be Component name as it is
     * unique across all application networkItems. If it is one of
     * {@link Constants#ITEM_TYPE_SHORTCUT} and {@link Constants#ITEM_TYPE_FOLDER},
     * it will be the id of that.
     */
    @NonNull
    @ColumnInfo(name = "item_id")
    public String id;

    /**
     * One of {@link Constants#ITEM_TYPE_APPLICATION}
     * {@link Constants#ITEM_TYPE_SHORTCUT} {@link Constants#ITEM_TYPE_FOLDER}
     */
    @ColumnInfo(name = "item_type")
    public int itemType;

    /**
     * The id of the container that holds this item. For the desktop, this will be
     * {@link Constants#CONTAINER_DESKTOP}. For the all applications folder it will
     * be {@link #NO_ID}. For user folders it will be the id of the folder.
     */
    @NonNull
    @ColumnInfo(name = "container")
    public long container = NO_ID;

    /**
     * Indicates the screen in which the shortcut appears if the container types is
     * {@link Constants#CONTAINER_DESKTOP}. (i.e., ignore if the container type is
     * {@link Constants#CONTAINER_HOTSEAT})
     */
    @ColumnInfo(name = "screen_id")
    public long screenId = -1;

    /** Indicates the position of the associated cell. */
    @ColumnInfo(name = "cell")
    public int cell = INVALID_CELL;

    /** Title of the item */
    @ColumnInfo(name = "title")
    public CharSequence title;

    @Ignore
    public UserHandle user;

    /** Icon of the item */
    @Ignore
    public Drawable icon;

    /** Used for shortcuts on api lower than oreo. */
    @ColumnInfo(name = "icon", typeAffinity = ColumnInfo.BLOB)
    public byte[] icon_blob;

    /** Intent used to launch this shortcut. */
    @Ignore
    public Intent launchIntent;

    @ColumnInfo(name = "intent_uri")
    public String launchIntentUri;

    /**
     * Package name of the respective launcher item. For folders it would be
     * "FOLDER".
     */
    @ColumnInfo(name = "package")
    public String packageName;

    public LauncherItem() {
    }

    public Intent getIntent() {
        if (launchIntent != null) {
            return launchIntent;
        }
        if (launchIntentUri != null) {
            try {
                launchIntent = Intent.parseUri(launchIntentUri, 0);
                return launchIntent;
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public ComponentName getTargetComponent() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getComponent();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "[id: " + id + ", item_type: " + itemType + ", container: " + container + ", screen: " + screenId
                + ", cell: " + cell + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof LauncherItem)) {
            return false;
        }

        LauncherItem launcherItem = (LauncherItem) obj;
        return launcherItem.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.id.hashCode();
        return hash;
    }
}
