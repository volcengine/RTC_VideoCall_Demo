// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallRTSManager.h"
#import "JoinRTSParams.h"
#import "VideoCallRTCManager.h"
#import "IMService.h"
#import "VideoCallViewController.h"
#import "VideoCallBeautySettingViewController.h"

// 用户不存在
static NSInteger const kUserNotExist = 419;

// 用户忙线中
static NSInteger const kUserIsBusy = 813;

@implementation VideoCallRTSManager

- (instancetype)init {
    if (self = [super init]) {
        [self addListener];
    }
    return self;
}

- (void)connectRTSSuccessful {
    [VideoCallRTSManager clearUser:^(RTSACKModel * _Nonnull model) {
        
    }];
}

// 添加消息监听
- (void)addListener {
    __weak typeof(self) weakSelf = self;
    [self onSceneListener:@"videooneInform" block:^(RTSNoticeModel * _Nonnull noticeModel) {
        [weakSelf handleMessage:noticeModel.data];
    }];
}

- (void)handleMessage:(NSDictionary *)dict {
    VideoCallEventType eventType = [dict[@"event_type_code"] integerValue];
    VideoCallVoipInfo *info = [VideoCallVoipInfo yy_modelWithJSON:dict[@"voip_info"]];
    dispatch_async(dispatch_get_main_queue(), ^{
        if (eventType == VideoCallEventTypeCreateRoom) {
            [self handleInviteMessage:info];
        } else {
            if ([self.delegate respondsToSelector:@selector(videoCallRTSManager:onReceivedMessage:infoModel:)]) {
                [self.delegate videoCallRTSManager:self onReceivedMessage:eventType infoModel:info];
            }
        }
    });
}

- (void)handleInviteMessage:(VideoCallVoipInfo *)info {
    if ([self canResponseInvitationMessage]) {
        // 收到通话邀请判断有没有权限
        [SystemAuthority authorizationStatusWithType:AuthorizationTypeAudio block:^(BOOL isAuthorize) {
            if (isAuthorize) {
                if (![VideoCallRTCManager shareRtc].rtcEngineKit) {
                    [VideoCallRTSManager connectRTCBlock:^(BOOL result) {
                        [VideoCallRTSManager jumpToVideoCallViewController:info currentViewController:nil];
                    }];
                } else {
                    [VideoCallRTSManager jumpToVideoCallViewController:info currentViewController:nil];
                }
            } else {
                AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
                alertCancelModel.title = LocalizedString(@"confirm");
                [[AlertActionManager shareAlertActionManager] showWithMessage:LocalizedString(@"no_microphone_permission") actions:@[alertCancelModel]];
                
                [VideoCallRTSManager updateStatus:VideoCallStateRefused info:info block:^(RTSACKModel * _Nonnull model) {
                    
                }];
            }
        }];
    } else {
        // 在其他场景中Toast提示
        [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"other_scenes_receive_video_call")];
        
        [VideoCallRTSManager updateStatus:VideoCallStateRefused info:info block:^(RTSACKModel * _Nonnull model) {
            
        }];
    }
}

- (BOOL)canResponseInvitationMessage {
    
    // APP在前台
    BOOL applicationActive = ([UIApplication sharedApplication].applicationState != UIApplicationStateBackground);
    
    // 在音视频通话 主页 意见反馈 修改昵称 等页面
    UIViewController *topViewController = [DeviceInforTool topViewController];
    NSString *classString = NSStringFromClass([topViewController class]);
    BOOL stayResponsePage = ([classString hasPrefix:@"VideoCall"] ||
                             [classString hasPrefix:@"Feedback"] ||
                             [classString isEqualToString:@"MenuViewController"] ||
                             [classString isEqualToString:@"UserNameViewController"]);
    
    UIViewController *presentedViewController = topViewController.presentedViewController;
    if ([presentedViewController isKindOfClass:[UIAlertController class]]) {
        [presentedViewController dismissViewControllerAnimated:NO completion:nil];
    }
    
    if (stayResponsePage && applicationActive) {
        return YES;
    }
    return NO;
}


#pragma mark - classMethods
+ (void)jumpToVideoCallViewController:(VideoCallVoipInfo *)infoModel currentViewController:(UIViewController *_Nullable)viewController {
    
    if ([[DeviceInforTool topViewController] isKindOfClass:[VideoCallViewController class]] ||
        [VideoCallViewController currentController]) {
        return;
    }
    
    if (!viewController) {
        viewController = [DeviceInforTool topViewController];
    }
  
    VideoCallViewController *ctrl = [[VideoCallViewController alloc] init];
    ctrl.infoModel = infoModel;
    ctrl.modalPresentationStyle = UIModalPresentationFullScreen;
    if ([[DeviceInforTool topViewController] isKindOfClass:[VideoCallBeautySettingViewController class]]) {
        [viewController presentViewController:ctrl animated:NO completion:^{
            [viewController.navigationController popViewControllerAnimated:NO];
        }];
    } else {
        [viewController presentViewController:ctrl animated:NO completion:nil];
    }
}

+ (void)connectRTCBlock:(void(^)(BOOL result))block {
    [[IMService shared] getRTCAppInfo:IMSceneNameVideoCall block:^(NSString * _Nullable appId, NSString * _Nullable bid) {
        if (!appId || !bid) {
            if (block) {
                block(NO);
            }
        } else {
            [[VideoCallRTCManager shareRtc] createRTCVideo:appId bid:bid];
            if (block) {
                block(YES);
            }
        }
    }];
}

+ (VideoCallRTSManager *)getRTSManager {
    return (VideoCallRTSManager *)[[IMService shared] getRTSManager:IMSceneNameVideoCall];
}

#pragma mark - sendServerMessage

+ (void)searchUser:(NSString *)userID
             block:(void(^)(NSArray<VideoCallUserModel *> *userList, NSString *errorMessage))block; {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    [dic setValue:userID forKey:@"keyword"];

    NSDictionary *dicData = [JoinRTSParams addTokenToParams:[dic copy]];
    [[VideoCallRTSManager getRTSManager] emitWithAck:@"videooneGetUserList"
                                         with:dicData
                                        block:^(RTSACKModel * _Nonnull ackModel) {
        NSArray *userList = nil;
        NSString *errorMessage = nil;
        if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
            NSArray *userArray = ackModel.response[@"data"];
            userList = [NSArray yy_modelArrayWithClass:[VideoCallUserModel class] json:userArray];
            
            if ([ackModel.response[@"err_no"] integerValue] == kUserNotExist) {
                errorMessage = LocalizedString(@"user_not_exist");
            } else {
                errorMessage = ackModel.response[@"err_tips"];
            }
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if (block) {
                block(userList, errorMessage);
            }
        });
    }];
}

+ (void)callUser:(VideoCallUserModel *)userModel block:(void(^)(BOOL success, VideoCallVoipInfo *info, NSString *message))block {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    [dic setValue:@(userModel.callType) forKey:@"type"];
    [dic setValue:[LocalUserComponent userModel].uid forKey:@"from_user_id"];
    [dic setValue:userModel.uid forKey:@"to_user_id"];

    NSDictionary *dicData = [JoinRTSParams addTokenToParams:[dic copy]];
    [[VideoCallRTSManager getRTSManager] emitWithAck:@"videooneCreateRoom"
                                         with:dicData
                                        block:^(RTSACKModel * _Nonnull ackModel) {
        
        VideoCallVoipInfo *info = nil;
        NSString *message = ackModel.message;
        if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
            info = [VideoCallVoipInfo yy_modelWithJSON:ackModel.response[@"data"]];
        }
        if (ackModel.code == kUserIsBusy) {
            message = LocalizedString(@"user_is_busy");
        }
    
        dispatch_async(dispatch_get_main_queue(), ^{
            if (block) {
                block(ackModel.result ,info, message);
            }
        });
    }];
}

+ (void)updateStatus:(VideoCallState)status info:(VideoCallVoipInfo *)info block:(void(^)(RTSACKModel *model))block {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    [dic setValue:info.roomId forKey:@"room_id"];
    [dic setValue:@(info.callType) forKey:@"type"];
    [dic setValue:@(status) forKey:@"status"];
    
    NSDictionary *dicData = [JoinRTSParams addTokenToParams:[dic copy]];
    [[VideoCallRTSManager getRTSManager] emitWithAck:@"videooneUpdateStatus"
                                         with:dicData
                                        block:^(RTSACKModel * _Nonnull ackModel) {
    
        dispatch_async(dispatch_get_main_queue(), ^{
            if (block) {
                block(ackModel);
            }
        });
    }];
}

+ (void)clearUser:(void (^)(RTSACKModel *model))block {
    NSDictionary *dic = [JoinRTSParams addTokenToParams:nil];
    
    [[VideoCallRTSManager getRTSManager] emitWithAck:@"videooneClearUser" with:dic block:^(RTSACKModel * _Nonnull ackModel) {
        
        if (block) {
            block(ackModel);
        }
        NSLog(@"[%@]-twClearUser %@ \n %@", [self class], dic, ackModel.response);
    }];
}

@end
