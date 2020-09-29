package com.pa.paperless.utils;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.pa.paperless.data.bean.MeetDirFileInfo;
import com.pa.paperless.data.bean.PictureInfo;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.data.constant.WpsModel;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.boling.paperless.BuildConfig;
import com.pa.boling.paperless.R;
import com.pa.paperless.service.NativeService;
import com.pa.paperless.service.ShotApplication;
import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

/**
 * Created by Administrator on 2017/11/24.
 */

public class FileUtil {
    public static final String TAG = "FileUtil-->";

    /**
     * 获取字符串的前/后缀名
     *
     * @param filepath 字符串
     * @param type     =0 获取后缀 =1获取前缀
     *                 eg: a\b\c.txt  =0
     */
    public static String getCutStr(String filepath, int type) {
        if ((filepath != null) && (filepath.length() > 0)) {
            int dot = filepath.lastIndexOf('.');
            //该文件名有.符号  且不能是最后一个字符
            if ((dot > -1) && (dot < (filepath.length() - 1))) {
                if (type == 0) {//获取后缀
                    return filepath.substring(dot + 1);
                } else {//获取前缀名称
                    return filepath.substring(0, dot);
                }
            }
        }
        //有的文件是没有后缀的
        return "";
    }

    //缓存目录下的所有图片和文档文件
    public static void cacheDirFile(int dirId) {
        try {
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo info = NativeUtil.getInstance().queryMeetDirFile(dirId);
            if (info == null) {
                LogUtil.e(TAG, "cacheDirFile 查询会议目录文件为null");
                return;
            }
            List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> itemList = info.getItemList();
            for (InterfaceFile.pbui_Item_MeetDirFileDetailInfo item : itemList) {
                cacheFile(item);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private static void cacheFile(@NonNull InterfaceFile.pbui_Item_MeetDirFileDetailInfo info) {
        String fileName = info.getName().toStringUtf8();
        if (isVideoFile(fileName) || isOtherFile(fileName)) {
            LogUtil.e(TAG, "downAllFile 不缓存视频和其它类的文件");
            return;
        }
//        if (isPictureFile(fileName) && FileSizeUtil.getFileMb(info.getSize()) > Macro.MAX_CACHE_IMAGE_SIZE) {
//            LogUtil.i(TAG, "downAllFile 该图片文件大小超过10M，不进行自动缓存");
//            return;
//        }
        CreateDir(Macro.CACHE_ALL_FILE);
        int mediaid = info.getMediaid();
        long size = info.getSize();
        String pathname = Macro.CACHE_ALL_FILE + fileName;
        File file = new File(pathname);
        if (!file.exists() || file.length() != size) {
            NativeUtil.getInstance().creationFileDownload(pathname, mediaid, 1, 0, Macro.DOWNLOAD_CACHE_FILE);
        } else {
            LogUtil.e(TAG, fileName + " 文件已经存在");
            if (isPictureFile(fileName)) {
                LogUtil.i(TAG, "downAllFile 如果是图片文件则添加到集合中去 mediaid=" + mediaid + ",路径=" + file.getAbsolutePath());
                NativeService.addPictureInfo(new PictureInfo(mediaid, file.getAbsolutePath()));
            }
        }
    }

    /**
     * 下载文件到离线会议
     */
    public static void downOfflineFile(MeetDirFileInfo data) {
        NativeUtil.getInstance().createFileCache(data.getDirid(), data.getMediaId(), 1, 0, Macro.DOWNLOAD_OFFLINE_MEETING);
    }

    /**
     * 下载指定文件到指定目录
     *
     * @param dir 下载的目录
     */
    public static void downFile(MeetDirFileInfo data, String dir) {
        CreateDir(dir);
        String fileName = data.getFileName();
        int mediaId = data.getMediaId();
        long size = data.getSize();
        String pathname = dir + fileName;
        File file = new File(pathname);
        if (!file.exists() || file.length() != size) {//如果文件中没有或则大小不一致则进行覆盖下载
            NativeUtil.getInstance().creationFileDownload(pathname, mediaId, 1, 0, Macro.DOWNLOAD_FILE);
        } else {
            LogUtil.i(TAG, "downQueue " + fileName + " 无须再次下载");
            ToastUtil.showToast(ShotApplication.applicationContext.getString(R.string.file_already_exists, fileName));
        }
    }

    public static void openFile(String dir, String filename, final NativeUtil nativeUtil, final int mediaid, Context context, long filesize) {
        //先创建好目录
        CreateDir(dir);
        dir += filename;
        LogUtil.e(TAG, "openFile : 要打开的文件 --> " + dir);
        File file1 = new File(dir);
        File file2 = new File(Macro.MEET_MATERIAL + filename);
        if (!file1.exists() || file1.length() != filesize) {//本地不存在
            LogUtil.d(TAG, "openFile: 本地不存在.." + dir);
            if (!file2.exists() || file2.length() != filesize) {
                Values.isOpenFile = true;
                nativeUtil.creationFileDownload(dir, mediaid, 1, 0, Macro.DOWNLOAD_VIEW_FILE);
            } else {
                OpenThisFile(context, file2, mediaid);
            }
        } else {//存在最新文件
            LogUtil.d(TAG, "openFile: 存在最新文件..");
            OpenThisFile(context, file1, mediaid);
        }
    }

    public static void OpenThisFile(Context context, File file, int mediaid) {
        String filename = file.getName();
        LogUtil.e(TAG, "OpenThisFile :   --> " + filename);
        if (FileUtil.isVideoFile(filename)) {
            return;
        }
        if (FileUtil.isPictureFile(filename)) {
            EventBus.getDefault().post(new EventMessage(EventType.open_picture, file.getAbsolutePath(), mediaid));
        } else if (FileUtil.isDocumentFile(filename)) {
            EventBus.getDefault().post(new EventMessage(EventType.WPS_BROAD_CASE_INFORM, true));
            /** **** **  如果是文档类文件并且不是pdf文件，设置只能使用WPS软件打开  ** **** **/
            Bundle bundle = new Bundle();
            bundle.putString(WpsModel.OPEN_MODE, WpsModel.OpenMode.NORMAL); // 正常模式

            bundle.putBoolean(WpsModel.SEND_CLOSE_BROAD, true); // 文件关闭时是否发送广播
            bundle.putBoolean(WpsModel.SEND_SAVE_BROAD, true); // 文件保存时是否发送广播
            bundle.putBoolean(WpsModel.HOMEKEY_DOWN, true); // 单机home键是否发送广播
            bundle.putBoolean(WpsModel.BACKKEY_DOWN, true); // 单机back键是否发送广播

            bundle.putString(WpsModel.THIRD_PACKAGE, WpsModel.PackageName.NORMAL); // 第三方应用的包名，用于对改应用合法性的验证
            bundle.putBoolean(WpsModel.CLEAR_FILE, false); //关闭后是否删除打开文件

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(WpsModel.PackageName.NORMAL, WpsModel.ClassName.NORMAL);
            intent.putExtras(bundle);
            uriX(context, intent, file);
        } else {
            //已经存在才打开文件
            Intent intent = new Intent();
            //设置intent的Action属性
            intent.setAction(Intent.ACTION_VIEW);
            uriX(context, intent, file);
        }
    }

    /**
     * 抽取成工具方法
     */
    private static void uriX(Context context, Intent intent, File file) {
        if (Build.VERSION.SDK_INT > 23) {//android 7.0以上时，URI不能直接暴露
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //另：androidx.core.content.FileProvider替换android.support.v4.content.FileProvider
            Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(uriForFile, "application/vnd.android.package-archive");
        } else {
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastUtil.showToast(R.string.please_check_wps);
            e.printStackTrace();
        }
    }

    /**
     * 查找指定目录下的指定类型文件
     *
     * @param dir         搜索目录
     * @param Extension   扩展名 ".txt"
     * @param IsIterative 是否进入子文件夹
     */
    public static List<File> GetFiles(String dir, String Extension, boolean IsIterative, List<File> lstFile) {
        File[] files = new File(dir).listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isFile()) {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))   //判断扩展名
                    lstFile.add(f);
                if (!IsIterative)
                    break;
            } else if (f.isDirectory() && f.getPath().indexOf("/.") == -1)  //忽略点文件（隐藏文件/文件夹）
                GetFiles(f.getPath(), Extension, IsIterative, lstFile);
        }
        return lstFile;
    }

    /**
     * 多级创建目录
     */
    public static boolean CreateDir(String dir) {
        File file = new File(dir);
        if (file.exists()) {
            return true;
        } else if (file.mkdirs()) {
            return true;
        }
        return false;
    }

    /**
     * 多级创建文件
     *
     * @param filepath C:\meetsystem\abc.txt
     */
    public static File CreateFile(String filepath) {
        File file = new File(filepath);
//        CreateDir(file.getAbsolutePath());
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 将BitMap保存到指定目录下
     */
    public static void saveBitmap(Bitmap bitmap, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                LogUtil.e(TAG, "saveBitmap :  回收 --> ");
                bitmap.recycle();
            }
        }
    }

    /**
     * 文件名是否合法
     *
     * @param fileName 传入不包含后缀
     */
    public static boolean isLegalName(String fileName) {
        String regex = "[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$";
        return fileName.matches(regex);
    }

    /**
     * 判断是否为视频文件
     */
    public static boolean isVideoFile(String fileName) {
        if (!fileName.contains(".")) {//该文件没有后缀
            return false;
        }
        //获取文件的扩展名  mp3/mp4...
        String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        if (fileEnd.equals("mp4")
                || fileEnd.equals("3gp")
                || fileEnd.equals("wav")
                || fileEnd.equals("mp3")
                || fileEnd.equals("wmv")
                || fileEnd.equals("ts")
                || fileEnd.equals("rmvb")
                || fileEnd.equals("mov")
                || fileEnd.equals("m4v")
                || fileEnd.equals("avi")
                || fileEnd.equals("m3u8")
                || fileEnd.equals("3gpp")
                || fileEnd.equals("3gpp2")
                || fileEnd.equals("mkv")
                || fileEnd.equals("flv")
                || fileEnd.equals("divx")
                || fileEnd.equals("f4v")
                || fileEnd.equals("rm")
                || fileEnd.equals("asf")
                || fileEnd.equals("ram")
                || fileEnd.equals("mpg")
                || fileEnd.equals("v8")
                || fileEnd.equals("swf")
                || fileEnd.equals("m2v")
                || fileEnd.equals("asx")
                || fileEnd.equals("ra")
                || fileEnd.equals("naivx")
                || fileEnd.equals("xvid")
        ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否为图片文件
     *
     * @param fileName eg: 123.png
     */
    public static boolean isPictureFile(String fileName) {
        if (!fileName.contains(".")) {//该文件没有后缀
            return false;
        }
        //获取文件的扩展名
        String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        if (fileEnd.equals("jpg")
                || fileEnd.equals("png")
                || fileEnd.equals("gif")
                || fileEnd.equals("img")
                || fileEnd.equals("bmp")
                || fileEnd.equals("jpeg")
        ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否为文档类文件
     */
    public static boolean isDocumentFile(String fileName) {
        if (!fileName.contains(".")) {//该文件没有后缀
            return false;
        }
        //获取文件的扩展名 -->获得的是小写：toLowerCase()  /大写:toUpperCase()
        String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        if (fileEnd.equals("txt")
                || fileEnd.equals("doc")
                || fileEnd.equals("docx")
                || fileEnd.equals("log")
                || fileEnd.equals("dot")
                || fileEnd.equals("dotx")
                || fileEnd.equals("ppt")
                || fileEnd.equals("pptx")
                || fileEnd.equals("pps")
                || fileEnd.equals("ppsx")
                || fileEnd.equals("pot")
                || fileEnd.equals("potx")
                || fileEnd.equals("xls")
                || fileEnd.equals("xlsx")
                || fileEnd.equals("xlt")
                || fileEnd.equals("xltx")
                || fileEnd.equals("wpt")
                || fileEnd.equals("wps")
                || fileEnd.equals("csv")
                || fileEnd.equals("pdf")
        ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否为pdf文件
     */
    public static boolean ispdfFile(String fileName) {
        //获取文件的扩展名 -->获得的是小写：toLowerCase()  /大写:toUpperCase()
        String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        if (fileEnd.equals("pdf")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否为除文档、视频、图片外的其它类文件
     */
    public static boolean isOtherFile(String fileName) {
        //除去文档/视频/图片其它的后缀名文件
        return !isDocumentFile(fileName) && !isVideoFile(fileName) && !isPictureFile(fileName);
    }

    /**
     * 向文件中写入数据
     *
     * @param filePath 目标文件全路径
     * @param data     要写入的数据
     * @return true表示写入成功  false表示写入失败
     */
    public static boolean writeBytes(String filePath, byte[] data) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(data);
            fos.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    /**
     * 从文件中读取数据
     *
     * @param file 文件路径
     */
    public static byte[] readBytes(String file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            int len = fis.available();
            byte[] buffer = new byte[len];
            fis.read(buffer);
            fis.close();
            return buffer;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * 查找指定目录下的指定文件
     *
     * @param dir      父目录
     * @param filename 文件名
     */
    public static File findFilePathByName(String dir, final String filename) {
        File baseDir = new File(dir);
        if (!baseDir.exists() || !baseDir.isDirectory()) {  // 判断目录是否存在
            LogUtil.e(TAG, "FileUtil.findFilePathByName :   --> " + "文件查找失败：" + dir + "不是一个目录！");
            return null;
        }
        File[] fl = baseDir.listFiles();
        List<File> fs = new ArrayList<>();
        List<File> files = findFile(fl, filename, fs);
        if (files.size() > 0) {
            LogUtil.e(TAG, "FileUtil.findFilePathByName :  查找到文件： --> " + files.get(0).getName());
            return files.get(0);
        } else {
            LogUtil.e(TAG, "FileUtil.findFilePathByName :  没有查找到文件 --> ");
            return null;
        }
    }

    private static List<File> findFile(File[] files, String filename, List<File> fs) {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile()) {
                if (filename.equals(file.getName())) {
                    LogUtil.e(TAG, "FileUtil.findFile :  找到文件： --> " + file.getName());
                    fs.add(file);
                }
            } else if (file.isDirectory()) {
                LogUtil.e(TAG, "FileUtil.findFile :  当前为目录 --> ");
                File[] files1 = file.listFiles();
                if (files1.length > 0) {//如果目录下还有文件，递归查找
                    findFile(files1, filename, fs);
                }
            }
        }
        return fs;
    }

    /**
     * 查询目录下指定文件
     *
     * @param name 文件名
     * @return 返回文件路径
     */
    private static File findFileByName(File[] files, String name) {
        File file = null;
        for (File f : files) {
            if (f.isDirectory()) {
                return findFileByName(f.listFiles(), name);
            } else if (f.isFile()) {
                if (f.getName().equals(name)) {
                    file = f;
                }
            }
        }
        return file;
    }

    /**
     * 删除目录下的所有文件，包含目录文件夹
     *
     * @param dirfile 目录文件
     */
    public static boolean deleteAllFile(@NonNull File dirfile) {
        if (!dirfile.exists())
            return true;
        File[] files = dirfile.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
                NativeService.delPictureInfo(f.getAbsolutePath());
            } else if (f.isDirectory()) {
                deleteAllFile(f);
            }
        }
        return dirfile.delete();
    }

    /**
     * 获取该目录下的所有子目录
     *
     * @param dirpath 目录路径
     * @return 该目录下的所有目录的路径
     */
    public static ArrayList<String> findSubDir(String dirpath) {
        ArrayList<String> temps = new ArrayList<>();
        File dirfile = new File(dirpath);
        if (dirfile.exists() && dirfile.isDirectory()) {
            File[] files = dirfile.listFiles();
            for (File f1 : files) {
                if (f1.isDirectory()) {
                    temps.add(f1.getAbsolutePath());
                }
            }
        }
        return temps;
    }

    /**
     * 查找目录下的文件
     *
     * @param dirfile 所有文件路径集合
     */
    public static ArrayList<String> getDirSubFile(String dirfile) {
        File file = new File(dirfile);
        ArrayList<String> temps = new ArrayList<>();
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f1 : files) {
                if (f1.isFile()) {
                    temps.add(f1.getAbsolutePath());
                }
            }
        }
        return temps;
    }

    /**
     * 获取该目录包括子目录下的所有文件的路径
     *
     * @param dirpath 目录文件
     */
    public static void findFile(String dirpath, ArrayList<String> temps) {
        File dirfile = new File(dirpath);
        if (dirfile.exists() && dirfile.isDirectory()) {
            File[] files = dirfile.listFiles();
            for (File f1 : files) {
                if (f1.isFile()) {
                    temps.add(f1.getAbsolutePath());
                } else if (f1.isDirectory()) {
                    findFile(f1.getAbsolutePath(), temps);
                }
            }
        }
    }

    /**
     * 打开文件
     */
    public static void openLocalFile(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = null;
        //7.0以后，用了Content Uri 替换了原本的File Uri
        if (Build.VERSION.SDK_INT > 23) {//android 7.0以上时，URI不能直接暴露
            // 方式一
//            uri = getImageContentUri(context, file);
            // 方式二
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //获取文件file的MIME类型
        String type = getMIMEType(file);
        //设置intent的data和Type属性。
        intent.setDataAndType(uri, type);
        //跳转
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            ToastUtil.showToast(R.string.no_application_can_open);
        }
    }

    /**
     * 获取文件 Content Uri 地址
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     */
    private static String getMIMEType(File file) {
        if (!file.isFile()) {
            LogUtil.e(TAG, "FileUtil.getMIMEType :  错误：你传递的不是一个文件 --> ");
            return null;
        }
        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        // 获取文件的后缀名
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) { //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    //建立一个MIME类型与文件后缀名的匹配表
    private static final String[][] MIME_MapTable = {
            // --{后缀名， MIME类型}   --
            {".3gp", "video/3gpp"},
            {".3gpp", "video/3gpp"},
            {".aac", "audio/x-mpeg"},
            {".amr", "audio/x-mpeg"},
            {".apk", "application/vnd.android.package-archive"},
            {".avi", "video/x-msvideo"},
            {".aab", "application/x-authoware-bin"},
            {".aam", "application/x-authoware-map"},
            {".aas", "application/x-authoware-seg"},
            {".ai", "application/postscript"},
            {".aif", "audio/x-aiff"},
            {".aifc", "audio/x-aiff"},
            {".aiff", "audio/x-aiff"},
            {".als", "audio/x-alpha5"},
            {".amc", "application/x-mpeg"},
            {".ani", "application/octet-stream"},
            {".asc", "text/plain"},
            {".asd", "application/astound"},
            {".asf", "video/x-ms-asf"},
            {".asn", "application/astound"},
            {".asp", "application/x-asap"},
            {".asx", " video/x-ms-asf"},
            {".au", "audio/basic"},
            {".avb", "application/octet-stream"},
            {".awb", "audio/amr-wb"},
            {".bcpio", "application/x-bcpio"},
            {".bld", "application/bld"},
            {".bld2", "application/bld2"},
            {".bpk", "application/octet-stream"},
            {".bz2", "application/x-bzip2"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".cal", "image/x-cals"},
            {".ccn", "application/x-cnc"},
            {".cco", "application/x-cocoa"},
            {".cdf", "application/x-netcdf"},
            {".cgi", "magnus-internal/cgi"},
            {".chat", "application/x-chat"},
            {".clp", "application/x-msclip"},
            {".cmx", "application/x-cmx"},
            {".co", "application/x-cult3d-object"},
            {".cod", "image/cis-cod"},
            {".cpio", "application/x-cpio"},
            {".cpt", "application/mac-compactpro"},
            {".crd", "application/x-mscardfile"},
            {".csh", "application/x-csh"},
            {".csm", "chemical/x-csml"},
            {".csml", "chemical/x-csml"},
            {".css", "text/css"},
            {".cur", "application/octet-stream"},
            {".doc", "application/msword"},
            {".dcm", "x-lml/x-evm"},
            {".dcr", "application/x-director"},
            {".dcx", "image/x-dcx"},
            {".dhtml", "text/html"},
            {".dir", "application/x-director"},
            {".dll", "application/octet-stream"},
            {".dmg", "application/octet-stream"},
            {".dms", "application/octet-stream"},
            {".dot", "application/x-dot"},
            {".dvi", "application/x-dvi"},
            {".dwf", "drawing/x-dwf"},
            {".dwg", "application/x-autocad"},
            {".dxf", "application/x-autocad"},
            {".dxr", "application/x-director"},
            {".ebk", "application/x-expandedbook"},
            {".emb", "chemical/x-embl-dl-nucleotide"},
            {".embl", "chemical/x-embl-dl-nucleotide"},
            {".eps", "application/postscript"},
            {".epub", "application/epub+zip"},
            {".eri", "image/x-eri"},
            {".es", "audio/echospeech"},
            {".esl", "audio/echospeech"},
            {".etc", "application/x-earthtime"},
            {".etx", "text/x-setext"},
            {".evm", "x-lml/x-evm"},
            {".evy", "application/x-envoy"},
            {".exe", "application/octet-stream"},
            {".fh4", "image/x-freehand"},
            {".fh5", "image/x-freehand"},
            {".fhc", "image/x-freehand"},
            {".fif", "image/fif"},
            {".fm", "application/x-maker"},
            {".fpx", "image/x-fpx"},
            {".fvi", "video/isivideo"},
            {".flv", "video/x-msvideo"},
            {".gau", "chemical/x-gaussian-input"},
            {".gca", "application/x-gca-compressed"},
            {".gdb", "x-lml/x-gdb"},
            {".gif", "image/gif"},
            {".gps", "application/x-gps"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".hdf", "application/x-hdf"},
            {".hdm", "text/x-hdml"},
            {".hdml", "text/x-hdml"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".hlp", "application/winhlp"},
            {".hqx", "application/mac-binhex40"},
            {".hts", "text/html"},
            {".ice", "x-conference/x-cooltalk"},
            {".ico", "application/octet-stream"},
            {".ief", "image/ief"},
            {".ifm", "image/gif"},
            {".ifs", "image/ifs"},
            {".imy", "audio/melody"},
            {".ins", "application/x-net-install"},
            {".ips", "application/x-ipscript"},
            {".ipx", "application/x-ipix"},
            {".it", "audio/x-mod"},
            {".itz", "audio/x-mod"},
            {".ivr", "i-world/i-vrml"},
            {".j2k", "image/j2k"},
            {".jad", "text/vnd.sun.j2me.app-descriptor"},
            {".jam", "application/x-jam"},
            {".jnlp", "application/x-java-jnlp-file"},
            {".jpe", "image/jpeg"},
            {".jpz", "image/jpeg"},
            {".jwc", "application/jwc"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".kjx", "application/x-kjx"},
            {".lak", "x-lml/x-lak"},
            {".latex", "application/x-latex"},
            {".lcc", "application/fastman"},
            {".lcl", "application/x-digitalloca"},
            {".lcr", "application/x-digitalloca"},
            {".lgh", "application/lgh"},
            {".lha", "application/octet-stream"},
            {".lml", "x-lml/x-lml"},
            {".lmlpack", "x-lml/x-lmlpack"},
            {".log", "text/plain"},
            {".lsf", "video/x-ms-asf"},
            {".lsx", "video/x-ms-asf"},
            {".lzh", "application/x-lzh "},
            {".m13", "application/x-msmediaview"},
            {".m14", "application/x-msmediaview"},
            {".m15", "audio/x-mod"},
            {".m3u", "audio/x-mpegurl"},
            {".m3url", "audio/x-mpegurl"},
            {".ma1", "audio/ma1"},
            {".ma2", "audio/ma2"},
            {".ma3", "audio/ma3"},
            {".ma5", "audio/ma5"},
            {".man", "application/x-troff-man"},
            {".map", "magnus-internal/imagemap"},
            {".mbd", "application/mbedlet"},
            {".mct", "application/x-mascot"},
            {".mdb", "application/x-msaccess"},
            {".mdz", "audio/x-mod"},
            {".me", "application/x-troff-me"},
            {".mel", "text/x-vmel"},
            {".mi", "application/x-mif"},
            {".mid", "audio/midi"},
            {".midi", "audio/midi"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".mif", "application/x-mif"},
            {".mil", "image/x-cals"},
            {".mio", "audio/x-mio"},
            {".mmf", "application/x-skt-lbs"},
            {".mng", "video/x-mng"},
            {".mny", "application/x-msmoney"},
            {".moc", "application/x-mocha"},
            {".mocha", "application/x-mocha"},
            {".mod", "audio/x-mod"},
            {".mof", "application/x-yumekara"},
            {".mol", "chemical/x-mdl-molfile"},
            {".mop", "chemical/x-mopac-input"},
            {".movie", "video/x-sgi-movie"},
            {".mpn", "application/vnd.mophun.application"},
            {".mpp", "application/vnd.ms-project"},
            {".mps", "application/x-mapserver"},
            {".mrl", "text/x-mrml"},
            {".mrm", "application/x-mrm"},
            {".ms", "application/x-troff-ms"},
            {".mts", "application/metastream"},
            {".mtx", "application/metastream"},
            {".mtz", "application/metastream"},
            {".mzv", "application/metastream"},
            {".nar", "application/zip"},
            {".nbmp", "image/nbmp"},
            {".nc", "application/x-netcdf"},
            {".ndb", "x-lml/x-ndb"},
            {".ndwn", "application/ndwn"},
            {".nif", "application/x-nif"},
            {".nmz", "application/x-scream"},
            {".nokia-op-logo", "image/vnd.nok-oplogo-color"},
            {".npx", "application/x-netfpx"},
            {".nsnd", "audio/nsnd"},
            {".nva", "application/x-neva1"},
            {".oda", "application/oda"},
            {".oom", "application/x-atlasMate-plugin"},
            {".ogg", "audio/ogg"},
            {".pac", "audio/x-pac"},
            {".pae", "audio/x-epac"},
            {".pan", "application/x-pan"},
            {".pbm", "image/x-portable-bitmap"},
            {".pcx", "image/x-pcx"},
            {".pda", "image/x-pda"},
            {".pdb", "chemical/x-pdb"},
            {".pdf", "application/pdf"},
            {".pfr", "application/font-tdpfr"},
            {".pgm", "image/x-portable-graymap"},
            {".pict", "image/x-pict"},
            {".pm", "application/x-perl"},
            {".pmd", "application/x-pmd"},
            {".png", "image/png"},
            {".pnm", "image/x-portable-anymap"},
            {".pnz", "image/png"},
            {".pot", "application/vnd.ms-powerpoint"},
            {".ppm", "image/x-portable-pixmap"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pqf", "application/x-cprplayer"},
            {".pqi", "application/cprplayer"},
            {".prc", "application/x-prc"},
            {".proxy", "application/x-ns-proxy-autoconfig"},
            {".prop", "text/plain"},
            {".ps", "application/postscript"},
            {".ptlk", "application/listenup"},
            {".pub", "application/x-mspublisher"},
            {".pvx", "video/x-pv-pvx"},
            {".qcp", "audio/vnd.qcelp"},
            {".qt", "video/quicktime"},
            {".qti", "image/x-quicktime"},
            {".qtif", "image/x-quicktime"},
            {".r3t", "text/vnd.rn-realtext3d"},
            {".ra", "audio/x-pn-realaudio"},
            {".ram", "audio/x-pn-realaudio"},
            {".ras", "image/x-cmu-raster"},
            {".rdf", "application/rdf+xml"},
            {".rf", "image/vnd.rn-realflash"},
            {".rgb", "image/x-rgb"},
            {".rlf", "application/x-richlink"},
            {".rm", "audio/x-pn-realaudio"},
            {".rmf", "audio/x-rmf"},
            {".rmm", "audio/x-pn-realaudio"},
            {".rnx", "application/vnd.rn-realplayer"},
            {".roff", "application/x-troff"},
            {".rp", "image/vnd.rn-realpix"},
            {".rpm", "audio/x-pn-realaudio-plugin"},
            {".rt", "text/vnd.rn-realtext"},
            {".rte", "x-lml/x-gps"},
            {".rtf", "application/rtf"},
            {".rtg", "application/metastream"},
            {".rtx", "text/richtext"},
            {".rv", "video/vnd.rn-realvideo"},
            {".rwc", "application/x-rogerwilco"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".s3m", "audio/x-mod"},
            {".s3z", "audio/x-mod"},
            {".sca", "application/x-supercard"},
            {".scd", "application/x-msschedule"},
            {".sdf", "application/e-score"},
            {".sea", "application/x-stuffit"},
            {".sgm", "text/x-sgml"},
            {".sgml", "text/x-sgml"},
            {".shar", "application/x-shar"},
            {".shtml", "magnus-internal/parsed-html"},
            {".shw", "application/presentations"},
            {".si6", "image/si6"},
            {".si7", "image/vnd.stiwap.sis"},
            {".si9", "image/vnd.lgtwap.sis"},
            {".sis", "application/vnd.symbian.install"},
            {".sit", "application/x-stuffit"},
            {".skd", "application/x-koan"},
            {".skm", "application/x-koan"},
            {".skp", "application/x-koan"},
            {".skt", "application/x-koan"},
            {".slc", "application/x-salsa"},
            {".smd", "audio/x-smd"},
            {".smi", "application/smil"},
            {".smil", "application/smil"},
            {".smp", "application/studiom"},
            {".smz", "audio/x-smd"},
            {".sh", "application/x-sh"},
            {".snd", "audio/basic"},
            {".spc", "text/x-speech"},
            {".spl", "application/futuresplash"},
            {".spr", "application/x-sprite"},
            {".sprite", "application/x-sprite"},
            {".sdp", "application/sdp"},
            {".spt", "application/x-spt"},
            {".src", "application/x-wais-source"},
            {".stk", "application/hyperstudio"},
            {".stm", "audio/x-mod"},
            {".sv4cpio", "application/x-sv4cpio"},
            {".sv4crc", "application/x-sv4crc"},
            {".svf", "image/vnd"},
            {".svg", "image/svg-xml"},
            {".svh", "image/svh"},
            {".svr", "x-world/x-svr"},
            {".swf", "application/x-shockwave-flash"},
            {".swfl", "application/x-shockwave-flash"},
            {".t", "application/x-troff"},
            {".tad", "application/octet-stream"},
            {".talk", "text/x-speech"},
            {".tar", "application/x-tar"},
            {".taz", "application/x-tar"},
            {".tbp", "application/x-timbuktu"},
            {".tbt", "application/x-timbuktu"},
            {".tcl", "application/x-tcl"},
            {".tex", "application/x-tex"},
            {".texi", "application/x-texinfo"},
            {".texinfo", "application/x-texinfo"},
            {".tgz", "application/x-tar"},
            {".thm", "application/vnd.eri.thm"},
            {".tif", "image/tiff"},
            {".tiff", "image/tiff"},
            {".tki", "application/x-tkined"},
            {".tkined", "application/x-tkined"},
            {".toc", "application/toc"},
            {".toy", "image/toy"},
            {".tr", "application/x-troff"},
            {".trk", "x-lml/x-gps"},
            {".trm", "application/x-msterminal"},
            {".tsi", "audio/tsplayer"},
            {".tsp", "application/dsptype"},
            {".tsv", "text/tab-separated-values"},
            {".ttf", "application/octet-stream"},
            {".ttz", "application/t-time"},
            {".txt", "text/plain"},
            {".ult", "audio/x-mod"},
            {".ustar", "application/x-ustar"},
            {".uu", "application/x-uuencode"},
            {".uue", "application/x-uuencode"},
            {".vcd", "application/x-cdlink"},
            {".vcf", "text/x-vcard"},
            {".vdo", "video/vdo"},
            {".vib", "audio/vib"},
            {".viv", "video/vivo"},
            {".vivo", "video/vivo"},
            {".vmd", "application/vocaltec-media-desc"},
            {".vmf", "application/vocaltec-media-file"},
            {".vmi", "application/x-dreamcast-vms-info"},
            {".vms", "application/x-dreamcast-vms"},
            {".vox", "audio/voxware"},
            {".vqe", "audio/x-twinvq-plugin"},
            {".vqf", "audio/x-twinvq"},
            {".vql", "audio/x-twinvq"},
            {".vre", "x-world/x-vream"},
            {".vrml", "x-world/x-vrml"},
            {".vrt", "x-world/x-vrt"},
            {".vrw", "x-world/x-vream"},
            {".vts", "workbook/formulaone"},
            {".wax", "audio/x-ms-wax"},
            {".wbmp", "image/vnd.wap.wbmp"},
            {".web", "application/vnd.xara"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wi", "image/wavelet"},
            {".wis", "application/x-InstallShield"},
            {".wm", "video/x-ms-wm"},
            {".wmd", "application/x-ms-wmd"},
            {".wmf", "application/x-msmetafile"},
            {".wml", "text/vnd.wap.wml"},
            {".wmlc", "application/vnd.wap.wmlc"},
            {".wmls", "text/vnd.wap.wmlscript"},
            {".wmlsc", "application/vnd.wap.wmlscriptc"},
            {".wmlscript", "text/vnd.wap.wmlscript"},
            {".wmv", "video/x-ms-wmv"},
            {".wmx", "video/x-ms-wmx"},
            {".wmz", "application/x-ms-wmz"},
            {".wpng", "image/x-up-wpng"},
            {".wps", "application/vnd.ms-works"},
            {".wpt", "x-lml/x-gps"},
            {".wri", "application/x-mswrite"},
            {".wrl", "x-world/x-vrml"},
            {".wrz", "x-world/x-vrml"},
            {".ws", "text/vnd.wap.wmlscript"},
            {".wsc", "application/vnd.wap.wmlscriptc"},
            {".wv", "video/wavelet"},
            {".wvx", "video/x-ms-wvx"},
            {".wxl", "application/x-wxl"},
            {".x-gzip", "application/x-gzip"},
            {".xar", "application/vnd.xara"},
            {".xbm", "image/x-xbitmap"},
            {".xdm", "application/x-xdma"},
            {".xdma", "application/x-xdma"},
            {".xdw", "application/vnd.fujixerox.docuworks"},
            {".xht", "application/xhtml+xml"},
            {".xhtm", "application/xhtml+xml"},
            {".xhtml", "application/xhtml+xml"},
            {".xla", "application/vnd.ms-excel"},
            {".xlc", "application/vnd.ms-excel"},
            {".xll", "application/x-excel"},
            {".xlm", "application/vnd.ms-excel"},
            {".xls", "application/vnd.ms-excel"},
            {".xlt", "application/vnd.ms-excel"},
            {".xlw", "application/vnd.ms-excel"},
            {".xm", "audio/x-mod"},
            {".xml", "text/xml"},
            {".xmz", "audio/x-mod"},
            {".xpi", "application/x-xpinstall"},
            {".xpm", "image/x-xpixmap"},
            {".xsit", "text/xml"},
            {".xsl", "text/xml"},
            {".xul", "text/xul"},
            {".xwd", "image/x-xwindowdump"},
            {".xyz", "chemical/x-pdb"},
            {".yz1", "application/x-yz1"},
            {".z", "application/x-compress"},
            {".zac", "application/x-zaurus-zac"},
            {".zip", "application/zip"},
            {"", "*/*"}
    };
}