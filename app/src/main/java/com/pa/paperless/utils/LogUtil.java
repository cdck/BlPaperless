package com.pa.paperless.utils;

import android.util.Log;

import static com.pa.paperless.service.ShotApplication.isDebug;

/**
 * @author xlk
 * @date 2019/8/14
 */
public class LogUtil {
    private static final String TAG = "xlk_log";

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(TAG, tag + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(TAG, tag + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(TAG, tag + msg);
        }
    }

    public static void v(String tag, String msg) {
        if (isDebug) {
            Log.v(TAG, tag + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(TAG, tag + msg);
        }
    }
}
