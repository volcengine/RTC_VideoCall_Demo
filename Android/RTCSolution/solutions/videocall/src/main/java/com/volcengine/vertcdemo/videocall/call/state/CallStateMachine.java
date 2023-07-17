package com.volcengine.vertcdemo.videocall.call.state;

import android.text.TextUtils;
import android.util.Log;

import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.utils.ActivityDataManager;
import com.volcengine.vertcdemo.utils.WeakHandler;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.CallCmd;
import com.volcengine.vertcdemo.videocall.call.CallStateOwner;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.model.VoipInform;
import com.volcengine.vertcdemo.videocall.util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * 通话状态机
 */
public class CallStateMachine implements CallStateOwner {
    private static final String TAG = "CallStateMachine";

    /**
     * 主叫：被叫应答超时,即主叫发出呼叫后，如果到达超时时间还没有收到被叫方接通或者拒接消息，自动取消本次呼叫
     */
    private static final int WHAT_CALLER_AUTO_CANCEL = 20001;
    /**
     * 被叫：自动超时应答,即被叫收到主叫发起的呼叫后，如果超时时间内用户没有主动接通或者拒接，自动拒接本次呼叫
     */
    private static final int WHAT_CALLEE_AUTO_REFUSE = 20002;

    public interface CallStateObserver {
        void onCallStateChange(VoipState oldState, VoipState newState, VoipInfo info);
    }

    private final HashSet<CallStateObserver> mObservers = new HashSet<>();

    /**
     * 添加状态监听器
     */
    public void addStateObserver(CallStateObserver observer) {
        synchronized (mObservers) {
            mObservers.add(observer);
        }
    }

    /**
     * 移除状态监听器
     */
    public void removeStateObserver(CallStateObserver observer) {
        synchronized (mObservers) {
            mObservers.remove(observer);
        }
    }

    private IState mCurState;
    private VoipInfo mVoipInfo;
    private final StateFactory mFactory = new StateFactory();
    private final WeakHandler.IHandler mHandler = msg -> {
        if (msg.what == WHAT_CALLER_AUTO_CANCEL) {
            execCommand(CallCmd.TIME_OUT, new CallCmd.Params());
        } else if (msg.what == WHAT_CALLEE_AUTO_REFUSE) {
            execCommand(CallCmd.TIME_OUT, new CallCmd.Params());
        }
    };
    private final WeakHandler mTimer = new WeakHandler(mHandler);

    public CallStateMachine() {
        this.mCurState = new IdleState(this);
        notifyStateChange(null, mCurState.getStatus());
    }

    @Override
    public IState getIState() {
        return mCurState;
    }

    @Override
    public void updateState(IState state, VoipInfo voipInfo) {
        mVoipInfo = voipInfo;
        VoipState oldState = mCurState.getStatus();
        mCurState = state;
        notifyStateChange(oldState, state.getStatus());
    }

    private void notifyStateChange(VoipState oldState, VoipState newState) {
        synchronized (mObservers) {
            for (CallStateObserver item : mObservers) {
                item.onCallStateChange(oldState, newState, mVoipInfo);
            }
        }
    }

    @Override
    public VoipInfo getVoipInfo() {
        return mVoipInfo;
    }

    @Override
    public IState createState(VoipState state) {
        return mFactory.createState(state, this);
    }

    /**
     * 执行命令
     */
    public void execCommand(CallCmd cmd, CallCmd.Params params) {
        switch (cmd) {
            case DIAL://主叫：执行拨号
                if (TextUtils.isEmpty(params.callerUid)
                        || TextUtils.isEmpty(params.calleeUid)
                        || params.callType == null) {
                    Log.d(TAG, "CallStateMachine execCommand DIAL failed "
                            + "callerUid:" + params.callerUid
                            + ",calleeUid:" + params.calleeUid
                            + ",callType:" + params.callType);
                    return;
                }
                mCurState.dial(params.callerUid, params.calleeUid, params.callType,
                        result -> {
                            Log.d(TAG, "CallStateMachine execCommand DIAL result:" + result);
                            if (result.success) {
                                //启动被叫应答超时任务
                                mTimer.sendEmptyMessageDelayed(WHAT_CALLER_AUTO_CANCEL, TimeUnit.SECONDS.toMillis(60));
                            }
                            Util.notifyExecResult(result.result, result.success, params.callback);
                        });
                break;
            case CANCEL://主叫：执行取消呼叫
                mTimer.removeMessages(WHAT_CALLER_AUTO_CANCEL);
                if (mVoipInfo == null) {
                    Log.d(TAG, "CallStateMachine execCommand CANCEL failed mVoipInfo is null");
                    return;
                }
                mCurState.cancel(mVoipInfo.roomID, mVoipInfo.getCallType(), result -> {
                    Log.d(TAG, "CallStateMachine execCommand CANCEL result:" + result);
                    Util.notifyExecResult(result.result, result.success, params.callback);
                });
                break;
            case HANGUP://主/被叫：执行挂断
                if (mVoipInfo == null) {
                    Log.d(TAG, "CallStateMachine execCommand HANGUP failed mVoipInfo is null");
                    return;
                }
                mCurState.hangup(mVoipInfo.roomID, mVoipInfo.getCallType(), result -> {
                    Log.d(TAG, "CallStateMachine execCommand HANGUP result:" + result);
                    Util.notifyExecResult(result.result, result.success, params.callback);
                });
                SolutionToast.show(Util.getString(R.string.closed_call));
                break;
            case ACCEPT://被叫：执行接通呼叫
                mTimer.removeMessages(WHAT_CALLEE_AUTO_REFUSE);
                Log.d(TAG, "CallStateMachine execCommand ACCEPT mVoipInfo:" + (mVoipInfo == null ? "null" : mVoipInfo.toString()));
                if (mVoipInfo == null) {
                    return;
                }
                mCurState.accept(mVoipInfo.token, mVoipInfo.calleeUid, mVoipInfo.roomID, mVoipInfo.getCallType(), result -> {
                    Log.d(TAG, "CallStateMachine execCommand ACCEPT result:" + result);
                    Util.notifyExecResult(result.result, result.success, params.callback);
                });
                break;
            case REFUSE://被叫：执行拒绝呼叫
                mTimer.removeMessages(WHAT_CALLEE_AUTO_REFUSE);
                Log.d(TAG, "CallStateMachine execCommand REFUSE mVoipInfo:" + (mVoipInfo == null ? "null" : mVoipInfo.toString()));
                if (mVoipInfo == null) {
                    return;
                }
                mCurState.refuse(mVoipInfo.roomID, mVoipInfo.getCallType(), result -> {
                    Log.d(TAG, "CallStateMachine execCommand REFUSE result:" + result);
                    Util.notifyExecResult(result.result, result.success, params.callback);
                });
                //已拒绝提示
                SolutionToast.show(Util.getString(R.string.local_user_refuse));
                break;
            case TIME_OUT://主叫：呼叫超时
                //对方无应答提示
                boolean isCaller = mCurState.getStatus() == VoipState.CALLING;
                Log.d(TAG, "CallStateMachine execCommand TIME_OUT isCaller:" + isCaller);
                if (isCaller) {
                    SolutionToast.show(Util.getString(R.string.call_timeout));
                }
                mTimer.removeMessages(WHAT_CALLER_AUTO_CANCEL);
                Log.d(TAG, "CallStateMachine execCommand TIME_OUT mVoipInfo:" + (mVoipInfo == null ? "null" : mVoipInfo.toString()));
                if (mVoipInfo == null) {
                    return;
                }
                mCurState.timeout(mVoipInfo.roomID, mVoipInfo.getCallType(), result -> {
                    Log.d(TAG, "CallStateMachine execCommand TIME_OUT result:" + result);
                    Util.notifyExecResult(result.result, result.success, params.callback);
                });
                break;
        }
    }

    /**
     * 收到外部事件
     */
    public void onReceiveEvent(VoipInform voipInform) {
        mVoipInfo = voipInform.voipInfo;
        switch (voipInform.eventCode) {
            case VoipInform.EVENT_CODE_CREATE_ROOM://被叫：收到主叫呼叫
                ActivityDataManager activityData = ActivityDataManager.getInstance();
                if (!activityData.isForeground()) {
                    return;
                }
                mTimer.sendEmptyMessageDelayed(WHAT_CALLEE_AUTO_REFUSE, TimeUnit.SECONDS.toMillis(60));
                if (mVoipInfo != null) {
                    mCurState.onReceiveRinging(mVoipInfo.roomID, mVoipInfo.getCallType());
                }
                break;
            case VoipInform.EVENT_CODE_CANCEL://被叫：收到主叫取消呼叫, 此时被叫还未进房无法统一走EVENT_CODE_LEAVE_ROOM
                leaveRoom();
                break;
            case VoipInform.EVENT_CODE_LEAVE_ROOM://主叫：收到被叫拒绝、收到被叫挂断、收到被叫应答超时; 被叫：收到主叫挂断
                if (mCurState.getStatus() == VoipState.CALLING) {
                    SolutionToast.show(Util.getString(R.string.remote_user_refuse));
                }
                leaveRoom();
                break;
            case VoipInform.EVENT_CODE_ACCEPTED://主叫：被叫方接通呼叫,但是还未执行进房
            case VoipInform.EVENT_CODE_ANSWER_CALL://主叫：被叫方接通呼叫,已经执行进房
                mTimer.removeMessages(WHAT_CALLER_AUTO_CANCEL);
                mTimer.removeMessages(WHAT_CALLEE_AUTO_REFUSE);
                if (mVoipInfo != null) {
                    mCurState.onReceiveAccepted(mVoipInfo.roomID, mVoipInfo.getCallType(), null);
                }
                break;
            case VoipInform.EVENT_CODE_OVERTIME://主/被叫：体验超过20分钟限制
                SolutionToast.show(Util.getString(R.string.minutes_error_message));
                leaveRoom();
                break;
        }
    }

    private void leaveRoom() {
        mTimer.removeMessages(WHAT_CALLER_AUTO_CANCEL);
        mTimer.removeMessages(WHAT_CALLEE_AUTO_REFUSE);
        if (mVoipInfo != null) {
            mCurState.onReceiveLeaveRoom(mVoipInfo.roomID, mVoipInfo.getCallType());
        }
    }

    static class StateFactory {
        private final HashMap<VoipState, IState> statesCache = new HashMap<>();

        public IState createState(VoipState state, CallStateOwner owner) {
            IState cache = statesCache.get(state);
            if (cache != null) {
                return cache;
            }
            switch (state) {
                case IDLE:
                    IState idleState = new IdleState(owner);
                    statesCache.put(state, idleState);
                    return idleState;
                case CALLING:
                    IState callingState = new CallingState(owner);
                    statesCache.put(state, callingState);
                    return callingState;
                case ONTHECALL:
                    IState onTheCallState = new OnTheCallState(owner);
                    statesCache.put(state, onTheCallState);
                    return onTheCallState;
                case RINGING:
                    IState ringingState = new RingingState(owner);
                    statesCache.put(state, ringingState);
                    return ringingState;
                case ACCEPTED:
                    IState acceptedState = new AcceptedState(owner);
                    statesCache.put(state, acceptedState);
                    return acceptedState;
            }
            return null;
        }
    }

}
