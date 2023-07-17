package com.volcengine.vertcdemo.videocall.floatwindow;

import static androidx.lifecycle.Lifecycle.State.RESUMED;
import static com.volcengine.vertcdemo.videocall.call.state.VoipState.CALLING;
import static com.volcengine.vertcdemo.videocall.call.state.VoipState.ONTHECALL;
import static com.volcengine.vertcdemo.videocall.call.state.VoipState.RINGING;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.Rational;
import android.view.TextureView;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.annotation.RequiresApi;

import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.CallEngine;
import com.volcengine.vertcdemo.videocall.call.RTCController;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.databinding.ActivityVideoCallVoipBinding;
import com.volcengine.vertcdemo.videocall.databinding.LayoutVideoCallPanelBinding;
import com.volcengine.vertcdemo.videocall.util.Util;

import java.util.HashMap;

/**
 * 视频画中画组件
 */
public class VideoFloatWindowComponent {
    private static final String TAG = "VideoFloatWindow";
    private final ComponentActivity mHost;
    private final ActivityVideoCallVoipBinding mActivityBinding;
    private final LayoutVideoCallPanelBinding mVideoCallBinding;
    private String mRemoteUid;
    private String mRemoteUname;
    private final HashMap<String, Boolean> mCameraStatus;

    public VideoFloatWindowComponent(ComponentActivity host,
                                     ActivityVideoCallVoipBinding activityBinding,
                                     LayoutVideoCallPanelBinding videoCallBinding,
                                     HashMap<String, Boolean> cameraStatus) {
        mHost = host;
        mActivityBinding = activityBinding;
        mVideoCallBinding = videoCallBinding;
        mCameraStatus = cameraStatus;
    }

    /**
     * 是否支持画中画功能
     */
    private Boolean mIsSupportPip;

    private boolean isSupportPiP() {
        if (mIsSupportPip == null) {
            mIsSupportPip = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    && mHost.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
        }
        return mIsSupportPip;
    }

    /**
     * 是否处于画中画
     */
    public boolean isInPiP() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false;
        }
        return mHost.isInPictureInPictureMode();
    }

    /**
     * 进入画中画
     *
     * @param aspectRatio 画中画小窗宽高比
     */
    public void enterPiP(Rational aspectRatio,
                         String remoteUid,
                         String remoteUname) {
        if (!isSupportPiP() || isInPiP()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!hasPiPPermission()) {
                SolutionToast.show(R.string.pip_permission_guide);
                startPiPPermissionSetting();
                return;
            }
            mRemoteUid = remoteUid;
            mRemoteUname = remoteUname;
            PictureInPictureParams mPiPParams = new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build();
            if (mHost.getLifecycle().getCurrentState() != RESUMED) {
                return;
            }
            mHost.enterPictureInPictureMode(mPiPParams);
        }
    }

    /**
     * 进出画中画的系统回调：Android画中画功能小窗就是将整个Activity画面按照设置的比例缩放后渲染出来，因此进入画中画模式时
     * 需要将不需要展示的UI元素隐藏，退出画中画模式时再将Activity正常UI元素恢复
     *
     * @param isInPiPMode 是否为进入画中画
     */
    public void onPictureInPictureModeChanged(boolean isInPiPMode, String roomId) {
        Log.d(TAG, "VideoFloatWindowComponent onPictureInPictureModeChanged isInPiPMode:" + isInPiPMode);
        CallEngine.getInstance().setInFloatWindow(isInPiPMode);
        updateFloatWindowUi(isInPiPMode, roomId);
    }

    /**
     * 更新画中画悬浮窗UI
     */
    public void updateFloatWindowUi(boolean inPiPMode, String roomId) {
        VoipState curState = CallEngine.getInstance().getCurVoipState();
        String floatWindowRenderUid = curState != VoipState.ONTHECALL
                ? SolutionDataManager.ins().getUserId()
                : mRemoteUid;
        if (inPiPMode) {//处于PIP中
            mActivityBinding.rootViewInPip.setVisibility(View.VISIBLE);
            startRenderVideo(mActivityBinding.videoPipRenderView, floatWindowRenderUid, roomId);
            updateRenderUname(curState);
            updateCallStatus(curState);
            mActivityBinding.rootViewNonPip.setVisibility(View.GONE);
        } else {//已退出PIP
            mActivityBinding.rootViewNonPip.setVisibility(View.VISIBLE);
            //恢Activity视频渲染
            String windowUid = (String) mVideoCallBinding.videoWindowRenderSv.getTag(R.id.render_view_uid);
            startRenderVideo(mVideoCallBinding.videoWindowRenderSv, windowUid, roomId);

            String screenUid = (String) mVideoCallBinding.videoScreenRenderSv.getTag(R.id.render_view_uid);
            startRenderVideo(mVideoCallBinding.videoScreenRenderSv, screenUid, roomId);

            mActivityBinding.rootViewInPip.setVisibility(View.GONE);
            mRemoteUname = null;
        }
    }

    /**
     * 更新通话状态
     */
    public void updateCallStatus(String status) {
        if (!isInPiP() || TextUtils.isEmpty(status)) {
            return;
        }
        mActivityBinding.videoPipCallStatus.setText(status);
    }

    /**
     * 视频渲染
     *
     * @param renderView 渲染目标View
     * @param userId     被渲染的用户id
     */
    private void startRenderVideo(TextureView renderView, String userId, String roomId) {
        RTCController rtcController = CallEngine.getInstance().getRTCController();
        renderView.setTag(R.id.render_view_uid, userId);
        boolean cameraOn = mCameraStatus.get(userId) != null && Boolean.TRUE.equals(mCameraStatus.get(userId));
        renderView.setVisibility(cameraOn ? View.VISIBLE : View.GONE);
        if (cameraOn) {
            if (TextUtils.equals(SolutionDataManager.ins().getUserId(), userId)) {
                rtcController.startRenderLocalVideo(renderView);
            } else {
                rtcController.startRenderRemoteVideo(userId, roomId, renderView);
            }
        }
    }

    /**
     * 更新通话状态
     */
    private void updateCallStatus(VoipState curState) {
        if (!isInPiP()) {
            return;
        }
        int statusStringResId = 0;
        if (curState == CALLING) {
            statusStringResId = R.string.calling_wait_accept;
        } else if (curState == RINGING) {
            statusStringResId = R.string.called_video_wait_accept;
        }
        if (statusStringResId != 0) {
            updateCallStatus(Util.getString(statusStringResId));
        }
    }

    /**
     * 更新当前悬浮窗中渲染用户的用户名前缀
     */
    private void updateRenderUname(VoipState curState) {
        if (!isInPiP() || curState == null) {
            return;
        }
        String renderUname = curState != ONTHECALL ? SolutionDataManager.ins().getUserName() : mRemoteUname;
        if (!TextUtils.isEmpty(renderUname)) {
            mActivityBinding.videoPipNamePrefix.setRemoteNamePrefix(renderUname.substring(0, 1));
        }
        if (curState != ONTHECALL) {
            mActivityBinding.videoPipNamePrefix.startCallingAnimation();
        } else {
            mActivityBinding.videoPipNamePrefix.stopCallingAnimation();
        }
    }

    /**
     * 检查是否有画中画权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean hasPiPPermission() {
        AppOpsManager appOpsManager = (AppOpsManager) mHost.getSystemService(Context.APP_OPS_SERVICE);
        if (appOpsManager == null) return false;
        int pid = android.os.Process.myUid();
        String packageName = mHost.getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return appOpsManager.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, pid, packageName) == AppOpsManager.MODE_ALLOWED;
        } else {
            return appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, pid, packageName) == AppOpsManager.MODE_ALLOWED;
        }
    }

    /**
     * 开启画中画权限设置页面
     */
    private void startPiPPermissionSetting() {
        try {
            mHost.startActivity(new Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS"));
        } catch (Exception exception) {
            Log.d(TAG, "start pip permission failed:" + exception);
        }
    }
}
