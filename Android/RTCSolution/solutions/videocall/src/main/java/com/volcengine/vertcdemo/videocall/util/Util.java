package com.volcengine.vertcdemo.videocall.util;

import android.os.Looper;

import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.utils.AppUtil;

public class Util {

    public static String getString(int resId, String... suffix) {
        return AppUtil.getApplicationContext().getString(resId, (Object[]) suffix);
    }

    public static String formatCallDuration(int callDuration) {
        StringBuilder sb = new StringBuilder();
        int min = callDuration / 60;
        if (min < 10) {
            sb.append("0").append(min);
        } else {
            sb.append(min);
        }
        sb.append(":");
        int second = callDuration % 60;
        if (second < 10) {
            sb.append("0").append(second);
        } else {
            sb.append(second);
        }
        return sb.toString();
    }

    public static <T> void notifyExecResult(T data, boolean success, Callback callBack) {
        if (callBack == null) return;
        AppExecutors.execRunnableInMainThread(() -> callBack.onResult(new Callback.Result<>(success, data)));
    }
}
