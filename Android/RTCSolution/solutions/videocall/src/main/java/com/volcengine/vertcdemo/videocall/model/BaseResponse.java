package com.volcengine.vertcdemo.videocall.model;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizResponse;

public abstract class BaseResponse implements RTSBizResponse {
    @SerializedName("err_no")
    public int errorNo;
    @SerializedName("message")
    public String message;
    @SerializedName("err_tips")
    public String errorTip;
}
