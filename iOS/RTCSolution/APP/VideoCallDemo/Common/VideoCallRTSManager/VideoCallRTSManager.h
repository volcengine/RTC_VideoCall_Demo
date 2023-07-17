// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "VideoCallUserModel.h"
#import "VideoCallVoipInfo.h"
#import "BaseRTSManager.h"
@class VideoCallRTSManager;

NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallRTSManagerDelegate <NSObject>

/**
 * @brief 收到信令消息回调
 * @param manager VideoCallRTSManager 模型
 * @param type 消息类型
 * @param info 房间信息
 */
- (void)videoCallRTSManager:(VideoCallRTSManager *)manager
          onReceivedMessage:(VideoCallEventType)type
                  infoModel:(VideoCallVoipInfo *)info;

@end

@interface VideoCallRTSManager : BaseRTSManager

@property (nonatomic, weak) id<VideoCallRTSManagerDelegate> delegate;

/**
 * @brief 获取VideoCallRTSManager实例对象
 */
+ (VideoCallRTSManager *)getRTSManager;

/**
 * @brief 创建RTC引擎
 * @param block Callback
 */
+ (void)connectRTCBlock:(void(^)(BOOL result))block;

/**
 * @brief 跳转通话页面
 * @param infoModel 通话信息Model
 * @param viewController 当前ViewController
 */
+ (void)jumpToVideoCallViewController:(VideoCallVoipInfo *)infoModel currentViewController:(UIViewController *_Nullable)viewController;

/**
 * @brief 通过用户ID搜索用户
 * @param userID User ID
 * @param block Callback
 */
+ (void)searchUser:(NSString *)userID
             block:(void(^)(NSArray<VideoCallUserModel *> *userList, NSString *errorMessage))block;

/**
 * @brief 呼叫用户
 * @param userModel 被叫用户信息
 * @param block Callback
 */
+ (void)callUser:(VideoCallUserModel *)userModel
           block:(void(^)(BOOL success, VideoCallVoipInfo *info, NSString *message))block;

/**
 * @brief 更新用户状态
 * @param status 状态
 * @param info voip信息
 * @param block Callback
 */
+ (void)updateStatus:(VideoCallState)status
                info:(VideoCallVoipInfo *)info
               block:(void(^)(RTSACKModel *model))block;

/**
 * @brief 清除用户状态
 * @param block Callback
 */
+ (void)clearUser:(void (^)(RTSACKModel *model))block;

@end

NS_ASSUME_NONNULL_END
