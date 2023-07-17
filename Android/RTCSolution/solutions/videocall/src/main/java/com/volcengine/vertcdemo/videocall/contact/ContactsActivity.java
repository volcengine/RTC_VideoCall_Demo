package com.volcengine.vertcdemo.videocall.contact;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Keep;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vertcdemo.joinrtsparams.bean.ImChannelConfig;
import com.volcengine.vertcdemo.common.IAction;
import com.volcengine.vertcdemo.common.SolutionBaseActivity;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.common.TextWatcherAdapter;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.http.AppNetworkStatusUtil;
import com.volcengine.vertcdemo.im.IMService;
import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.utils.IMEUtils;
import com.volcengine.vertcdemo.videocall.PreviewActivity;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.call.CallEngine;
import com.volcengine.vertcdemo.videocall.call.CallStateHelper;
import com.volcengine.vertcdemo.videocall.call.observer.AbsCallObserver;
import com.volcengine.vertcdemo.videocall.call.observer.CallObserver;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.databinding.ActivityVideoCallContactsBinding;
import com.volcengine.vertcdemo.videocall.model.Contact;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;
import com.volcengine.vertcdemo.videocall.util.Util;

/**
 * 联系人页面
 */
public class ContactsActivity extends SolutionBaseActivity {
    private static final String EXTRA_KEY_CONFIG = "scene_config";
    private ActivityVideoCallContactsBinding mBinding;
    private ImChannelConfig.Scene mScene;
    private ContactAdapter mSearchResultAdapter;
    private ContactAdapter mSearchHistoryAdapter;
    private ContactViewModel mViewModel;

    /**
     * 联系人列表中主动触发音视频通话的监听器
     */
    private final TriggerCallListener mTriggerCallListener = (calleeUid, calleUname) -> {
        if (!AppNetworkStatusUtil.isConnected(getApplicationContext())) {
            SolutionToast.show(R.string.network_not_available_tip);
            return;
        }
        //发起新的通话时检测是否已经存在通话，如果存在需要先结束上次通话
        CallStateHelper.checkCallStateWhenDial(this,
                //展示触发通话对话框
                () -> CallTriggerDialog.showDialog(getSupportFragmentManager(), calleeUid, calleUname));
    };

    /**
     * RTC及通话状态回调
     */
    CallObserver mCallObserver = new AbsCallObserver() {
        @Override
        public void onCallStateChange(VoipState oldState, VoipState newState, VoipInfo info) {
            if (newState == VoipState.RINGING) {
                CallStateHelper.handleRingState(ContactsActivity.this, info, mScene.bid);
            }
            //拨打成功时记录拨打历史
            else if (newState == VoipState.CALLING) {
                String calleeUid = info.calleeUid;
                Contact callee = mSearchResultAdapter == null ? null : mSearchResultAdapter.getContact(calleeUid);
                if (callee == null) {
                    callee = mSearchHistoryAdapter == null ? null : mSearchHistoryAdapter.getContact(calleeUid);
                }
                if (callee != null) {
                    mViewModel.insertHistoryContact(callee);
                }
            }
            //回到闲置状态展示拨打历史记录
            else if (newState == VoipState.IDLE) {
                mViewModel.clearSearchResult();
                mViewModel.loadHistoryContact();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityVideoCallContactsBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ContactViewModel.class);
        setContentView(mBinding.getRoot());
        initData();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CallEngine callEngine = CallEngine.getInstance();
        callEngine.removeObserver(mCallObserver);
        if (!CallStateHelper.existOtherVideoCallActivity(ContactsActivity.class.getCanonicalName())
                && !callEngine.isInFloatWindow()) {
            callEngine.destroy();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        IMEUtils.closeIME(mBinding.getRoot());
        return super.dispatchTouchEvent(ev);
    }

    private void initView() {
        mBinding.titleBarLayout.setTitle(R.string.video_calls);
        mBinding.titleBarLayout.setLeftBack(v -> onBackPressed());
        mBinding.titleBarLayout.setRightIcon(R.drawable.ic_video_effect,
                v -> CallStateHelper.checkCallStateWhenDial(this, this::enterVideoEffectSetting));
        mBinding.selfIdTv.setText(getString(R.string.my_id, SolutionDataManager.ins().getUserId()));

        Drawable searchIcon = ContextCompat.getDrawable(this, R.drawable.ic_search);
        if (searchIcon != null) {
            searchIcon.setBounds(0, 0, 40, 40);
            mBinding.searchView.setCompoundDrawables(searchIcon, null, null, null);
        }
        mBinding.searchView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                return false;
            }
            searchUser();
            return false;
        });
        mBinding.searchBtn.setOnClickListener(v -> searchUser());
        mBinding.searchView.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mViewModel.clearSearchResult();
                    mViewModel.loadHistoryContact();
                }
            }
        });
        mBinding.searchResultRcv.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultAdapter = new ContactAdapter(mTriggerCallListener);
        mBinding.searchResultRcv.setAdapter(mSearchResultAdapter);

        mBinding.searchHistoryRcv.setLayoutManager(new LinearLayoutManager(this));
        mSearchHistoryAdapter = new ContactAdapter(mTriggerCallListener);
        mBinding.searchHistoryRcv.setAdapter(mSearchHistoryAdapter);

        mViewModel.searchResult.observe(this, contacts -> {
            mSearchResultAdapter.replace(contacts);
            handleEmptyView();
        });
        mViewModel.searchHistory.observe(this, contacts -> {
            mSearchHistoryAdapter.replace(contacts);
            handleEmptyView();
        });
    }

    private void initData() {
        //加载搜索历史记录
        mViewModel.loadHistoryContact();
        //初始化RTC引擎和RTS客户端
        Intent intent = getIntent();
        mScene = intent.getParcelableExtra(EXTRA_KEY_CONFIG);
        if (mScene == null) {
            return;
        }
        CallEngine callEngine = CallEngine.getInstance();
        callEngine.init(mScene.rtcAppId, mScene.bid);
        VoipState curState = callEngine.getCurVoipState();
        //用户在场景选择页时以悬浮窗模式通话，重新进入音视频通话场景，不能调用clearUser接口
        if (curState == null || curState == VoipState.IDLE) {
            callEngine.getRTSClient().clearUser();
        }
        callEngine.addObserver(mCallObserver);
    }

    private void handleEmptyView() {
        int searchResultSize = mViewModel.searchResult.getValue() == null ? 0 : mViewModel.searchResult.getValue().size();
        int searchHistorySize = mViewModel.searchHistory.getValue() == null ? 0 : mViewModel.searchHistory.getValue().size();
        if (searchResultSize == 0 && searchHistorySize == 0) {
            mBinding.emptyHintLl.setVisibility(View.VISIBLE);
            mBinding.searchResultRcv.setVisibility(View.GONE);
            mBinding.searchHistoryRcv.setVisibility(View.GONE);
            return;
        }
        mBinding.emptyHintLl.setVisibility(View.GONE);
        mBinding.searchResultRcv.setVisibility(searchResultSize > 0 ? View.VISIBLE : View.GONE);
        if (searchResultSize > 0) {
            mBinding.searchHistoryRcv.setVisibility(View.GONE);
            return;
        }
        mBinding.searchHistoryRcv.setVisibility(View.VISIBLE);
    }

    /**
     * 开启美颜设置
     */
    private void enterVideoEffectSetting() {
        PreviewActivity.start(this);
    }

    /**
     * 搜索可以呼叫用户
     */
    private void searchUser() {
        CharSequence keyWord = mBinding.searchView.getText();
        if (TextUtils.isEmpty(keyWord)
                || !TextUtils.isDigitsOnly(keyWord)
                || keyWord.length() != 10) {
            SolutionToast.show(R.string.search_uid_check_error);
            return;
        }
        if (!AppNetworkStatusUtil.isConnected(getApplicationContext())) {
            SolutionToast.show(R.string.network_link_down);
            return;
        }
        mViewModel.searchUser(
                CallEngine.getInstance().getRTSClient(),
                keyWord.toString(),
                result -> {
                    if (!result.success) {
                        SolutionToast.show(result.result.toString());
                    }
                });
    }

    @Keep
    @SuppressWarnings("unused")
    public static void prepareSolutionParams(Activity activity, IAction<Object> doneAction) {
        ImChannelConfig config = IMService.getService().getConfig();
        ImChannelConfig.Scene scene = config == null ? null : config.getScene("videoone");
        if (scene != null && scene.isValid()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClass(AppUtil.getApplicationContext(), ContactsActivity.class);
            intent.putExtra(EXTRA_KEY_CONFIG, scene);
            activity.startActivity(intent);
        } else {
            SolutionToast.show(Util.getString(R.string.secene_config_empty));
        }
        if (doneAction != null) {
            doneAction.act(null);
        }
    }


    /**
     * 触发音视频电话监听器
     */
    public interface TriggerCallListener {
        void onTrigger(String calleeUid, String calleeUname);
    }
}