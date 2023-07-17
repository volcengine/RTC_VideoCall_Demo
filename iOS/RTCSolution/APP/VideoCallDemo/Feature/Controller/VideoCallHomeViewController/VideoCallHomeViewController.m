// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallHomeViewController.h"
#import "VideoCallBeautySettingViewController.h"
#import "VideoCallRTCManager.h"
#import "VideoCallBeautySettingTipView.h"
#import "VideoCallSearchView.h"
#import "VideoCallUserListView.h"
#import "VideoCallRTSManager.h"
#import "VideoCallViewController.h"
#import "VideoCallUserModel.h"

@interface VideoCallHomeViewController ()<VideoCallUserListViewDelegate, VideoCallSearchViewDelegate>

@property (nonatomic, strong) UIView *navView;
@property (nonatomic, strong) UILabel *userIDLabel;
@property (nonatomic, strong) VideoCallSearchView *searchView;
@property (nonatomic, strong) VideoCallUserListView *userListView;
@property (nonatomic, strong) NSArray<VideoCallUserModel *> *userArray;
@property (nonatomic, strong) VideoCallUserModel *preUserModel;
@property (nonatomic, strong) VideoCallBeautySettingTipView *tipView;

@end

@implementation VideoCallHomeViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setupViews];
    
    self.userArray = [VideoCallUserModel getCallHistory];
    self.userListView.dataArray = self.userArray;
}

- (void)setupViews {
    self.view.backgroundColor = [UIColor colorFromHexString:@"#1E1E1E"];
    
    [self.view addSubview:self.navView];
    [self.view addSubview:self.tipView];
    [self.view addSubview:self.userIDLabel];
    [self.view addSubview:self.searchView];
    [self.view addSubview:self.userListView];
    
    [self.navView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.top.equalTo(self.view);
        make.height.mas_equalTo(44 + [DeviceInforTool getStatusBarHight]);
    }];
    [self.tipView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.navView.mas_bottom);
        make.left.right.equalTo(self.view);
    }];
    [self.searchView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self.view);
        make.top.equalTo(self.tipView.mas_bottom).offset(12);
    }];
    [self.userIDLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view);
        make.top.equalTo(self.searchView.mas_bottom).offset(12);
    }];
    [self.userListView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.bottom.equalTo(self.view);
        make.top.equalTo(self.userIDLabel.mas_bottom).offset(12);
    }];
}

#pragma mark - VideoCallUserListViewDelegate
- (void)videoCallUserListView:(VideoCallUserListView *)userListView didClickUser:(VideoCallUserModel *)userData {
    // 网络不可用提示
    if (![[VideoCallRTCManager shareRtc] networkAvailable]) {
        [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"network_not_available_tip")];
        return;
    }
    // 不能给自己打电话
    if ([userData.uid isEqualToString:[LocalUserComponent userModel].uid]) {
        [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"call_self_tip")];
        return;
    }
    self.preUserModel = userData;
    
    if ([VideoCallViewController currentController]) {
        [self initiateCallDuringCall];
    } else {
        [self selectVideoCallType];
    }
}

// 通话中发起通话
- (void)initiateCallDuringCall {
    __weak typeof(self) weakSelf = self;
    AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
    alertCancelModel.title = LocalizedString(@"cancel");
    AlertActionModel *alertModel = [[AlertActionModel alloc] init];
    alertModel.title = LocalizedString(@"confirm");
    alertModel.alertModelClickBlock = ^(UIAlertAction * _Nonnull action) {
        [[VideoCallViewController currentController] hangup];
        [weakSelf selectVideoCallType];
    };
    [[AlertActionManager shareAlertActionManager] showWithMessage:[self getOnTheCallTip] actions:@[alertCancelModel, alertModel]];
}

- (void)selectVideoCallType {
    __weak typeof(self) weakSelf = self;
    UIAlertController *sheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:LocalizedString(@"cancel") style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        
    }];
    
    UIAlertAction *videoAction = [UIAlertAction actionWithTitle:LocalizedString(@"call_video_title") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        weakSelf.preUserModel.callType = VideoCallTypeVideo;
        [weakSelf checkAudioPermission];
    }];
    
    UIAlertAction *audioAction = [UIAlertAction actionWithTitle:LocalizedString(@"call_audio_title") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        weakSelf.preUserModel.callType = VideoCallTypeAudio;
        [weakSelf checkAudioPermission];
    }];
    [sheet addAction:cancelAction];
    [sheet addAction:videoAction];
    [sheet addAction:audioAction];
    [self presentViewController:sheet animated:YES completion:nil];
}

- (void)checkAudioPermission {
    __weak typeof(self) weakSelf = self;
    [SystemAuthority authorizationStatusWithType:AuthorizationTypeAudio block:^(BOOL isAuthorize) {
        if (isAuthorize) {
            [weakSelf callingToUser];
        } else {
            [weakSelf permissionDenialTip:AuthorizationTypeAudio];
        }
    }];
}

- (void)permissionDenialTip:(AuthorizationType)type {
    NSString *message = (type == AuthorizationTypeAudio) ? LocalizedString(@"no_microphone_permission") : LocalizedString(@"no_camera_permission");;
    AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
    alertCancelModel.title = LocalizedString(@"confirm");
    [[AlertActionManager shareAlertActionManager] showWithMessage:message actions:@[alertCancelModel]];
}

- (void)callingToUser {
    __weak typeof(self) weakSelf = self;
    [VideoCallRTSManager callUser:self.preUserModel block:^(BOOL success, VideoCallVoipInfo * _Nonnull info, NSString * _Nonnull message) {
        if (success) {
            // 拨打成功开始响铃
            info.toUserName = weakSelf.preUserModel.name;
            [VideoCallRTSManager jumpToVideoCallViewController:info currentViewController:weakSelf];
            
            // 添加通话记录
            [weakSelf addUserHistoryList:weakSelf.preUserModel];
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:message];
        }
    }];
}

#pragma mark - VideoCallSearchViewDelegate
- (void)videoCallSearchView:(VideoCallSearchView *)searchView didSearchUser:(NSString *)userID {
    __weak typeof(self) weakSelf = self;
    [VideoCallRTSManager searchUser:userID block:^(NSArray<VideoCallUserModel *> * _Nonnull userList, NSString * _Nonnull errorMessage) {
        if (userList.count > 0) {
            weakSelf.userListView.dataArray = userList;
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:errorMessage];
        }
    }];
}

- (void)videoCallSearchViewDidClearSearch:(VideoCallSearchView *)searchView {
    self.userListView.dataArray = self.userArray;
}

#pragma mark - actions
- (void)navBackAction:(UIButton *)sender {
    // 不在通话中退出，销毁RTC
    if (![VideoCallViewController currentController]) {
        [[VideoCallRTCManager shareRtc] destroyRTCVideo];
    }
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)rightButtonAction:(UIButton *)sender {
    if ([VideoCallViewController currentController]) {
        __weak typeof(self) weakSelf = self;
        AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
        alertCancelModel.title = LocalizedString(@"cancel");
        AlertActionModel *alertModel = [[AlertActionModel alloc] init];
        alertModel.title = LocalizedString(@"confirm");
        alertModel.alertModelClickBlock = ^(UIAlertAction * _Nonnull action) {
            [[VideoCallViewController currentController] hangup];
            VideoCallBeautySettingViewController *ctrl = [[VideoCallBeautySettingViewController alloc] init];
            [weakSelf.navigationController pushViewController:ctrl animated:YES];
        };
        [[AlertActionManager shareAlertActionManager] showWithMessage:[self getOnTheCallTip] actions:@[alertCancelModel, alertModel]];
    } else {
        VideoCallBeautySettingViewController *ctrl = [[VideoCallBeautySettingViewController alloc] init];
        [self.navigationController pushViewController:ctrl animated:YES];
    }
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [super touchesBegan:touches withEvent:event];
    [self.searchView endEditing:YES];
}

- (void)userListViewClick {
    [self.searchView endEditing:YES];
}

#pragma mark - private

// 添加历史通话记录
- (void)addUserHistoryList:(VideoCallUserModel *)userModel {
    NSMutableArray *array = self.userArray.mutableCopy;
    if (array == nil) {
        array = [NSMutableArray array];
    }
    [array insertObject:userModel atIndex:0];
    if (array.count > 10) {
        [array removeObjectsInRange:NSMakeRange(10, array.count - 10)];
    }
    self.userArray = array.copy;
    self.userListView.dataArray = self.userArray;
    [VideoCallUserModel saveCallHistory:array];
}

- (NSString *)getOnTheCallTip {
    NSString *message = @"";
    if ([VideoCallViewController currentController].infoModel.callType == VideoCallTypeAudio) {
        message = LocalizedString(@"video_call_in_audio_calling_tip");
    } else if ([VideoCallViewController currentController].infoModel.callType == VideoCallTypeVideo) {
        message = LocalizedString(@"video_call_in_video_calling_tip");
    }
    return message;
}

#pragma mark - getter
- (UIView *)navView {
    if (!_navView) {
        _navView = [[UIView alloc] init];
        UILabel *titleLabel = [[UILabel alloc] init];
        titleLabel.font = [UIFont systemFontOfSize:17];
        titleLabel.textColor = [UIColor whiteColor];
        titleLabel.text = LocalizedString(@"audio_video_calls");
        UIButton *leftButton = [[UIButton alloc] init];
        [leftButton setImage:[UIImage imageNamed:@"nav_left" bundleName:@"ToolKit"] forState:UIControlStateNormal];
        [leftButton addTarget:self action:@selector(navBackAction:) forControlEvents:UIControlEventTouchUpInside];
        [leftButton setImageEdgeInsets:UIEdgeInsetsMake(11, 11, 11, 11)];
        UIButton *rightButton = [[UIButton alloc] init];
        [rightButton setImage:[UIImage imageNamed:@"beauty_setting_entrance" bundleName:HomeBundleName] forState:UIControlStateNormal];
        [rightButton addTarget:self action:@selector(rightButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        
        [_navView addSubview:titleLabel];
        [_navView addSubview:leftButton];
        [_navView addSubview:rightButton];
        [titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(_navView);
            make.centerX.equalTo(_navView);
            make.height.mas_equalTo(44);
        }];
        [leftButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.height.width.mas_equalTo(44);
            make.left.mas_equalTo(0);
            make.centerY.equalTo(titleLabel);
        }];
        [rightButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.mas_equalTo(-16);
            make.centerY.equalTo(titleLabel);
        }];
    }
    return _navView;
}

- (UILabel *)userIDLabel {
    if (!_userIDLabel) {
        _userIDLabel = [[UILabel alloc] init];
        _userIDLabel.font = [UIFont systemFontOfSize:13];
        _userIDLabel.textColor = [UIColor.whiteColor colorWithAlphaComponent:0.6];
        _userIDLabel.text = [NSString stringWithFormat:@"我的ID：%@", [LocalUserComponent userModel].uid];
    }
    return _userIDLabel;
}

- (VideoCallSearchView *)searchView {
    if (!_searchView) {
        _searchView = [[VideoCallSearchView alloc] init];
        _searchView.delegate = self;
    }
    return _searchView;
}

- (VideoCallUserListView *)userListView {
    if (!_userListView) {
        _userListView = [[VideoCallUserListView alloc] init];
        _userListView.delegate = self;
        [_userListView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(userListViewClick)]];
    }
    return _userListView;
}

- (VideoCallBeautySettingTipView *)tipView {
    if (!_tipView) {
        _tipView = [[VideoCallBeautySettingTipView alloc] init];
        _tipView.message = LocalizedString(@"beauty_setting_tip");
        _tipView.backgroundColor = [UIColor.blackColor colorWithAlphaComponent:0.2];
    }
    return _tipView;
}

#pragma mark - dealloc

- (void)dealloc {
    
}

@end
