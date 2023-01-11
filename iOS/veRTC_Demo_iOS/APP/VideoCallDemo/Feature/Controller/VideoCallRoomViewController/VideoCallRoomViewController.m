#import "VideoCallRoomViewController.h"
#import "VideoCallSettingViewController.h"
#import "SystemAuthority.h"
#import "VideoCallRoomNavView.h"
#import "VideoCallRoomBottomView.h"
#import "VideoCallRoomViewController+Listener.h"
#import "UIViewController+Orientation.h"
#import "VideoCallRTCManager.h"
#import "VideoCallRTMManager.h"
#import "VideoCallAvatarPageView.h"
#import "VideoCallAvatarView.h"
#import "VideoCallFullScreenView.h"
#import "VideoCallStatsComponent.h"

@interface VideoCallRoomViewController () <UINavigationControllerDelegate, VideoCallRTCManagerDelegate, VideoCallRoomNavViewDelegate, VideoCallRoomBottomViewDelegate, VideoCallAvatarPageViewDelegate>

@property (nonatomic, strong) VideoCallRoomNavView *navView;
@property (nonatomic, strong) VideoCallRoomBottomView *bottomView;
@property (nonatomic, strong) VideoCallAvatarPageView *roomMainView;
@property (nonatomic, strong) VideoCallFullScreenView *fullScreenView;
@property (nonatomic, strong) VideoCallRoomUserModel *localUserModel;
@property (nonatomic, strong) VideoCallStatsComponent *statsComponent;

@property (nonatomic, strong) NSMutableArray <VideoCallRoomUserModel *>*userArray;
@property (nonatomic, assign) NSInteger currentFullScreenUserIndex;
@property (nonatomic, assign) NSInteger duration;
@end

@implementation VideoCallRoomViewController

- (instancetype)initWithVideoSession:(VideoCallRoomUserModel *)loginModel
                            rtcToken:(NSString *)rtcToken
                            duration:(NSInteger)duration {
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationBecomeActive) name:UIApplicationWillEnterForegroundNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationEnterBackground) name: UIApplicationDidEnterBackgroundNotification object:nil];
        
        [UIApplication sharedApplication].idleTimerDisabled = YES;
        self.localUserModel = loginModel;
        self.duration = duration;
        
        [VideoCallRTCManager shareRtc].delegate = self;
        [[VideoCallRTCManager shareRtc] joinChannelWithModel:self.localUserModel
                                                    rtcToken:rtcToken];
        
        self.currentFullScreenUserIndex = -1;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor colorFromHexString:@"#1D2129"];
    
    [self addOrientationNotice];
    [self addRTSListener];
    [self createUIComponent];
    [self updateNavTime:self.duration];
    [self startLocalCameraPreview];
    
    __weak __typeof(self) wself = self;
    [VideoCallRTCManager shareRtc].rtcJoinRoomBlock = ^(NSString * _Nonnull roomId, NSInteger errorCode, NSInteger joinType) {
        if (joinType != 0 && errorCode == 0) {
            [wself videoCallReconnect];
        }
    };
        
    self.fullScreenView.clickOrientationBlock = ^(BOOL isLandscape) {
        if (!isLandscape) {
            [wself setDeviceInterfaceOrientation:UIDeviceOrientationLandscapeLeft];
        } else {
            [wself setDeviceInterfaceOrientation:UIDeviceOrientationPortrait];
        }
    };
    
    [self.bottomView updateButtonStatus:RoomBottomStatusMic close:!self.localUserModel.isEnableAudio];
    [self.bottomView updateButtonStatus:RoomBottomStatusCamera close:!self.localUserModel.isEnableVideo];
    [self.bottomView updateButtonStatus:RoomBottomStatusAudio close:!self.localUserModel.isSpeakers];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    [[VideoCallRTCManager shareRtc] updateRtcVideoParams];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    
    [self setAllowAutoRotate:ScreenOrientationPortrait];
}

#pragma mark - NSNotification

- (void)applicationBecomeActive {
    // APP 恢复活跃状态时，如果是开启相机状态需要恢复相机采集。
    // When the APP returns to the active state, if the camera is turned on, the camera acquisition needs to be resumed.
    if (self.localUserModel.isEnableVideo) {
        [[VideoCallRTCManager shareRtc] enableLocalVideo:YES];
    }
}

- (void)applicationEnterBackground {
    [[VideoCallRTCManager shareRtc] enableLocalVideo:NO];
}

#pragma mark - UIViewController+Orientation

- (void)orientationDidChang:(BOOL)isLandscape {
    CGFloat navHeight = 44 + [DeviceInforTool getStatusBarHight];
    CGFloat bottomBottom = 0;
    if (isLandscape) {
        navHeight = 0;
        bottomBottom = (128/2 + [DeviceInforTool getVirtualHomeHeight] * 2);
    }
    
    [self.navView mas_updateConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(navHeight);
    }];
    [self.bottomView mas_updateConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo(self.view).offset(bottomBottom);
    }];
}

#pragma mark - VideoCallRTCManagerDelegate

- (void)rtcManager:(VideoCallRTCManager * _Nonnull)rtcManager onUserJoined:(NSString *_Nullable)uid userName:(NSString *_Nullable)name {
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        [self addRemoteUserViewWithUid:uid name:name];
    });
}

- (void)rtcManager:(VideoCallRTCManager * _Nonnull)rtcManager onUserLeaved:(NSString *_Nullable)uid {
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        [self removeRemoteUserViewWithUid:uid isOnlyRemoveScreenShare:NO];
    });
}

- (void)rtcManager:(VideoCallRTCManager * _Nonnull)rtcManager didScreenStreamAdded:(NSString *_Nullable)screenStreamsUid {
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        [self addRemoteScreenViewWithUid:screenStreamsUid];
    });
}

- (void)rtcManager:(VideoCallRTCManager *_Nonnull)rtcManager didScreenStreamRemoved:(NSString *)screenStreamsUid {
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        [self removeRemoteUserViewWithUid:screenStreamsUid
                  isOnlyRemoveScreenShare:YES];
    });
}

- (void)rtcManager:(VideoCallRTCManager *_Nullable)rtcManager didUpdateVideoStatsInfo:(NSDictionary <NSString *, VideoCallRoomParamInfoModel *>*_Nullable)statsInfo {
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        NSMutableArray *videoStatsInfoArray = [NSMutableArray array];
        for (VideoCallRoomUserModel *userModel in self.userArray) {
            if (userModel.isScreen) {
                continue;
            }
            
            VideoCallRoomParamInfoModel *videoStatsInfo = [statsInfo objectForKey:userModel.uid];
            if (!videoStatsInfo) {
                continue;
            }
            
            videoStatsInfo.name = userModel.name;
            [videoStatsInfoArray addObject:videoStatsInfo];
        }
        
        [self.statsComponent setVideoStats:videoStatsInfoArray];
    });
}

- (void)rtcManager:(VideoCallRTCManager *_Nullable)rtcManager didUpdateAudioStatsInfo:(NSDictionary <NSString *, VideoCallRoomParamInfoModel *>*_Nullable)statsInfo {
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        NSMutableArray *audioStatsInfoArray = [NSMutableArray array];
        for (VideoCallRoomUserModel *userModel in self.userArray) {
            if (userModel.isScreen) {
                continue;
            }
            
            VideoCallRoomParamInfoModel *audioStatsInfo = [statsInfo objectForKey:userModel.uid];
            if (!audioStatsInfo) {
                continue;
            }
            
            audioStatsInfo.name = userModel.name;
            [audioStatsInfoArray addObject:audioStatsInfo];
        }
        
        [self.statsComponent setAudioStats:audioStatsInfoArray];
    });
}

- (void)rtcManager:(VideoCallRTCManager *)rtcManager reportAllAudioVolume:(NSDictionary<NSString *, NSNumber *> *)volumeInfo {
    
    for (VideoCallRoomUserModel *userModel in self.userArray) {
        NSUInteger index = [self.userArray indexOfObject:userModel];
        VideoCallAvatarView *avatarView = (VideoCallAvatarView *)[self.roomMainView avatarViewAtIndex:index];
        
        VideoCallAvatarViewMicStatus micStatus = VideoCallAvatarViewMicStatusOff;
        
        if (userModel.isEnableAudio) {
            NSString *uid = userModel.uid;
            NSNumber *value = [volumeInfo objectForKey:uid];
            if (value.integerValue > 0) {
                micStatus = VideoCallAvatarViewMicStatusSpeaking;
            } else {
                micStatus = VideoCallAvatarViewMicStatusOn;
            }
        } else {
            micStatus = VideoCallAvatarViewMicStatusOff;
        }
        [avatarView setMicStatus:micStatus];
    }
}

- (void)rtcManager:(VideoCallRTCManager *_Nonnull)rtcManager onUserMuteAudio:(NSString * _Nonnull)uid isMute:(BOOL)isMute {
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        [self setMuteAudioWithUid:uid ismute:isMute];
    });
}

- (void)rtcManager:(VideoCallRTCManager *_Nonnull)rtcManager onUserMuteVideo:(NSString * _Nonnull)uid isMute:(BOOL)isMute {
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        [self setMuteVideoWithUid:uid ismute:isMute];
    });
}

- (void)rtcManager:(VideoCallRTCManager *)rtcManager onAudioRouteChanged:(BOOL)isHeadset {
    if (isHeadset) {
        [self.bottomView updateButtonStatus:RoomBottomStatusAudio enable:NO];
    } else {
        [self.bottomView updateButtonStatus:RoomBottomStatusAudio enable:YES];
        [self.bottomView updateButtonStatus:RoomBottomStatusAudio close:NO];
    }
}

#pragma mark - VideoCallRoomBottomViewDelegate

- (void)VideoCallRoomBottomView:(VideoCallRoomBottomView *)VideoCallRoomBottomView itemButton:(VideoCallRoomItemButton *)itemButton didSelectStatus:(RoomBottomStatus)status {
    if (status == RoomBottomStatusMic) {
        [self clickRoomBottomStatusMic:itemButton];
    } else if (status == RoomBottomStatusCamera) {
        [self clickRoomBottomStatusCamera:itemButton];
    } else if (status == RoomBottomStatusAudio) {
        BOOL isSpeakers = (itemButton.status == ButtonStatusActive);
        if (isSpeakers) {
            itemButton.status = ButtonStatusNone;
            itemButton.desTitle = @"扬声器";
        } else {
            itemButton.status = ButtonStatusActive;
            itemButton.desTitle = @"听筒";
        }
        ByteRTCAudioRoute route = isSpeakers ? ByteRTCAudioRouteSpeakerphone : ByteRTCAudioRouteEarpiece;
        [[VideoCallRTCManager shareRtc] setEnableSpeakerphone:route];
        self.localUserModel.isSpeakers = isSpeakers;
    } else if (status == RoomBottomStatusParameter) {
        [self.statsComponent showStatsView];
    } else if (status == RoomBottomStatusSet) {
        VideoCallSettingViewController *settingsVC = [[VideoCallSettingViewController alloc] init];
        [self.navigationController pushViewController:settingsVC animated:YES];
    } else {
        
    }
}

#pragma mark - VideoCallRoomNavViewDelegate

- (void)VideoCallRoomNavView:(VideoCallRoomNavView *)VideoCallRoomNavView didSelectStatus:(RoomNavStatus)status {
    if (status == RoomNavStatusHangeup) {
        [self showEndView];
    } else if (status == RoomNavStatusSwitchCamera) {
        if (self.localUserModel.isEnableVideo) {
            [[VideoCallRTCManager shareRtc] switchCamera];
        }
    }
}

#pragma mark - VideoCallAvatarPageViewDelegate

- (void)onShowAvatarView:(UIView *)avatarView index:(NSUInteger)index {
    
}

- (void)onHideAvatarView:(UIView *)avatarView index:(NSUInteger)index {
    
}

- (void)onClickAvatarView:(UIView *)avatarView index:(NSUInteger)index {
    if (self.userArray.count <= 2) {
        //1v1时，点击小窗成为主窗口
        if (index != self.roomMainView.mainViewIndex) {
            [self.roomMainView bringViewToMainAvatarViewOfIndex:index];
        }
    } else {
        VideoCallRoomUserModel *userModel = self.userArray[index];
        if (!userModel.isScreen) {
            return;
        }
        
        [self showFullScreenViewWithIndex:index];
    }
}

- (void)onScrollToPageIndex:(NSUInteger)pageIndex {
    
}

#pragma mark - Network request Action

- (void)videoCallReconnect {
    __weak __typeof(self) wself = self;
    [VideoCallRTMManager reconnectWithBlock:^(NSString * _Nonnull RTCToken, RTMACKModel * _Nonnull model) {
        if (model.result) {
            
        } else if (model.code == RTMStatusCodeUserIsInactive ||
                   model.code == RTMStatusCodeRoomDisbanded ||
                   model.code == RTMStatusCodeUserNotFound) {
            [wself hangUp];
        } else {

        }
    }];
}

#pragma mark - Publish Action

- (void)hangUp {
    //socket api end/leave room
    [VideoCallRTMManager leaveRoom];
    //sdk api end room
    [[VideoCallRTCManager shareRtc] leaveChannel];
    //ui
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Private Action

- (void)setMuteVideoWithUid:(NSString *)uid ismute:(BOOL)isMute {
    for (VideoCallRoomUserModel *userModel in self.userArray) {
        if ([userModel.uid isEqualToString:uid] && !userModel.isScreen) {
            NSUInteger index = [self.userArray indexOfObject:userModel];
        
            VideoCallAvatarView *avatarView = (VideoCallAvatarView *)[self.roomMainView avatarViewAtIndex:index];
            [avatarView setVideoStatus:isMute ? VideoCallAvatarViewVideoStatusOff : VideoCallAvatarViewVideoStatusOn];
        }
    }
}

- (void)setMuteAudioWithUid:(NSString *)uid ismute:(BOOL)isMute {
    for (VideoCallRoomUserModel *userModel in self.userArray) {
        if ([userModel.uid isEqualToString:uid]) {
            NSUInteger index = [self.userArray indexOfObject:userModel];
            userModel.isEnableAudio = !isMute;
            VideoCallAvatarView *avatarView = (VideoCallAvatarView *)[self.roomMainView avatarViewAtIndex:index];
            [avatarView setMicStatus:isMute ? VideoCallAvatarViewMicStatusOff : VideoCallAvatarViewMicStatusOn];
        }
    }
}

- (void)showFullScreenViewWithIndex:(NSUInteger)index {
    if (index >= self.userArray.count) {
        return;
    }
    
    self.currentFullScreenUserIndex = index;
    
    VideoCallRoomUserModel *userModel = self.userArray[index];
    NSString *uid = userModel.uid;
    
    [self setAllowAutoRotate:ScreenOrientationLandscapeAndPortrait];
    __weak __typeof(self) wself = self;
    [self.fullScreenView show:uid
                     userName:userModel.name
                       roomId:self.localUserModel.roomId
                        block:^(BOOL isRemove) {
        if (userModel.isScreen) {
            if (!isRemove) {
                // 如果是移除屏幕流，不需要执行重新添加。
                // 如果是手动点击，需要重新添加屏幕流。
                [wself addRemoteScreenViewWithUid:uid];
            }
        } else {
            // reset user flow
            [wself restoreUserStreamDisplay];
        }
        [wself setAllowAutoRotate:ScreenOrientationPortrait];
        wself.currentFullScreenUserIndex = -1;
    }];
}

- (void)restoreUserStreamDisplay {
    UIView *smallView = [self.roomMainView avatarViewAtIndex:self.currentFullScreenUserIndex];
    if (!smallView) {
        return;
    }
    
    VideoCallRoomUserModel *userModel = self.userArray[self.currentFullScreenUserIndex];
    
    ByteRTCVideoCanvas *videoCanvas = [ByteRTCVideoCanvas new];
    videoCanvas.view = smallView;
    videoCanvas.renderMode = ByteRTCRenderModeHidden;
    videoCanvas.roomId = self.localUserModel.roomId;
    videoCanvas.uid = userModel.uid;
    [[VideoCallRTCManager shareRtc] setupRemoteVideo:videoCanvas];
}

- (void)updateNavTime:(NSInteger)time {
    if (time < 0) {
        time = 0;
    }
    self.navView.meetingTime = time;
    self.navView.localVideoSession = self.localUserModel;
}

- (void)createUIComponent {
    [self.view addSubview:self.navView];
    [self.view addSubview:self.bottomView];
    
    CGFloat navHeight = 44 + [DeviceInforTool getStatusBarHight];
    CGFloat bottomHeight = 128/2 + [DeviceInforTool getVirtualHomeHeight];
    
    [self.navView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.left.right.equalTo(self.view);
        make.height.mas_equalTo(navHeight);
    }];
    
    [self.bottomView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self.view);
        make.bottom.equalTo(self.view).offset(0);
        make.height.mas_equalTo(bottomHeight);
    }];
    
    self.roomMainView = [[VideoCallAvatarPageView alloc] initWithFrame:CGRectMake(0, navHeight, self.view.frame.size.width, self.view.frame.size.height - navHeight - bottomHeight)];
    self.roomMainView.avatarPageViewDelegate = self;
    [self.view addSubview:self.roomMainView];
    
    [self.view addSubview:self.fullScreenView];
    [self.fullScreenView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self.view);
        make.top.equalTo(self.navView.mas_bottom);
        make.bottom.equalTo(self.bottomView.mas_top);
    }];
    
    self.statsComponent = [[VideoCallStatsComponent alloc] initWithSuperView:self.view];
}

- (void)showEndView {
    AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
    alertCancelModel.title = @"确认";
    __weak typeof(self) weakSelf = self;
    alertCancelModel.alertModelClickBlock = ^(UIAlertAction * _Nonnull action) {
        [weakSelf hangUp];
    };
    AlertActionModel *alertModel = [[AlertActionModel alloc] init];
    alertModel.title = @"取消";
    [[AlertActionManager shareAlertActionManager] showWithMessage:@"是否确定退出房间？" actions:@[alertModel, alertCancelModel]];
}

- (void)clickRoomBottomStatusMic:(VideoCallRoomItemButton *)itemButton {
    [SystemAuthority authorizationStatusWithType:AuthorizationTypeAudio block:^(BOOL isAuthorize) {
        if (isAuthorize) {
            BOOL isEnableAudio = (itemButton.status == ButtonStatusActive) ? YES : NO;
            itemButton.status = isEnableAudio ? ButtonStatusNone : ButtonStatusActive;
            self.localUserModel.isEnableAudio = isEnableAudio;
            [[VideoCallRTCManager shareRtc] publishAudioStream:isEnableAudio];
            [SystemAuthority autoJumpWithAuthorizationStatusWithType:AuthorizationTypeAudio];
            
            [self setMuteAudioWithUid:[LocalUserComponent userModel].uid ismute:!self.localUserModel.isEnableAudio];
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:@"麦克风权限已关闭，请至设备设置页开启"];
        }
    }];
}

- (void)clickRoomBottomStatusCamera:(VideoCallRoomItemButton *)itemButton {
    [SystemAuthority authorizationStatusWithType:AuthorizationTypeCamera block:^(BOOL isAuthorize) {
        if (isAuthorize) {
            itemButton.status = (itemButton.status == ButtonStatusActive) ? ButtonStatusNone : ButtonStatusActive;
            self.localUserModel.isEnableVideo = !self.localUserModel.isEnableVideo;
            [[VideoCallRTCManager shareRtc] enableLocalVideo:self.localUserModel.isEnableVideo];
            [SystemAuthority autoJumpWithAuthorizationStatusWithType:AuthorizationTypeCamera];
            
            [self setMuteVideoWithUid:[LocalUserComponent userModel].uid ismute:!self.localUserModel.isEnableVideo];
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:@"摄像头权限已关闭，请至设备设置页开启"];
        }
    }];
}

- (void)startLocalCameraPreview {
    NSString *name = [NSString stringWithFormat:@"%@(我)", [LocalUserComponent userModel].name];
    [self.userArray addObject:self.localUserModel];
    
    VideoCallAvatarView *avatarView = [VideoCallAvatarView new];
    [avatarView setName:name];
    [self.roomMainView addAvatarView:avatarView];
    
    ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
    canvas.view = avatarView.videoContainerView;
    canvas.renderMode = ByteRTCRenderModeHidden;
    canvas.view.backgroundColor = [UIColor clearColor];
    //设置本地视频显示信息
    //Set local video display information
    [[VideoCallRTCManager shareRtc] setupLocalVideo:canvas];
}

- (void)addRemoteUserViewWithUid:(NSString *)uid name:(NSString *)name {
    VideoCallRoomUserModel *userModel = [VideoCallRoomUserModel new];
    userModel.uid = uid;
    userModel.name = name;
    userModel.isScreen = NO;
    [self.userArray addObject:userModel];
    
    VideoCallAvatarView *avatarView = [VideoCallAvatarView new];
    [avatarView setName:name];
    [self.roomMainView addAvatarView:avatarView];

    ByteRTCVideoCanvas *videoCanvas = [ByteRTCVideoCanvas new];
    videoCanvas.view = avatarView.videoContainerView;
    videoCanvas.renderMode = ByteRTCRenderModeHidden;
    videoCanvas.roomId = self.localUserModel.roomId;
    videoCanvas.uid = uid;
    [[VideoCallRTCManager shareRtc] setupRemoteVideo:videoCanvas];
}

- (void)removeRemoteUserViewWithUid:(NSString *)uid
            isOnlyRemoveScreenShare:(BOOL)isScreenShare {
    if (isScreenShare) {
        [self.fullScreenView dismiss:YES];
    }
    NSMutableArray *userModelMovedArray = [NSMutableArray array];
    for (VideoCallRoomUserModel *userModel in self.userArray) {
        if ([userModel.uid isEqualToString:uid]) {
            if (isScreenShare && !userModel.isScreen) {
                continue;
            }
            NSUInteger index = [self.userArray indexOfObject:userModel];

            [userModelMovedArray addObject:userModel];
            [self.roomMainView removeAvatarViewAtIndex:index];
        }
    }
    [self.userArray removeObjectsInArray:userModelMovedArray];
}

- (void)addRemoteScreenViewWithUid:(NSString *)uid {
    // 屏幕流模型
    VideoCallRoomUserModel *screenModel = nil;
    
    // 共享屏幕流的用户模型
    VideoCallRoomUserModel *screenUserModel = nil;
    for (VideoCallRoomUserModel *model in self.userArray) {
        if ([model.uid isEqualToString:uid]) {
            if (model.isScreen) {
                screenModel = model;
            } else {
                screenUserModel = model;
            }
            break;
        }
    }
    if (!screenModel) {
        NSString *tagMessage = [NSString stringWithFormat:@"%@的屏幕共享",screenUserModel.name];
        screenModel = [VideoCallRoomUserModel new];
        screenModel.uid = uid;
        screenModel.name = tagMessage;
        screenModel.isScreen = YES;
        screenModel.isEnableAudio = screenUserModel.isEnableAudio;
        screenModel.isEnableVideo = YES;
        [self.userArray insertObject:screenModel atIndex:0];
        
        VideoCallAvatarView *avatarView = [VideoCallAvatarView new];
        [avatarView setName:tagMessage];
        [self.roomMainView addAvatarView:avatarView atIndex:0];
    }
    
    VideoCallAvatarView *avatarView = (VideoCallAvatarView *)[self.roomMainView avatarViewAtIndex:0];
    UIView *screenRenderView = [[VideoCallRTCManager shareRtc] getScreenStreamViewWithUid:uid];
    screenRenderView.hidden = NO;
    [avatarView.videoContainerView addSubview:screenRenderView];
    [screenRenderView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(avatarView.videoContainerView);
    }];
}

#pragma mark - Getter

- (VideoCallRoomBottomView *)bottomView {
    if (!_bottomView) {
        _bottomView = [[VideoCallRoomBottomView alloc] init];
        _bottomView.delegate = self;
    }
    return _bottomView;
}

- (VideoCallRoomNavView *)navView {
    if (!_navView) {
        _navView = [[VideoCallRoomNavView alloc] init];
        _navView.delegate = self;
    }
    return _navView;
}

- (NSMutableArray<VideoCallRoomUserModel *> *)userArray {
    if (!_userArray) {
        _userArray = [NSMutableArray array];
    }
    return _userArray;
}

- (VideoCallFullScreenView *)fullScreenView {
    if (!_fullScreenView) {
        _fullScreenView = [[VideoCallFullScreenView alloc] init];
    }
    return _fullScreenView;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [UIApplication sharedApplication].idleTimerDisabled = NO;
}

@end
