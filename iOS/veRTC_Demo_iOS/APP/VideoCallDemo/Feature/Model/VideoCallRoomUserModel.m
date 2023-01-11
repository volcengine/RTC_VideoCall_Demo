//
//  VideoCallRoomUserModel.m
//  quickstart
//
//  Created by on 2021/4/2.
//  
//

#import "VideoCallRoomUserModel.h"
#import "VideoCallRTCManager.h"

@implementation VideoCallRoomUserModel

- (instancetype)initWithUid:(NSString *)uid {
    self = [super init];
    if (self) {
        self.uid = uid;
    }
    return self;
}

@end
