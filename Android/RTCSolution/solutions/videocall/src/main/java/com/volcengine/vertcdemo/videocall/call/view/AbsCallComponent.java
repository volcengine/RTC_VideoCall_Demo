package com.volcengine.vertcdemo.videocall.call.view;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.ss.bytertc.engine.data.AudioRoute;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.http.AppNetworkStatusUtil;
import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.utils.DebounceClickListener;
import com.volcengine.vertcdemo.utils.WeakHandler;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.CallEngine;
import com.volcengine.vertcdemo.videocall.call.RTCController;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.databinding.ActivityVideoCallVoipBinding;
import com.volcengine.vertcdemo.videocall.floatwindow.PopBackgroundPermissionUtil;
import com.volcengine.vertcdemo.videocall.util.Util;

import java.util.HashMap;

public abstract class AbsCallComponent {
    protected final FragmentActivity mHostActivity;
    protected final ActivityVideoCallVoipBinding mActivityBinding;
    protected ImageView mMicTogglerIv;
    protected TextView mCallingHintTv;
    protected ImageView mAudioRouteIv;
    protected TextView mAudioRouteTv;
    protected ImageView mAcceptIv;
    protected ImageView mHangupIv;
    protected String mRemoteUserName;
    protected String mCallerUid;
    protected String mCalleeUid;
    protected CallEngine mCallEngine;
    protected RTCController mRTCController;

    protected Runnable mCleanTask;

    public AbsCallComponent(FragmentActivity hostActivity,
                            ActivityVideoCallVoipBinding activityBinding,
                            String remoteUserName,
                            String callerUid,
                            String calleeUid,
                            Runnable cleanTask) {
        this.mHostActivity = hostActivity;
        this.mActivityBinding = activityBinding;
        this.mRemoteUserName = remoteUserName;
        this.mCallerUid = callerUid;
        this.mCalleeUid = calleeUid;
        this.mCallEngine = CallEngine.getInstance();
        this.mRTCController = mCallEngine.getRTCController();
        this.mCleanTask = cleanTask;
    }

    public void initData() {
    }

    public abstract void initView();

    /**
     * 根据通话状态刷新UI
     *
     * @param newState 新的通话状态
     */
    public abstract void refreshUi(VoipState newState);

    /**
     * 清屏功能
     *
     * @param clearingScreen 当前是否为状态
     */
    public abstract void toggleCleanScreen(boolean clearingScreen);

    /**
     * 根据当前麦克风开关状态更新UI
     */
    public void updateMicStatus() {
        if (mMicTogglerIv == null) {
            return;
        }
        mMicTogglerIv.setImageResource(mRTCController.isClosedMic()
                ? R.drawable.ic_microphone_off : R.drawable.ic_microphone_on);
    }

    /**
     * 根据当前音频路由状态更新UI
     */
    public void updateAudioRouteStatus() {
        AudioRoute route = mRTCController.getCurAudioRoute();
        boolean enable = route == AudioRoute.AUDIO_ROUTE_EARPIECE
                || route == AudioRoute.AUDIO_ROUTE_SPEAKERPHONE;
        setEnableStatus(enable, mAudioRouteIv);
        setEnableStatus(enable, mAudioRouteTv);
        if (!enable) {
            mAudioRouteIv.setEnabled(false);
            return;
        }
        mAudioRouteIv.setEnabled(true);
        mAudioRouteTv.setText(route == AudioRoute.AUDIO_ROUTE_SPEAKERPHONE
                ? R.string.speaker
                : R.string.earpiece);
        mAudioRouteIv.setImageResource(route == AudioRoute.AUDIO_ROUTE_SPEAKERPHONE ?
                R.drawable.ic_audio_route_speaker :
                R.drawable.ic_audio_route_earpiece);
    }

    /**
     * 更新网络质量
     */
    public void updateNetQuality(HashMap<String, Boolean> quality) {
        if (quality == null || quality.size() == 0) {
            return;
        }
        if (mCallEngine.getCurVoipState() != VoipState.ONTHECALL) {
            return;
        }
        Boolean localUserNetBad = quality.get(getLocalUserId());
        Boolean remoteUserNetBad = quality.get(getRemoteUserId());
        mCallingHintTv.setVisibility(
                (Boolean.TRUE.equals(localUserNetBad) || Boolean.TRUE.equals(remoteUserNetBad))
                        ? View.VISIBLE
                        : View.GONE);
        if (Boolean.TRUE.equals(localUserNetBad)) {
            mCallingHintTv.setText(R.string.local_network_bad);
            return;
        }
        if (Boolean.TRUE.equals(remoteUserNetBad)) {
            mCallingHintTv.setText(R.string.remote_network_bad);
        }
    }

    /**
     * 设置UI点击监听
     */
    protected void setClickListener() {
        //开关麦克风
        mMicTogglerIv.setOnClickListener(wrapClickForCleanScreen(v -> mRTCController.toggleMic()));
        //音频路由切换
        mAudioRouteIv.setOnClickListener(wrapClickForCleanScreen(v -> mRTCController.switchAudioRoute()));
        //接通通话
        mAcceptIv.setOnClickListener(
                DebounceClickListener.create(v -> {
                    if (checkNetAvailable()) {
                        mCallEngine.accept(result -> {
                            if (!result.success) {
                                SolutionToast.show((String) result.result);
                            }
                        });
                    }
                }));
        //挂断通话
        mHangupIv.setOnClickListener(DebounceClickListener.create(v -> mCallEngine.hangup(null)));
    }

    /**
     * 进入悬浮窗模式
     */
    protected abstract void enterFloatWindowMode();

    protected boolean checkPopBackgroundPermission() {
        boolean hasPermission = PopBackgroundPermissionUtil.hasPopupBackgroundPermission();
        if (!hasPermission) {
            SolutionToast.show(Util.getString(R.string.pop_background_permission));
        }
        return hasPermission;
    }

    /**
     * 设置按钮开启状态：如果不能启用设置为灰色
     */
    protected void setEnableStatus(boolean enable, ImageView view) {
        if (enable) {
            view.clearColorFilter();
        } else {
            view.setColorFilter(Color.GRAY);
        }
    }

    protected void setEnableStatus(boolean enable, TextView view) {
        view.setTextColor(ContextCompat.getColor(mHostActivity, enable ? R.color.white : R.color.gray_86909C));
    }

    protected View.OnClickListener wrapClickForCleanScreen(View.OnClickListener listener) {
        return DebounceClickListener.create(v -> {
            if (mCleanTask != null) {
                mCleanTask.run();
            }
            if (listener != null) {
                listener.onClick(v);
            }
        });
    }

    /**
     * 获取远端用户id
     */
    protected String getRemoteUserId() {
        return TextUtils.equals(SolutionDataManager.ins().getUserId(), mCallerUid) ? mCalleeUid : mCallerUid;
    }

    /**
     * 获取本地用户id
     */
    protected String getLocalUserId() {
        return SolutionDataManager.ins().getUserId();
    }

    /**
     * 检查网络是否可用，如果没有弹Toast提示
     *
     * @return true 有网络链接
     */
    private boolean checkNetAvailable() {
        if (AppNetworkStatusUtil.isConnected(AppUtil.getApplicationContext())) {
            return true;
        }
        SolutionToast.show(R.string.network_link_down);
        return false;
    }


}
