//
//  VideoCallAvatarView.h
//  VideoCallDemo
//
//  Created by on 2022/7/21.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, VideoCallAvatarViewVideoStatus) {
    VideoCallAvatarViewVideoStatusOff,
    VideoCallAvatarViewVideoStatusOn
};

typedef NS_ENUM(NSInteger, VideoCallAvatarViewMicStatus) {
    VideoCallAvatarViewMicStatusOff,
    VideoCallAvatarViewMicStatusOn,
    VideoCallAvatarViewMicStatusSpeaking
};

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallAvatarView : UIView

@property (nonatomic, strong) UIView *videoContainerView;

- (void)setVideoStatus:(VideoCallAvatarViewVideoStatus)status;

- (void)setMicStatus:(VideoCallAvatarViewMicStatus)status;

- (void)setName:(NSString *)name;

@end

NS_ASSUME_NONNULL_END
