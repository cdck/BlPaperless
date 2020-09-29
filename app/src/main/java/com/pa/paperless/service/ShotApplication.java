
package com.pa.paperless.service;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaCodec;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;

import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.fragment.AgendaFragment;
import com.pa.paperless.utils.CrashHandler;
import com.pa.paperless.utils.LogUtil;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.pa.paperless.utils.ScreenUtils;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsDownloader;
import com.tencent.smtt.sdk.TbsListener;
import com.pa.paperless.helper.ScreenRecorder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import com.pa.paperless.data.constant.BroadCaseAction;
import com.pa.paperless.data.constant.EventType;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


/**
 * @author Administrator
 * @date 2018/6/9
 */
public class ShotApplication extends Application {

    private static final String TAG = "ShotApplication-->";
    public static boolean isDebug = true;
    public static final boolean isRedTheme = true;
    private Intent fabIntent;
    private Intent NatIntent;
    private int VideoQuality = 2, dpi, width, height;
    private static ScreenRecorder recorder;
    private static List<Activity> activityList;
    public static int SCREEN_WIDTH = 0, SCREEN_HEIGHT = 0, CameraW = 0, CameraH = 0;
    public static boolean openFab, openNat;
    public static boolean initX5Finished = false;//是否加载完成
    public static LocalBroadcastManager lbm;
    public static Context applicationContext;
    /**
     * 屏幕录制与截图
     */
    private int result;
    private Intent intent;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    public static int maxBitRate = 500 * 1000;//码率值
//    public static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
//            5,
//            Runtime.getRuntime().availableProcessors() + 1,//可用处理器
////            150,
//            10L, TimeUnit.SECONDS,
////                new ArrayBlockingQueue<>(100),
//            new SynchronousQueue<>(),
////                new ThreadPoolExecutor.CallerRunsPolicy()
//            new NamingThreadFactory("boling-threadPool-thread")
//    );

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
            }
        }
    };

    private void capture() {
        if (stopRecord()) {
            LogUtil.i(TAG, "capture: 屏幕录制已停止");
        } else {
            MediaProjection projection = getmMediaProjection();
            if (projection == null) {
                //直接获取保存的  manager\intent和result
                projection = getMediaProjectionManager().getMediaProjection(getResult(), getIntent());
                setmMediaProjection(projection);
            }
            if (recorder != null) {
                recorder.quit();
            }
            if (recorder == null) {
                recorder = new ScreenRecorder(screenWidth, screenHeight, maxBitRate, dpi, projection, "");
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
//        ActivityStackManager.getInstance().init(this);
        LogUtil.i(TAG, "Application onCreate -->");
//        AutoSize.checkAndInit(this);
//        AutoSizeConfig.getInstance()
//                .setLog(false);
        initScreenParam();
        loadX5();
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadCaseAction.SCREEN_SHOT);
        filter.addAction(BroadCaseAction.STOP_SCREEN_SHOT);
        lbm.registerReceiver(receiver, filter);
        CrashHandler.getInstance().init(applicationContext);

        //屏幕适配
//        AutoSize.checkAndInit(this);
//        AutoSize.initCompatMultiProcess(appContext);
//        AutoSizeConfig.getInstance()
//                .setLog(true)
//                .setPrivateFontScale(0)//字体大小放大的比例, 设为 0 则取消此功能
//                .getExternalAdaptManager()
//                .addCancelAdaptOfActivity(ImagePreviewActivity.class)//取消对MapActivity的适配
//                .addExternalAdaptInfoOfActivity(ZoneFragment.class, new ExternalAdaptInfo(true, 360))
//                .addExternalAdaptInfoOfActivity(BindFragment.class, new ExternalAdaptInfo(true, 360))
//                .addExternalAdaptInfoOfActivity(TaskFragment.class, new ExternalAdaptInfo(true, 360))
//        ;
    }

    public static QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
        @Override
        public void onCoreInitFinished() {
            //x5内核初始化完成回调接口，此接口回调并表示已经加载起来了x5，有可能特殊情况下x5内核加载失败，切换到系统内核。
            LogUtil.i(TAG, "x5内核 onCoreInitFinished-->");
        }

        @Override
        public void onViewInitFinished(boolean b) {
            initX5Finished = true;
            //ToastUtil.showToast(usedX5 ? R.string.tencent_x5_load_successfully : R.string.tencent_x5_load_failed);
            //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
            LogUtil.d(TAG, "x5内核 onViewInitFinished: 加载X5内核是否成功: " + b);
            System.out.println("onViewInitFinished 加载X5内核是否成功=" + b);
            AgendaFragment.needReStart = !b;
            EventBus.getDefault().post(new EventMessage(EventType.init_x5_finished));
        }
    };

    public static void loadX5() {
        boolean canLoadX5 = QbSdk.canLoadX5(applicationContext);
        LogUtil.i(TAG, "x5内核  是否可以加载X5内核 -->" + canLoadX5);
        if (canLoadX5) {
            initX5();
        } else {
            QbSdk.setDownloadWithoutWifi(true);
            QbSdk.setTbsListener(new TbsListener() {
                @Override
                public void onDownloadFinish(int i) {
                    LogUtil.d(TAG, "x5内核 onDownloadFinish -->下载X5内核：" + i);
                }

                @Override
                public void onInstallFinish(int i) {
                    LogUtil.d(TAG, "x5内核 onInstallFinish -->安装X5内核：" + i);
                    if (i == TbsListener.ErrorCode.INSTALL_SUCCESS_AND_RELEASE_LOCK) {
                        initX5();
                    }
                }

                @Override
                public void onDownloadProgress(int i) {
                    LogUtil.d(TAG, "x5内核 onDownloadProgress -->下载X5内核：" + i);
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

    public static int screenWidth, screenHeight;

    private void initScreenParam() {
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        LogUtil.e(TAG, "initScreenParam displayMetrics=" + displayMetrics.toString());
//        int screenWidth = ScreenUtils.getScreenWidth(this);
//        int screenHeight = ScreenUtils.getScreenHeight(this);
//        int virtualBarHeigh = ScreenUtils.getVirtualBarHeight(this);
//        LogUtil.e(TAG, "initScreenParam --> 导航栏高度： " + virtualBarHeigh + "," + screenWidth + "," + screenHeight);

        DisplayMetrics dm = new DisplayMetrics();
        WindowManager window = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        if (window != null) {
            window.getDefaultDisplay().getMetrics(dm);
            ShotApplication.screenWidth = dm.widthPixels;
            ShotApplication.screenHeight = dm.heightPixels;
            width = dm.widthPixels;
            height = dm.heightPixels;
            LogUtil.e(TAG, "initScreenParam :  屏幕宽高 --> " + width + "," + height);
            if (width % 2 != 0) width--;
            if (height % 2 != 0) height--;
            if (width > 1280) {
                width = 1280;
            }
            if (height > 720) {
                height = 720;
            }
            SCREEN_WIDTH = width;
            SCREEN_HEIGHT = height;
            //屏幕密度（0.75 / 1.0 / 1.5）
            float density = dm.density;
            //屏幕密度DPI（120 / 160 / 240）
            dpi = dm.densityDpi;
            LogUtil.e(TAG, "initScreenParam : dpi=" + dpi + ",density=" + dm.toString());
            if (dpi > 320) {
                dpi = 320;
            }
        }
    }

    public static void setMaxBitRate(int type) {
        if (type < 100) maxBitRate = 100 * 1000;
        else if (type > 10000) maxBitRate = 10000 * 1000;
        else maxBitRate = type * 1000;
    }

    public static View getRootView() {
        return ((ViewGroup) (currentActivity().getWindow().getDecorView().findViewById(android.R.id.content))).getChildAt(0);
    }

    /**
     * 获取当前的Activity
     */
    public static Activity currentActivity() {
        return activityList.get(activityList.size() - 1);
    }

    public void addActivity(Activity activity) {
        if (activityList == null) {
            activityList = new ArrayList<>();
        }
        if (!activityList.contains(activity)) {
            LogUtil.d(TAG, "addActivity: 添加activity:  " + activity);
            activityList.add(activity);
        }
    }

    public void delActivity(Activity activity) {
        if (activityList != null && activityList.contains(activity)) {
            LogUtil.d(TAG, "delActivity: 删除activity:  " + activity);
            activityList.remove(activity);
            activity.finish();
        }
        if (activityList != null && activityList.isEmpty()) {
            LogUtil.e(TAG, "delActivity :  注销屏幕录制广播 --> ");
            lbm.unregisterReceiver(receiver);
        }
    }

    public void delAllActivity() {
        openFab(false);
        openNat(false);
        if (activityList != null) {
            for (Activity a : activityList) {
                a.finish();
            }
        }
        LogUtil.e(TAG, "delAllActivity :  注销屏幕录制广播 --> ");
        lbm.unregisterReceiver(receiver);
    }

    public void openFab(boolean open) {
        if (open && !openFab) {
            if (fabIntent == null) {
                fabIntent = new Intent(this, FabService.class);
            }
            LogUtil.d(TAG, "openFab: 开启服务..");
            startService(fabIntent);
            openFab = true;
        } else if (!open && openFab) {
            if (fabIntent != null) {
                LogUtil.d(TAG, "openFab: 停止服务..");
                stopService(fabIntent);
                openFab = false;
            } else {
                LogUtil.d(TAG, "openFab: fabIntent为null..");
            }
        }
    }

    public void openNat(boolean open) {
        if (open && !openNat) {
            if (NatIntent == null) {
                NatIntent = new Intent(this, NativeService.class);
            }
            LogUtil.d(TAG, "openNat: 开启服务..");
            startService(NatIntent);
            openNat = true;
        } else if (!open && openNat) {
            if (NatIntent != null) {
                LogUtil.d(TAG, "openNat: 停止服务..");
                stopService(NatIntent);
                openNat = false;
            } else {
                LogUtil.d(TAG, "openNat: NatIntent为null..");
            }
        }
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public int getResult() {
        return result;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setResult(int result1) {
        this.result = result1;
    }

    public void setIntent(Intent intent1) {
        this.intent = intent1;
    }

    public MediaProjectionManager getMediaProjectionManager() {
        return mMediaProjectionManager;
    }

    public void setMediaProjectionManager(MediaProjectionManager mMediaProjectionManager) {
        this.mMediaProjectionManager = mMediaProjectionManager;
    }

    public MediaProjection getmMediaProjection() {
        return mMediaProjection;
    }

    public void setmMediaProjection(MediaProjection mMediaProjection) {
        this.mMediaProjection = mMediaProjection;
    }

}
