// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallControlComponent.h"
#import "VideoCallControlView.h"
#import "VideoCallRTCManager.h"
#import "BytedEffectProtocol.h"

@interface VideoCallControlComponent ()<VideoCallControlViewDelegate>

@property (nonatomic, weak) UIView *superView;
@property (nonatomic, strong) VideoCallControlView *controlView;
@property (nonatomic, strong) UILabel *tipLabel;
@property (nonatomic, strong) BytedEffectProtocol *beautyComponent;
@property (nonatomic, strong) VideoCallVoipInfo *infoModel;
@property (nonatomic, assign) BOOL cameraNoPermission;
@property (nonatomic, assign) BOOL hasNetworkToast;

@end

@implementation VideoCallControlComponent

- (instancetype)initWithSuperView:(UIView *)superView userModel:(VideoCallVoipInfo *)infoModel {
    if (self = [super init]) {
        self.superView = superView;
        self.infoModel = infoModel;
        [superView addSubview:self.controlView];
        [superView addSubview:self.tipLabel];
        [self.controlView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.right.bottom.equalTo(superView);
        }];
        [self.tipLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(superView);
            make.bottom.equalTo(self.controlView.mas_top).offset(-10);
        }];
        
        [[VideoCallRTCManager shareRtc] enableLocalAudio:YES];
        [self updateMicState];
        
        if (infoModel.callType == VideoCallTypeVideo) {
            [[VideoCallRTCManager shareRtc] setCameraID:ByteRTCCameraIDFront];
            [self.beautyComponent resume];
            __weak typeof(self) weakSelf = self;
            [SystemAuthority authorizationStatusWithType:AuthorizationTypeCamera block:^(BOOL isAuthorize) {
                if (!isAuthorize) {
                    [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"no_camera_permission")];
                } else {
                    [[VideoCallRTCManager shareRtc] enableLocalVideo:YES];
                }
                weakSelf.cameraNoPermission = !isAuthorize;
                [weakSelf updateCameraState];
            }];
        }
        
        // 设置初始音频路由
        ByteRTCAudioRoute currentRoute = [[VideoCallRTCManager shareRtc] currentDeviceAudioRoute];
        if (currentRoute != ByteRTCAudioRouteHeadset && currentRoute != ByteRTCAudioRouteHeadsetBluetooth) {
            [[VideoCallRTCManager shareRtc] setDeviceAudioRoute:ByteRTCAudioRouteSpeakerphone];
        }
        [self updateAudioRouteButtonState];
    }
    return self;
}

#pragma mark - VideoCallControlViewDelegate
- (void)videoCallControlView:(VideoCallControlView *)controlView didClickControlType:(VideoCallControlType)type button:(nonnull VideoCallControlButton *)button {
    switch (type) {
        case VideoCallControlTypeMic: {
            BOOL isOpen = [self getButtonNextState:button];
            if (!isOpen) {
                [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"local_user_close_mic")];
            }
            [[VideoCallRTCManager shareRtc] enableLocalAudio:isOpen];
        }
            break;
        case VideoCallControlTypeAudioRoute: {
            BOOL isOpen = [self getButtonNextState:button];
            ByteRTCAudioRoute audioRoute = isOpen ? ByteRTCAudioRouteSpeakerphone : ByteRTCAudioRouteEarpiece;
            if (audioRoute == [[VideoCallRTCManager shareRtc] currentDeviceAudioRoute]) {
                return;
            }
            if (isOpen) {
                [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"open_speaker_phone")];
            }
            [[VideoCallRTCManager shareRtc] setDeviceAudioRoute:audioRoute];
        }
            break;
        case VideoCallControlTypeCamera: {
            if (button.type == VideoCallControlButtonTypeNoPermission) {
                // 没有摄像头权限，点击提示开启权限
                [self cameraNoPermissionTip];
                
            } else {
                BOOL isOpen = [self getButtonNextState:button];
                if (!isOpen) {
                    [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"local_user_close_camera")];
                }
                [[VideoCallRTCManager shareRtc] enableLocalVideo:isOpen];
                if ([self.delegate respondsToSelector:@selector(videoCallControlComponent:onCameraEnableChanged:)]) {
                    [self.delegate videoCallControlComponent:self onCameraEnableChanged:isOpen];
                }
                
                [self updateCameraState];
            }
        }
            break;
        case VideoCallControlTypeCameraSwitch: {
            [[VideoCallRTCManager shareRtc] switchCamera];
        }
            break;
        case VideoCallControlTypeBeauty: {
            if (self.beautyComponent) {
                self.controlView.hidden = YES;
                __weak typeof(self) weakSelf = self;
                [self.beautyComponent showInView:self.superView animated:YES dismissBlock:^(BOOL result) {
                    weakSelf.controlView.hidden = NO;
                    [weakSelf startCountingClearScreen:YES];
                }];
            } else {
                [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"not_support_beauty_error")];
            }
        }
            break;
        case VideoCallControlTypeAccept: {
            if ([self.delegate respondsToSelector:@selector(videoCallControlComponentOnAccept:)]) {
                [self.delegate videoCallControlComponentOnAccept:self];
            }
        }
            break;
        case VideoCallControlTypehangUp: {
            if ([self.delegate respondsToSelector:@selector(videoCallControlComponentOnHangup:)]) {
                [self.delegate videoCallControlComponentOnHangup:self];
            }
        }
            break;
            
        default:
            break;
    }
    
    if (type == VideoCallControlTypeBeauty || type == VideoCallControlTypehangUp) {
        [self startCountingClearScreen:NO];
    } else {
        [self startCountingClearScreen:YES];
    }
}

- (BOOL)getButtonNextState:(VideoCallControlButton *)button {
    BOOL isOpen = YES;
    if (button.type == VideoCallControlButtonTypeNormal) {
        button.type = VideoCallControlButtonTypeSelected;
        isOpen = NO;
    } else {
        button.type = VideoCallControlButtonTypeNormal;
        isOpen = YES;
    }
    return isOpen;
}

#pragma mark - private
- (void)linkTimeout {
    if ([self.delegate respondsToSelector:@selector(videoCallControlComponentOnTimeOut:)]) {
        [self.delegate videoCallControlComponentOnTimeOut:self];
    }
}

- (void)startCountingClearScreen:(BOOL)isStart {
    if ([self.delegate respondsToSelector:@selector(videoCallControlComponent:onStartCountingClearScreen:)]) {
        [self.delegate videoCallControlComponent:self onStartCountingClearScreen:isStart];
    }
}

// 更新麦克风按钮状态
- (void)updateMicState {
    VideoCallControlButton *micButton = [self.controlView getButtonWithType:VideoCallControlTypeMic];
    micButton.type = [[VideoCallRTCManager shareRtc] currentLocalAudioEnable] ? VideoCallControlButtonTypeNormal : VideoCallControlButtonTypeSelected;
}

// 更新摄像头按钮状态
- (void)updateCameraState {
    VideoCallControlButton *cameraButton = [self.controlView getButtonWithType:VideoCallControlTypeCamera];
    if (self.cameraNoPermission) {
        // 把图标设置为关闭
        cameraButton.type = VideoCallControlButtonTypeSelected;
        cameraButton.type = VideoCallControlButtonTypeNoPermission;
    } else {
        cameraButton.type = [[VideoCallRTCManager shareRtc] currentLocalVideoEnable] ? VideoCallControlButtonTypeNormal : VideoCallControlButtonTypeSelected;
    }
    VideoCallControlButton *switchCameraButton = [self.controlView getButtonWithType:VideoCallControlTypeCameraSwitch];
    switchCameraButton.enabled = (cameraButton.type == VideoCallControlButtonTypeNormal);
    VideoCallControlButton *beautyButton = [self.controlView getButtonWithType:VideoCallControlTypeBeauty];
    beautyButton.enabled = (cameraButton.type == VideoCallControlButtonTypeNormal);
}

// 更新音频路由按钮状态
- (void)updateAudioRouteButtonState {
    VideoCallControlButton *audioRouteButton = [self.controlView getButtonWithType:VideoCallControlTypeAudioRoute];
    ByteRTCAudioRoute audioRoute = [[VideoCallRTCManager shareRtc] currentDeviceAudioRoute];
    if (audioRoute == ByteRTCAudioRouteHeadset || audioRoute == ByteRTCAudioRouteHeadsetBluetooth) {
        audioRouteButton.enabled = NO;
    } else if (audioRoute == ByteRTCAudioRouteSpeakerphone) {
        audioRouteButton.enabled = YES;
        audioRouteButton.type = VideoCallControlButtonTypeNormal;
    } else if (audioRoute == ByteRTCAudioRouteEarpiece) {
        audioRouteButton.enabled = YES;
        audioRouteButton.type = VideoCallControlButtonTypeSelected;
    }
}

// 没有摄像头权限提示
- (void)cameraNoPermissionTip {
    AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
    alertCancelModel.title = LocalizedString(@"cancel");
    AlertActionModel *alertModel = [[AlertActionModel alloc] init];
    alertModel.title = LocalizedString(@"ok");
    alertModel.alertModelClickBlock = ^(UIAlertAction * _Nonnull action) {
        [SystemAuthority autoJumpWithAuthorizationStatusWithType:AuthorizationTypeCamera];
    };
    [[AlertActionManager shareAlertActionManager] showWithMessage:LocalizedString(@"camera_permission_disabled") actions:@[alertCancelModel, alertModel]];
}

#pragma mark - public
- (void)setState:(VideoCallState)state {
    _state = state;
    
    [self.controlView setupViewWithState:state callType:self.infoModel.callType];
    self.tipLabel.text = [self.controlView getTipMessage];
    
    if (state == VideoCallStateCalling || state == VideoCallStateRinging) {
        [self performSelector:@selector(linkTimeout) withObject:nil afterDelay:60];
    } else {
        [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(linkTimeout) object:nil];
    }
}

- (void)audioRouteChanged:(ByteRTCAudioRoute)audioRoute {
    [self updateAudioRouteButtonState];
}

- (void)updateControlViewHidden:(BOOL)isHidden {
    self.controlView.hidden = isHidden;
}

- (void)updateNetworkMessage:(NSString *)message qualityVeryBad:(BOOL)isVeryBad {
    self.tipLabel.text = message;
    if (isVeryBad) {
        VideoCallControlButton *cameraButton = [self.controlView getButtonWithType:VideoCallControlTypeCamera];
        if (!self.hasNetworkToast && self.infoModel.callType == VideoCallTypeVideo && cameraButton.type == VideoCallControlButtonTypeNormal) {
            [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"network_bad_close_camera")];
        }
        self.hasNetworkToast = YES;
    } else {
        self.hasNetworkToast = NO;
    }
}

#pragma mark - getter
- (VideoCallControlView *)controlView {
    if (!_controlView) {
        _controlView = [[VideoCallControlView alloc] init];
        _controlView.delegate = self;
    }
    return _controlView;
}

- (BytedEffectProtocol *)beautyComponent {
    if (!_beautyComponent) {
        _beautyComponent = [[BytedEffectProtocol alloc] initWithRTCEngineKit:[VideoCallRTCManager shareRtc].rtcEngineKit type:BytedEffectTypeVideoCall];
    }
    return _beautyComponent;
}

- (UILabel *)tipLabel {
    if (!_tipLabel) {
        _tipLabel = [[UILabel alloc] init];
        _tipLabel.font = [UIFont systemFontOfSize:16];
        _tipLabel.textColor = [UIColor.whiteColor colorWithAlphaComponent:0.8];
    }
    return _tipLabel;
}

@end
