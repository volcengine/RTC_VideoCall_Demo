// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.common;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {
    private static final AppExecutors sInstance = new AppExecutors();
    private final ExecutorService mDiskIO = Executors.newSingleThreadExecutor();
    private final ExecutorService mNetworkIO = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Executor mMainThread = mMainHandler::post;

    private AppExecutors() {
    }

    public static ExecutorService diskIO() {
        return sInstance.mDiskIO;
    }

    public static Executor mainThread() {
        return sInstance.mMainThread;
    }

    public static Handler mainHandler() {
        return sInstance.mMainHandler;
    }

    public static void execRunnableInMainThread(@NonNull Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mainThread().execute(runnable);
        }
    }

    public static ExecutorService networkIO() {
        return sInstance.mNetworkIO;
    }
}
