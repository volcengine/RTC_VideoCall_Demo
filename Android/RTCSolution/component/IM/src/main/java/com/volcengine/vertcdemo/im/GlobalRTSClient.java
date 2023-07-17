package com.volcengine.vertcdemo.im;

import static com.ss.bytertc.rts.engine.type.UserMessageSendResult.USER_MESSAGE_SEND_RESULT_NOT_LOGIN;
import static com.ss.bytertc.rts.engine.type.UserMessageSendResult.USER_MESSAGE_SEND_RESULT_SUCCESS;
import static com.volcengine.vertcdemo.im.IMService.ERROR_CODE_UNKNOWN;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.vertcdemo.joinrtsparams.bean.ImChannelConfig;
import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.RTSLogoutEvent;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.ErrorTool;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.ServerResponse;
import com.volcengine.vertcdemo.core.net.rts.IRTSCallback;
import com.volcengine.vertcdemo.core.net.rts.RTSBizResponse;
import com.volcengine.vertcdemo.core.net.rts.RTSRequest;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用全局RTS通道
 */
public abstract class GlobalRTSClient implements IMRTSClient {
    private static final String TAG = "GlobalRTSClient";
    /*** RTS请求集合，Key:发送消息id; value为请求requestId */
    private final ConcurrentHashMap<Long, String> mMessageIdRequestIdMap = new ConcurrentHashMap<>();
    /*** RTM请求集合，Key:请求requestId; value为请求回调及数据类型class */
    private final ConcurrentHashMap<String, IRTSCallback> mRequestIdCallbackMap = new ConcurrentHashMap<>();
    /*** RTS通知消息监听器*/
    protected final ConcurrentHashMap<String, IRTSCallback> mEventListeners = new ConcurrentHashMap<>();

    /**
     * 组装RTM业务消息，并发送
     *
     * @param eventName   事件名称
     * @param roomId      房间号
     * @param content     事件需要的参数
     * @param resultClass 返回数据的class
     * @param callback    回调接口
     */
    public <T extends RTSBizResponse> void sendServerMessage(String eventName,
                                                             String roomId,
                                                             JsonObject content,
                                                             @Nullable Class<T> resultClass,
                                                             @Nullable IRequestCallback<T> callback) {
        sendServerMessage(eventName, roomId, content, new RTSRequest<>(eventName, callback, resultClass));
    }

    /**
     * 组装RTM业务消息，并发送
     *
     * @param eventName 事件名称
     * @param roomId    房间号
     * @param content   事件需要的参数
     * @param callback  回调接口
     */
    public <T extends RTSBizResponse> void sendServerMessage(String eventName,
                                                             String roomId,
                                                             JsonObject content,
                                                             IRTSCallback callback) {
        if (content == null) {
            content = new JsonObject();
        }
        content.addProperty("login_token", SolutionDataManager.ins().getToken());
        String requestId = String.valueOf(UUID.randomUUID());
        JsonObject message = new JsonObject();
        ImChannelConfig config = IMService.getService().getConfig();
        message.addProperty("app_id", config == null ? null : config.rtsAppId);
        message.addProperty("room_id", roomId);
        message.addProperty("user_id", SolutionDataManager.ins().getUserId());
        message.addProperty("event_name", eventName);
        message.addProperty("content", content.toString());
        message.addProperty("request_id", requestId);
        message.addProperty("im_channel", true);
        message.addProperty("device_id", SolutionDataManager.ins().getDeviceId());
        long msgId = sendServerMessage(requestId, message.toString(), callback);
        Log.d(TAG, "GlobalRTSClient sendServerMessage msgId:" + msgId + ",content:" + content);
        if (msgId > 0) {
            mRequestIdCallbackMap.put(requestId, callback);
        } else {
            if (callback != null) {
                callback.onError(-1, "sendServerMessage failed: " + msgId);
            }
        }
    }

    /**
     * 给业务服务器发送消息的回调，当调用 sendServerMessage 或 sendServerBinaryMessage 接口发送消息后，会收到此回调
     */
    @Override
    public void onServerMessageSendResult(long messageId, int error, ByteBuffer message) {
        Log.d(TAG, "GlobalRTSClient onServerMessageSendResult messageId:" + messageId + ",error:" + error + ",this:" + this);
        String requestId = mMessageIdRequestIdMap.remove(messageId);
        if (error == USER_MESSAGE_SEND_RESULT_NOT_LOGIN) {
            // RTS 退出登录
            SolutionDemoEventManager.post(new RTSLogoutEvent());
        } else if (requestId != null && error != USER_MESSAGE_SEND_RESULT_SUCCESS) {
            final IRTSCallback callback = mRequestIdCallbackMap.remove(requestId);
            notifyRequestFail(ERROR_CODE_UNKNOWN, "GlobalRTSClient sendServerMessage fail error:" + error, callback);
        }
    }

    /**
     * 收到RTM业务请求回调消息及通知消息，并解析
     */
    @Override
    public void onReceivedMessage(String uid, String message) {
        Log.d(TAG, "GlobalRTSClient onMessageReceived uid:" + uid + ",message:" + message);
        try {
            JSONObject messageJson = new JSONObject(message);
            String messageType = messageJson.getString("message_type");
            if (TextUtils.equals(messageType, ServerResponse.MESSAGE_TYPE_RETURN)) {
                String requestId = messageJson.getString("request_id");
                final IRTSCallback callback = mRequestIdCallbackMap.remove(requestId);
                if (callback == null) {
                    Log.d(TAG, "GlobalRTSClient onResponseReceived callback is null");
                    return;
                }
                Log.d(TAG, String.format("GlobalRTSClient onResponseReceived (%s): %s", requestId, message));

                final int code = messageJson.optInt("code");
                if (code == 200) {
                    final String data = messageJson.optString("response");
                    AppExecutors.execRunnableInMainThread(() -> callback.onSuccess(data));
                } else {
                    final String msg = ErrorTool.getErrorMessageByErrorCode(code, messageJson.optString("message"));
                    AppExecutors.execRunnableInMainThread(() -> callback.onError(code, msg));
                }
            } else if (TextUtils.equals(messageType, ServerResponse.MESSAGE_TYPE_INFORM)) {
                String event = messageJson.getString("event");
                if (!TextUtils.isEmpty(event)) {
                    IRTSCallback eventListener = mEventListeners.get(event);
                    if (eventListener != null) {
                        String dataStr = messageJson.optString("data");
                        Log.d(TAG, String.format("GlobalRTSClient onInformReceived event: %s \n message: %s", event, dataStr));
                        eventListener.onSuccess(dataStr);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "GlobalRTSClient onMessageReceived parse message failed uid:" + uid + ",message:" + message);
        }
    }

    /**
     * 客户端给业务服务器发送文本消息,发送的文本消息内容消息不超过 62KB
     *
     * @return 消息id
     */
    private <T extends RTSBizResponse> long sendServerMessage(String requestId,
                                                              String message,
                                                              IRTSCallback callBack) {
        if (TextUtils.isEmpty(message)) {
            return ERROR_CODE_UNKNOWN;
        }
        long msgId = sendServerMessage(message);
        if (callBack != null) {
            mMessageIdRequestIdMap.put(msgId, requestId);
        }
        return msgId;
    }

    private static void notifyRequestFail(int code, String msg, @Nullable IRTSCallback callback) {
        if (callback == null) return;
        AppExecutors.execRunnableInMainThread(() -> callback.onError(code, msg));
    }
}
