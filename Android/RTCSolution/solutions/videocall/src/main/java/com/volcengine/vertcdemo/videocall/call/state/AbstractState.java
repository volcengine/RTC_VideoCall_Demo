package com.volcengine.vertcdemo.videocall.call.state;

import static com.volcengine.vertcdemo.videocall.util.Util.getString;

import android.util.Log;

import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.VideoCallRTSClient;
import com.volcengine.vertcdemo.videocall.call.CallEngine;
import com.volcengine.vertcdemo.videocall.call.CallStateOwner;
import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.call.RTCController;
import com.volcengine.vertcdemo.videocall.model.BaseResponse;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.util.Callback;
import com.volcengine.vertcdemo.videocall.util.Util;

public abstract class AbstractState implements IState {
    private static final String TAG = "AbstractState";
    protected final VideoCallRTSClient mRTSClient;
    protected final RTCController mRTCController;
    protected CallStateOwner mStateOwner;

    public AbstractState(CallStateOwner stateOwner) {
        mRTSClient = CallEngine.getInstance().getRTSClient();
        mRTCController = CallEngine.getInstance().getRTCController();
        mStateOwner = stateOwner;
    }

    @Override
    public abstract VoipState getStatus();

    @Override
    public void dial(String caller, String callee, CallType callType, Callback callBack) {
        notifyExecFailedByState("dial", callBack);
    }

    @Override
    public void cancel(String roomId, CallType callType, Callback callback) {
        notifyExecFailedByState("cancel", callback);
    }


    @Override
    public void accept(String token, String userId, String roomId, CallType callType, Callback callback) {
        notifyExecFailedByState("accept", callback);
    }

    @Override
    public void joinRTCRoom(String token, String userId, String roomId, CallType callType, Callback callback) {
        notifyExecFailedByState("joinRTCRoom", callback);
    }


    @Override
    public void refuse(String roomId, CallType callType, Callback callback) {
        notifyExecFailedByState("refuse", callback);
    }

    @Override
    public void timeout(String roomId, CallType callType, Callback callback) {
        notifyExecFailedByState("timeout", callback);
    }

    @Override
    public void hangup(String roomId, CallType callType, Callback callback) {
        notifyExecFailedByState("hangup", callback);
    }

    @Override
    public void onReceiveAccepted(String roomId, CallType callType, Callback callback) {
        notifyExecFailedByState("onReceiveAccepted", callback);
    }

    @Override
    public void onReceiveRinging(String roomId, CallType callType) {
        notifyExecFailedByState("onReceiveRinging", null);
    }

    @Override
    public void onReceiveLeaveRoom(String roomId, CallType callType) {
        restoreIdle(callType);
    }

    /**
     * 恢复为空闲状态
     */
    protected void restoreIdle(CallType callType) {
        //如果正在响铃，则停止响铃
        mRTCController.stopRing();
        //终止RTC房间
        mRTCController.terminateRoom();
        //如果正在音视频采集，则停止音视频采集和发布
        mRTCController.stopAudioCapture();
        mRTCController.stopVoicePublish();
        if (callType == CallType.VIDEO) {
            mRTCController.stopVideoCapture();
            mRTCController.stopVideoPublish();
        }
        //更新状态机状态为空闲
        updateState(VoipState.IDLE, null);
    }

    /**
     * 向服务端同步状态
     *
     * @param roomId       房间号
     * @param targetStatus 新的状态
     * @param callType     呼叫类型
     * @param callback     同步成功失败结果
     */
    protected void updateState(String roomId, VoipState targetStatus, CallType callType, Callback callback) {
        AppExecutors.networkIO().execute(() ->
                mRTSClient.updateVoipState(roomId, callType, targetStatus, new IRequestCallback<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse data) {
                        if (data.errorNo == 0) {
                            Util.notifyExecResult(data, true, callback);
                        } else {
                            notifyExecFailed(data.errorNo, data.errorTip, callback);
                        }
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        notifyExecFailed(errorCode, message, callback);
                    }
                }));
    }

    /**
     * 通知执行失败
     *
     * @param errorCode 错误码
     * @param message   错误消息
     * @param callback  错误回调
     */
    protected void notifyExecFailed(int errorCode, String message, Callback callback) {
        String msg = getString(R.string.dial_fail_because_biz, "errorCode:" + errorCode + ",message:" + message);
        Log.d(TAG, "AbstractState notifyExecFailed msg" + msg);
        Util.notifyExecResult(msg, false, callback);
    }

    /**
     * 通知此状态下不支持当前操作
     */
    private void notifyExecFailedByState(String cmd, Callback callBack) {
        String message = getString(R.string.exec_fail_because_status, getStatus().getName(), cmd);
        Log.d(TAG, message);
    }

    /**
     * 更新状态机状态
     *
     * @param stateEnum 新的状态
     * @param voipInfo  新的通话数据，如果为null则清空原有数据
     */
    protected void updateState(VoipState stateEnum, VoipInfo voipInfo) {
        if (mStateOwner != null) {
            IState newState = mStateOwner.createState(stateEnum);
            if (newState != null) {
                mStateOwner.updateState(newState, voipInfo);
            }
        }
    }
}
