// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BaseRTSManager : NSObject

/**
 * @brief 发出 RTS 请求
 * @param event 请求事件KEY
 * @param item 请求参数
 * @param block 发出 RTS 请求结果回调
 */
- (void)emitWithAck:(NSString *)event
               with:(NSDictionary *)item
              block:(__nullable RTCSendServerMessageBlock)block;

/**
 * @brief 注册 RTS 监听
 * @param key 注册监听需要的key
 * @param block 当收到监听时回调
 */
- (void)onSceneListener:(NSString *)key
                  block:(RTCRoomMessageBlock)block;

/**
 * @brief 发送 p2server 消息的结果回调
 * @param msgid 本条消息的 ID
 * @param error 消息发送结果
 * @param message 应用服务器收到 HTTP 请求后，在 ACK 中返回的信息
 */
- (void)onServerMessageSendResult:(int64_t)msgid
                            error:(ByteRTCUserMessageSendResult)error
                          message:(NSData *)message;

/**
 * @brief 收到远端发来的消息
 * @param uid 用户 ID
 * @param message 消息内容
 */
- (void)onMessageReceived:(NSString *)uid
                  message:(NSString *)message;

/**
 * @brief 链接RTS成功
 */
- (void)connectRTSSuccessful;


@end

NS_ASSUME_NONNULL_END
