// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "EffectBeautyEffectModel.h"
#import "VideoCallEffectItem.h"
@class VideoCallBeautyView;

NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallBeautyViewDelegate <NSObject>

- (void)videoCallBeautyView:(VideoCallBeautyView *)beautyView didCleanEffect:(VideoCallEffectItem *)item;
- (void)videoCallBeautyView:(VideoCallBeautyView *)beautyView didReloadEffectItem:(VideoCallEffectItem *)item;
- (void)videoCallBeautyView:(VideoCallBeautyView *)beautyView didChangeEffectItemValue:(VideoCallEffectItem *)item;
- (void)videoCallBeautyViewDidReset:(VideoCallBeautyView *)beautyView;

@end

@interface VideoCallBeautyView : UIView

@property (nonatomic, weak) id<VideoCallBeautyViewDelegate> delegate;
@property (nonatomic, strong) NSArray<VideoCallEffectItem *> *itemArray;

- (void)saveBeautyConfig;


@end

NS_ASSUME_NONNULL_END
