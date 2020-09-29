package com.pa.paperless.broadcase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import static com.pa.paperless.data.constant.EventType.NETWORK_CHANGE;

/**
 * @author xlk
 * @date 2019/12/31
 * @Description: 监听网络状态的广播
 */
public class NetWorkReceiver extends BroadcastReceiver {
    private final String TAG = "NetWorkReceiver-->";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            LogUtil.d(TAG, "网络状态改变");
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) return;
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                String name = info.getTypeName();
                LogUtil.d(TAG, "当前网络名称：" + name);
                Values.isOnline = 1;
            } else {
                LogUtil.d(TAG, "没有可用网络");
                Values.isOnline = 0;
            }
            EventBus.getDefault().post(new EventMessage(NETWORK_CHANGE, Values.isOnline));
        }
    }
}
