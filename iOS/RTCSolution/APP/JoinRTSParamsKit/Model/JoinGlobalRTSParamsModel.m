// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "JoinGlobalRTSParamsModel.h"

@implementation JoinGlobalRTSParamsModel

+ (NSDictionary *)modelCustomPropertyMapper {
    return @{@"rtsAppId" : @"rts_app_id",
             @"rtsUserId" : @"im_user_id",
             @"rtsToken" : @"rts_token",
             @"serverUrl" : @"server_url",
             @"serverSignature" : @"server_signature",
             @"sceneInfo" : @"scene_infos"
    };
}

@end
