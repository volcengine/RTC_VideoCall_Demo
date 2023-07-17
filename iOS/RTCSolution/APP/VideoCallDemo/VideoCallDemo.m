// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallDemo.h"
#import "JoinRTSParams.h"
#import "VideoCallDemoConstants.h"
#import "VideoCallRTCManager.h"
#import "VideoCallHomeViewController.h"

extern int VideoCallOnTheCallType;

@implementation VideoCallDemo

- (void)pushDemoViewControllerBlock:(void (^)(BOOL result))block {
    [super pushDemoViewControllerBlock:block];
    
    if (VideoCallOnTheCallType > 0) {
        VideoCallHomeViewController *next = [[VideoCallHomeViewController alloc] init];
        UIViewController *topVC = [DeviceInforTool topViewController];
        [topVC.navigationController pushViewController:next animated:YES];
        if (block) {
            block(YES);
        }
        
        return;
    }
    
    [VideoCallRTSManager connectRTCBlock:^(BOOL result) {
        if (result) {
            
            [VideoCallRTSManager clearUser:^(RTSACKModel * _Nonnull model) {
                
            }];
            
            VideoCallHomeViewController *next = [[VideoCallHomeViewController alloc] init];
            UIViewController *topVC = [DeviceInforTool topViewController];
            [topVC.navigationController pushViewController:next animated:YES];
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"connection_failed")];
        }
        if (block) {
            block(result);
        }
    }];
}

@end
