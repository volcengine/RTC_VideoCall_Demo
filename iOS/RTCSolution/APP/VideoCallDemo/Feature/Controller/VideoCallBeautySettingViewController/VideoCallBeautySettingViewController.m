// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallBeautySettingViewController.h"
#import "BytedEffectProtocol.h"
#import "VideoCallRTCManager.h"

@interface VideoCallBeautySettingViewController ()

@property (nonatomic, strong) UIView *videoView;
@property (nonatomic, strong) UIButton *closeButton;
@property (nonatomic, strong) BytedEffectProtocol *beautyComponent;

@end

@implementation VideoCallBeautySettingViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setupViews];
    
    [[VideoCallRTCManager shareRtc] startRenderLocalVideo:self.videoView];
    [self.beautyComponent resume];
    [self.beautyComponent showInView:self.view animated:NO dismissBlock:^(BOOL result) {
        
    }];
    __weak typeof(self) weakSelf = self;
    [SystemAuthority authorizationStatusWithType:AuthorizationTypeCamera block:^(BOOL isAuthorize) {
        if (isAuthorize) {
            [[VideoCallRTCManager shareRtc] enableLocalVideo:YES];
        } else {
            [weakSelf cameraNoPermissionTip];
        }
    }];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.beautyComponent saveBeautyConfig];
}

- (void)setupViews {
    self.view.backgroundColor = [UIColor colorFromHexString:@"#1E1E1E"];
    [self.view addSubview:self.videoView];
    [self.view addSubview:self.closeButton];
    
    [self.videoView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.view);
    }];
    [self.closeButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.view).offset(5);
        make.top.equalTo(self.view).offset([DeviceInforTool getStatusBarHight]);
        make.size.mas_equalTo(CGSizeMake(44, 44));
    }];
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

#pragma mark - actions

- (void)closeButtonClick {
    [self.navigationController popViewControllerAnimated:YES];
    [[VideoCallRTCManager shareRtc] enableLocalVideo:NO];
}

#pragma mark - getter
- (UIView *)videoView {
    if (!_videoView) {
        _videoView = [[UIView alloc] init];
    }
    return _videoView;
}

- (UIButton *)closeButton {
    if (!_closeButton) {
        _closeButton = [[UIButton alloc] init];
        [_closeButton setImage:[UIImage imageNamed:@"close_room_icon" bundleName:HomeBundleName] forState:UIControlStateNormal];
        [_closeButton addTarget:self action:@selector(closeButtonClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _closeButton;
}

- (BytedEffectProtocol *)beautyComponent {
    if (!_beautyComponent) {
        _beautyComponent = [[BytedEffectProtocol alloc] initWithRTCEngineKit:[VideoCallRTCManager shareRtc].rtcEngineKit type:BytedEffectTypeVideoCall];
    }
    return _beautyComponent;
}

@end
