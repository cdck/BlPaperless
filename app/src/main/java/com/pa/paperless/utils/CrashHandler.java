package com.pa.paperless.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.pa.paperless.data.constant.Macro.INIT_FILE_SD_PATH;

/**
 * Created by Administrator on 2018/2/25.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private Thread.UncaughtExceptionHandler mDefaultHandler;// 系统默认的UncaughtException处理类
    private static CrashHandler INSTANCE = new CrashHandler();// CrashHandler实例
    private Context mContext;// 程序的Context对象
    private Map<String, String> info = new HashMap<String, String>();// 用来存储设备信息和异常信息
    private SimpleDateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd_HH:mm:ss");// 用于格式化日期,作为日志文件名的一部分
    private String BUGPATH = INIT_FILE_SD_PATH + "/Log";

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
        Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
    }

    /**
     * 当UncaughtException发生时会转入该重写的方法来处理
     */
    public void uncaughtException(Thread thread, Throwable ex) {
        // 如果自定义的没有处理则让系统默认的异常处理器来处理
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);// 如果处理了，让程序继续运行3秒再退出，保证文件保存并上传到服务器
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 异常信息
     * @return true 如果处理了该异常信息;否则返回false.
     */
    public boolean handleException(Throwable ex) {
        if (ex == null)
            return false;
        new Thread() {
            public void run() {
                Looper.prepare();
                ToastUtil.showToast( "很抱歉,程序出现异常,即将退出");
                Looper.loop();
            }
        }.start();
        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
        saveCrashInfo2File(ex);
        //打印错误信息
        ex.printStackTrace();
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param context
     */
    public void collectDeviceInfo(Context context) {
        try {
            PackageManager pm = context.getPackageManager();// 获得包管理器
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                info.put("versionName", versionName);
                info.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Field[] fields = Build.class.getDeclaredFields();// 反射机制
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                info.put(field.getName(), field.get("").toString());
                LogUtil.d(TAG, field.getName() + ":" + field.get(""));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将程序崩溃信息保存到本地文件
     *
     * @param ex
     * @return
     */
    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();

        String devInfo = "手机设备名：" + Build.DEVICE
                + "\n手机品牌：" + android.os.Build.BRAND + ", 手机厂商：" + android.os.Build.MANUFACTURER
                + "\n手机型号：" + android.os.Build.MODEL
                + "\n系统版本名称：" + Build.DISPLAY
                + "\n操作系统: " + android.os.Build.VERSION.RELEASE
                + "\nSDK_INI：" + Build.VERSION.SDK_INT
                + "\n主板名：" + Build.BOARD
                + "\n硬件：" + Build.HARDWARE+"\n\n";
        sb.append(devInfo);

        for (Map.Entry<String, String> entry : info.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\r\n");
        }
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);

        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        ex.printStackTrace();
        // 循环着把所有的异常信息写入writer中
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }

        pw.close();// 记得关闭
        String result = writer.toString();
        sb.append(result);
        // 保存文件
        long timeStamp = System.currentTimeMillis();
        String time = format.format(new Date());
        String fileName = "crash-" + time + "-" + timeStamp + ".log";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File dir = new File(BUGPATH);
                LogUtil.i("CrashHandler", dir.toString());
                if (!dir.exists())
                    dir.mkdir();
                FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
                fos.write(sb.toString().getBytes());
                fos.close();
                return fileName;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
