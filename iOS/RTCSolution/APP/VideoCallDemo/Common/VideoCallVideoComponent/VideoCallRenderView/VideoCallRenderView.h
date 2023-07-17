// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
@class VideoCallRenderView;

NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallRenderViewDelegate <NSObject>

- (void)videoCallRenderViewOnTouched:(VideoCallRenderView *)view;

@end

@interface VideoCallRenderView : UIView

@property (nonatomic, weak) id<VideoCallRenderViewDelegate> delegate;
@property (nonatomic, strong, readonly) UIView *renderView;
@property (nonatomic, assign) BOOL isEnableVideo;
@property (nonatomic, copy) NSString *name;

- (void)updateTimeString:(NSString *)timeStr;
- (void)updateTimeLablehidden:(BOOL)isHidden;

@end

NS_ASSUME_NONNULL_END
