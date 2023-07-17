package com.volcengine.vertcdemo.videocall.call.state;

import com.volcengine.vertcdemo.videocall.call.CallStateOwner;
import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.util.Callback;
import com.volcengine.vertcdemo.videocall.util.Util;

/**
 * 主/被叫：通话中状态
 */
public class OnTheCallState extends AbstractState {

    public OnTheCallState(CallStateOwner stateOwner) {
        super(stateOwner);
    }

    @Override
    public VoipState getStatus() {
        return VoipState.ONTHECALL;
    }

    @Override
    public void onReceiveAccepted(String roomId, CallType callType, Callback callback) {
        //被叫侧在Ringing、Accepted两个状态都会触发通话被接通的通知，提高成功接通率，因此在OnTheCall
        // 状态下也可能收到接通的通知，不需要处理。
    }

    /**
     * 突然断网情况下也应该可以挂断，因此需要不管网络请求状态如何都先将状态机的状态置回空闲状态
     */
    @Override
    public void hangup(String roomId, CallType callType, Callback callback) {
        //状态机状态回置为空闲
        restoreIdle(callType);
        //通过业务服务器通知对方挂断通话
        updateState(roomId, VoipState.TERMINATED, callType, result -> {
            Util.notifyExecResult(result.result, result.success, callback);
        });
    }

}
