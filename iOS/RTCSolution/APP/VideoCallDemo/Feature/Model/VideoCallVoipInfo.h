// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, VideoCallType) {
    VideoCallTypeVideo = 1,
    VideoCallTypeAudio = 2,
};

typedef NS_ENUM(NSInteger, VideoCallState) {
    VideoCallStateIdle          = 0, // 初始状态
    VideoCallStateCalling       = 1, // 主叫呼叫中
    VideoCallStateRinging       = 2, // 被叫被呼叫中
    VideoCallStateAccept        = 3, // 被叫接受通话邀请
    VideoCallStateOnTheCall     = 4, // 通话中
    
    VideoCallStateTerminated     = 101, // 任意一方挂断
    VideoCallStateOccupied       = 102, // 接收方正在通话中
    VideoCallStateRefused        = 103, // 接收方拒绝
    VideoCallStateCancelled      = 104, // 发起方取消
    VideoCallStateUnavailable    = 105, // 发起方超时，感觉没啥必要
    VideoCallStateRTCError       = 106, // RTC 出错
    VideoCallStateConflictCall   = 107, // 互相拨打而挂断
};

typedef NS_ENUM(NSInteger, VideoCallEventType) {
    VideoCallEventTypeCreateRoom = 1,       //被叫：收到被呼叫
    VideoCallEventTypeAnswerCall,           // 主叫：接收到被叫接通
    VideoCallEventTypeLeaveRoom,            // 主叫：收到被叫拒绝、收到被叫挂断、收到被叫应答超时 被叫：收到主叫挂断
    VideoCallEventTypeAccepted,             // 主叫：收到被叫接通
    VideoCallEventTypeCancel,               // 被叫：收到主叫取消呼叫
    VideoCallEventTypeOverTime,             // 体验超时
};

@interface VideoCallVoipInfo : NSObject

@property (nonatomic, copy) NSString *roomId;
@property (nonatomic, copy) NSString *token;
@property (nonatomic, copy) NSString *userId;
@property (nonatomic, copy) NSString *fromUserId;
@property (nonatomic, copy) NSString *fromUserName;
@property (nonatomic, copy) NSString *toUserId;
@property (nonatomic, copy) NSString *toUserName;

@property (nonatomic, assign) VideoCallType callType;
@property (nonatomic, assign) VideoCallState status;

- (NSString *)showUserName;

@end

NS_ASSUME_NONNULL_END
