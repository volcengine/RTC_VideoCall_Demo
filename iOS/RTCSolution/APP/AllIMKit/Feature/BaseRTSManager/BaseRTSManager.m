// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "BaseRTSManager.h"
#import "IMService.h"

typedef NSString* RTSMessageType;
static RTSMessageType const RTSMessageTypeResponse = @"return";
static RTSMessageType const RTSMessageTypeNotice = @"inform";

@interface BaseRTSManager ()

@property (nonatomic, strong) NSMutableDictionary *listenerDic;
@property (nonatomic, strong) NSMutableDictionary *senderDic;

@end

@implementation BaseRTSManager

#pragma mark - public

- (void)emitWithAck:(NSString *)event
               with:(NSDictionary *)item
              block:(RTCSendServerMessageBlock)block {
    if (IsEmptyStr(event)) {
        [self throwErrorAck:RTSStatusCodeInvalidArgument
                    message:@"Lack EventName"
                      block:block];
        return;
    }

    NSString *appId = [[IMService shared] getRTSAppId];
    NSString *roomId = @"";
    if ([item isKindOfClass:[NSDictionary class]]) {
        roomId = item[@"room_id"];
    }
    NSString *wisd = [NetworkingTool getWisd];
    RTSRequestModel *requestModel = [[RTSRequestModel alloc] init];
    requestModel.imChannel = YES;
    requestModel.eventName = event;
    requestModel.app_id = appId;
    requestModel.roomID = roomId;
    requestModel.userID = [LocalUserComponent userModel].uid;
    requestModel.requestID = [NetworkingTool MD5ForLower16Bate:wisd];
    requestModel.content = [item yy_modelToJSONString];
    requestModel.deviceID = [NetworkingTool getDeviceId];
    requestModel.requestBlock = block;
    NSString *json = [requestModel yy_modelToJSONString];
    // 客户端向应用服务器发送一条文本消息（P2Server）
    requestModel.msgid = (NSInteger)[[IMService shared] sendServerMessage:json];
    
    NSString *key = requestModel.requestID;
    [self.senderDic setValue:requestModel forKey:key];
    [self addLog:@"sendServerMessage-" message:json];
}

- (void)onSceneListener:(NSString *)key
                  block:(RTCRoomMessageBlock)block {
    if (IsEmptyStr(key)) {
        return;
    }
    [self.listenerDic setValue:block forKey:key];
}

- (void)onServerMessageSendResult:(int64_t)msgid error:(ByteRTCUserMessageSendResult)error message:(NSData *)message {
    if (error != ByteRTCUserMessageSendResultSuccess) {
        // 发送失败
        // Failed to send
        NSString *key = @"";
        for (RTSRequestModel *model in self.senderDic.allValues) {
            if (model.msgid == msgid) {
                key = model.requestID;
                [self throwErrorAck:RTSStatusCodeSendMessageFaild
                            message:[NetworkingTool messageFromResponseCode:RTSStatusCodeSendMessageFaild]
                              block:model.requestBlock];
                NSLog(@"[%@]-收到消息发送结果 %@ msgid %lld request_id %@ ErrorCode %ld", [self class], model.eventName, msgid, key, (long)error);
                break;
            }
        }
        if (NOEmptyStr(key)) {
            [self.senderDic removeObjectForKey:key];
        }
    }
}

- (void)onMessageReceived:(NSString *)uid message:(NSString *)message {
    [self dispatchMessageFrom:uid message:message];
    [self addLog:@"onMessageReceived-" message:message];
}

- (void)connectRTSSuccessful {
    // 需要子类重写
}

#pragma mark - private
- (void)throwErrorAck:(NSInteger)code message:(NSString *)message
                block:(__nullable RTCSendServerMessageBlock)block {
    if (!block) {
        return;
    }
    RTSACKModel *ackModel = [[RTSACKModel alloc] init];
    ackModel.code = code;
    ackModel.message = message;
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        block(ackModel);
    });
}

- (void)dispatchMessageFrom:(NSString *)uid message:(NSString *)message {
    NSDictionary *dic = [NetworkingTool decodeJsonMessage:message];
    if (!dic || !dic.count) {
        return;
    }
    NSString *messageType = dic[@"message_type"];
    if ([messageType isKindOfClass:[NSString class]] &&
        [messageType isEqualToString:RTSMessageTypeResponse]) {
        [self receivedResponseFrom:uid object:dic];
        return;
    }
    
    if ([messageType isKindOfClass:[NSString class]] &&
        [messageType isEqualToString:RTSMessageTypeNotice]) {
        [self receivedNoticeFrom:uid object:dic];
        return;
    }
}

- (void)receivedResponseFrom:(NSString *)uid object:(NSDictionary *)object {
    RTSACKModel *ackModel = [RTSACKModel modelWithMessageData:object];
    if (IsEmptyStr(ackModel.requestID)) {
        return;
    }
    NSString *key = ackModel.requestID;
    RTSRequestModel *model = self.senderDic[key];
    if (model && [model isKindOfClass:[RTSRequestModel class]]) {
        if (model.requestBlock) {
            dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
                model.requestBlock(ackModel);
            });
        }
    }
    [self.senderDic removeObjectForKey:key];
}

- (void)receivedNoticeFrom:(NSString *)uid object:(NSDictionary *)object {
    RTSNoticeModel *noticeModel = [RTSNoticeModel yy_modelWithJSON:object];
    if (IsEmptyStr(noticeModel.eventName)) {
        return;
    }
    RTCRoomMessageBlock block = self.listenerDic[noticeModel.eventName];
    if (block) {
        dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
            block(noticeModel);
        });
    }
}

#pragma mark - Tool

- (void)addLog:(NSString *)key message:(NSString *)message {
    NSLog(@"[%@]-%@ %@", [self class], key, [NetworkingTool decodeJsonMessage:message]);
}

#pragma mark - getter

- (NSMutableDictionary *)listenerDic {
    if (!_listenerDic) {
        _listenerDic = [[NSMutableDictionary alloc] init];
    }
    return _listenerDic;
}

- (NSMutableDictionary *)senderDic {
    if (!_senderDic) {
        _senderDic = [[NSMutableDictionary alloc] init];
    }
    return _senderDic;
}

@end
