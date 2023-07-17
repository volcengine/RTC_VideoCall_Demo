// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallViewController.h"
#import "VideoCallHomeViewController.h"
#import "VideoCallControlComponent.h"
#import "VideoCallAudioComponent.h"
#import "VideoCallVideoComponent.h"
#import "VideoCallRTCManager.h"
#import "PIPComponent.h"

extern int VideoCallOnTheCallType;

@interface VideoCallViewController ()<VideoCallControlComponentDelegate, VideoCallComponentDelegate, PIPComponentDelegate, VideoCallRTCManagerDelegate, VideoCallRTSManagerDelegate>

@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UIView *videoView;
@property (nonatomic, strong) UIButton *narrowButton;
@property (nonatomic, strong) UILabel *timeLabel;
@property (nonatomic, strong) VideoCallControlComponent *controlComponent;

@property (nonatomic, strong) id<VideoCallComponent> videoComponent;
@property (nonatomic, strong) PIPComponent *pipComponent;

@property (nonatomic, assign) VideoCallState state;

@property (nonatomic, strong) GCDTimer *timer;
@property (nonatomic, assign) NSInteger time;
@property (nonatomic, assign) BOOL remoteUserJoined;

@end

static VideoCallViewController *_callViewController = nil;

@implementation VideoCallViewController

+ (VideoCallViewController *)currentController {
    return _callViewController;
}

- (instancetype)init {
    if (self = [super init]) {
        [VideoCallRTCManager shareRtc].delegate = self;
        [VideoCallRTSManager getRTSManager].delegate = self;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setupViews];
    
    self.state = [self.infoModel.fromUserId isEqualToString:[LocalUserComponent userModel].uid]? VideoCallStateCalling : VideoCallStateRinging;
    
    VideoCallOnTheCallType = (int)self.infoModel.callType;
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(closeVideoCallNotice:) name:NotificationCloseVideoCallNarrow object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationWillTerminate) name:UIApplicationWillTerminateNotification object:nil];
    
    [UIApplication sharedApplication].idleTimerDisabled = YES;
}

- (void)setupViews {
    self.view.backgroundColor = [UIColor colorFromHexString:@"#1E1E1E"];
    [self.view addSubview:self.contentView];
    [self.contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.view);
    }];
    [self.contentView addSubview:self.videoView];
    [self.videoView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.contentView);
    }];
    
    [self videoComponent];
    [self controlComponent];
    [self.view addSubview:self.narrowButton];
    [self.view addSubview:self.timeLabel];
    [self.narrowButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.view).offset(5);
        make.top.equalTo(self.view).offset(30 + [DeviceInforTool getStatusBarHight]);
        make.size.mas_equalTo(CGSizeMake(44, 44));
    }];
    [self.timeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view);
        make.centerY.equalTo(self.narrowButton);
    }];
}

#pragma mark - VideoCallRTCManagerDelegate

- (void)videoCallRTCManager:(VideoCallRTCManager *)manager onUserToggleMic:(BOOL)micOn userID:(nonnull NSString *)userID {
    if (!micOn) {
        [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"remote_user_close_mic")];
    }
}

- (void)videoCallRTCManager:(VideoCallRTCManager *)manager onUserToggleCamera:(BOOL)cameraOn userID:(NSString *)userID {
    if (self.infoModel.callType == VideoCallTypeVideo) {
        if ([userID isEqualToString:[LocalUserComponent userModel].uid]) {
            [self.videoComponent updateUserCamera:cameraOn isLocalUser:YES];
        } else {
            [self.videoComponent updateUserCamera:cameraOn isLocalUser:NO];
        }
        if (!cameraOn) {
            [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"remote_user_close_camera")];
        }
    }
}

- (void)videoCallRTCManager:(VideoCallRTCManager *)manager onNetworkQualityChanged:(NSString *)message qualityVeryBad:(BOOL)isVeryBad {
    [self.controlComponent updateNetworkMessage:message qualityVeryBad:isVeryBad];
}

- (void)videoCallRTCManager:(VideoCallRTCManager *)manager onUserJoined:(NSString *)userID {
    [self.videoComponent startRenderRemoteView:userID];
    self.remoteUserJoined = YES;
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(joinRoomTimeout) object:nil];
}

- (void)videoCallRTCManager:(VideoCallRTCManager *)manager onUserLeaved:(NSString *)userID {
    [self hangup];
}

- (void)videoCallRTCManager:(VideoCallRTCManager *)manager onAudioRouteChanged:(ByteRTCAudioRoute)device {
    [self.controlComponent audioRouteChanged:device];
}

#pragma mark - VideoCallRTSManagerDelegate
- (void)videoCallRTSManager:(VideoCallRTSManager *)manager onReceivedMessage:(VideoCallEventType)type infoModel:(VideoCallVoipInfo *)info {
    if (self.state == VideoCallStateIdle) {
        // 房间被销毁不再接收消息
        return;
    }
    switch (type) {
        case VideoCallEventTypeAnswerCall: {
            // 主叫收到被叫进房
            if (self.state == VideoCallStateOnTheCall) {
                return;
            }
            __weak typeof(self) weakSelf = self;
            self.infoModel.status = VideoCallStateOnTheCall;
            [VideoCallRTSManager updateStatus:VideoCallStateOnTheCall info:self.infoModel block:^(RTSACKModel * _Nonnull model) {
                if (model.result) {
                    [[VideoCallRTCManager shareRtc] startPublishStream];
                    weakSelf.state = VideoCallStateOnTheCall;
                } else {
                    [[ToastComponent shareToastComponent] showWithMessage:model.message delay:0.5];
                    [weakSelf quitViewController];
                }
            }];
        }
            break;
        case VideoCallEventTypeLeaveRoom: {
            // 主叫：收到被叫拒绝、收到被叫挂断、收到被叫应答超时 被叫：收到主叫挂断
            if (self.state == VideoCallStateOnTheCall) {
                //主叫: 收到被叫挂断 被叫：收到主叫挂断
                [self hangup];
            } else {
                // 主叫：收到被叫拒绝、收到被叫应答超时
                [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"called_refuse_tip") delay:1.0];
                [self quitViewController];
            }
        }
            break;
        case VideoCallEventTypeCancel: {
            // 被叫：收到主叫取消呼叫
            [self quitViewController];
        }
            break;
        case VideoCallEventTypeOverTime: {
            // 体验超时
            [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"minutes_error_message") delay:0.5];
            [self quitViewController];
        }
            break;
 
        default:
            break;
    }
}

#pragma mark - VideoCallControlComponentDelegate
- (void)videoCallControlComponentOnAccept:(VideoCallControlComponent *)component {
    
    // 不是空闲状态，不处理接听事件
    if (self.infoModel.status != VideoCallStateIdle) {
        return;
    }
    
    self.infoModel.status = VideoCallStateAccept;
    [VideoCallRTSManager updateStatus:VideoCallStateAccept info:self.infoModel block:^(RTSACKModel * _Nonnull model) {

    }];
    [self joinRTCRoom];
}

- (void)videoCallControlComponentOnHangup:(VideoCallControlComponent *)component {
    // 点击挂断按钮
    VideoCallState updateState;
    if (self.state == VideoCallStateRinging) {
        // 被叫拒绝
        updateState = VideoCallStateRefused;
    } else if (self.state == VideoCallStateCalling) {
        // 主叫取消
        updateState = VideoCallStateCancelled;
    } else {
        // 通话中挂断
        updateState = VideoCallStateTerminated;
        [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"hang_up_tip") delay:0.5];
    }
    self.infoModel.status = VideoCallStateTerminated;
    [VideoCallRTSManager updateStatus:updateState info:self.infoModel block:^(RTSACKModel * _Nonnull model) {
        
    }];
    [self quitViewController];
}

- (void)videoCallControlComponent:(VideoCallControlComponent *)component onCameraEnableChanged:(BOOL)enable {
    if (!enable) {
        [self.videoComponent updateUserCamera:enable isLocalUser:YES];
    }
}

- (void)videoCallControlComponentOnTimeOut:(VideoCallControlComponent *)component {
    
    if (self.state == VideoCallStateCalling) {
        [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"calling_wait_time_out_tip") delay:0.5];
    }
    
    [VideoCallRTSManager updateStatus:VideoCallStateUnavailable info:self.infoModel block:^(RTSACKModel * _Nonnull model) {
        
    }];
    [self quitViewController];
}

- (void)videoCallControlComponent:(VideoCallControlComponent *)component onStartCountingClearScreen:(BOOL)isStart {
    if (isStart) {
        [self startCountingClearScreen];
    } else {
        [self stopCountingClearScreen];
    }
}

#pragma mark - VideoCallComponentDelegate
- (void)videoCallComponentDidCloseNarrow:(id<VideoCallComponent>)component {
    _callViewController = nil;
    [self startCountingClearScreen];
}

#pragma mark - PIPComponentDelegate
- (void)pipComponent:(PIPComponent *)pipComponent willStartWithContentView:(nonnull UIView *)contentView {
    [self.videoComponent startPIPWithView:contentView];
}

- (void)pipComponentDidStopPIP:(PIPComponent *)pipComponent {
    [self.videoComponent stopPIP];
}

#pragma mark - action
- (void)narrowButtonClick {
    _callViewController = self;
    [self.videoComponent becomeNarrow];
    [self stopCountingClearScreen];
}

- (void)videoViewClick {
    if (self.narrowButton.isHidden) {
        self.narrowButton.hidden = NO;
        self.timeLabel.hidden = NO;
        [self.controlComponent updateControlViewHidden:NO];
        [self startCountingClearScreen];
    } else {
        self.narrowButton.hidden = YES;
        self.timeLabel.hidden = YES;
        [self.controlComponent updateControlViewHidden:YES];
    }
}

#pragma mark - NSNotification
- (void)closeVideoCallNotice:(NSNotification *)noti {
    [self hangup];
    [[VideoCallRTCManager shareRtc] destroyRTCVideo];
}

- (void)applicationWillTerminate {
    [self hangup];
}

#pragma mark - private
- (void)setState:(VideoCallState)state {
    _state = state;
    
    self.videoComponent.state = state;
    self.controlComponent.state = state;
    
    if (state == VideoCallStateCalling) {
        // 主叫呼叫中
        [[VideoCallRTCManager shareRtc] startAudioMixing];
        [self callerJoinRoom];
        self.narrowButton.hidden = NO;
        
    } else if (state == VideoCallStateRinging) {
        // 被叫呼叫中
        [[VideoCallRTCManager shareRtc] startAudioMixing];
        self.narrowButton.hidden = YES;
        
    } else if (state == VideoCallStateOnTheCall) {
        // 通话中
        [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"on_the_call")];
        [[VideoCallRTCManager shareRtc] stopAudioMixing];
        self.narrowButton.hidden = NO;
        
        [self startTimer];
        [self.videoView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(videoViewClick)]];
        if (self.infoModel.callType == VideoCallTypeVideo) {
            self.pipComponent = [[PIPComponent alloc] initWithContentView:[self.videoComponent getPIPContentView]];
            self.pipComponent.delegate = self;
        }
        
        [self startCountingClearScreen];
        
    } else {
        [self.timer stopTimer];
        [[VideoCallRTCManager shareRtc] stopAudioMixing];
    }
}

/// 主叫提前进房
- (void)callerJoinRoom {
    __weak typeof(self) weakSelf = self;
    [[VideoCallRTCManager shareRtc] joinRoomWithToken:self.infoModel.token roomID:self.infoModel.roomId uid:[LocalUserComponent userModel].uid complete:^(BOOL success) {
        if (!success) {
            weakSelf.infoModel.status = VideoCallStateRTCError;
            [VideoCallRTSManager updateStatus:VideoCallStateRTCError info:weakSelf.infoModel block:^(RTSACKModel * _Nonnull model) {
                
            }];
            [weakSelf quitViewController];
        }
    }];
}

/// 被叫进房
- (void)joinRTCRoom {
    __weak typeof(self) weakSelf = self;
    [[VideoCallRTCManager shareRtc] joinRoomWithToken:self.infoModel.token roomID:self.infoModel.roomId uid:[LocalUserComponent userModel].uid complete:^(BOOL success) {
        if (success) {
            weakSelf.infoModel.status = VideoCallStateOnTheCall;
            [VideoCallRTSManager updateStatus:VideoCallStateOnTheCall info:weakSelf.infoModel block:^(RTSACKModel * _Nonnull model) {
                if (model.result) {
                    [[VideoCallRTCManager shareRtc] startPublishStream];
                    weakSelf.state = VideoCallStateOnTheCall;
                    
                    if (!weakSelf.remoteUserJoined) {
                        [weakSelf performSelector:@selector(joinRoomTimeout) withObject:nil afterDelay:15];
                    }
                    
                } else {
                    [[ToastComponent shareToastComponent] showWithMessage:model.message delay:0.5];
                    [weakSelf quitViewController];
                }
            }];
        } else {
            weakSelf.infoModel.status = VideoCallStateRTCError;
            [VideoCallRTSManager updateStatus:VideoCallStateRTCError info:weakSelf.infoModel block:^(RTSACKModel * _Nonnull model) {
                
            }];
            [weakSelf quitViewController];
        }
    }];
}

// 被叫进房后15秒，主叫未进房退出
- (void)joinRoomTimeout {
    self.infoModel.status = VideoCallStateTerminated;
    [VideoCallRTSManager updateStatus:VideoCallStateTerminated info:self.infoModel block:^(RTSACKModel * _Nonnull model) {
        
    }];
    [self quitViewController];
}

- (void)startTimer {
    [self.timer stopTimer];
    self.time = 0;
    __weak typeof(self) weakSelf = self;
    [self.timer startTimerWithSpace:1 block:^(BOOL result) {
        weakSelf.time += 1;
        [weakSelf updateTime];
    }];
    [self updateTime];
}

- (void)updateTime {
    NSInteger second = self.time % 60;
    NSInteger minute = self.time / 60;
    NSString *timeString = [NSString stringWithFormat:@"%02ld:%02ld", minute, second];
    if (_callViewController || self.pipComponent.isActive) {
        [self.videoComponent updateTimeString:timeString];
    } else {
        self.timeLabel.text = timeString;
    }
}

- (void)quitViewController {
    [[VideoCallRTCManager shareRtc] leaveRoom];
    
    if (_callViewController) {
        [self.videoComponent hangup];
        _callViewController = nil;
    } else {
        [self dismissViewControllerAnimated:NO completion:nil];
    }
    VideoCallOnTheCallType = 0;
    [self.pipComponent unregisterPiP];
    self.state = VideoCallStateIdle;
}

// 挂断
- (void)hangup {
    if (self.infoModel.status == VideoCallStateTerminated) {
        return;
    }
    if (self.state == VideoCallStateOnTheCall) {
        [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"hang_up_tip") delay:0.5];
        self.infoModel.status = VideoCallStateTerminated;
        [VideoCallRTSManager updateStatus:VideoCallStateTerminated info:self.infoModel block:^(RTSACKModel * _Nonnull model) {
            
        }];
    } else if (self.state == VideoCallStateCalling) {
        self.infoModel.status = VideoCallStateCancelled;
        [VideoCallRTSManager updateStatus:VideoCallStateCancelled info:self.infoModel block:^(RTSACKModel * _Nonnull model) {
            
        }];
    } else if (self.state == VideoCallStateRinging) {
        self.infoModel.status = VideoCallStateRefused;
        [VideoCallRTSManager updateStatus:VideoCallStateRefused info:self.infoModel block:^(RTSACKModel * _Nonnull model) {
            
        }];
    }
    
    [self quitViewController];
}

- (void)startCountingClearScreen {
    if (self.narrowButton.isHidden || self.state != VideoCallStateOnTheCall) {
        return;
    }
    [self stopCountingClearScreen];
    [self performSelector:@selector(videoViewClick) withObject:nil afterDelay:5];
}

- (void)stopCountingClearScreen {
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(videoViewClick) object:nil];
}

#pragma mark - getter
- (UIView *)contentView {
    if (!_contentView) {
        _contentView = [[UIView alloc] init];
    }
    return _contentView;
}

- (UIView *)videoView {
    if (!_videoView) {
        _videoView = [[UIView alloc] init];
    }
    return _videoView;
}

- (VideoCallControlComponent *)controlComponent {
    if (!_controlComponent) {
        _controlComponent = [[VideoCallControlComponent alloc] initWithSuperView:self.view userModel:self.infoModel];
        _controlComponent.delegate = self;
    }
    return _controlComponent;
}

- (id<VideoCallComponent>)videoComponent {
    if (!_videoComponent) {
        if (self.infoModel.callType == VideoCallTypeVideo) {
            _videoComponent = [[VideoCallVideoComponent alloc] initWithSuperView:self.videoView infoModel:self.infoModel];
        } else {
            _videoComponent = [[VideoCallAudioComponent alloc] initWithSuperView:self.videoView infoModel:self.infoModel];
        }
        _videoComponent.delegate = self;
    }
    return _videoComponent;
}

- (UIButton *)narrowButton {
    if (!_narrowButton) {
        _narrowButton = [[UIButton alloc] init];
        [_narrowButton setImage:[UIImage imageNamed:@"narrow" bundleName:HomeBundleName] forState:UIControlStateNormal];
        [_narrowButton addTarget:self action:@selector(narrowButtonClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _narrowButton;
}

- (UILabel *)timeLabel {
    if (!_timeLabel) {
        _timeLabel = [[UILabel alloc] init];
        _timeLabel.font = [UIFont systemFontOfSize:20];
        _timeLabel.textColor = UIColor.whiteColor;
    }
    return _timeLabel;
}

- (GCDTimer *)timer {
    if (!_timer) {
        _timer = [[GCDTimer alloc] init];
    }
    return _timer;
}

#pragma mark - dealloc
- (void)dealloc {
    NSString *topViewControllerName = NSStringFromClass([[DeviceInforTool topViewController] class]);
    if (![topViewControllerName hasPrefix:@"VideoCall"]) {
        [[VideoCallRTCManager shareRtc] destroyRTCVideo];
    }
    
    [UIApplication sharedApplication].idleTimerDisabled = NO;
}

@end
