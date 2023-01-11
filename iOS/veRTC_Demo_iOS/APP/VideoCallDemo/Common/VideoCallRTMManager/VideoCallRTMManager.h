//
//  VideoCallRTMManager.h
//  SceneRTCDemo
//
//  Created by on 2021/3/16.
//

#import <Foundation/Foundation.h>
#import "VideoCallRoomUserModel.h"
#import "BaseRTCManager.h"

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallRTMManager : BaseRTCManager

#pragma mark - Get meeting data

/*
 * Join the meeting
 * @param loginModel Login user data
 * @param block Callback
 */
+ (void)joinRoom:(VideoCallRoomUserModel *)loginModel
           block:(void (^)(NSString *token,
                           NSInteger duration,
                           RTMACKModel *model))block;

/*
 * Leave room
 */
+ (void)leaveRoom;

/*
 * Reconnect
 * @param block Callback
 */
+ (void)reconnectWithBlock:(void (^)(NSString *RTCToken,
                                     RTMACKModel *model))block;

#pragma mark - Notification message


/*
 * On close room
 * @param block Callback
 */
+ (void)onCloseRoomWithBlock:(void (^)(NSString *roomId))block;

@end

NS_ASSUME_NONNULL_END
