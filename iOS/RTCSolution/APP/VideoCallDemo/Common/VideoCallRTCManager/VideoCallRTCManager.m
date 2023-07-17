// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallRTCManager.h"

@interface VideoCallRTCManager ()<ByteRTCVideoDelegate, ByteRTCRoomDelegate>

@property (nonatomic, assign) ByteRTCCameraID currnetCameraID;
@property (nonatomic, assign) ByteRTCAudioRoute audioRoute;
@property (nonatomic, assign) ByteRTCAudioRoute currentAudioRoute;
@property (nonatomic, assign) ByteRTCNetworkType networkType;
@property (nonatomic, assign) BOOL localAudioEnable;
@property (nonatomic, assign) BOOL localVideoEnable;

@property (nonatomic, strong) ByteRTCRoom *rtcRoom;

@property (nonatomic, copy) void(^joinRoomBlock)(BOOL success);

@end

@implementation VideoCallRTCManager

+ (VideoCallRTCManager *_Nullable)shareRtc {
    static VideoCallRTCManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[VideoCallRTCManager alloc] init];
    });
    return manager;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        // 默认状态设置
        self.currnetCameraID = ByteRTCCameraIDFront;
        self.audioRoute = ByteRTCAudioRouteEarpiece;
        self.networkType = ByteRTCNetworkTypeUnknown;
    }
    return self;
}

- (void)createRTCVideo:(NSString *)appId bid:(NSString *)bid {
    if (self.rtcEngineKit) {
        [ByteRTCVideo destroyRTCVideo];
        self.rtcEngineKit = nil;
    }
    // 创建 RTC 引擎
    self.rtcEngineKit = [ByteRTCVideo createRTCVideo:appId delegate:self parameters:@{}];
    [self configeRTCEngine];
    // 设置 Business ID
    [self.rtcEngineKit setBusinessId:bid];
}

- (void)configeRTCEngine {
    
    // 设置视频帧方向
    [self.rtcEngineKit setVideoOrientation:ByteRTCVideoOrientationPortrait];
    
    // 设置视频编码参数
    ByteRTCVideoEncoderConfig *config = [[ByteRTCVideoEncoderConfig alloc] init];
    config.width = 540;
    config.height = 960;
    config.frameRate = 15;
    config.maxBitrate = 1520;
    [self.rtcEngineKit setMaxVideoEncoderConfig:config];
    
    // 不会频繁修改AudioSession
    [self.rtcEngineKit setRuntimeParameters:@{@"rtc.audio_session_deactive" : @(YES)}];
}

- (void)destroyRTCVideo {
    // 销毁引擎
    [ByteRTCVideo destroyRTCVideo];
    self.rtcEngineKit = nil;
}

- (void)joinRoomWithToken:(NSString *)token roomID:(NSString *)roomID uid:(NSString *)uid complete:(void(^)(BOOL success))complete {
    self.joinRoomBlock = complete;
    
    self.rtcRoom = [self.rtcEngineKit createRTCRoom:roomID];
    self.rtcRoom.delegate = self;
    
    ByteRTCUserInfo *info = [[ByteRTCUserInfo alloc] init];
    info.userId = uid;
    ByteRTCRoomConfig *roomConfig = [[ByteRTCRoomConfig alloc] init];
    roomConfig.profile = ByteRTCRoomProfileCommunication;
    roomConfig.isAutoPublish = NO;
    roomConfig.isAutoSubscribeAudio = YES;
    roomConfig.isAutoSubscribeVideo = YES;
    
    [self.rtcRoom joinRoom:token userInfo:info roomConfig:roomConfig];
}

- (void)startPublishStream {
    [self.rtcEngineKit setAudioScenario:ByteRTCAudioScenarioHighqualityChat];
    [self.rtcRoom publishStream:ByteRTCMediaStreamTypeBoth];
}

- (void)enableLocalVideo:(BOOL)enable {
    if (enable) {
        [self.rtcEngineKit startVideoCapture];
        
    } else {
        [self.rtcEngineKit stopVideoCapture];
    }
    self.localVideoEnable = enable;
}

- (BOOL)currentLocalVideoEnable {
    return self.localVideoEnable;
}

- (void)enableLocalAudio:(BOOL)enable {
    if (enable) {
        [self.rtcEngineKit startAudioCapture];
    } else {
        [self.rtcEngineKit stopAudioCapture];
    }
    self.localAudioEnable = enable;
}

- (BOOL)currentLocalAudioEnable {
    return self.localAudioEnable;
}

- (void)startRenderLocalVideo:(UIView *)view {
    UIView *renderView = [[UIView alloc] init];
    ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
    canvas.view = renderView;
    canvas.renderMode = ByteRTCRenderModeHidden;
    [self.rtcEngineKit setLocalVideoCanvas:ByteRTCStreamIndexMain withCanvas:canvas];

    [view addSubview:renderView];
    [renderView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(view);
    }];
}

- (void)startRenderRemoteVideo:(UIView *)view userID:(NSString *)userID {
    ByteRTCRemoteStreamKey *streamKey = [[ByteRTCRemoteStreamKey alloc] init];
    streamKey.userId = userID;
    streamKey.roomId = [self.rtcRoom getRoomId];
    streamKey.streamIndex = ByteRTCStreamIndexMain;
    
    ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
    canvas.view = view;
    canvas.renderMode = ByteRTCRenderModeHidden;
    
    [self.rtcEngineKit setRemoteVideoCanvas:streamKey withCanvas:canvas];
}


- (void)switchCamera {
    // 切换 前置/后置 摄像头
        
    ByteRTCCameraID cameraID = self.currnetCameraID;
    if (cameraID == ByteRTCCameraIDFront) {
        cameraID = ByteRTCCameraIDBack;
    } else {
        cameraID = ByteRTCCameraIDFront;
    }
    
    [self setCameraID:cameraID];
}

- (void)setCameraID:(ByteRTCCameraID)cameraID {
    if (cameraID == ByteRTCCameraIDFront) {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    } else {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeNone];
    }
    
    [self.rtcEngineKit switchCamera:cameraID];
    self.currnetCameraID = cameraID;
}

- (void)setDeviceAudioRoute:(ByteRTCAudioRoute)route {
    [self.rtcEngineKit setDefaultAudioRoute:route];
}

- (ByteRTCAudioRoute)currentDeviceAudioRoute {
    return self.currentAudioRoute;
}

- (void)startAudioMixing {
    
    [self.rtcEngineKit setAudioScenario:ByteRTCAudioScenarioMedia];
    
    NSString *bundlePath = [[NSBundle mainBundle] pathForResource:HomeBundleName ofType:@"bundle"];
    NSBundle *bundle = [NSBundle bundleWithPath:bundlePath];
    NSString *filePath = [bundle pathForResource:@"call_receive" ofType:@"mp3"];
    
    ByteRTCAudioMixingConfig *config = [[ByteRTCAudioMixingConfig alloc] init];
    config.type = ByteRTCAudioMixingTypePlayout;
    config.playCount = 0;
    
    [[self.rtcEngineKit getAudioMixingManager] startAudioMixing:0 filePath:filePath config:config];
}

- (void)stopAudioMixing {
    [[self.rtcEngineKit getAudioMixingManager] stopAudioMixing:0];
}

- (void)leaveRoom {
    [self.rtcRoom leaveRoom];
    [self.rtcRoom destroy];
    self.rtcRoom = nil;
    
    [self enableLocalAudio:NO];
    [self enableLocalVideo:NO];
}

- (void)openPIPMode {
    // PIP模式下需开启
    [self.rtcEngineKit setRuntimeParameters:@{@"rtc.enable_pip_mode":@(YES)}];
}

- (void)setRemoteVideoSink:(ByteRTCRemoteStreamKey* _Nonnull)streamKey delegate:(id<ByteRTCVideoSinkDelegate> _Nullable)delegate {
    [self.rtcEngineKit setRemoteVideoSink:streamKey withSink:delegate withPixelFormat:ByteRTCVideoSinkPixelFormatNV12];
}

- (BOOL)networkAvailable {
    return self.networkType != ByteRTCNetworkTypeDisconnected;
}

#pragma mark - ByteRTCRoomDelegate
- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRoomStateChanged:(NSString *)roomId withUid:(NSString *)uid state:(NSInteger)state extraInfo:(NSString *)extraInfo {
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.joinRoomBlock) {
            self.joinRoomBlock(state == 0);
            self.joinRoomBlock = nil;
        }
    });
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onUserJoined:(ByteRTCUserInfo *)userInfo elapsed:(NSInteger)elapsed {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(videoCallRTCManager:onUserJoined:)]) {
            [self.delegate videoCallRTCManager:self onUserJoined:userInfo.userId];
        }
    });
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onUserLeave:(NSString *)uid reason:(ByteRTCUserOfflineReason)reason {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(videoCallRTCManager:onUserLeaved:)]) {
            [self.delegate videoCallRTCManager:self onUserLeaved:uid];
        }
    });
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onNetworkQuality:(ByteRTCNetworkQualityStats *)localQuality remoteQualities:(NSArray<ByteRTCNetworkQualityStats *> *)remoteQualities {
    ByteRTCNetworkQualityStats *remoteQuality = remoteQualities.firstObject;
    if (!remoteQuality) {
        return;
    }
    
    NSString *message = @"";
    if (MAX(localQuality.txQuality, localQuality.rxQuality) <= ByteRTCNetworkQualityPoor &&
        MAX(remoteQuality.txQuality, remoteQuality.rxQuality) <= ByteRTCNetworkQualityPoor) {
        message = @"";
        
    } else if (MAX(localQuality.txQuality, localQuality.rxQuality) >= ByteRTCNetworkQualityBad) {
        message = LocalizedString(@"local_network_poor_tip");
        
    } else if (MAX(remoteQuality.txQuality, remoteQuality.rxQuality) >= ByteRTCNetworkQualityBad) {
        message = LocalizedString(@"remote_network_poor_tip");
    }
    
    BOOL isVeryBad = (MAX(localQuality.txQuality, localQuality.rxQuality) >= ByteRTCNetworkQualityVeryBad);
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(videoCallRTCManager:onNetworkQualityChanged:qualityVeryBad:)]) {
            [self.delegate videoCallRTCManager:self onNetworkQualityChanged:message qualityVeryBad:isVeryBad];
        }
    });
}

#pragma mark - ByteRTCVideoDelegate
- (void)rtcEngine:(ByteRTCVideo *)engine onUserStartAudioCapture:(NSString *)roomId uid:(NSString *)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(videoCallRTCManager:onUserToggleMic:userID:)]) {
            [self.delegate videoCallRTCManager:self onUserToggleMic:YES userID:userId];
        }
    });
}

- (void)rtcEngine:(ByteRTCVideo *)engine onUserStopAudioCapture:(NSString *)roomId uid:(NSString *)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(videoCallRTCManager:onUserToggleMic:userID:)]) {
            [self.delegate videoCallRTCManager:self onUserToggleMic:NO userID:userId];
        }
    });
}

- (void)rtcEngine:(ByteRTCVideo *)engine onUserStartVideoCapture:(NSString *)roomId uid:(NSString *)uid {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(videoCallRTCManager:onUserToggleCamera:userID:)]) {
            [self.delegate videoCallRTCManager:self onUserToggleCamera:YES userID:uid];
        }
    });
}

- (void)rtcEngine:(ByteRTCVideo *)engine onUserStopVideoCapture:(NSString *)roomId uid:(NSString *)uid {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(videoCallRTCManager:onUserToggleCamera:userID:)]) {
            [self.delegate videoCallRTCManager:self onUserToggleCamera:NO userID:uid];
        }
    });
}

- (void)rtcEngine:(ByteRTCVideo *)engine onFirstLocalVideoFrameCaptured:(ByteRTCStreamIndex)streamIndex withFrameInfo:(ByteRTCVideoFrameInfo *)frameInfo {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(videoCallRTCManager:onUserToggleCamera:userID:)]) {
            [self.delegate videoCallRTCManager:self onUserToggleCamera:YES userID:[LocalUserComponent userModel].uid];
        }
    });
}

- (void)rtcEngine:(ByteRTCVideo *)engine onAudioRouteChanged:(ByteRTCAudioRoute)device {
    self.currentAudioRoute = device;
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(videoCallRTCManager:onAudioRouteChanged:)]) {
            [self.delegate videoCallRTCManager:self onAudioRouteChanged:device];
        }
    });
}

- (void)rtcEngine:(ByteRTCVideo *)engine onNetworkTypeChanged:(ByteRTCNetworkType)type {
    self.networkType = type;
}

@end
