package com.volcengine.vertcdemo.videocall.call;

import com.volcengine.vertcdemo.videocall.util.Callback;

/**
 * 呼叫相关命令，也就是用户主动发起的行为
 */
public enum CallCmd {
    /***主叫：发起音视频呼叫*/
    DIAL("dial"),
    /***主叫：取消呼叫*/
    CANCEL("cancel"),
    /***被叫：接听呼叫*/
    ACCEPT("accept"),
    /*** 被叫：拒绝接听*/
    REFUSE("refuse"),
    /***主/被叫:超时*/
    TIME_OUT("time_out"),
    /***主/被叫:已经在通话中，挂断呼叫*/
    HANGUP("hangup");

    public final String name;

    CallCmd(String name) {
        this.name = name;
    }

    /**
     * 呼叫命令携带的参数
     */
    public static class Params {
        /***呼叫类型*/
        public CallType callType;
        /***主叫用户id*/
        public String callerUid;
        /***被叫用户id*/
        public String calleeUid;
        /***执行结果回调*/
        public Callback callback;

        public Params() {
        }

        public Params(Callback callback) {
            this.callback = callback;
        }
    }
}
