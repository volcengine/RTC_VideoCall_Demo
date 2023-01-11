package com.volcengine.vertcdemo.videocall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.volcengine.vertcdemo.videocall.R;

/**
 * 房间页面底部功能区域单个功能控件
 * <p>
 * 包含一个图标和文案控件
 * <p>
 * 功能：
 * 1.设置图标资源 {@link #setIcon(int)}
 * 2.设文案资源 {@link #setText(int)}
 */
public class BottomOptionView extends LinearLayout {

    private ImageView mIconIv;
    private TextView mTextTv;

    public BottomOptionView(Context context) {
        super(context);
        initView();
    }

    public BottomOptionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BottomOptionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_option_view, this, true);
        mIconIv = findViewById(R.id.option_icon);
        mTextTv = findViewById(R.id.option_text);
    }

    /**
     * 设置图标资源
     *
     * @param id 资源id
     */
    public void setIcon(@DrawableRes int id) {
        mIconIv.setImageResource(id);
    }

    /**
     * 设置文案资源
     *
     * @param id 资源id
     */
    public void setText(@StringRes int id) {
        mTextTv.setText(id);
    }
}
