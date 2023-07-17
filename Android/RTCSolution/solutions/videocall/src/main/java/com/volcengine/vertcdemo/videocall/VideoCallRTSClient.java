package com.volcengine.vertcdemo.videocall;

import static com.volcengine.vertcdemo.videocall.util.Util.getString;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.volcengine.vertcdemo.common.GsonUtils;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.rts.IRTSCallback;
import com.volcengine.vertcdemo.im.GlobalRTSClient;
import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.model.DialResponse;
import com.volcengine.vertcdemo.videocall.model.GetUsersResponse;
import com.volcengine.vertcdemo.videocall.model.VoipInform;
import com.volcengine.vertcdemo.videocall.util.Callback;

import java.util.UUID;

public class VideoCallRTSClient extends GlobalRTSClient {
    private static final String TAG = "VideoCallRTSClient";

    private JsonObject getCommonParams(String cmd) {
        JsonObject params = new JsonObject();
        params.addProperty("user_id", SolutionDataManager.ins().getUserId());
        params.addProperty("event_name", cmd);
        params.addProperty("request_id", UUID.randomUUID().toString());
        params.addProperty("device_id", SolutionDataManager.ins().getDeviceId());
        params.addProperty("login_token", SolutionDataManager.ins().getToken());
        return params;
    }

    /**
     * 进入场景时清理上次的用户
     */
    public void clearUser() {
        String cmd = "videooneClearUser";
        JsonObject params = getCommonParams(cmd);
        sendServerMessage(cmd, null, params, GetUsersResponse.class, null);
    }

    /**
     * 获取通信列表
     */
    public void getUserList(String keyWord, IRequestCallback callback) {
        if (TextUtils.isEmpty(keyWord)) {
            callback.onError(-1, getString(R.string.argument_is_invalid));
            return;
        }
        String cmd = "videooneGetUserList";
        JsonObject params = getCommonParams(cmd);
        params.addProperty("keyword", keyWord);
        sendServerMessage(cmd, null, params, GetUsersResponse.class, callback);
    }

    /**
     * 拨打电话
     */
    public void dialVoip(String callerUid, String calleeUid, CallType callType, IRequestCallback callback) {
        if (TextUtils.isEmpty(callerUid) || TextUtils.isEmpty(calleeUid) || callType == null) {
            callback.onError(-1, getString(R.string.argument_is_invalid));
            return;
        }
        String cmd = "videooneCreateRoom";
        JsonObject params = getCommonParams(cmd);
        params.addProperty("type", callType.getValue());
        params.addProperty("from_user_id", callerUid);
        params.addProperty("to_user_id", calleeUid);
        sendServerMessage(cmd, null, params, DialResponse.class, callback);
    }

    /**
     * 更新通话状态
     */
    public void updateVoipState(String roomId, CallType type, VoipState status, IRequestCallback callback) {
        if (TextUtils.isEmpty(roomId) || type == null || status == null) {
            callback.onError(-1, getString(R.string.argument_is_invalid));
            return;
        }
        Log.d(TAG, "updateVoipState status:" + status);
        String cmd = "videooneUpdateStatus";
        JsonObject params = getCommonParams(cmd);
        params.addProperty("room_id", roomId);
        params.addProperty("user_id", SolutionDataManager.ins().getUserId());
        params.addProperty("type", type.getValue());
        params.addProperty("status", status.getValue());
        sendServerMessage(cmd, roomId, params, DialResponse.class, callback);
    }

    /**
     * 设置收到通话信令回调
     */
    public void addEventObserver(Callback callback) {
        if (callback == null) {
            return;
        }
        mEventListeners.put("videooneInform", new IRTSCallback() {
            @Override
            public void onSuccess(@Nullable String data) {
                if (TextUtils.isEmpty(data)) {
                    return;
                }
                VoipInform inform = GsonUtils.gson().fromJson(data, VoipInform.class);
                callback.onResult(new Callback.Result<>(true, inform));
            }

            @Override
            public void onError(int errorCode, @Nullable String message) {
                //ignore
            }
        });
    }

}
