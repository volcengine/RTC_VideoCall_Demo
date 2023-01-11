//
//  VideoCallRoomViewController+Listener.m
//  SceneRTCDemo
//
//  Created by on 2021/3/16.
//

#import "VideoCallRoomViewController+Listener.h"
#import "VideoCallRTMManager.h"
#import "VideoCallRTCManager.h"

@implementation VideoCallRoomViewController (Listener)

- (void)addRTSListener {
    __weak __typeof(self) wself = self;
    //User Join
    [VideoCallRTMManager onCloseRoomWithBlock:^(NSString * _Nonnull roomId) {
        [wself hangUp];
        [[ToastComponent shareToastComponent] showWithMessage:@"本次会议时已超过15分钟" delay:0.8];
    }];
        
}

@end
