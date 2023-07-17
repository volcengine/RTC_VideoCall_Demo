package com.volcengine.vertcdemo.videocall.model;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.videocall.call.CallType;

public class VoipInfo {

    @SerializedName("status")
    public int status;
    @SerializedName("type")
    public int callType;
    @SerializedName("room_id")
    public String roomID;
    @SerializedName("token")
    public String token;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("from_user_id")
    public String callerUid;
    @SerializedName("from_user_name")
    public String callerUname;
    @SerializedName("to_user_id")
    public String calleeUid;
    @SerializedName("ttl")
    public long ttl;
    @SerializedName("rtc_app_id")
    public String rtcAppId;

    public CallType getCallType() {
        return CallType.formValue(callType);
    }

    @Override
    public String toString() {
        return "VoipInfo{" +
                "status=" + status +
                ", callType=" + callType +
                ", roomID='" + roomID + '\'' +
                ", userId='" + userId + '\'' +
                ", callerUid='" + callerUid + '\'' +
                ", callerUname='" + callerUname + '\'' +
                ", calleeUid='" + calleeUid + '\'' +
                '}';
    }
}
