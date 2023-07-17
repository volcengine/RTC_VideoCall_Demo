// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VideoCallUserModel.h"
@class VideoCallUserListView;

NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallUserListViewDelegate <NSObject>

- (void)videoCallUserListView:(VideoCallUserListView *)userListView didClickUser:(VideoCallUserModel *)userData;

@end

@interface VideoCallUserListView : UIView

@property (nonatomic, weak) id<VideoCallUserListViewDelegate> delegate;
@property (nonatomic, copy) NSArray<VideoCallUserModel *> *dataArray;

@end

NS_ASSUME_NONNULL_END
