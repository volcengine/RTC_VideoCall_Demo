// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.im;

import static com.volcengine.vertcdemo.core.net.http.AppNetworkStatusEvent.NETWORK_STATUS_CONNECTED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ss.bytertc.base.utils.NetworkUtils;
import com.ss.bytertc.rts.engine.RTS;
import com.ss.bytertc.rts.engine.handler.IRTSEventHandler;
import com.ss.bytertc.rts.engine.type.LoginErrorCode;
import com.vertcdemo.joinrtsparams.bean.ImChannelConfig;
import com.vertcdemo.joinrtsparams.common.JoinRTSManager;
import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.AppTokenExpiredEvent;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.http.AppNetworkStatusEvent;
import com.volcengine.vertcdemo.utils.WeakHandler;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("unused")
@Keep
public class IMService implements IIM {

    private static final String TAG = "IMService";
    public static final int ERROR_CODE_UNKNOWN = -1;

    private static class Holder {
        @SuppressLint("StaticFieldLeak")
        private static final IMService service = new IMService();
    }

    public IMService() {
        SolutionDemoEventManager.register(this);
    }

    public static IIM getService() {
        return Holder.service;
    }

    @Override
    public void registerMessageReceiver(@NonNull String key, @NonNull IMRTSClient messageReceiver) {
        updateMessageReceiver(() -> {
            synchronized (mMessageReceivers) {
                mMessageReceivers.put(key, messageReceiver);
            }
        });
    }

    @Override
    public void unregisterMessageReceiver(@NonNull String key) {
        updateMessageReceiver(() -> {
            synchronized (mMessageReceivers) {
                mMessageReceivers.remove(key);
            }
        });
    }

    private void updateMessageReceiver(Runnable runnable) {
        if (mDispatching) {
            AppExecutors.mainThread().execute(runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public void addInitCallback(InitCallback callback) {
        if (callback == null) {
            return;
        }
        updateInitCallback(() -> {
            synchronized (mInitCallbacks) {
                mInitCallbacks.add(callback);
            }
        });
    }

    @Override
    public void removeInitCallback(InitCallback callback) {
        if (callback == null) {
            return;
        }
        updateInitCallback(() -> {
            synchronized (mInitCallbacks) {
                mInitCallbacks.remove(callback);
            }
        });
    }

    private void updateInitCallback(Runnable runnable) {
        if (mInitResultNotifying) {
            AppExecutors.mainThread().execute(runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public IMRTSClient getRTSClient(@NonNull String key) {
        return mMessageReceivers.get(key);
    }

    @Override
    public ImChannelConfig getConfig() {
        return mConfig;
    }

    @Override
    public long sendServerMessage(@NonNull String message) {
        if (mRTS == null || !mInitSuccess) {
            Log.d(TAG, "IMService sendServerMessage mRTS:" + mRTS + ",mInitSuccess:" + mInitSuccess);
            return -1;
        }
        return mRTS.sendServerMessage(message);
    }

    private final Object mInitDataLock = new Object();
    private boolean mInitializing;
    private boolean mInitSuccess;
    private Context mContext;
    private String mUserId;
    private String mLoginToken;
    private RTS mRTS;
    private ImChannelConfig mConfig;
    private final Collection<InitCallback> mInitCallbacks = new HashSet<>(2);
    private final HashMap<String, IMRTSClient> mMessageReceivers = new HashMap<>();
    private boolean mDispatching;
    private boolean mInitResultNotifying;

    private final IRTSEventHandler mRTSEventHandler = new IRTSEventHandler() {

        @Override
        public void onLoginResult(String uid, int error_code, int elapsed) {
            Holder.service.onLoginResult(uid, error_code);
        }


        @Override
        public void onServerParamsSetResult(int error) {
            Holder.service.onServerParamsSetResult(error);
        }

        @Override
        public void onServerMessageSendResult(long msgId, int error, ByteBuffer message) {
            Holder.service.onServerMessageSendResult(msgId, error, message);
        }

        @Override
        public void onMessageReceived(String uid, String message) {
            Holder.service.onMessageReceived(uid, message);
        }

    };

    private static final int WHAT_CONNECT_RTS = 30001;
    private final WeakHandler.IHandler mReInitHandler = msg -> {
        if (mInitSuccess) return;
        if (msg.what == WHAT_CONNECT_RTS) {
            init(mContext, mUserId, mLoginToken, null);
        }
    };
    private final WeakHandler mReInitializer = new WeakHandler(mReInitHandler);

    @Override
    public boolean isInitSuccess() {
        return mInitSuccess;
    }

    @Override
    public void init(@NonNull Context application,
                     @NonNull String userId,
                     @NonNull String loginToken,
                     @Nullable InitCallback initCallback) {
        if (!NetworkUtils.isNetworkAvailable(application)) {
            if (initCallback != null) {
                initCallback.onFailed(ERROR_CODE_UNKNOWN, application.getString(R.string.network_link_down));
            }
            return;
        }
        synchronized (mInitDataLock) {
            if (mInitializing) {
                addInitCallback(initCallback);
                return;
            }
            if (mInitSuccess) {
                if (initCallback != null) {
                    initCallback.onSuccess();
                }
                return;
            }
            mInitializing = true;
        }
        mContext = application;
        mUserId = userId;
        mLoginToken = loginToken;
        addInitCallback(initCallback);
        JoinRTSManager.getIMChannelInfo(loginToken, new IRequestCallback<ImChannelConfig>() {
            @Override
            public void onSuccess(ImChannelConfig data) {
                mConfig = data;
                connectRTS(application);
            }

            @Override
            public void onError(int errorCode, String message) {
                String errorMsg = "join rts failed errorCode:" + errorCode + ",message:" + message;
                handleInitFailed(errorCode, message);
            }
        });
    }

    private void connectRTS(@NonNull Context application) {
        JSONObject param = new JSONObject();
        try {
            param.put("rtc.device_id", SolutionDataManager.ins().getDeviceId());
        } catch (JSONException e) {
            //ignore
        }
        mRTS = RTS.createRTS(application, mConfig.rtsAppId, mRTSEventHandler, param);
        if (mRTS != null) {
            long invokeResult = mRTS.login(mConfig.rtsToken, mConfig.imUserId);
            if (invokeResult != 0) {
                String errorMsg = "rts login failed, invoke 'login' but return " + invokeResult;
                handleInitFailed(ERROR_CODE_UNKNOWN, errorMsg);
            }
        } else {
            String errorMsg = "create rts failed,invoke 'createRTS' but return null";
            handleInitFailed(ERROR_CODE_UNKNOWN, errorMsg);
        }
    }

    public void disConnectRTS() {
        mRTS.logout();
        synchronized (mInitDataLock) {
            mInitializing = false;
            mInitSuccess = false;
        }
    }

    private void onLoginResult(String uid, int code) {
        if (code == LoginErrorCode.LOGIN_ERROR_CODE_SUCCESS) {
            mRTS.setServerParams(mConfig.serverSignature, mConfig.serverUrl);
        } else {
            handleInitFailed(code, "rts login failed");
        }
    }

    private void onServerParamsSetResult(int error) {
        if (error != 200) {
            handleInitFailed(error, "rts set server params failed");
            return;
        }
        handleInitSuccess();
    }

    public void onServerMessageSendResult(long msgId, int error, ByteBuffer message) {
        synchronized (mMessageReceivers) {
            mDispatching = true;
            for (IMRTSClient client : mMessageReceivers.values()) {
                client.onServerMessageSendResult(msgId, error, message);
            }
            mDispatching = false;
        }
    }

    private void onMessageReceived(String uid, String message) {
        synchronized (mMessageReceivers) {
            mDispatching = true;
            for (IMRTSClient client : mMessageReceivers.values()) {
                client.onReceivedMessage(mUserId, message);
            }
            mDispatching = false;
        }
    }

    private void handleInitFailed(int errorCode, String errorMessage) {
        synchronized (mInitDataLock) {
            mInitializing = false;
        }
        mReInitializer.sendEmptyMessageDelayed(WHAT_CONNECT_RTS, 500);
        notifyInitFailed(errorCode, errorMessage);
    }

    private void notifyInitFailed(int errorCode, String errorMessage) {
        if (mInitCallbacks.isEmpty()) {
            return;
        }
        synchronized (mInitCallbacks) {
            mInitResultNotifying = true;
            for (InitCallback cb : mInitCallbacks) {
                Log.d(TAG, "IMService notifyInitFailed errorCode:" + errorCode
                        + ",errorMessage:" + errorMessage
                        + ",cb:" + cb);
                cb.onFailed(errorCode, errorMessage);
            }
            mInitResultNotifying = false;
        }
    }

    private void handleInitSuccess() {
        synchronized (mInitDataLock) {
            mInitSuccess = true;
            mInitializing = false;
        }
        notifyInitSuccess();
    }

    private void notifyInitSuccess() {
        if (mInitCallbacks.isEmpty()) {
            return;
        }
        synchronized (mInitCallbacks) {
            mInitResultNotifying = true;
            for (InitCallback cb : mInitCallbacks) {
                Log.d(TAG, "IMService notifyInitSuccess cb:" + cb);
                cb.onSuccess();
            }
            mInitResultNotifying = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkStatusChanged(AppNetworkStatusEvent event) {
        if (event.status == NETWORK_STATUS_CONNECTED && !mInitSuccess) {
            init(mContext, mUserId, mLoginToken, null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpiredEvent(AppTokenExpiredEvent event) {
        disConnectRTS();
    }

}
