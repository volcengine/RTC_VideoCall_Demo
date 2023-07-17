// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VideoCallUserModel.h"
#import "VideoCallControlButton.h"
@class VideoCallControlView;

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, VideoCallControlType) {
    VideoCallControlTypeMic = 1,            // 麦克风开关
    VideoCallControlTypeAudioRoute,          // 听筒扬声器
    VideoCallControlTypeCamera,             // 摄像头开关
    VideoCallControlTypeCameraSwitch,       // 镜头翻转
    VideoCallControlTypeBeauty,             // 美颜
    VideoCallControlTypeAccept,             // 接听
    VideoCallControlTypehangUp,             // 挂断，拒绝
};

@protocol VideoCallControlViewDelegate <NSObject>

- (void)videoCallControlView:(VideoCallControlView *)controlView
         didClickControlType:(VideoCallControlType)type
                      button:(VideoCallControlButton *)button;

@end

@interface VideoCallControlView : UIView

@property (nonatomic, weak) id<VideoCallControlViewDelegate> delegate;

- (void)setupViewWithState:(VideoCallState)state
                  callType:(VideoCallType)callType;

- (VideoCallControlButton *)getButtonWithType:(VideoCallControlType)type;

- (NSString *)getTipMessage;

@end

NS_ASSUME_NONNULL_END
