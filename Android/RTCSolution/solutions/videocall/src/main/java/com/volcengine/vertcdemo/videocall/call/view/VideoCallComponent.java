package com.volcengine.vertcdemo.videocall.call.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Rational;
import android.view.TextureView;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.volcengine.vertcdemo.common.SolutionCommonDialog;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.utils.DebounceClickListener;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.databinding.ActivityVideoCallVoipBinding;
import com.volcengine.vertcdemo.videocall.databinding.LayoutVideoCallPanelBinding;
import com.volcengine.vertcdemo.videocall.effect.EffectFragment;
import com.volcengine.vertcdemo.videocall.floatwindow.VideoFloatWindowComponent;

import java.util.HashMap;

public class VideoCallComponent extends AbsCallComponent {
    private LayoutVideoCallPanelBinding mVideoCallBinding;
    private TextureView mScreenRenderView;
    private TextureView mWindowRenderView;
    private VideoFloatWindowComponent mVideoFloatWindow;
    private final HashMap<String, Boolean> mCameraStatus = new HashMap<>(2);
    private EffectFragment mEffectFragment;
    private String mRoomId;

    public VideoCallComponent(FragmentActivity hostActivity,
                              ActivityVideoCallVoipBinding activityBinding,
                              String remoteUserName,
                              String callerUid,
                              String calleeUid,
                              Runnable cleanTask) {
        super(hostActivity, activityBinding, remoteUserName, callerUid, calleeUid, cleanTask);
    }

    @Override
    public void initData() {
        //本地用户如果没有摄像头权限，等同于关闭摄像头
        boolean hasCameraPermission = hasCameraPermission();
        if (!hasCameraPermission) {
            mRTCController.setClosedCamera(true);
        }
        mCameraStatus.put(getLocalUserId(), hasCameraPermission);
    }

    @Override
    public void initView() {
        mActivityBinding.videoCallPanelVs.inflate();
        View callPanel = mActivityBinding.getRoot().findViewById(R.id.video_call_panel);
        mVideoCallBinding = LayoutVideoCallPanelBinding.bind(callPanel);
        mMicTogglerIv = mVideoCallBinding.microphoneTogglerBtn;
        mAudioRouteIv = mVideoCallBinding.audioRouteBtn;
        mAudioRouteTv = mVideoCallBinding.audioRouteTv;
        mAcceptIv = mVideoCallBinding.dialBtn;
        mHangupIv = mVideoCallBinding.hangupBtn;
        mScreenRenderView = mVideoCallBinding.videoScreenRenderSv;
        mWindowRenderView = mVideoCallBinding.videoWindowRenderSv;
        mCallingHintTv = mVideoCallBinding.callingHintTv;
        //渲染本地画面
        startRenderVideo(mScreenRenderView, getLocalUserId());
        //如果没有摄像头权限提示开启
        if (!hasCameraPermission()) {
            SolutionToast.show(R.string.camera_permission_hint);
        }
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
                mVideoCallBinding.microphoneTogglerBtn.setVisibility(View.GONE);
                mVideoCallBinding.microphoneTogglerTv.setVisibility(View.GONE);
                mVideoCallBinding.audioRouteBtn.setVisibility(View.GONE);
                mVideoCallBinding.audioRouteTv.setVisibility(View.GONE);
                mVideoCallBinding.videoEffectBtn.setVisibility(View.GONE);
                mVideoCallBinding.videoEffectTv.setVisibility(View.GONE);
                mVideoCallBinding.dialBtn.setVisibility(View.GONE);
                mVideoCallBinding.cameraSwitchOnCallBtn.setVisibility(View.GONE);
                mVideoCallBinding.cameraSwitchOnCallTv.setVisibility(View.GONE);
                mActivityBinding.namePrefix.startCallingAnimation();
                break;
            case ONTHECALL:
                mCallingHintTv.setVisibility(View.GONE);
                mVideoCallBinding.microphoneTogglerBtn.setVisibility(View.VISIBLE);
                mVideoCallBinding.microphoneTogglerTv.setVisibility(View.VISIBLE);
                mVideoCallBinding.audioRouteBtn.setVisibility(View.VISIBLE);
                mVideoCallBinding.audioRouteTv.setVisibility(View.VISIBLE);
                mVideoCallBinding.cameraSwitcherCallingTv.setVisibility(View.GONE);
                mVideoCallBinding.cameraSwitcherCallingBtn.setVisibility(View.GONE);
                mVideoCallBinding.videoEffectBtn.setVisibility(View.VISIBLE);
                mVideoCallBinding.videoEffectTv.setVisibility(View.VISIBLE);
                mVideoCallBinding.dialBtn.setVisibility(View.GONE);
                mVideoCallBinding.cameraSwitchOnCallBtn.setVisibility(View.VISIBLE);
                mVideoCallBinding.cameraSwitchOnCallTv.setVisibility(View.VISIBLE);
                mActivityBinding.namePrefix.stopCallingAnimation();
                break;
            case RINGING:
                mActivityBinding.enterPipBtn.setVisibility(View.GONE);
                mCallingHintTv.setText(R.string.called_video_wait_accept);
                mCallingHintTv.setVisibility(View.VISIBLE);
                mVideoCallBinding.microphoneTogglerBtn.setVisibility(View.GONE);
                mVideoCallBinding.microphoneTogglerTv.setVisibility(View.GONE);
                mVideoCallBinding.audioRouteBtn.setVisibility(View.GONE);
                mVideoCallBinding.audioRouteTv.setVisibility(View.GONE);
                mVideoCallBinding.videoEffectBtn.setVisibility(View.GONE);
                mVideoCallBinding.videoEffectTv.setVisibility(View.GONE);
                mVideoCallBinding.cameraSwitchOnCallBtn.setVisibility(View.GONE);
                mVideoCallBinding.cameraSwitchOnCallTv.setVisibility(View.GONE);
                mActivityBinding.namePrefix.startCallingAnimation();
                break;
        }
        updateMicStatus();
        updateAudioRouteStatus();
        updateCameraStatus();
        updateVideoFloatWindow();
    }

    @Override
    public void toggleCleanScreen(boolean clearingScreen) {
        mVideoCallBinding.videoControlBtns.setVisibility(clearingScreen ? View.VISIBLE : View.GONE);
    }

    /**
     * 更新通话时长
     */
    public void updateCallDuration(String duration) {
        if (mVideoFloatWindow != null) {
            mVideoFloatWindow.updateCallStatus(duration);
        }
    }

    /**
     * RTC 远端用户进入房间时回调
     */
    public void onUserJoined(String userId) {
        mVideoCallBinding.videoWindowRenderFl.setVisibility(View.VISIBLE);
        //默认大窗为远端画面
        exchangeRenderView();
        updateUname();
    }

    /**
     * RTC 远端用户解析首帧视频回调
     */
    public void onFirstRemoteVideoFrameDecoded(String roomId, String userId) {
        mRoomId = roomId;
        mCameraStatus.put(userId, true);
        boolean localInScreen = localInScreen();
        TextureView remoteRenderVideo = localInScreen ? mWindowRenderView : mScreenRenderView;
        startRenderVideo(remoteRenderVideo, userId);
        if (mRTCController != null) {
            updateVideoFloatWindow();
        }
    }

    /**
     * RTC完成第一帧视频帧或屏幕视频帧采集时，收到此回调(等同于开启摄像头)
     */
    public void onFirstLocalVideoFrameCaptured() {
        TextureView localRenderVideo = localInScreen() ? mScreenRenderView : mWindowRenderView;
        localRenderVideo.setVisibility(View.VISIBLE);
    }

    /**
     * 用户摄像头开关回调
     */
    public void onUserToggleCamera(String userId, boolean on) {
        mCameraStatus.put(userId, on);
        if (mScreenRenderView == null) {
            return;
        }
        Object uidInScreen = mScreenRenderView.getTag(R.id.render_view_uid);
        if (uidInScreen == null) {
            return;
        }
        TextureView targetView = TextUtils.equals((String) uidInScreen, userId)
                ? mScreenRenderView
                : mWindowRenderView;
        startRenderVideo(targetView, userId);
        //本地用户开关摄像头
        if (TextUtils.equals(getLocalUserId(), userId)) {
            updateCameraStatus();
            if (!on) {
                SolutionToast.show(R.string.local_user_close_camera);
            }
            return;
        }
        //远端用户开关摄像头
        if (!on) {
            SolutionToast.show(R.string.remote_user_close_camera);
        }
        updateVideoFloatWindow();
    }


    /**
     * 进入或退出画中画模式的系统回调，仅视频通话使用
     */
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (mVideoFloatWindow != null) {
            mVideoFloatWindow.onPictureInPictureModeChanged(isInPictureInPictureMode, mRoomId);
        }
    }

    @Override
    protected void setClickListener() {
        super.setClickListener();
        //进入PIP悬浮窗
        mActivityBinding.enterPipBtn.setOnClickListener(DebounceClickListener.create(v -> {
            enterFloatWindowMode();
        }));
        //开关摄像头
        mVideoCallBinding.cameraTogglerBtn.setOnClickListener(wrapClickForCleanScreen(v -> {
            if (checkCameraPermission()) {
                mRTCController.toggleCamera();
            }
        }));
        //切换前后摄像头
        View.OnClickListener listener = wrapClickForCleanScreen(v -> {
            boolean closedCamera = mRTCController.isClosedCamera();
            if (closedCamera) return;
            mRTCController.switchCamera();
        });
        mVideoCallBinding.cameraSwitchOnCallBtn.setOnClickListener(listener);
        mVideoCallBinding.cameraSwitcherCallingBtn.setOnClickListener(listener);
        //开启美颜设置
        mVideoCallBinding.videoEffectBtn.setOnClickListener(DebounceClickListener.create(v -> {
            boolean closedCamera = mRTCController.isClosedCamera();
            if (closedCamera) {
                return;
            }
            mVideoCallBinding.effectFl.setVisibility(View.VISIBLE);
            mEffectFragment = EffectFragment.start(R.id.effect_container, mHostActivity.getSupportFragmentManager());
            mVideoCallBinding.videoControlBtns.setVisibility(View.GONE);
        }));
        //关闭美颜设置
        mVideoCallBinding.effectFl.setOnClickListener(DebounceClickListener.create(v -> {
            if (mEffectFragment != null && mEffectFragment.isVisible()) {
                EffectFragment.remove(mEffectFragment, mHostActivity.getSupportFragmentManager());
                mVideoCallBinding.effectFl.setVisibility(View.GONE);
                mVideoCallBinding.videoControlBtns.setVisibility(View.VISIBLE);
            }
        }));
        //大小窗切换
        mVideoCallBinding.videoWindowRenderFl.setOnClickListener(DebounceClickListener.create(v -> {
            exchangeRenderView();
            updateUname();
        }));
    }

    @Override
    protected void enterFloatWindowMode() {
        if (!checkPopBackgroundPermission()) {
            return;
        }
        if (mVideoFloatWindow == null) {
            mVideoFloatWindow = new VideoFloatWindowComponent(mHostActivity, mActivityBinding, mVideoCallBinding, mCameraStatus);
        }
        Rational rational = new Rational(90, 130);
        mVideoFloatWindow.enterPiP(rational, getRemoteUserId(), mRemoteUserName);
    }

    /**
     * 更新用户名
     */
    private void updateUname() {
        boolean localInScreen = localInScreen();
        String windowUserName = localInScreen ? mRemoteUserName : SolutionDataManager.ins().getUserName();
        String windowNamePrefix = TextUtils.isEmpty(windowUserName) ? "" : windowUserName.substring(0, 1);
        mVideoCallBinding.namePrefixTv.setText(windowNamePrefix);

        String screenUserName = localInScreen ? SolutionDataManager.ins().getUserName() : mRemoteUserName;
        String screenNamePrefix = TextUtils.isEmpty(screenUserName) ? "" : screenUserName.substring(0, 1);
        mActivityBinding.namePrefix.setRemoteNamePrefix(screenNamePrefix);
        mActivityBinding.remoteNameTv.setText(screenUserName);
    }

    /**
     * 根据摄像头开关及摄像头权限更新UI
     */
    private void updateCameraStatus() {
        Boolean cameraOn = mCameraStatus.get(getLocalUserId());
        if (cameraOn != null) {
            mVideoCallBinding.cameraTogglerBtn.setImageResource(cameraOn
                    ? R.drawable.ic_camera_on
                    : R.drawable.ic_camera_off_red);
            setEnableStatus(cameraOn, mVideoCallBinding.videoEffectBtn);
            setEnableStatus(cameraOn, mVideoCallBinding.videoEffectTv);
            setEnableStatus(cameraOn, mVideoCallBinding.cameraSwitchOnCallBtn);
            setEnableStatus(cameraOn, mVideoCallBinding.cameraSwitchOnCallTv);
            setEnableStatus(cameraOn, mVideoCallBinding.cameraSwitcherCallingBtn);
            setEnableStatus(cameraOn, mVideoCallBinding.cameraSwitcherCallingTv);
        }
    }

    /**
     * 根据当前通话状态更新视频悬浮窗中通话状态
     */
    private void updateVideoFloatWindow() {
        if (mVideoFloatWindow == null) return;
        boolean inPip = mVideoFloatWindow.isInPiP();
        mVideoFloatWindow.updateFloatWindowUi(inPip, mRoomId);
    }

    /**
     * 本地用户视频是否在大窗中渲染
     */
    private boolean localInScreen() {
        String uidInScreen = (String) mScreenRenderView.getTag(R.id.render_view_uid);
        return TextUtils.equals(uidInScreen, getLocalUserId());
    }

    /**
     * 大小窗切换
     */
    private void exchangeRenderView() {
        boolean localInScreen = localInScreen();
        TextureView localRenderView = localInScreen ? mWindowRenderView : mScreenRenderView;
        startRenderVideo(localRenderView, getLocalUserId());

        String remoteUid = getRemoteUserId();
        TextureView remoteRenderView = localRenderView == mWindowRenderView ? mScreenRenderView : mWindowRenderView;
        startRenderVideo(remoteRenderView, remoteUid);
    }

    /**
     * 视频渲染
     *
     * @param renderView 渲染目标View
     * @param userId     被渲染的用户id
     */
    private void startRenderVideo(TextureView renderView, String userId) {
        renderView.setTag(R.id.render_view_uid, userId);
        boolean cameraOn = mCameraStatus.get(userId) != null && Boolean.TRUE.equals(mCameraStatus.get(userId));
        //本地用户开启摄像头将View置为可见在onFirstLocalVideoFrameCaptured回调中，这样避免展示上次的最后一帧
        if (!cameraOn || !TextUtils.equals(getLocalUserId(), userId)) {
            renderView.setVisibility(cameraOn ? View.VISIBLE : View.GONE);
        }
        if (TextUtils.equals(getLocalUserId(), userId)) {
            mRTCController.startRenderLocalVideo(renderView);
        } else {
            mRTCController.startRenderRemoteVideo(userId, mRoomId, renderView);
        }
    }

    /**
     * 检查是否有摄像头权限，如果没有弹窗提示用户到设置授予
     *
     * @return true 有摄像头权限不需要弹窗提示
     */
    private boolean checkCameraPermission() {
        if (hasCameraPermission()) {
            return true;
        }
        SolutionCommonDialog dialog = new SolutionCommonDialog(mHostActivity);
        dialog.setMessage(mHostActivity.getString(R.string.camera_permission_hint));
        dialog.setCancelable(false);
        dialog.setNegativeListener(negBtn -> dialog.dismiss());
        dialog.setPositiveListener(pos -> {
            dialog.dismiss();
            mHostActivity.startActivity(new Intent(Settings.ACTION_SETTINGS));
        });
        dialog.show();
        return false;
    }

    /**
     * 是否有摄像头权限
     */
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(AppUtil.getApplicationContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED;
    }

}
