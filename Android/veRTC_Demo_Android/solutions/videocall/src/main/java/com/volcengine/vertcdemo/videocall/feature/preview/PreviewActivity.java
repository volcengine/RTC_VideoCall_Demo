package com.volcengine.vertcdemo.videocall.feature.preview;

import static com.volcengine.vertcdemo.core.net.rts.RTSInfo.KEY_RTM;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Keep;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.adapter.TextWatcherAdapter;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.vertcdemo.joinrtsparams.bean.JoinRTSRequest;
import com.vertcdemo.joinrtsparams.common.JoinRTSManager;
import com.volcengine.vertcdemo.common.IAction;
import com.volcengine.vertcdemo.common.LengthFilterWithCallback;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.ErrorTool;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.ServerResponse;
import com.volcengine.vertcdemo.core.net.rts.RTSBaseClient;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.Utils;
import com.volcengine.vertcdemo.videocall.bean.JoinRoomResponse;
import com.volcengine.vertcdemo.videocall.core.Constants;
import com.volcengine.vertcdemo.videocall.core.VideoCallRTCManager;
import com.volcengine.vertcdemo.videocall.event.AudioRouterEvent;
import com.volcengine.vertcdemo.videocall.event.MediaStatusEvent;
import com.volcengine.vertcdemo.videocall.event.RefreshPreviewEvent;
import com.volcengine.vertcdemo.videocall.feature.main.RoomMainActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Pattern;

/**
 * 预览页面
 * <p>
 * 功能：
 * 1.控制摄像头、麦克风、扬声器开关状态
 * 2.预览自己视频
 * 3.进入房间
 */
public class PreviewActivity extends BaseActivity {

    private static final String TAG = "PreviewActivity";

    public static final String ROOM_INPUT_REGEX = "^[0-9]+$";

    private RTSInfo mRtmInfo; // 该场景需要用到的rtm相关数据

    private String mRoomId;

    private FrameLayout mRenderContainer;
    private FrameLayout mRenderDisable;
    private EditText mInputEt;
    private TextView mInputErrorTv;
    private ImageView mMicBtn;
    private ImageView mCameraBtn;
    private ImageView mSpeakerPhoneBtn;
    private TextView mJoinRoomBtn;

    private boolean mRoomIdOverflow = false;

    private final Runnable mRoomIdDismissRunnable = () -> mInputErrorTv.setVisibility(View.GONE);

    private final TextWatcherAdapter mTextWatcher = new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            updateJoinButtonStatus();
        }
    };

    private final IRequestCallback<JoinRoomResponse> mJoinCallback = new IRequestCallback<JoinRoomResponse>() {
        @Override
        public void onSuccess(JoinRoomResponse data) {
            Intent intent = new Intent(PreviewActivity.this, RoomMainActivity.class);
            intent.putExtra(Constants.EXTRA_ROOM_ID, mRoomId);
            intent.putExtra(Constants.EXTRA_TOKEN, data.rtcToken);
            intent.putExtra(Constants.EXTRA_LAST_TS, data.durationS * 1000);
            startActivity(intent);
        }

        @Override
        public void onError(int errorCode, String message) {
            SafeToast.show(ErrorTool.getErrorMessageByErrorCode(errorCode, message));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        initRTSInfo();

        SafeToast.show(R.string.preview_experience_tip);
    }

    @Override
    protected void setupStatusBar() {
        WindowUtils.setLayoutFullScreen(getWindow());
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        super.onGlobalLayoutCompleted();

        findViewById(R.id.preview_back_btn).setOnClickListener((v) -> finish());
        mRenderContainer = findViewById(R.id.preview_render_container);
        mRenderDisable = findViewById(R.id.preview_render_disable);
        mInputEt = findViewById(R.id.preview_input_et);
        mInputEt.removeTextChangedListener(mTextWatcher);
        mInputEt.addTextChangedListener(mTextWatcher);

        InputFilter roomIDFilter = new LengthFilterWithCallback(18, (overflow) -> {
            if (overflow) {
                mInputErrorTv.setVisibility(View.VISIBLE);
                mInputErrorTv.setText(R.string.preview_input_room_id_waring);
                mInputErrorTv.removeCallbacks(mRoomIdDismissRunnable);
                mInputErrorTv.postDelayed(mRoomIdDismissRunnable, 2500);
            } else {
                mInputErrorTv.setVisibility(View.GONE);
                mInputErrorTv.removeCallbacks(mRoomIdDismissRunnable);
            }
            mRoomIdOverflow = overflow;
        });
        InputFilter[] meetingIDFilters = new InputFilter[]{roomIDFilter};
        mInputEt.setFilters(meetingIDFilters);

        mInputErrorTv = findViewById(R.id.preview_input_error);
        mJoinRoomBtn = findViewById(R.id.preview_enter_btn);
        mJoinRoomBtn.setOnClickListener((v) -> enterRoom());
        mJoinRoomBtn.setEnabled(false);

        mMicBtn = findViewById(R.id.preview_mic_btn);
        mCameraBtn = findViewById(R.id.preview_camera_btn);
        mSpeakerPhoneBtn = findViewById(R.id.preview_speaker_phone_btn);

        TextView versionBtn = findViewById(R.id.preview_version);
        versionBtn.setText(getString(R.string.preview_version,
                SolutionDataManager.ins().getAppVersionName(),
                RTCEngine.getSdkVersion()));

        initRTC();

        SolutionDemoEventManager.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRenderDisable != null) {
            setLocalVideoCanvas();
        }
        if (VideoCallRTCManager.ins().isCameraOn()) {
            VideoCallRTCManager.ins().startVideoCapture(true);
        }
        if (VideoCallRTCManager.ins().isMicOn()) {
            VideoCallRTCManager.ins().startPublishAudio(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SolutionDemoEventManager.unregister(this);
        VideoCallRTCManager.ins().clearUserView();
        VideoCallRTCManager.ins().destroyEngine();
    }

    /**
     * 从Intent中获取RTM信息
     */
    private void initRTSInfo() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mRtmInfo = intent.getParcelableExtra(RTSInfo.KEY_RTM);
        if (mRtmInfo == null || !mRtmInfo.isValid()) {
            finish();
        }
    }

    /**
     * 根据从服务端获取的RTM信息初始化RTC
     */
    private void initRTC() {
        VideoCallRTCManager.ins().initEngine(mRtmInfo);
        RTSBaseClient rtsClient = VideoCallRTCManager.ins().getRTSClient();
        if (rtsClient == null) {
            finish();
            return;
        }
        rtsClient.login(mRtmInfo.rtmToken, (resultCode, message) -> {
            if (resultCode == RTSBaseClient.LoginCallBack.SUCCESS) {
                initView();
            } else {
                SafeToast.show("Login Rtm Fail Error:" + resultCode + ",Message:" + message);
            }
        });
    }

    private void initView() {
        mCameraBtn.setImageResource(VideoCallRTCManager.ins().isCameraOn()
                ? R.drawable.camera_enable_icon : R.drawable.camera_disable_icon);
        mMicBtn.setImageResource(VideoCallRTCManager.ins().isMicOn()
                ? R.drawable.micro_phone_enable_icon : R.drawable.micro_phone_disable_icon);
        mSpeakerPhoneBtn.setImageResource(VideoCallRTCManager.ins().isSpeakerphone()
                ? R.drawable.speakerphone_icon : R.drawable.earpiece_icon);

        mCameraBtn.setOnClickListener((v) -> VideoCallRTCManager.ins()
                .startVideoCapture(!VideoCallRTCManager.ins().isCameraOn()));
        mMicBtn.setOnClickListener((v) -> VideoCallRTCManager.ins()
                .startPublishAudio(!VideoCallRTCManager.ins().isMicOn()));
        mSpeakerPhoneBtn.setOnClickListener((v) -> VideoCallRTCManager.ins()
                .useSpeakerphone(!VideoCallRTCManager.ins().isSpeakerphone()));

        setLocalVideoCanvas();
    }

    /**
     * 根据用户输入的房间名，控制加入房间按钮的状态
     */
    private void updateJoinButtonStatus() {
        int roomIDLength = mInputEt.getText().length();
        boolean meetingIdRegexMatch = false;
        if (Pattern.matches(ROOM_INPUT_REGEX, mInputEt.getText().toString())) {
            if (mRoomIdOverflow) {
                mInputErrorTv.setVisibility(View.VISIBLE);
                mInputErrorTv.setText(R.string.preview_input_room_id_waring);
                mInputErrorTv.removeCallbacks(mRoomIdDismissRunnable);
                mInputErrorTv.postDelayed(mRoomIdDismissRunnable, 2500);
            } else {
                mInputErrorTv.setVisibility(View.INVISIBLE);
                meetingIdRegexMatch = true;
            }
        } else {
            if (roomIDLength > 0) {
                mInputErrorTv.setVisibility(View.VISIBLE);
                mInputErrorTv.setText(R.string.preview_input_wrong_content_waring);
            } else {
                mInputErrorTv.setVisibility(View.INVISIBLE);
            }
        }
        boolean joinBtnEnable = roomIDLength > 0 && roomIDLength <= 18
                && meetingIdRegexMatch;
        mJoinRoomBtn.setEnabled(joinBtnEnable);
    }

    /**
     * 加入房间页面
     */
    private void enterRoom() {
        String roomId = mInputEt.getText().toString().trim();
        if (TextUtils.isEmpty(roomId)) {
            return;
        }
        // todo 隔离临时方案
        roomId = "call_" + roomId;
        mRoomId = roomId;
        VideoCallRTCManager.ins().getRTSClient().requestJoinRoom(roomId, mJoinCallback);
    }

    /**
     * 设置本端视频渲染
     */
    private void setLocalVideoCanvas() {
        if (VideoCallRTCManager.ins().isCameraOn()) {
            TextureView textureView = VideoCallRTCManager.ins().getRenderView(SolutionDataManager.ins().getUserId());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Utils.attachViewToViewGroup(mRenderContainer, textureView, params);
            VideoCallRTCManager.ins().setLocalVideoCanvas(false, textureView);
            mRenderDisable.setVisibility(View.GONE);
        } else {
            mRenderContainer.removeAllViews();
            mRenderDisable.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaStatusEvent(MediaStatusEvent event) {
        if (!TextUtils.equals(SolutionDataManager.ins().getUserId(), event.uid)) {
            return;
        }
        boolean on = event.status == Constants.MEDIA_STATUS_ON;
        if (event.mediaType == Constants.MEDIA_TYPE_AUDIO) {
            mMicBtn.setImageResource(on ? R.drawable.micro_phone_enable_icon
                    : R.drawable.micro_phone_disable_icon);
        } else if (event.mediaType == Constants.MEDIA_TYPE_VIDEO) {
            mCameraBtn.setImageResource(on ? R.drawable.camera_enable_icon :
                    R.drawable.camera_disable_icon);
            setLocalVideoCanvas();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioRouterEvent(AudioRouterEvent event) {
        mSpeakerPhoneBtn.setImageResource(event.isSpeakerPhone ? R.drawable.speakerphone_icon
                : R.drawable.earpiece_icon);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshPreviewEvent(RefreshPreviewEvent event) {
        // 所有媒体恢复到默认状态
        VideoCallRTCManager.ins().startVideoCapture(true);
        VideoCallRTCManager.ins().startPublishAudio(true);
        VideoCallRTCManager.ins().switchCamera(true);
        VideoCallRTCManager.ins().useSpeakerphone(true);
        VideoCallRTCManager.ins().setMirrorType(true);

        setLocalVideoCanvas();
    }

    /**
     * 场景的入口方法
     * <p>
     * 壳工程通过反射调用该方法，成功后则打开本页面，否则弹出提示
     *
     * @param activity   调用者页面
     * @param doneAction 完成joinRTM后回调
     */
    @Keep
    @SuppressWarnings("unused")
    public static void prepareSolutionParams(Activity activity, IAction<Object> doneAction) {
        Log.d(TAG, "prepareSolutionParams() invoked");
        IRequestCallback<ServerResponse<RTSInfo>> callback = new IRequestCallback<ServerResponse<RTSInfo>>() {
            @Override
            public void onSuccess(ServerResponse<RTSInfo> response) {
                RTSInfo data = response == null ? null : response.getData();
                if (data == null || !data.isValid()) {
                    onError(-1, "");
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClass(Utilities.getApplicationContext(), PreviewActivity.class);
                intent.putExtra(KEY_RTM, data);
                activity.startActivity(intent);
                if (doneAction != null) {
                    doneAction.act(null);
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                if (doneAction != null) {
                    doneAction.act(null);
                }
            }
        };
        JoinRTSRequest request = new JoinRTSRequest();
        request.scenesName = Constants.SOLUTION_NAME_ABBR;
        request.loginToken = SolutionDataManager.ins().getToken();

        JoinRTSManager.setAppInfoAndJoinRTM(request, callback);
    }
}