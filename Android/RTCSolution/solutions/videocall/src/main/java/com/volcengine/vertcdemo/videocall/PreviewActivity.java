package com.volcengine.vertcdemo.videocall;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.volcengine.vertcdemo.common.SolutionBaseActivity;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.videocall.call.CallEngine;
import com.volcengine.vertcdemo.videocall.call.observer.AbsCallObserver;
import com.volcengine.vertcdemo.videocall.call.observer.CallObserver;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.databinding.ActivityVideoCallPreviewBinding;
import com.volcengine.vertcdemo.videocall.effect.EffectFragment;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;

/**
 * 预览界面：主要用于设置美颜效果
 */
public class PreviewActivity extends SolutionBaseActivity {
    //预览过程中如果收到呼叫通知，自动关闭自己
    CallObserver mCallObserver = new AbsCallObserver() {
        @Override
        public void onCallStateChange(VoipState oldState, VoipState newState, VoipInfo info) {
            if (oldState != VoipState.IDLE) {
                finish();
            }
        }
    };

    private ActivityVideoCallPreviewBinding binding;
    private final CallEngine mCallEngine = CallEngine.getInstance();

    public static void start(Context context) {
        Intent intent = new Intent(context, PreviewActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoCallPreviewBinding.inflate(getLayoutInflater());
        binding.closeBtn.setOnClickListener(v -> finish());
        setContentView(binding.getRoot());
        boolean hasCameraPermission = hasPermission(Manifest.permission.CAMERA);
        if (!hasCameraPermission) {
            requestPermissions(Manifest.permission.CAMERA);
        } else {
            startVideo();
        }
        EffectFragment.start(R.id.effect_fl, getSupportFragmentManager());
        mCallEngine.addObserver(mCallObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCallEngine.removeObserver(mCallObserver);
    }

    @Override
    protected void onPermissionResult(String permission, boolean granted) {
        if (TextUtils.equals(permission, Manifest.permission.CAMERA)) {
            if (granted) {
                startVideo();
            } else {
                SolutionToast.show(getString(R.string.reject_permission, "camera"));
                finish();
            }
        }
    }

    private void startVideo() {
        mCallEngine.getRTCController().startVideoCapture();
        mCallEngine.getRTCController().startRenderLocalVideo(binding.videoRenderSv);
    }
}