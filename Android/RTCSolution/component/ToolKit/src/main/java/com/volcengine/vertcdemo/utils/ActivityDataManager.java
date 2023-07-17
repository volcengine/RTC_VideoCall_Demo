package com.volcengine.vertcdemo.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActivityDataManager {
    private static class Holder {
        private static final ActivityDataManager instance = new ActivityDataManager();
    }

    public static ActivityDataManager getInstance() {
        return Holder.instance;
    }

    private ActivityDataManager() {
    }

    private final Stack<Activity> mActivities = new Stack<>();
    private final List<ForegroundStatusListener> mEnterOrExitForegroundListeners = new ArrayList<>();
    private boolean mIsForeground = false;
    private int mActivityNum;

    /**
     * 初始化
     */
    public void init(@NonNull Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                mActivities.push(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                boolean oldStatus = mIsForeground;
                mActivityNum++;
                mIsForeground = true;
                if (!oldStatus) {
                    notifyForegroundStatusChange();
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                boolean oldStatus = mIsForeground;
                mActivityNum--;
                if (mActivityNum == 0) {
                    mIsForeground = false;
                    if (oldStatus) {
                        notifyForegroundStatusChange();
                    }
                } else if (mActivityNum < 0) {
                    mActivityNum = 0;
                    mIsForeground = false;
                    if (oldStatus) {
                        notifyForegroundStatusChange();
                    }
                }
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                mActivities.pop();
            }
        });
    }

    public void addEnterExistForegroundListener(ForegroundStatusListener listener) {
        synchronized (mEnterOrExitForegroundListeners) {
            mEnterOrExitForegroundListeners.add(listener);
        }
    }

    public void removeExistForegroundListener(ForegroundStatusListener listener) {
        synchronized (mEnterOrExitForegroundListeners) {
            mEnterOrExitForegroundListeners.remove(listener);
        }
    }

    private void notifyForegroundStatusChange() {
        synchronized (mEnterOrExitForegroundListeners) {
            for (ForegroundStatusListener listener : mEnterOrExitForegroundListeners) {
                listener.onChange(mIsForeground);
            }
        }
    }

    /**
     * 当前是否处于前台
     */
    public boolean isForeground() {
        return mIsForeground;
    }

    /**
     * 获取最顶部的Activity
     */
    public Activity getTopActivity() {
        return mActivities.peek();
    }

    /**
     * 获取所有的Activity
     */
    public List<Activity> getActivities() {
        return mActivities;
    }

    /**
     * 进出前后台监听
     */
    public interface ForegroundStatusListener {
        void onChange(boolean enterForeground);
    }

}
