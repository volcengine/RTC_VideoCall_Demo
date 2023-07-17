package com.volcengine.vertcdemo.videocall.effect.view;

import com.volcengine.vertcdemo.videocall.effect.SelectEffectListener;
import com.volcengine.vertcdemo.videocall.effect.model.Effect;
import com.volcengine.vertcdemo.videocall.effect.model.Effects;

public interface IEffectsView {
    /**
     * 设置UI展示美颜效果数据
     *
     * @param effects 美颜效果数据集合
     */
    void setData(Effects effects);

    /**
     * 设置选中监听器
     */
    void setSelectListener(SelectEffectListener listener);

    /**
     * 单项美颜数据变化时更新UI
     */
    void notifyItemChange(Effect effect);
}
