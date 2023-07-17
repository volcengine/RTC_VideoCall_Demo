// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VideoCallVoipInfo.h"

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallPIPView : UIView

@property (nonatomic, assign) BOOL isEnableVideo;
@property (nonatomic, copy) NSString *name;

- (void)startPIPWithInfoModel:(VideoCallVoipInfo *)infoModel;

- (void)stopPIP;

- (void)updateTimeString:(NSString *)timeStr;

@end

NS_ASSUME_NONNULL_END
