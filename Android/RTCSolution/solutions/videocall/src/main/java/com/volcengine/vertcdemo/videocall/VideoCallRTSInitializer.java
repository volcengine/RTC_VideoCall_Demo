package com.volcengine.vertcdemo.videocall;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.startup.Initializer;

import com.ss.bytertc.base.utils.NetworkUtils;
import com.volcengine.vertcdemo.common.SPUtils;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.im.IIM;
import com.volcengine.vertcdemo.im.IMService;
import com.volcengine.vertcdemo.utils.ActivityDataManager;
import com.volcengine.vertcdemo.videocall.call.CallStateHelper;
import com.volcengine.vertcdemo.videocall.call.CallType;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.call.view.CallActivity;
import com.volcengine.vertcdemo.videocall.model.VoipInform;
import com.volcengine.vertcdemo.videocall.util.Callback;
import com.volcengine.vertcdemo.videocall.util.Constant;
import com.volcengine.vertcdemo.videocall.util.Util;

import java.util.Collections;
import java.util.List;

public class VideoCallRTSInitializer implements Initializer {
    private Context mContext;
    private VideoCallRTSClient mGlobalRTSClient;
    private final Callback callback = result -> {
        VoipInform inform = result.result instanceof VoipInform
                ? (VoipInform) result.result
                : null;
        if (inform == null || inform.voipInfo == null) {
            return;
        }
        if (inform.eventCode == VoipInform.EVENT_CODE_CREATE_ROOM) {
            ActivityDataManager activityData = ActivityDataManager.getInstance();
            Activity topActivity = activityData.getTopActivity();
            //App不在前台时不处理
            if (!activityData.isForeground() || topActivity == null) {
                return;
            }
            //App在场景选择页面进入呼叫流程
            String topActivityName = topActivity.getClass().getCanonicalName();
            if (activityData.getActivities().size() == 1 &&
                    TextUtils.equals(topActivityName, "com.volcengine.vertcdemo.MainActivity")) {
                CallStateHelper.handleRingState(topActivity, inform.voipInfo, null);
                return;
            }
            //App处于其他场景页面时自动挂断
            if (!topActivityName.contains("com.volcengine.vertcdemo.videocall")) {
                SolutionToast.show(Util.getString(R.string.received_video_call));
            }
        }
    };

    private final IIM.InitCallback mInitCallback = new IIM.InitCallback() {
        @Override
        public void onSuccess() {
            //全局信道建立成功后为防止上次呼叫没有被取消，对端一直处于呼叫中的状态
            if (NetworkUtils.isNetworkAvailable(mContext)) {
                mGlobalRTSClient.clearUser();
                IMService.getService().removeInitCallback(mInitCallback);
            }
        }

        @Override
        public void onFailed(int errorCode, @Nullable String errorMessage) {
            //ignore
        }
    };

    @NonNull
    @Override
    public VideoCallRTSClient create(@NonNull Context context) {
        mContext = context;
        mGlobalRTSClient = new VideoCallRTSClient();
        mGlobalRTSClient.addEventObserver(callback);
        IMService.getService().addInitCallback(mInitCallback);
        IMService.getService().registerMessageReceiver(Constant.IM_CLIENT_KEY_GLOBAL, mGlobalRTSClient);
        return mGlobalRTSClient;
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.EMPTY_LIST;
    }
}
