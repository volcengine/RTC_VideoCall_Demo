// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.vertcdemo.joinrtsparams.common;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vertcdemo.joinrtsparams.bean.ImChannelConfig;
import com.vertcdemo.joinrtsparams.bean.JoinRTSRequest;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.ServerResponse;
import com.volcengine.vertcdemo.core.net.http.HttpRequestHelper;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;

import org.json.JSONObject;

import java.util.UUID;

public class JoinRTSManager {

    private static final String TAG = "JoinRTSManager";

    public static void setAppInfoAndJoinRTM(JoinRTSRequest joinRTSRequest,
                                            IRequestCallback<ServerResponse<RTSInfo>> callBack) {
        if (joinRTSRequest == null) {
            if (callBack != null) {
                callBack.onError(-1, "input can not be empty");
            }
            return;
        }
        String message = null;

        JsonElement element = new Gson().toJsonTree(joinRTSRequest);
        JsonObject content = element.getAsJsonObject();

        if (TextUtils.isEmpty(Constants.APP_ID)) {
            message = "APPID";
        } else {
            content.addProperty("app_id", Constants.APP_ID);
        }
        if (TextUtils.isEmpty(Constants.APP_KEY)) {
            message = "APPKey";
        } else {
            content.addProperty("app_key", Constants.APP_KEY);
        }
        if (TextUtils.isEmpty(Constants.VOLC_AK)) {
            message = "AccessKeyID";
        } else {
            content.addProperty("volc_ak", Constants.VOLC_AK);
        }
        if (TextUtils.isEmpty(Constants.VOLC_SK)) {
            message = "SecretAccessKey";
        } else {
            content.addProperty("volc_sk", Constants.VOLC_SK);
        }

        if (!TextUtils.isEmpty(message)) {
            if (callBack != null) {
                callBack.onError(-1, String.format("%s is empty", message));
            }
            return;
        }
        try {
            JSONObject params = new JSONObject();
            params.put("event_name", "setAppInfo");
            params.put("content", content.toString());
            params.put("device_id", SolutionDataManager.ins().getDeviceId());

            Log.d(TAG, "setAppInfo params: " + params);
            HttpRequestHelper.sendPost(params, RTSInfo.class, callBack);
        } catch (Exception e) {
            Log.d(TAG, "setAppInfo failed", e);
            callBack.onError(-1, e.getMessage());
        }
    }

    public static void getIMChannelInfo(String loginToken,
                                        IRequestCallback<ImChannelConfig> callBack) {
        JSONObject params = new JSONObject();
        try {
            JSONObject content = new JSONObject();
            content.put("login_token", loginToken);
            content.put("platform", "android");
            content.put("app_id", Constants.APP_ID);
            content.put("app_key", Constants.APP_KEY);
            content.put("volc_ak", Constants.VOLC_AK);
            content.put("volc_sk", Constants.VOLC_SK);
            params.put("content", content.toString());
            params.put("event_name", "getIMChannelInfo");
            params.put("device_id", SolutionDataManager.ins().getDeviceId());
            params.put("request_id", UUID.randomUUID().toString());
        } catch (Exception e) {
            //ignore
        }
        Log.d(TAG, "IMService init params:" + params);
        HttpRequestHelper.sendPost(params, ImChannelConfig.class,
                new IRequestCallback<ServerResponse<ImChannelConfig>>() {
                    @Override
                    public void onSuccess(ServerResponse<ImChannelConfig> data) {
                        if (callBack == null) {
                            return;
                        }
                        ImChannelConfig config = data == null ? null : data.getData();
                        if (config != null && config.isValid()) {
                            callBack.onSuccess(data.getData());
                        } else {
                            callBack.onError(
                                    data == null ? -1 : data.getCode(),
                                    data == null ? "im channel info is null" : data.getMsg()
                            );
                        }
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        if (callBack != null) {
                            callBack.onError(errorCode, message);
                        }
                    }
                });
    }

}
