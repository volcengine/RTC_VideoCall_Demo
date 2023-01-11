package com.volcengine.vertcdemo.videocall;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class Utils {

    /**
     * 用于将子View绑定到ViewGroup，内部做判断尽可能不闪烁
     *
     * @param viewGroup 要绑定的目标父布局
     * @param view      要绑定的控件
     * @param params    要绑定的控件参数
     */
    public static void attachViewToViewGroup(ViewGroup viewGroup, View view, ViewGroup.LayoutParams params) {
        if (view == null || viewGroup == null) {
            return;
        }
        ViewParent viewParent = view.getParent();
        if (viewParent == null) {
            viewGroup.addView(view, params);
        } else if (viewParent instanceof ViewGroup) {
            ViewGroup parentViewGroup = (ViewGroup) viewParent;
            if (parentViewGroup != viewGroup) {
                parentViewGroup.removeView(view);
                viewGroup.addView(view, params);
            }
        }
    }
}
