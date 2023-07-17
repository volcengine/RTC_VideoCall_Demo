// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallNarrowWindow : UIView

- (void)becomeNarrowWindow:(UIView *)contentView
                  desFrame:(CGRect)desFrame
                  complete:(void(^)(BOOL finished))complete;

- (void)closeNarrowWindow:(void(^)(BOOL finished))complete;

@end

NS_ASSUME_NONNULL_END
