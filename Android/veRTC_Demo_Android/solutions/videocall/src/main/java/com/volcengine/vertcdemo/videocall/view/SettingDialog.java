package com.volcengine.vertcdemo.videocall.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatDialog;

import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.core.Constants;
import com.volcengine.vertcdemo.videocall.core.VideoCallRTCManager;

import java.util.ArrayList;

/**
 * 房间内设置对话框
 * <p>
 * 功能：
 * 1.设置视频分辨率
 * 2.设置通话质量
 * 3.设置本地镜像
 */
public class SettingDialog extends AppCompatDialog {

    private final View mView;

    public SettingDialog(Context context) {
        super(context, R.style.CommonDialog);
        setCancelable(true);

        mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_setting, null);
        mView.setOnClickListener((v) -> {
        });
        mView.findViewById(R.id.setting_back).setOnClickListener((v) -> dismiss());
        SettingItemLayout resolutionLayout = mView.findViewById(R.id.setting_resolution);
        SettingItemLayout qualityLayout = mView.findViewById(R.id.setting_quality);
        SettingItemLayout mirrorLayout = mView.findViewById(R.id.setting_mirror);

        resolutionLayout.setData(getContext().getString(R.string.setting_dialog_resolution),
                VideoCallRTCManager.ins().getResolution());
        qualityLayout.setData(getContext().getString(R.string.setting_dialog_quality),
                VideoCallRTCManager.ins().getAudioQuality());
        mirrorLayout.setData(getContext().getString(R.string.setting_dialog_mirror),
                VideoCallRTCManager.ins().isVideoMirror());

        resolutionLayout.setOnClickListener((v) -> {
            SubSettingDialog subSettingDialog = new SubSettingDialog(getContext());
            subSettingDialog.setData(getContext().getString(R.string.setting_dialog_resolution),
                    new ArrayList<>(Constants.RESOLUTION_MAP.keySet()),
                    VideoCallRTCManager.ins().getResolution(),
                    (position, str) -> {
                        VideoCallRTCManager.ins().setVideoResolution(str);
                        resolutionLayout.setData(getContext().getString(R.string.setting_dialog_resolution),
                                VideoCallRTCManager.ins().getResolution());
                    });
            subSettingDialog.show();
        });
        qualityLayout.setOnClickListener((v) -> {
            SubSettingDialog subSettingDialog = new SubSettingDialog(getContext());
            subSettingDialog.setData(getContext().getString(R.string.setting_dialog_quality),
                    new ArrayList<>(Constants.QUALITY_MAP.keySet()),
                    VideoCallRTCManager.ins().getAudioQuality(),
                    (position, str) -> {
                        VideoCallRTCManager.ins().setAudioProfile(str);
                        qualityLayout.setData(getContext().getString(R.string.setting_dialog_quality),
                                VideoCallRTCManager.ins().getAudioQuality());
                    });
            subSettingDialog.show();
        });
        mirrorLayout.setOnCheckListener((v, isChecked) -> {
            VideoCallRTCManager.ins().setMirrorType(isChecked);
            mirrorLayout.setData(getContext().getString(R.string.setting_dialog_mirror),
                    VideoCallRTCManager.ins().isVideoMirror());
        });
    }

    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowUtils.getScreenWidth(getContext());
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        getWindow().setContentView(mView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }
}
