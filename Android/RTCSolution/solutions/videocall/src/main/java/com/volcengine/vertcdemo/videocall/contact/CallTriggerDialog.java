package com.volcengine.vertcdemo.videocall.contact;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.utils.DebounceClickListener;
import com.volcengine.vertcdemo.utils.Utils;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.view.CallActivity;
import com.volcengine.vertcdemo.videocall.call.CallEngine;
import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.databinding.DialogVideoCallTriggerCallBinding;
import com.volcengine.vertcdemo.videocall.util.Constant;
import com.volcengine.vertcdemo.videocall.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 通话触发对话框
 */
public class CallTriggerDialog extends BottomSheetDialogFragment {
    private static final String FRAGMENT_TAG = "CallTriggerDialog";
    private static final int REQUEST_PERMISSION_CODE_VOICE = 1001;
    private static final int REQUEST_PERMISSION_CODE_VIDEO = 1002;
    private final String mCallerUid;
    private final String mCalleeUid;
    private final String mCalleeName;

    public static void showDialog(@NonNull FragmentManager fragmentManager, String calleeUid, String calleeName) {
        CallTriggerDialog dialog = new CallTriggerDialog(calleeUid, calleeName);
        dialog.show(fragmentManager, FRAGMENT_TAG);
    }

    public CallTriggerDialog(String callee, String calleeName) {
        mCallerUid = SolutionDataManager.ins().getUserId();
        mCalleeUid = callee;
        mCalleeName = calleeName;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, com.volcengine.vertcdemo.core.R.style.SolutionCommonDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogVideoCallTriggerCallBinding binding = DialogVideoCallTriggerCallBinding.inflate(inflater);
        Drawable videoDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_video_call);
        if (videoDrawable != null) {
            videoDrawable.setBounds(0, 0, (int) Utils.dp2Px(16), (int) Utils.dp2Px(16));
            binding.videoCall.setCompoundDrawablePadding((int) Utils.dp2Px(4));
            binding.videoCall.setCompoundDrawables(videoDrawable, null, null, null);
        }
        binding.videoCallFl.setOnClickListener(DebounceClickListener.create(v -> {
            List<String> permissions = new ArrayList<>();
            //是否有录音权限
            if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            //是否有摄像头权限
            if (!hasPermission(Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            int size = permissions.size();
            if (size == 0) {
                triggerCall(CallType.VIDEO);
            } else {
                //申请缺少的权限
                requestPermissions(permissions.toArray(permissions.toArray(new String[size])), REQUEST_PERMISSION_CODE_VIDEO);
            }
        }));

        Drawable voiceDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_voice_call);
        if (voiceDrawable != null) {
            voiceDrawable.setBounds(0, 0, (int) Utils.dp2Px(16), (int) Utils.dp2Px(16));
            binding.videoCall.setCompoundDrawablePadding((int) Utils.dp2Px(6));
            binding.voiceCall.setCompoundDrawables(voiceDrawable, null, null, null);
        }

        binding.voiceCallFl.setOnClickListener(DebounceClickListener.create(v -> {
                    if (hasPermission(Manifest.permission.RECORD_AUDIO)) {
                        triggerCall(CallType.VOICE);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE_VOICE);
                    }
                })
        );

        binding.cancelButton.setOnClickListener(v -> dismiss());
        return binding.getRoot();
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(AppUtil.getApplicationContext(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE_VOICE
                || requestCode == REQUEST_PERMISSION_CODE_VIDEO) {
            CallType callType = requestCode == REQUEST_PERMISSION_CODE_VOICE ? CallType.VOICE : CallType.VIDEO;
            if (hasPermission(Manifest.permission.RECORD_AUDIO)) {
                triggerCall(callType);
            } else {
                String mic = getString(R.string.microphone);
                SolutionToast.show(getString(R.string.reject_permission, mic));
                dismiss();
            }
        }
    }

    private boolean mDialing;

    /**
     * 触发呼叫
     *
     * @param callType 通话类型
     */
    private void triggerCall(CallType callType) {
        if (TextUtils.equals(mCalleeUid, mCallerUid)) {
            SolutionToast.show(R.string.call_self_tip);
            return;
        }
        if (mDialing) {
            return;
        }
        mDialing = true;
        CallEngine.getInstance().dial(mCalleeUid, callType, result -> {
            dismiss();
            if (!result.success) {
                if (result.result instanceof String
                        && ((String) result.result).contains(String.valueOf(Constant.ERROR_CODE_813))) {
                    SolutionToast.show(Util.getString(R.string.peer_is_busy));
                    return;
                }
                SolutionToast.show((String) result.result);
            } else {
                CallActivity.start(callType, mCallerUid, mCalleeUid, mCalleeName);
            }
            mDialing = false;
        });
    }

}
