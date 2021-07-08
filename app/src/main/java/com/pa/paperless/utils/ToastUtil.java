package com.pa.paperless.utils;

import android.widget.Toast;

import com.pa.paperless.service.App;

/**
 * @author Created by xlk on 2021/5/7.
 * @desc
 */
public class ToastUtil {
    public static void showLong(String msg) {
        Toast.makeText(App.applicationContext, msg, Toast.LENGTH_LONG).show();
    }

    public static void showLong(int resid) {
        showLong(App.applicationContext.getString(resid));
    }

    public static void showShort(String msg) {
        Toast.makeText(App.applicationContext, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showShort(int resid) {
        showShort(App.applicationContext.getString(resid));
    }
}
