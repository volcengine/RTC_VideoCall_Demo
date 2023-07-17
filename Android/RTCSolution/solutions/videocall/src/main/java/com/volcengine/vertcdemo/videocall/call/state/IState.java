package com.volcengine.vertcdemo.videocall.call.state;

import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.util.Callback;

public interface IState {
    /**
     * 返回当前状态
     */
    VoipState getStatus();

    /**
     * 主叫：发起通话
     *
     * @param callee   被叫id
     * @param callType 通话类型：1. 视频通话；2. 语音通话
     * @param callback 成功失败回调
     */
    void dial(String caller, String callee, CallType callType, Callback callback);

    /***
     * 主叫：取消呼叫
     * @param roomId 房间id
     * @param callType 通话类型：1. 视频通话；2. 语音通话
     * @param callback 成功失败回调
     */
    void cancel(String roomId, CallType callType, Callback callback);

    /***
     * 被叫：接听呼叫
     * @param roomId 房间id
     * @param callType 通话类型：1. 视频通话；2. 语音通话
     * @param callback 成功失败回调
     */
    void accept(String token, String userId, String roomId, CallType callType, Callback callback);

    /**
     * 被叫：进入RTC 房间
     */
    void joinRTCRoom(String token, String userId, String roomId, CallType callType, Callback callback);

    /***
     * 被叫：拒绝呼叫
     * @param roomId 房间id
     * @param callType 通话类型：1. 视频通话；2. 语音通话
     * @param callback 成功失败回调
     */
    void refuse(String roomId, CallType callType, Callback callback);

    /***
     * 主/被叫：挂断进行中的通话
     * @param roomId 房间id
     * @param callType 通话类型：1. 视频通话；2. 语音通话
     * @param callback 成功失败回调
     */
    void hangup(String roomId, CallType callType, Callback callback);

    void timeout(String roomId, CallType callType, Callback callback);

    /**
     * 主叫：收到被叫接通呼叫
     *
     * @param roomId   房间ID
     * @param callType 呼叫类型
     * @param callback 更新状态回调
     */
    void onReceiveAccepted(String roomId, CallType callType, Callback callback);

    /**
     * 被叫：收到被呼叫
     *
     * @param roomId   房间ID
     * @param callType 呼叫类型
     */
    void onReceiveRinging(String roomId, CallType callType);

    /**
     * 被/主叫：收到离房通知
     *
     * @param roomId   房间ID
     * @param callType 呼叫类型
     */
    void onReceiveLeaveRoom(String roomId, CallType callType);

}
