// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "VideoCallRTSManager.h"
@class VideoCallRTCManager;

NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallRTCManagerDelegate <NSObject>

/**
 * @brief 远端用户麦克风状态发生改变
 * @param manager VideoCallRTCManager 模型
 * @param micOn 麦克风状态
 * @param userID 用户ID
 */
- (void)videoCallRTCManager:(VideoCallRTCManager *)manager
            onUserToggleMic:(BOOL)micOn
                     userID:(NSString *)userID;

/**
 * @brief 远端用户摄像头状态发生改变
 * @param manager VideoCallRTCManager 模型
 * @param cameraOn 摄像头状态
 * @param userID 用户ID
 */
- (void)videoCallRTCManager:(VideoCallRTCManager *)manager
         onUserToggleCamera:(BOOL)cameraOn
                     userID:(NSString *)userID;

/**
 * @brief 用户网络质量变化回调
 * @param manager VideoCallRTCManager 模型
 * @param message 网络状态改变提示语
 * @param isVeryBad 本端网络状态非常不好
 */
- (void)videoCallRTCManager:(VideoCallRTCManager *)manager
           onNetworkQualityChanged:(NSString *)message
              qualityVeryBad:(BOOL)isVeryBad;

/**
 * @brief 远端用户加入房间回调
 * @param manager VideoCallRTCManager 模型
 * @param userID 用户ID
 */
- (void)videoCallRTCManager:(VideoCallRTCManager *)manager
               onUserJoined:(NSString *)userID;

/**
 * @brief 远端用户离开房间回调
 * @param manager VideoCallRTCManager 模型
 * @param userID 用户ID
 */
- (void)videoCallRTCManager:(VideoCallRTCManager *)manager
               onUserLeaved:(NSString *)userID;

/**
 * @brief 音频路由改变回调
 * @param manager VideoCallRTCManager 模型
 * @param device 当前音频路由
 */
- (void)videoCallRTCManager:(VideoCallRTCManager *)manager
        onAudioRouteChanged:(ByteRTCAudioRoute)device;

@end

@interface VideoCallRTCManager : NSObject

@property (nonatomic, weak) id<VideoCallRTCManagerDelegate> delegate;
// RTC 引擎
@property (nonatomic, strong, nullable) ByteRTCVideo *rtcEngineKit;

+ (VideoCallRTCManager *_Nullable)shareRtc;

/**
 * @brief 创建RTC引擎
 * @param appId RTC AppId
 * @param bid BusinessId
 */
- (void)createRTCVideo:(NSString *)appId bid:(NSString *)bid;

/**
 * @brief 销毁RTC引擎
 */
- (void)destroyRTCVideo;

/**
 * @brief 开关本地视频采集
 * @param enable ture：开启视频采集 false：关闭视频采集
 */
- (void)enableLocalVideo:(BOOL)enable;

/**
 * @brief 获取本地视频采集状态
 */
- (BOOL)currentLocalVideoEnable;

/**
 * @brief 开关本地音频采集
 * @param enable ture：开启音频采集 false：关闭音频采集
 */
- (void)enableLocalAudio:(BOOL)enable;

/**
 * @brief 获取本地音频采集状态
 */
- (BOOL)currentLocalAudioEnable;

/**
 * @brief 设置本地用户渲染视图
 * @param view View
 */
- (void)startRenderLocalVideo:(UIView *)view;

/**
 * @brief 设置远端用户渲染视图
 * @param view View
 * @param userID User ID
 */
- (void)startRenderRemoteVideo:(UIView *)view
                        userID:(NSString *)userID;

/**
 * @brief 加入RTC房间
 * @param token Token
 * @param roomID Room ID
 * @param uid User ID
 * @param complete Callback
 */
- (void)joinRoomWithToken:(NSString *)token
                   roomID:(NSString *)roomID
                      uid:(NSString *)uid
                 complete:(void(^)(BOOL success))complete;

/**
 * @brief 开始发布音视频流
 */
- (void)startPublishStream;

/**
 * @brief 切换前置/后置相机
 */
- (void)switchCamera;

/**
 * @brief 设置摄像头类型
 * @param cameraID 摄像头类型
 */
- (void)setCameraID:(ByteRTCCameraID)cameraID;

/**
 * @brief 设置音频路由
 * @param route ByteRTCAudioRoute 对象
 */
- (void)setDeviceAudioRoute:(ByteRTCAudioRoute)route;

/**
 * @brief 获取当前音频路由
 */
- (ByteRTCAudioRoute)currentDeviceAudioRoute;

/**
 * @brief 离开 RTC 房间
 */
- (void)leaveRoom;

/**
 * @brief 开启文件混音
 */
- (void)startAudioMixing;

/**
 * @brief 关闭文件混音
 */
- (void)stopAudioMixing;

/**
 * @brief 开启PIP模式
 */
- (void)openPIPMode;

/**
 * @brief 自定义视频采集渲染
 * @param streamKey 远端流信息
 * @param delegate 自定义视频渲染器, 如果需要解除绑定，必须将 delegate 设置为 null
 */
- (void)setRemoteVideoSink:(ByteRTCRemoteStreamKey* _Nonnull)streamKey
                  delegate:(id<ByteRTCVideoSinkDelegate> _Nullable)delegate;

/**
 * @brief 获取网络状态是否可用
 */
- (BOOL)networkAvailable;

@end

NS_ASSUME_NONNULL_END
