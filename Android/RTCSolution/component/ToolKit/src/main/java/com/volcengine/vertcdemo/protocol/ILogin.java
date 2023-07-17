// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.protocol;

import android.content.Context;

import com.volcengine.vertcdemo.common.IAction;

public interface ILogin {
    boolean isLogin();

    void notifyLoginSuccess();

    void showLoginView(Context context, Runnable successTask);

    void closeAccount(IAction<Boolean> action);
}
