package com.volcengine.vertcdemo.videocall.call;

import com.volcengine.vertcdemo.videocall.model.VoipInfo;

/**
 * 收到的跟通话相关的事件
 */
public enum CallEvent {

    /***主叫：收到被叫接通*/
    ANSWER_CALL("answer_call"),
    /***主叫：收到被叫拒绝*/
    REFUSED("refused"),
    /***主叫：收到被叫应答超时*/
    TIMEOUT("time_out"),

    /*** 被叫：收到呼叫*/
    RINGING("ringing"),
    /*** 被叫：收到主叫取消呼叫*/
    CANCELED("canceled"),
    /***主/被叫:收到挂断呼叫*/
    HANGUP("hangup"),
    /***主/被叫:RTC 出错*/
    RTC_ERROR("rtc_error");

    public final String name;
    CallEvent(String name) {
        this.name = name;
    }

    /**
     * 通话相关的事件携带的信息
     */
   public static class Info{
       public String userId;
       public String appId;
       public VoipInfo voipInfo;
   }
}
