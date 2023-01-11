package com.volcengine.vertcdemo.videocall.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.videocall.R;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.util.ArrayList;
import java.util.List;

/**
 * 子设置对话框
 * <p>
 * 功能：
 * 1.展示列表类型的选项 com.volcengine.vertcdemo.videocall.view.SubSettingDialog#setData(java.lang.String, java.util.List, java.lang.String, com.volcengine.vertcdemo.videocall.view.SubSettingDialog.OnSelectedCallback)
 * <p>
 * 说明：
 * 1.设置好默认选择的选项后，会延迟500ms滑动。原因看com.wx.wheelview.widget.WheelView#setSelection(int)
 */
public class SubSettingDialog extends AppCompatDialog {

    private final View mView;
    private final TextView mTitle;
    private final WheelView<String> mWheelView;
    private OnSelectedCallback mOnSelectedCallback;

    private int mSelectPosition;
    private String mSelectValue;

    public SubSettingDialog(Context context) {
        super(context, R.style.CommonDialog);
        setCancelable(true);

        mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sub_setting, null);
        mView.setOnClickListener((v) -> dismiss());
        mView.findViewById(R.id.sub_setting_content).setOnClickListener((v) -> {
        });

        TextView cancel = mView.findViewById(R.id.sub_setting_cancel);
        cancel.setOnClickListener((v) -> dismiss());
        mTitle = mView.findViewById(R.id.sub_setting_title);
        TextView confirm = mView.findViewById(R.id.sub_setting_confirm);
        confirm.setOnClickListener(v -> {
            if (mOnSelectedCallback != null) {
                mOnSelectedCallback.onSelected(mSelectPosition, mSelectValue);
            }
            dismiss();
        });
        mWheelView = mView.findViewById(R.id.sub_setting_wheel_view);
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.backgroundColor = Color.parseColor("#272E3B");
        style.selectedTextColor = Color.parseColor("#E5E6EB");
        style.textColor = Color.parseColor("#86909C");
        mWheelView.setStyle(style);
    }

    /**
     * 设置展示内容和回调
     *
     * @param title       对话框标题
     * @param data        列表数据
     * @param selectValue 默认选中的数据
     * @param callback    选中回调
     */
    public void setData(String title, List<String> data, String selectValue,
                        OnSelectedCallback callback) {
        mSelectValue = selectValue;
        mOnSelectedCallback = callback;

        mTitle.setText(title);
        if (data == null) {
            data = new ArrayList<>();
        }
        mWheelView.setWheelAdapter(new ArrayWheelAdapter(getContext()));
        mWheelView.setWheelData(data);
        int index = 0;
        for (int i = 0; i < data.size(); i++) {
            if (TextUtils.equals(selectValue, data.get(i))) {
                index = i;
                break;
            }
        }
        mWheelView.setSelection(index);
        mWheelView.setOnWheelItemSelectedListener((position, s) -> {
            mSelectPosition = position;
            mSelectValue = s;
        });
    }

    @Override
    public void show() {
        super.show();

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowUtils.getScreenWidth(getContext());
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        getWindow().setContentView(mView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        getWindow().setGravity(Gravity.BOTTOM);
    }

    public interface OnSelectedCallback {
        void onSelected(int position, String str);
    }
}
