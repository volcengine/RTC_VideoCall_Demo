package com.volcengine.vertcdemo.videocall.call.state;

import static com.volcengine.vertcdemo.videocall.util.Util.getString;
import static com.volcengine.vertcdemo.videocall.util.Util.notifyExecResult;

import android.util.Log;

import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.CallStateOwner;
import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.model.DialResponse;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.util.Callback;
import com.volcengine.vertcdemo.videocall.util.Constant;
import com.volcengine.vertcdemo.videocall.util.Util;

/**
 * 空闲状态
 */
public class IdleState extends AbstractState {

    public IdleState(CallStateOwner stateOwner) {
        super(stateOwner);
    }

    @Override
    public VoipState getStatus() {
        return VoipState.IDLE;
    }

    @Override
    public void dial(String caller, String callee, CallType callType, Callback callback) {
        AppExecutors.networkIO().execute(() -> {
            //通过业务服务器呼叫被叫
            mRTSClient.dialVoip(caller, callee, callType, new IRequestCallback<DialResponse>() {

                @Override
                public void onSuccess(DialResponse data) {
                    VoipInfo voipInfo = data.data;
                    //创建并进入RTC房间
                    int joinRoomResult = mRTCController.creteAndJoinRTCRoom(voipInfo.token, caller, voipInfo.roomID, false);
                    if (joinRoomResult == 0) {
                        //开启RTC 音视频采集
                        mRTCController.startAudioCapture();
                        if (callType == CallType.VIDEO) {
                            mRTCController.startVideoCapture();
                        }
                        //播放铃声
                        mRTCController.playRing();
                        //更新状态机状态到呼叫中状态
                        updateState(VoipState.CALLING, voipInfo);
                        notifyExecResult(data, true, callback);
                    } else {
                        String msg = getString(R.string.dial_fail_because_rtc, "errorCode:" + joinRoomResult);
                        notifyExecResult(msg, false, callback);
                    }
                }

                @Override
                public void onError(int errorCode, String message) {
                    String msg = getString(R.string.dial_fail_because_biz, "errorCode:" + errorCode + ",message:" + message);
                    notifyExecResult(msg, false, callback);
                }
            });
        });
    }

    @Override
    public void onReceiveRinging(String roomId, CallType callType) {
        //播放铃声
        mRTCController.playRing();
        //进入通话页用户就需要预览因此响铃就开启视频采集
        if (callType == CallType.VIDEO) {
            //开启视频采集
            mRTCController.startVideoCapture();
        }
        //更新状态机的状态到响铃中
        VoipInfo voipInfo = mStateOwner.getVoipInfo();
        voipInfo.status = VoipState.RINGING.getValue();
        updateState(VoipState.RINGING, voipInfo);
    }
}
