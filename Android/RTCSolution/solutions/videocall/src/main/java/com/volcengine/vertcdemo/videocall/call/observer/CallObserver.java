package com.volcengine.vertcdemo.videocall.call.observer;

import com.ss.bytertc.engine.data.AudioRoute;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.data.VideoFrameInfo;
import com.volcengine.vertcdemo.videocall.call.state.CallStateMachine;

import java.util.HashMap;

/**
 * 播放相关状态变化监听, 整合了RTC相关回调和通话状态相关变化
 */
public interface CallObserver extends CallStateMachine.CallStateObserver {

    /**
     * 远端用户进房
     *
     * @param userId 进入用户的id
     */
    void onUserJoined(String userId);

    /**
     * 远端用户首帧解码
     *
     * @param roomId 房间id
     * @param userId 用户id
     */
    void onFirstRemoteVideoFrameDecoded(String roomId, String userId);

    /**
     * 完成第一帧视频帧或屏幕视频帧采集时，收到此回调
     */
    void onFirstLocalVideoFrameCaptured();

    /**
     * 本地/远端用户开关麦克风
     *
     * @param userId 用户id
     * @param on     true 为打开，false 为关闭
     */
    void onUserToggleMic(String userId, boolean on);

    /**
     * 本地/远端用户开关摄像头
     *
     * @param userId 用户id
     * @param on     true 为打开，false 为关闭
     */
    void onUserToggleCamera(String userId, boolean on);

    /**
     * 音频路由发生变化
     *
     * @param route 路由
     */
    void onAudioRouteChanged(AudioRoute route);

    /**
     * 更新通话时长
     *
     * @param callDuration 当前通话时长，单位为秒
     */
    void onUpdateCallDuration(int callDuration);

    /**
     * 通话双方网络质量变化
     *
     * @param blocked userId和是否卡顿Boolean值的映射
     */
    void onUserNetQualityChange(HashMap<String, Boolean> blocked);
}
