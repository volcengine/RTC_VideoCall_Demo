// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.volcengine.vertcdemo.common.IAction;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.login.LoginActivity;
import com.volcengine.vertcdemo.protocol.ILogin;
import com.volcengine.vertcdemo.utils.CloseAccountManager;

import java.util.function.Function;

@SuppressWarnings("unused")
@Keep
public class ILoginImpl implements ILogin {
    private static class Holder {
        private static final ILoginImpl loginService = new ILoginImpl();
    }

    public static ILogin getLoginService() {
        return Holder.loginService;
    }

    private Runnable mSuccessTask;

    public void notifyLoginSuccess() {
        if (mSuccessTask != null) {
            mSuccessTask.run();
        }
    }

    @Override
    public boolean isLogin() {
        return !TextUtils.isEmpty(SolutionDataManager.ins().getToken());
    }

    @Override
    public void showLoginView(Context context, Runnable successTask) {
        if (context == null) {
            return;
        }
        if (isLogin()) {
            if (successTask != null) successTask.run();
            return;
        }
        mSuccessTask = successTask;
        if (!isLogin()) {
            context.startActivity(new Intent(context, LoginActivity.class));
        }
    }

    @Override
    public void closeAccount(IAction<Boolean> action) {
        CloseAccountManager.delete(action);
    }
}
