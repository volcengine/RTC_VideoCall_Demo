package com.volcengine.vertcdemo.videocall.call;

/**
 * 通话类型
 */
public enum CallType {
    /**
     * 视频通话
     */
    VIDEO(1),
    /**
     * 语音通话
     */
    VOICE(2);


    private final int value;

    CallType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CallType formValue(int value) {
        if (value == VIDEO.getValue()) {
            return VIDEO;
        } else {
            return VOICE;
        }
    }
}
