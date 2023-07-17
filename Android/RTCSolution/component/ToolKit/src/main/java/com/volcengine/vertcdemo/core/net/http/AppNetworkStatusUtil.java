// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.core.net.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;

/**
 * {zh}
 * 应用网络状态判断
 * <p>
 * 参考: https://developer.android.com/reference/android/net/NetworkRequest
 * <p>
 * 需要在 AndroidManifest.xml 申请权限：
 * <uses-permission android:name="android.permission.INTERNET" />
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 */

/**
 * {en}
 * Application network status judgment
 * <p>
 * Reference: https://developer.android.com/reference/android/net/NetworkRequest
 * <p>
 * Need to apply for permissions in AndroidManifest.xml:
 * <uses-permission android:name="android.permission.INTERNET" />
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 */
public class AppNetworkStatusUtil {
    private static Context sContext;

    private static final String TAG = "AppNetworkStatus";

    private static final NetworkCallback sCallback = new NetworkCallback() {

        /** {zh}
         * 网络连接成功
         */
        /** {en}
         * Network connection successful
         */
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            notifyNetStatus();
        }

        /** {zh}
         * 网络已断开连接
         */
        /** {en}
         * Network disconnected
         */
        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            notifyNetStatus();
        }

        /** {zh}
         * 网络连接超时或网络不可达
         */
        /** {en}
         * The network connection timed out or the network is unreachable
         */
        @Override
        public void onUnavailable() {
            super.onUnavailable();
            notifyNetStatus();
        }
    };

    private static void notifyNetStatus() {
        if (isConnected(sContext)) {
            Log.e(TAG, "notifyNetStatus isConnected");
            SolutionDemoEventManager.post(new AppNetworkStatusEvent(AppNetworkStatusEvent.NETWORK_STATUS_CONNECTED));
        } else {
            Log.e(TAG, "notifyNetStatus disConnected");
            SolutionDemoEventManager.post(new AppNetworkStatusEvent(AppNetworkStatusEvent.NETWORK_STATUS_DISCONNECTED));
        }
    }

    /** {zh}
     * 注册网络状态监听
     * @param context 上下文对象，为空则注册失败
     */
    /**
     * {en}
     * Register network status monitoring
     *
     * @param context context object, if it is empty, the registration will fail
     */
    public static void registerNetworkCallback(@Nullable Context context) {
        if (context == null) {
            Log.e(TAG, "registerNetworkCallback failed, because app context is null");
            return;
        }
        sContext = context.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Log.e(TAG, "registerNetworkCallback invoke");
            NetworkRequest.Builder builder = new NetworkRequest.Builder();

            NetworkRequest request = builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .build();

            connectivityManager.registerNetworkCallback(request, sCallback);
        } else {
            Log.e(TAG, "registerNetworkCallback failed, because ConnectivityManager is null");
        }
    }

    /** {zh}
     * 取消注册网络状态监听
     * @param context 上下文对象，为空则取消注册失败
     */
    /**
     * {en}
     * Unregister network status monitoring
     *
     * @param context context object, if it is empty, unregistration fails
     */
    public static void unregisterNetworkCallback(@Nullable Context context) {
        if (context == null) {
            Log.e(TAG, "unregisterNetworkCallback failed, because app context is null");
            return;
        }
        Context appContext = context.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Log.e(TAG, "unregisterNetworkCallback invoke");

            connectivityManager.unregisterNetworkCallback(sCallback);
        } else {
            Log.e(TAG, "unregisterNetworkCallback failed, because ConnectivityManager is null");
        }
    }

    /** {zh}
     * 网络是否连接
     * @param context 上下文对象
     * @return true: 处于连接状态
     */
    /**
     * {en}
     * Whether the network is connected
     *
     * @param context context object
     * @return true: connected
     */
    public static boolean isConnected(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
