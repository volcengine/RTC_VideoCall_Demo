package com.volcengine.vertcdemo.videocall.call;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.volcengine.vertcdemo.common.SolutionCommonDialog;
import com.volcengine.vertcdemo.utils.ActivityDataManager;
import com.volcengine.vertcdemo.utils.DebounceClickListener;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.call.view.CallActivity;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.model.VoipInform;

import java.util.List;

public class CallStateHelper {

    /**
     * 收到呼叫
     */
    public static void handleRingState(Activity context, VoipInfo voipInfo, String bid) {
        CallEngine callEngine = CallEngine.getInstance();
        boolean hasMicPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
        if (!hasMicPermission) {//收到被呼叫，没有麦克风权限弹窗提示并挂断
            SolutionCommonDialog hintDialog = new SolutionCommonDialog(context);
            hintDialog.setMessage(context.getString(R.string.mic_permission_hint));
            hintDialog.setCancelable(false);
            hintDialog.setPositiveListener(DebounceClickListener.create(v -> hintDialog.dismiss()));
            hintDialog.show();
            callEngine.hangup(null);
            return;
        }
        //呼叫引擎从未初始化过，则先初始化
        if (!callEngine.isInited()) {
            callEngine.init(voipInfo.rtcAppId, bid);
            VoipInform inform = new VoipInform();
            inform.eventCode = VoipInform.EVENT_CODE_CREATE_ROOM;
            inform.voipInfo = voipInfo;
            callEngine.onReceiveCallEvent(inform);
        }
        CallActivity.start(voipInfo.getCallType(), voipInfo.callerUid, voipInfo.calleeUid, voipInfo.callerUname);
    }

    /**
     * 进入其他场景时检测是否仍在1V1通话，如果存在需要先关闭并销毁RTC后，才能进入其他场景
     *
     * @param host           提示弹窗宿主Activity
     * @param startSceneTask 开启其他场景的任务
     */
    public static void checkCallStateWhenEnterScene(Activity host, Runnable startSceneTask) {
        checkCallState(host, startSceneTask, true);
    }

    /**
     * 发起新的通话时检测是否已经存在通话，如果存在需要先结束上次通话，不需要销毁RTC
     *
     * @param host     提示弹窗宿主Activity
     * @param dialTask 发起通话任务
     */
    public static void checkCallStateWhenDial(Activity host, Runnable dialTask) {
        checkCallState(host, dialTask, false);
    }

    private static void checkCallState(Activity host, Runnable startSceneTask, boolean destroyRTC) {
        VoipState state = CallEngine.getInstance().getCurVoipState();
        boolean calling = state != null && state != VoipState.IDLE;
        //虽然没有在通话流程中，但是有进入过此场景，引擎已经初始化过，如果是进入其他场景则需要销毁，避免出现多引擎
        if (!calling) {
            if (destroyRTC) {
                CallEngine.getInstance().destroy();
            }
            startSceneTask.run();
            return;
        }
        SolutionCommonDialog dialog = new SolutionCommonDialog(host);
        VoipInfo voipInfo = CallEngine.getInstance().getVoipInfo();
        CallType callType = voipInfo != null ? voipInfo.getCallType() : CallType.VOICE;
        String type = host.getString(callType == CallType.VIDEO ? R.string.video : R.string.voice);
        String hint = host.getString(R.string.terminate_call_hint, type);
        dialog.setMessage(hint);
        dialog.setPositiveListener(v -> {
            CallEngine callEngine = CallEngine.getInstance();
            callEngine.hangup(destroyRTC ? (result -> callEngine.destroy()) : null);
            startSceneTask.run();
            dialog.dismiss();
        });
        dialog.setNegativeListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * 是否存在视频呼叫场景其他页面
     *
     * @param excludeActivity 需要排除的页面类全名
     */
    public static boolean existOtherVideoCallActivity(String excludeActivity) {
        List<Activity> activities = ActivityDataManager.getInstance().getActivities();
        for (Activity item : activities) {
            String canonicalName = item == null ? null : item.getClass().getCanonicalName();
            if (!TextUtils.isEmpty(excludeActivity) && TextUtils.equals(canonicalName, excludeActivity)) {
                continue;
            }
            if (canonicalName != null && canonicalName.contains("com.volcengine.vertcdemo.videocall")) {
                return true;
            }
        }
        return false;
    }
}
