package com.pa.paperless.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceDownload;
import com.mogujie.tt.protobuf.InterfaceIM;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePlaymedia;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceStream;
import com.mogujie.tt.protobuf.InterfaceUpload;
import com.mogujie.tt.protobuf.InterfaceWhiteboard;
import com.pa.boling.paperless.R;
import com.pa.paperless.activity.DrawBoardActivity;
import com.pa.paperless.activity.MeetingActivity;
import com.pa.paperless.data.bean.PictureInfo;
import com.pa.paperless.data.bean.ReceiveMeetIMInfo;
import com.pa.paperless.broadcase.WpsBroadCastReciver;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.data.constant.WpsModel;
import com.pa.paperless.utils.Dispose;
import com.pa.paperless.utils.LogUtil;

import com.pa.paperless.video.VideoActivity;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.MyUtils;
import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import cc.shinichi.library.ImagePreview;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.pa.paperless.activity.DrawBoardActivity.disposePicOpermemberid;
import static com.pa.paperless.activity.DrawBoardActivity.disposePicSrcmemid;
import static com.pa.paperless.activity.DrawBoardActivity.disposePicSrcwbidd;
import static com.pa.paperless.activity.DrawBoardActivity.sharing;
import static com.pa.paperless.activity.DrawBoardActivity.newName;
import static com.pa.paperless.activity.MeetingActivity.chatisshowing;
import static com.pa.paperless.activity.MeetingActivity.mBadge;
import static com.pa.paperless.data.constant.Macro.CACHE_ALL_FILE;
import static com.pa.paperless.data.constant.Values.mPermissionsList;
import static com.pa.paperless.data.constant.Values.videoIsShowing;
import static com.pa.paperless.fragment.SharedFileFragment.shareFileName;
import static com.pa.paperless.service.FabService.FabPicName;
import static com.pa.paperless.utils.MyUtils.getMediaid;
import static com.pa.paperless.utils.MyUtils.isHasPermission;
import static com.pa.paperless.data.constant.Values.localDevId;
import static com.pa.paperless.data.constant.Values.localMemberId;
import static com.pa.paperless.data.constant.Values.localMemberName;

/**
 * Created by xlk
 * on 2018/6/12.
 */
public class NativeService extends Service {
    private final String TAG = "NativeService-->";
    private NativeUtil jni = NativeUtil.getInstance();
    private File upLoadPostilFile;//复制的临时文件
    private WpsBroadCastReciver reciver;
    public static boolean haveNewPlayInform = false;
    public static List<String> piclist = new ArrayList<>();
    public static ArrayList<PictureInfo> pictureInfos = new ArrayList<>();

    @Override
    public void onDestroy() {
        App.nativeServiceIsOpened = false;
        LogUtil.i(TAG, "NativeService.onDestroy " + this);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        LogUtil.i(TAG, "NativeService.onCreate " + this);
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "NativeService.onStartCommand " + this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.i(TAG, "NativeService.onBind " + this);
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.i(TAG, "NativeService.onUnbind " + this);
        return super.onUnbind(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.meet_chat_info://收到会议交流信息
                receiveMeetChatInfo(message);
                break;
            //文件导出成功
            case EventType.export_finish:
                Object[] objects1 = message.getObjects();
                File file = (File) objects1[0];
                String modeStr = (String) objects1[1];
                if (!modeStr.equals("cache")) {
                    String absolutePath = file.getParentFile().getAbsolutePath();
                    ToastUtils.showShort(R.string.tip_export_success, absolutePath);
                } else {
                    String name = file.getName();
                    LogUtil.i(TAG, "会议笔记导出成功 -->  路径： " + file.getAbsolutePath() + ",  name= " + name);
                    if (!name.equals("tempNote.txt")) {//过滤掉临时文件
                        jni.addLocalFile2Cache("会议笔记", file.getAbsolutePath());
                    }
                }
                break;
            case EventType.SIGNIN_SEAT_INFORM://会场设备信息变更通知
                queryAdmins();
                break;
            //参会人权限变更通知
            case EventType.MEMBER_PERMISSION_INFORM:
                InterfaceMember.pbui_Type_MemberPermission o = jni.queryAttendPeoplePermissions();
                mPermissionsList.clear();
                mPermissionsList.addAll(o.getItemList());
                for (int i = 0; i < mPermissionsList.size(); i++) {
                    InterfaceMember.pbui_Item_MemberPermission item = mPermissionsList.get(i);
                    if (item.getMemberid() == localMemberId) {
                        Values.localPermission = item.getPermission();
                        LogUtils.i(TAG, "getEventMessage 本机的权限=" + Values.localPermission
                                + "\n是否有同屏权限=" + isHasPermission(Macro.permission_code_screen)
                                + "\n是否有投影权限=" + isHasPermission(Macro.permission_code_projection)
                                + "\n是否有上传权限=" + isHasPermission(Macro.permission_code_upload)
                                + "\n是否有下载权限=" + isHasPermission(Macro.permission_code_download)
                                + "\n是否有投票权限=" + isHasPermission(Macro.permission_code_vote)
                        );
                    }
                }
                break;
            //收到WPS编辑完成后的广播
            case EventType.updata_to_postil:
                reWpsFinishInform(message);
                break;
            //上传进度通知
            case EventType.Upload_Progress:
                uploadInform(message);
                break;
            //下载进度回调
            case EventType.DOWNLOAD_PROGRESS:
                downloadInform(message);
                break;
            //媒体播放通知
            case EventType.MEDIA_PLAY_INFORM:
                receiveMediaPlayInform(message);
                break;
            //收到流播放通知
            case EventType.PLAY_STREAM_NOTIFY:
                receivePlayStreamInform(message);
                break;
            //添加图片通知
            case EventType.ADD_PIC_INFORM:
                receiveAddPic(message);
                break;
            //设备控制变更通知
            case EventType.DEVICE_CONTROL_INFORM:
                deviceControl(message);
                break;
            //会议排位变更通知
            case EventType.MeetSeat_Change_Inform:
                fun_queryMeetRanking(message);
                break;
            //设备会议信息变更通知
            case EventType.DEVMEETINFO_CHANGE_INFORM:
                fun_queryDevMeetInfo();
                break;
            case EventType.WPS_BROAD_CASE_INFORM:
                boolean register = (boolean) message.getObject();
                LogUtil.i(TAG, "收到是否开启WPS广播:" + register);
                if (register) {
                    registerWpsBroadCase();
                } else {
                    unregisterWpsBroadCase();
                }
                break;
            case EventType.open_picture://收到打开图片
                try {
                    Object[] objects = message.getObjects();
                    String filepath = (String) objects[0];
                    int mediaid = (int) objects[1];
                    addPictureInfo(new PictureInfo(mediaid, filepath));
                    int index = piclist.indexOf(filepath);
                    System.out.println("打开的图片 -->index= " + index + ",mediaid=" + mediaid);
                    if (index != -1) {
                        photoViewer(piclist, index);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case EventType.MEETDIR_CHANGE_INFORM://会议目录变更通知

                break;
            case EventType.MEETDIR_FILE_CHANGE_INFORM://会议目录文件变更通知
                cacheAllFile(message);
                break;
            default:
                break;
        }
    }

    private void cacheAllFile(EventMessage message) {
        InterfaceBase.pbui_MeetNotifyMsgForDouble object = (InterfaceBase.pbui_MeetNotifyMsgForDouble) message.getObject();
        int id = object.getId();
        int subid = object.getSubid();
        int opermethod = object.getOpermethod();
        LogUtil.i(TAG, "cacheAllFile 会议目录文件变更通知 id=" + id + ",subid=" + subid + ",opermethod=" + opermethod);
        if (id == Macro.ANNOTATION_FILE_DIRECTORY_ID) {
            LogUtil.e(TAG, "cacheAllFile 不缓存批注目录下的文件");
            return;
        }
//        if (opermethod != 2) {
//            LogUtil.e(TAG, "cacheAllFile 只会缓存上传的文件");
//            return;
//        }
        LogUtil.i(TAG, "cacheAllFile 缓存所有的文档和图片文件");
        FileUtil.cacheDirFile(id);
    }

    /**
     * 打开查看图片
     *
     * @param piclist 图片的路径集合
     */
    private void photoViewer(List<String> piclist, int index) {
        //使用方式
//        PictureConfig config = new PictureConfig.Builder()
//                .setListData((ArrayList<String>) piclist)	//图片数据List<String> list
//                .setPosition(index)	//图片下标（从第position张图片开始浏览）
////                .setDownloadPath("pictureviewer")	//图片下载文件夹地址
//                .setIsShowNumber(true)//是否显示数字下标
////                .needDownload(true)	//是否支持图片下载
////                .setPlacrHolder(R.mipmap.icon)	//占位符图片（图片加载完成前显示的资源图片，来源drawable或者mipmap）
//                .build();
//        ImagePagerActivity.startActivity(this, config);
        ImagePreview.getInstance()
                .setContext(App.currentActivity())
                .setImageList(piclist)//设置图片地址集合
                .setIndex(index)//设置开始的索引
                .setShowDownButton(false)//设置是否显示下载按钮
//                .setShowCloseButton(false)//设置是否显示关闭按钮
//                .setEnableDragClose(true)//设置是否开启下拉图片退出
//                .setEnableUpDragClose(true)//设置是否开启上拉图片退出
//                .setEnableClickClose(true)//设置是否开启点击图片退出
                .setShowErrorToast(true)
                .start();
    }

    private void queryAdmins() {
//        InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo object = jni.placeDeviceRankingInfo(NativeService.roomId);
//        if (object == null) return;
//        filterMembers.clear();
//        List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> itemList = object.getItemList();
//        for (InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item : itemList) {
//            int role = item.getRole();
//            if (role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE) {
//                LogUtil.i(TAG, "queryAdmins 秘书：" + item.getMemberid() + ", 名字：" + item.getMembername().toStringUtf8());
//                filterMembers.put(item.getMemberid(), item);
//            }
//        }
    }

    private void receiveMeetChatInfo(EventMessage message) {
        LogUtil.d(TAG, "receiveMeetChatInfo: ");
        if (!chatisshowing) {//确保会议交流界面不在显示状态
            ToastUtils.showShort(R.string.new_message);
            InterfaceIM.pbui_Type_MeetIM receiveMsg = (InterfaceIM.pbui_Type_MeetIM) message.getObject();
            //获取之前的未读消息个数
            int badgeNumber1 = mBadge.getBadgeNumber();
            LogUtil.e(TAG, "receiveMeetChatInfo :  原来的个数 --> " + badgeNumber1);
            if (receiveMsg != null) {
                if (receiveMsg.getMsgtype() == 0) {//证明是文本类消息
                    int all = badgeNumber1 + 1;
                    List<ReceiveMeetIMInfo> receiveMeetIMInfos = Dispose.ReceiveMeetIMinfo(receiveMsg);
                    receiveMeetIMInfos.get(0).setType(true);//设置是接收的消息
                    MeetingActivity.mReceiveMsg.add(receiveMeetIMInfos.get(0));
                    LogUtil.e(TAG, "receiveMeetChatInfo :  收到的信息个数 --> " + MeetingActivity.mReceiveMsg.size());
//                    List<EventBadge> num = new ArrayList<>();
//                    num.add(new EventBadge(all));
                    LogUtil.e(TAG, "receiveMeetChatInfo :  传递的总未读消息个数 --> " + all);
                    mBadge.setBadgeNumber(all);
                }
            }
        }
    }

    public void registerWpsBroadCase() {
        if (reciver == null) {
            reciver = new WpsBroadCastReciver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(WpsModel.Reciver.ACTION_SAVE);
            filter.addAction(WpsModel.Reciver.ACTION_CLOSE);
            filter.addAction(WpsModel.Reciver.ACTION_HOME);
//            filter.addAction(WpsModel.Reciver.ACTION_BACK);
            registerReceiver(reciver, filter);
        }
    }

    public void unregisterWpsBroadCase() {
        if (reciver != null) {
            unregisterReceiver(reciver);
            reciver = null;
        }
    }

    private void fun_queryDevMeetInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceFaceShowDetail info = jni.queryDeviceMeetInfo();
            if (info == null) return;
            localMemberId = info.getMemberid();
            LogUtils.i(TAG, "fun_queryDevMeetInfo 设置本机参会人ID" + localMemberId);
            localDevId = info.getDeviceid();
            localMemberName = MyUtils.b2s(info.getMembername());
            Values.roomId = info.getRoomid();
            Values.meetingName = MyUtils.b2s(info.getMeetingname());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryMeetRanking(EventMessage message) {
        InterfaceBase.pbui_MeetNotifyMsg object = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
        if (object == null) return;

        int opermethod = object.getOpermethod();
        int id = object.getId();
        if (opermethod == 3) {

        }
        try {
            InterfaceRoom.pbui_Type_MeetSeatDetailInfo meetSeat = jni.queryMeetRanking();
            if (meetSeat == null) return;
            List<InterfaceRoom.pbui_Item_MeetSeatDetailInfo> itemList = meetSeat.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceRoom.pbui_Item_MeetSeatDetailInfo info = itemList.get(i);
                int nameId = info.getNameId();
                int role = info.getRole();
                if (Values.localMemberId == nameId) {
                    //本机是否有秘书管理的权限
                    if (role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere.getNumber()
                            || role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary.getNumber()
                            || role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin.getNumber()) {
                        Values.hasAllPermissions = true;
                    } else {
                        Values.hasAllPermissions = false;
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void deviceControl(EventMessage message) {
        InterfaceDevice.pbui_Type_DeviceControl object = (InterfaceDevice.pbui_Type_DeviceControl) message.getObject();
        int oper = object.getOper();//enum Pb_DeviceControlFlag
        int operval1 = object.getOperval1();//操作对应的参数 如更换主界面的媒体ID
        int operval2 = object.getOperval2();//操作对应的参数
        LogUtil.i(TAG, "设备控制变更通知 oper:" + oper + ", operval1:" + operval1 + ", operval2:" + operval2);
        if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYLOGO.getNumber()) {
            LogUtil.d(TAG, "deviceControl: 更换Logo通知");
            //本地没有才下载
//            if (!SpHelper.checkCurrentMediaId(this, operval1, 3)) {//logo的faceID固定是3
            FileUtil.createDir(Macro.ROOT);
            jni.creationFileDownload(Macro.ROOT + Macro.DOWNLOAD_MAIN_LOGO + ".png", operval1, 1, 0, Macro.DOWNLOAD_MAIN_LOGO);
//            }
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_SHUTDOWN.getNumber()) {//关机
            LogUtil.d(TAG, "deviceControl: 关机");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_REBOOT.getNumber()) {//重启
            LogUtil.d(TAG, "deviceControl: 重启");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_PROGRAMRESTART.getNumber()) {//重启软件
            LogUtil.d(TAG, "deviceControl: 重启软件");
            App.lbm.sendBroadcast(new Intent("relaunchApp"));
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTUP.getNumber()) {//升
            LogUtil.d(TAG, "deviceControl: 升");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTDOWN.getNumber()) {//降
            LogUtil.d(TAG, "deviceControl: 降");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTSTOP.getNumber()) {//停止升（降）
            LogUtil.d(TAG, "deviceControl: 停止升(降)");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYMAINBG.getNumber()) {//更换主界面
            LogUtil.d(TAG, "deviceControl: 更换主界面");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYPROJECTBG.getNumber()) {//更换投影界面
            LogUtil.d(TAG, "deviceControl: 更换投影界面");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYSUBBG.getNumber()) {//更换子界面
            LogUtil.d(TAG, "deviceControl: 更换子界面");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYFONTCOLOR.getNumber()) {//更换字体颜色
            LogUtil.d(TAG, "deviceControl: 更换字体颜色");
        }
    }

    private void receiveAddPic(EventMessage message) {
        InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail object = (InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail) message.getObject();
        LogUtil.v(TAG, "receiveAddPic :  收到添加图片操作 ");
        int rPicSrcmemid = object.getSrcmemid();
        long rPicSrcwbid = object.getSrcwbid();
        ByteString rPicData = object.getPicdata();
        int opermemberid = object.getOpermemberid();
        Values.operid = object.getOperid();
        if (!sharing) {
            if (disposePicOpermemberid == opermemberid && disposePicSrcmemid == rPicSrcmemid
                    && disposePicSrcwbidd == rPicSrcwbid) {
                DrawBoardActivity.tempPicData = rPicData;
                disposePicOpermemberid = 0;
                disposePicSrcmemid = 0;
                disposePicSrcwbidd = 0;
            }
            return;
        } else {
            EventBus.getDefault().post(new EventMessage(EventType.is_share_pic, object));
        }
    }

    private void reWpsFinishInform(EventMessage message) {
        String path = (String) message.getObject();
        //获取要上传的文件
        upLoadPostilFile = new File(path);
        int mediaid = getMediaid(path);
        String fileName = upLoadPostilFile.getName();
        LogUtil.v(TAG, "NativeService.reWpsFinishInform :  收到WPS编辑完成后的文件路径 --> " + path + ",  fileName: " + fileName);
        /** **** **  将编辑保存完的文件上传到批注文档目录  ** **** **/
        jni.uploadFile(InterfaceMacro.Pb_Upload_Flag.Pb_MEET_UPLOADFLAG_ONLYENDCALLBACK.getNumber(),
                2, 0, fileName, path, 0, Macro.upload_wps_file);
    }

    private void uploadInform(EventMessage message) throws InvalidProtocolBufferException {
        InterfaceUpload.pbui_TypeUploadPosCb object = (InterfaceUpload.pbui_TypeUploadPosCb) message.getObject();
        int mediaId = object.getMediaId();
        int per = object.getPer();
        String userStr = object.getUserstr().toStringUtf8();
        byte[] bytes = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaId);
        InterfaceBase.pbui_CommonTextProperty pbui_commonTextProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
        String uploadFileName = MyUtils.b2s(pbui_commonTextProperty.getPropertyval());
        String name = FileUtil.getCutStr(uploadFileName, 1);//获取当前上传文件的前缀文件名
        if (newName != null && newName.equals(name)) {
            LogUtil.v(TAG, "uploadInform: 当前上传的是画板图片...");
        } else if (FabPicName != null && FabPicName.equals(name)) {
            LogUtil.v(TAG, "uploadInform: 当前上传的是截图图片...");
        } else if (shareFileName != null && shareFileName.equals(name)) {
            LogUtil.v(TAG, "uploadInform: 当前上传的是共享文件...");
        }
        if (per < 100) {
            LogUtil.v(TAG, "uploadInform: " + uploadFileName + " 上传进度:" + per);
        }
        if (per == 100) {
            if (FabPicName != null && FabPicName.equals(name)) {
                LogUtil.v(TAG, "uploadInform: 截图图片上传完毕...");
                Timer t = new Timer();
                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {
                        if (FabService.FabPicFile != null && FabService.FabPicFile.exists()) {
                            LogUtil.d(TAG, "uploadInform run: 删除文件..");
                            FabService.FabPicFile.delete();
                            FabService.FabPicFile = null;
                            cancel();
                            t.cancel();
                        }
                    }
                };
                t.schedule(tt, 1000);
            }
            if (upLoadPostilFile != null && upLoadPostilFile.exists()) {
                LogUtil.d(TAG, "uploadInform: " + uploadFileName + " 上传完成,进行删除..");
                upLoadPostilFile.delete();//删除临时文件
                upLoadPostilFile = null;//重新置空
            }
        }
    }

    private void downloadInform(EventMessage message) {
        InterfaceDownload.pbui_Type_DownloadCb object5 = (InterfaceDownload.pbui_Type_DownloadCb) message.getObject();
        int mediaid = object5.getMediaid();
        int progress = object5.getProgress();
        int nstate = object5.getNstate();
        String filepath = MyUtils.b2s(object5.getPathname());
        String s = filepath.substring(filepath.lastIndexOf("/") + 1).toLowerCase();
        String Userstr = MyUtils.b2s(object5.getUserstr());
        boolean canToast = !Userstr.equals(Macro.DOWNLOAD_MAIN_BG) &&
                !Userstr.equals(Macro.DOWNLOAD_MAIN_LOGO) &&
                !Userstr.equals(Macro.DOWNLOAD_SUB_BG) &&
                !Userstr.equals(Macro.DOWNLOAD_NOTICE_LOGO) &&
                !Userstr.equals(Macro.DOWNLOAD_NOTICE_BG) &&
                !Userstr.equals(Macro.DOWNLOAD_ROOM_BG) &&
                !Userstr.equals(Macro.DOWNLOAD_CACHE_FILE);
        if (nstate == 1) {
            LogUtil.v(TAG, "downloadInform: " + getString(R.string.download_progress, s, progress + "%"));
            if (canToast) {
                ToastUtils.showShort(R.string.download_progress, s, progress + "%");
            }
        } else if (nstate == 2) {
        } else if (nstate == 3) {
        } else if (nstate == 4) {//下载退出 ---不管成功与否,下载结束最后一次的状态都是这个
            File f = new File(filepath);
            LogUtil.i(TAG, "downloadInform: 下载完成:" + filepath);
            if (f.exists()) {
                if (canToast) {
                    ToastUtils.showShort(R.string.download_finsh, f.getName());
                }
                if (Userstr.equals(Macro.DOWNLOAD_CACHE_FILE)
                        || Userstr.equals(Macro.DOWNLOAD_FILE)) {
                    if (FileUtil.isPicture(s)) {
                        PictureInfo pictureInfo = new PictureInfo(mediaid, f.getAbsolutePath());
                        addPictureInfo(pictureInfo);
                    }
                }
                LogUtil.i(TAG, "downloadInform: 下载完成: " + f.getName() + ", 媒体ID: " + mediaid);
                if (Userstr.equals(Macro.DOWNLOAD_MAIN_BG)) {//主页背景图下载完成
                    EventBus.getDefault().post(new EventMessage(EventType.CHANGE_MAIN_BG, filepath));
                } else if (Userstr.equals(Macro.DOWNLOAD_MAIN_LOGO)) {//logo图标下载完成
                    EventBus.getDefault().post(new EventMessage(EventType.CHANGE_LOGO_IMG, filepath));
                } else if (Userstr.equals(Macro.DOWNLOAD_SUB_BG)) {//子界面背景下载完成
                    EventBus.getDefault().post(new EventMessage(EventType.SUB_BG_PNG_IMG, filepath));
                } else if (Userstr.equals(Macro.DOWNLOAD_NOTICE_LOGO)) {//公告logo图标下载完成
                    EventBus.getDefault().post(new EventMessage(EventType.NOTICE_LOGO_PNG_TAG, filepath));
                } else if (Userstr.equals(Macro.DOWNLOAD_NOTICE_BG)) {//公告背景图片下载完成
                    EventBus.getDefault().post(new EventMessage(EventType.NOTICE_BG_PNG_TAG, filepath));
                } else if (Userstr.equals(Macro.DOWNLOAD_ROOM_BG)) {//会场底图背景图片下载完成
                    EventBus.getDefault().post(new EventMessage(EventType.ROOM_BG_PIC_ID, filepath));
                } else if (Userstr.equals(Macro.DOWNLOAD_AGENDA_FILE)) {//会议议程文件下载完成
                    LogUtil.i(TAG, "会议议程文件下载完成 -->" + filepath);
                    EventBus.getDefault().post(new EventMessage(EventType.MEETING_AGENDA_FILE, filepath));
                } else if (Userstr.equals(Macro.DOWNLOAD_PUSH_FILE + mediaid)) {
                    LogUtil.i(TAG, "NativeService.downloadInform : " + s + " 推送文件下载完成 --> ");
                    Values.isOpenFile = true;
                } else if (Userstr.equals(Macro.DOWNLOAD_OFFLINE_MEETING)) {//下载到离线会议
//                    /storage/emulated/0/NETCONFIG/meetcache/2020年度第一次会议/文件/会议aPI.pdf
                    LogUtil.d(TAG, "缓存文件下载完成：" + filepath);
                } else if (Userstr.equals(Macro.DOWNLOAD_CACHE_FILE)) {
                    LogUtil.d(TAG, "提前下载好文件：" + filepath);
                }
                if (Values.isOpenFile) {
                    //下载完成如果用户点击的是查看按钮则进行打开操作
                    LogUtil.d(TAG, "downloadInform: 正在打开:" + f.getName());
                    Values.isOpenFile = false;//就算下载多个也只会打开一个文件
                    FileUtil.OpenThisFile(this, f, mediaid);
                }
            } else {
                LogUtil.e(TAG, "downloadInform: 文件不存在..");
            }
        }
    }

    public static void delPictureInfo(String filePath) {
        Iterator<PictureInfo> iterator = pictureInfos.iterator();
        while (iterator.hasNext()) {
            PictureInfo next = iterator.next();
            if (next.getFilePath().equals(filePath)) {
                LogUtil.i("test", "delPictureInfo 从集合中删除：" + filePath);
                iterator.remove();
            }
        }
        Collections.sort(pictureInfos);
        piclist.clear();
        for (int i = 0; i < pictureInfos.size(); i++) {
            piclist.add(pictureInfos.get(i).getFilePath());
        }
    }

    public static void addPictureInfo(PictureInfo pictureInfo) {
        if (!pictureInfos.contains(pictureInfo)) {
            LogUtil.i("test", "addPictureInfo 添加到集合中：" + pictureInfo.toString());
            pictureInfos.add(pictureInfo);
            Collections.sort(pictureInfos);
            piclist.clear();
            for (int i = 0; i < pictureInfos.size(); i++) {
                piclist.add(pictureInfos.get(i).getFilePath());
            }
        }
    }

    private void receiveMediaPlayInform(final EventMessage message) throws InvalidProtocolBufferException {
        InterfacePlaymedia.pbui_Type_MeetMediaPlay data = (InterfacePlaymedia.pbui_Type_MeetMediaPlay) message.getObject();
        int createdeviceid = data.getCreatedeviceid();
        int mediaid = data.getMediaid();
        int res = data.getRes();
        int triggeruserval = data.getTriggeruserval();
        int triggerid = data.getTriggerid();

        boolean isMandatory = (InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE & triggeruserval)
                == InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE;
        /* **** **  根据媒体ID计算出该文件的类型  ** **** */
        //大类
        int maintype = mediaid & Macro.MAIN_TYPE_BITMASK;
        //小类
        int subtype = mediaid & Macro.SUB_TYPE_BITMASK;
        //音频或视频
        if (maintype == Macro.MEDIA_FILE_TYPE_AUDIO || maintype == Macro.MEDIA_FILE_TYPE_VIDEO) {
            LogUtil.e(TAG, "receiveMediaPlayInform :  媒体播放通知 " + videoIsShowing);
            if (res != 0) {
                return;
            }
            //是否是强制性播放
            Values.isMandatory = isMandatory;
            haveNewPlayInform = true;
//            if (!videoIsShowing) {
            startActivity(new Intent(NativeService.this, VideoActivity.class)
                    .setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    .putExtra("action", EventType.MEDIA_PLAY_INFORM)
                    .putExtra("subtype", subtype)
                    .putExtra("data", data.toByteArray()));
//            }
        } else {
//            if (videoIsShowing) {
//                EventBus.getDefault().post(new EventMessage(EventType.CLOSE_VIDEO_ACTIVITY));
//            }
            //创建好下载目录
            FileUtil.createDir(CACHE_ALL_FILE);
            /* **** **  查询该媒体ID的文件名  ** **** */
            byte[] bytes = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaid);
            InterfaceBase.pbui_CommonTextProperty pbui_commonTextProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
            String fileName = MyUtils.b2s(pbui_commonTextProperty.getPropertyval());
            File file = new File(CACHE_ALL_FILE + fileName);
            LogUtil.i(TAG, "receiveMediaPlayInform fileName=" + fileName);
            if (!file.exists()) {
                jni.creationFileDownload(CACHE_ALL_FILE + fileName, mediaid, 0, 0, Macro.DOWNLOAD_PUSH_FILE + mediaid);
            } else {
                FileUtil.OpenThisFile(this, file, mediaid);
            }
        }
    }

    private void receivePlayStreamInform(final EventMessage message) {
        InterfaceStream.pbui_Type_MeetStreamPlay data = (InterfaceStream.pbui_Type_MeetStreamPlay) message.getObject();
        int triggerid = data.getTriggerid();
        int res = data.getRes();
        int deviceid = data.getDeviceid();
        int triggeruserval = data.getTriggeruserval();
        boolean isMandatory = (InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE & triggeruserval)
                == InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE;
        LogUtil.i(TAG, "receivePlayStreamInform :  流播放通知 --->>> 当前是否正在播放中= "
                + videoIsShowing + ", 是否强制= " + isMandatory + ",当前是否强制中=" + Values.isMandatory);
        if (res != 0) {
            //只处理播放资源为0的
            return;
        }
        if (Values.isMandatory) {//正在强制性播放中
            if (isMandatory) {//收到新的强制性播放

            } else {//收到的不是强制性播放
                LogUtil.i(TAG, "receivePlayStreamInform -->" + "当前属于强制性播放中，不处理非强制播放的通知");
                return;
            }
        }
        //是否是强制性播放
        Values.isMandatory = isMandatory;
        haveNewPlayInform = true;
//        if (!videoIsShowing) {
        startActivity(new Intent(NativeService.this, VideoActivity.class)
                .setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                .putExtra("action", EventType.PLAY_STREAM_NOTIFY)
                .putExtra("deivceid", deviceid)
                .putExtra("data", data.toByteArray())
        );
//        } else {
        if (Values.isMandatory) {
            EventBus.getDefault().post(new EventMessage(EventType.MANDATORY_PLAY));
        }
//        }
    }
}
