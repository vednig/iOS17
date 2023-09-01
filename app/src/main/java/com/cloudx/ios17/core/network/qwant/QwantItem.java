package com.cloudx.ios17.core.network.qwant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QwantItem {
    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("suggestType")
    @Expose
    private Integer suggestType;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSuggestType() {
        return suggestType;
    }

    public void setSuggestType(Integer suggestType) {
        this.suggestType = suggestType;
    }
}
