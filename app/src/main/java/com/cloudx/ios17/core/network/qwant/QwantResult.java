package com.cloudx.ios17.core.network.qwant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QwantResult {
    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("data")
    @Expose
    private QwantData data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public QwantData getData() {
        return data;
    }

    public void setData(QwantData data) {
        this.data = data;
    }
}
