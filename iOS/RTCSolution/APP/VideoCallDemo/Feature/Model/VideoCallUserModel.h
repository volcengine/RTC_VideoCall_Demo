// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "BaseUserModel.h"
#import "VideoCallVoipInfo.h"

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallUserModel : BaseUserModel

@property (nonatomic, assign) VideoCallType callType;

+ (NSArray<VideoCallUserModel *> *)getCallHistory;

+ (void)saveCallHistory:(NSArray<VideoCallUserModel *> *)userList;

@end

NS_ASSUME_NONNULL_END
