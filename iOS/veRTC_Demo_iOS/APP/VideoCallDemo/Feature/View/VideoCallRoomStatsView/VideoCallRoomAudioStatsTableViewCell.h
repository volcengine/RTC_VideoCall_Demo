//
//  VideoCallRoomAudioStatsTableViewCell.h
//  VoiceChatDemo
//
//  Created by on 2022/7/26.
//

#import <UIKit/UIKit.h>
#import "VideoCallRoomParamInfoModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallRoomAudioStatsTableViewCell : UITableViewCell

- (void)updateUIWithModel:(VideoCallRoomParamInfoModel *)model;

@end

NS_ASSUME_NONNULL_END
