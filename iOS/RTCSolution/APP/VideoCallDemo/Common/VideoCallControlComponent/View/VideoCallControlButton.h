// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, VideoCallControlButtonType) {
    VideoCallControlButtonTypeNormal,
    VideoCallControlButtonTypeSelected,
    VideoCallControlButtonTypeNoPermission,
};

@interface VideoCallControlButton : UIButton

@property (nonatomic, assign) VideoCallControlButtonType type;

- (instancetype)initWithNormalImage:(NSString *)normalImage
                        normalTitle:(NSString * _Nullable)normalTitle
                      selectedImage:(NSString *)selectedImage
                      selectedTitle:(NSString * _Nullable)selectedTitle;

- (instancetype)initWithImage:(NSString *)imageName;

@end

NS_ASSUME_NONNULL_END
