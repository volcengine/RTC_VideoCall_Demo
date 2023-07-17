package com.volcengine.vertcdemo.videocall.effect.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.volcengine.vertcdemo.utils.Utils;
import com.volcengine.vertcdemo.videocall.databinding.LayoutVideoCallEffectsChildViewBinding;
import com.volcengine.vertcdemo.videocall.effect.EffectItemDecoration;
import com.volcengine.vertcdemo.videocall.effect.SelectEffectListener;
import com.volcengine.vertcdemo.videocall.effect.model.Effect;
import com.volcengine.vertcdemo.videocall.effect.model.Effects;

public class EffectsChildView extends FrameLayout implements IEffectsView {
    private LayoutVideoCallEffectsChildViewBinding mBinding;
    private RecyclerView mRecyclerView;
    private EffectsAdapter mAdapter;
    private BackListener mBackListener;
    private Effects mEffects;

    public void setBackListener(BackListener backListener) {
        this.mBackListener = backListener;
    }

    public EffectsChildView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public EffectsChildView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public EffectsChildView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mBinding = LayoutVideoCallEffectsChildViewBinding.inflate(LayoutInflater.from(context));
        MarginLayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.leftMargin = (int) Utils.dp2Px(10);
        addView(mBinding.getRoot(), params);
        mRecyclerView = mBinding.effectsRcv;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new EffectsAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new EffectItemDecoration());
        mBinding.backArrowIv.setOnClickListener(v -> {
            if (mBackListener != null) {
                mBackListener.onBack(mEffects);
            }
        });
    }

    @Override
    public void setData(Effects effects) {
        if (effects == null || effects.child.size() == 0) {
            return;
        }
        mEffects = effects;
        mBinding.titleTv.setText(effects.name);
        if (mAdapter != null) {
            mAdapter.setData(effects.child);
        }
    }

    @Override
    public void setSelectListener(SelectEffectListener listener) {
        if (mAdapter != null) {
            mAdapter.setListener(listener);
        }
    }

    @Override
    public void notifyItemChange(Effect effect) {
        if (mAdapter != null) {
            mAdapter.notifyItemChange(effect);
        }
    }

    public interface BackListener {
        void onBack(Effects effects);
    }

}
