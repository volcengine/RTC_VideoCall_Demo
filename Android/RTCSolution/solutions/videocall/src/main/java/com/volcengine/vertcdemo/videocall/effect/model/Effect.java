package com.volcengine.vertcdemo.videocall.effect.model;

import androidx.annotation.StringRes;

import com.volcengine.vertcdemo.utils.AppUtil;

/**
 * 美颜效果父类
 */
public abstract class Effect {
    public static final int TYPE_BEAUTY = 1;
    public static final int TYPE_RESHAPE = 2;
    public static final int TYPE_FILTER = 3;
    public static final int TYPE_NONE = 4;
    /**
     * UI上展示的名称
     */
    public String name;
    /**
     * UI上显示的icon资源id
     */
    public int imageRes;

    /**
     * 是否被选中
     */
    protected boolean selected;
    /**
     * UI上展示的美颜类型
     */
    public int type;
    /**
     * 父级别引用
     */
    public Effects parent;

    public Effect(@StringRes int name, int imageRes, int type, Effects parent) {
        this.name = getString(name);
        this.imageRes = imageRes;
        this.type = type;
        this.parent = parent;
    }

    private static String getString(int resId) {
        return AppUtil.getApplicationContext().getString(resId);
    }

    /**
     * 是否被选中
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * 设置是否被选中，如果被选中更新父级节点中被选中子效果引用为自己，
     * 如果取消选中且父级节点中被选中子效果引用为自己则删除
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        if (parent == null) return;
        if (selected && parent.selectedChild != this) {
            parent.selectedChild = this;
        }
        if (!selected && parent.selectedChild == this) {
            parent.selectedChild = null;
        }
    }
}
