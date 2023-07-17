// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "VideoCallVoipInfo.h"
@class PIPComponent;

NS_ASSUME_NONNULL_BEGIN

@protocol PIPComponentDelegate <NSObject>

/**
 * @brief PIP已经开启回调
 * @param pipComponent PIPComponent 对象
 * @param contentView PIP控制器展示视图
 */
- (void)pipComponent:(PIPComponent *)pipComponent willStartWithContentView:(UIView *)contentView;

/**
 * @brief PIP已经结束开启回调
 * @param pipComponent PIPComponent 对象
 */
- (void)pipComponentDidStopPIP:(PIPComponent *)pipComponent;

@end

@interface PIPComponent : NSObject

@property (nonatomic, weak) id<PIPComponentDelegate> delegate;

@property (nonatomic, assign) BOOL isActive;

/**
 * @brief 初始化PIPComponent
 * @param contentView sourceView
 */
- (instancetype)initWithContentView:(UIView *)contentView;

/**
 * @brief 取消注册PIP功能
 */
- (void)unregisterPiP;

@end

NS_ASSUME_NONNULL_END
