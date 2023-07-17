package com.volcengine.vertcdemo.im;

import java.nio.ByteBuffer;

public interface IMRTSClient {
    /**
     * 收到消息
     *
     * @param message 收到的消息内容
     */
    void onReceivedMessage(String uid, String message);

    /**
     * 收到发送业务服务器消息结果回调
     *
     * @param messageId 消息id
     * @param error     错误码
     * @param message   负载信息
     */
    void onServerMessageSendResult(long messageId, int error, ByteBuffer message);

    /**
     * 发送业务消息到业务服务器
     *
     * @param message 发送内容
     * @return 消息id
     */
    default long sendServerMessage(String message) {
        return IMService.getService().sendServerMessage(message);
    }
}
