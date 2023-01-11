package com.volcengine.vertcdemo.videocall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.core.VideoCallRTCManager;

import java.util.Locale;

/**
 * 房间主页顶部功能区域
 * <p>
 * 包含功能：
 * 1.切换前后摄像头（内部直接调用RTC manager的接口）
 * 2.显示房间名 {@link #setRoomId(String)}
 * 3.房间持续时间计时 {@link #startCountDown(long)}
 * 4.离开房间(通过 {@link #setITitleCallback(ITitleCallback)} 将事件回调给调用方)
 */
public class TitleView extends ConstraintLayout {

    private long mStartTs;
    private ITitleCallback mITitleCallback;
    private TextView mRoomTv;
    private TextView mDurationTv;

    public TitleView(@NonNull Context context) {
        super(context);
        initView();
    }

    public TitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TitleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_title, this, true);

        findViewById(R.id.title_zoom).setOnClickListener((v) -> {
            if (mITitleCallback != null) {
                mITitleCallback.onZoomClick();
            }
        });
        findViewById(R.id.title_switch_camera).setOnClickListener((v) ->
                VideoCallRTCManager.ins().switchCamera(!VideoCallRTCManager.ins().isFrontCamera()));
        mRoomTv = findViewById(R.id.title_room_id);
        mDurationTv = findViewById(R.id.title_duration);
        findViewById(R.id.title_hang_up).setOnClickListener((v) -> {
            if (mITitleCallback != null) {
                mITitleCallback.onHangUpClick();
            }
        });
    }

    public void setITitleCallback(ITitleCallback callback) {
        this.mITitleCallback = callback;
    }

    public void setRoomId(@Nullable String roomId) {
        if (roomId == null) {
            roomId = "";
        }
        // todo 隔离临时方案
        mRoomTv.setText(String.format("ID: %s", roomId.replace("call_", "")));
    }

    /**
     * 开始倒计时
     *
     * @param lastTimeMs 持续时间，单位ms
     */
    public void startCountDown(long lastTimeMs) {
        mStartTs = System.currentTimeMillis() - lastTimeMs;
        updateDuration();
    }

    /**
     * 循环更新倒计时
     */
    private void updateDuration() {
        long lastTs = System.currentTimeMillis() - mStartTs;
        mDurationTv.setText(formatTs(lastTs));

        mDurationTv.postDelayed(this::updateDuration, 500);
    }

    /**
     * 格式化时间
     *
     * @param time 时间戳，单位ms
     * @return mm:ss
     */
    private String formatTs(long time) {
        long minute = time / 60_000;
        String mStr;
        if (minute >= 10) {
            mStr = String.format(Locale.US, "%d", minute);
        } else if (minute > 0) {
            mStr = String.format(Locale.US, "0%d", minute);
        } else {
            mStr = "00";
        }

        long second = (time / 1000) % 60;
        String sStr;
        if (second >= 10) {
            sStr = String.format(Locale.US, "%d", second);
        } else if (second > 0) {
            sStr = String.format(Locale.US, "0%d", second);
        } else {
            sStr = "00";
        }

        return String.format(Locale.US, "%s:%s", mStr, sStr);
    }

    public interface ITitleCallback {

        void onZoomClick();

        void onHangUpClick();
    }
}
