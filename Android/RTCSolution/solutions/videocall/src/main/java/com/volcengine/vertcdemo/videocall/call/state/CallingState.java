package com.volcengine.vertcdemo.videocall.call.state;

import com.volcengine.vertcdemo.videocall.call.CallStateOwner;
import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.util.Callback;
import com.volcengine.vertcdemo.videocall.util.Util;

/**
 * 主叫：呼叫中状态
 */
public class CallingState extends AbstractState {
    public CallingState(CallStateOwner stateOwner) {
        super(stateOwner);
    }

    @Override
    public VoipState getStatus() {
        return VoipState.CALLING;
    }

    @Override
    public void cancel(String roomId, CallType callType, Callback callback) {
        //状态机状态回置为空闲
        restoreIdle(callType);
        //通过业务服务器通知被叫取消呼叫
        updateState(roomId, VoipState.CANCELLED, callType, result -> {
            Util.notifyExecResult(result.result, result.success, callback);
        });
    }

    @Override
    public void timeout(String roomId, CallType callType, Callback callback) {
        //状态机状态回置为空闲
        restoreIdle(callType);
        //通知业务服务器呼叫超时
        updateState(roomId, VoipState.UNAVAILABLE, callType, result -> {
            Util.notifyExecResult(result.result, result.success, callback);
        });
    }

    @Override
    public void onReceiveAccepted(String roomId, CallType callType, Callback callback) {
        //停止铃声
        mRTCController.stopRing();
        //向房间推送音视频流
        mRTCController.startVoicePublish();
        if (callType == CallType.VIDEO) {
            mRTCController.startVideoPublish();
        }
        //设置音频场景为通话
        mRTCController.setAudioScenario(false);
        //更新状态机状态进入通话中状态
        VoipInfo voipInfo = mStateOwner.getVoipInfo();
        voipInfo.status = VoipState.ONTHECALL.getValue();
        updateState(VoipState.ONTHECALL, voipInfo);
    }

}
