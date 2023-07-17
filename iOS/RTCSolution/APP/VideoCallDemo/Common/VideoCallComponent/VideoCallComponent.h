// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "VideoCallVoipInfo.h"
@protocol VideoCallComponent;

NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallComponentDelegate <NSObject>

/**
 * @brief 小窗关闭回调
 * @param component VideoCallComponent 对象
 */
- (void)videoCallComponentDidCloseNarrow:(id<VideoCallComponent>)component;

@end

@protocol VideoCallComponent <NSObject>

@property (nonatomic, weak) id<VideoCallComponentDelegate> delegate;
@property (nonatomic, assign) VideoCallState state;

/**
 * @brief 初始化VideoCallComponent
 * @param superView 父视图
 * @param infoModel 房间信息
 */
- (instancetype)initWithSuperView:(UIView *)superView
                        infoModel:(VideoCallVoipInfo *)infoModel;

/**
 * @brief 开启应用内小窗
 */
- (void)becomeNarrow;

/**
 * @brief 更新通话时间
 * @param timeStr 通话时间
 */
- (void)updateTimeString:(NSString *)timeStr;

/**
 * @brief 通话挂断
 */
- (void)hangup;

/**
 * @brief 更新用户摄像头状态
 * @param enable 摄像头状态
 * @param isLocalUser 是否是本地用户
 */
- (void)updateUserCamera:(BOOL)enable
             isLocalUser:(BOOL)isLocalUser;

/**
 * @brief 开始远端用户渲染
 * @param userId User ID
 */
- (void)startRenderRemoteView:(NSString *)userId;

/**
 * @brief 获取PIP source view
 */
- (UIView *)getPIPContentView;

/**
 * @brief 开启PIP
 * @param view PIP view
 */
- (void)startPIPWithView:(UIView *)view;

/**
 * @brief 关闭PIP
 */
- (void)stopPIP;

@end

NS_ASSUME_NONNULL_END
