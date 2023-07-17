package com.volcengine.vertcdemo.videocall.floatwindow;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;

import com.volcengine.vertcdemo.utils.AppUtil;

import java.lang.reflect.Method;

/**
 * 检测是否有后台弹出页面权限
 */
public class PopBackgroundPermissionUtil {
    private static final String TAG = "PopPermissionUtil";
    private static final int HW_OP_CODE_POPUP_BACKGROUND_WINDOW = 100000;
    private static final int XM_OP_CODE_POPUP_BACKGROUND_WINDOW = 10021;

    /**
     * 是否有后台弹出页面权限
     */
    public static boolean hasPopupBackgroundPermission() {
        if (isHuawei()) {
            return checkHwPermission();
        }
        if (isXiaoMi()) {
            return checkXmPermission();
        }
        if (isVivo()) {
            return checkVivoPermission();
        }
        return true;
    }

    public static boolean isHuawei() {
        return Build.MANUFACTURER.toLowerCase().contains("huawei");
    }

    public static boolean isXiaoMi() {
        return Build.MANUFACTURER.toLowerCase().contains("xiaomi");
    }

    public static boolean isVivo() {
        return Build.MANUFACTURER.toLowerCase().contains("vivo");
    }

    public static boolean isOppo() {
        return Build.MANUFACTURER.toLowerCase().contains("oppo");
    }


    private static boolean checkHwPermission() {
        Context context = AppUtil.getApplicationContext();
        try {
            Class c = Class.forName("com.huawei.android.app.AppOpsManagerEx");
            Method m = c.getDeclaredMethod("checkHwOpNoThrow", AppOpsManager.class, int.class, int.class, String.class);
            Integer result = (Integer) m.invoke(c.newInstance(),
                    new Object[]{(AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE), HW_OP_CODE_POPUP_BACKGROUND_WINDOW,
                            Binder.getCallingUid(),
                            context.getPackageName()});
            Log.d(TAG, "PopBackgroundPermissionUtil checkHwPermission result:" + (AppOpsManager.MODE_ALLOWED == result));
            return AppOpsManager.MODE_ALLOWED == result;
        } catch (Exception e) {
            //ignore
        }
        return false;
    }

    private static boolean checkXmPermission() {
        Context context = AppUtil.getApplicationContext();
        AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            Method method = ops.getClass().getMethod("checkOpNoThrow", new Class[]{int.class, int.class, String.class});
            Integer result = (Integer) method.invoke(ops, XM_OP_CODE_POPUP_BACKGROUND_WINDOW, android.os.Process.myUid(), context.getPackageName());
            Log.d(TAG, "PopBackgroundPermissionUtil checkXmPermission result:" + (AppOpsManager.MODE_ALLOWED == result));
            return result == AppOpsManager.MODE_ALLOWED;

        } catch (Exception e) {
            //ignore
        }
        return false;
    }

    private static boolean checkVivoPermission() {
        Context context = AppUtil.getApplicationContext();
        Uri uri = Uri.parse("content://com.vivo.permissionmanager.provider.permission/start_bg_activity");
        String selection = "pkgname = ?";
        String[] selectionArgs = new String[]{context.getPackageName()};
        int result = 1;
        ContentResolver contentResolver = context.getContentResolver();
        try (Cursor cursor = contentResolver.query(uri, null, selection, selectionArgs, null)) {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(cursor.getColumnIndex("currentstate"));
            }
        } catch (Exception exception) {
            //ignore
        }
        Log.d(TAG, "PopBackgroundPermissionUtil checkVivoPermission result:" + (AppOpsManager.MODE_ALLOWED == result));
        return result == AppOpsManager.MODE_ALLOWED;
    }

}
