package com.cloudx.ios17.core.database.model;

import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.Constants;
import com.cloudx.ios17.core.utils.Constants;

import java.util.List;

public class FolderItem extends LauncherItem {

    /** Stores networkItems that user saved in this folder. */
    public List<LauncherItem> items;

    public FolderItem() {
        itemType = Constants.ITEM_TYPE_FOLDER;
    }
}
