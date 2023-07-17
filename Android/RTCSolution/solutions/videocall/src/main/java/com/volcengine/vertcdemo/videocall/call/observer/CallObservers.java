package com.volcengine.vertcdemo.videocall.call.observer;

import com.ss.bytertc.engine.data.AudioRoute;
import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.common.IAction;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;

import java.util.HashMap;
import java.util.HashSet;

public class CallObservers implements CallObserver {
    private final HashSet<CallObserver> observers = new HashSet<>(3);
    private boolean isNotifying = false;

    /**
     * 添加RTC相关回调和通话状态相关变化监听器
     */
    public void addObserver(CallObserver callObserver) {
        if (callObserver == null) {
            return;
        }
        addOrRemoveObserver(callObserver, true);
    }

    /**
     * 移除RTC相关回调和通话状态相关变化监听器
     */
    public void removeObserver(CallObserver callObserver) {
        if (callObserver == null) {
            return;
        }
        addOrRemoveObserver(callObserver, false);
    }

    private void addOrRemoveObserver(CallObserver callObserver, boolean add) {
        Runnable task = () -> {
            synchronized (observers) {
                if (add) {
                    observers.add(callObserver);
                } else {
                    observers.remove(callObserver);
                }
            }
        };
        if (isNotifying) {
            AppExecutors.mainThread().execute(task);
        } else {
            task.run();
        }
    }

    @Override
    public void onUserJoined(String userId) {
        invoke(observer -> observer.onUserJoined(userId));
    }

    @Override
    public void onFirstLocalVideoFrameCaptured(){
        invoke(observer -> observer.onFirstLocalVideoFrameCaptured());
    }

    @Override
    public void onFirstRemoteVideoFrameDecoded(String roomId, String userId) {
        invoke(observer -> observer.onFirstRemoteVideoFrameDecoded(roomId, userId));
    }

    @Override
    public void onUserToggleMic(String userId, boolean on) {
        invoke(observer -> observer.onUserToggleMic(userId, on));
    }

    @Override
    public void onUserToggleCamera(String userId, boolean on) {
        invoke(observer -> observer.onUserToggleCamera(userId, on));
    }

    @Override
    public void onAudioRouteChanged(AudioRoute route) {
        invoke(observer -> observer.onAudioRouteChanged(route));
    }

    @Override
    public void onUpdateCallDuration(int callDuration) {
        invoke(observer -> observer.onUpdateCallDuration(callDuration));
    }

    @Override
    public void onCallStateChange(VoipState oldState, VoipState newState, VoipInfo info) {
        invoke(observer -> observer.onCallStateChange(oldState, newState, info));
    }

    @Override
    public void onUserNetQualityChange(HashMap<String, Boolean> blocked) {
        invoke(observer -> observer.onUserNetQualityChange(blocked));
    }

    private void invoke(IAction<CallObserver> action) {
        Runnable runnable = () -> {
            synchronized (observers) {
                //CallObserver回调中可能有从observers中移除观察者，或向obervers增加观察者的任务，从而回
                // 调回addOrRemoveObserver方法，引发ConcurrentModificationException异常，增加isNotifying标志位，
                // 在addOrRemoveObserver方法中判断处理
                isNotifying = true;
                for (CallObserver item : observers) {
                    action.act(item);
                }
                isNotifying = false;
            }
        };
        AppExecutors.execRunnableInMainThread(runnable);
    }

}
