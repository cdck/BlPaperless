package com.pa.paperless.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/11/7.
 */

public class ScreenUtils {
    private static final String TAG = "ScreenUtils-->";

    /**
     * 获得屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        LogUtil.i(TAG, "屏幕宽度：" + outMetrics.widthPixels);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        LogUtil.i(TAG, "屏幕高度：" + outMetrics.heightPixels);
        return outMetrics.heightPixels;
    }

    /**
     * 获得状态栏的高度
     */
    public static int getStatusBarHeight(Context context) {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.i(TAG, "状态栏高度：" + statusHeight);
        return statusHeight;
    }

    /**
     * 获取虚拟功能键高度，只有屏幕垂直时才有效
     */
    public static int getVirtualBarHeight(Context context) {
        int vh = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - display.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.i(TAG, "垂直虚拟功能键高度：" + vh);
        return vh;
    }

    /**
     * 获取虚拟键高度(无论是否隐藏)
     *
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        LogUtil.i(TAG, "虚拟功能键高度：" + result);
        return result;
    }

    /**
     * 虚拟按键是否打开
     */
    public static boolean isNavigationBarShown(Activity activity) {
        //虚拟键的view,为空或者不可见时是隐藏状态
        View view = activity.findViewById(android.R.id.navigationBarBackground);
        if (view == null) {
            return false;
        }
        int visible = view.getVisibility();
        boolean b = visible != View.GONE && visible != View.INVISIBLE;
        LogUtil.i(TAG, "虚拟按键是否打开：" + b);
        return b;
    }

    /**
     * 获取是否存在NavigationBar
     */
    public boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        }
        LogUtil.i(TAG, "虚拟按钮是否打开： " + hasNavigationBar);
        return hasNavigationBar;
    }

    /**
     * 判断当前是否是竖屏方向
     *
     * @return =true 竖屏，=false 横屏
     */
    public static boolean isVertical(Context context) {
        boolean b = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        LogUtil.i(TAG, "isVertical 设备是否竖屏：" + b);
        return b;
    }


    /**
     * 标题栏高度
     *
     * @return
     */
    public static int getTitleBarHeight(Activity activity) {
        int top = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        LogUtil.i(TAG, "标题栏高度：" + top);
        return top;
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
