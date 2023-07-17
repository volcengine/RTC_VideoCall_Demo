package com.volcengine.vertcdemo.videocall.call.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.utils.Utils;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.databinding.LayoutViewRemoteNamePrefixBinding;

public class RemoteNameView extends FrameLayout {

    private final LayoutViewRemoteNamePrefixBinding mBinding;

    public RemoteNameView(Context context, AttributeSet atrrs) {
        this(context, atrrs, 0);
    }

    public RemoteNameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_view_remote_name_prefix, this, true);
        mBinding = LayoutViewRemoteNamePrefixBinding.bind(this);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PeerNameView);
            int layoutSize = typedArray.getInteger(R.styleable.PeerNameView_layout_size, 0);
            int textSize = typedArray.getInteger(R.styleable.PeerNameView_text_size, 0);
            int innerSize = typedArray.getInteger(R.styleable.PeerNameView_inner_size, 0);
            post(() -> {
                ViewGroup.LayoutParams layoutParams = mBinding.namePrefixFl.getLayoutParams();
                layoutParams.width = (int) Utils.dp2Px(layoutSize);
                layoutParams.height = (int) Utils.dp2Px(layoutSize);

                mBinding.namePrefixTv.setTextSize(Utils.dp2Px(textSize));

                ViewGroup.LayoutParams textParams = mBinding.namePrefixTv.getLayoutParams();
                textParams.width = (int) Utils.dp2Px(innerSize);
                textParams.height = (int) Utils.dp2Px(innerSize);

                ViewGroup.LayoutParams middleIvParams = mBinding.animationMiddleIv.getLayoutParams();
                middleIvParams.width = (int) Utils.dp2Px(innerSize);
                middleIvParams.height = (int) Utils.dp2Px(innerSize);

                ViewGroup.LayoutParams outIvParams = mBinding.animationOuterIv.getLayoutParams();
                outIvParams.width = (int) Utils.dp2Px(innerSize);
                outIvParams.height = (int) Utils.dp2Px(innerSize);
            });

            typedArray.recycle();
        }
    }

    public void setRemoteNamePrefix(String remoteNamePrefix) {
        mBinding.namePrefixTv.setText(remoteNamePrefix);
    }

    private AnimationSet mCallingMiddleAnimation;
    private AnimationSet mCallingOuterAnimation;

    /***开启响铃动画*/
    public void startCallingAnimation() {
        mCallingMiddleAnimation = new AnimationSet(true);
        //缩放动画，以中心从原始放大到1.4倍
        ScaleAnimation middleScaleAnimation = new ScaleAnimation(1.0f, 1.4f, 1.0f, 1.4f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        middleScaleAnimation.setDuration(800);
        middleScaleAnimation.setRepeatCount(Animation.INFINITE);
        //渐变动画
        AlphaAnimation middleAlphaAnimation = new AlphaAnimation(1.0f, 0.5f);
        middleAlphaAnimation.setRepeatCount(Animation.INFINITE);
        mCallingMiddleAnimation.setDuration(800);
        mCallingMiddleAnimation.addAnimation(middleScaleAnimation);
        mCallingMiddleAnimation.addAnimation(middleAlphaAnimation);
        mBinding.animationMiddleIv.startAnimation(mCallingMiddleAnimation);

        mCallingOuterAnimation = new AnimationSet(true);
        //缩放动画，以中心从1.4倍放大到1.8倍
        ScaleAnimation outerScaleAnimation = new ScaleAnimation(1.4f, 1.8f, 1.4f, 1.8f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        outerScaleAnimation.setDuration(800);
        outerScaleAnimation.setRepeatCount(Animation.INFINITE);
        //渐变动画
        AlphaAnimation outerAlphaAnimation = new AlphaAnimation(0.5f, 0.1f);
        outerAlphaAnimation.setRepeatCount(Animation.INFINITE);
        mCallingOuterAnimation.setDuration(800);

        mCallingOuterAnimation.addAnimation(outerScaleAnimation);
        mCallingOuterAnimation.addAnimation(outerAlphaAnimation);
        mBinding.animationOuterIv.startAnimation(mCallingOuterAnimation);
    }

    /***停止响铃动画*/
    public void stopCallingAnimation() {
        if (mCallingMiddleAnimation != null) {
            mCallingMiddleAnimation.cancel();
            mBinding.animationMiddleIv.clearAnimation();
        }
        if (mCallingOuterAnimation != null) {
            mCallingOuterAnimation.cancel();
            mBinding.animationOuterIv.clearAnimation();
        }
    }
}
