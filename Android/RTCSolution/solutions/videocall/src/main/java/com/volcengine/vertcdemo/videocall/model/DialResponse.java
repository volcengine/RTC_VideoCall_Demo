package com.volcengine.vertcdemo.videocall.model;

import com.google.gson.annotations.SerializedName;

public class DialResponse extends BaseResponse {
    @SerializedName("data")
    public VoipInfo data;
}
