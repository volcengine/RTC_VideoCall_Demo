// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

@class BytedEffectProtocol;

NS_ASSUME_NONNULL_BEGIN
/**
 * @type keytype
 * @brief 美颜类型。
 */
typedef NS_ENUM(NSInteger, BytedEffectType) {
    
    /**
     * @brief 默认美颜类型。
     */
    BytedEffectTypeDefault = 0,
    
    /**
     * @brief 音视频通话美颜类型。
     */
    BytedEffectTypeVideoCall = 1,
};

@protocol BytedEffectDelegate <NSObject>

- (instancetype)protocol:(BytedEffectProtocol *)protocol
    initWithRTCEngineKit:(id)rtcEngineKit;

- (void)protocol:(BytedEffectProtocol *)protocol
    showWithView:(UIView *)superView
    dismissBlock:(void (^)(BOOL result))block;

- (void)protocol:(BytedEffectProtocol *)protocol
      showInView:(UIView *)superView
        animated:(BOOL)animated
    dismissBlock:(void (^)(BOOL result))block;

- (void)protocol:(BytedEffectProtocol *)protocol resume:(BOOL)result;

- (void)protocol:(BytedEffectProtocol *)protocol reset:(BOOL)result;

- (void)protocol:(BytedEffectProtocol *)protocol saveBeautyConfig:(BOOL)result;

@end

@interface BytedEffectProtocol : NSObject

/**
 * @brief 初始化
 * @param rtcEngineKit 对象
 */
- (instancetype)initWithRTCEngineKit:(id)rtcEngineKit;

/**
 * @brief 初始化
 * @param rtcEngineKit 对象
 * @param type 美颜类型
 */
- (instancetype)initWithRTCEngineKit:(id)rtcEngineKit
                                type:(BytedEffectType)type;

/**
 * @brief 展示美颜面板
 * @param superView 展示的 UIView
 * @param block 美颜面板消失后的回调
 */
- (void)showWithView:(UIView *)superView
        dismissBlock:(void (^)(BOOL result))block;

/**
 * @brief 展示美颜面板
 * @param superView 展示的 UIView
 * @param animated 是否展示动画
 * @param block 美颜面板消失后的回调
 */
- (void)showInView:(UIView *)superView
          animated:(BOOL)animated
      dismissBlock:(void (^)(BOOL result))block;

/**
 * @brief 恢复美颜效果
 */
- (void)resume;

/**
 * @brief 重置美颜缓存数据
 */
- (void)reset;

/**
 * @brief 保存美颜数据
 */
- (void)saveBeautyConfig;

@end

NS_ASSUME_NONNULL_END
