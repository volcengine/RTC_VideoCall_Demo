// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallNarrowWindow.h"

static CGFloat kBerthRegionWidth = 10;

@interface VideoCallNarrowWindow ()

@end

@implementation VideoCallNarrowWindow

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.layer.masksToBounds = YES;
        [self addGestureRecognizer:[[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(panGesture:)]];
    }
    return self;
}

- (void)panGesture:(UIPanGestureRecognizer *)gesture {
    if (gesture.state == UIGestureRecognizerStateBegan) {
        
    } else if (gesture.state == UIGestureRecognizerStateChanged) {
        CGPoint translation = [gesture translationInView:self];
        self.center = CGPointMake(self.center.x + translation.x, self.center.y + translation.y);
        [gesture setTranslation:CGPointZero inView:self];
        
    } else if (gesture.state == UIGestureRecognizerStateEnded ||
               gesture.state == UIGestureRecognizerStateCancelled) {
        
        CGRect toFrame = self.frame;
        if (self.centerX < SCREEN_WIDTH / 2.0) {
            toFrame.origin.x = kBerthRegionWidth;
        } else {
            toFrame.origin.x = SCREEN_WIDTH - kBerthRegionWidth - self.width;
        }
        if (self.top < [DeviceInforTool getStatusBarHight]) {
            toFrame.origin.y = [DeviceInforTool getStatusBarHight];
        } else if (self.bottom > SCREEN_HEIGHT - [DeviceInforTool getVirtualHomeHeight]) {
            toFrame.origin.y = SCREEN_HEIGHT - [DeviceInforTool getVirtualHomeHeight] - self.height;
        }
        [UIView animateWithDuration:0.64 delay:0.0 usingSpringWithDamping:0.59 initialSpringVelocity:0 options:UIViewAnimationOptionCurveLinear animations:^{
            self.frame = toFrame;
        } completion:^(BOOL finished) {
            
        }];
    }
}

- (void)becomeNarrowWindow:(UIView *)contentView
                  desFrame:(CGRect)desFrame
                  complete:(void(^)(BOOL finished))complete {
    
    self.frame = UIScreen.mainScreen.bounds;
    [[UIApplication sharedApplication].keyWindow addSubview:self];
    [self addSubview:contentView];
    
    [contentView mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self);
    }];
    [self layoutIfNeeded];
    
    [UIView animateWithDuration:0.25 animations:^{
        self.frame = desFrame;
        self.layer.cornerRadius = 10;
        self.layer.borderWidth = 1;
        self.layer.borderColor = [UIColor colorFromHexString:@"#565A60"].CGColor;
        [self layoutIfNeeded];
    } completion:^(BOOL finished) {
        if (complete) {
            complete(finished);
        }
    }];
}

- (void)closeNarrowWindow:(void(^)(BOOL finished))complete {
    [UIView animateWithDuration:0.25 animations:^{
        self.frame = UIScreen.mainScreen.bounds;
        self.layer.cornerRadius = 0;
        self.layer.borderWidth = 0;
        self.layer.borderColor = [UIColor clearColor].CGColor;
        [self layoutIfNeeded];
    } completion:^(BOOL finished) {
        if (complete) {
            complete(finished);
        }
        [self removeFromSuperview];
    }];
}

- (void)dealloc {
    NSLog(@"VideoCallNarrowWindow dealloc");
}

@end
