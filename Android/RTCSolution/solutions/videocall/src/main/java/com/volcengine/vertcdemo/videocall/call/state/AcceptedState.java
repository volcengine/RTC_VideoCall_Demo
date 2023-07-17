package com.volcengine.vertcdemo.videocall.call.state;

import static com.volcengine.vertcdemo.videocall.util.Util.getString;
import static com.volcengine.vertcdemo.videocall.util.Util.notifyExecResult;

import android.util.Log;

import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.CallStateOwner;
import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.util.Callback;

/**
 * 被叫：刚点击接通但还未进RTC房间状态
 */
public class AcceptedState extends AbstractState {

    public AcceptedState(CallStateOwner stateOwner) {
        super(stateOwner);
    }

    @Override
    public VoipState getStatus() {
        return VoipState.ACCEPTED;
    }

    @Override
    public void joinRTCRoom(String token, String userId, String roomId, CallType callType, Callback callback) {
        //进入RTC房间
        boolean joinRTCRoomSuccess = mRTCController.creteAndJoinRTCRoom(token, userId, roomId, true) == 0;
        if (joinRTCRoomSuccess) {
            //更新状态机状态为：通话中
            updateState(roomId, VoipState.ONTHECALL, callType, result -> {
                if (result.success) {
                    VoipInfo voipInfo = mStateOwner.getVoipInfo();
                    if (voipInfo == null){
                        return;
                    }
                    voipInfo.status = VoipState.ONTHECALL.getValue();
                    updateState(VoipState.ONTHECALL, voipInfo);
                    notifyExecResult(voipInfo, true, callback);
                    return;
                }
                String msg = getString(R.string.dial_fail_because_biz, "updateState");
                notifyExecResult(msg, false, callback);
            });
        } else {
            String msg = getString(R.string.dial_fail_because_rtc, "joinRoom");
            notifyExecResult(msg, false, callback);
        }
    }

}
