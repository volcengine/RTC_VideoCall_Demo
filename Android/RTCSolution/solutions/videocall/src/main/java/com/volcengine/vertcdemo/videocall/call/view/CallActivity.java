package com.volcengine.vertcdemo.videocall.call.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.ss.bytertc.engine.data.AudioRoute;
import com.volcengine.vertcdemo.common.SolutionBaseActivity;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.utils.ActivityDataManager;
import com.volcengine.vertcdemo.utils.WeakHandler;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.CallEngine;
import com.volcengine.vertcdemo.videocall.call.CallStateHelper;
import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.call.observer.CallObserver;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.databinding.ActivityVideoCallVoipBinding;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.util.Util;

import java.util.HashMap;

public class CallActivity extends SolutionBaseActivity {
    private static final int WHAT_CLEAN_SCREEN = 10002;
    private static final String TAG = "CallActivity";
    private static final String KEY_CALL_TYPE = "call_type";
    private static final String KEY_CALLER_UID = "caller_uid";
    private static final String KEY_CALLEE_UID = "callee_uid";
    private static final String KEY_REMOTE_USER_NAME = "remote_user_name";
    private ActivityVideoCallVoipBinding mBinding;
    private AbsCallComponent mCallComponent;
    private CallType mCallType;
    private String mRemoteUserName;
    private boolean mClearingScreen;
    private CallEngine mCallEngine;
    private final WeakHandler.IHandler mHandler = msg -> {
        if (msg.what == WHAT_CLEAN_SCREEN) {
            toggleCleanScreen();
        }
    };
    private final WeakHandler mWeakHandler = new WeakHandler(mHandler);

    /***通话状态、RTC相关回调*/
    private final CallObserver mCallObserver = new CallObserver() {

        @Override
        public void onCallStateChange(VoipState oldState, VoipState newState, VoipInfo info) {
            if (newState == VoipState.ONTHECALL) {
                SolutionToast.show(R.string.on_the_call);
            }
            refreshUi();
        }

        @Override
        public void onUserJoined(String userId) {
            if (mCallComponent instanceof VideoCallComponent) {
                ((VideoCallComponent) mCallComponent).onUserJoined(userId);
            }
        }

        @Override
        public void onFirstRemoteVideoFrameDecoded(String roomId, String userId) {
            if (mCallComponent instanceof VideoCallComponent) {
                ((VideoCallComponent) mCallComponent).onFirstRemoteVideoFrameDecoded(roomId, userId);
            }
        }

        @Override
        public void onFirstLocalVideoFrameCaptured() {
            if (mCallComponent instanceof VideoCallComponent) {
                ((VideoCallComponent) mCallComponent).onFirstLocalVideoFrameCaptured();
            }
        }

        @Override
        public void onUserToggleMic(String userId, boolean on) {
            String localUid = SolutionDataManager.ins().getUserId();
            if (TextUtils.equals(localUid, userId)) {
                mCallComponent.updateMicStatus();
                VoipState newState = mCallEngine.getCurVoipState();
                if (!on && newState != VoipState.IDLE) {
                    SolutionToast.show(R.string.local_user_close_mic);
                }
            } else if (!on) {
                SolutionToast.show(R.string.remote_user_close_mic);
            }
        }

        @Override
        public void onUserToggleCamera(String userId, boolean on) {
            if (mCallComponent instanceof VideoCallComponent) {
                ((VideoCallComponent) mCallComponent).onUserToggleCamera(userId, on);
            }
        }

        @Override
        public void onAudioRouteChanged(AudioRoute route) {
            if (mCallComponent != null) {
                mCallComponent.updateAudioRouteStatus();
            }
            VoipState newState = mCallEngine.getCurVoipState();
            if (route == AudioRoute.AUDIO_ROUTE_SPEAKERPHONE && newState == VoipState.ONTHECALL) {
                SolutionToast.show(R.string.opened_speaker);
            }
        }

        @Override
        public void onUpdateCallDuration(int callDuration) {
            String duration = Util.formatCallDuration(callDuration);
            mBinding.callDuration.setText(duration);
            if (mCallComponent instanceof VideoCallComponent) {
                ((VideoCallComponent) mCallComponent).updateCallDuration(duration);
            }
        }

        @Override
        public void onUserNetQualityChange(HashMap<String, Boolean> blocked) {
            if (mCallComponent != null) {
                mCallComponent.updateNetQuality(blocked);
            }
        }
    };
    /***App Home监听器*/
    private final BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.equals(intent.getAction(), Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                return;
            }
            if (mCallEngine.getCurVoipState() != VoipState.ONTHECALL || mCallEngine.isInFloatWindow()) {
                return;
            }
            if (mCallComponent != null) {
                mCallComponent.enterFloatWindowMode();
            }
        }
    };

    /**
     * 开启通话页面
     */
    public static void start(CallType callType,
                             String callerUid,
                             String calleeUid,
                             String remoteUserName) {
        if (TextUtils.isEmpty(calleeUid)) {
            return;
        }
        Activity topActivity = ActivityDataManager.getInstance().getTopActivity();
        if (topActivity != null) {
            Intent intent = new Intent(topActivity, CallActivity.class);
            intent.putExtra(KEY_CALL_TYPE, callType.getValue());
            intent.putExtra(KEY_CALLER_UID, callerUid);
            intent.putExtra(KEY_CALLEE_UID, calleeUid);
            intent.putExtra(KEY_REMOTE_USER_NAME, remoteUserName);
            topActivity.startActivity(intent);
        }
    }

    /**
     * 进入或退出画中画模式的系统回调，仅视频通话使用
     */
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        //画中画悬浮窗点击关闭按钮时Activity的状态回置为Lifecycle.State.CREATED，代表挂断通话
        if (getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {
            hangup();
            return;
        }
        if (mCallComponent instanceof VideoCallComponent) {
            ((VideoCallComponent) mCallComponent).onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityVideoCallVoipBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());
        initData();
        initView();
        refreshUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCallEngine.removeObserver(mCallObserver);
        getApplication().unregisterReceiver(mHomeReceiver);
        if (!CallStateHelper.existOtherVideoCallActivity(CallActivity.class.getCanonicalName())
                && !mCallEngine.isInFloatWindow()) {
            mCallEngine.destroy();
        }
    }

    @Override
    protected boolean onMicrophonePermissionClose() {
        Log.d(TAG, "onMicrophonePermissionClose");
        finish();
        return true;
    }

    @Override
    protected boolean onCameraPermissionClose() {
        Log.d(TAG, "onCameraPermissionClose");
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        //通话界面屏蔽返回按钮
    }

    Runnable mCleanScreenTask = () -> {
        mWeakHandler.removeMessages(WHAT_CLEAN_SCREEN);
        mWeakHandler.sendEmptyMessageDelayed(WHAT_CLEAN_SCREEN, 5000);
    };

    private void initData() {
        int callTypeValue = getIntent().getIntExtra(KEY_CALL_TYPE, 0);
        mCallType = CallType.formValue(callTypeValue);
        Intent intent = getIntent();
        String callerUid = intent.getStringExtra(KEY_CALLER_UID);
        String calleeUid = intent.getStringExtra(KEY_CALLEE_UID);
        mRemoteUserName = intent.getStringExtra(KEY_REMOTE_USER_NAME);
        mCallEngine = CallEngine.getInstance();
        mCallEngine.addObserver(mCallObserver);
        mCallComponent = mCallType == CallType.VIDEO
                ? new VideoCallComponent(this, mBinding, mRemoteUserName, callerUid, calleeUid, mCleanScreenTask)
                : new VoiceCallComponent(this, mBinding, mRemoteUserName, callerUid, calleeUid, mCleanScreenTask);
        mCallComponent.initData();
        getApplication().registerReceiver(mHomeReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    private void initView() {
        //禁止截屏、录屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        mCallComponent.initView();
        //清屏功能
        mBinding.getRoot().setOnClickListener(v -> toggleCleanScreen());
        //对端用户信息
        mBinding.namePrefix.setRemoteNamePrefix(mRemoteUserName.substring(0, 1));
        mBinding.remoteNameTv.setText(mRemoteUserName);
    }

    private void refreshUi() {
        VoipState newState = mCallEngine.getCurVoipState();
        if (newState == null) {
            finish();
            return;
        }
        switch (newState) {
            case IDLE:
                finish();
                break;
            case CALLING:
                mBinding.enterPipBtn.setVisibility(View.VISIBLE);
                mBinding.namePrefix.startCallingAnimation();
                break;
            case ONTHECALL:
                mBinding.enterPipBtn.setVisibility(View.VISIBLE);
                mBinding.ringingTuneNameTv.setVisibility(View.GONE);
                mBinding.namePrefix.stopCallingAnimation();
                mBinding.namePrefix.setVisibility(mCallType == CallType.VOICE ? View.VISIBLE : View.GONE);
                mBinding.remoteNameTv.setVisibility(mCallType == CallType.VOICE ? View.VISIBLE : View.GONE);
                mWeakHandler.sendEmptyMessageDelayed(WHAT_CLEAN_SCREEN, 1000);
                break;
            case RINGING:
                mBinding.enterPipBtn.setVisibility(View.GONE);
                mBinding.namePrefix.startCallingAnimation();
                break;
        }
        if (mCallComponent != null) {
            mCallComponent.refreshUi(newState);
        }
    }

    private void toggleCleanScreen() {
        VoipState curState = mCallEngine.getCurVoipState();
        if (curState != VoipState.ONTHECALL) {
            return;
        }
        mWeakHandler.removeMessages(WHAT_CLEAN_SCREEN);
        boolean oldClearing = mClearingScreen;
        if (oldClearing) {
            mBinding.enterPipBtn.setVisibility(View.VISIBLE);
            mBinding.callDuration.setVisibility(View.VISIBLE);
        } else {
            mBinding.enterPipBtn.setVisibility(View.GONE);
            mBinding.callDuration.setVisibility(View.GONE);
        }
        if (mCallComponent != null) {
            mCallComponent.toggleCleanScreen(oldClearing);
            mClearingScreen = !oldClearing;
        }
        if (oldClearing) {
            mWeakHandler.sendEmptyMessageDelayed(WHAT_CLEAN_SCREEN, 5000);
        }
    }

    /**
     * 挂断:通过将状态机器的状态重新置回IDLE状态，回调到 {@link VideoCallComponent#refreshUi(VoipState)}
     * 或者{@link VoiceCallComponent#refreshUi(VoipState)}中调用finish方法关闭页面
     */
    private void hangup() {
        mCallEngine.hangup(null);
    }
}
