// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
@class VideoCallSearchView;
NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallSearchViewDelegate <NSObject>

- (void)videoCallSearchView:(VideoCallSearchView *)searchView didSearchUser:(NSString *)userID;

- (void)videoCallSearchViewDidClearSearch:(VideoCallSearchView *)searchView;

@end

@interface VideoCallSearchView : UIView

@property (nonatomic, weak) id<VideoCallSearchViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
