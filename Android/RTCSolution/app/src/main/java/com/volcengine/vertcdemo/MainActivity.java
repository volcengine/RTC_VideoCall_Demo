// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.common.SolutionBaseActivity;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.AppTokenExpiredEvent;
import com.volcengine.vertcdemo.core.eventbus.RTSLogoutEvent;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.utils.AgreementManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends SolutionBaseActivity implements AgreementManager.ResultCallback {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AgreementManager.check(this);
        SolutionDemoEventManager.register(this);
    }

    @Override
    public void onResult(boolean agree) {
        if (agree) {
            setContentView(R.layout.activity_main);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SolutionDemoEventManager.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpiredEvent(AppTokenExpiredEvent event) {
        AppExecutors.mainHandler().postDelayed(
                () -> ILoginImpl.getLoginService().showLoginView(this, null),
                500);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRTSLogoutEvent(RTSLogoutEvent event) {
        Log.d(TAG, "onRTSLogoutEvent");
        finishOtherActivity();
        SolutionToast.show(R.string.same_logged_in);
        SolutionDataManager.ins().logout();
    }

    private void finishOtherActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
