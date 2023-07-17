// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "AllIMKitConstants.h"
#import "BaseRTSManager.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^RTCInfoBlock)(NSString *_Nullable appId, NSString *_Nullable bid);

@interface IMService : NSObject

+ (instancetype)shared;

/**
 * @brief 向服务器发消息
 * @param message 消息内容
 */
- (int64_t)sendServerMessage:(NSString *)message;

/**
 * @brief 根据场景名获取RTC信息
 * @param sceneName 场景名
 * @param block 回调
 */
- (void)getRTCAppInfo:(NSString *)sceneName block:(RTCInfoBlock)block;

/**
 * @brief 获取 RTS app ID
 */
- (NSString *)getRTSAppId;

/**
 * @brief 注册消息接收器
 * @param manager 消息接收器
 * @param name 场景名
 */
- (void)registerRTSManager:(BaseRTSManager *)manager name:(IMSceneName)name;

/**
 * @brief 获取注册的消息接收器
 */
- (BaseRTSManager *)getRTSManager:(IMSceneName)name;

/**
 * @brief 移除消息接收器
 * @param name 场景名
 */
- (void)unregisterRTSManager:(IMSceneName)name;

@end

NS_ASSUME_NONNULL_END
