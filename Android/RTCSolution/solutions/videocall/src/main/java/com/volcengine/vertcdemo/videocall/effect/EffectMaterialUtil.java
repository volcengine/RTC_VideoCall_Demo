package com.volcengine.vertcdemo.videocall.effect;

import static com.volcengine.vertcdemo.utils.FileUtils.copyAssetFolder;

import com.volcengine.vertcdemo.utils.AppUtil;

import java.io.File;

/**
 * 美颜资源管理类
 * 1.负责资源的拷贝
 * 2.负责各种资源路径的获取
 * https://www.volcengine.com/docs/6705/102038#%E6%BB%A4%E9%95%9C%E5%BA%8F%E5%8F%B7%E5%AF%B9%E5%BA%94
 */
public class EffectMaterialUtil {
    private static final String LICENSE_NAME = ;

    /**
     * 获取证书文件路径
     */
    public static String getLicensePath() {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath()
                + "/resource/cv/LicenseBag.bundle" + LICENSE_NAME;
    }

    /**
     * 获取模型文件路径
     */
    public static String getModelPath() {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath()
                + "/resource/cv/ModelResource.bundle";
    }

    /**
     * 获取美颜文件路径
     */
    public static String getBeautyPath() {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath()
                + "/resource/cv/ComposeMakeup.bundle/ComposeMakeup/beauty_Android_lite";
    }

    /**
     * 获取正常美形文件路径
     */
    public static String getLiteReshapePath() {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath()
                + "/resource/cv/ComposeMakeup.bundle/ComposeMakeup/reshape_lite";
    }

    /**
     * 获取男神美形文件路径
     */
    public static String getBoyReshapePath() {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath()
                + "/resource/cv/ComposeMakeup.bundle/ComposeMakeup/reshape_boy";
    }

    /**
     * 获取女神美形文件路径
     */
    public static String getGirlReshapePath() {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath()
                + "/resource/cv/ComposeMakeup.bundle/ComposeMakeup/reshape_girl";
    }

    /**
     * 获取自然美形文件路径
     */
    public static String getNatureReshapePath() {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath()
                + "/resource/cv/ComposeMakeup.bundle/ComposeMakeup/reshape_nature";
    }

    /**
     * 获取美形4项目文件路径
     */
    public static String get4ItemReshapePath() {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath()
                + "/resource/cv/ComposeMakeup.bundle/ComposeMakeup/beauty_4Items";
    }

    /**
     * 获取滤镜文件路径
     *
     * @param name 滤镜文件名称
     */
    public static String getFilterPathByName(String name) {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath()
                + "/resource/cv/FilterResource.bundle/Filter/" + name;
    }

    /**
     * 获取滤镜文件路径
     */
    public static String getFilterPath() {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath()
                + "/resource/cv/FilterResource.bundle/Filter/";
    }

    /**
     * 初始化美颜资源文件
     * 将安装包内的资源文件拷贝到外部存储上
     */
    public static void initEffectMaterial() {
        copy("cv/LicenseBag.bundle", true);
        copy("cv/ModelResource.bundle", false);
        copy("cv/FilterResource.bundle", false);
        copy("cv/ComposeMakeup.bundle", false);
    }

    private static void copy(String fileName, boolean deleteOnExit) {
        File targetFile = new File(getExternalResourcePath(), fileName);
        if (deleteOnExit) {
            targetFile.deleteOnExit();
        }
        if (!targetFile.exists()) {
            copyAssetFolder(AppUtil.getApplicationContext(), fileName, targetFile.getAbsolutePath());
        }
    }

    private static String getExternalResourcePath() {
        return AppUtil.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath() + "/resource/";
    }
}
