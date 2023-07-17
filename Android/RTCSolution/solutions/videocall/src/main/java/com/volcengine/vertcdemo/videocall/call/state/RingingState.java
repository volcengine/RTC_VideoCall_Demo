package com.volcengine.vertcdemo.videocall.call.state;

import static com.volcengine.vertcdemo.videocall.util.Util.getString;
import static com.volcengine.vertcdemo.videocall.util.Util.notifyExecResult;

import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.CallStateOwner;
import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.util.Callback;
import com.volcengine.vertcdemo.videocall.util.Util;

/**
 * 被叫：收到呼叫中状态
 */
public class RingingState extends AbstractState {

    public RingingState(CallStateOwner stateOwner) {
        super(stateOwner);
    }

    @Override
    public VoipState getStatus() {
        return VoipState.RINGING;
    }

    @Override
    public void accept(String token, String userId, String roomId, CallType callType, Callback callback) {
        updateState(roomId, VoipState.ACCEPTED, callType, result -> {
            if (result.success) {
                //停止铃声
                mRTCController.stopRing();
                //开启RTC音视频采集，这里不开启视频采集是因为在Idle状态收到呼叫请求时({@link IdleState#onReceiveRinging})已经开启了
                mRTCController.startAudioCapture();
                //设置音频场景为通话
                mRTCController.setAudioScenario(false);
                //更新本地状态到已接受通话
                VoipInfo voipInfo = mStateOwner.getVoipInfo();
                if (voipInfo == null){
                    return;
                }
                voipInfo.status = VoipState.ACCEPTED.getValue();
                updateState(VoipState.ACCEPTED, voipInfo);
                //进入RTC房间
                mStateOwner.getIState().joinRTCRoom(token, userId, roomId, callType, callback);
            } else {
                String msg = getString(R.string.dial_fail_because_biz, "accept");
                notifyExecResult(msg, false, callback);
            }
        });
    }

    /**
     * 突然断网情况下也应该可以拒接，因此需要不管网络请求状态如何都先将状态机的状态置回空闲状态
     */
    @Override
    public void refuse(String roomId, CallType callType, Callback callback) {
        //状态机状态回置为空闲
        restoreIdle(callType);
        //更新服务端本地状态
        updateState(roomId, VoipState.REFUSED, callType, result -> {
            Util.notifyExecResult(result.result, result.success, callback);
        });
    }

    @Override
    public void timeout(String roomId, CallType callType, Callback callback) {
        //状态机状态回置为空闲
        restoreIdle(callType);
        //通知业务服务器呼叫超时
        updateState(roomId, VoipState.UNAVAILABLE, callType, result -> {
            Util.notifyExecResult(result.result, true, callback);
        });
    }
}
