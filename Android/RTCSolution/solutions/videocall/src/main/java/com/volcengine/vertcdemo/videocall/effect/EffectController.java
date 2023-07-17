package com.volcengine.vertcdemo.videocall.effect;

import android.util.Log;

import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.video.IVideoEffect;
import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.effect.model.Effect;
import com.volcengine.vertcdemo.videocall.effect.model.EffectNode;
import com.volcengine.vertcdemo.videocall.effect.model.Effects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 美颜效果控制器
 * 1. 初始化美颜物料
 * 2. 负责跟RTC相关API交互
 */
public class EffectController {
    private static final String TAG = "EffectController";
    private IVideoEffect mVideoEffect;
    private final List<Effects> mEffecs = new ArrayList<>(3);

    /**
     * 初始化美颜数据，通话引擎创建时第哦啊用
     *
     * @param rtcVideo RTC引擎
     */
    public void init(RTCVideo rtcVideo) {
        mVideoEffect = rtcVideo == null ? null : rtcVideo.getVideoEffectInterface();
        if (mVideoEffect == null) return;
        //初始化美颜数据
        initEffectData();
        //初始化美颜引擎，涉及文件操作子线程执行
        AppExecutors.networkIO().execute(() -> {
            //初始化美颜物料
            EffectMaterialUtil.initEffectMaterial();
            AppExecutors.execRunnableInMainThread(() -> {
                //初始化美颜功能
                IVideoEffect videoEffect = mVideoEffect;
                int result = videoEffect.initCVResource(
                        EffectMaterialUtil.getLicensePath(),
                        EffectMaterialUtil.getModelPath());
                if (result != 0) {
                    hintInvokeCVFail(result, "initCVResource");
                    return;
                }
                videoEffect.enableVideoEffect();
                List<String> nodes = Arrays.asList(
                        EffectMaterialUtil.getBeautyPath(),
                        EffectMaterialUtil.getLiteReshapePath(),
                        EffectMaterialUtil.getBoyReshapePath(),
                        EffectMaterialUtil.getGirlReshapePath(),
                        EffectMaterialUtil.getNatureReshapePath(),
                        EffectMaterialUtil.get4ItemReshapePath());
                result = videoEffect.setEffectNodes(nodes);
                if (result != 0) {
                    hintInvokeCVFail(result, "setEffectNodes");
                    return;
                }
                //设置默认美颜效果
                setDefaultEffect();
            });
        });
    }

    /**
     * 初始化美颜数据
     */
    private void initEffectData() {
        Effects beauty = EffectDataUtil.createBeauty();
        Effects reshape = EffectDataUtil.createReshape();
        Effects filter = EffectDataUtil.createFilter();
        mEffecs.addAll(Arrays.asList(beauty, reshape, filter));
    }

    public List<Effects> getEffects() {
        return mEffecs;
    }

    /**
     * 将美颜效果置为默认效果
     */
    public void resetEffects() {
        //原有美颜数据清空
        mEffecs.clear();
        //重新初始化美颜数据
        initEffectData();
        //将美颜效果置为默认效果
        setDefaultEffect();
    }

    private void setDefaultEffect() {
        for (Effects item : mEffecs) {
            setEffect(item);
        }
    }

    private void setEffect(Effect effect) {
        if (effect == null) {
            return;
        }
        if (effect instanceof EffectNode) {
            changeEffect((EffectNode) effect);
        } else if (effect instanceof Effects) {
            List<Effect> child = ((Effects) effect).child;
            int childSize = child == null ? 0 : child.size();
            if (childSize == 0) {
                return;
            }
            for (Effect childItem : child) {
                if (childItem != null) {
                    setEffect(childItem);
                }
            }
        }
    }


    /**
     * 提示调用CV接口失败
     */
    private void hintInvokeCVFail(int errorCode, String methodName) {
        int resId = R.string.cv_init_fail_other;
        if (errorCode == -1000) {
            resId = R.string.cv_init_fail_1000;
        } else if (errorCode == -1002) {
            resId = R.string.cv_init_fail_1002;
        }
        Log.d(TAG, "init cv failed:" + AppUtil.getApplicationContext().getString(resId));
    }

    /**
     * 改变美颜效果
     *
     * @param node 具体效果数据
     */
    public void changeEffect(EffectNode node) {
        if (node == null || node.type == Effect.TYPE_NONE) return;
        if (node.type == Effect.TYPE_BEAUTY) {
            changeBeautyEffect(node.materialPath, node.materialKey, node.value);
        } else if (node.type == Effect.TYPE_RESHAPE) {
            changeReshapeEffect(node.materialPath, node.materialKey, node.value);
        } else if (node.type == Effect.TYPE_FILTER) {
            changeFilterEffect(node.materialKey, node.value);
        }
    }

    /**
     * 修改美颜强度
     *
     * @param path  美形物料地址
     * @param key   美颜的键值
     * @param value 具体的值，取值范围[0, 1]，值越大越明显
     */
    private void changeBeautyEffect(String path, String key, float value) {
        if (mVideoEffect == null) {
            return;
        }
        mVideoEffect.updateEffectNode(path, key, value);
    }

    /**
     * 修改美形强度
     *
     * @param path  美形物料地址
     * @param key   美形的键值
     * @param value 具体的值，取值范围[0, 1]，值越大越明显
     */
    private void changeReshapeEffect(String path, String key, float value) {
        if (mVideoEffect == null) {
            return;
        }
        mVideoEffect.updateEffectNode(path, key, value);
    }

    /**
     * 修改滤镜强度
     *
     * @param key   滤镜名称
     * @param value 具体的值，取值范围[0, 1]，值越大越明显
     */
    private void changeFilterEffect(String key, float value) {
        if (mVideoEffect == null) {
            return;
        }
        mVideoEffect.setColorFilter(EffectMaterialUtil.getFilterPathByName(key));
        mVideoEffect.setColorFilterIntensity(value);
    }

}
