package com.volcengine.vertcdemo.videocall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.videocall.R;

/**
 * 设计对话框单条设置控件
 * <p>
 * 功能：
 * 1.设置字符串型数值
 * 2.设置开关型数值
 */
public class SettingItemLayout extends FrameLayout {

    private TextView mKey;
    private TextView mTextValue;
    private View mTextLayout;
    private Switch mSwitchValue;

    public SettingItemLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public SettingItemLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SettingItemLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.item_setting_layout, this, true);
        mKey = findViewById(R.id.setting_key);
        mTextValue = findViewById(R.id.setting_text_value);
        mTextLayout = findViewById(R.id.setting_text_value_layout);
        mSwitchValue = findViewById(R.id.setting_switch_value);
    }

    /**
     * 设置字符串类型数值
     *
     * @param key   设置名
     * @param value 设置值
     */
    public void setData(String key, String value) {
        mKey.setText(key);
        mTextValue.setText(value);

        mTextLayout.setVisibility(VISIBLE);
        mSwitchValue.setVisibility(GONE);
    }

    /**
     * 设置开关类型数值
     *
     * @param key   设置名
     * @param value 设置值
     */
    public void setData(String key, boolean value) {
        mKey.setText(key);
        mSwitchValue.setChecked(value);

        mTextLayout.setVisibility(GONE);
        mSwitchValue.setVisibility(VISIBLE);
    }

    /**
     * 设置开关状态变化回调
     *
     * @param listener 回调
     */
    public void setOnCheckListener(CompoundButton.OnCheckedChangeListener listener) {
        mSwitchValue.setOnCheckedChangeListener(listener);
    }
}
