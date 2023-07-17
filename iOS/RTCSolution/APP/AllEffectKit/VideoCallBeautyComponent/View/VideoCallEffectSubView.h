// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VideoCallEffectItem.h"
@class VideoCallEffectSubView;

NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallEffectSubViewDelegate <NSObject>

- (void)videoCallEffectSubView:(VideoCallEffectSubView *)view didClickEffectItem:(VideoCallEffectItem *)item;
- (void)videoCallEffectSubViewDidClickReset:(VideoCallEffectSubView *)view;
- (void)videoCallEffectSubView:(VideoCallEffectSubView *)view onSelectedEffectType:(VideoCallEffectItemType)type;

@end

@interface VideoCallEffectSubView : UIView

@property (nonatomic, weak) id<VideoCallEffectSubViewDelegate> delegate;

@property (nonatomic, strong) NSArray<VideoCallEffectItem *> *itemArray;

@property (nonatomic, strong) VideoCallEffectItem *item;

- (instancetype)initWithFrame:(CGRect)frame rootView:(BOOL)rootView;

- (void)reloadData;

@end

NS_ASSUME_NONNULL_END
