// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "PIPComponent.h"
#import <AVKit/AVKit.h>
#import "VideoCallRTCManager.h"

@interface PIPComponent ()<AVPictureInPictureControllerDelegate>

@property (nonatomic, strong) AVPictureInPictureController *pipController;
@property (nonatomic, strong) UIViewController *callViewController;


@end

@implementation PIPComponent

- (instancetype)initWithContentView:(UIView *)contentView {
    if (self = [super init]) {
        
        if (@available(iOS 15.0, *)) {
            if ([AVPictureInPictureController isPictureInPictureSupported]) {
                
                [[VideoCallRTCManager shareRtc] openPIPMode];
                
                AVPictureInPictureVideoCallViewController *callViewController = [[AVPictureInPictureVideoCallViewController alloc] init];
                callViewController.preferredContentSize = UIScreen.mainScreen.bounds.size;
                self.callViewController = callViewController;
                
                AVPictureInPictureControllerContentSource *contentSource = [[AVPictureInPictureControllerContentSource alloc] initWithActiveVideoCallSourceView:contentView contentViewController:callViewController];
                self.pipController = [[AVPictureInPictureController alloc] initWithContentSource:contentSource];
                self.pipController.delegate = self;
                self.pipController.canStartPictureInPictureAutomaticallyFromInline = YES;
            
                [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleNotification:) name:UIApplicationDidBecomeActiveNotification object:nil];
            }
        }
    }
    return self;
}

/// 注销
- (void)unregisterPiP {
    
    if (@available(iOS 15.0, *)) {
        [self stopPiP];
        
        [self.pipController setContentSource:nil];
        self.pipController.delegate = nil;
        self.pipController = nil;
    }
    
}

#pragma mark - AVPictureInPictureControllerDelegate
- (void)pictureInPictureControllerWillStartPictureInPicture:(AVPictureInPictureController *)pictureInPictureController {
    NSLog(@"pictureInPictureControllerWillStartPictureInPicture");
    if ([self.delegate respondsToSelector:@selector(pipComponent:willStartWithContentView:)]) {
        [self.delegate pipComponent:self willStartWithContentView:self.callViewController.view];
    }
    
}

- (void)pictureInPictureControllerDidStartPictureInPicture:(AVPictureInPictureController *)pictureInPictureController {
    NSLog(@"pictureInPictureControllerDidStartPictureInPicture");

}

- (void)pictureInPictureController:(AVPictureInPictureController *)pictureInPictureController failedToStartPictureInPictureWithError:(NSError *)error {
    NSLog(@"failedToStartPictureInPictureWithError");
    
    if ([self.delegate respondsToSelector:@selector(pipComponentDidStopPIP:)]) {
        [self.delegate pipComponentDidStopPIP:self];
    }
}

- (void)pictureInPictureController:(AVPictureInPictureController *)pictureInPictureController restoreUserInterfaceForPictureInPictureStopWithCompletionHandler:(void (^)(BOOL))completionHandler {
    NSLog(@"restoreUserInterfaceForPictureInPictureStopWithCompletionHandler");
    // 执行回调的闭包
    completionHandler(YES);
}

- (void)pictureInPictureControllerWillStopPictureInPicture:(AVPictureInPictureController *)pictureInPictureController {
    NSLog(@"pictureInPictureControllerWillStopPictureInPicture");
    
}

- (void)pictureInPictureControllerDidStopPictureInPicture:(AVPictureInPictureController *)pictureInPictureController {
    NSLog(@"pictureInPictureControllerDidStopPictureInPicture");
    if ([self.delegate respondsToSelector:@selector(pipComponentDidStopPIP:)]) {
        [self.delegate pipComponentDidStopPIP:self];
    }
}

#pragma mark - Notification
- (void)handleNotification:(NSNotification *)notification {
    if ([notification.name isEqual:UIApplicationDidBecomeActiveNotification]) {
        [self stopPiP];
    }
}

/// 关闭
- (void)stopPiP {
    if (self.pipController.isPictureInPictureActive) {
        [self.pipController stopPictureInPicture];
    }
}

#pragma mark - public
- (BOOL)isActive {
    return self.pipController.isPictureInPictureActive;
}

#pragma mark - dealloc
- (void)dealloc {
    NSLog(@"%s", __func__);
}

@end
