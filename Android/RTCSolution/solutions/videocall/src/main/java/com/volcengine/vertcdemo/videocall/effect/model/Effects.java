package com.volcengine.vertcdemo.videocall.effect.model;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 美颜效果数据中间节点
 */
public class Effects extends Effect {
    /**
     * 子效果是否为单选
     */
    public boolean isSingleChoice;
    /**
     * 子效果集合
     */
    public List<Effect> child;

    /**
     * 当前被选中的子效果,记录是为了避免每次都循环查找
     */
    protected Effect selectedChild;

    public Effects(@StringRes int name, boolean isSingleChoice, int type) {
        super(name, 0, type, null);
        this.isSingleChoice = isSingleChoice;
    }

    public Effects(@StringRes int name, int imageRes, boolean isSingleChoice, int type, Effects parent) {
        super(name, imageRes, type, parent);
        this.isSingleChoice = isSingleChoice;
    }

    /**
     * 添加子效果
     */
    public void addChildren(Effect... node) {
        if (child == null) {
            child = new ArrayList<>();
        }
        child.addAll(Arrays.asList(node));
    }

    /**
     * 当前节点选中的子效果
     */
    public Effect getSelectedChild() {
        return selectedChild;
    }

}
