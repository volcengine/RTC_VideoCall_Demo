package com.volcengine.vertcdemo.videocall.call;

import static com.volcengine.vertcdemo.utils.FileUtils.copyAssetFile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.core.content.ContextCompat;

import com.ss.bytertc.engine.RTCRoom;
import com.ss.bytertc.engine.RTCRoomConfig;
import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.VideoCanvas;
import com.ss.bytertc.engine.VideoEncoderConfig;
import com.ss.bytertc.engine.audio.IAudioMixingManager;
import com.ss.bytertc.engine.data.AudioMixingConfig;
import com.ss.bytertc.engine.data.AudioMixingType;
import com.ss.bytertc.engine.data.AudioRoute;
import com.ss.bytertc.engine.data.CameraId;
import com.ss.bytertc.engine.data.MirrorType;
import com.ss.bytertc.engine.data.RemoteStreamKey;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.data.VideoOrientation;
import com.ss.bytertc.engine.handler.IRTCRoomEventHandler;
import com.ss.bytertc.engine.type.AudioProfileType;
import com.ss.bytertc.engine.type.AudioScenarioType;
import com.ss.bytertc.engine.type.ChannelProfile;
import com.ss.bytertc.engine.type.MediaStreamType;
import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.videocall.call.observer.CallObservers;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class RTCController {
    /***摄像头ID*/
    private CameraId mCameraId = CameraId.CAMERA_ID_FRONT;
    /****用户是否关闭麦克风**/
    private boolean mClosedMic;
    /****用户是否关闭摄像头: 如果没有授予**/
    private boolean mClosedCamera;

    private RTCRoom mRTCRoom;
    private final CallObservers mObserver;
    private final RTCVideo mRTCVideo;
    private final IRTCRoomEventHandler mRoomEventHandler;

    public RTCController(RTCVideo rtcVideo,
                         CallObservers observer,
                         IRTCRoomEventHandler roomEventHandler) {
        mRTCVideo = rtcVideo;
        mObserver = observer;
        mRoomEventHandler = roomEventHandler;
    }

    /**
     * 设置业务id
     *
     * @param bizId 业务id
     */
    public void setBid(String bizId) {
        if (mRTCVideo == null) {
            return;
        }
        //设置业务id
        mRTCVideo.setBusinessId(bizId);
    }

    /***是否已经设置视频相关参数*/
    private boolean mSetVideoConfig;

    /***开启视频采集*/
    public void startVideoCapture() {
        if (mRTCVideo == null || mClosedCamera) {
            return;
        }
        if (!mSetVideoConfig) {
            //采集视频：分辨率 720*1280，帧率 15fps，码流大小自适应 -1
            VideoEncoderConfig config = new VideoEncoderConfig(720, 1280, 15, -1, 0);
            mRTCVideo.setVideoEncoderConfig(config);
            //设置视频方向
            mRTCVideo.setVideoOrientation(VideoOrientation.Portrait);
            mSetVideoConfig = true;
        }
        //设置镜像效果
        setMirrorType(mCameraId);
        //开启视频采集
        mRTCVideo.startVideoCapture();
    }

    /***关闭视频采集*/
    public void stopVideoCapture() {
        if (mRTCVideo == null) {
            return;
        }
        //关闭视频采集
        mRTCVideo.stopVideoCapture();
    }

    /**
     * 开启视频发布
     */
    public void startVideoPublish() {
        if (mRTCRoom == null || mClosedCamera) {
            return;
        }
        mRTCRoom.publishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_VIDEO);
    }

    /**
     * 关闭视频发布
     */
    public void stopVideoPublish() {
        if (mRTCRoom == null) {
            return;
        }
        mRTCRoom.unpublishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_VIDEO);
    }

    /***开启音频采集*/
    public void startAudioCapture() {
        if (mRTCVideo == null || mClosedMic) {
            return;
        }
        mRTCVideo.startAudioCapture();
    }

    /***关闭音频采集*/
    public void stopAudioCapture() {
        if (mRTCVideo == null) {
            return;
        }
        mRTCVideo.stopAudioCapture();
    }


    /**
     * 开启音频发布
     */
    public void startVoicePublish() {
        if (mRTCRoom == null || mClosedMic) {
            return;
        }
        mRTCRoom.publishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
    }

    /**
     * 设置音频场景
     *
     * @param isMedia true时设置为 AudioScenarioType.AUDIO_SCENARIO_MEDIA，
     *                false 时设置为AudioScenarioType.AUDIO_SCENARIO_HIGHQUALITY_CHAT
     */
    public void setAudioScenario(boolean isMedia) {
        if (mRTCVideo == null) {
            return;
        }
        AudioScenarioType type = isMedia ? AudioScenarioType.AUDIO_SCENARIO_MEDIA
                : AudioScenarioType.AUDIO_SCENARIO_HIGHQUALITY_CHAT;
        mRTCVideo.setAudioScenario(type);
    }

    /**
     * 关闭音频发布
     */
    public void stopVoicePublish() {
        if (mRTCRoom == null) {
            return;
        }
        mRTCRoom.unpublishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
    }

    /***切换前后摄像头*/
    public void switchCamera() {
        if (mRTCVideo == null) {
            return;
        }
        CameraId targetId = mCameraId == CameraId.CAMERA_ID_FRONT
                ? CameraId.CAMERA_ID_BACK
                : CameraId.CAMERA_ID_FRONT;
        mRTCVideo.switchCamera(targetId);
        setMirrorType(targetId);
        mCameraId = targetId;
    }

    /***切换内置听筒和扬声器*/
    public void switchAudioRoute() {
        if (mRTCVideo == null) return;
        AudioRoute targetAudioRoute = getCurAudioRoute() == AudioRoute.AUDIO_ROUTE_SPEAKERPHONE
                ? AudioRoute.AUDIO_ROUTE_EARPIECE
                : AudioRoute.AUDIO_ROUTE_SPEAKERPHONE;
        mRTCVideo.setDefaultAudioRoute(targetAudioRoute);
        notifyAudioChange(targetAudioRoute);
    }

    /**
     * 通知音频路由变化
     */
    public void notifyAudioChange(AudioRoute audioRoute) {
        //刷新UI
        if (mObserver != null) {
            mObserver.onAudioRouteChanged(audioRoute);
        }
    }

    /**
     * 获取当前使用的音频路由
     */
    public AudioRoute getCurAudioRoute() {
        return mRTCVideo.getAudioRoute();
    }

    /**
     * 开关麦克风
     */
    public void toggleMic() {
        mClosedMic = !mClosedMic;
        if (mClosedMic) {
            stopAudioCapture();
            stopVoicePublish();
        } else {
            startAudioCapture();
            startVoicePublish();
        }
        if (mObserver != null) {
            mObserver.onUserToggleMic(SolutionDataManager.ins().getUserId(), !mClosedMic);
        }
    }

    /**
     * 用户是否关闭了麦克风
     */
    public boolean isClosedMic() {
        return mClosedMic;
    }

    /**
     * 用户是否关闭了摄像头，用户没有授予摄像头权限等同于关闭了摄像头
     */
    public boolean isClosedCamera() {
        return mClosedCamera;
    }

    /**
     * 设置关闭摄像头标志位，包括因为权限被关闭
     *
     * @param closed 摄像头是否被关闭
     */
    public void setClosedCamera(boolean closed) {
        mClosedCamera = closed;
        if (mObserver != null) {
            //因为上层业务用的是on(麦克风是否处于开启状态),所以这里传mClosingMic的非值
            mObserver.onUserToggleCamera(SolutionDataManager.ins().getUserId(), !mClosedCamera);
        }
    }

    /**
     * 开关摄像头
     */
    public void toggleCamera() {
        mClosedCamera = !mClosedCamera;
        if (mClosedCamera) {
            stopVideoCapture();
            stopVideoPublish();
        } else {
            startVideoCapture();
            startVideoPublish();
        }
        if (mObserver != null) {
            //因为上层业务用的是on(摄像头是否处于开启状态),所以这里传mClosingCamera的非值
            mObserver.onUserToggleCamera(SolutionDataManager.ins().getUserId(), !mClosedCamera);
        }
    }

    /**
     * 开启本地采集视频渲染
     *
     * @param view 渲染目标视图
     */
    public void startRenderLocalVideo(TextureView view) {
        if (mRTCVideo == null) {
            return;
        }
        VideoCanvas canvas = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN);
        mRTCVideo.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, canvas);
    }

    /**
     * 开启本地采集视频渲染
     *
     * @param view 渲染目标视图
     */
    public void startRenderLocalVideo(SurfaceView view) {
        if (mRTCVideo == null) {
            return;
        }
        VideoCanvas canvas = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN);
        mRTCVideo.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, canvas);
    }

    /**
     * 开启远端用户视频渲染
     *
     * @param view 渲染目标视图
     */
    public void startRenderRemoteVideo(String remoteUserId, String roomId, TextureView view) {
        if (mRTCVideo == null || TextUtils.isEmpty(remoteUserId)) {
            return;
        }
        VideoCanvas canvas = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN);
        mRTCVideo.setRemoteVideoCanvas(new RemoteStreamKey(roomId, remoteUserId, StreamIndex.STREAM_INDEX_MAIN), canvas);
    }

    /**
     * 创建并加入RTC房间
     *
     * @param token  RTC TOKEN
     * @param userId 加入房间用户id
     * @param roomID 房间号
     * @return 进房结果，为0则调用成功
     */
    public int creteAndJoinRTCRoom(String token, String userId, String roomID, boolean autoPublish) {
        mRTCRoom = mRTCVideo.createRTCRoom(roomID);
        if (mRoomEventHandler != null) {
            mRTCRoom.setRTCRoomEventHandler(mRoomEventHandler);
        }
        RTCRoomConfig roomConfig = new RTCRoomConfig(ChannelProfile.CHANNEL_PROFILE_CHAT, autoPublish, true, true);
        return mRTCRoom.joinRoom(token, new UserInfo(userId, null), roomConfig);
    }

    /**
     * 播放铃声
     */
    public void playRing() {
        mixingManager = mRTCVideo == null ? null : mRTCVideo.getAudioMixingManager();
        if (mixingManager == null || mRinging.get()) {
            return;
        }
        setAudioScenario(true);
        String filePathInAssets = "call_receive.mp3";
        String parentPath = AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath() + "/resource/";
        File ringMusicFile = new File(parentPath, filePathInAssets);
        if (ringMusicFile.exists()) {
            startAudioMixing(ringMusicFile);
        } else {
            AppExecutors.networkIO().execute(() -> {
                boolean success = copyAssetFile(AppUtil.getApplicationContext(), filePathInAssets, ringMusicFile.getAbsolutePath());
                if (success) {
                    startAudioMixing(ringMusicFile);
                }
            });
        }
    }

    /***停止铃声*/
    public void stopRing() {
        if (mixingManager != null && mRinging.get()) {
            mixingManager.stopAudioMixing(MIX_ID);
            mRinging.set(false);
        }
    }

    /***终止RTC房间*/
    public void terminateRoom() {
        resetDevices();
        if (mRTCRoom != null) {
            mRTCRoom.unpublishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_BOTH);
            mRTCRoom.leaveRoom();
            mRTCRoom.destroy();
            mRTCRoom = null;
        }
    }

    public void destroy() {
        stopAudioCapture();
        stopVideoCapture();
        stopVoicePublish();
        stopVideoPublish();
        terminateRoom();
        if (mRTCVideo != null) {
            RTCVideo.destroyRTCVideo();
        }
    }

    /**
     * 设置镜像效果
     *
     * @param cameraId 当前使用到的摄像头ID
     */
    private void setMirrorType(CameraId cameraId) {
        if (mRTCVideo == null) {
            return;
        }
        mRTCVideo.setLocalVideoMirrorType(
                cameraId == CameraId.CAMERA_ID_FRONT
                        ? MirrorType.MIRROR_TYPE_RENDER_AND_ENCODER
                        : MirrorType.MIRROR_TYPE_NONE);
    }

    /**
     * 退出房间时将设备相关状态、数据重置
     */
    private void resetDevices() {
        mClosedCamera = false;
        mClosedMic = false;
        if (mCameraId == CameraId.CAMERA_ID_BACK) {
            mRTCVideo.switchCamera(CameraId.CAMERA_ID_FRONT);
            mCameraId = CameraId.CAMERA_ID_FRONT;
        }
        if (getCurAudioRoute() != AudioRoute.AUDIO_ROUTE_SPEAKERPHONE) {
            mRTCVideo.setDefaultAudioRoute(AudioRoute.AUDIO_ROUTE_SPEAKERPHONE);
        }
    }

    private IAudioMixingManager mixingManager;
    private static final int MIX_ID = 19;
    private final AtomicBoolean mRinging = new AtomicBoolean();

    private void startAudioMixing(File ringMusicFile) {
        mixingManager.startAudioMixing(MIX_ID, ringMusicFile.getAbsolutePath(),
                new AudioMixingConfig(AudioMixingType.AUDIO_MIXING_TYPE_PLAYOUT, -1));
        mRinging.set(true);
    }

}
