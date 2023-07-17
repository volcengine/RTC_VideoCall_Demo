// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallUserModel.h"

#define VIDEO_CALL_HISTORY_PATH [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject] stringByAppendingPathComponent:@"video_call_history.plist"]

@implementation VideoCallUserModel

+ (NSArray<VideoCallUserModel *> *)getCallHistory {
    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:VIDEO_CALL_HISTORY_PATH];

    NSString *uid = [LocalUserComponent userModel].uid;

    NSArray *list = [dict objectForKey:uid];
    
    if (!list) {
        return nil;
    }
    return [NSArray yy_modelArrayWithClass:[VideoCallUserModel class] json:list];
}

+ (void)saveCallHistory:(NSArray<VideoCallUserModel *> *)userList {
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        NSArray *objectArray = [userList yy_modelToJSONObject];

        NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:VIDEO_CALL_HISTORY_PATH];

        NSMutableDictionary *dictM = [NSMutableDictionary dictionary];
        [dictM addEntriesFromDictionary:dict];

        NSString *uid = [LocalUserComponent userModel].uid;
        if (!uid) {
            return;
        }
        [dictM setObject:objectArray forKey:uid];

        [dictM writeToFile:VIDEO_CALL_HISTORY_PATH atomically:YES];
    });
}

@end
