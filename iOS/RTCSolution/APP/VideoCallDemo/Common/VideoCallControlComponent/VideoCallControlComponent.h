// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "VideoCallVoipInfo.h"
@class VideoCallControlComponent;

NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallControlComponentDelegate <NSObject>

/**
 * @brief 用户接受通话回调
 * @param component VideoCallControlComponent 对象
 */
- (void)videoCallControlComponentOnAccept:(VideoCallControlComponent *)component;

/**
 * @brief 用户拒绝、挂断、取消通话回调
 * @param component VideoCallControlComponent 对象
 */
- (void)videoCallControlComponentOnHangup:(VideoCallControlComponent *)component;

/**
 * @brief 用户开关摄像头回调
 * @param component VideoCallControlComponent 对象
 * @param enable 摄像头状态
 */
- (void)videoCallControlComponent:(VideoCallControlComponent *)component
            onCameraEnableChanged:(BOOL)enable;

/**
 * @brief 超时未接听回调
 * @param component VideoCallControlComponent 对象
 */
- (void)videoCallControlComponentOnTimeOut:(VideoCallControlComponent *)component;

/**
 * @brief 是否需要清屏回调
 * @param component VideoCallControlComponent 对象
 * @param isStart 是否清屏
 */
- (void)videoCallControlComponent:(VideoCallControlComponent *)component onStartCountingClearScreen:(BOOL)isStart;

@end

@interface VideoCallControlComponent : NSObject

@property (nonatomic, weak) id<VideoCallControlComponentDelegate> delegate;

@property (nonatomic, assign) VideoCallState state;

/**
 * @brief 初始化
 * @param superView 父视图
 * @param infoModel 房间信息
 */
- (instancetype)initWithSuperView:(UIView *)superView
                        userModel:(VideoCallVoipInfo *)infoModel;

/**
 * @brief 音频路由改变
 * @param audioRoute 当前音频路由
 */
- (void)audioRouteChanged:(ByteRTCAudioRoute)audioRoute;

/**
 * @brief 更新按钮显示隐藏状态
 * @param isHidden 是否隐藏
 */
- (void)updateControlViewHidden:(BOOL)isHidden;


- (void)updateNetworkMessage:(NSString *)message qualityVeryBad:(BOOL)isVeryBad;

@end

NS_ASSUME_NONNULL_END
