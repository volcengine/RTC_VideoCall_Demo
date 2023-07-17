package com.volcengine.vertcdemo.videocall.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetUsersResponse extends BaseResponse {
    @SerializedName("data")
    public List<Contact> data;
}
