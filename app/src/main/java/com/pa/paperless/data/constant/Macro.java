package com.pa.paperless.data.constant;


import android.os.Environment;

import com.mogujie.tt.protobuf.InterfaceMacro;

/**
 * @author Administrator
 * @date 2018/2/5
 */

public class Macro {
    /**
     * app各类文件存放路径 SD卡下
     */
    public static final String INIT_FILE_SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NETCONFIG";
    public static final String FILENAME = "client.ini";
    public static final String FILENAME_DEV = "client.dev";

    //下载的文件存放目录
    public static final String ROOT = INIT_FILE_SD_PATH + "/无纸化文件/";
    public static final String MEET_MATERIAL = ROOT + "会议资料/";
    public static final String SHARE_MATERIAL = ROOT + "共享资料/";
    public static final String POSTIL_FILE = ROOT + "批注文件/";
    public static final String MEET_AGENDA = ROOT + "会议议程/";

    public static final String VOTE_RESULT = ROOT + "导出文件/";
    public static final String MEET_NOTE = ROOT + "会议笔记/";
    public static final String CACHE_FILE = ROOT + "临时文件/";
    public static final String CACHE_ALL_FILE = ROOT + "缓存文件/";

    public static final String extra_inviteflag = "extra_inviteflag";//设备对讲
    public static final String extra_operdeviceid = "extra_operdeviceid";//设备对讲

    /**
     * 上传tag
     */
    public static final String upload_wps_file = "upload_wps_file";//上传WPS编辑后的文件
    public static final String upload_drawBoard_pic = "upload_drawBoard_pic";//上传画板图片
    public static final String upload_local_file = "upload_local_file";//上传本地文件
    public static final String upload_screen_shot = "upload_screen_shot";//上传屏幕截图

    /**
     * 下载tag
     */
    //主页背景图片
    public static final String DOWNLOAD_MAIN_BG = "main_bg";
    //logo图标
    public static final String DOWNLOAD_MAIN_LOGO = "logo";
    //议程文件
    public static final String DOWNLOAD_AGENDA_FILE = "agenda_file";
    //子界面背景
    public static final String DOWNLOAD_SUB_BG = "sub_bg";
    //公告页面logo图标
    public static final String DOWNLOAD_NOTICE_LOGO = "notice_logo";
    //公告页面背景图片
    public static final String DOWNLOAD_NOTICE_BG = "notice_bg";
    //会场底图背景图片
    public static final String DOWNLOAD_ROOM_BG = "room_bg";
    //用户点击查看文件时
    public static final String DOWNLOAD_VIEW_FILE = "should_open_file";
    //缓存文件
    public static final String DOWNLOAD_CACHE_FILE = "cache_file";
    //下载文件
    public static final String DOWNLOAD_FILE = "download";
    //推送文件
    public static final String DOWNLOAD_PUSH_FILE = "push_file";
    //下载到离线会议
    public static final String DOWNLOAD_OFFLINE_MEETING = "download_offline_meeting";

    /**
     * 投票时提交，用于签到参与投票
     */
    public static final int PB_VOTE_SELFLAG_CHECKIN = 0x80000000;

    /**
     * 限制范围
     */
    public static final int title_max_length = 30;//标题最大字数限制
    public static final int content_max_length = 106;//公告内容字数限制
    public static final double MAX_CACHE_IMAGE_SIZE = 10;//最大可自动下载的图片 单位M

    /**
     * 特定的目录ID
     */
    public static final int SHARED_FILE_DIRECTORY_ID = 1;//共享文件目录ID
    public static final int ANNOTATION_FILE_DIRECTORY_ID = 2;//批注文件目录ID
    public static final int MEETDATA_FILE_DIRECTORY_ID = 3;//会议资料目录ID

    /**
     * 权限码
     */
    public static final int permission_code_screen = 1;//同屏权限
    public static final int permission_code_projection = 2;//投影权限
    public static final int permission_code_upload = 4;//上传权限
    public static final int permission_code_download = 8;//下载权限
    public static final int permission_code_vote = 16;//投票权限

    /**
     * 设备ID类型
     */

    //区域服务器
//    public static int DEVICE_AREA_SERVER = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_AreaServer.getNumber();
    //媒体服务器
//    public static int DEVICE_MEDIA_SERVER = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MediaServer.getNumber();
    //流服务器
//    public static int DEVICE_STREAM_SERVER = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_StreamServer.getNumber();
    //会议数据库设备
    public static int DEVICE_MEET_DB = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetDBServer.getNumber();
    //会议茶水服务
    public static int DEVICE_MEET_SERVICE = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetService.getNumber();
    //会议投影机
    public static int DEVICE_MEET_PROJECTIVE = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetProjective.getNumber();
    //会议流采集设备
    public static int DEVICE_MEET_CAPTURE = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetCapture.getNumber();
    //会议终端设备
    public static int DEVICE_MEET_CLIENT = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetClient.getNumber();
    //会议视频对讲客户端
    public static int DEVICE_MEET_VIDEO_CLIENT = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetVideoClient.getNumber();
    //会议发布
    public static int DEVICE_MEET_PUBLISH = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetPublish.getNumber();
    //PHP中转数据设备
    public static int DEVICE_MEET_PHPCLIENT = InterfaceMacro.Pb_DeviceIDType.Pb_DEVICE_MEET_PHPCLIENT.getNumber();
    //会议一键同屏设备
    public static int DEVICE_MEET_SHARE = InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetShare.getNumber();
    /**
     * 大数 设备与该值相与再比较
     * eg:判断是不是会议一键同屏设备： (devId & DEVICE_MEET_ID_MASK) == DEVICE_MEET_SHARE
     */
    public static int DEVICE_MEET_ID_MASK = 0xfffc0000;


    //  大类
    /**
     * 音频
     */
    public static int MEDIA_FILE_TYPE_AUDIO = 0x00000000;
    /**
     * 视频
     */
    public static int MEDIA_FILE_TYPE_VIDEO = 0x20000000;
    /**
     * 录制
     */
    public static int MEDIA_FILE_TYPE_RECORD = 0x40000000;
    /**
     * 图片
     */
    public static int MEDIA_FILE_TYPE_PICTURE = 0x60000000;
    /**
     * 升级
     */
    public static int MEDIA_FILE_TYPE_UPDATE = 0xe0000000;
    /**
     * 临时文件
     */
    public static int MEDIA_FILE_TYPE_TEMP = 0x80000000;
    /**
     * 其它文件
     */
    public static int MEDIA_FILE_TYPE_OTHER = 0xa0000000;
    public static int MAIN_TYPE_BITMASK = 0xe0000000;

    //  小类
    /**
     * PCM文件
     */
    public static int MEDIA_FILE_TYPE_PCM = 0x01000000;
    /**
     * MP3文件
     */
    public static int MEDIA_FILE_TYPE_MP3 = 0x02000000;
    /**
     * WAV文件
     */
    public static int MEDIA_FILE_TYPE_ADPCM = 0x03000000;
    /**
     * FLAC文件
     */
    public static int MEDIA_FILE_TYPE_FLAC = 0x04000000;
    /**
     * MP4文件
     */
    public static int MEDIA_FILE_TYPE_MP4 = 0x07000000;
    /**
     * MKV文件
     */
    public static int MEDIA_FILE_TYPE_MKV = 0x08000000;
    /**
     * RMVB文件
     */
    public static int MEDIA_FILE_TYPE_RMVB = 0x09000000;
    /**
     * RM文件
     */
    public static int MEDIA_FILE_TYPE_RM = 0x0a000000;
    /**
     * AVI文件
     */
    public static int MEDIA_FILE_TYPE_AVI = 0x0b000000;
    /**
     * bmp文件
     */
    public static int MEDIA_FILE_TYPE_BMP = 0x0c000000;
    /**
     * jpeg文件
     */
    public static int MEDIA_FILE_TYPE_JPEG = 0x0d000000;
    /**
     * png文件
     */
    public static int MEDIA_FILE_TYPE_PNG = 0x0e000000;
    /**
     * 其它文件
     */
    public static int MEDIA_FILE_TYPE_OTHER_SUB = 0x10000000;

    public static int SUB_TYPE_BITMASK = 0x1f000000;


    //自定义 Fragment索引
    /**
     * 会议议程
     */
    public static final int PB_MEET_FUN_CODE_AGENDA_BULLETIN = 0;
    /**
     * 会议资料
     */
    public static final int PB_MEET_FUN_CODE_MATERIAL = 1;
    /**
     * 共享文件
     */
    public static final int PB_MEET_FUN_CODE_SHARED_FILE = 2;
    /**
     * 批注文件
     */
    public static final int PB_MEET_FUN_CODE_POSTIL = 3;
    /**
     * 会议交流
     */
    public static final int PB_MEET_FUN_CODE_MESSAGE = 4;
    /**
     * 视频直播
     */
    public static final int PB_MEET_FUN_CODE_VIDEO_STREAM = 5;
    /**
     * 电子白板
     */
    public static final int PB_MEET_FUN_CODE_WHITE_BOARD = 6;
    /**
     * 网页
     */
    public static final int PB_MEET_FUN_CODE_WEB_BROWSER = 7;
    /**
     * 投票
     */
    public static final int PB_MEET_FUN_CODE_VOTE = 8;
    /**
     * 签到座位
     */
    public static final int PB_MEET_FUN_CODE_SIGN_IN_RESULT = 9;
    /**
     * 外部文档
     */
    public static final int PB_MEET_FUN_CODE_DOCUMENT = 10;
    /**
     * 签到详情
     */
    public static final int PB_MEET_FUN_CODE_SIGN_IN_SEAT = 11;

    /**
     * 设备控制
     */
    public static final int PB_MEET_FUN_CODE_DEV_CONTROL = 12;
    /**
     * 摄像控制
     */
    public static final int PB_MEET_FUN_CODE_CAMERA_CONTROL = 13;
    /**
     * 投票管理
     */
    public static final int PB_MEET_FUN_CODE_VOTE_MANAGE = 14;
    /**
     * 选举管理
     */
    public static final int PB_MEET_FUN_CODE_ELECTORAL_MANAGE = 15;
    /**
     * 投票结果
     */
    public static final int PB_MEET_FUN_CODE_VOTE_RESULT = 16;
    /**
     * 选举结果
     */
    public static final int PB_MEET_FUN_CODE_ELECTORAL_RESULT = 17;
    /**
     * 屏幕管理
     */
    public static final int PB_MEET_FUN_CODE_SCREEN_MANAGE = 18;
    /**
     * 权限管理
     */
    public static final int PB_MEET_FUN_CODE_PERMISSION_MANAGE = 19;
    /**
     * 会议公告
     */
    public static final int PB_MEET_FUN_CODE_COMMUNIQUE = 20;
    /**
     * 打开后台
     */
    public static final int PB_MEET_FUN_CODE_OPEN_BACKGROUND = 21;


    //编码类型
    /**
     * VP8 video (i.e. video in .webm)
     */
    public static final String AMIME_VIDEO_VP8 = "video/x-vnd.on2.vp8";
    /**
     * VP9 video (i.e. video in .webm)
     */
    public static final String AMIME_VIDEO_VP9 = "video/x-vnd.on2.vp9";
    /**
     * SCREEN_HEIGHT.264/AVC video
     */
    public static final String AMIME_VIDEO_AVC = "video/avc";
    /**
     * SCREEN_HEIGHT.265/HEVC video
     */
    public static final String AMIME_VIDEO_HEVC = "video/hevc";
    /**
     * MPEG4 video
     */
    public static final String AMIME_VIDEO_MPEG4 = "video/mp4v-es";

    /**
     *
     */
    public static final String AMIME_VIDEO_MPEG2 = "video/mpeg2";
    /**
     * SCREEN_HEIGHT.263 video
     */
    public static final String AMIME_VIDEO_H263 = "video/3gpp";
    /**
     * AMR narrowband audio
     */
    public static final String AMIME_AUDIO_AMR_NB = "audio/3gpp";

    /**
     * AMR wideband audio
     */
    public static final String AMIME_AUDIO_AMR_WB = "audio/amr-wb";
    /**
     * MPEG1/2 audio layer III
     */
    public static final String AMIME_AUDIO_MP3 = "audio/mpeg";
    /**
     * AAC audio(note,this is raw AAC packets,not packaged in LATM!)
     */
    public static final String AMIME_AUDIO_RAW_AAC = "audio/mp4a-latm";
    /**
     * vorbis audio
     */
    public static final String AMIME_AUDIO_VORBIS = "audio/vorbis";
    /**
     * G.711 alaw audio
     */
    public static final String AMIME_AUDIO_G711_ALAW = "audio/g711-alaw";
    /**
     * G.711 ulaw audio
     */
    public static final String AMIME_AUDIO_G711_MLAW = "audio/g711-mlaw";


    /**
     * 获取指定的MimeType
     *
     * @param codecId 后台回调ID
     * @return MimeType
     * codecid 解码器ID ffmpeg3.2 (h264=28,h265=174,mpeg4=13,vp8=140,vp9=168)
     * ffmpeg 4.0 later(h264=27,h265=173,mpeg4=12,vp8=139,vp9=167)
     */
    public static String getMimeType(int codecId) {
        switch (codecId) {
            case 2:
                return Macro.AMIME_VIDEO_MPEG2;
            case 12:
            case 13:
                return Macro.AMIME_VIDEO_MPEG4;
            case 139:
            case 140:
                return Macro.AMIME_VIDEO_VP8;
            case 167:
            case 168:
                return Macro.AMIME_VIDEO_VP9;
            case 173:
            case 174:
                return Macro.AMIME_VIDEO_HEVC;
            case 27:
            case 28:
            default:
                return Macro.AMIME_VIDEO_AVC;
        }
    }
}
