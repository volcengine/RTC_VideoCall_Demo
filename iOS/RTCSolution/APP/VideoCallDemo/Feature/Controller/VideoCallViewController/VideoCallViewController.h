// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VideoCallVoipInfo.h"

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallViewController : UIViewController

@property (nonatomic, strong) VideoCallVoipInfo *infoModel;

+ (VideoCallViewController *)currentController;

- (void)hangup;

@end

NS_ASSUME_NONNULL_END
