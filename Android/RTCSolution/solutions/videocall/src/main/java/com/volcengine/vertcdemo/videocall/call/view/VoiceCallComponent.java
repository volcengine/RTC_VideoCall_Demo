package com.volcengine.vertcdemo.videocall.call.view;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentActivity;

import com.volcengine.vertcdemo.utils.DebounceClickListener;
import com.volcengine.vertcdemo.utils.WeakHandler;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.databinding.ActivityVideoCallVoipBinding;
import com.volcengine.vertcdemo.videocall.databinding.LayoutVoiceCallPanelBinding;
import com.volcengine.vertcdemo.videocall.floatwindow.VoiceFloatWindowComponent;

public class VoiceCallComponent extends AbsCallComponent {
    private LayoutVoiceCallPanelBinding mVoiceCallBinding;

    public VoiceCallComponent(FragmentActivity hostActivity,
                              ActivityVideoCallVoipBinding activityBinding,
                              String remoteUserName,
                              String callerUid,
                              String calleeUid,
                              Runnable cleanTask) {
        super(hostActivity, activityBinding, remoteUserName, callerUid, calleeUid, cleanTask);
    }

    @Override
    public void initView() {
        mActivityBinding.voiceCallPanelVs.inflate();
        View callPanel = mActivityBinding.getRoot().findViewById(R.id.voice_call_panel);
        mVoiceCallBinding = LayoutVoiceCallPanelBinding.bind(callPanel);

        mMicTogglerIv = mVoiceCallBinding.microphoneTogglerBtn;
        mAudioRouteIv = mVoiceCallBinding.audioRouteBtn;
        mAudioRouteTv = mVoiceCallBinding.audioRouteTv;
        mAcceptIv = mVoiceCallBinding.dialBtn;
        mHangupIv = mVoiceCallBinding.hangupBtn;
        mCallingHintTv = mVoiceCallBinding.callingHintTv;
        setClickListener();
    }

    @Override
    public void refreshUi(VoipState newState) {
        switch (newState) {
            case IDLE:
                //Activity 中处理
                break;
            case CALLING:
                mCallingHintTv.setText(R.string.calling_wait_accept);
                mCallingHintTv.setVisibility(View.VISIBLE);
                mVoiceCallBinding.dialBtn.setVisibility(View.GONE);
                mVoiceCallBinding.hangupBtn.setVisibility(View.VISIBLE);
                break;
            case ONTHECALL:
                mCallingHintTv.setVisibility(View.GONE);
                //隐藏接通按钮
                mVoiceCallBinding.dialBtn.setVisibility(View.GONE);
                //调整挂断按钮布局
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mVoiceCallBinding.getRoot());
                constraintSet.connect(mVoiceCallBinding.hangupBtn.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.applyTo(mVoiceCallBinding.getRoot());
                //展示挂断按钮布局
                mVoiceCallBinding.hangupBtn.setVisibility(View.VISIBLE);
                break;
            case RINGING:
                mCallingHintTv.setText(R.string.called_audio_wait_accept);
                mCallingHintTv.setVisibility(View.VISIBLE);
                mVoiceCallBinding.dialBtn.setVisibility(View.VISIBLE);
                mVoiceCallBinding.hangupBtn.setVisibility(View.VISIBLE);
                break;
        }
        updateMicStatus();
        updateAudioRouteStatus();
    }

    @Override
    public void toggleCleanScreen(boolean clearingScreen) {
        mVoiceCallBinding.getRoot().setVisibility(clearingScreen ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void setClickListener() {
        super.setClickListener();
        //进入悬浮窗
        mActivityBinding.enterPipBtn.setOnClickListener(DebounceClickListener.create(v -> {
            enterFloatWindowMode();
        }));
    }

    @Override
    protected void enterFloatWindowMode() {
        if (!checkPopBackgroundPermission()) {
            return;
        }
        if (!VoiceFloatWindowComponent.hasPermission()) {
            VoiceFloatWindowComponent.startOverlaySetting(mHostActivity);
            return;
        }
        VoiceFloatWindowComponent.getInstance().showFloatWindow(mRemoteUserName);
        mHostActivity.finish();
    }

}
