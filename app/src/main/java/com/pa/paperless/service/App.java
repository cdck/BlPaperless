
package com.pa.paperless.service;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.pa.paperless.activity.MainActivity;
import com.pa.paperless.activity.MeetingActivity;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.fragment.AgendaFragment;
import com.pa.paperless.utils.CrashHandler;
import com.pa.paperless.utils.LogUtil;

import android.view.View;
import android.view.ViewGroup;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsDownloader;
import com.tencent.smtt.sdk.TbsListener;
import com.pa.paperless.helper.ScreenRecorder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.pa.paperless.data.constant.BroadCaseAction;
import com.pa.paperless.data.constant.EventType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import me.jessyan.autosize.AutoSizeConfig;


/**
 * @author Administrator
 * @date 2018/6/9
 */
public class App extends Application {

    private static final String TAG = "App-->";
    public static boolean isDebug = false;
    public static final boolean read2file = false;
    /**
     * 红蓝版本切换时同时需要替换资源文件
     */
    public static final boolean isRedTheme = true;

    public static List<Activity> activities = new ArrayList<>();
    /**
     * 悬浮窗服务和后台服务
     */
    private Intent fabIntent, natIntent;
    public static boolean fabServiceIsOpened, nativeServiceIsOpened;
    /**
     * 屏幕录制相关
     */
    private static ScreenRecorder recorder;
    public static int recorderWidth, recorderHeight, dpi;
    public static int maxBitRate = 1000 * 1000;//码率值
    /**
     * 屏幕录制与截图
     */
    public static int result;
    public static Intent intent;
    public static MediaProjectionManager mediaProjectionManager;
    public static MediaProjection mediaProjection;

    public static int CameraW = 0, CameraH = 0;
    public static boolean initX5Finished = false;//是否加载完成
    public static LocalBroadcastManager lbm;
    public static int screenWidth, screenHeight;
    public static Context applicationContext;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            int type = intent.getIntExtra("type", 0);
            LogUtil.e(TAG, "onReceive :   --> type=" + type + " , action=" + action);
            if (type == BroadCaseAction.SCREEN_SHOT_TYPE) {
                if (action.equals(BroadCaseAction.SCREEN_SHOT)) {
                    LogUtil.e(TAG, "screen_shot -->");
                    capture();
                } else if (action.equals(BroadCaseAction.STOP_SCREEN_SHOT)) {
                    LogUtil.e(TAG, "stop_screen_shot -->");
                    if (stopRecord()) {
                        LogUtil.i(TAG, "stopStreamInform: 屏幕录制已停止..");
                    } else {
                        LogUtil.e(TAG, "stopStreamInform :  屏幕录制停止失败 -->");
                    }
                }
            } else if (action.equals("relaunchApp")) {
                LogUtils.e(TAG, "重启应用");
                openFab(false);
                openNat(false);
                AppUtils.relaunchApp(true);
            }
        }
    };

    private void capture() {
        if (stopRecord()) {
            LogUtil.i(TAG, "capture: 屏幕录制已停止");
        } else {
            if (mediaProjection == null) {
                mediaProjection = mediaProjectionManager.getMediaProjection(result, intent);
            }
            if (recorder != null) {
                recorder.quit();
            }
            if (recorder == null) {
                recorder = new ScreenRecorder(recorderWidth, recorderHeight, maxBitRate, dpi, mediaProjection, "");
            }
            recorder.start();//启动录屏线程
            LogUtil.i(TAG, "capture: 开启屏幕录制");
        }
    }

    public static boolean stopRecord() {
        if (recorder != null) {
            LogUtil.e(TAG, "stopRecord 停止录制屏幕 -->");
            recorder.quit();
            recorder = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        LogUtils.Config config = LogUtils.getConfig();
        config.setLog2FileSwitch(true);
        config.setDir(Macro.logcat_dir);
        config.setSaveDays(7);
        LogUtils.i(TAG, "Application onCreate -->");
//        AutoSize.checkAndInit(this);
        AutoSizeConfig.getInstance().setLog(false);
        initScreenParam();
        loadX5();
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadCaseAction.SCREEN_SHOT);
        filter.addAction(BroadCaseAction.STOP_SCREEN_SHOT);
        filter.addAction("relaunchApp");
        lbm.registerReceiver(receiver, filter);
        CrashHandler.getInstance().init(applicationContext);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                activities.add(activity);
                LogUtils.d(TAG, "onActivityCreated " + activity + ",Activity数量=" + activities.size() + logAxt());
                if (activity.getClass().getName().equals(MeetingActivity.class.getName())) {
                    openFab(true);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                LogUtils.i(TAG, "onActivityStarted " + activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                LogUtils.i(TAG, "onActivityResumed " + activity);
                if (!ServiceUtils.isServiceRunning(NativeService.class)) {
                    nativeServiceIsOpened = false;
                    openNat(true);
                }
                if (activity.getClass().getName().equals(MeetingActivity.class.getName())) {
                    Set<String> allRunningServices = ServiceUtils.getAllRunningServices();
                    Iterator<String> iterator = allRunningServices.iterator();
                    String allServices = "";
                    while (iterator.hasNext()) {
                        allServices += "\n" + iterator.next();
                    }
                    LogUtils.i(TAG, "所有正在运行的服务：" + allServices);
                    if (!ServiceUtils.isServiceRunning(FabService.class)) {
                        fabServiceIsOpened = false;
                        openFab(true);
                    }
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                LogUtils.i(TAG, "onActivityPaused " + activity);
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                LogUtils.i(TAG, "onActivityStopped " + activity);
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                LogUtils.i(TAG, "onActivitySaveInstanceState " + activity);
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                activities.remove(activity);
                LogUtils.e(TAG, "onActivityDestroyed " + activity + ",Activity数量=" + activities.size() + logAxt());
                if (activity.getClass().getName().equals(MeetingActivity.class.getName())) {
                    openFab(false);
                }
                if (activities.isEmpty()) {
                    openNat(false);
                    System.exit(0);
                }
            }
        });
    }

    /**
     * 获取当前的Activity
     */
    public static Activity currentActivity() {
        return activities.get(activities.size() - 1);
    }

    private String logAxt() {
        String ret = "打印所有的Activity:\n";
        for (Activity activity : activities) {
            String a = activity.getCallingPackage() + "  #  " + activity + "\n";
            ret += a;
        }
        return ret;
    }

    public static QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
        @Override
        public void onCoreInitFinished() {
            //x5内核初始化完成回调接口，此接口回调并表示已经加载起来了x5，有可能特殊情况下x5内核加载失败，切换到系统内核。
            LogUtils.i(TAG, "x5内核 onCoreInitFinished-->");
        }

        @Override
        public void onViewInitFinished(boolean b) {
            initX5Finished = true;
            //ToastUtils.showShort(usedX5 ? R.string.tencent_x5_load_successfully : R.string.tencent_x5_load_failed);
            //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
            LogUtils.d(TAG, "x5内核 onViewInitFinished: 加载X5内核是否成功: " + b);
            AgendaFragment.needReStart = !b;
            EventBus.getDefault().post(new EventMessage(EventType.init_x5_finished));
        }
    };

    public static void loadX5() {
        boolean canLoadX5 = QbSdk.canLoadX5(applicationContext);
        LogUtils.i(TAG, "x5内核  是否可以加载X5内核 -->" + canLoadX5);
        if (canLoadX5) {
            initX5();
        } else {
            QbSdk.setDownloadWithoutWifi(true);
            QbSdk.setTbsListener(new TbsListener() {
                @Override
                public void onDownloadFinish(int i) {
                    LogUtils.d(TAG, "x5内核 onDownloadFinish -->下载X5内核：" + i);
                }

                @Override
                public void onInstallFinish(int i) {
                    LogUtils.d(TAG, "x5内核 onInstallFinish -->安装X5内核：" + i);
                    if (i == TbsListener.ErrorCode.INSTALL_SUCCESS_AND_RELEASE_LOCK) {
                        initX5();
                    }
                }

                @Override
                public void onDownloadProgress(int i) {
                    LogUtils.d(TAG, "x5内核 onDownloadProgress -->下载X5内核：" + i);
                }
            });
            new Thread(() -> {
                //判断是否要自行下载内核
//                boolean needDownload = TbsDownloader.needDownload(mContext, TbsDownloader.DOWNLOAD_OVERSEA_TBS);
//                LogUtil.i(TAG, "loadX5 是否需要自行下载X5内核" + needDownload);
//                if (needDownload) {
//                    // 根据实际的网络情况下，选择是否下载或是其他操作
//                    // 例如: 只有在wifi状态下，自动下载，否则弹框提示
//                    // 启动下载
//                    TbsDownloader.startDownload(mContext);
//                }
                TbsDownloader.startDownload(applicationContext);
            }).start();
        }
    }

    public static void initX5() {
        //目前线上sdk存在部分情况下initX5Enviroment方法没有回调，您可以不用等待该方法回调直接使用x5内核。
        QbSdk.initX5Environment(applicationContext, cb);
        //如果您需要得知内核初始化状态，可以使用QbSdk.preinit接口代替
//        QbSdk.preInit(applicationContext, cb);
    }

    private void initScreenParam() {
        screenWidth = ScreenUtils.getScreenWidth();
        screenHeight = ScreenUtils.getScreenHeight();
        dpi = ScreenUtils.getScreenDensityDpi();
        recorderWidth = screenWidth;
        recorderHeight = screenHeight;
        LogUtils.e(TAG, "initScreenParam :  屏幕宽高 --> " + recorderWidth + "," + recorderHeight + ",dpi=" + dpi);
        if (dpi > 320) {
            dpi = 320;
        }
    }

    public static void setMaxBitRate(int type) {
        if (type < 500) maxBitRate = 500 * 1000;
        else if (type > 10000) maxBitRate = 10000 * 1000;
        else maxBitRate = type * 1000;
    }

    public static View getRootView() {
        return ((ViewGroup) (currentActivity().getWindow().getDecorView().findViewById(android.R.id.content))).getChildAt(0);
    }

    public void openFab(boolean open) {
        LogUtils.i(TAG, "开关服务：FabService open=" + open);
        if (open && !fabServiceIsOpened) {
            if (fabIntent == null) {
                fabIntent = new Intent(this, FabService.class);
            }
            LogUtil.d(TAG, "openFab: 开启服务..");
            startService(fabIntent);
            fabServiceIsOpened = true;
        } else if (!open && fabServiceIsOpened) {
            if (fabIntent != null) {
                LogUtil.d(TAG, "openFab: 停止服务..");
                stopService(fabIntent);
                fabServiceIsOpened = false;
            } else {
                LogUtil.d(TAG, "openFab: fabIntent为null..");
            }
        }
    }

    public void openNat(boolean open) {
        LogUtils.i(TAG, "开关服务：NativeService open=" + open);
        if (open) {
            if (!nativeServiceIsOpened) {
                if (natIntent == null) {
                    natIntent = new Intent(this, NativeService.class);
                }
                LogUtil.d(TAG, "openNat: 开启服务..");
                startService(natIntent);
                nativeServiceIsOpened = true;
            } else {
                LogUtils.i(TAG, "NativeService服务已经开启");
            }
        } else {
            if (nativeServiceIsOpened) {
                if (natIntent != null) {
                    LogUtil.d(TAG, "openNat: 停止服务..");
                    stopService(natIntent);
                    nativeServiceIsOpened = false;
                }
            } else {
                LogUtils.i(TAG, "NativeService服务还未开启");
            }
        }
    }
}
