package com.pa.paperless.broadcase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.pa.paperless.utils.LogUtil;

import com.pa.paperless.activity.MainActivity;

/**
 * Created by Administrator on 2018/6/5.
 *  接收到开机广播，实现自启动应用
 */

public class BootBroadCaseReciver extends BroadcastReceiver {
    private final String TAG = "BootBroadCaseReciver-->";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.e(TAG, "onReceive :  接收到开机广播 --> " + action);
        Intent paperless = new Intent(context, MainActivity.class);
        paperless.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(paperless);
    }
}
