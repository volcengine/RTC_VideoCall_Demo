// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "EffectBeautyEffectModel.h"

@class EffectBeautyView;

NS_ASSUME_NONNULL_BEGIN


@protocol EffectBeautyViewDelegate <NSObject>

- (void)effectBeautyView:(EffectBeautyView *)beautyView didClickedEffect:(EffectBeautyEffectModel *_Nonnull)model;

- (void)effectBeautyView:(EffectBeautyView *)beautyView didChangeEffectValue:(EffectBeautyEffectModel *_Nonnull)model;

@end

@interface EffectBeautyView : UIView

@property (nonatomic, weak) id<EffectBeautyViewDelegate> delegate;

- (void)saveBeautyConfig;

- (void)reload;

@end

NS_ASSUME_NONNULL_END
