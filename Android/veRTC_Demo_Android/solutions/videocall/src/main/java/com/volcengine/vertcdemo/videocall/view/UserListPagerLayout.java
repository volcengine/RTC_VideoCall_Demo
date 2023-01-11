package com.volcengine.vertcdemo.videocall.view;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.common.IAction;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.bean.VideoCallUserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 房间内用户信息列表控件
 * <p>
 * 内部使用RecyclerView做展示，每一个元素都是{@link UserRenderView}
 * <p>
 * 功能：
 * 1.设置用户列表 {@link #setUserList(List)}
 * 2.更新用户的视频状态 {@link #updateUserVideoStatus(String, boolean, boolean)}
 * 3.更新用户的音频状态 {@link #updateUserAudioStatus(String, boolean)}
 * 4.更新用户的音量大小展示 {@link #updateUserSpeakingStatus(String, boolean)}
 * 5.设置用户点击事件 {@link #setOnUserViewClick(IAction)}
 */
public class UserListPagerLayout extends FrameLayout {

    private final List<VideoCallUserInfo> mUserInfoList = new ArrayList<>();

    private UserRenderView mMainUserView;
    private UserRenderView mSmallUserView;

    private IAction<VideoCallUserInfo> mUserViewClick;
    private final IAction<VideoCallUserInfo> mUserViewClickDelegate = userInfo -> {
        if (mUserViewClick != null) {
            mUserViewClick.act(userInfo);
        }
    };
    private final UserListPagerAdapter mUserListAdapter = new UserListPagerAdapter(mUserViewClickDelegate);

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == SCROLL_STATE_IDLE) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof GridLayoutManager) {
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                    int pos = gridLayoutManager.findFirstCompletelyVisibleItemPosition();
                    recyclerView.scrollToPosition((pos / 4) * 4);
                }
            }
        }
    };

    public UserListPagerLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public UserListPagerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public UserListPagerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_user_list_pager, this, true);
        RecyclerView userListRv = findViewById(R.id.user_list_rv);
        mMainUserView = findViewById(R.id.main_user_view);
        mSmallUserView = findViewById(R.id.small_user_view);
        mSmallUserView.shrinkPrefixUISize(true);
        userListRv.clearOnScrollListeners();
        userListRv.addOnScrollListener(mOnScrollListener);


        userListRv.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.HORIZONTAL, false));
        userListRv.setAdapter(mUserListAdapter);

        setUserList(null);

        mSmallUserView.setOnClickListener((v) -> exchangedMainAndSmallViewData());
    }

    /**
     * 交换大小视图的数据
     */
    private void exchangedMainAndSmallViewData() {
        if (mUserInfoList.size() != 2) {
            return;
        }
        ArrayList<VideoCallUserInfo> list = new ArrayList<>();
        for (VideoCallUserInfo userInfo: mUserInfoList) {
            list.add(0, userInfo);
        }
        setUserList(list);
    }

    /**
     * 设置用户数据列表
     *
     * @param userInfoList 用户列表
     */
    public void setUserList(List<VideoCallUserInfo> userInfoList) {
        mUserInfoList.clear();
        if (userInfoList != null) {
            mUserInfoList.addAll(userInfoList);
        }

        if (userInfoList == null || userInfoList.size() == 0 || userInfoList.size() > 2) {
            mMainUserView.bindInfo(null);
            mSmallUserView.bindInfo(null);
            mMainUserView.setVisibility(INVISIBLE);
            mSmallUserView.setVisibility(INVISIBLE);
        } else {
            mMainUserView.bindInfo(userInfoList.get(0));
            mMainUserView.setVisibility(VISIBLE);
            if (userInfoList.size() == 2) {
                mSmallUserView.bindInfo(userInfoList.get(1));
                mSmallUserView.setVisibility(VISIBLE);
            } else {
                mSmallUserView.setVisibility(INVISIBLE);
            }
        }

        mUserListAdapter.setData(userInfoList);
    }

    /**
     * 更新用户的音频状态
     *
     * @param uid  用户id
     * @param isOn 是否开启
     */
    public void updateUserAudioStatus(String uid, boolean isOn) {
        mMainUserView.updateAudioStatus(uid, isOn);
        mSmallUserView.updateAudioStatus(uid, isOn);
        mUserListAdapter.updateUserMicStatus(uid, isOn);
    }

    /**
     * 更新用户的视频状态
     *
     * @param uid      用户id
     * @param isScreen 是否是屏幕流
     * @param isOn     是否是开启
     */
    public void updateUserVideoStatus(String uid, boolean isScreen, boolean isOn) {
        mMainUserView.updateVideoStatus(uid, isScreen, isOn);
        mSmallUserView.updateVideoStatus(uid, isScreen, isOn);
        mUserListAdapter.updateUserCameraStatus(uid, isScreen, isOn);
    }

    /**
     * 更新用户的音量大小UI展示
     *
     * @param uid        用户id
     * @param isSpeaking 是否在说话
     */
    public void updateUserSpeakingStatus(String uid, boolean isSpeaking) {
        mMainUserView.updateSpeakingStatus(uid, isSpeaking);
        mSmallUserView.updateSpeakingStatus(uid, isSpeaking);
        mUserListAdapter.updateUserSpeakingStatus(uid, isSpeaking);
    }

    /**
     * 设置用户的点击事件
     *
     * @param userViewClick 点击事件
     */
    public void setOnUserViewClick(IAction<VideoCallUserInfo> userViewClick) {
        mUserViewClick = userViewClick;
    }

    /**
     * 用户列表的adapter
     */
    private static class UserListPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<VideoCallUserInfo> mUserInfoList = new ArrayList<>();
        private final IAction<VideoCallUserInfo> mUserViewClick;

        // 以下的常量用于区分刷新ViewHolder什么内容
        private final String IS_SPEAKING = "is_speaking";
        private final String IS_NOT_SPEAKING = "is_not_speaking";

        private final String MIC_ON = "mic_on";
        private final String MIC_OFF = "mic_off";

        private final String CAMERA_ON = "camera_on";
        private final String CAMERA_OFF = "camera_off";

        private final String SCREEN_SHARE_ON = "screen_share_on";

        public UserListPagerAdapter(IAction<VideoCallUserInfo> userViewClick) {
            mUserViewClick = userViewClick;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            UserRenderView view = new UserRenderView(parent.getContext());
            ViewGroup.LayoutParams layoutParams = new LayoutParams(
                    WindowUtils.getScreenWidth(parent.getContext()) / 2, LayoutParams.MATCH_PARENT);
            view.setLayoutParams(layoutParams);
            return new UserListViewHolder(view, mUserViewClick);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof UserListViewHolder) {
                ((UserListViewHolder) holder).bindInfo(mUserInfoList.get(position));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads);
            } else {
                if (holder instanceof UserListViewHolder) {
                    UserListViewHolder userListViewHolder = (UserListViewHolder) holder;
                    if (payloads.contains(IS_SPEAKING)) {
                        userListViewHolder.updateSpeakingStatus(mUserInfoList.get(position).userId, true);
                    } else if (payloads.contains(IS_NOT_SPEAKING)) {
                        userListViewHolder.updateSpeakingStatus(mUserInfoList.get(position).userId, false);
                    }

                    if (payloads.contains(MIC_ON)) {
                        userListViewHolder.updateMicStatus(mUserInfoList.get(position).userId, true);
                    } else if (payloads.contains(MIC_OFF)) {
                        userListViewHolder.updateMicStatus(mUserInfoList.get(position).userId, false);
                    }

                    if (payloads.contains(CAMERA_ON)) {
                        userListViewHolder.updateCameraStatus(mUserInfoList.get(position).userId, false, true);
                    } else if (payloads.contains(CAMERA_OFF)) {
                        userListViewHolder.updateCameraStatus(mUserInfoList.get(position).userId, false, false);
                    }

                    if (payloads.contains(SCREEN_SHARE_ON)) {
                        userListViewHolder.updateCameraStatus(mUserInfoList.get(position).userId, true, true);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return mUserInfoList.size();
        }

        public void setData(List<VideoCallUserInfo> userInfoList) {
            mUserInfoList.clear();
            if (userInfoList == null || userInfoList.size() <= 2) {
                notifyDataSetChanged();
                return;
            }
            mUserInfoList.addAll(userInfoList);
            if (userInfoList.size() % 4 != 0) {
                // 计算用来填充空位数量，保证列表中用户的数量为4的整数倍
                int padding = (userInfoList.size() / 4 + 1) * 4 - userInfoList.size();
                for (int i = 0; i < Math.abs(padding); i++) {
                    mUserInfoList.add(new VideoCallUserInfo());
                }
            }

            // 调整列表元素位置
            adjustUserInfoPosition(mUserInfoList);

            notifyDataSetChanged();
        }

        /**
         * 调整 userList 内元素的顺序用来适配 RecyclerView 的排布顺序
         * 数据顺序:
         * 1 -> 2
         * 3 -> 4
         * RecyclerView顺序：
         * 1 -> 3
         * 2 -> 4
         * @param userInfoList 数据列表
         */
        private void adjustUserInfoPosition(List<VideoCallUserInfo> userInfoList) {
            int pageCount = userInfoList.size() / 4;
            for (int i = 0; i < pageCount; i++) {
                int curPageSecondItem = i * 4 + 1;
                int curPageThirdItem = i * 4 + 2;
                VideoCallUserInfo userInfo = userInfoList.get(curPageSecondItem);
                userInfoList.set(curPageSecondItem, userInfoList.get(curPageThirdItem));
                userInfoList.set(curPageThirdItem, userInfo);
            }
        }

        public void updateUserMicStatus(String uid, boolean isOn) {
            if (TextUtils.isEmpty(uid)) {
                return;
            }
            for (int i = 0; i < mUserInfoList.size(); i++) {
                VideoCallUserInfo userInfo = mUserInfoList.get(i);
                if (TextUtils.equals(uid, userInfo.userId)) {
                    userInfo.isMicOn = isOn;
                    notifyItemChanged(i, isOn ? MIC_ON : MIC_OFF);
                }
            }
        }

        public void updateUserCameraStatus(String uid, boolean isScreenShare, boolean isOn) {
            if (TextUtils.isEmpty(uid)) {
                return;
            }
            for (int i = 0; i < mUserInfoList.size(); i++) {
                VideoCallUserInfo userInfo = mUserInfoList.get(i);
                if (TextUtils.equals(uid, userInfo.userId)) {
                    userInfo.isCameraOn = isOn;
                    if (isScreenShare) {
                        notifyItemChanged(i, SCREEN_SHARE_ON);
                    } else {
                        notifyItemChanged(i, isOn ? CAMERA_ON : CAMERA_OFF);
                    }
                }
            }
        }

        public void updateUserSpeakingStatus(String uid, boolean isSpeaking) {
            if (TextUtils.isEmpty(uid)) {
                return;
            }
            for (int i = 0; i < mUserInfoList.size(); i++) {
                VideoCallUserInfo userInfo = mUserInfoList.get(i);
                if (TextUtils.equals(uid, userInfo.userId)) {
                    notifyItemChanged(i, isSpeaking ? IS_SPEAKING : IS_NOT_SPEAKING);
                }
            }
        }
    }

    /**
     * 继承 RecyclerView.ViewHolder 的UserRenderView
     * 功能与UserRenderView基本一致
     */
    private static class UserListViewHolder extends RecyclerView.ViewHolder {

        private VideoCallUserInfo mUserInfo;
        private final UserRenderView mUserRenderView;
        private final IAction<VideoCallUserInfo> mUserViewClick;

        public UserListViewHolder(@NonNull View itemView, IAction<VideoCallUserInfo> userViewClick) {
            super(itemView);
            if (itemView instanceof UserRenderView) {
                mUserRenderView = (UserRenderView) itemView;
            } else {
                mUserRenderView = null;
            }
            mUserViewClick = userViewClick;

            itemView.setOnClickListener(v -> {
                if (mUserViewClick != null) {
                    mUserViewClick.act(mUserInfo);
                }
            });
        }

        public void bindInfo(VideoCallUserInfo userInfo) {
            mUserInfo = userInfo;
            if (mUserRenderView != null) {
                mUserRenderView.bindInfo(userInfo);
            }
        }

        public void updateMicStatus(String uid, boolean isOn) {
            if (mUserRenderView != null) {
                mUserRenderView.updateAudioStatus(uid, isOn);
            }
        }

        public void updateCameraStatus(String uid, boolean isScreenShare, boolean isOn) {
            if (mUserRenderView != null) {
                mUserRenderView.updateVideoStatus(uid, isScreenShare, isOn);
            }
        }

        public void updateSpeakingStatus(String uid, boolean isSpeaking) {
            if (mUserRenderView != null) {
                mUserRenderView.updateSpeakingStatus(uid, isSpeaking);
            }
        }
    }
}
