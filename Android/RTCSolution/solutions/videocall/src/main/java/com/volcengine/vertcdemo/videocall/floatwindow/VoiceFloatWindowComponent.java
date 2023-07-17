package com.volcengine.vertcdemo.videocall.floatwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.view.CallActivity;
import com.volcengine.vertcdemo.videocall.call.CallEngine;
import com.volcengine.vertcdemo.videocall.call.observer.AbsCallObserver;
import com.volcengine.vertcdemo.videocall.call.observer.CallObserver;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.databinding.LayoutFloatWindowVoiceBinding;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.util.Util;

public class VoiceFloatWindowComponent {
    private LayoutFloatWindowVoiceBinding mBinding;
    private WindowManager.LayoutParams mWindowParams;
    private WindowManager mWindowManager;
    private Context mContext;
    private View mFloatLayout;
    private float mInViewX;
    private float mInViewY;
    private float mDownInScreenX;
    private float mDownInScreenY;
    private float mInScreenX;
    private float mInScreenY;
    private boolean mInFloatWindow;

    @SuppressLint("StaticFieldLeak")
    private volatile static VoiceFloatWindowComponent mInstance;

    private VoiceFloatWindowComponent() {
        this.mContext = AppUtil.getApplicationContext();
        initFloatWindow();
    }

    public static VoiceFloatWindowComponent getInstance() {
        if (mInstance == null) {
            synchronized (VoiceFloatWindowComponent.class) {
                if (mInstance == null) {
                    mInstance = new VoiceFloatWindowComponent();
                }
            }
        }
        return mInstance;
    }

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 获取相对View的坐标，即以此View左上角为原点
                    mInViewX = motionEvent.getX();
                    mInViewY = motionEvent.getY();
                    // 获取相对屏幕的坐标，即以屏幕左上角为原点
                    mDownInScreenX = motionEvent.getRawX();
                    mDownInScreenY = motionEvent.getRawY() - getSysBarHeight();
                    mInScreenX = motionEvent.getRawX();
                    mInScreenY = motionEvent.getRawY() - getSysBarHeight();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 更新浮动窗口位置参数
                    mInScreenX = motionEvent.getRawX();
                    mInScreenY = motionEvent.getRawY() - getSysBarHeight();
                    mWindowParams.x = (int) (mInScreenX - mInViewX);
                    mWindowParams.y = (int) (mInScreenY - mInViewY);
                    // 手指移动的时候更新小悬浮窗的位置
                    mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
                    break;
                case MotionEvent.ACTION_UP:
                    // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                    if (mDownInScreenX == mInScreenX && mDownInScreenY == mInScreenY) {
                        hideFloatWindow();
                        restoreCallActivity();
                    }
                    break;
            }
            return true;
        }
    };

    private final CallObserver mCallObserver = new AbsCallObserver() {
        @Override
        public void onCallStateChange(VoipState oldState, VoipState newState, VoipInfo info) {
            handleRingingAnimation();
            if (newState == VoipState.IDLE) {
                hideFloatWindow();
            }
        }

        @Override
        public void onUpdateCallDuration(int callDuration) {
            String duration = Util.formatCallDuration(callDuration);
            mBinding.callStatus.setText(duration);
        }

    };

    private void initFloatWindow() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (inflater == null) {
            return;
        }
        mFloatLayout = inflater.inflate(R.layout.layout_float_window_voice, null);
        mBinding = LayoutFloatWindowVoiceBinding.bind(mFloatLayout.getRootView());
        mFloatLayout.setOnTouchListener(mTouchListener);

        mWindowParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }


    private int getSysBarHeight() {
        int result = 0;
        Resources resources = AppUtil.getApplicationContext().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 显示悬浮窗
     */
    public void showFloatWindow(String peerUname) {
        if (mInFloatWindow) {
            return;
        }
        mInFloatWindow = true;
        CallEngine.getInstance().setInFloatWindow(true);
        CallEngine.getInstance().addObserver(mCallObserver);
        if (mFloatLayout.getParent() == null) {
            DisplayMetrics metrics = new DisplayMetrics();
            // 默认固定位置，靠屏幕右边缘的中间
            mWindowManager.getDefaultDisplay().getMetrics(metrics);
            mWindowParams.x = metrics.widthPixels;
            mWindowParams.y = metrics.heightPixels / 2 - getSysBarHeight();
            mWindowManager.addView(mFloatLayout, mWindowParams);
        }
        setPeerNamePrefix(peerUname);
        handleRingingAnimation();
        setCallStatus();
    }


    private void setCallStatus() {
        VoipState state = CallEngine.getInstance().getCurVoipState();
        if (state == VoipState.CALLING) {
            mBinding.callStatus.setText(R.string.float_window_status_calling);
        }
    }

    private void handleRingingAnimation() {
        VoipState state = CallEngine.getInstance().getCurVoipState();
        if (state == VoipState.CALLING || state == VoipState.RINGING) {
            mBinding.peerNamePrefix.startCallingAnimation();
        }
        if (state != VoipState.CALLING && state != VoipState.RINGING) {
            mBinding.peerNamePrefix.stopCallingAnimation();
        }
    }

    /**
     * 隐藏悬浮窗
     */
    private void hideFloatWindow() {
        if (!mInFloatWindow) {
            return;
        }
        mInFloatWindow = false;
        CallEngine.getInstance().setInFloatWindow(false);
        if (mFloatLayout.getParent() != null) {
            mWindowManager.removeView(mFloatLayout);
        }
        CallEngine.getInstance().removeObserver(mCallObserver);
    }

    /**
     * 恢复通话页面
     */
    private void restoreCallActivity() {
        VoipInfo voipInfo = CallEngine.getInstance().getVoipInfo();
        if (voipInfo == null) {
            return;
        }
        CallActivity.start(voipInfo.getCallType(),
                voipInfo.callerUid,
                voipInfo.calleeUid,
                voipInfo.callerUname);
    }

    /**
     * 设置通话对端用户名前缀
     */
    private void setPeerNamePrefix(String peerNamePrefix) {
        if (TextUtils.isEmpty(peerNamePrefix)) {
            return;
        }
        mBinding.peerNamePrefix.setRemoteNamePrefix(peerNamePrefix.substring(0, 1));
    }

    /**
     * 检查是否有悬浮窗权限
     */
    public static boolean hasPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(AppUtil.getApplicationContext());
    }


    public static final int REQUEST_CODE_OVERLAY = 3001;

    /**
     * 跳转设置中心
     */
    public static void startOverlaySetting(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = activity.getPackageName();
            Uri uri = Uri.parse("package:" + packageName);
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
            activity.startActivityForResult(intent, REQUEST_CODE_OVERLAY);
        }
    }
}
