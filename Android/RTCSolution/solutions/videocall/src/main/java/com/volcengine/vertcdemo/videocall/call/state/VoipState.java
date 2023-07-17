package com.volcengine.vertcdemo.videocall.call.state;

/**
 * 呼叫状态: 100 以下是呼叫中状态，100以上是结束状态
 */
public enum VoipState {
    /***空闲状态*/
    IDLE("idle", 0),
    /***用户发起后是 CALLING*/
    CALLING("calling", 1),
    /***接收方拉到呼叫请求后变成 RINGING 状态*/
    RINGING("ringing", 2),
    /*** 接收方接受呼叫*/
    ACCEPTED("accepted", 3),
    /*** 接通*/
    ONTHECALL("on_the_call", 4),

    /*** 任意一方挂断*/
    TERMINATED("terminated", 101),
    /*** 接收方正在通话中*/
    OCCUPIED("occupied", 102),
    /*** 呼叫被拒绝*/
    REFUSED("refused", 103),
    /***取消呼叫*/
    CANCELLED("cancelled", 104),
    /*** 超时*/
    UNAVAILABLE("unavailable", 105),
    /***RTC 错误*/
    RTC_ERROR("rtc_error", 106);

    private String name;
    private int value;

    VoipState(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "VoipState{" +
                "name='" + name + '\'' +
                ", ordinal=" + value +
                '}';
    }
}
