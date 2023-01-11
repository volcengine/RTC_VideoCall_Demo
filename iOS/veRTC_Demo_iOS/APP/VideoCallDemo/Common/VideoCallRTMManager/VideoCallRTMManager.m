//
//  VideoCallRTMManager.m
//  SceneRTCDemo
//
//  Created by on 2021/3/16.
//

#import "VideoCallRTMManager.h"
#import "VideoCallRTCManager.h"
#import "JoinRTSParams.h"
#import "Core.h"

@implementation VideoCallRTMManager

#pragma mark - Get meeting data

+ (void)joinRoom:(VideoCallRoomUserModel *)loginModel
           block:(void (^)(NSString *token,
                           NSInteger duration,
                           RTMACKModel *model))block {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    [dic setValue:loginModel.uid forKey:@"user_id"];
    [dic setValue:loginModel.roomId forKey:@"room_id"];
    
    NSDictionary *dicData = [JoinRTSParams addTokenToParams:[dic copy]];
    [[VideoCallRTCManager shareRtc] emitWithAck:@"videocallJoinRoom"
                                         with:dicData
                                        block:^(RTMACKModel * _Nonnull ackModel) {
        NSString *token = @"";
        NSInteger duration = 0;
        if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
            token = ackModel.response[@"rtc_token"];
            duration = [[NSString stringWithFormat:@"%@", ackModel.response[@"duration"]] integerValue];
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            if (block) {
                block(token, duration, ackModel);
            }
        });
    }];
}

+ (void)leaveRoom {
    NSDictionary *dic = [JoinRTSParams addTokenToParams:nil];
    [[VideoCallRTCManager shareRtc] emitWithAck:@"videocallLeaveRoom"
                                         with:dic
                                        block:nil];
}

+ (void)reconnectWithBlock:(void (^)(NSString *RTCToken,
                                     RTMACKModel *model))block {
    NSDictionary *dic = [JoinRTSParams addTokenToParams:nil];
    [[VideoCallRTCManager shareRtc] emitWithAck:@"videocallReconnect"
                                         with:dic
                                        block:^(RTMACKModel * _Nonnull ackModel) {
        NSString *token = @"";
        if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
            token = ackModel.response[@"rtc_token"];
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            if (block) {
                block(token, ackModel);
            }
        });
    }];
}

#pragma mark - Notification message

+ (void)onCloseRoomWithBlock:(void (^)(NSString *roomId))block {
    [[VideoCallRTCManager shareRtc] onSceneListener:@"videocallOnCloseRoom" block:^(RTMNoticeModel * _Nonnull noticeModel) {
        NSString *roomId = @"";
        if ([noticeModel.data isKindOfClass:[NSDictionary class]]) {
            roomId = noticeModel.data[@"room_id"];
        }
        if (block) {
            block(roomId);
        }
    }];
}

@end
