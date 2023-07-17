// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallAudioView : UIView

@property (nonatomic, strong, readonly) UIView *contentView;

@property (nonatomic, assign) BOOL animation;

- (void)updateUserName:(NSString *)userName;

- (void)updateText:(NSString *)text;

@end

NS_ASSUME_NONNULL_END
