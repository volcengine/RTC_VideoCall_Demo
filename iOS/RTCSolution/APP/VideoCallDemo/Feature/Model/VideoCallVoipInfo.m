// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallVoipInfo.h"

@implementation VideoCallVoipInfo

+ (NSDictionary *)modelCustomPropertyMapper {
    return @{
        @"roomId" : @"room_id",
        @"token" : @"token",
        @"fromUserId" : @"from_user_id",
        @"fromUserName" : @"from_user_name",
        @"toUserId" : @"to_user_id",
        @"callType" : @"type",
        @"status" : @"status",
        @"userId" : @"user_id",
    };
}

- (NSString *)showUserName {
    if ([self.userId isEqualToString:self.fromUserId]) {
        return self.toUserName;
    }
    return self.fromUserName;
}

@end
