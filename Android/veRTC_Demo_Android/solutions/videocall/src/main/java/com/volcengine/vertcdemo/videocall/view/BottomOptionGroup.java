package com.volcengine.vertcdemo.videocall.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.core.Constants;
import com.volcengine.vertcdemo.videocall.core.VideoCallRTCManager;
import com.volcengine.vertcdemo.videocall.event.AudioRouterEvent;
import com.volcengine.vertcdemo.videocall.event.MediaStatusEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 房间页面底部功能区域
 * <p>
 * 包含有五个独立的功能按钮
 * <p>
 * 包含功能：
 * 1.打开关闭麦克风（内部调用RTC manager接口，并根据事件改变UI显示）
 * 2.打开关闭摄像头（内部调用RTC manager接口，并根据事件改变UI显示）
 * 3.切换扬声器（内部调用RTC manager接口，并根据事件改变UI显示）
 * 4.打开实时数据统计对话框
 * 5.打开设置对话框
 */
public class BottomOptionGroup extends LinearLayout {

    private BottomOptionView mMicrophone;
    private BottomOptionView mCamera;
    private BottomOptionView mSpeakerPhone;

    public BottomOptionGroup(Context context) {
        super(context);
        initView();
    }

    public BottomOptionGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BottomOptionGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_option_group, this, true);
        mMicrophone = findViewById(R.id.group_microphone);
        mCamera = findViewById(R.id.group_camera);
        mSpeakerPhone = findViewById(R.id.group_speaker_phone);
        BottomOptionView mediaStats = findViewById(R.id.group_media_stats);
        BottomOptionView setting = findViewById(R.id.group_setting);

        mMicrophone.setText(R.string.bottom_option_microphone);
        mCamera.setText(R.string.bottom_option_camera);
        mediaStats.setText(R.string.bottom_option_media_stats);
        setting.setText(R.string.bottom_option_setting);

        updateMicrophone(VideoCallRTCManager.ins().isMicOn());
        updateCamera(VideoCallRTCManager.ins().isCameraOn());
        updateSpeakerPhone(VideoCallRTCManager.ins().isSpeakerphone());
        mediaStats.setIcon(R.drawable.media_stat_icon);
        setting.setIcon(R.drawable.setting_icon);

        mMicrophone.setOnClickListener((v) ->
                VideoCallRTCManager.ins().startPublishAudio(!VideoCallRTCManager.ins().isMicOn()));
        mCamera.setOnClickListener((v) ->
                VideoCallRTCManager.ins().startVideoCapture(!VideoCallRTCManager.ins().isCameraOn()));
        mSpeakerPhone.setOnClickListener((v) ->
                VideoCallRTCManager.ins().useSpeakerphone(!VideoCallRTCManager.ins().isSpeakerphone()));
        mediaStats.setOnClickListener((v) -> {
            MediaStatsDialog mediaStatsDialog = new MediaStatsDialog(getContext());
            mediaStatsDialog.show();
        });
        setting.setOnClickListener((v) -> {
            SettingDialog settingDialog = new SettingDialog(getContext());
            settingDialog.show();
        });
    }

    /**
     * 根据麦克风状态更新UI
     *
     * @param isOn 是否打开
     */
    public void updateMicrophone(boolean isOn) {
        mMicrophone.setIcon(isOn ? R.drawable.micro_phone_enable_icon : R.drawable.micro_phone_disable_icon);
    }

    /**
     * 根据摄像头状态更新UI
     *
     * @param isOn 是否打开
     */
    public void updateCamera(boolean isOn) {
        mCamera.setIcon(isOn ? R.drawable.camera_enable_icon : R.drawable.camera_disable_icon);
    }

    /**
     * 根据音频路由状态更新UI
     *
     * @param isOn 是否使用扬声器
     */
    public void updateSpeakerPhone(boolean isOn) {
        mSpeakerPhone.setIcon(isOn ? R.drawable.speakerphone_icon
                : R.drawable.earpiece_icon);
        mSpeakerPhone.setText(isOn ? R.string.bottom_option_speaker_phone : R.string.bottom_option_earpiece);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        SolutionDemoEventManager.register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        SolutionDemoEventManager.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaStatusEvent(MediaStatusEvent event) {
        if (!TextUtils.equals(SolutionDataManager.ins().getUserId(), event.uid)) {
            return;
        }
        boolean on = event.status == Constants.MEDIA_STATUS_ON;
        if (event.mediaType == Constants.MEDIA_TYPE_AUDIO) {
            mMicrophone.setIcon(on ? R.drawable.micro_phone_enable_icon
                    : R.drawable.micro_phone_disable_icon);
        } else if (event.mediaType == Constants.MEDIA_TYPE_VIDEO) {
            mCamera.setIcon(on ? R.drawable.camera_enable_icon :
                    R.drawable.camera_disable_icon);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioRouterEvent(AudioRouterEvent event) {
        updateSpeakerPhone(event.isSpeakerPhone);
    }
}
