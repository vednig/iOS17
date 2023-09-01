package com.cloudx.ios17.core.network.qwant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QwantData {
    @SerializedName("items")
    @Expose
    private List<QwantItem> items = null;

    public List<QwantItem> getItems() {
        return items;
    }

    public void setItems(List<QwantItem> items) {
        this.items = items;
    }
}
