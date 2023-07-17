package com.volcengine.vertcdemo.im;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vertcdemo.joinrtsparams.bean.ImChannelConfig;

/**
 * 全局通信通道接口
 */
public interface IIM {

    void init(@NonNull Context application,
              @NonNull String userId,
              @NonNull String loginToken,
              @Nullable InitCallback initCallback);

    /**
     * 是否已经初始化成功
     */
    boolean isInitSuccess();

    /**
     * 注册消息发送接收器
     */
    void registerMessageReceiver(@NonNull String key, @NonNull IMRTSClient receiver);

    /**
     * 通过消息接收器关联的key移除消息接收发送器
     */
    void unregisterMessageReceiver(@NonNull String key);

    /**
     * 添加初始化成功监听
     */
    void addInitCallback(InitCallback callback);

    /**
     * 移除初始化成功监听
     */
    void removeInitCallback(InitCallback callback);

    /**
     * 通过消息接收器关联的key获取消息发送接收器
     */
    IMRTSClient getRTSClient(@NonNull String key);

    /**
     * 获取全局信道下发的配置
     */
    ImChannelConfig getConfig();

    /**
     * 向业务服务器发送消息
     *
     * @param message 消息内容
     * @return 消息id
     */
    long sendServerMessage(@NonNull String message);

    interface InitCallback {
        void onSuccess();

        void onFailed(int errorCode, @Nullable String errorMessage);
    }

}
