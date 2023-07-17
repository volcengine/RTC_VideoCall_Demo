package com.volcengine.vertcdemo.videocall.effect.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.volcengine.vertcdemo.utils.DebounceClickListener;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.databinding.LayoutEffectNodeBinding;
import com.volcengine.vertcdemo.videocall.effect.SelectEffectListener;
import com.volcengine.vertcdemo.videocall.effect.model.Effect;
import com.volcengine.vertcdemo.videocall.effect.model.EffectNode;
import com.volcengine.vertcdemo.videocall.effect.model.Effects;

import java.util.ArrayList;
import java.util.List;

public class EffectsAdapter extends RecyclerView.Adapter<EffectsAdapter.VH> {
    private final List<Effect> mEffects = new ArrayList<>();
    private SelectEffectListener mListener;

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Effect> effects) {
        mEffects.clear();
        mEffects.addAll(effects);
        notifyDataSetChanged();
    }

    /**
     * 刷新某项数据相关UI
     */
    public void notifyItemChange(Effect effect) {
        int position = mEffects.indexOf(effect);
        if (position >= 0) {
            notifyItemChanged(position);
        }
    }

    /**
     * 设置效果选择监听器
     */
    public void setListener(SelectEffectListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VH holder = new VH(LayoutEffectNodeBinding.inflate(LayoutInflater.from(parent.getContext())), mListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Effect effect = mEffects.get(position);
        if (effect == null) return;
        holder.bindData(effect);
    }

    @Override
    public int getItemCount() {
        return mEffects.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        private final Context mContext;

        private final LayoutEffectNodeBinding mBinding;
        private Effect mEffect;
        private final SelectEffectListener mListener;

        public VH(@NonNull LayoutEffectNodeBinding binding, SelectEffectListener listener) {
            super(binding.getRoot());
            mContext = binding.getRoot().getContext();
            mBinding = binding;
            mListener = listener;
            mBinding.getRoot().setOnClickListener(DebounceClickListener.create(v -> {
                if (mEffect == null || mListener == null) return;
                mListener.selectEffect(mEffect);
            }));
        }

        public void bindData(Effect node) {
            mEffect = node;
            mBinding.effectText.setText(node.name);
            mBinding.effectText.setTextColor(node.isSelected() ? Color.WHITE : ContextCompat.getColor(mContext, R.color.gray_86909C));
            if (node.type == Effect.TYPE_NONE) {
                mBinding.effectIcon.setImageResource(node.imageRes);
                int paddingPx = (int) dip2Px(10);
                mBinding.effectIcon.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                mBinding.effectText.setTextColor(ContextCompat.getColor(mContext, R.color.gray_86909C));
                mBinding.effectIcon.setBackgroundResource(node.isSelected()
                        ? R.drawable.effect_btn_selected_bg
                        : R.drawable.effect_btn_normal_bg);
                mBinding.effectIcon.clearColorFilter();
            } else if (node.type == Effect.TYPE_FILTER && node instanceof EffectNode) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(node.imageRes)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mBinding.effectIcon);
                int paddingPx = (int) dip2Px(2);
                mBinding.effectIcon.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                mBinding.effectIcon.setBackgroundResource(node.isSelected() ? R.drawable.bg_effect_selected : 0);
                mBinding.effectIcon.clearColorFilter();
            } else {
                mBinding.effectIcon.setImageResource(node.imageRes);
                int paddingPx = (int) dip2Px(10);
                mBinding.effectIcon.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                mBinding.effectIcon.setBackgroundResource(node.isSelected() ? R.drawable.effect_btn_selected_bg : R.drawable.effect_btn_normal_bg);
                boolean hasValidValue = hasValidValue(node);
                int color = hasValidValue ? R.color.blue : R.color.white;
                mBinding.effectIcon.setColorFilter(mContext.getResources().getColor(color));
            }
        }

        private float dip2Px(float dipValue) {
            float scale = mContext.getResources().getDisplayMetrics().density;
            return dipValue * scale + 0.5F;
        }

        /**
         * 是否有有效值，如果为中间节点则看所有叶子节点中是否含有有效值
         */
        private boolean hasValidValue(Effect effect) {
            if (effect instanceof EffectNode) {
                return ((EffectNode) effect).value > 0;
            } else if (effect instanceof Effects) {
                Effects effects = (Effects) effect;
                if (effects.child == null || effects.child.size() == 0) {
                    return false;
                }
                boolean result = false;
                for (Effect item : effects.child) {
                    if (result) {
                        return true;
                    }
                    result = hasValidValue(item);
                }
                return result;
            }
            return false;
        }
    }

}
