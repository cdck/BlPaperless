package com.pa.paperless.data.constant;

/**
 * @author by xlk
 * @date 2020/8/12 14:16
 * @desc
 */
public class EventType {
    private static final int BASE_EVENT_TYPE = 100000;
    //会议信息变更通知
    public static final int MEETINFO_CHANGE_INFORM = BASE_EVENT_TYPE + 1;
    //平台初始化完毕
    public static final int PLATFORM_INITIALIZATION = BASE_EVENT_TYPE + 2;
    //设备寄存器变更通知
    public static final int DEV_REGISTER_INFORM = BASE_EVENT_TYPE + 3;
    //参会人员变更通知
    public static final int MEMBER_CHANGE_INFORM = BASE_EVENT_TYPE + 4;
    //设备会议信息变更通知
    public static final int DEVMEETINFO_CHANGE_INFORM = BASE_EVENT_TYPE + 5;
    //会场信息变更通知
    public static final int PLACEINFO_CHANGE_INFORM = BASE_EVENT_TYPE + 6;
    //会场设备信息变更通知
    public static final int PLACE_DEVINFO_CHANGEINFORM = BASE_EVENT_TYPE + 7;
    //管理员变更通知
    public static final int ADMIN_NOTIFI_INFORM = BASE_EVENT_TYPE + 8;
    //会议管理员控制的会场变更通知
    public static final int MEETADMIN_PLACE_NOTIFIINFROM = BASE_EVENT_TYPE + 9;
    //常用人员变更通知
    public static final int QUERY_COMMON_PEOPLE = BASE_EVENT_TYPE + 10;
    //登录返回
    public static final int LOGIN_BACK = BASE_EVENT_TYPE + 11;
    //会议界面时间回调
    public static final int MEET_DATE = BASE_EVENT_TYPE + 12;
    //界面状态变更通知
    public static final int FACESTATUS_CHANGE_INFORM = BASE_EVENT_TYPE + 13;
    //辅助签到变更通知
    public static final int SIGN_EVENT = BASE_EVENT_TYPE + 14;
    //签到变更通知
    public static final int SIGN_CHANGE_INFORM = BASE_EVENT_TYPE + 15;
    //收到打开白板操作
    public static final int OPEN_BOARD = BASE_EVENT_TYPE + 16;
    //下载进度回调 -- 下载完成
    public static final int DOWNLOAD_PROGRESS = BASE_EVENT_TYPE + 17;
    //会议目录文件变更通知
    public static final int MEETDIR_FILE_CHANGE_INFORM = BASE_EVENT_TYPE + 18;
    //网页变更通知
    public static final int NETWEB_INFORM = BASE_EVENT_TYPE + 19;
    //媒体播放通知
    public static final int MEDIA_PLAY_INFORM = BASE_EVENT_TYPE + 20;
    //会议排位变更通知
    public static final int MeetSeat_Change_Inform = BASE_EVENT_TYPE + 21;
    //停止播放
    public static final int STOP_PLAY = BASE_EVENT_TYPE + 22;
    //投票提交人变更通知
    public static final int VoteMember_ChangeInform = BASE_EVENT_TYPE + 23;
    //投票变更通知
    public static final int Vote_Change_Inform = BASE_EVENT_TYPE + 24;
    //有新的投票发起通知
    public static final int newVote_launch_inform = BASE_EVENT_TYPE + 25;
    //上传进度通知 高频回调
    public static final int Upload_Progress = BASE_EVENT_TYPE + 26;
    //播放进度通知
    public static final int PLAY_PROGRESS_NOTIFY = BASE_EVENT_TYPE + 27;
    //流播放通知
    public static final int PLAY_STREAM_NOTIFY = BASE_EVENT_TYPE + 28;
    //采集流通知
    public static final int START_COLLECTION_STREAM_NOTIFY = BASE_EVENT_TYPE + 29;
    //停止采集流通知
    public static final int STOP_COLLECTION_STREAM_NOTIFY = BASE_EVENT_TYPE + 30;
    //会议视频变更通知
    public static final int Meet_vedio_changeInform = BASE_EVENT_TYPE + 31;
    //添加绘画通知
    public static final int ADD_DRAW_INFORM = BASE_EVENT_TYPE + 32;
    //同意加入通知
    public static final int AGREED_JOIN = BASE_EVENT_TYPE + 33;
    //拒绝加入通知
    public static final int REJECT_JOIN = BASE_EVENT_TYPE + 34;
    //参会人员退出白板通知
    public static final int EXIT_WHITE_BOARD = BASE_EVENT_TYPE + 35;
    //添加文本通知
    public static final int ADD_DRAW_TEXT = BASE_EVENT_TYPE + 36;
    //添加图片通知
    public static final int ADD_PIC_INFORM = BASE_EVENT_TYPE + 37;
    //添加墨迹通知
    public static final int ADD_INK_INFORM = BASE_EVENT_TYPE + 38;
    //白板删除记录通知
    public static final int WHITEBROADE_DELETE_RECOREINFORM = BASE_EVENT_TYPE + 39;
    //白板清空记录通知
    public static final int WHITEBOARD_EMPTY_RECORDINFORM = BASE_EVENT_TYPE + 40;
    //会议功能变更通知
    public static final int MEET_FUNCTION_CHANGEINFO = BASE_EVENT_TYPE + 41;
    //议程变更通知
    public static final int AGENDA_CHANGE_INFO = BASE_EVENT_TYPE + 42;
    //公告变更通知
    public static final int NOTICE_CHANGE_INFO = BASE_EVENT_TYPE + 43;
    //在视屏直播页面通知会议界面打开同屏控制
    public static final int OPEN_SCREENSPOP = BASE_EVENT_TYPE + 44;
    //在视屏直播界面通知会议界面打开投影控
    public static final int OPEN_PROJECTOR = BASE_EVENT_TYPE + 45;
    //停止资源通知
    public static final int STOP_STRAM_INFORM = BASE_EVENT_TYPE + 46;
    //参会人权限变更通知
    public static final int MEMBER_PERMISSION_INFORM = BASE_EVENT_TYPE + 47;
    //收到YUV格式解码数据
    public static final int CALLBACK_YUVDISPLAY = BASE_EVENT_TYPE + 48;
    //收到解码数据
    public static final int CALLBACK_VIDEO_DECODE = BASE_EVENT_TYPE + 49;
    //开始播放
    public static final int CALLBACK_STARTDISPLAY = BASE_EVENT_TYPE + 50;
    //停止播放
    public static final int CALLBACK_STOPDISPLAY = BASE_EVENT_TYPE + 51;
    //截取播放中画面
    public static final int CUT_VIDEO_IMAGE = BASE_EVENT_TYPE + 52;
    //发送注册/注销WPS广播
    public static final int WPS_BROAD_CASE_INFORM = BASE_EVENT_TYPE + 53;
    //停止公告通知
    public static final int CLOSE_NOTICE_INFORM = BASE_EVENT_TYPE + 54;
    //发送通知打开计时控件
    public static final int SEND_SCREEN_TIME = BASE_EVENT_TYPE + 55;
    //网络变更广播时发送
    public static final int NETWORK_CHANGE = BASE_EVENT_TYPE + 56;
    //收到强制性播放流通知
    public static final int MANDATORY_PLAY = BASE_EVENT_TYPE + 57;
    //网络检查时已经初始化完成了
    public static final int BUS_MAINSTART = BASE_EVENT_TYPE + 58;
    //平台初始化失败
    public static final int platform_initialization_failed = BASE_EVENT_TYPE + 59;
    // 在WebView正在展示时 按返回键则发送回到上一个网页
    public static final int go_back_html = BASE_EVENT_TYPE + 60;
    // 发送通知打开图片
    public static final int open_picture = BASE_EVENT_TYPE + 61;
    // 使用WPS编辑后，点击保存时发送消息上传到批注服务器列表中
    public static final int updata_to_postil = BASE_EVENT_TYPE + 62;
    //收到参会人员数量和签到数量广播通知
    public static final int signin_count = BASE_EVENT_TYPE + 63;
    // 界面配置变更通知
    public static final int ICC_changed_inform = BASE_EVENT_TYPE + 64;
    // 拍摄的照片
    public static final int take_photo = BASE_EVENT_TYPE + 65;
    // 会议笔记导入文本状态
    public static final int is_loading = BASE_EVENT_TYPE + 66;
    //会议交流信息
    public static final int meet_chat_info = BASE_EVENT_TYPE + 67;
    //文件导出成功
    public static final int export_finish = BASE_EVENT_TYPE + 68;
    //视屏截图
    public static final int shot_video = BASE_EVENT_TYPE + 69;
    //共享中收到的图片
    public static final int is_share_pic = BASE_EVENT_TYPE + 70;
    public static final int seetbar_progress = BASE_EVENT_TYPE + 71;
    //HOME键点击监听
    public static final int click_key_home = BASE_EVENT_TYPE + 72;
    //设备控制通知
    public static final int DEVICE_CONTROL_INFORM = BASE_EVENT_TYPE + 73;
    public static final int CHANGE_LOGO_IMG = BASE_EVENT_TYPE + 74;
    public static final int publish_notice = BASE_EVENT_TYPE + 75;
    //发送主页背景变更
    public static final int CHANGE_MAIN_BG = BASE_EVENT_TYPE + 76;
    //会场设备排位详细信息变更通知
    public static final int SIGNIN_SEAT_INFORM = BASE_EVENT_TYPE + 77;
    //签到图形
    public static final int SIGNIN_SEAT_FRAG = BASE_EVENT_TYPE + 78;
    //打开签到详情
    public static final int SIGNIN_DETAILS = BASE_EVENT_TYPE + 79;
    //子界面背景下载完成
    public static final int SUB_BG_PNG_IMG = BASE_EVENT_TYPE + 80;
    //收到别人请求权限
    public static final int RECEIVE_PERMISSION_REQUEST = BASE_EVENT_TYPE + 81;
    //收到请求回复
    public static final int RECEIVE_REQUEST_REPLY = BASE_EVENT_TYPE + 82;
    //收到文件推送通知
    public static final int PUSH_FILE_INFORM = BASE_EVENT_TYPE + 83;
    //通知fab开启文件推送
    public static final int INFORM_PUSH_FILE = BASE_EVENT_TYPE + 84;
    //签到结果返回 是否成功
    public static final int SIGNIN_BACK = BASE_EVENT_TYPE + 85;
    //投票录入文件导出成功
    public static final int EXPORT_VOTEENTRY_FINISH = BASE_EVENT_TYPE + 86;
    //更新程序通知
    public static final int update_client = BASE_EVENT_TYPE + 87;
    //公告logo图标下载完成
    public static final int NOTICE_LOGO_PNG_TAG = BASE_EVENT_TYPE + 88;
    //公告背景图片下载完成
    public static final int NOTICE_BG_PNG_TAG = BASE_EVENT_TYPE + 89;
    //会场底图图片下载完成
    public static final int ROOM_BG_PIC_ID = BASE_EVENT_TYPE + 90;
    //会议议程文件下载完成
    public static final int MEETING_AGENDA_FILE = BASE_EVENT_TYPE + 91;
    //腾讯x5内核安装完成
    public static final int init_x5_finished = BASE_EVENT_TYPE + 92;
    //会议目录变更通知
    public static final int MEETDIR_CHANGE_INFORM = BASE_EVENT_TYPE + 93;
}
