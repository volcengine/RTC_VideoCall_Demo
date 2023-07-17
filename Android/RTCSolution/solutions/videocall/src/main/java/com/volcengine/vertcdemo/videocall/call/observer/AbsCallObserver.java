package com.volcengine.vertcdemo.videocall.call.observer;

import com.ss.bytertc.engine.data.AudioRoute;

import java.util.HashMap;

/**
 * 播放相关状态变化监听, 默认实现了RTC相关回调，子类需要实现通话状态变化回调
 */
public abstract class AbsCallObserver implements CallObserver {

    /**
     * 远端用户进房
     *
     * @param userId 进入用户的id
     */
    public void onUserJoined(String userId) {
    }

    /**
     * 远端用户首帧解码
     *
     * @param roomId 房间id
     * @param userId 用户id
     */
    public void onFirstRemoteVideoFrameDecoded(String roomId, String userId) {
    }

    /**
     * 完成第一帧视频帧或屏幕视频帧采集时，收到此回调
     */
    public void onFirstLocalVideoFrameCaptured() {

    }

    /**
     * 本地/远端用户开关麦克风
     *
     * @param userId 用户id
     * @param on     true 为打开，false 为关闭
     */
    public void onUserToggleMic(String userId, boolean on) {
    }

    /**
     * 本地/远端用户开关摄像头
     *
     * @param userId 用户id
     * @param on     true 为打开，false 为关闭
     */
    public void onUserToggleCamera(String userId, boolean on) {
    }

    /**
     * 音频路由发生变化
     *
     * @param route 路由
     */
    public void onAudioRouteChanged(AudioRoute route) {
    }

    /**
     * 通话时长更新
     *
     * @param callDuration 当前通话时长，单位为秒
     */
    public void onUpdateCallDuration(int callDuration) {
    }

    /**
     * 通话双方网络质量变化
     *
     * @param blocked userId和是否卡顿Boolean值的映射
     */
    public void onUserNetQualityChange(HashMap<String, Boolean> blocked) {
    }
}
