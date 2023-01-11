//
//  VideoCallStatsComponent.h
//  VideoCallDemo
//
//  Created by on 2022/8/3.
//

#import <Foundation/Foundation.h>
#import "VideoCallRoomParamInfoModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallStatsComponent : NSObject

- (instancetype)initWithSuperView:(UIView *)superView;

- (void)setVideoStats:(NSArray <VideoCallRoomParamInfoModel *>*)videoStats;

- (void)setAudioStats:(NSArray <VideoCallRoomParamInfoModel *>*)audioStats;

- (void)showStatsView;

@end

NS_ASSUME_NONNULL_END
