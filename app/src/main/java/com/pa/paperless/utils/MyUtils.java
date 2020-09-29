package com.pa.paperless.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;

import com.pa.paperless.data.constant.Values;

import android.telephony.TelephonyManager;
import android.widget.PopupWindow;


import com.google.protobuf.ByteString;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.Macro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/11/17.
 */

public class MyUtils {
    public static final String TAG = "MyUtils-->";

    /**
     * @param type =0检查是否有后置摄像头，=1检查是否有前置摄像头
     */
    public static boolean checkCamera(Context context, int type) {
        // 不兼容Android 5.0以下版本
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length > 0) {
                if (type == 0) {
                    //后置摄像头
                    return cameraIds[0] != null;
                } else {
                    //后置摄像头
                    return cameraIds[1] != null;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 重启应用
     */
    public static void reStartApp(Activity cxt) {
        Intent intent = cxt.getBaseContext().getPackageManager().getLaunchIntentForPackage(cxt.getBaseContext().getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(cxt.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) cxt.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, restartIntent);
        Process.killProcess(Process.myPid());
    }

    /**
     * 计算媒体ID
     *
     * @param path 文件路径
     * @return 媒体ID
     */
    public static int getMediaid(String path) {
        //其它
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return Macro.MEDIA_FILE_TYPE_OTHER | Macro.MEDIA_FILE_TYPE_OTHER_SUB;
        }
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return Macro.MEDIA_FILE_TYPE_RECORD | Macro.MEDIA_FILE_TYPE_OTHER_SUB;
        }
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return Macro.MEDIA_FILE_TYPE_UPDATE | Macro.MEDIA_FILE_TYPE_OTHER_SUB;
        }
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return Macro.MEDIA_FILE_TYPE_TEMP | Macro.MEDIA_FILE_TYPE_OTHER_SUB;
        }
        //
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return Macro.MAIN_TYPE_BITMASK | Macro.SUB_TYPE_BITMASK;
        }
        //音频
        if (FileUtil.isVideoFile(path)) {
            return Macro.MEDIA_FILE_TYPE_AUDIO | Macro.MEDIA_FILE_TYPE_PCM;
        }
        if (FileUtil.isVideoFile(path)) {
            return Macro.MEDIA_FILE_TYPE_AUDIO | Macro.MEDIA_FILE_TYPE_MP3;
        }
        if (FileUtil.isVideoFile(path)) {
            return Macro.MEDIA_FILE_TYPE_AUDIO | Macro.MEDIA_FILE_TYPE_ADPCM;
        }
        if (FileUtil.isVideoFile(path)) {
            return Macro.MEDIA_FILE_TYPE_AUDIO | Macro.MEDIA_FILE_TYPE_FLAC;
        }
        if (FileUtil.isVideoFile(path)) {
            return Macro.MEDIA_FILE_TYPE_AUDIO | Macro.MEDIA_FILE_TYPE_MP4;
        }
        //视屏
        if (FileUtil.isVideoFile(path)) {
            return Macro.MEDIA_FILE_TYPE_VIDEO | Macro.MEDIA_FILE_TYPE_MKV;
        }
        if (FileUtil.isVideoFile(path)) {
            return Macro.MEDIA_FILE_TYPE_VIDEO | Macro.MEDIA_FILE_TYPE_RMVB;
        }
        if (FileUtil.isVideoFile(path)) {
            return Macro.MEDIA_FILE_TYPE_VIDEO | Macro.MEDIA_FILE_TYPE_AVI;
        }
        if (FileUtil.isVideoFile(path)) {
            return Macro.MEDIA_FILE_TYPE_VIDEO | Macro.MEDIA_FILE_TYPE_RM;
        }
        //图片
        if (FileUtil.isPictureFile(path)) {
            return Macro.MEDIA_FILE_TYPE_PICTURE | Macro.MEDIA_FILE_TYPE_BMP;
        }
        if (FileUtil.isPictureFile(path)) {
            return Macro.MEDIA_FILE_TYPE_PICTURE | Macro.MEDIA_FILE_TYPE_JPEG;
        }
        if (FileUtil.isPictureFile(path)) {
            return Macro.MEDIA_FILE_TYPE_PICTURE | Macro.MEDIA_FILE_TYPE_PNG;
        }
        return 0;
    }

//    /**
//     * 获取设备唯一值
//     */
//    public static String getUniqueId(Context context) {
//        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String imei = "";
//        if (telephonyManager != null) {
//            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    imei = telephonyManager.getImei();
//                } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//                    imei = telephonyManager.getDeviceId();
//                }
//            } else {
//                LogUtil.i(TAG, "getIMEI 木有权限");
//            }
//        }
//        if (imei == null || imei.isEmpty()) {
//            imei = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
//        }
//        return imei;
//    }

    /**
     * 获取手机的唯一标识符
     * AndroidId 和 Serial Number结合使用
     */
    public static String getUniqueId(Context context) {
        @SuppressLint("HardwareIds") String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        @SuppressLint("HardwareIds") String id = androidID + Build.SERIAL;
        try {
            return toMD5(id);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return id;
        }
    }

    /**
     * 字符串md5加密
     *
     * @param text 要加密的字符串
     * @return 加密后的字符串
     * @throws NoSuchAlgorithmException 算法异常
     */
    private static String toMD5(String text) throws NoSuchAlgorithmException {
        //获取摘要器 MessageDigest
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        //通过摘要器对字符串的二进制字节数组进行hash计算
        byte[] digest = messageDigest.digest(text.getBytes());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            //循环每个字符 将计算结果转化为正整数;
            int digestInt = digest[i] & 0xff;
            //将10进制转化为较短的16进制
            String hexString = Integer.toHexString(digestInt);
            //转化结果如果是个位数会省略0,因此判断并补0
            if (hexString.length() < 2) {
                sb.append(0);
            }
            //将循环结果添加到缓冲区
            sb.append(hexString);
        }
        //返回整个结果
        return sb.toString();
    }


    /**
     * 设置popupWindow 的动画
     */
    public static void setPopAnimal(PopupWindow popupWindow) {
        popupWindow.setAnimationStyle(R.style.Anim_PopupWindow);
    }

    /**
     * String 转 ByteString
     */
    public static ByteString s2b(String name) {
        return ByteString.copyFrom(name, Charset.forName("UTF-8"));
    }

    /**
     * ByteString 转 String
     *
     * @param string
     * @return
     */
    public static String b2s(ByteString string) {
        return string.toStringUtf8();
    }

    /**
     * 读取TXT格式文件
     *
     * @param strFilePath
     * @return 返回该文件的文本内容
     */
    public static String ReadTxtFile(String strFilePath) {
        LogUtil.e(TAG, "MyUtils.ReadTxtFile :  要读取的文件 --> " + strFilePath);
        long startTime = System.currentTimeMillis();
        LogUtil.e(TAG, "MyUtils.ReadTxtFile :  开始 --> " + startTime);
        String path = String.valueOf(strFilePath);
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            LogUtil.e(TAG, "MyUtils.ReadTxtFile :  错误：不能读取文件夹的内容 --> ");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                LogUtil.e(TAG, "MyUtils.ReadTxtFile :  没有找到该文件 --> ");
            } catch (IOException e) {
                LogUtil.e(TAG, "MyUtils.ReadTxtFile :  读取文件异常 --> " + e.getMessage());
            }
        }
        long endTime = System.currentTimeMillis();
        LogUtil.e(TAG, "MyUtils.ReadTxtFile :  结束 --> " + endTime + "  用时：" + (endTime - startTime) / 600);
        return content;
    }


    /**
     * 获取选中的项数
     *
     * @param permission 10进制int型数据
     * @return
     */
    public static List<Integer> getChoose(int permission) {
        List<Integer> ls = new ArrayList<>();
        //将10进制转换成2进制字符串 010001
        String to2 = Integer.toBinaryString(permission);
        int length = to2.length();
        for (int j = 0; j < length; j++) {
            char c = to2.charAt(j);
            //将 char 转换成int型整数
            int a = c - '0';
            if (a == 1) {
                //从右往左数 <--
                //举个栗子： 000100  得到的是第3个
                int i1 = length - j;
                ls.add(i1);
            }
        }
        return ls;
    }

    /**
     * @param memberid 人员ID
     * @param code     参会人权限
     */
    public static boolean isHavePermission(int memberid, int code) {
        for (int i = 0; i < Values.mPermissionsList.size(); i++) {
            InterfaceMember.pbui_Item_MemberPermission item = Values.mPermissionsList.get(i);
            if (item.getMemberid() == memberid) {
                int permission = item.getPermission();
                return isHasPermission(code, permission);
            }
        }
        return false;
    }

    /**
     * 查看本机是否有某权限
     */
    public static boolean isHasPermission(int code) {
        LogUtil.i(TAG, "isHasPermission " + code);
        if (Values.hasAllPermissions) return true;
        return (Values.localPermission & code) == code;
    }

    /**
     * 判断是否有指定权限
     *
     * @param code       指定的权限码 Macro.permission_code_screen
     * @param permission 参会人的权限
     */
    public static boolean isHasPermission(int code, int permission) {
        return (permission & code) == code;
    }

    /**
     * 翻转bitmap (-1,1)左右翻转  (1,-1)上下翻转
     *
     * @param srcBitmap
     * @param sx
     * @param sy
     * @return
     */
    public static Bitmap turnCurrentLayer(Bitmap srcBitmap, float sx, float sy) {
        Bitmap cacheBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);// 创建缓存像素的位图
        int w = cacheBitmap.getWidth();
        int h = cacheBitmap.getHeight();
        Canvas cv = new Canvas(cacheBitmap);//使用canvas在bitmap上面画像素
        Matrix mMatrix = new Matrix();//使用矩阵 完成图像变换
        mMatrix.postScale(sx, sy);//重点代码，记住就ok
        Bitmap resultBimtap = Bitmap.createBitmap(srcBitmap, 0, 0, w, h, mMatrix, true);
        cv.drawBitmap(resultBimtap,
                new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight()),
                new Rect(0, 0, w, h), null);
        return resultBimtap;
    }

    /**
     * 将一个数字转换成时间 00:00:00
     *
     * @param nowTime eg:211
     * @return
     */
    public static String intTotime(int nowTime) {
        int hour = 0;
        int min = 0;
        int sec = 0;
        if (nowTime % 3600 == 0) {
            hour = nowTime / 3600;
        } else {
            hour = nowTime / 3600;
            int lastTime = nowTime % 3600;
            if (lastTime % 60 == 0) {
                min = lastTime / 60;
            } else {
                min = lastTime / 60;
                sec = lastTime % 60;
            }
        }
        String hourStr = (hour < 10) ? "0" + hour : hour + "";
        String minStr = (min < 10) ? "0" + min : min + "";
        String secStr = (sec < 10) ? "0" + sec : sec + "";
        LogUtil.i(TAG, "结果：" + hourStr + ":" + minStr + ":" + secStr);
        return hourStr + ":" + minStr + ":" + secStr;
    }

    /**
     * 数学除法运算
     *
     * @param a     被除数
     * @param b     除数
     * @param scale 小数位
     * @return
     */
    public static double divide(double a, double b, int scale) {
        BigDecimal d = new BigDecimal(Double.toString(a));
        BigDecimal e = new BigDecimal(Double.toString(b));
        return d.divide(e, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
