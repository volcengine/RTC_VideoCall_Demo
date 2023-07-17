package com.volcengine.vertcdemo.videocall.call;

import static com.volcengine.vertcdemo.videocall.util.Callback.Result;

import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NavUtils;

import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.data.AudioRoute;
import com.ss.bytertc.engine.data.RemoteStreamKey;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.data.VideoFrameInfo;
import com.ss.bytertc.engine.handler.IRTCRoomEventHandler;
import com.ss.bytertc.engine.handler.IRTCVideoEventHandler;
import com.ss.bytertc.engine.type.NetworkQuality;
import com.ss.bytertc.engine.type.NetworkQualityStats;
import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.AppTokenExpiredEvent;
import com.volcengine.vertcdemo.core.eventbus.RTSLogoutEvent;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.im.IMService;
import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.utils.WeakHandler;
import com.volcengine.vertcdemo.videocall.VideoCallRTSClient;
import com.volcengine.vertcdemo.videocall.call.observer.AbsCallObserver;
import com.volcengine.vertcdemo.videocall.call.observer.CallObservers;
import com.volcengine.vertcdemo.videocall.call.observer.CallObserver;
import com.volcengine.vertcdemo.videocall.call.state.CallStateMachine;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.effect.EffectController;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.model.VoipInform;
import com.volcengine.vertcdemo.videocall.util.Callback;
import com.volcengine.vertcdemo.videocall.util.Constant;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CallEngine {
    private static final String TAG = "CallEngine";

    //<editor-fold desc="单例">
    private CallEngine() {
    }

    private static class Inner {
        private final static CallEngine instance = new CallEngine();
    }

    public static CallEngine getInstance() {
        return CallEngine.Inner.instance;
    }
    //</editor-fold>

    //<editor-fold desc="状态监听">
    private final CallObservers mObserver = new CallObservers();

    /*** 设置状态监听器*/
    public void addObserver(CallObserver observer) {
        if (observer == null) {
            return;
        }
        mObserver.addObserver(observer);
    }

    /*** 移除状态监听器*/
    public void removeObserver(CallObserver observer) {
        if (observer == null) {
            return;
        }
        mObserver.removeObserver(observer);
    }
    //</editor-fold>

    private RTCVideo mRTCVideo;
    private EffectController mEffectController;
    private VideoCallRTSClient mRTSClient;
    private CallStateMachine mCallStateMachine;
    private RTCController mRTCControl;
    private final IRTCVideoEventHandler mVideoEventHandler = new IRTCVideoEventHandler() {

        @Override
        public void onFirstLocalVideoFrameCaptured(StreamIndex streamIndex, VideoFrameInfo frameInfo) {
            mObserver.onFirstLocalVideoFrameCaptured();
        }

        @Override
        public void onFirstRemoteVideoFrameDecoded(RemoteStreamKey remoteStreamKey, VideoFrameInfo frameInfo) {
            mObserver.onFirstRemoteVideoFrameDecoded(remoteStreamKey.getRoomId(), remoteStreamKey.getUserId());
        }

        @Override
        public void onUserStartVideoCapture(String roomId, String uid) {
            mObserver.onUserToggleCamera(uid, true);
        }

        @Override
        public void onUserStopVideoCapture(String roomId, String uid) {
            mObserver.onUserToggleCamera(uid, false);
        }


        @Override
        public void onUserStartAudioCapture(String roomId, String uid) {
            mObserver.onUserToggleMic(uid, true);
        }

        @Override
        public void onUserStopAudioCapture(String roomId, String uid) {
            mObserver.onUserToggleMic(uid, false);
        }

        @Override
        public void onAudioRouteChanged(AudioRoute route) {
            AppExecutors.execRunnableInMainThread(() -> {
                if (mRTCControl != null) {
                    mRTCControl.notifyAudioChange(route);
                }
            });
        }
    };

    private final IRTCRoomEventHandler mRoomEventHandler = new IRTCRoomEventHandler() {

        @Override
        public void onUserJoined(UserInfo userInfo, int elapsed) {
            mObserver.onUserJoined(userInfo.getUid());
        }

        @Override
        public void onUserLeave(String uid, int reason) {
            String localUid = SolutionDataManager.ins().getUserId();
            //如果对端用户从RTC房间离开，则自动挂断通话
            if (!TextUtils.equals(uid, localUid) && mCallStateMachine != null) {
                hangup(null);
            }
        }

        @Override
        public void onNetworkQuality(NetworkQualityStats localQuality, NetworkQualityStats[] remoteQualities) {
            String localUid = SolutionDataManager.ins().getUserId();
            HashMap<String, Boolean> networkQuality = new HashMap<>(2);
            networkQuality.put(localUid, isBad(localQuality.txQuality));
            for (NetworkQualityStats quality : remoteQualities) {
                networkQuality.put(quality.uid, isBad(localQuality.rxQuality));
            }
            mObserver.onUserNetQualityChange(networkQuality);
        }

        private boolean isBad(int quality) {
            return quality != NetworkQuality.NETWORK_QUALITY_GOOD
                    && quality != NetworkQuality.NETWORK_QUALITY_EXCELLENT;
        }
    };
    private boolean mInFloatWindow;

    /**
     * 是否已经初始化过
     */
    public boolean isInited() {
        return mRTCVideo != null;
    }

    /**
     * 初始化
     *
     * @param appId 火山引擎AppId
     * @param bid   火山引擎业务ID
     */
    public void init(String appId, String bid) {
        //用户退到场景选择页面，没有进入其他场景，再次返回1V1,此时mRTCVideo为非空
        if (mRTCVideo != null) {
            return;
        }
        mRTCVideo = RTCVideo.createRTCVideo(AppUtil.getApplicationContext(), appId, mVideoEventHandler, null, null);
        initRTSClient();
        initRTControl(bid);
        initEffectController();
        initStateMachine();
        initCallDurationTimer();
        SolutionDemoEventManager.register(this);
    }

    /**
     * 初始化RTC 控制器
     *
     * @param bid 业务ID,便于后续问题追查
     */
    private void initRTControl(String bid) {
        mRTCControl = new RTCController(mRTCVideo, mObserver, mRoomEventHandler);
        if (!TextUtils.isEmpty(bid)) {
            mRTCControl.setBid(bid);
        }
    }

    /**
     * 初始化美颜控制器
     */
    private void initEffectController() {
        mEffectController = new EffectController();
        mEffectController.init(mRTCVideo);
    }

    Callback rtsCallback = voipResult -> {
        if (voipResult.result instanceof VoipInform && mCallStateMachine != null) {
            mCallStateMachine.onReceiveEvent((VoipInform) voipResult.result);
        }
    };

    /**
     * 初始化RTS
     */
    public void initRTSClient() {
        mRTSClient = new VideoCallRTSClient();
        mRTSClient.addEventObserver(rtsCallback);
        IMService.getService().registerMessageReceiver(Constant.IM_CLIENT_KEY_VIDEO_CALL, mRTSClient);
    }

    /**
     * 初始化通话状态机
     */
    private void initStateMachine() {
        mCallStateMachine = new CallStateMachine();
        mCallStateMachine.addStateObserver(mObserver);
    }

    /**
     * 收到业务呼叫事件。在场景选择页收到被呼叫时调用
     */
    public void onReceiveCallEvent(VoipInform voipInform) {
        if (mCallStateMachine == null) {
            return;
        }
        mCallStateMachine.onReceiveEvent(voipInform);
    }

    /**
     * 获取VideoCallRTS, 必须先执行{#initRTSClient}
     */
    public VideoCallRTSClient getRTSClient() {
        return mRTSClient;
    }

    /**
     * 返回RTCController
     */
    public RTCController getRTCController() {
        return mRTCControl;
    }

    /**
     * 获取美颜控制器, 必须先执行 #initEffectController
     */
    public EffectController getEffectController() {
        return mEffectController;
    }

    /**
     * 初始化通话时长监听器
     */
    private final CallObserver mCallDurationObserver = new AbsCallObserver() {

        @Override
        public void onCallStateChange(VoipState oldState, VoipState newState, VoipInfo info) {
            if (newState == VoipState.IDLE) {
                Log.d(TAG, "CallEngine onCallStateChange restore idle");
                mCallDuration = 0;
                mDurationTimer.removeMessages(WHAT_UPDATE_DURATION);
                mInFloatWindow = false;
            } else if (newState == VoipState.ONTHECALL) {
                Log.d(TAG, "CallEngine initCallDurationTimer 1:" + mCallDuration);
                mDurationTimer.post(() -> {
                    Log.d(TAG, "CallEngine initCallDurationTimer 2:" + mCallDuration);
                    updateDuration();
                });

            }
        }
    };

    private void initCallDurationTimer() {
        mObserver.addObserver(mCallDurationObserver);
    }

    //<editor-fold desc="通话行为">

    /**
     * 主叫：执行拨号
     *
     * @param calleeUid 被叫用户id
     * @param callType  呼叫类型
     * @param callback  结果回调
     */
    public void dial(String calleeUid, CallType callType, Callback callback) {
        if (mCallStateMachine == null) {
            callback.onResult(new Result<>(false, "CallStateMachine is null!"));
            return;
        }
        CallCmd.Params params = new CallCmd.Params();
        params.calleeUid = calleeUid;
        params.callerUid = SolutionDataManager.ins().getUserId();
        params.callType = callType;
        params.callback = callback;
        mCallStateMachine.execCommand(CallCmd.DIAL, params);
    }

    /**
     * 挂断：主叫取消呼叫、被叫拒绝呼叫、主/被叫在通话中挂断
     */
    public void hangup(Callback callback) {
        if (mCallStateMachine == null) {
            callback.onResult(new Result<>(false, "CallStateMachine is null!"));
            return;
        }
        VoipState curState = mCallStateMachine.getIState().getStatus();
        if (curState == VoipState.IDLE) {
            return;
        }
        CallCmd cmd = CallCmd.HANGUP;
        if (curState == VoipState.CALLING) {
            cmd = CallCmd.CANCEL;
        } else if (curState == VoipState.RINGING || curState == VoipState.ACCEPTED) {
            cmd = CallCmd.REFUSE;
        }
        mCallStateMachine.execCommand(cmd, new CallCmd.Params(callback));
    }

    /**
     * 被叫：接通通话
     */
    public void accept(Callback callback) {
        if (mCallStateMachine == null) {
            callback.onResult(new Result<>(false, "CallStateMachine is null!"));
            return;
        }
        mCallStateMachine.execCommand(CallCmd.ACCEPT, new CallCmd.Params(callback));
    }
    //</editor-fold>

    /**
     * 获取当前通话状态
     *
     * @return 只有在场景化场景选择页面中进入其他场景时，因为没有进入进入过本场景，CallEngine没有初始化过，状态机为空
     */
    public VoipState getCurVoipState() {
        if (mCallStateMachine == null) {
            return null;
        }
        return mCallStateMachine.getIState().getStatus();
    }

    /**
     * 获取通话相关数据
     *
     * @return 只有在场景化场景选择页面中进入其他场景时，因为没有进入进入过本场景，CallEngine没有初始化过，状态机为空
     */
    public VoipInfo getVoipInfo() {
        if (mCallStateMachine == null) {
            return null;
        }
        return mCallStateMachine.getVoipInfo();
    }

    /***通话时长计时器*/
    private static final int WHAT_UPDATE_DURATION = 10001;
    private int mCallDuration;
    private final WeakHandler.IHandler mDurationHandler = msg -> {
        if (msg.what == WHAT_UPDATE_DURATION) {
            updateDuration();
        }
    };
    private final WeakHandler mDurationTimer = new WeakHandler(mDurationHandler);

    private void updateDuration() {
        mCallDuration++;
        mDurationTimer.sendEmptyMessageDelayed(WHAT_UPDATE_DURATION, TimeUnit.SECONDS.toMillis(1));
        mObserver.onUpdateCallDuration(mCallDuration);
    }

    /**
     * 是否在悬浮窗状态
     */
    public boolean isInFloatWindow() {
        Log.d(TAG, "CallEngine isInFloatWindow:" + mInFloatWindow);
        return mInFloatWindow;
    }

    /**
     * 设置是否在悬浮窗状态的标志
     */
    public void setInFloatWindow(boolean inFloatWindow) {
        Log.d(TAG, "CallEngine setInFloatWindow:" + inFloatWindow);
        this.mInFloatWindow = inFloatWindow;
    }

    /**
     * 退出当前场景销毁相关资源
     */
    public void destroy() {
        Log.d(TAG, "CallEngine destroy");
        mObserver.removeObserver(mCallDurationObserver);
        if (mCallStateMachine != null) {
            mCallStateMachine.removeStateObserver(mObserver);
            mCallStateMachine = null;
        }
        if (mRTCControl != null) {
            mRTCControl.destroy();
            mRTCControl = null;
        }
        IMService.getService().unregisterMessageReceiver(Constant.IM_CLIENT_KEY_VIDEO_CALL);
        SolutionDemoEventManager.unregister(this);
        mRTSClient = null;
        mEffectController = null;
        mRTCVideo = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppTokenExpired(AppTokenExpiredEvent event) {
        Log.d(TAG, "CallEngine onLogout");
        hangup(null);
    }

}
