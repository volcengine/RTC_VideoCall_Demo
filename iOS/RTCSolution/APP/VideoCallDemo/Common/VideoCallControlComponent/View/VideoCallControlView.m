// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallControlView.h"
#import "VideoCallRTCManager.h"

@interface VideoCallControlView ()

@property (nonatomic, strong) UIView *topView;
@property (nonatomic, strong) UIView *bottomView;
@property (nonatomic, strong) VideoCallControlButton *telephoneButton;

@property (nonatomic, strong) NSMutableDictionary<NSNumber *, VideoCallControlButton *> *dict;
@property (nonatomic, copy) NSString *message;

@end

@implementation VideoCallControlView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self addSubview:self.topView];
        [self addSubview:self.bottomView];
        [self.bottomView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(self).offset(-32 - [DeviceInforTool getVirtualHomeHeight]);
            make.centerX.equalTo(self);
            make.height.mas_equalTo(88);
            make.width.mas_equalTo(0);
        }];
        [self.topView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(self.bottomView.mas_top).offset(-40);
            make.centerX.equalTo(self);
            make.height.mas_equalTo(88);
            make.width.mas_equalTo(0);
            make.top.equalTo(self);
        }];
    }
    return self;
}

- (void)setupViewWithState:(VideoCallState)state callType:(VideoCallType)callType {
    self.message = @"";
    
    NSArray *topButtons = nil;
    NSArray *bottomButtons = nil;
    if (callType == VideoCallTypeAudio) {
        
        if (state == VideoCallStateCalling || state == VideoCallStateOnTheCall) {
            // 主叫  通话中
            topButtons = @[@(VideoCallControlTypeMic), @(VideoCallControlTypeAudioRoute)];
            bottomButtons = @[@(VideoCallControlTypehangUp)];
            if (state == VideoCallStateCalling) {
                self.message = LocalizedString(@"calling_wait_accept");
            }
        } else if (state == VideoCallStateRinging) {
            // 被叫
            topButtons = @[@(VideoCallControlTypeMic), @(VideoCallControlTypeAudioRoute)];
            bottomButtons = @[@(VideoCallControlTypehangUp), @(VideoCallControlTypeAccept)];
            self.message = LocalizedString(@"called_audio_wait_accept");
        }
        
        
    } else {
        if (state == VideoCallStateCalling) {
            // 主叫
            self.message = LocalizedString(@"calling_wait_accept");
            topButtons = @[@(VideoCallControlTypeCamera), @(VideoCallControlTypeCameraSwitch)];
            bottomButtons = @[@(VideoCallControlTypehangUp)];
        } else if (state == VideoCallStateRinging) {
            // 被叫
            topButtons = @[@(VideoCallControlTypeCamera), @(VideoCallControlTypeCameraSwitch)];
            bottomButtons = @[@(VideoCallControlTypehangUp), @(VideoCallControlTypeAccept)];
            self.message = LocalizedString(@"called_video_wait_accept");
        } else if (state == VideoCallStateOnTheCall) {
            // 通话中
            topButtons = @[@(VideoCallControlTypeCamera), @(VideoCallControlTypeMic), @(VideoCallControlTypeAudioRoute)];
            bottomButtons = @[@(VideoCallControlTypeBeauty), @(VideoCallControlTypehangUp), @(VideoCallControlTypeCameraSwitch)];
        }
    }
    [self updateView:self.topView buttonTypes:topButtons];
    [self updateView:self.bottomView buttonTypes:bottomButtons];
}

- (void)updateView:(UIView *)view buttonTypes:(NSArray<NSNumber *> *)array {
    [view.subviews makeObjectsPerformSelector:@selector(removeFromSuperview)];
    [view mas_updateConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo(array.count * 64 + (array.count - 1) * 44);
    }];
    for (int i = 0; i < array.count; i++) {
        VideoCallControlButton *button = [self getButtonWithType:[array[i] integerValue]];
        [view addSubview:button];
        [button mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(view);
            make.left.equalTo(view).offset(108*i);
            make.width.mas_equalTo(64);
        }];
    }
}

- (VideoCallControlButton *)createButtonWithType:(VideoCallControlType)type {
    VideoCallControlButton *button = nil;
    switch (type) {
        case VideoCallControlTypeAccept: {
            button = [[VideoCallControlButton alloc] initWithNormalImage:@"video_call_accept" normalTitle:@"" selectedImage:@"video_call_accept" selectedTitle:@""];
        }
            break;
        case VideoCallControlTypehangUp: {
            button = [[VideoCallControlButton alloc] initWithNormalImage:@"video_call_hangup" normalTitle:@"" selectedImage:@"video_call_hangup" selectedTitle:@""];
        }
            break;
        case VideoCallControlTypeMic: {
            button = [[VideoCallControlButton alloc] initWithNormalImage:@"mic_on" normalTitle:LocalizedString(@"microphone") selectedImage:@"mic_off" selectedTitle:LocalizedString(@"microphone")];
        }
            break;
        case VideoCallControlTypeAudioRoute: {
            button = [[VideoCallControlButton alloc] initWithNormalImage:@"video_call_lounds_speak" normalTitle:LocalizedString(@"lound_speaker") selectedImage:@"video_call_telephone" selectedTitle:LocalizedString(@"telephone")];
        }
            break;
        case VideoCallControlTypeCamera: {
            button = [[VideoCallControlButton alloc] initWithNormalImage:@"camera_on" normalTitle:LocalizedString(@"camera") selectedImage:@"camera_off" selectedTitle:LocalizedString(@"camera")];
        }
            break;
        case VideoCallControlTypeCameraSwitch: {
            button = [[VideoCallControlButton alloc] initWithNormalImage:@"camera_switch" normalTitle:LocalizedString(@"camera_switch") selectedImage:@"camera_switch" selectedTitle:LocalizedString(@"camera_switch")];
        }
            break;
        case VideoCallControlTypeBeauty: {
            button = [[VideoCallControlButton alloc] initWithNormalImage:@"call_linked_beauty" normalTitle:LocalizedString(@"beauty") selectedImage:@"call_linked_beauty" selectedTitle:LocalizedString(@"beauty")];
        }
            break;
            
        default:
            break;
    }
    button.tag = type;
    [button addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
    
    return button;
}

#pragma mark - action
- (void)buttonClick:(VideoCallControlButton *)button {
    if ([self.delegate respondsToSelector:@selector(videoCallControlView:didClickControlType:button:)]) {
        [self.delegate videoCallControlView:self didClickControlType:button.tag button:button];
    }
}

#pragma mark - public

- (VideoCallControlButton *)getButtonWithType:(VideoCallControlType)type {
    VideoCallControlButton *button = self.dict[@(type)];
    if (!button) {
        button = [self createButtonWithType:type];
        [self.dict setObject:button forKey:@(type)];
    }
    return button;
}

- (NSString *)getTipMessage {
    return self.message;
}

#pragma mark - getter

- (UIView *)topView {
    if (!_topView) {
        _topView = [[UIView alloc] init];
    }
    return _topView;
}

- (UIView *)bottomView {
    if (!_bottomView) {
        _bottomView = [[UIView alloc] init];
    }
    return _bottomView;
}

- (NSMutableDictionary<NSNumber *,VideoCallControlButton *> *)dict {
    if (!_dict) {
        _dict = [NSMutableDictionary dictionary];
    }
    return _dict;
}

@end
