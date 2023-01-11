//
//  VideoCallRoomStatsView.h
//  VoiceChatDemo
//
//  Created by on 2022/7/26.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallRoomStatsView : UIView

@property (nonatomic, readonly) UITableView *statsTableView;
@property (nonatomic, readonly) NSUInteger currentSelectedIndex;

@end

NS_ASSUME_NONNULL_END
