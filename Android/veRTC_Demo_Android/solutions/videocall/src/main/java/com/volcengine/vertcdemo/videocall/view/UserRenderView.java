package com.volcengine.vertcdemo.videocall.view;

import android.content.Context;
import android.text.TextUtils;
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

import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.Utils;
import com.volcengine.vertcdemo.videocall.bean.VideoCallUserInfo;
import com.volcengine.vertcdemo.videocall.core.VideoCallRTCManager;

/**
 * 用户列表中单个用户控件
 * <p>
 * 功能：
 * 1.绑定用户信息 {@link #bindInfo(VideoCallUserInfo)}
 * 2.更新用户视频状态 {@link #updateVideoStatus(String, boolean, boolean)}
 * 3.更新用户音频状态 {@link #updateAudioStatus(String, boolean)}
 * 4.更新用户说话音量状态 {@link #updateSpeakingStatus(String, boolean)}
 * 5.改变内部元素尺寸 {@link #shrinkPrefixUISize(boolean)}
 */
public class UserRenderView extends FrameLayout {

    private FrameLayout mRenderContainer;
    private View mSpeakingStatus;
    private View mExtraInfoLayout;
    private ImageView mMicOn;
    private TextView mUserName;
    private TextView mUserNamePrefix;

    private VideoCallUserInfo mUserInfo;

    public UserRenderView(@NonNull Context context) {
        super(context);
        initView();
    }

    public UserRenderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public UserRenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_user_render_view, this, true);
        mRenderContainer = findViewById(R.id.user_render_container);
        mUserName = findViewById(R.id.user_render_name);
        mUserNamePrefix = findViewById(R.id.user_render_name_prefix);
        mExtraInfoLayout = findViewById(R.id.user_extra_info_layout);
        mMicOn = findViewById(R.id.user_render_mic);
        mSpeakingStatus = findViewById(R.id.user_render_name_speaking);
    }

    /**
     * 缩小用户名首字母显示和音量显示控件尺寸
     *
     * @param shrink 是否缩小
     */
    public void shrinkPrefixUISize(boolean shrink) {
        int prefixSize;
        int speakStatusSize;
        if (shrink) {
            prefixSize = (int) Utilities.dip2Px(42);
            speakStatusSize = (int) Utilities.dip2Px(58);
        } else {
            prefixSize = (int) Utilities.dip2Px(80);
            speakStatusSize = (int) Utilities.dip2Px(112);
        }
        ViewGroup.LayoutParams prefixParams = mUserNamePrefix.getLayoutParams();
        prefixParams.width = prefixSize;
        prefixParams.height = prefixSize;
        mUserNamePrefix.setLayoutParams(prefixParams);
        ViewGroup.LayoutParams speakingParams = mSpeakingStatus.getLayoutParams();
        speakingParams.width = speakStatusSize;
        speakingParams.height = speakStatusSize;
        mSpeakingStatus.setLayoutParams(speakingParams);
    }

    /**
     * 绑定用户信息
     *
     * @param userInfo 用户信息
     */
    public void bindInfo(VideoCallUserInfo userInfo) {
        boolean isSelf = userInfo != null && TextUtils.equals(userInfo.userId,
                SolutionDataManager.ins().getUserId());
        mUserInfo = userInfo;

        mSpeakingStatus.setVisibility(INVISIBLE);
        if (userInfo == null || TextUtils.isEmpty(userInfo.userId)) {
            mUserName.setText("");
            mRenderContainer.removeAllViews();
            mUserNamePrefix.setVisibility(INVISIBLE);
            mExtraInfoLayout.setVisibility(INVISIBLE);
            mSpeakingStatus.setVisibility(INVISIBLE);
        } else {
            mExtraInfoLayout.setVisibility(VISIBLE);
            String userName = String.format("%s%s", userInfo.userName,
                    isSelf ? getContext().getString(R.string.user_render_view_me) : "");

            if (userInfo.isScreenShare) {
                userName = getResources().getString(R.string.user_render_view_screen_share, userName);
            }
            mUserName.setText(userName);

            updateAudioStatus(userInfo.userId, userInfo.isMicOn);

            updateVideoStatus(userInfo.userId, userInfo.isScreenShare, userInfo.isCameraOn);
        }
    }

    /**
     * 根据视频开关状态更新UI
     *
     * @param uid      用户id
     * @param isScreen 是不是屏幕流
     * @param isOn     是否打开
     */
    public void updateVideoStatus(String uid, boolean isScreen, boolean isOn) {
        if (mUserInfo == null || TextUtils.isEmpty(uid)) {
            return;
        }
        if (!TextUtils.equals(uid, mUserInfo.userId)) {
            return;
        }
        if (isScreen != mUserInfo.isScreenShare) {
            return;
        }
        mUserInfo.isCameraOn = isOn;

        if (isOn || mUserInfo.isScreenShare) {
            mUserNamePrefix.setVisibility(INVISIBLE);
            mSpeakingStatus.setVisibility(INVISIBLE);

            TextureView view;
            if (mUserInfo.isScreenShare) {
                view = VideoCallRTCManager.ins().getScreenRenderView(uid);
            } else {
                view = VideoCallRTCManager.ins().getRenderView(uid);
            }
            ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            Utils.attachViewToViewGroup(mRenderContainer, view, params);

            if (TextUtils.equals(mUserInfo.userId, SolutionDataManager.ins().getUserId())) {
                VideoCallRTCManager.ins().setLocalVideoCanvas(mUserInfo.isScreenShare, view);
            } else {
                VideoCallRTCManager.ins().setRemoteVideCanvas(mUserInfo.userId, mUserInfo.isScreenShare, view);
            }
        } else {
            mRenderContainer.removeAllViews();
            mUserNamePrefix.setVisibility(VISIBLE);
            mUserNamePrefix.setText(mUserInfo.getNamePrefix());
        }
    }

    /**
     * 根据音频开关状态更新UI
     *
     * @param uid  用户id
     * @param isOn 是否打开
     */
    public void updateAudioStatus(String uid, boolean isOn) {
        if (mUserInfo == null || TextUtils.isEmpty(uid)) {
            return;
        }
        if (!TextUtils.equals(uid, mUserInfo.userId)) {
            return;
        }
        mUserInfo.isMicOn = isOn;

        int resId;
        if (isOn) {
            resId = R.drawable.micro_phone_enable_icon;
        } else {
            resId = R.drawable.micro_phone_disable_icon;
        }
        mMicOn.setImageResource(resId);
    }

    /**
     * 根据是否正在说话更新UI
     *
     * @param uid        用户id
     * @param isSpeaking 是否正在说话
     */
    public void updateSpeakingStatus(String uid, boolean isSpeaking) {
        if (mUserInfo == null || TextUtils.isEmpty(uid)) {
            return;
        }
        if (!TextUtils.equals(uid, mUserInfo.userId)) {
            return;
        }
        if (!mUserInfo.isCameraOn) {
            mSpeakingStatus.setVisibility(isSpeaking ? VISIBLE : INVISIBLE);
        }

        int resId;
        if (mUserInfo.isMicOn) {
            if (isSpeaking) {
                resId = R.drawable.micro_phone_active_icon;
            } else {
                resId = R.drawable.micro_phone_enable_icon;
            }
        } else {
            resId = R.drawable.micro_phone_disable_icon;
        }
        mMicOn.setImageResource(resId);
    }
}
