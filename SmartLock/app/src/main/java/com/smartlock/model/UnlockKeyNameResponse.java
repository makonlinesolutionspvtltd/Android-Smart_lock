package com.smartlock.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UnlockKeyNameResponse {
    @SerializedName("response")
    @Expose
    public UnlockKeyNameItems response;

}
