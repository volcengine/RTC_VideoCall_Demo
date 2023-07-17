// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallVideoComponent.h"
#import "VideoCallRTCManager.h"
#import "VideoCallAvatarView.h"
#import "VideoCallRenderView.h"
#import "VideoCallViewController.h"
#import "VideoCallPIPView.h"
#import "VideoCallNarrowWindow.h"

@interface VideoCallVideoComponent ()<VideoCallRenderViewDelegate>

@property (nonatomic, weak) UIView *superView;
@property (nonatomic, strong) VideoCallRenderView *localView;
@property (nonatomic, strong) VideoCallRenderView *remoteView;
@property (nonatomic, strong) VideoCallPIPView *pipView;
@property (nonatomic, strong) VideoCallAvatarView *avatarView;
@property (nonatomic, strong) VideoCallVoipInfo *infoModel;
@property (nonatomic, assign) BOOL isNarrow;
@property (nonatomic, strong) VideoCallNarrowWindow *narrowWindow;

@end

@implementation VideoCallVideoComponent
@synthesize delegate = _delegate;
@synthesize state = _state;

- (instancetype)initWithSuperView:(UIView *)superView infoModel:(nonnull VideoCallVoipInfo *)infoModel {
    if (self = [super init]) {
        self.superView = superView;
        self.infoModel = infoModel;
    
        [superView addSubview:self.localView];
        [superView addSubview:self.remoteView];
        [superView addSubview:self.avatarView];
        [self.localView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self.localView.superview);
        }];
        [self.remoteView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(self.remoteView.superview).offset(-20);
            make.top.equalTo(self.remoteView.superview).offset(22 + [DeviceInforTool getStatusBarHight]);
            make.size.mas_equalTo(CGSizeMake(90, 129));
        }];
    
        [self.avatarView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(superView).offset(104 + [DeviceInforTool getStatusBarHight]);
            make.left.right.equalTo(superView);
            make.height.mas_equalTo(184);
        }];
        self.avatarView.name = [self.infoModel showUserName];
        self.localView.name = [LocalUserComponent userModel].name;
        self.remoteView.name = [self.infoModel showUserName];
        
        [[VideoCallRTCManager shareRtc] startRenderLocalVideo:self.localView.renderView];
    }
    return self;
}

#pragma mark - VideoCallComponent

- (void)setState:(VideoCallState)state {
    _state = state;
    
    if (state == VideoCallStateCalling || state == VideoCallStateRinging) {
        self.avatarView.hidden = NO;
        self.avatarView.animation = YES;
        self.remoteView.hidden = YES;
    } else {
        self.avatarView.hidden = YES;
        self.avatarView.animation = NO;
        self.remoteView.hidden = NO;
    }
}

- (void)becomeNarrow {
    self.isNarrow = YES;
    
    VideoCallRenderView *narrowView = nil;
    if (self.state == VideoCallStateOnTheCall) {
        [self setFullRenderView:self.remoteView];
        narrowView = self.remoteView;
        [narrowView updateTimeString:@""];
    } else {
        narrowView = self.localView;
    }
    
    CGRect desFrame = CGRectMake(SCREEN_WIDTH - 90 - 20, 22 + [DeviceInforTool getStatusBarHight], 90, 129);
    [self.narrowWindow becomeNarrowWindow:narrowView
                                 desFrame:desFrame
                                 complete:^(BOOL finished) {
        [narrowView updateTimeLablehidden:NO];
    }];
    [[VideoCallViewController currentController] dismissViewControllerAnimated:NO completion:nil];
}

- (void)updateTimeString:(NSString *)timeStr {
    [self.remoteView updateTimeString:timeStr];
    [self.pipView updateTimeString:timeStr];
}

- (void)hangup {
    [self.narrowWindow removeFromSuperview];
    [self.localView removeFromSuperview];
    [self.remoteView removeFromSuperview];
}

#pragma mark - Video
- (void)updateUserCamera:(BOOL)enable isLocalUser:(BOOL)isLocalUser {
    if (isLocalUser) {
        self.localView.isEnableVideo = enable;
    } else {
        self.remoteView.isEnableVideo = enable;
        self.pipView.isEnableVideo = enable;
    }
}

- (void)startRenderRemoteView:(NSString *)userId {
    self.remoteView.renderView.hidden = NO;
    [[VideoCallRTCManager shareRtc] startRenderRemoteVideo:self.remoteView.renderView userID:userId];
    if (self.isNarrow) {
        [self.narrowWindow addSubview:self.remoteView];
        [self.remoteView mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self.narrowWindow);
        }];
        [self.remoteView updateTimeLablehidden:NO];
        [self.superView addSubview:self.localView];
    } else {
        [self setFullRenderView:self.remoteView];
    }
}

#pragma mark - PIP

- (UIView *)getPIPContentView {
    return self.remoteView;
}

- (void)startPIPWithView:(UIView *)view {
    // 内部渲染画中画
//    [view addSubview:self.remoteView];
//    [self.remoteView mas_remakeConstraints:^(MASConstraintMaker *make) {
//        make.edges.equalTo(view);
//    }];
//    [self.remoteView.superview layoutIfNeeded];
//    [self.remoteView updateTimeLablehidden:NO];
    
    // 外部渲染画中画
    if (self.pipView) {
        [self.pipView removeFromSuperview];
        self.pipView = nil;
    }
    self.pipView = [[VideoCallPIPView alloc] init];
    self.pipView.name = [self.infoModel showUserName];
    self.pipView.isEnableVideo = self.remoteView.isEnableVideo;
    [self.pipView startPIPWithInfoModel:self.infoModel];
    [view addSubview:self.pipView];
    [self.pipView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(view);
    }];
}

- (void)stopPIP {
    // 内部渲染画中画
//    [self.superView addSubview:self.localView];
//    [self.superView addSubview:self.remoteView];
//
//    [self setFullRenderView:self.remoteView];
//    [self.remoteView updateTimeLablehidden:YES];
    
    // 外部渲染画中画
    if (self.pipView) {
        [self.pipView stopPIP];
        [self.pipView removeFromSuperview];
        self.pipView = nil;
    }
}

#pragma mark - VideoCallRenderViewDelegate

- (void)videoCallRenderViewOnTouched:(VideoCallRenderView *)view {
    if (self.isNarrow) {
        // 小窗模式，关闭小窗
        [self closeNarrowView:view];
    } else {
        // 大小窗切换
        [self setFullRenderView:view];
    }
}

- (void)closeNarrowView:(VideoCallRenderView *)view {
    [self.localView updateTimeLablehidden:YES];
    [self.remoteView updateTimeLablehidden:YES];
    self.isNarrow = NO;
    
    [self.narrowWindow closeNarrowWindow:^(BOOL finished) {
        [[DeviceInforTool topViewController] presentViewController:[VideoCallViewController currentController] animated:NO completion:nil];
        [self.superView insertSubview:view atIndex:0];
        [view mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self.superView);
        }];
        [self setFullRenderView:view];
        if ([self.delegate respondsToSelector:@selector(videoCallComponentDidCloseNarrow:)]) {
            [self.delegate videoCallComponentDidCloseNarrow:self];
        }
    }];
}

#pragma mark - private

- (void)setFullRenderView:(VideoCallRenderView *)view {
    [view mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(view.superview);
    }];
    UIView *narrowView = self.localView == view ? self.remoteView : self.localView;
    [narrowView mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(narrowView.superview).offset(-20);
        make.top.equalTo(narrowView.superview).offset(56 - 44 + [DeviceInforTool getStatusBarHight]);
        make.size.mas_equalTo(CGSizeMake(90, 129));
    }];
    [self.superView bringSubviewToFront:narrowView];
    [self.superView layoutIfNeeded];
}

#pragma mark - getter
- (VideoCallRenderView *)localView {
    if (!_localView) {
        _localView = [[VideoCallRenderView alloc] initWithFrame:UIScreen.mainScreen.bounds];
        _localView.delegate = self;
    }
    return _localView;
}

- (VideoCallRenderView *)remoteView {
    if (!_remoteView) {
        _remoteView = [[VideoCallRenderView alloc] init];
        _remoteView.delegate = self;
        _remoteView.renderView.hidden = YES;
    }
    return _remoteView;
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
