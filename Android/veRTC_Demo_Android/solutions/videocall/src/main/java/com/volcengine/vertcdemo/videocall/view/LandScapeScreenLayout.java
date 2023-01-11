package com.volcengine.vertcdemo.videocall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.common.IAction;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.Utils;
import com.volcengine.vertcdemo.videocall.bean.VideoCallUserInfo;
import com.volcengine.vertcdemo.videocall.core.VideoCallRTCManager;

/**
 * 横屏屏幕共享页面控件
 * <p>
 * 用于横屏页面数据展示
 * <p>
 * 功能：
 * 1.绑定数据 {@link #bind(VideoCallUserInfo)}
 * 2.设置缩小回调 {@link #setZoomAction(IAction)}
 */
public class LandScapeScreenLayout extends FrameLayout {

    public FrameLayout mContainer;
    public TextView mUserName;
    public ImageView mUserMic;
    public View mZoom;

    public VideoCallUserInfo mUserInfo;
    private IAction<VideoCallUserInfo> mZoomAction;

    public LandScapeScreenLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public LandScapeScreenLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LandScapeScreenLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_full_screen, this, true);
        mContainer = findViewById(R.id.full_screen_user_render_container);
        mUserName = findViewById(R.id.full_screen_user_name);
        mUserMic = findViewById(R.id.full_screen_user_mic);
        mZoom = findViewById(R.id.full_screen_user_render_zoom);
        mZoom.setOnClickListener((v -> {
            if (mZoomAction != null) {
                mZoomAction.act(mUserInfo);
            }
        }));
    }

    /**
     * 绑定用户数据
     *
     * @param userInfo 用户信息
     */
    public void bind(@Nullable VideoCallUserInfo userInfo) {
        mContainer.removeAllViews();
        mUserInfo = userInfo;
        if (userInfo == null) {
            mUserName.setText("");
            mUserMic.setImageResource(R.drawable.micro_phone_enable_icon);
            mContainer.removeAllViews();
            return;
        }
        mUserName.setText(getResources().getString(R.string.user_render_view_screen_share, userInfo.userName));
        ViewGroup.LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        TextureView view = VideoCallRTCManager.ins().getScreenRenderView(userInfo.userId);
        Utils.attachViewToViewGroup(mContainer, view, layoutParams);
    }

    /**
     * 设置缩小事件回调
     *
     * @param zoomAction 回调
     */
    public void setZoomAction(@Nullable IAction<VideoCallUserInfo> zoomAction) {
        mZoomAction = zoomAction;
    }
}
