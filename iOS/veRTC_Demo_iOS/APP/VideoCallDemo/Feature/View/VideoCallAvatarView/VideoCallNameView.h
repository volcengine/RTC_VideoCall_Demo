//
//  VideoCallNameView.h
//  VideoCallDemo
//
//  Created by on 2022/7/22.
//

#import <UIKit/UIKit.h>
#import "VideoCallAvatarView.h"

NS_ASSUME_NONNULL_BEGIN


@interface VideoCallNameView : UIView

- (void)setMicStatus:(VideoCallAvatarViewMicStatus)status;

- (void)setName:(NSString *)name;

@end

NS_ASSUME_NONNULL_END
