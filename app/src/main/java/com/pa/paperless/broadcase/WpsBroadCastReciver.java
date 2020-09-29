package com.pa.paperless.broadcase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.LogUtil;

import com.pa.paperless.activity.MeetingActivity;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.WpsModel;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

/**
 * Created by xlk
 * on 2018/5/30.
 * 调用WPS打开文档后，接收WPS关闭和保存的广播
 */
public class WpsBroadCastReciver extends BroadcastReceiver {

    private final String TAG = "WpsBroadCastReciver-->";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.e(TAG, "onReceive : wps广播  --> " + action);
        if (action == null) {
            return;
        }
        switch (action) {
//            case WpsModel.Reciver.ACTION_BACK://返回键
//                LogUtil.e(TAG, "onReceive :  返回键 --> ");
//                EventBus.getDefault().post(new EventMessage(EventType.WPS_BROAD_CASE_INFORM, false));
//                jump2meet(context);
//                break;
            case WpsModel.Reciver.ACTION_CLOSE://关闭文件时的广播
                EventBus.getDefault().post(new EventMessage(EventType.WPS_BROAD_CASE_INFORM, false));
                String closeFile = intent.getStringExtra(WpsModel.ReciverExtra.CLOSEFILE);
                String thirdPackage1 = intent.getStringExtra(WpsModel.ReciverExtra.THIRDPACKAGE);
                LogUtil.e(TAG, "onReceive :  关闭文件收到广播 --> closeFile：" + closeFile + ", \n thirdPackage：" + thirdPackage1);
                jump2meet(context);
                break;
            case WpsModel.Reciver.ACTION_HOME://home键广播
                LogUtil.e(TAG, "onReceive :  home键广播 --> ");
                EventBus.getDefault().post(new EventMessage(EventType.WPS_BROAD_CASE_INFORM, false));
                break;
            case WpsModel.Reciver.ACTION_SAVE://保存文件时的广播
//                EventBus.getDefault().post(new EventMessage(EventType.WPS_BROAD_CASE_INFORM, false));
                String openFile = intent.getStringExtra(WpsModel.ReciverExtra.OPENFILE);//文件最初的路径
                String thirdPackage = intent.getStringExtra(WpsModel.ReciverExtra.THIRDPACKAGE);//传入的第三方的包名。
                String savePath = intent.getStringExtra(WpsModel.ReciverExtra.SAVEPATH);//文件这次保存的路径
                LogUtil.e(TAG, "onReceive :  保存键广播 --> \n文件最初的路径： "
                        + openFile + "\n传入的第三方的包名：" + thirdPackage + "\n文件这次保存的路径：" + savePath);
                File file = new File(savePath);
                LogUtil.e(TAG, "onReceive -->" + "保存的文件是否存在："+file.exists());
                EventBus.getDefault().post(new EventMessage(EventType.updata_to_postil, savePath));
                break;
        }
    }

    private void jump2meet(Context context) {
        Intent intent1 = new Intent(context, MeetingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);
    }
}
