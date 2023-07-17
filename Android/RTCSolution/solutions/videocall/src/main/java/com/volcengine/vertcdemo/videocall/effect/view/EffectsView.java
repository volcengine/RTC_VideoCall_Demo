package com.volcengine.vertcdemo.videocall.effect.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.volcengine.vertcdemo.utils.Utils;
import com.volcengine.vertcdemo.videocall.effect.EffectItemDecoration;
import com.volcengine.vertcdemo.videocall.effect.SelectEffectListener;
import com.volcengine.vertcdemo.videocall.effect.model.Effect;
import com.volcengine.vertcdemo.videocall.effect.model.Effects;

public class EffectsView extends FrameLayout implements IEffectsView {
    private RecyclerView mRecyclerView;
    private EffectsAdapter mAdapter;

    public EffectsView(@NonNull Context context) {
        super(context);
        initView();
    }

    public EffectsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EffectsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mRecyclerView = new RecyclerView(getContext());
        MarginLayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.leftMargin = (int) Utils.dp2Px(10);
        addView(mRecyclerView, params);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new EffectsAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new EffectItemDecoration());
    }

    @Override
    public void setData(Effects effects) {
        if (effects == null || effects.child == null
                || effects.child.size() == 0) {
            return;
        }
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


    public void notifyItemChange(Effect effect) {
        if (mAdapter != null && effect != null) {
            mAdapter.notifyItemChange(effect);
        }
    }

}
