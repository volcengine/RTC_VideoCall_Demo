package com.volcengine.vertcdemo.videocall.effect;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.volcengine.vertcdemo.videocall.call.CallEngine;
import com.volcengine.vertcdemo.videocall.databinding.DialogVideoCallEffectBinding;
import com.volcengine.vertcdemo.videocall.effect.model.Effect;
import com.volcengine.vertcdemo.videocall.effect.model.EffectNode;
import com.volcengine.vertcdemo.videocall.effect.model.Effects;
import com.volcengine.vertcdemo.videocall.effect.view.EffectsView;
import com.volcengine.vertcdemo.videocall.effect.view.IEffectsView;
import com.volcengine.vertcdemo.videocall.effect.view.TabViewPageAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 美颜设置页面
 * 负责展示UI设置和回调用户点击、修改进度条事件
 */
public class EffectFragment extends Fragment {
    private static final String TAG = "EffectFragment";
    private DialogVideoCallEffectBinding mBinding;
    private EffectController mEffectController;
    private List<Effects> mTabEffects;
    private List<EffectsView> mTabEffectsViews = new ArrayList<>(3);
    private SeekBar mSeekbar;
    private TextView mSeekbarTv;
    private TextView mSeekbarTitle;
    private int mCurTabIndex;

    /**
     * 效果选中监听器
     */
    private final SelectEffectListener mSelectListener = curSelectEffect -> {
        Effects parentEffect = curSelectEffect.parent;
        IEffectsView curEffectView = (parentEffect == mTabEffects.get(mCurTabIndex))
                ? mTabEffectsViews.get(mCurTabIndex)
                : mBinding.effectsChildView;

        Effect lastSelected = parentEffect.getSelectedChild();
        //取消上次选中
        if (lastSelected != null) {
            lastSelected.setSelected(false);
            curEffectView.notifyItemChange(lastSelected);
        }
        //更新本次选中
        curSelectEffect.setSelected(true);
        curEffectView.notifyItemChange(curSelectEffect);
        //处理子界面
        if (curSelectEffect instanceof Effects) {
            mBinding.effectsChildView.setData((Effects) curSelectEffect);
            toggleEffectsChildView(false);
        }
        //更新选中的效果进度
        if (curSelectEffect instanceof EffectNode) {
            updateProgress(curSelectEffect);
        } else if (curSelectEffect instanceof Effects) {
            Effect selectedChildren = ((Effects) curSelectEffect).getSelectedChild();
            updateProgress(selectedChildren);
        }
        //处理清除逻辑
        boolean isNone = curSelectEffect.type == Effect.TYPE_NONE;
        if (isNone) {
            Effects parentEffects = curSelectEffect.parent;
            for (Effect item : parentEffects.child) {
                if (item == curSelectEffect) continue;
                clearEffect(item);
            }
            return;
        }
        //处理单选多选
        if (parentEffect.isSingleChoice
                && parentEffect.child != null
                && parentEffect.child.size() > 0) {
            clearSiblingEffect(curSelectEffect);
        }
    };

    /**
     * 清除父级下其他效果
     *
     * @param effect 保留的效果
     */
    private void clearSiblingEffect(Effect effect) {
        Effects parentEffect = effect.parent;
        for (Effect item : parentEffect.child) {
            if (item == effect) continue;
            clearEffect(item);
        }
    }

    /**
     * 清除特定效果：包括更新UI和引擎API调用
     *
     * @param effect 需要清除的效果
     */
    private void clearEffect(Effect effect) {
        Effects parentEffect = effect.parent;
        int indexInTab = mTabEffects.indexOf(parentEffect);
        IEffectsView view = indexInTab >= 0
                ? mTabEffectsViews.get(indexInTab)
                : mBinding.effectsChildView;
        if (effect instanceof EffectNode) {
            EffectNode node = (EffectNode) effect;
            if (node.value > 0) {
                node.value = 0;
                node.setSelected(false);
                if (mEffectController != null) {
                    mEffectController.changeEffect(node);
                }
                view.notifyItemChange(effect);
            }
        } else if (effect instanceof Effects) {
            Effects effects = (Effects) effect;
            effects.setSelected(false);
            if (effects.child == null || effects.child.size() == 0) return;
            for (Effect children : effects.child) {
                clearEffect(children);
            }
            view.notifyItemChange(effect);
        }
    }

    /**
     * 切换TabView和二级UI
     *
     * @param off true 时显示TabView; false 时显示二级UI
     */
    private void toggleEffectsChildView(boolean off) {
        mBinding.effectsChildView.setVisibility(off ? View.GONE : View.VISIBLE);
        mBinding.effectTab.setVisibility(off ? View.VISIBLE : View.GONE);
        mBinding.effectVp.setVisibility(off ? View.VISIBLE : View.GONE);
        mBinding.resetBtn.setVisibility(off ? View.VISIBLE : View.GONE);
        Effects mCurTabEffects = mTabEffects.get(mCurTabIndex);
        if (off) {
            updateProgress(mCurTabEffects);
        } else {
            EffectNode selectedNote = getSelectedChild(mCurTabEffects);
            updateProgress(selectedNote);
        }
    }

    public EffectFragment() {
        initEffectController();
    }

    private void initEffectController() {
        mEffectController = CallEngine.getInstance().getEffectController();
        if (mEffectController != null) {
            mTabEffects = mEffectController.getEffects();
        }
        if (mEffectController == null || mTabEffects == null) {
            Log.d(TAG, "Create EffectFragment failed mEffectController:" + mEffectController + ",mEffects:" + mTabEffects);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DialogVideoCallEffectBinding.inflate(getLayoutInflater());
        initUI();
        return mBinding.getRoot();
    }

    private void initUI() {
        ViewPager viewPager = mBinding.effectVp;
        mTabEffectsViews = getTabsView();
        TabViewPageAdapter adapter = new TabViewPageAdapter(getTabsTitle(), mTabEffectsViews);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = mBinding.effectTab;
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mCurTabIndex = tab.getPosition();
                Effects curTabEffects = mTabEffects.get(mCurTabIndex);
                updateProgress(curTabEffects.getSelectedChild());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mSeekbar = mBinding.effectSeekbar;
        mSeekbarTv = mBinding.effectSeekbarValue;
        mSeekbarTitle = mBinding.effectSeekbarTitle;
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mSeekbarTv != null) {
                    mSeekbarTv.setText(String.valueOf(progress));
                }
                if (!fromUser) {
                    return;
                }
                float fProgress = ((float) progress) / 100;
                EffectNode node = getSelectedChild(mTabEffects.get(mCurTabIndex));
                if (node != null) {
                    node.value = fProgress;
                    if (mEffectController != null) {
                        mEffectController.changeEffect(node);
                    }
                    updateUIByProgress(node);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBinding.effectsChildView.setBackListener(effects -> {
            Effect selectedChildren = effects.getSelectedChild();
            if (selectedChildren != null) {
                selectedChildren.setSelected(false);
            }
            toggleEffectsChildView(true);
        });
        mBinding.effectsChildView.setSelectListener(mSelectListener);
        mBinding.resetBtn.setOnClickListener(v -> {
            mEffectController.resetEffects();
            mTabEffects = mEffectController.getEffects();
            for (int i = 0; i < mTabEffectsViews.size(); i++) {
                EffectsView tabView = mTabEffectsViews.get(i);
                tabView.setData(mTabEffects.get(i));
            }
            Effect curSelectEffect = getSelectedChild(mTabEffects.get(mCurTabIndex));
            if (curSelectEffect != null) {
                updateProgress(curSelectEffect);
            }
        });
        Effect effect = mTabEffects.get(mCurTabIndex).getSelectedChild();
        if (effect != null) {
            updateProgress(effect);
        }
    }

    private List<String> getTabsTitle() {
        List<String> tabs = new ArrayList<>(3);
        for (Effects item : mTabEffects) {
            tabs.add(item.name);
        }
        return tabs;
    }

    private List<EffectsView> getTabsView() {
        List<EffectsView> tabs = new ArrayList<>();
        if (mTabEffects != null && mTabEffects.size() > 0) {
            Context context = getContext();
            if (context != null) {
                for (Effects section : mTabEffects) {
                    EffectsView effectsView = new EffectsView(context);
                    effectsView.setSelectListener(mSelectListener);
                    effectsView.setData(section);
                    tabs.add(effectsView);
                }
            }
        }
        return tabs;
    }

    /**
     * 获取当前选中的效果，如果传入的是中间节点，则获取被选中的叶子节点
     */
    private EffectNode getSelectedChild(Effects effects) {
        if (effects == null || effects.getSelectedChild() == null) {
            return null;
        }
        Effect child = effects.getSelectedChild();
        return (child instanceof EffectNode) ? (EffectNode) child : getSelectedChild((Effects) child);
    }

    /**
     * 更新美颜进度条
     *
     * @param effect 目标美颜类别
     */
    private void updateProgress(Effect effect) {
        if (effect == null
                || effect instanceof Effects
                || !((EffectNode) effect).hasProgress) {
            mSeekbar.setVisibility(View.GONE);
            mSeekbarTv.setVisibility(View.GONE);
            mSeekbarTitle.setVisibility(View.GONE);
            return;
        }

        mSeekbar.setVisibility(View.VISIBLE);
        mSeekbarTv.setVisibility(View.VISIBLE);
        mSeekbarTitle.setVisibility(View.VISIBLE);

        int currentProgress = (int) (((EffectNode) effect).value * 100);
        mSeekbar.setProgress(currentProgress);
        mSeekbarTv.setText(String.valueOf(currentProgress));
    }

    /**
     * 根据美颜进度条更新美颜设置中的UI
     *
     * @param node 当前被选中的美颜效果
     */
    private void updateUIByProgress(EffectNode node) {
        EffectsView curTabView = mTabEffectsViews.get(mCurTabIndex);
        if (mTabEffects.get(mCurTabIndex).child.contains(node)) {
            curTabView.notifyItemChange(node);
        } else {
            curTabView.notifyItemChange(node.parent);
            mBinding.effectsChildView.notifyItemChange(node);
        }
    }

    /**
     * 开启美颜设置对话框
     */
    public static EffectFragment start(int containerResId, FragmentManager manager) {
        if (containerResId == 0 || manager == null) {
            return null;
        }
        EffectController controller = CallEngine.getInstance().getEffectController();
        if (controller == null) {
            return null;
        }
        EffectFragment effectFragment = new EffectFragment();
        manager.beginTransaction()
                .add(containerResId, effectFragment)
                .commitAllowingStateLoss();
        return effectFragment;
    }

    /**
     * 关闭美颜设置对话框
     */
    public static void remove(Fragment fragment, FragmentManager manager) {
        if (fragment == null || manager == null) {
            return;
        }
        if (fragment.isVisible()) {
            manager.beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
        }
    }
}
