// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo;

import android.app.Application;
import android.text.TextUtils;

import com.volcengine.vertcdemo.common.SPUtils;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.http.AppNetworkStatusUtil;
import com.volcengine.vertcdemo.im.IMService;
import com.volcengine.vertcdemo.utils.ActivityDataManager;
import com.volcengine.vertcdemo.utils.AppUtil;

public class SolutionApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtil.initApp(this);
        String localUid = SolutionDataManager.ins().getUserId();
        String loginToken = SolutionDataManager.ins().getToken();
        if (!TextUtils.isEmpty(localUid) && !TextUtils.isEmpty(loginToken)) {
            IMService.getService().init(this, localUid, loginToken, null);
        }
        ActivityDataManager.getInstance().init(this);
        new CrashHandler(this);
        AppNetworkStatusUtil.registerNetworkCallback(this);
    }
}
