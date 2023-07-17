package com.volcengine.vertcdemo.videocall.model;

import com.google.gson.annotations.SerializedName;

public class VoipInform {
    public static final int EVENT_CODE_CREATE_ROOM = 1;
    public static final int EVENT_CODE_ANSWER_CALL = 2;
    public static final int EVENT_CODE_LEAVE_ROOM = 3;
    public static final int EVENT_CODE_ACCEPTED = 4;
    public static final int EVENT_CODE_CANCEL = 5;
    public static final int EVENT_CODE_OVERTIME = 6;

    @SerializedName("event_type_code")
    public int eventCode;
    @SerializedName("voip_info")
    public VoipInfo voipInfo;
    @SerializedName("timeout_sig")
    public int timeOut;
}
