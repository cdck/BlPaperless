package com.pa.paperless.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;

/**
 * @author by xlk
 * @date 2020/6/9 10:10
 * @desc 说明
 */
public class AppDevUtil {

    public static boolean isHasCamera(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
                || packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                ;
    }

    private static boolean checkCameraFacing(final int facing) {
        if (getSdkVersion() < Build.VERSION_CODES.GINGERBREAD) {
            return false;
        }
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasBackFacingCamera() {
        final int CAMERA_FACING_BACK = 0;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    public static boolean hasFrontFacingCamera() {
        final int CAMERA_FACING_BACK = 1;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    public static int getSdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 判断应用是否开启了允许通知
     */
    public static boolean areNotificationsEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= 24) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager.areNotificationsEnabled();
        } else {
            return true;
        }
    }

    /**
     * 前往开启允许通知页面，如果是关闭状态则Toast无效
     */
    public static void jump2inform(Context context) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 26) {
            // android 8.0引导
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
        } else if (Build.VERSION.SDK_INT >= 21) {
            // android 5.0-7.0
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else {
            // 其他
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
