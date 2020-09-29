package com.pa.paperless.data.constant;

/**
 * @author xlk
 * @date 2018/5/30
 */

public class WpsModel {
    /**
     * 打开文件的模式。
     */
    public static final String OPEN_MODE = "OpenMode";
    /**
     * 文件保存时是否发送广播
     */
    public static final String SEND_SAVE_BROAD = "SendSaveBroad";
    /**
     * 文件关闭时是否发送广播
     */
    public static final String SEND_CLOSE_BROAD = "SendCloseBroad";
    /**
     * 监听home键并发广播
     */
    public static final String HOMEKEY_DOWN = "HomeKeyDown";
    /**
     * 监听back键并发广播
     */
    public static final String BACKKEY_DOWN = "BackKeyDown";
    /**
     * 第三方的包名，关闭的广播会包含该项。
     */
    public static final String THIRD_PACKAGE = "ThirdPackage";
    /**
     * 关闭文件时是否请空临时文件
     */
    public static final String CLEAR_BUFFER = "ClearBuffer";
    /**
     * 关闭文件时是否删除使用记录
     */
    public static final String CLEAR_TRACE = "ClearTrace";
    /**
     * 关闭文件时是否删除打开的文件
     */
    public static final String CLEAR_FILE = "ClearFile";
    /**
     * 文件上次查看的进度
     */
    public static final String VIEW_PROGRESS = "ViewProgress";
    /**
     * 是否自动跳转到上次查看的进度
     */
    public static final String AUTO_JUMP = "AutoJump";
    /**
     * 文件保存路径
     */
    public static final String SAVE_PATH = "SavePath";
    /**
     * 文件上次查看的视图的缩放
     */
    public static final String VIEW_SCALE = "ViewScale";
    /**
     * 文件上次查看的视图的X坐标
     */
    public static final String VIEW_SCALE_X = "ViewScrollX";
    /**
     * 文件上次查看的视图的Y坐标
     */
    public static final String VIEW_SCALE_Y = "ViewScrollY";
    /**
     * 批注的作者
     */
    public static final String USER_NAME = "UserName";
    /**
     * 以修订模式打开文档
     */
    public static final String ENTER_REVISE_MODE = "EnterReviseMode";
    /**
     * Wps生成的缓存文件外部是否可见
     */
    public static final String CACHE_FILE_INVISIBLE = "CacheFileInvisible";

    public class OpenMode {
        public static final String NORMAL = "Normal";// 正常模式
        public static final String READ_ONLY = "ReadOnly";// 只读模式
        public static final String READ_MODE = "ReadMode";// 打开直接进入阅读器模式 仅Word、TXT文档支持
        public static final String SAVE_ONLY = "SaveOnly";// 保存模式(打开文件,另存,关闭) 仅Word、TXT文档支持
    }

    public class ClassName {
        public static final String NORMAL = "cn.wps.moffice.documentmanager.PreStartActivity2";// 普通版
        public static final String ENGLISH = "cn.wps.moffice.documentmanager.PreStartActivity2";// 英文版
        public static final String ENTERPRISE = "cn.wps.moffice.documentmanager.PreStartActivity2";// 企业版
    }

    public class PackageName {
        public static final String NORMAL = "cn.wps.moffice_eng";// 普通版
        public static final String ENGLISH = "cn.wps.moffice_eng";// 英文版
        public static final String ENTERPRISE = "cn.wps.moffice_ent";// 企业版
    }

    public class Reciver {
        public static final String ACTION_BACK = "com.kingsoft.writer.back.key.down";// 返回键广播
        public static final String ACTION_HOME = "com.kingsoft.writer.home.key.down";// Home键广播
        public static final String ACTION_SAVE = "cn.wps.moffice.file.save";// 保存广播
        public static final String ACTION_CLOSE = "cn.wps.moffice.file.close";// 关闭文件广播
    }

    public class ReciverExtra {
        //关闭文件广播返回的信息
        public static final String CLOSEFILE = "CloseFile";//关闭文件的路径
        public static final String THIRDPACKAGE = "ThirdPackage";//传入的第三方的包名
        public static final String VIEWPROGRESS = "ViewProgress";//文件查看的进度
        public static final String VIEWSCALE = "ViewScale";//文件上次查看的视图的缩放
        public static final String VIEWSCROLLX = "ViewScrollX";//文件上次查看的视图的X坐标
        public static final String VIEWSCROLLY = "ViewScrollY";//文件上次查看的视图的Y坐标
        //保存文件广播返回的信息
        public static final String OPENFILE = "OpenFile";//文件最初的路径
        public static final String SAVEPATH = "SavePath";//文件这次保存的路径
    }
}
