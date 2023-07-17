package com.volcengine.vertcdemo.videocall.effect.model;

import androidx.annotation.StringRes;

/**
 * 美颜效果数据的叶子节点
 */
public class EffectNode extends Effect {
    /**
     * 美颜素材key
     */
    public String materialKey;
    /**
     * 美颜素材存放路径
     */
    public String materialPath;
    /**
     * 本效果是否有强度值
     */
    public boolean hasProgress;
    /**
     * 当前强度值
     */
    public float value;

    public EffectNode(@StringRes int name,
                      int imageRes,
                      String materialKey,
                      String materialPath,
                      boolean hasProgress,
                      int type,
                      Effects parent) {
        super(name, imageRes, type, parent);
        this.materialKey = materialKey;
        this.materialPath = materialPath;
        this.hasProgress = hasProgress;
    }

    public EffectNode(@StringRes int name,
                      int imageRes,
                      String materialKey,
                      String materialPath,
                      boolean hasProgress,
                      float value,
                      int type,
                      Effects parent) {
        super(name, imageRes, type, parent);
        this.materialKey = materialKey;
        this.materialPath = materialPath;
        this.hasProgress = hasProgress;
        this.value = value;
    }
}
