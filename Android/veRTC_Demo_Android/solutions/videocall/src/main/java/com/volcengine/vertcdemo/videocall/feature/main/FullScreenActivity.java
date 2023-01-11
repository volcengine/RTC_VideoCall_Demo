package com.volcengine.vertcdemo.videocall.feature.main;

import android.os.Bundle;
import android.text.TextUtils;

import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.bean.VideoCallUserInfo;
import com.volcengine.vertcdemo.videocall.core.VideoCallDataManager;
import com.volcengine.vertcdemo.videocall.event.FullScreenFinishEvent;
import com.volcengine.vertcdemo.videocall.event.RoomFinishEvent;
import com.volcengine.vertcdemo.videocall.event.ScreenShareEvent;
import com.volcengine.vertcdemo.videocall.view.LandScapeScreenLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 屏幕共享横屏展示页面
 * <p>
 * 该页面独立是为了简化UI变化的逻辑
 */
public class FullScreenActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        LandScapeScreenLayout landScapeScreenLayout = findViewById(R.id.full_screen_layout);

        VideoCallUserInfo userInfo = VideoCallDataManager.ins().getScreenShareUser();
        if (userInfo == null || TextUtils.isEmpty(userInfo.userId)) {
            finish();
        }
        landScapeScreenLayout.bind(userInfo);
        landScapeScreenLayout.setZoomAction((ui) -> finish());

        SolutionDemoEventManager.register(this);
    }

    @Override
    protected void setupStatusBar() {
        WindowUtils.setLayoutFullScreen(getWindow());
    }

    @Override
    public void finish() {
        super.finish();
        //本页面关闭时需要通知横屏屏幕共享已经结束
        SolutionDemoEventManager.post(new FullScreenFinishEvent());
        SolutionDemoEventManager.unregister(this);
    }

    //屏幕共享结束时需要关闭本页面
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScreenShareEvent(ScreenShareEvent event) {
        if (!event.isStart) {
            finish();
        }
    }

    //房间关闭时需要关闭本页面
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRoomFinishEvent(RoomFinishEvent event) {
        finish();
    }
}