package com.pa.paperless.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.service.App;
import com.pa.paperless.utils.LogUtil;

import android.view.WindowManager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.pa.boling.paperless.BuildConfig;
import com.pa.boling.paperless.R;
import com.pa.paperless.broadcase.InnerRecevier;
import com.pa.paperless.data.constant.BroadCaseAction;
import com.pa.paperless.utils.MyUtils;


import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

/**
 * @author xlk
 * @date 2017/5/18
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG = this.getClass().getSimpleName() + "-->";
    private InnerRecevier innerReceiver;
    protected App myApp;
    private InterfaceBase.pbui_Type_MeetUpdateNotify updateNotify;
    private final int INSTALL_REQUEST_CODE = 10012;
    private boolean isRegisterUpdateReceiver = false;//是否注册了升级广播
    private boolean receiverUpdate = true;//是否可以接收升级通知

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            int type = intent.getIntExtra("type", 0);
            LogUtil.e(TAG, "onReceive :   --> type= " + type + " , action = " + action);
            if (BroadCaseAction.UPDATE_APP.equals(action)) {
                LogUtil.e(TAG, "onReceive :  更新软件 --> " + receiverUpdate);
                byte[] data = intent.getByteArrayExtra("data");
                if (!receiverUpdate) {
                    return;
                }
                receiverUpdate = false;
                try {
                    updateNotify = InterfaceBase.pbui_Type_MeetUpdateNotify.parseFrom(data);
                    showUpdateAppdlg();
//                    unreceiverUpdate();
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                LogUtils.i(TAG, "屏幕监听-亮屏");
                EventBus.getDefault().post(new EventMessage(EventType.ACTION_SCREEN_ON));
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                LogUtils.i(TAG, "屏幕监听-息屏");
                EventBus.getDefault().post(new EventMessage(EventType.ACTION_SCREEN_OFF));
            }
        }
    };

    protected void applyPermissions(String... pers) {
        XXPermissions.with(this).permission(pers).request(new OnPermission() {
            @Override
            public void hasPermission(List<String> granted, boolean all) {

            }

            @Override
            public void noPermission(List<String> denied, boolean quick) {

            }
        });
    }

    private void showUpdateAppdlg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.update_now_or_not));
        builder.setPositiveButton(getString(R.string.ensure), (dialog, which) -> {
            installApk();
            dialog.dismiss();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            receiverUpdate = true;
            LogUtil.e(TAG, "onReceive :  点击取消更新 --> ");
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        //一：调用这个方法时，按对话框以外的地方不起作用。按返回键还起作用
        alertDialog.setCanceledOnTouchOutside(false);
        //二：调用这个方法时，按对话框以外的地方不起作用。按返回键也不起作用
        //alertDialog.setCanceleable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        } else {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        alertDialog.show();
    }

    protected void installApk() {
        LogUtil.d(TAG, " install ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // android8.0 是否有安装位置来源应用程序权限
            if (getPackageManager().canRequestPackageInstalls()) {
                updateApp();
            } else {
                //设置安装未知应用来源的权限
                LogUtil.e(TAG, "installApk :  设置安装未知应用来源的权限 --> " + this.getLocalClassName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, INSTALL_REQUEST_CODE);
            }
        } else {
            updateApp();
        }
    }

    private void updateApp() {
        String updatePath = MyUtils.b2s(updateNotify.getUpdatepath());
        LogUtil.e(TAG, "updateApp :  updatePath --> " + updatePath);
        Intent intent;
        //ACTION_INSTALL_PACKAGE   ACTION_VIEW
        //华为7.0 使用ACTION_INSTALL_PACKAGE的话，安装成功后不会有 提示打开新应用界面
        intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(updatePath + "/update.apk");
        //7.0 如果不加，最后安装成功后就没有完成和打开的界面；
        //点击打开，无法打开新版本应用
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //后台已经下载好了
        if (file.exists()) {
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileProvider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                LogUtil.e(TAG, "updateApp :  uri 111--> " + uri);
            } else {
                uri = Uri.fromFile(file);
                LogUtil.e(TAG, "updateApp :  uri 222--> " + uri);
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(intent);
            //android8.0添加下面这行 会出现解析出错
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                //8.0以下如果不加，最后不会提示完成、打开页面。
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.e(TAG, "onActivityResult :   --> ");
        if (requestCode == INSTALL_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (getPackageManager().canRequestPackageInstalls()) {
                    LogUtil.e(TAG, "onActivityResult :   --> ");
                    updateApp();
                } else {
                    ToastUtils.showShort(R.string.error_instiall_apk /*, MyUtils.b2s(updateNotify.getUpdatepath())*/);
                    showUpdateAppdlg();
                    LogUtil.e(TAG, "onActivityResult :  手动安装 --> ");
                }
            }
        }
//        else if (requestCode == request_code_capture) {
//            LogUtil.i(TAG, "onActivityResult 开始录制屏幕：" + (manager != null));
//            if (manager != null) {
//                MediaProjection projection = manager.getMediaProjection(resultCode, data);
//                ((App) getApplication()).setResult(resultCode);
//                ((App) getApplication()).setIntent(data);
//                ((App) getApplication()).setmMediaProjection(projection);
//                recorder = new ScreenRecorder(App.width, App.height, maxBitRate, App.dpi, projection, "");
//                recorder.start();
//            }
//        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onCreate :   --->>> ");
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        receiver();
        if (myApp == null) {
            myApp = (App) getApplication();
        }
    }

    protected void receiver() {
        LogUtil.d(TAG, "receiver: 开启广播...");
        if (innerReceiver == null) {
            innerReceiver = new InnerRecevier();
        }
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(innerReceiver, intentFilter);
        receiverUpdate();
    }

    protected void receiverUpdate() {
        //是否可以接收应用升级通知
        if (!isRegisterUpdateReceiver) {
            LogUtil.i(TAG, "receiverUpdate 开启软件升级广播。。。");
            IntentFilter filter = new IntentFilter();
            filter.addAction(BroadCaseAction.UPDATE_APP);//升级App
            filter.addAction(Intent.ACTION_SCREEN_ON);//屏幕亮屏
            filter.addAction(Intent.ACTION_SCREEN_OFF);//屏幕息屏
            registerReceiver(broadcastReceiver, filter);
            isRegisterUpdateReceiver = true;
        }
    }

    protected void unreceiverUpdate() {
        if (isRegisterUpdateReceiver) {
            LogUtil.i(TAG, "unreceiverUpdate 解除软件升级广播。。。");
            unregisterReceiver(broadcastReceiver);
            isRegisterUpdateReceiver = false;
        }
    }

    protected void unreceiver() {
        if (innerReceiver != null) {
            unregisterReceiver(innerReceiver);
            innerReceiver = null;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onTrimMemory :   --->>> level= " + level);
        super.onTrimMemory(level);
        switch (level) {
            case TRIM_MEMORY_UI_HIDDEN:
                // 进行资源释放操作
                break;
            case TRIM_MEMORY_RUNNING_MODERATE:
                break;
            case TRIM_MEMORY_RUNNING_LOW:
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onStart :   --->>> ");
        Values.can_open_camera = true;
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onDestroy :    ");
        unreceiver();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onNewIntent :   --->>> ");
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onResume :   --->>> ");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onPause :   --->>> ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onStop :   --->>> ");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onRestart :   --->>> " + this);
        super.onRestart();
    }
}