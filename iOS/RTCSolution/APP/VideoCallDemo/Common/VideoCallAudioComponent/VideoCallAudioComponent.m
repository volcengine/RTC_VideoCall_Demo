// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallAudioComponent.h"
#import "VideoCallAudioView.h"
#import "VideoCallAvatarView.h"
#import "VideoCallRTCManager.h"
#import "VideoCallViewController.h"
#import "VideoCallNarrowWindow.h"

@interface VideoCallAudioComponent ()

@property (nonatomic, weak) UIView *superView;
@property (nonatomic, strong) VideoCallAudioView *audioView;
@property (nonatomic, strong) VideoCallAvatarView *avatarView;

@property (nonatomic, strong) VideoCallVoipInfo *infoModel;
@property (nonatomic, strong) UITapGestureRecognizer *tapGesture;
@property (nonatomic, strong) VideoCallNarrowWindow *narrowWindow;

@end


@implementation VideoCallAudioComponent
@synthesize delegate = _delegate;
@synthesize state = _state;

- (instancetype)initWithSuperView:(UIView *)superView infoModel:(nonnull VideoCallVoipInfo *)infoModel {
    if (self = [super init]) {
        self.superView = superView;
        self.infoModel = infoModel;
        [superView addSubview:self.audioView];
        [superView addSubview:self.avatarView];
        [self.avatarView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(superView).offset(148 - 44 + [DeviceInforTool getStatusBarHight]);
            make.left.right.equalTo(superView);
            make.height.mas_equalTo(156);
        }];
        self.avatarView.name = [infoModel showUserName];
    }
    return self;
}

#pragma mark - action
- (void)narrowViewTouch {
    [self.narrowWindow closeNarrowWindow:^(BOOL finished) {
        self.audioView.contentView.hidden = YES;
        self.tapGesture.enabled = NO;
        [[DeviceInforTool topViewController] presentViewController:[VideoCallViewController currentController] animated:NO completion:nil];
        [self.superView insertSubview:self.audioView atIndex:0];
        if ([self.delegate respondsToSelector:@selector(videoCallComponentDidCloseNarrow:)]) {
            [self.delegate videoCallComponentDidCloseNarrow:self];
        }
    }];
}

#pragma mark - VideoCallComponent
- (void)becomeNarrow {
    CGRect desFrame = CGRectMake(SCREEN_WIDTH - 103, 51 + [DeviceInforTool getStatusBarHight], 88, 88);
    [self.narrowWindow becomeNarrowWindow:self.audioView
                                 desFrame:desFrame
                                 complete:^(BOOL finished) {
        self.audioView.animation = (self.state == VideoCallStateCalling || self.state == VideoCallStateRinging);
        self.audioView.contentView.hidden = NO;
    }];
    self.tapGesture.enabled = YES;
    [[VideoCallViewController currentController] dismissViewControllerAnimated:NO completion:nil];
}

- (void)updateTimeString:(NSString *)timeStr {
    [self.audioView updateText:timeStr];
}

- (void)hangup {
    [self.narrowWindow removeFromSuperview];
    [self.audioView removeFromSuperview];
}

// video
- (void)updateUserCamera:(BOOL)enable isLocalUser:(BOOL)isLocalUser {
    
}

- (void)startRenderRemoteView:(NSString *)userId {
    
}

// PIP
- (UIView *)getPIPContentView {
    return self.audioView;
}

- (void)startPIPWithView:(UIView *)view {
    
}

- (void)stopPIP {
    
}

#pragma mark - public
- (void)setState:(VideoCallState)state {
    _state = state;
    
    if (state == VideoCallStateCalling || state == VideoCallStateRinging) {
        [self.avatarView mas_updateConstraints:^(MASConstraintMaker *make) {
            make.height.mas_equalTo(184);
        }];
        self.avatarView.animation = YES;
    } else {
        [self.avatarView mas_updateConstraints:^(MASConstraintMaker *make) {
            make.height.mas_equalTo(156);
        }];
        self.avatarView.animation = NO;
        self.audioView.animation = NO;
        [self.audioView updateText:@""];
    }
}

#pragma mark - getter
- (VideoCallAudioView *)audioView {
    if (!_audioView) {
        _audioView = [[VideoCallAudioView alloc] initWithFrame:UIScreen.mainScreen.bounds];
        [_audioView updateUserName:[self.infoModel showUserName]];
        [_audioView addGestureRecognizer:self.tapGesture];
    }
    return _audioView;
}

- (UITapGestureRecognizer *)tapGesture {
    if (!_tapGesture) {
        _tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(narrowViewTouch)];
        _tapGesture.enabled = NO;
    }
    return _tapGesture;
}

- (VideoCallAvatarView *)avatarView {
    if (!_avatarView) {
        _avatarView = [[VideoCallAvatarView alloc] init];
    }
    return _avatarView;
}

- (VideoCallNarrowWindow *)narrowWindow {
    if (!_narrowWindow) {
        _narrowWindow = [[VideoCallNarrowWindow alloc] init];
    }
    return _narrowWindow;
}

@end
