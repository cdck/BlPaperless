package com.pa.paperless.broadcase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.LogUtil;

import com.pa.paperless.data.constant.EventType;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by xlk
 * on 2018/7/25.
 * 监听用户点击HOME键/与任务键广播
 */

public class InnerRecevier extends BroadcastReceiver {

    private final String TAG = "InnerRecevier-->";
    final String SYSTEM_DIALOG_REASON_KEY = "reason";

    final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

    final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (reason != null) {
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    EventBus.getDefault().post(new EventMessage(EventType.click_key_home));
                    LogUtil.d(TAG, "onReceive: 用户点击了HOME键...........");
                } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                    LogUtil.d(TAG, "onReceive: 用户点击了多任务键..........");
                }
            }
        }
    }
}
