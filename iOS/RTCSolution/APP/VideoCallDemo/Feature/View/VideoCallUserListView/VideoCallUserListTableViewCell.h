// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VideoCallUserModel.h"
@class VideoCallUserListTableViewCell;

NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallUserListTableViewCellDelegate <NSObject>

- (void)videoCallUserListTableViewCell:(VideoCallUserListTableViewCell *)cell didClickUser:(VideoCallUserModel *)userData;

@end

@interface VideoCallUserListTableViewCell : UITableViewCell

@property (nonatomic, weak) id<VideoCallUserListTableViewCellDelegate> delegate;
@property (nonatomic, strong) VideoCallUserModel *data;

@end

NS_ASSUME_NONNULL_END
