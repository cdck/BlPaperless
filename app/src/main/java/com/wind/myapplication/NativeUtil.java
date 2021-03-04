package com.wind.myapplication;

import android.content.Intent;
import android.graphics.PointF;

import com.blankj.utilcode.util.ToastUtils;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.service.App;
import com.pa.paperless.utils.LogUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceAdmin;
import com.mogujie.tt.protobuf.InterfaceAgenda;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceContext;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceDownload;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceIM;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePlaymedia;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceSignin;
import com.mogujie.tt.protobuf.InterfaceStatistic;
import com.mogujie.tt.protobuf.InterfaceStop;
import com.mogujie.tt.protobuf.InterfaceStream;
import com.mogujie.tt.protobuf.InterfaceUpload;
import com.mogujie.tt.protobuf.InterfaceVideo;
import com.mogujie.tt.protobuf.InterfaceVote;
import com.mogujie.tt.protobuf.InterfaceWhiteboard;
import com.pa.paperless.data.bean.SubmitVoteBean;
import com.pa.paperless.data.constant.BroadCaseAction;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.utils.IniUtil;
import com.pa.paperless.utils.MyUtils;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


import static android.content.Intent.FLAG_DEBUG_LOG_RESOLUTION;
import static com.pa.paperless.data.constant.BroadCaseAction.SCREEN_SHOT_TYPE;
import static com.pa.paperless.data.constant.EventType.CALLBACK_YUVDISPLAY;
import static com.pa.paperless.service.App.lbm;
import static com.pa.paperless.utils.MyUtils.s2b;


/**
 * @author xlk
 * @date 2017/12/6
 */

public class NativeUtil {

    private final String TAG = "NativeUtil-->";
    private final String CASE_TAG = "Case_Log";
    private static NativeUtil instance;

    static {
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("postproc-54");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");
        System.loadLibrary("SDL2");
        System.loadLibrary("main");
        System.loadLibrary("NetClient");
        System.loadLibrary("Codec");
        System.loadLibrary("ExecProc");
        System.loadLibrary("Device-OpenSles");
        System.loadLibrary("meetcoreAnd");
        System.loadLibrary("PBmeetcoreAnd");
        System.loadLibrary("meetAnd");
        System.loadLibrary("native-lib");
        System.loadLibrary("z");
    }

    private NativeUtil() {
    }

    public static synchronized NativeUtil getInstance() {
        if (instance == null) {
            instance = new NativeUtil();
        }
        return instance;
    }

    /**
     * 查询可加入的同屏会话
     */
    public InterfaceDevice.pbui_Type_DeviceResPlay queryCanJoin() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESINFO.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryCanJoin :  查询可加入的同屏会话失败 --->>> ");
            return null;
        }
        return InterfaceDevice.pbui_Type_DeviceResPlay.parseFrom(array);
    }

    /**
     * 初始化无纸化网络平台
     *
     * @param uniqueId
     * @return
     */
    public boolean javaInitSys(String uniqueId) {
        LogUtil.e(TAG, "NativeUtil.javaInitSys :  keystr手机的唯一标识符 --> " + uniqueId);
        InterfaceBase.pbui_MeetCore_InitParam.Builder tmp = InterfaceBase.pbui_MeetCore_InitParam.newBuilder();
        tmp.setPconfigpathname(s2b(IniUtil.inifile.getAbsolutePath()));
        tmp.setProgramtype(InterfaceMacro.Pb_ProgramType.Pb_MEET_PROGRAM_TYPE_MEETCLIENT.getNumber());
        tmp.setStreamnum(4);
        tmp.setLogtofile(0);
        tmp.setKeystr(s2b(uniqueId));
        InterfaceBase.pbui_MeetCore_InitParam pb = tmp.build();
        boolean bret = true;
        LogUtil.e(TAG, "javaInitSys:start!");
        if (-1 == Init_walletSys(pb.toByteArray())) {
            LogUtil.e(TAG, "NativeUtil.javaInitSys :  初始化失败了 --> ");
            bret = false;
        }
        LogUtil.e(TAG, "javaInitSys:finish!");
        return bret;
    }

    /**
     * 登录操作
     * Pb_String_LenLimit 限制:密码长度、用户名长度
     *
     * @param isascill =0md5字符密码 =1明文密码
     * @param name     用户名 常用人员手机号
     * @param pwd      用户密码(ascill/md5ascill)
     * @param mode     =0管理员登陆 =1常用人员登陆 =2离线本地模式
     */
    public void login(int isascill, String name, String pwd, int mode) {
        InterfaceAdmin.pbui_Type_AdminLogon build = InterfaceAdmin.pbui_Type_AdminLogon.newBuilder()
                .setAdminname(MyUtils.s2b(name))
                .setAdminpwd(MyUtils.s2b(pwd))
                .setLogonmode(mode)
                .setIsascill(isascill).build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ADMIN.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON.getNumber(), build.toByteArray());

    }

    /**
     * 查询会议
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceMeet.pbui_Type_MeetMeetInfo queryMeeting() throws InvalidProtocolBufferException {
        byte[] bytes = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (bytes != null) {
            return InterfaceMeet.pbui_Type_MeetMeetInfo.parseFrom(bytes);
        }
        return null;
    }

    //退出释放资源
    public void exit() {
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_EXITENV.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_EXIT.getNumber(), null);
    }

    /**
     * 初始化播放资源
     *
     * @param resid 资源ID
     * @param w
     * @param h
     * @return
     */
    public void initVideoRes(int resid, int w, int h) {
        InterfacePlaymedia.pbui_Type_MeetInitPlayRes.Builder builder = InterfacePlaymedia.pbui_Type_MeetInitPlayRes.newBuilder();
        builder.setRes(resid);
        builder.setY(0);
        builder.setX(0);
        builder.setW(w);
        builder.setH(h);
        InterfacePlaymedia.pbui_Type_MeetInitPlayRes build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_INIT.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.initVideoRes:  初始化播放资源 --->>> " + w + "," + h);
    }

    /**
     * 释放播放资源
     */
    public void mediaDestroy(int resValue) {
        InterfacePlaymedia.pbui_Type_MeetDestroyPlayRes.Builder builder = InterfacePlaymedia.pbui_Type_MeetDestroyPlayRes.newBuilder();
        builder.setRes(resValue);
        InterfacePlaymedia.pbui_Type_MeetDestroyPlayRes build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DESTORY.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.mediaDestroy 3211行:  释放播放资源 --->>> ");
    }

    /**
     * 查询设备信息
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceDevice.pbui_Type_DeviceDetailInfo queryDeviceInfo() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryDeviceInfo :  查询设备信息失败 --> ");
//            dataManage.removeAllDevice();
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryDeviceInfo :  查询设备信息成功 --> ");
        InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = InterfaceDevice.pbui_Type_DeviceDetailInfo.parseFrom(array);
        List<InterfaceDevice.pbui_Item_DeviceDetailInfo> pdevList = deviceDetailInfo.getPdevList();
//        for (InterfaceDevice.pbui_Item_DeviceDetailInfo info : pdevList) {
//            dataManage.addDevice(info);
//        }
        return deviceDetailInfo;
    }

    /**
     * 查询指定ID的设备信息
     *
     * @param devid
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceDevice.pbui_Type_DeviceDetailInfo queryDevInfoById(int devid) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID build = InterfaceBase.pbui_QueryInfoByID.newBuilder()
                .setId(devid).build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SINGLEQUERYBYID.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryDevInfoById :  查询指定ID的设备信息失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryDevInfoById :  查询指定ID的设备信息成功 --> ");
        InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = InterfaceDevice.pbui_Type_DeviceDetailInfo.parseFrom(array);
        InterfaceDevice.pbui_Item_DeviceDetailInfo pbui_item_deviceDetailInfo = deviceDetailInfo.getPdevList().get(0);
//        dataManage.addDevice(pbui_item_deviceDetailInfo);
        return deviceDetailInfo;
    }

    /**
     * 按属性ID查询指定设备属性
     *
     * @param propetyid InterfaceMacro.Pb_MeetDevicePropertyID
     * @param devId     =0是本机
     * @return pbui_DeviceInt32uProperty（整数）如果是查询的网络状态=0离线，=1在线
     * pbui_DeviceStringProperty（字符串）
     */
    public byte[] queryDevicePropertiesById(int propetyid, int devId) {
        InterfaceDevice.pbui_MeetDeviceQueryProperty.Builder builder = InterfaceDevice.pbui_MeetDeviceQueryProperty.newBuilder();
        builder.setPropertyid(propetyid);
        builder.setDeviceid(devId);
        builder.setParamterval(0);
        InterfaceDevice.pbui_MeetDeviceQueryProperty build = builder.build();
        byte[] bytes = build.toByteArray();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), bytes);
        if (array == null) {
            LogUtil.e(TAG, "queryDevicePropertiesById :  按属性ID查询指定设备属性失败 --->>> propetyid=" + propetyid + ",devId=" + devId);
            return null;
        }
        LogUtil.e(TAG, "queryDevicePropertiesById:  按属性ID查询指定设备属性成功 --->>> propetyid=" + propetyid + ",devId=" + devId);
        return array;
    }

    /**
     * 修改本机界面状态
     * Pb_MemState_MainFace=0; //处于主界面
     * Pb_MemState_MemFace=1;//参会人员界面
     * Pb_MemState_AdminFace=2;//后台管理界面
     *
     * @param propertyid
     * @param value
     */
    public void setInterfaceState(int propertyid, int value) {
        InterfaceContext.pbui_MeetContextInfo.Builder builder = InterfaceContext.pbui_MeetContextInfo.newBuilder();
        builder.setPropertyid(propertyid);
        builder.setPropertyval(value);
        InterfaceContext.pbui_MeetContextInfo build = builder.build();
        byte[] bytes = build.toByteArray();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETCONTEXT.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SETPROPERTY.getNumber(), bytes);
        LogUtil.e(TAG, "setInterfaceState:  修改本机界面状态 为 --->>> " + value);
    }

    /**
     * 修改设备信息
     *
     * @param modflag       参见 Pb_DeviceModifyFlag
     * @param devcieid
     * @param devname
     * @param ipinfo        pbui_SubItem_DeviceIpAddrInfo ip信息
     * @param liftgroupres0 升降话筒组ID
     * @param liftgroupres1 升降话筒组ID
     * @param deviceflag    参见  Pb_MeetDeviceFlag
     */
    public void modifyDeviceInfo(int modflag, int devcieid, ByteString devname, InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo ipinfo,
                                 int liftgroupres0, int liftgroupres1, int deviceflag) {
        InterfaceDevice.pbui_DeviceModInfo.Builder builder = InterfaceDevice.pbui_DeviceModInfo.newBuilder();
        builder.setModflag(modflag);
        builder.setDevcieid(devcieid);
        builder.setDevname(devname);
        builder.addIpinfo(ipinfo);
        builder.setLiftgroupres0(liftgroupres0);
        builder.setLiftgroupres1(liftgroupres1);
        builder.setDeviceflag(deviceflag);
        InterfaceDevice.pbui_DeviceModInfo build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_MODIFYINFO.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.modifyDeviceInfo:  13.修改设备信息传入的设备ID --->>> " + devcieid);
    }

    /**
     * 辅助签到操作
     *
     * @param devids
     */
    public void signAlterationOperate(List<Integer> devids) {
        InterfaceDevice.pbui_MeetDoEnterMeet.Builder builder = InterfaceDevice.pbui_MeetDoEnterMeet.newBuilder();
        builder.addAllDevid(devids);
        InterfaceDevice.pbui_MeetDoEnterMeet build = builder.build();
        byte[] bytes = build.toByteArray();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ENTER.getNumber(), bytes);
        LogUtil.e(TAG, "NativeUtil.signAlterationOperate:  18.辅助签到操作 --->>> ");
    }

    /**
     * 发送请求参会人员权限请求
     *
     * @param devid     向谁的设备申请权限
     * @param privilege 需要申请的权限
     * @return
     */
    public void sendAttendRequestPermissions(int devid, int privilege) {
        InterfaceDevice.pbui_Type_MeetRequestPrivilege.Builder builder = InterfaceDevice.pbui_Type_MeetRequestPrivilege.newBuilder();
        builder.addDevid(devid);
        builder.setPrivilege(privilege);
        InterfaceDevice.pbui_Type_MeetRequestPrivilege build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REQUESTPRIVELIGE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.sendRequestPermissions:  发送请求参会人员权限请求 --->>> " + privilege);
    }

    /**
     * 回复参会人员权限请求
     *
     * @param devid      回复给的对象
     * @param returncode 1=同意,0=不同意
     * @return
     */
    public void revertAttendPermissionsRequest(int devid, int returncode) {
        InterfaceDevice.pbui_Type_MeetResponseRequestPrivilege.Builder builder = InterfaceDevice.pbui_Type_MeetResponseRequestPrivilege.newBuilder();
        builder.addDevid(devid);
        builder.setReturncode(returncode);
        InterfaceDevice.pbui_Type_MeetResponseRequestPrivilege build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESPONSEPRIVELIGE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "revertAttendPermissionsRequest:    回复参会人员权限请求 --->>> devid= " + devid);
    }

    /**
     * 按属性ID查询指定上下文属性
     *
     * @param propertyid Pb_ContextPropertyID
     * @return InterfaceContext.pbui_MeetContextInfo
     * @throws InvalidProtocolBufferException
     */
    public InterfaceContext.pbui_MeetContextInfo queryContextProperty(int propertyid) throws InvalidProtocolBufferException {
        InterfaceContext.pbui_QueryMeetContextInfo.Builder builder = InterfaceContext.pbui_QueryMeetContextInfo.newBuilder();
        builder.setPropertyid(propertyid);
        InterfaceContext.pbui_QueryMeetContextInfo build = builder.build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETCONTEXT.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryContextProperty :  31.按属性ID查询指定上下文属性失败 --->>> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryContextProperty:    31.按属性ID查询指定上下文属性成功 --->>> ");
        return InterfaceContext.pbui_MeetContextInfo.parseFrom(array);
    }

    /**
     * Interface_Macro.Pb_ContextPropertyID
     * 修改上下文属性
     *
     * @param propertyId
     * @param propertyVal
     */
    public void setContextProperty(int propertyId, int propertyVal) {
        InterfaceContext.pbui_MeetContextInfo build = InterfaceContext.pbui_MeetContextInfo.newBuilder()
                .setPropertyid(propertyId)
                .setPropertyval(propertyVal)
                .build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETCONTEXT.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SETPROPERTY.getNumber(),
                build.toByteArray());
    }

    /**
     * 删除会议
     *
     * @param meetMeetInfo
     */
    public void deleteMeeting(InterfaceMeet.pbui_Item_MeetMeetInfo meetMeetInfo) {
        InterfaceMeet.pbui_Type_MeetMeetInfo build = InterfaceMeet.pbui_Type_MeetMeetInfo.newBuilder()
                .addItem(meetMeetInfo).build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL.getNumber(),
                build.toByteArray());
    }

    /**
     * 删除会议目录
     */
    public void deleteMeetDir(InterfaceFile.pbui_Item_MeetDirDetailInfo dirDetailInfo) {
        LogUtil.e(CASE_TAG, "删除会议目录ID：" + dirDetailInfo.getId());
        InterfaceFile.pbui_Type_MeetDirDetailInfo build1 = InterfaceFile.pbui_Type_MeetDirDetailInfo.newBuilder()
                .addItem(dirDetailInfo).build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL.getNumber(),
                build1.toByteArray());
    }

    /**
     * 删除会议目录文件
     *
     * @param selectFile
     */
    public void deleteMeetDirFile(int dirid, List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> selectFile) {
        LogUtil.e(TAG, " 删除会议目录文件 目录ID -->" + dirid);
        InterfaceFile.pbui_Type_MeetDirFileDetailInfo build = InterfaceFile.pbui_Type_MeetDirFileDetailInfo.newBuilder()
                .setDirid(dirid)
                .addAllItem(selectFile)
                .build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL.getNumber(),
                build.toByteArray());
    }


    /**
     * 查询议程
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceAgenda.pbui_meetAgenda queryAgenda() throws InvalidProtocolBufferException {
        InterfaceAgenda.pbui_meetAgenda build = InterfaceAgenda.pbui_meetAgenda.newBuilder()
                .setAgendatype(0)
                .build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETAGENDA.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryAgenda :  查询议程失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryAgenda :  查询议程成功 --> ");
        return InterfaceAgenda.pbui_meetAgenda.parseFrom(array);
    }

    /**
     * 网页查询
     */
    public InterfaceBase.pbui_meetUrl webQuery() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEFAULTURL.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.webQuery 494行:  41.网页查询 --->>> 失败");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.webQuery 498行:  41.网页查询 --->>> 成功");
        return InterfaceBase.pbui_meetUrl.getDefaultInstance().parseFrom(array);
    }

    /**
     * 查询公告
     */
    public InterfaceBullet.pbui_BulletDetailInfo queryNotice() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryNotice:  44.查询公告失败 --->>> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryNotice:  44.查询公告成功 --->>> ");
        return InterfaceBullet.pbui_BulletDetailInfo.parseFrom(array);
    }

    /**
     * 添加公告
     *
     * @param item
     */
    public void addNotice(List<InterfaceBullet.pbui_Item_BulletDetailInfo> item) {
        InterfaceBullet.pbui_BulletDetailInfo.Builder builder = InterfaceBullet.pbui_BulletDetailInfo.newBuilder();
        builder.addAllItem(item);
        InterfaceBullet.pbui_BulletDetailInfo build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.addNotice:  45.添加公告 --->>> ");
    }

    /**
     * 修改公告
     *
     * @param item
     */
    public void modifNotice(InterfaceBullet.pbui_Item_BulletDetailInfo item) {
        InterfaceBullet.pbui_BulletDetailInfo.Builder builder = InterfaceBullet.pbui_BulletDetailInfo.newBuilder();
        builder.addItem(item);
        InterfaceBullet.pbui_BulletDetailInfo build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_MODIFY.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.modifNotice:  46.修改公告 --->>> ");
    }

    /**
     * 删除公告
     *
     * @param item
     */
    public void deleteNotice(List<InterfaceBullet.pbui_Item_BulletDetailInfo> item) {
        InterfaceBullet.pbui_BulletDetailInfo.Builder builder = InterfaceBullet.pbui_BulletDetailInfo.newBuilder();
        builder.addAllItem(item);
        InterfaceBullet.pbui_BulletDetailInfo build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.deleteNotice:  47.删除公告 --->>> ");
    }

    /**
     * 查询指定的公告
     *
     * @param value
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceBullet.pbui_BulletDetailInfo queryAssignNotice(int value) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID.Builder builder = InterfaceBase.pbui_QueryInfoByID.newBuilder();
        builder.setId(value);
        InterfaceBase.pbui_QueryInfoByID build = builder.build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SINGLEQUERYBYID.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryAssignNotice :  查询指定的公告失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryAssignNotice :  查询指定的公告成功 --> ");
        return InterfaceBullet.pbui_BulletDetailInfo.parseFrom(array);
    }

    /**
     * 发布公告
     *
     * @param item
     * @param devids 要观看的设备
     */
    public void pushNotice(InterfaceBullet.pbui_Item_BulletDetailInfo item, List<Integer> devids) {
        InterfaceBullet.pbui_Type_MeetPublishBulletInfo.Builder builder = InterfaceBullet.pbui_Type_MeetPublishBulletInfo.newBuilder();
        builder.addAllDeviceid(devids);
        builder.setItem(item);
        InterfaceBullet.pbui_Type_MeetPublishBulletInfo build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_PUBLIST.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.issueNotice:  49.发布公告 --->>> ");
    }

    /**
     * 停止公告
     *
     * @param bulletid
     * @param devids
     */
    public void stopNotice(int bulletid, List<Integer> devids) {
        InterfaceBullet.pbui_Type_StopBullet.Builder builder = InterfaceBullet.pbui_Type_StopBullet.newBuilder();
        builder.setBulletid(bulletid);
        builder.addAllPdevid(devids);
        InterfaceBullet.pbui_Type_StopBullet build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_STOP.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.stopNotice :  停止公告 --> " + devids.toString());
    }

    /**
     * 执行终端控制
     *
     * @param oper enum Pb_DeviceControlFlag
     * @return
     */
    public void executeTerminalControl(int oper, int operval1, int operval2, List<Integer> devids) {
        InterfaceDevice.pbui_Type_DeviceOperControl.Builder builder = InterfaceDevice.pbui_Type_DeviceOperControl.newBuilder();
        builder.setOper(oper);
        builder.setOperval1(operval1);
        builder.setOperval2(operval2);
        builder.addAllDevid(devids);
        InterfaceDevice.pbui_Type_DeviceOperControl build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICECONTROL.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CONTROL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.executeTerminalControl:  62.执行终端控制 --->>> ");
    }

    /**
     * 创建一个文件下载
     *
     * @param pathname   下载媒体全路径名称
     * @param mediaid
     * @param newfile    0 不覆盖同名文件,1 覆盖下载
     * @param onlyfinish 1 表示只需要结束的通知
     * @param userStr    用户传入的自定义字串
     * @return
     */
    public void creationFileDownload(String pathname, int mediaid, int newfile, int onlyfinish, String userStr) {
        InterfaceDownload.pbui_Type_DownloadStart.Builder builder = InterfaceDownload.pbui_Type_DownloadStart.newBuilder();
        builder.setMediaid(mediaid);
        builder.setNewfile(newfile);
        builder.setOnlyfinish(onlyfinish);
        LogUtil.e(TAG, "creationFileDownload: 创建一个文件下载 mediaid --->>> " + mediaid + "  文件：" + pathname + ", userStr=" + userStr);
        builder.setPathname(s2b(pathname));
        builder.setUserstr(s2b(userStr));
        InterfaceDownload.pbui_Type_DownloadStart build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DOWNLOAD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), build.toByteArray());
    }

    /**
     * 创建一个文件离线本地缓存
     *
     * @param dirid      目录ID
     * @param mediaid    媒体ID
     * @param newfile    =0不覆盖同名文件,=1重新下载
     * @param onlyfinish =1表示只需要结束的通知
     * @param userStr    自定义标识
     */
    public void createFileCache(int dirid, int mediaid, int newfile, int onlyfinish, String userStr) {
        InterfaceDownload.pbui_Type_DownloadCache build = InterfaceDownload.pbui_Type_DownloadCache.newBuilder()
                .setDirid(dirid)
                .setMediaid(mediaid)
                .setNewfile(newfile)
                .setOnlyfinish(onlyfinish)
                .setUserstr(MyUtils.s2b(userStr))
                .build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DOWNLOAD.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SAVE.getNumber(), build.toByteArray());
    }


    /**
     * 上传文件
     *
     * @param uploadflag 上传标志 参见 Pb_Upload_Flag
     * @param dirid      上传的目录ID 参见 Pb_Upload_DefaultDirId
     * @param attrib     文件属性 参见 Pb_MeetFileAttrib
     * @param newname    上传后的新名称
     * @param pathname   全路径名
     * @param userval    用户自定义的值
     * @param userstr    用户传入的自定义字串(原编码格式返回)
     */
    public void uploadFile(int uploadflag, int dirid, int attrib, String newname, String pathname, int userval, String userstr) {
        InterfaceUpload.pbui_Type_AddUploadFile.Builder builder = InterfaceUpload.pbui_Type_AddUploadFile.newBuilder();
        builder.setUploadflag(uploadflag);
        builder.setDirid(dirid);
        builder.setAttrib(attrib);
        builder.setNewname(s2b(newname));
        builder.setPathname(s2b(pathname));
        builder.setUserval(userval);
        builder.setUserstr(s2b(userstr));
        InterfaceUpload.pbui_Type_AddUploadFile build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_UPLOAD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "uploadFile :   --> 上传文件 " + newname);
    }

    /**
     * 添加本地文件到缓存目录
     *
     * @param dirName 目录名称
     * @param path    文件路径
     */
    public void addLocalFile2Cache(String dirName, String path) {
        InterfaceUpload.pbui_Type_AddCacheFile build = InterfaceUpload.pbui_Type_AddCacheFile.newBuilder()
                .setDirname(MyUtils.s2b(dirName))
                .setPathname(MyUtils.s2b(path)).build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_UPLOAD.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SAVE.getNumber(), build.toByteArray());
    }

    /**
     * 91.查询指定ID的参会人员
     *
     * @param memberId 人员ID
     */
    public InterfaceMember.pbui_Type_MemberDetailInfo queryAttendPeopleFromId(int memberId) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID.Builder builder = InterfaceBase.pbui_QueryInfoByID.newBuilder();
        builder.setId(memberId);
        InterfaceBase.pbui_QueryInfoByID build = builder.build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SINGLEQUERYBYID.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryAttendPeopleFromId :  查询指定ID的参会人员失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryAttendPeopleFromId :  查询指定ID的参会人员成功 --> ");
        return InterfaceMember.pbui_Type_MemberDetailInfo.parseFrom(array);
    }

    /**
     * 92.查询参会人员
     */
    public InterfaceMember.pbui_Type_MemberDetailInfo queryAttendPeople() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryAttendPeople:  92.查询参会人员失败 --->>> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryAttendPeopleFromId:  92.查询参会人员成功 --->>> ");
        InterfaceMember.pbui_Type_MemberDetailInfo pbui_type_memberDetailInfo = InterfaceMember.pbui_Type_MemberDetailInfo.parseFrom(array);

        return pbui_type_memberDetailInfo;
    }

    /**
     * 查询参会人员属性
     *
     * @param propertyid InterfaceMacro.Pb_MemberPropertyID
     * @param memberId   传入参数 为0表示本机设置定的人员id
     */
    public InterfaceMember.pbui_Type_MeetMembeProperty queryMemberAttributes(int propertyid, int memberId) {
        InterfaceMember.pbui_Type_MeetMemberQueryProperty build = InterfaceMember.pbui_Type_MeetMemberQueryProperty.newBuilder()
                .setPropertyid(propertyid).setParameterval(memberId).build();
        byte[] bytes = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY_VALUE, build.toByteArray());
        if (bytes != null) {
            try {
                InterfaceMember.pbui_Type_MeetMembeProperty pbui_type_meetMembeProperty = InterfaceMember.pbui_Type_MeetMembeProperty.parseFrom(bytes);
                LogUtil.i(TAG, "queryMemberAttributes 查询参会人员属性成功 propertyid=" + propertyid + ", memberId=" + memberId);
                return pbui_type_meetMembeProperty;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        LogUtil.i(TAG, "queryMemberAttributes 查询参会人员属性失败 propertyid=" + propertyid + ", memberId=" + memberId);
        return null;
    }

    /**
     * 查询参会人员详细信息
     */
    public InterfaceMember.pbui_Type_MeetMemberDetailInfo queryAttendPeopleDetailed() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DETAILINFO.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryAttendPeopleDetailed :  查询参会人员详细信息失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryAttendPeopleDetailed :  查询参会人员详细信息成功 --> ");
        return InterfaceMember.pbui_Type_MeetMemberDetailInfo.parseFrom(array);
    }

    /**
     * 93.添加参会人员
     */
    public void addAttendPeople(InterfaceMember.pbui_Item_MemberDetailInfo info) {
        InterfaceMember.pbui_Type_MemberDetailInfo build = InterfaceMember.pbui_Type_MemberDetailInfo.newBuilder()
                .addItem(info).build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.addAttendPeople:  93.添加参会人员 --->>> ");
    }


    /**
     * 查询指定ID的参会人
     *
     * @param memberId 参会人ID
     */
    public InterfaceMember.pbui_Type_MemberDetailInfo queryMemberById(int memberId) {
        InterfaceBase.pbui_QueryInfoByID build = InterfaceBase.pbui_QueryInfoByID.newBuilder()
                .setId(memberId)
                .build();
        byte[] bytes = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SINGLEQUERYBYID_VALUE, build.toByteArray());
        if (bytes == null) {
            LogUtil.e(TAG, "queryMemberById 查询指定ID的参会人失败");
            return null;
        }
        LogUtil.i(TAG, "queryMemberById 查询指定ID的参会人成功");
        try {
            return InterfaceMember.pbui_Type_MemberDetailInfo.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 97.查询参会人员权限
     */
    public InterfaceMember.pbui_Type_MemberPermission queryAttendPeoplePermissions() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBERPERMISSION.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryAttendPeoplePermissions :  查询参会人员权限失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryAttendPeoplePermissions :  查询参会人员权限成功 --> ");
        return InterfaceMember.pbui_Type_MemberPermission.parseFrom(array);
    }

    /**
     * 110.查询设备会议信息
     */
    public InterfaceDevice.pbui_Type_DeviceFaceShowDetail queryDeviceMeetInfo() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEFACESHOW.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryDeviceMeetInfo:  110.查询设备会议信息 --->>> 失败");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryDeviceMeetInfo:  110.查询设备会议信息 --->>> 成功");
        return InterfaceDevice.pbui_Type_DeviceFaceShowDetail.parseFrom(array);
    }

    /**
     * 120.添加会场设备
     */
    public void addPlaceDevice(int roomid, int devid) {
        InterfaceRoom.pbui_Type_MeetRoomModDeviceInfo.Builder builder = InterfaceRoom.pbui_Type_MeetRoomModDeviceInfo.newBuilder();
        builder.setRoomid(roomid);
        builder.addDeviceid(devid);
        LogUtil.e(TAG, "NativeUtil.addPlaceDevice : 添加会场设备操作  --> 会议室ID: " + roomid + ", 设备ID: " + devid);
        InterfaceRoom.pbui_Type_MeetRoomModDeviceInfo build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.addPlaceDevice :  添加会场设备 --> ");
    }

    /**
     * 124.会场设备排位详细信息
     */
    public InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo placeDeviceRankingInfo(int id) {
        InterfaceBase.pbui_QueryInfoByID.Builder builder = InterfaceBase.pbui_QueryInfoByID.newBuilder();
        builder.setId(id);
        InterfaceBase.pbui_QueryInfoByID build = builder.build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DETAILINFO.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.placeDeviceRankingInfo :  查询会场设备排位详细信息失败 --> ");
            return null;
        }
        try {
            InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo pbui_type_meetRoomDevSeatDetailInfo = InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo.parseFrom(array);
            LogUtil.e(TAG, "NativeUtil.placeDeviceRankingInfo :  查询会场设备排位详细信息成功 --> ");
            return pbui_type_meetRoomDevSeatDetailInfo;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 129.查询指定ID的会议
     */
    public InterfaceMeet.pbui_Type_MeetMeetInfo queryMeetFromId(int value) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID.Builder builder = InterfaceBase.pbui_QueryInfoByID.newBuilder();
        builder.setId(value);
        InterfaceBase.pbui_QueryInfoByID build = builder.build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SINGLEQUERYBYID.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryMeetFromId :  查询指定ID的会议失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryMeetFromId :  查询指定ID的会议成功 --> ");
        return InterfaceMeet.pbui_Type_MeetMeetInfo.parseFrom(array);
    }

    /**
     * 136.查询会议目录
     */
    public InterfaceFile.pbui_Type_MeetDirDetailInfo queryMeetDir() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryMeetDir :  查询会议目录失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryMeetDir :  查询会议目录成功 --> ");
        return InterfaceFile.pbui_Type_MeetDirDetailInfo.parseFrom(array);
    }

    /**
     * 143.查询会议目录文件
     */
    public InterfaceFile.pbui_Type_MeetDirFileDetailInfo queryMeetDirFile(int value) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID.Builder builder = InterfaceBase.pbui_QueryInfoByID.newBuilder();
        builder.setId(value);
        LogUtil.e(TAG, "NativeUtil.queryMeetDirFile:  要查询的目录ID： --->>> " + value);
        InterfaceBase.pbui_QueryInfoByID build = builder.build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryMeetDirFile :  查询会议目录文件失败 --> " + value);
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryMeetDirFile :  查询会议目录文件成功 --> " + value);
        return InterfaceFile.pbui_Type_MeetDirFileDetailInfo.parseFrom(array);
    }

    /**
     * 149.查询文件属性
     */
    public byte[] queryFileProperty(int propertyid, int parmeterval/*,int parmeterva2*/) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_CommonQueryProperty.Builder builder = InterfaceBase.pbui_CommonQueryProperty.newBuilder();
        builder.setPropertyid(propertyid);
        builder.setParameterval(parmeterval);
//        builder.setParameterval2(parmeterva2);
        InterfaceBase.pbui_CommonQueryProperty build = builder.build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), build.toByteArray());
        //  pbui_CommonInt64uProperty、Type_MeetMFileQueryPropertyString、
        //
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryFileProperty:  149.查询文件属性失败 --->>> ");
            return new byte[0];
        }
//        LogUtil.e(TAG, "NativeUtil.queryFileProperty:  149.查询文件属性成功 --->>> ");
        return array;
    }

    /**
     * 172.查询会议视频
     */
    public InterfaceVideo.pbui_Type_MeetVideoDetailInfo queryMeetVedio() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVIDEO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryMeetVedio :  查询会议视频失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryMeetVedio :  查询会议视频成功 --> ");
        return InterfaceVideo.pbui_Type_MeetVideoDetailInfo.parseFrom(array);
    }

    /**
     * 181.查询会议排位
     */
    public InterfaceRoom.pbui_Type_MeetSeatDetailInfo queryMeetRanking() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryMeetRanking :  查询会议排位失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryMeetRanking :  查询会议排位成功 --> ");
        return InterfaceRoom.pbui_Type_MeetSeatDetailInfo.parseFrom(array);
    }

    /**
     * 182.修改会议排位
     */
    public void modifyMeetRanking(int nameid, int role, int seatid) {
        InterfaceRoom.pbui_Item_MeetSeatDetailInfo.Builder builder1 = InterfaceRoom.pbui_Item_MeetSeatDetailInfo.newBuilder();
        builder1.setNameId(nameid);
        builder1.setSeatid(seatid);
        builder1.setRole(role);
        InterfaceRoom.pbui_Type_MeetSeatDetailInfo.Builder builder = InterfaceRoom.pbui_Type_MeetSeatDetailInfo.newBuilder();
        builder.addItem(builder1);
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_MODIFY.getNumber(), builder.build().toByteArray());
        LogUtil.e(TAG, "NativeUtil.modifMeetRanking:  182.修改会议排位 --->>>nameid: " + nameid + ", role:  " + role + " , devid: " + seatid);
    }

    /**
     * 183.查询会议排位属性
     */
    public InterfaceBase.pbui_CommonInt32uProperty queryMeetRankingProperty(int propertyid) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_CommonQueryProperty.Builder builder = InterfaceBase.pbui_CommonQueryProperty.newBuilder();
        builder.setPropertyid(propertyid);
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), builder.build().toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryMeetRankingProperty :  查询会议排位属性失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryMeetRankingProperty :  查询会议排位属性成功 --> ");
        return InterfaceBase.pbui_CommonInt32uProperty.parseFrom(array);
    }

    /**
     * 发送会议交流信息
     */
    public void sendMeetChatInfo(String sendMsg, int msgType, Iterable<Integer> iterable) {
        InterfaceIM.pbui_Type_SendMeetIM.Builder builder = InterfaceIM.pbui_Type_SendMeetIM.newBuilder();
        builder.setMsg(s2b(sendMsg));
        builder.setMsgtype(msgType);
        builder.addAllUserids(iterable);
        InterfaceIM.pbui_Type_SendMeetIM build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETIM.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SEND.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.sendMeetInfo:  发送会议交流信息 --->>> ");
    }

    /**
     * 189.查询发起的投票
     */
    public InterfaceVote.pbui_Type_MeetOnVotingDetailInfo queryInitiateVote() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryInitiateVote:  189.查询发起的投票失败 --->>> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryVote:  189.查询发起的投票成功 --->>> ");
        return InterfaceVote.pbui_Type_MeetOnVotingDetailInfo.parseFrom(array);
    }

    /**
     * 191.新建一个投票
     */
    public void createVote(List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> votes) {
        InterfaceVote.pbui_Type_MeetOnVotingDetailInfo.Builder builder1 = InterfaceVote.pbui_Type_MeetOnVotingDetailInfo.newBuilder();
        builder1.addAllItem(votes);
        InterfaceVote.pbui_Type_MeetOnVotingDetailInfo build = builder1.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.createVote:  191.新建一个投票 --->>> ");
    }

    /**
     * 192.修改一个投票
     */
    public void modifyVote(InterfaceVote.pbui_Item_MeetOnVotingDetailInfo item) {
        InterfaceVote.pbui_Type_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Type_MeetOnVotingDetailInfo.newBuilder();
        builder.addItem(item);
        InterfaceVote.pbui_Type_MeetOnVotingDetailInfo build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_MODIFY.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.modifyVote:  192.修改一个投票 --->>> ");
    }

    /**
     * 193.发起投票
     */
    public void initiateVote(int voteid, int voteflag, int timeouts, List<Integer> memberIds) {
        InterfaceVote.pbui_ItemVoteStart.Builder b = InterfaceVote.pbui_ItemVoteStart.newBuilder();
        LogUtil.e(TAG, "NativeUtil.发起投票 :voteflag:" + voteflag + ", timeouts --> " + timeouts + ", memberIds:" + memberIds.size());
        b.setVoteid(voteid);
        b.setVoteflag(voteflag);
        b.setTimeouts(timeouts);
        b.addAllMemberid(memberIds);
        InterfaceVote.pbui_Type_MeetStartVoteInfo.Builder builder = InterfaceVote.pbui_Type_MeetStartVoteInfo.newBuilder();
        builder.addItem(b);
        InterfaceVote.pbui_Type_MeetStartVoteInfo build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.initiateVote:  193.发起投票 --->>> ");
    }

    /**
     * 194.删除投票
     */
    public void deleteVote(List<Integer> voteids) {
        InterfaceVote.pbui_Type_MeetStopVoteInfo.Builder builder = InterfaceVote.pbui_Type_MeetStopVoteInfo.newBuilder();
        builder.addAllVoteid(voteids);
        InterfaceVote.pbui_Type_MeetStopVoteInfo build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.deleteVote:  194.删除投票 --->>> ");
    }

    /**
     * 195.停止投票
     */
    public void stopVote(int voteid) {
        InterfaceVote.pbui_Type_MeetStopVoteInfo.Builder builder = InterfaceVote.pbui_Type_MeetStopVoteInfo.newBuilder();
        builder.addVoteid(voteid);
        InterfaceVote.pbui_Type_MeetStopVoteInfo build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_STOP.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.deleteVote:  195.停止投票 --->>> ");
    }

    /**
     * 196.提交投票结果
     */
    public void submitVoteResult(SubmitVoteBean submitVoteBeen) {
        InterfaceVote.pbui_Item_MeetSubmitVote.Builder builder1 = InterfaceVote.pbui_Item_MeetSubmitVote.newBuilder();
        builder1.setSelcnt(submitVoteBeen.getSelcnt());
        builder1.setVoteid(submitVoteBeen.getVoteid());
        builder1.setSelitem(submitVoteBeen.getSelectItem());
        LogUtil.e(TAG, "NativeUtil.submitVoteResult 2555行:  提交投票结果传递参数 --->>> 选择了的个数：" + submitVoteBeen.getSelcnt() +
                "  传递的投票ID  " + submitVoteBeen.getVoteid() + "   已经选择的十进制代表数： " + submitVoteBeen.getSelectItem());
        InterfaceVote.pbui_Type_MeetSubmitVote.Builder builder = InterfaceVote.pbui_Type_MeetSubmitVote.newBuilder();
        builder.addItem(builder1);
        InterfaceVote.pbui_Type_MeetSubmitVote build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SUBMIT.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.submitVoteResult:  196.提交投票结果 --->>> ");
    }

    /**
     * 扫码加入会议
     *
     * @param meetingid         会议ID
     * @param Pb_MeetMemberRole 参会人身份
     * @param builder1          参会人信息
     */
    public void scan(int meetingid, int Pb_MeetMemberRole, InterfaceMember.pbui_Item_MemberDetailInfo.Builder builder1) {
        InterfaceMember.pbui_Type_ScanEnterMeet.Builder builder = InterfaceMember.pbui_Type_ScanEnterMeet.newBuilder();
        builder.setMeetingid(meetingid);
        builder.setMemberrole(Pb_MeetMemberRole);
        builder.setMemberinfo(builder1);
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ENTER.getNumber(), builder.build().toByteArray());
        LogUtil.e(TAG, "NativeUtil.scan :  扫码加入会议 --> ");
    }

    /**
     * 200.查询投票
     */
    public InterfaceVote.pbui_Type_MeetVoteDetailInfo queryVote() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryVote :  查询投票失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryVote :  查询投票成功 --> ");
        return InterfaceVote.pbui_Type_MeetVoteDetailInfo.parseFrom(array);
    }

    /**
     * 按类别查询投票
     *
     * @param type InterfaceMacro.Pb_MeetVoteType
     */
    public InterfaceVote.pbui_Type_MeetVoteDetailInfo queryVoteByType(int type) throws InvalidProtocolBufferException {
        InterfaceVote.pbui_Type_MeetVoteComplexQuery build = InterfaceVote.pbui_Type_MeetVoteComplexQuery.newBuilder()
                .setMaintype(type).build();

        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_COMPLEXQUERY.getNumber(), build.toByteArray());
        if (array == null) {
            return null;
        } else {
            return InterfaceVote.pbui_Type_MeetVoteDetailInfo.parseFrom(array);
        }
    }

    /**
     * 203.查询指定投票的提交人
     */
    public InterfaceVote.pbui_Type_MeetVoteSignInDetailInfo queryOneVoteSubmitter(int value) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID.Builder builder = InterfaceBase.pbui_QueryInfoByID.newBuilder();
        builder.setId(value);
        InterfaceBase.pbui_QueryInfoByID build = builder.build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTESIGNED.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryOneVoteSubmitter :  查询指定投票的提交人失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryOneVoteSubmitter :  查询指定投票的提交人成功 --> ");
        return InterfaceVote.pbui_Type_MeetVoteSignInDetailInfo.parseFrom(array);
    }

    /**
     * 204.查询投票提交人属性
     */
    public InterfaceBase.pbui_CommonInt32uProperty queryVoteSubmitterProperty(int voteid, int memberid, int propertyid) {
        InterfaceVote.pbui_Type_MeetVoteQueryProperty.Builder builder = InterfaceVote.pbui_Type_MeetVoteQueryProperty.newBuilder();
        builder.setVoteid(voteid);
        builder.setMemberid(memberid);
        builder.setPropertyid(propertyid);
        InterfaceVote.pbui_Type_MeetVoteQueryProperty build = builder.build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTESIGNED.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryVoteSubmitterProperty :  查询投票提交人属性失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryVoteSubmitterProperty :  查询投票提交人属性成功 --> ");
        try {
            return InterfaceBase.pbui_CommonInt32uProperty.parseFrom(array);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 缓存会议数据
     *
     * @param type 数据类型
     * @param id   eg:需要缓存目录id=0表示缓存所有目录信息(不包括目录里的文件),当id=1时表示缓存该目录里的文件,
     *             如果id=0不支持则会返回ERROR_MEET_INTERFACE_PARAMETER
     * @param flag Interface_Macro.Pb_CacheFlag =1表示强制缓存
     */
    public void cacheData(int type, int id, int flag) {
        InterfaceBase.pbui_MeetCacheOper.Builder builder = InterfaceBase.pbui_MeetCacheOper.newBuilder();
        builder.setId(id);
        builder.setCacheflag(flag);
        LogUtil.e(TAG, "NativeUtil.cacheData :  缓存会议数据 --> type=" + type + ",id=" + id + ",flag=" + flag);
        call_method(type, InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CACHE.getNumber(), builder.build().toByteArray());
    }


    /**
     * 206.查询签到
     */
    public InterfaceSignin.pbui_Type_MeetSignInDetailInfo querySign() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.querySign :  查询签到失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.querySign :  查询签到成功 --> ");
        return InterfaceSignin.pbui_Type_MeetSignInDetailInfo.parseFrom(array);
    }

    /**
     * 删除签到记录
     *  meetingid 指定会议ID 0表示绑定的会议
     * @param memberids 为空表示删除指定会议的全部人员签到
     */
    public void deleteSign(List<Integer> memberids) {
        InterfaceSignin.pbui_Type_DoDeleteMeetSignIno build = InterfaceSignin.pbui_Type_DoDeleteMeetSignIno.newBuilder()
                .addAllMemberids(memberids)
                .setMeetingid(0)
                .build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN_VALUE, InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL_VALUE,
                build.toByteArray());
    }

    /**
     * 207.发送签到
     *
     * @param memberid 签到的人员ID,为0表示当前绑定的人员
     * @param signType 签到方式
     * @param pwd      密码
     * @param picdata  数据类型
     */
    public void sendSign(int memberid, int signType, String pwd, ByteString picdata) {
        InterfaceSignin.pbui_Type_DoMeetSignIno.Builder builder = InterfaceSignin.pbui_Type_DoMeetSignIno.newBuilder();
        builder.setMemberid(memberid);
        builder.setSigninType(signType);
        builder.setPassword(s2b(pwd));
        builder.setPsigndata(picdata);
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), builder.build().toByteArray());
        LogUtil.e(TAG, "NativeUtil.sendSign:  207.发送签到 --->>> signType: " + signType);
    }


    /**
     * 209.发起白板
     */
    public void coerceStartWhiteBoard(int operFlag, String mediaName, int operMemberid, int srcmemId, long srcwbId, List<Integer> allUserId) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl.Builder tmp3 = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl.newBuilder();
        tmp3.setOperflag(operFlag);
        tmp3.setMedianame(MyUtils.s2b(mediaName));
        tmp3.setOpermemberid(operMemberid);
        tmp3.setSrcmemid(srcmemId);
        tmp3.setSrcwbid(srcwbId);
        tmp3.addAllUserid(allUserId);
        tmp3.setOperflag(InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_REQUESTOPEN.getNumber());
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl build = tmp3.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CONTROL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.coerceStartBoard:  209.发起白板 --->>> ");
    }

    /**
     * 213.广播本身退出白板
     */
    public void broadcastStopWhiteBoard(int operflag, String medianame, int opermemberid, int srcmemid, long srcwbid, List<Integer> alluserid) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl.Builder builder = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl.newBuilder();
        builder.setOperflag(operflag);
        builder.setMedianame(MyUtils.s2b(medianame));
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.addAllUserid(alluserid);
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CONTROL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.inquiryStartWhiteBoard:  213.广播本身退出白板 --->>> ");
    }

    /**
     * 215.拒绝加入
     */
    public void rejectJoin(int opermemberid, int srcmemid, long srcwbid) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.Builder builder = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.newBuilder();
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REJECT.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.rejectJoin:  215.拒绝加入 --->>> ");
    }

    /**
     * 217.同意加入
     */
    public void agreeJoin(int opermemberid, int srcmemid, long srcwbid) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.Builder builder = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.newBuilder();
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setOpermemberid(opermemberid);
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ENTER.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.agreeJoin :  同意加入 --> ");
    }

    /**
     * 222.白板清空记录
     */
    public void whiteBoardClearRecord(int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp, int figuretype) {
        InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard.Builder builder = InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard.newBuilder();
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(figuretype);
        InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DELALL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.whiteBoardClearInform:  222.白板清空记录 --->>> ");
    }

    /**
     * 223.白板删除记录
     */
    public void whiteBoardDeleteRecord(int memberid, int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp, int figuretype) {
        InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard.Builder builder = InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard.newBuilder();
        builder.setMemberid(memberid);
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(figuretype);
        InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.whiteBoardDeleteRecord:  223.白板删除记录 --->>> ");
    }

    /**
     * 225.添加墨迹
     */
    public void addInk(int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp,
                       int figuretype, int linesize, int argb, List<PointF> allpinklist) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardInkItem.Builder builder = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardInkItem.newBuilder();
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(figuretype);
        builder.setLinesize(linesize);
        builder.setArgb(argb);
        for (int i = 0; i < allpinklist.size(); i++) {
            builder.addPinklist(allpinklist.get(i).x);
            builder.addPinklist(allpinklist.get(i).y);
        }
        LogUtil.e(TAG, "NativeUtil.addInk 2874行:   发送的xy个数--->>> " + builder.getPinklistCount());
//        builder.addAllPinklist(allpinklist);
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardInkItem build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDINK.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.addInk:  225.添加墨迹 --->>> ");
    }

    /**
     * 228.添加矩形、直线、圆形
     */
    public void addDrawFigure(int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp,
                              int type, int size, int color, List<Float> allpt) {
        InterfaceWhiteboard.pbui_Item_MeetWBRectDetail.Builder builder = InterfaceWhiteboard.pbui_Item_MeetWBRectDetail.newBuilder();
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(type);
        builder.setLinesize(size);
        builder.setArgb(color);
        builder.addAllPt(allpt);
        InterfaceWhiteboard.pbui_Item_MeetWBRectDetail build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDRECT.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.addDrawFigure:  228.添加矩形、直线、圆形 --->>> ");
    }

    /**
     * 231.添加文本
     */
    public void addText(int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp, int figuretype, int fontsize, int fontflag,
                        int argb, String fontname, float lx, float ly, String ptext) {
        InterfaceWhiteboard.pbui_Item_MeetWBTextDetail.Builder builder = InterfaceWhiteboard.pbui_Item_MeetWBTextDetail.newBuilder();
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(figuretype);
        builder.setFontsize(fontsize);
        builder.setFontflag(fontflag);
        builder.setArgb(argb);
        builder.setFontname(MyUtils.s2b(fontname));
        builder.setLx(lx);
        builder.setLy(ly);
        builder.setPtext(MyUtils.s2b(ptext));
        InterfaceWhiteboard.pbui_Item_MeetWBTextDetail build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDTEXT.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.addText:  231.添加文本 --->>> " + ptext);
    }

    /**
     * 234.添加图片
     */
    public void addPicture(int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp, int figuretype, float lx, float ly, ByteString picdata) throws InvalidProtocolBufferException {
        InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail.Builder builder = InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail.newBuilder();
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(figuretype);
        builder.setLx(lx);
        builder.setLy(ly);
        builder.setPicdata(picdata);
        LogUtil.e(TAG, "NativeUtil.addPicture 3007行:   --->>> " + figuretype);
        InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDPICTURE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.addPicture:  234.共享添加图片 --->>> ");
    }

    /**
     * 235.查询图片
     */
    public InterfaceWhiteboard.pbui_Type_MeetWBPictureDetail queryPicture(int srcmemid, int srcwbid, int figuretype, int opermemberid) throws InvalidProtocolBufferException {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardComplexQuery.Builder builder = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardComplexQuery.newBuilder();
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setFiguretype(figuretype);
        builder.setOpermemberid(opermemberid);
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardComplexQuery build = builder.build();
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), build.toByteArray());
        if (array == null) {
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryPicture:  235.查询图片 --->>> ");
        return InterfaceWhiteboard.pbui_Type_MeetWBPictureDetail.parseFrom(array);
    }


    /**
     * 238.查询会议功能
     */
    public InterfaceMeetfunction.pbui_Type_MeetFunConfigDetailInfo queryMeetFunction() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FUNCONFIG.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryMeetFunction :  查询会议功能失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryMeetFunction :  查询会议功能成功 --> ");
        return InterfaceMeetfunction.pbui_Type_MeetFunConfigDetailInfo.parseFrom(array);
    }

    /**
     * 248.停止资源操作
     */
    public void stopResourceOperate(List<Integer> resVal, List<Integer> devid) {
        InterfaceStop.pbui_Type_MeetDoStopResWork.Builder builder = InterfaceStop.pbui_Type_MeetDoStopResWork.newBuilder();
        builder.addAllRes(resVal);
        builder.addAllDeviceid(devid);
        InterfaceStop.pbui_Type_MeetDoStopResWork build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STOPPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CLOSE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "stopResourceOperate:  停止资源操作  --->>> devid=" + devid + ", resVal=" + resVal);
    }


    /**
     * 252.媒体播放操作
     */
    public void mediaPlayOperate(int mediaid, List<Integer> devIds, int pos, int res, int triggeruserval, int flag) {
        InterfacePlaymedia.pbui_Type_MeetDoMediaPlay.Builder builder = InterfacePlaymedia.pbui_Type_MeetDoMediaPlay.newBuilder();
        builder.setPlayflag(flag);
        builder.setMediaid(mediaid);
        builder.addAllDeviceid(devIds);
        builder.setPos(pos);
        builder.addRes(res);
        builder.setTriggeruserval(triggeruserval);
        InterfacePlaymedia.pbui_Type_MeetDoMediaPlay build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.mediaPlayOperate:  252.媒体播放操作 --->>> ");
    }

    /**
     * 253.设置播放位置
     */
    public void setPlayPlace(int resIndex, int pos, List<Integer> devIds, int triggeruserval, int playflag) {
        InterfacePlaymedia.pbui_Type_MeetDoSetPlayPos.Builder builder = InterfacePlaymedia.pbui_Type_MeetDoSetPlayPos.newBuilder();
        builder.setResindex(resIndex);
        builder.setPos(pos);
        builder.addAllDeviceid(devIds);
        builder.setTriggeruserval(triggeruserval);
        builder.setPlayflag(playflag);
        InterfacePlaymedia.pbui_Type_MeetDoSetPlayPos build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_MOVE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.setPlayPlace:  253.设置播放位置 --->>> ");
    }

    /**
     * 254.设置播放暂停
     */
    public void setPlayStop(int resIndex, List<Integer> devIds) {
        InterfacePlaymedia.pbui_Type_MeetDoPlayControl.Builder builder = InterfacePlaymedia.pbui_Type_MeetDoPlayControl.newBuilder();
        builder.setResindex(resIndex);
        builder.addAllDeviceid(devIds);
        InterfacePlaymedia.pbui_Type_MeetDoPlayControl build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_PAUSE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.setPlayStop:  254.设置播放暂停 --->>> ");
    }


    /**
     * 255.设置播放回复
     */
    public boolean setPlayRecover(int resIndex, List<Integer> devIds) {
        InterfacePlaymedia.pbui_Type_MeetDoPlayControl.Builder builder = InterfacePlaymedia.pbui_Type_MeetDoPlayControl.newBuilder();
        builder.setResindex(resIndex);
        builder.addAllDeviceid(devIds);
        InterfacePlaymedia.pbui_Type_MeetDoPlayControl build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_PLAY.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.setPlayRecover:  255.设置播放回复 --->>> ");
        return true;
    }

    /**
     * 264.流播放
     *
     * @param srcdeviceid    要抓取屏幕的设备ID
     * @param subid          2：抓取屏幕 3：摄像头
     * @param triggeruserval 一般写 0 InterfaceMacro.Pb_TriggerUsedef
     * @param allres         播放所用的资源
     * @param alldeviceid    通知的目标设备（进行播放的设备）
     */
    public void streamPlay(int srcdeviceid, int subid, int triggeruserval, List<Integer> allres, ArrayList<Integer> alldeviceid) {
        InterfaceStream.pbui_Type_MeetDoStreamPlay.Builder builder = InterfaceStream.pbui_Type_MeetDoStreamPlay.newBuilder();
        builder.setSrcdeviceid(srcdeviceid);
        builder.setSubid(subid);
        builder.setTriggeruserval(triggeruserval);
        builder.addAllRes(allres);
        builder.addAllDeviceid(alldeviceid);
        InterfaceStream.pbui_Type_MeetDoStreamPlay build = builder.build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STREAMPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "NativeUtil.streamPlay:  流播放 --->>> 抓取" + srcdeviceid + "的" + (subid == 2 ? "屏幕" : "摄像头") + ",资源ID：" + allres.get(0) + ",播放设备：" + alldeviceid.toString());
    }


    /**
     * 查询界面配置
     */
    public InterfaceFaceconfig.pbui_Type_FaceConfigInfo queryInterFaceConfiguration() throws InvalidProtocolBufferException {
        byte[] array = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETFACECONFIG.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "NativeUtil.queryInterFaceConfiguration :  查询界面配置失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "NativeUtil.queryInterFaceConfiguration :  查询界面配置成功 --> ");
        return InterfaceFaceconfig.pbui_Type_FaceConfigInfo.parseFrom(array);
    }


    /**
     * 保存参会人权限
     */
    public void saveMemberPermission(List<InterfaceMember.pbui_Item_MemberPermission> data) {
        InterfaceMember.pbui_Type_MemberPermission.Builder builder = InterfaceMember.pbui_Type_MemberPermission.newBuilder();
        builder.addAllItem(data);
        InterfaceMember.pbui_Type_MemberPermission build = builder.build();
        for (int i = 0; i < data.size(); i++) {
            InterfaceMember.pbui_Item_MemberPermission pbui_item_memberPermission = data.get(i);
            int memberid = pbui_item_memberPermission.getMemberid();
            int permission = pbui_item_memberPermission.getPermission();
            LogUtil.i(TAG, "saveMemberPermission :  保存参会人权限 --> memberid:" + memberid + ", permission:" + permission);
        }
        byte[] bytes = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBERPERMISSION.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SAVE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "saveMemberPermission :  保存参会人权限 --> " + (bytes == null));
    }

    /**
     * 获取指定会场ID的底图ID
     *
     * @param roomid 会场ID
     */
    public int queryMeetRoomProperty(int roomid) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_CommonQueryProperty.Builder builder = InterfaceBase.pbui_CommonQueryProperty.newBuilder();
        builder.setParameterval(roomid);
        builder.setParameterval2(roomid);
        builder.setPropertyid(InterfaceMacro.Pb_MeetRoomPropertyID.Pb_MEETROOM_PROPERTY_BGPHOTOID.getNumber());
        byte[] bytes = call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOM.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), builder.build().toByteArray());
        if (bytes != null) {
            InterfaceBase.pbui_CommonInt32uProperty pbui_commonInt32uProperty = InterfaceBase.pbui_CommonInt32uProperty.parseFrom(bytes);
            int propertyval = pbui_commonInt32uProperty.getPropertyval();
            LogUtil.e(TAG, "queryMeetRoomProperty :  获取指定会场ID的底图ID --> " + roomid + ",  propertyval:" + propertyval);
            return propertyval;
        } else {
            return 0;
        }
    }


    /**
     * 设备对讲
     *
     * @param devids
     * @param flag   Interface_device.Pb_DeviceInviteFlag
     */
    public void deviceIntercom(List<Integer> devids, int flag) {
        InterfaceDevice.pbui_Type_DoDeviceChat build = InterfaceDevice.pbui_Type_DoDeviceChat.newBuilder()
                .addAllDevid(devids)
                .setInviteflag(flag)
                .build();
        LogUtil.d(TAG, "deviceIntercom -->" + "设备对讲 flag = " + flag);
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REQUESTINVITE_VALUE, build.toByteArray());
    }

    /**
     * 回复设备对讲
     *
     * @param devid 回复的设备
     * @param flag  Interface_device.Pb_DeviceInviteFlag  =1同意，=0拒绝
     *              Pb_DEVICE_INVITECHAT_FLAG_DEAL
     */
    public void replyDeviceIntercom(int devid, int flag) {
        InterfaceDevice.pbui_Type_DeviceChat build = InterfaceDevice.pbui_Type_DeviceChat.newBuilder()
                .setOperdeviceid(devid)
                .setInviteflag(flag)
                .build();
        LogUtil.d(TAG, "deviceIntercom -->" + "回复设备对讲");
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESPONSEINVITE_VALUE, build.toByteArray());
    }

    /**
     * 停止设备对讲
     *
     * @param devid 发起端设备ID
     */
    public void stopDeviceIntercom(int devid) {
        InterfaceDevice.pbui_Type_DoExitDeviceChat build = InterfaceDevice.pbui_Type_DoExitDeviceChat.newBuilder()
                .setOperdeviceid(devid)
                .build();
        call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE, InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_EXITCHAT_VALUE, build.toByteArray());
    }

    //初始化无纸化接口
    //data 参考无纸化接口对照表
    //成功返回0 失败返回-1
    public native int Init_walletSys(byte[] data);

    //无纸化功能接口中调用
    //type 功能类型
    //method 功能类型的方法
    //data  方法需要的参数 参考无纸化接口对照表
    //成功返回对应的数组 失败返回null数组
    public native byte[] call_method(int type, int method, byte[] data);

    /**
     * 初始化桌面、摄像头采集
     *
     * @param type         流类型
     * @param channelindex 流通道索引值
     * @return 成功返回0 失败返回-1
     */
    public native int InitAndCapture(int type, int channelindex);

    public native void enablebackgroud(int type);

    //初始化桌面、摄像头采集
    //type 流类型
    //data 采集数据
    //成功返回0 失败返回-1
    public native int call(int type, int iskeyframe, long pts, byte[] data);

    public static int COLOR_FORMAT;

    //JNI获取桌面、摄像头的参数
    //type 流类型
    //oper 参数标识 用于区分获取的数据类型
    //成功返回操作属性对应的值 失败返回-1
    public int callback(int type, int oper) {
        switch (oper) {
            case 1://pixel format 编码时的颜色格式
            {
                if (type == 2) return 1;
                return COLOR_FORMAT;
            }
            case 2://width type=2 采集时的屏幕宽 ...
            {
                switch (type) {
                    case 2:
                        LogUtil.i(TAG, "callback 屏幕宽=" + App.recorderWidth);
                        return App.recorderWidth;
                    case 3:
                        LogUtil.i(TAG, "callback 摄像头宽=" + App.CameraW);
                        return App.CameraW;
                }
            }
            case 3://height type=2 采集时的屏幕高 ...
            {
                switch (type) {
                    case 2:
                        LogUtil.i(TAG, "callback 屏幕高=" + App.recorderHeight);
                        return App.recorderHeight;
                    case 3:
                        LogUtil.i(TAG, "callback 摄像头高=" + App.CameraH);
                        return App.CameraH;
                }

            }
            case 4://start capture 通知采集流 type=2采集屏幕，type=3采集摄像头
            {
                LogUtil.i("capture", "NativeUtil.callback :  通知采集流 --->>> " + type);
                if (type == SCREEN_SHOT_TYPE) {
                    Intent intent = new Intent();
                    intent.setAction(BroadCaseAction.SCREEN_SHOT);
                    intent.putExtra("type", type);
                    intent.addFlags(FLAG_DEBUG_LOG_RESOLUTION);
                    lbm.sendBroadcast(intent);
                } else {
                    EventBus.getDefault().post(new EventMessage(EventType.START_COLLECTION_STREAM_NOTIFY, type));
                }
                return 0;
            }
            case 5://stop capture 通知停止采集流 type=2屏幕，type=3摄像头
            {
                LogUtil.i("capture", "NativeUtil.callback :  通知停止采集流 --->>> " + type);
                Intent intent = new Intent();
                intent.setAction(BroadCaseAction.STOP_SCREEN_SHOT);
                intent.putExtra("type", type);
                lbm.sendBroadcast(intent);
                EventBus.getDefault().post(new EventMessage(EventType.STOP_COLLECTION_STREAM_NOTIFY, type));
                return 0;
            }
        }
        return 0;
    }

    //无纸化功能接口回调接口
    //type 功能类型
    //method 功能类型的方法
    //data  方法需要的参数 参考无纸化接口对照表
    //datalen data有数据时 datalen就有长度
    //返回0即可
    public int callback_method(int type, int method, byte[] data, int datalen) throws InvalidProtocolBufferException {
        if (type != 1) {
            LogUtil.v(CASE_TAG, "callback_method :  后台 --> type:" + type + ",method:" + method);
        }
        switch (type) {
            case 1: //3 高频回调
                InterfaceBase.pbui_Time pbui_time = InterfaceBase.pbui_Time.parseFrom(data);
                //微秒 转换成毫秒 除以 1000
                long usec = pbui_time.getUsec() / 1000;
                EventBus.getDefault().post(new EventMessage(EventType.MEET_DATE, usec));
                break;
            case 3://67 高频回调
                InterfaceDownload.pbui_Type_DownloadCb pbui_type_downloadCb = InterfaceDownload.pbui_Type_DownloadCb.parseFrom(data);
                int mediaid = pbui_type_downloadCb.getMediaid();
                int progress = pbui_type_downloadCb.getProgress();
                int nstate = pbui_type_downloadCb.getNstate();//1 下载中, 2 下载结束, 3 下载错误, 4 下载退出, 5 下载释放资料
//                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  67 下载进度回调  -- 高频回调 --->>> " + progress + "   mediaid: " + mediaid);
//                if (nstate == InterfaceMacro.Pb_Download_State.Pb_STATE_MEDIA_DOWNLOAD_WORKING.getNumber()) {
                //设置只在下载中发送通知,progress第一次100的时候也是下载中状态
                EventBus.getDefault().post(new EventMessage(EventType.DOWNLOAD_PROGRESS, pbui_type_downloadCb));
//                }
                break;
            case 5://68 高频回调
                EventBus.getDefault().post(new EventMessage(EventType.PLAY_PROGRESS_NOTIFY, InterfacePlaymedia.pbui_Type_PlayPosCb.parseFrom(data)));
                break;
            case 4://72 高频回调
                InterfaceUpload.pbui_TypeUploadPosCb object = InterfaceUpload.pbui_TypeUploadPosCb.parseFrom(data);
                EventBus.getDefault().post(new EventMessage(EventType.Upload_Progress, object));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  72 上传进度通知 -- 高频回调 --->>> " + object.getPer());
                break;
            case 2: //  平台初始化完毕
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON.getNumber()) {
                    LogUtil.i(CASE_TAG, "callback_method -->平台初始化失败通知");
//                    InterfaceBase.pbui_Type_LogonError pbui_type_logonError = InterfaceBase.pbui_Type_LogonError.parseFrom(data);
//                    EventBus.getDefault().post(new EventMessage(EventType.platform_initialization_failed, pbui_type_logonError));
                    ToastUtils.showShort(R.string.Platform_initialization_failed);
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    InterfaceBase.pbui_Ready pbui_ready = InterfaceBase.pbui_Ready.parseFrom(data);
                    int areaid = pbui_ready.getAreaid();
                    Values.isOver = true;
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  平台初始化完毕 --->>>  type   " + type);
                    EventBus.getDefault().post(new EventMessage(EventType.PLATFORM_INITIALIZATION));
                }
                break;
            case 6: //5   设备寄存器变更通知
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    if (datalen <= 0) {
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method :  datalen 小于 0 --> ");
                        break;
                    }
                    InterfaceDevice.pbui_Type_MeetDeviceBaseInfo object1 = InterfaceDevice.pbui_Type_MeetDeviceBaseInfo.parseFrom(data);
                    EventBus.getDefault().post(new EventMessage(EventType.DEV_REGISTER_INFORM, object1));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  5 设备寄存器变更通知 --->>> 设备ID:" + object1.getDeviceid() + "  寄存器ID:"
                            + object1.getAttribid() + " SerializedSize: " + object1.getSerializedSize());
                }
                break;
            case 7://会议网页
                InterfaceBase.pbui_meetUrl pbui_meetUrl1 = InterfaceBase.pbui_meetUrl.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  40 网页变更通知 --->>> ");
                EventBus.getDefault().post(new EventMessage(EventType.NETWEB_INFORM, pbui_meetUrl1));
                break;
            case 8://管理员
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.ADMIN_NOTIFI_INFORM, InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  51 管理员变更通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.LOGIN_BACK, InterfaceAdmin.pbui_Type_AdminLogonStatus.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  58 管理员登陆返回 --->>> ");
                }
                break;
            case 9://设备控制 更换公司Logo
                EventBus.getDefault().post(new EventMessage(EventType.DEVICE_CONTROL_INFORM, InterfaceDevice.pbui_Type_DeviceControl.parseFrom(data)));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  63 Logo变更通知 --->>> ");
                break;
            case 10://常用人员信息
                InterfaceBase.pbui_MeetNotifyMsg object6 = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                EventBus.getDefault().post(new EventMessage(EventType.QUERY_COMMON_PEOPLE, object6));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  73 常用人员变更通知 --->>> ");
                break;
            case 11://参会人员信息
                InterfaceBase.pbui_MeetNotifyMsg object7 = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                EventBus.getDefault().post(new EventMessage(EventType.MEMBER_CHANGE_INFORM, object7));
                int id2 = object7.getId();
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  90 参会人员变更通知 --->>> ");
                break;
            case 12://参会人员分组
                InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  101 参会人员分组变更通知 --->>> ");
                break;
            case 14://设备会议信息
                EventBus.getDefault().post(new EventMessage(EventType.DEVMEETINFO_CHANGE_INFORM));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  109 设备会议信息变更通知 --->>> ");
                break;
            case 15://111
                InterfaceBase.pbui_MeetNotifyMsg object3 = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                int opermethod1 = object3.getOpermethod();
                int id1 = object3.getId();
                EventBus.getDefault().post(new EventMessage(EventType.PLACEINFO_CHANGE_INFORM, object3));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  111 会场信息变更通知 --->>>opermethod= " + opermethod1 + ",id= " + id1);
                break;
            case 16:
                InterfaceBase.pbui_MeetNotifyMsgForDouble object2 = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(data);
                int opermethod = object2.getOpermethod();
                int id = object2.getId();
                int subid = object2.getSubid();
                EventBus.getDefault().post(new EventMessage(EventType.SIGNIN_SEAT_INFORM, object2));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  119 会场设备信息变更通知 --->>>opermethod= " + opermethod + ", id= " + id + ",subid= " + subid);
                if (method == 44) {// 会场设备排位详细信息
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method :  会场设备排位详细信息 --> ");
                } else {
                    EventBus.getDefault().post(new EventMessage(EventType.PLACE_DEVINFO_CHANGEINFORM, object2));
                }
                break;
            case 17://257
                EventBus.getDefault().post(new EventMessage(EventType.PUSH_FILE_INFORM, InterfacePlaymedia.pbui_Type_FilePush.parseFrom(data)));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  257 文件推送通知 --->>> ");
                break;
            case 18://259
//                InterfaceStream.pbui_Type_ReqStreamPush.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  259 请求播放流通道通知 --->>> ");
                break;
            case 19://261
//                InterfaceStream.pbui_Type_StreamPush.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  261 推送流通知 --->>> ");
                break;
            case 20://127
                InterfaceBase.pbui_MeetNotifyMsg object9 = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                EventBus.getDefault().post(new EventMessage(EventType.MEETINFO_CHANGE_INFORM, object9));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  127 会议信息变更通知 --->>>Opermethod= " + object9.getOpermethod() + ", id= " + object9.getId());
                break;
            case 21://33
                InterfaceBase.pbui_MeetNotifyMsg object4 = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                EventBus.getDefault().post(new EventMessage(EventType.AGENDA_CHANGE_INFO, object4));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  33 议程变更通知 --->>> id= " + object4.getId() + " , Opermethod= " + object4.getOpermethod());
                break;
            case 22://43-50
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.NOTICE_CHANGE_INFO, InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:   公告变更通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_PUBLIST.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.publish_notice, InterfaceBullet.pbui_BulletDetailInfo.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:   发布公告通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_STOP.getNumber()) {
                    LogUtil.e(CASE_TAG, "callback_method : 停止公告通知 data --> " + data);
//                    if(data!=null){
//                        InterfaceBullet.pbui_Type_StopBulletMsg pbui_type_stopBulletMsg = InterfaceBullet.pbui_Type_StopBulletMsg.parseFrom(data);
//                    }else {
//
//                    }
                    EventBus.getDefault().post(new EventMessage(EventType.CLOSE_NOTICE_INFORM));
                    LogUtil.e(CASE_TAG, "callback_method :  停止公告通知 --> ");
                }
                break;
            case 23://135
                EventBus.getDefault().post(new EventMessage(EventType.MEETDIR_CHANGE_INFORM, InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data)));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  135 会议目录变更通知 --->>> ");
                break;
            case 24://142-278
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber()) {
                    InterfaceFile.pbui_Type_MeetNewRecordFile.parseFrom(data);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  278 有新的录音文件媒体文件通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.MEETDIR_FILE_CHANGE_INFORM, InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  142 会议目录文件变更通知 --->>> ");
                }
                break;
            case 25://153
//                InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  153 会议目录权限变更通知 --->>> ");
                break;
            case 26://171
                EventBus.getDefault().post(new EventMessage(EventType.Meet_vedio_changeInform, InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data)));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  171 会议视频变更通知 --->>> ");
                break;
            case 27://177
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  177 会议双屏显示信息变更通知 --->>> ");
                break;
            case 28://180
                InterfaceBase.pbui_MeetNotifyMsg object10 = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                EventBus.getDefault().post(new EventMessage(EventType.MeetSeat_Change_Inform, object10));
                int id4 = object10.getId();
                int opermethod2 = object10.getOpermethod();
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  180 会议排位变更通知 --->>> opermethod= " + opermethod2 + ", id=" + id4);
                break;
            case 29://184
                EventBus.getDefault().post(new EventMessage(EventType.meet_chat_info, InterfaceIM.pbui_Type_MeetIM.parseFrom(data)));
//                EventBus.getDefault().post(new EventMessage(EventType.Receive_MeetChat_Info,pbui_type_meetIM));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  184 收到新的会议交流信息 --->>> ");
                break;
            case 30://188
                EventBus.getDefault().post(new EventMessage(EventType.newVote_launch_inform, InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data)));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  188 有新的投票发起通知 --->>> ");
                break;
            case 31://198
                EventBus.getDefault().post(new EventMessage(EventType.Vote_Change_Inform, InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data)));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  198 投票变更通知 --->>> ");
                break;
            case 32://202
                InterfaceBase.pbui_MeetNotifyMsg object5 = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                EventBus.getDefault().post(new EventMessage(EventType.VoteMember_ChangeInform, object5));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  202 投票提交人变更通知 --->>> ");
                break;
            case 33://59
                EventBus.getDefault().post(new EventMessage(EventType.MEETADMIN_PLACE_NOTIFIINFROM, InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data)));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  59 会议管理员控制的会场变更通知 --->>> ");
                break;
            case 34://205
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.SIGN_CHANGE_INFORM, InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  205 签到变更通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.SIGNIN_BACK, InterfaceBase.pbui_Type_MeetDBServerOperError.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method :  签到密码返回 --> ");
                }
                break;
            case 35://214-233
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ASK.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.OPEN_BOARD, InterfaceWhiteboard.pbui_Type_MeetStartWhiteBoard.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  214 收到白板打开操作 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REJECT.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.REJECT_JOIN, InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  216 拒绝加入通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ENTER.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.AGREED_JOIN, InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  218 同意加入通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_EXIT.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.EXIT_WHITE_BOARD, InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  219 参会人员退出白板通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CLEAR.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.WHITEBOARD_EMPTY_RECORDINFORM, InterfaceWhiteboard.pbui_Type_MeetClearWhiteBoard.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  220 白板清空记录通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.WHITEBROADE_DELETE_RECOREINFORM, InterfaceWhiteboard.pbui_Type_MeetClearWhiteBoard.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  221 白板删除记录通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDINK.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.ADD_INK_INFORM, InterfaceWhiteboard.pbui_Type_MeetWhiteBoardInkItem.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  224 添加墨迹通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDRECT.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.ADD_DRAW_INFORM, InterfaceWhiteboard.pbui_Item_MeetWBRectDetail.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  227 添加矩形、直线、圆形通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDTEXT.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.ADD_DRAW_TEXT, InterfaceWhiteboard.pbui_Item_MeetWBTextDetail.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  230 添加文本通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDPICTURE.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.ADD_PIC_INFORM, InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  233 添加图片通知 --->>> ");
                }
                break;
            case 36://236
                EventBus.getDefault().post(new EventMessage(EventType.MEET_FUNCTION_CHANGEINFO, InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data)));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  236 会议功能变更通知 --->>> ");
                break;
            case 37://100
                InterfaceBase.pbui_MeetNotifyMsg object8 = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                EventBus.getDefault().post(new EventMessage(EventType.MEMBER_PERMISSION_INFORM, object8));
                int id3 = object8.getId();
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  100 参会人员权限变更通知 --->>> ");
                break;
            case 38://79
                InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  79 常用人员分组变更通知 --->>> ");
                break;
            case 39://84-106 // TODO: 2017/12/29 ？？？两个回调一样 
                InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  84 常用人员分组人员变更通知 --->>> ");
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(data);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  106 参会人员分组人员变更通知 --->>> ");
                }
                break;
            case 40://242
//                InterfaceStream.pbui_Type_MeetScreenKeyBoardControl.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  242 鼠标操作通知 --->>> ");
                break;
            case 41://244
//                InterfaceStream.pbui_Type_MeetScreenKeyBoardControl.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  244 键盘操作通知 --->>> ");
                break;
            case 42://10.界面状态变更通知
                if (data.length > 0) {
                    InterfaceDevice.pbui_MeetDeviceMeetStatus object1 = InterfaceDevice.pbui_MeetDeviceMeetStatus.parseFrom(data);
                    EventBus.getDefault().post(new EventMessage(EventType.FACESTATUS_CHANGE_INFORM, object1));
                    int deviceid = object1.getDeviceid();
                    int facestatus = object1.getFacestatus();
                    int meetingid = object1.getMeetingid();
                    int memberid = object1.getMemberid();
                    int oldfacestatus = object1.getOldfacestatus();
                    InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = queryDevInfoById(deviceid);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  界面状态变更通知 --->>>" +
                            "deviceid=" + deviceid + ",facestatus= " + facestatus + ", oldfacestatus= "
                            + oldfacestatus + ", meetingid= " + meetingid + ", memberid= " + memberid);
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE:
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ZOOM.getNumber()) {
                    InterfaceDevice.pbui_MeetZoomResWin.parseFrom(data);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  15 窗口缩放变更 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ENTER.getNumber()) {
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  17 辅助签到变更通知 --->>> 直接调用辅助签到接口");
                    EventBus.getDefault().post(new EventMessage(EventType.SIGN_EVENT));
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_BROADCAST.getNumber()) {
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  21 收到参会人员数量和签到数量广播通知 --->>> ");
                    EventBus.getDefault().post(new EventMessage(EventType.signin_count, InterfaceDevice.pbui_Type_MeetMemberCastInfo.parseFrom(data)));
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REQUESTTOMANAGE.getNumber()) {
                    InterfaceDevice.pbui_Type_MeetRequestManageNotify.parseFrom(data);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  24 收到请求成为管理员请求 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESPONSETOMANAGE.getNumber()) {
                    InterfaceDevice.pbui_Type_MeetRequestManageResponse.parseFrom(data);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  26 收到请求成为管理员回复 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REQUESTPRIVELIGE.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.RECEIVE_PERMISSION_REQUEST, InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  28 收到请求参会人员权限请求 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESPONSEPRIVELIGE.getNumber()) {
                    EventBus.getDefault().post(new EventMessage(EventType.RECEIVE_REQUEST_REPLY, InterfaceDevice.pbui_Type_MeetRequestPrivilegeResponse.parseFrom(data)));
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  30 收到参会人员权限请求回复 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_EXITCHAT.getNumber()) {
                    LogUtil.e(CASE_TAG, "callback_method 退出对讲通知");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REQUESTINVITE.getNumber()) {
                    //收到设备对讲的通知
//                    InterfaceDevice.pbui_Type_ExitDeviceChat.parseFrom(data))
                    InterfaceDevice.pbui_Type_DeviceChat.parseFrom(data);
                }
                break;
            case 44://250
                InterfaceBase.pbui_Type_MeetDBServerOperError error = InterfaceBase.pbui_Type_MeetDBServerOperError.parseFrom(data);
                int status = error.getStatus();
                int method1 = error.getMethod();
                int type1 = error.getType();
                switch (status) {
                    case 0://多条查询记录
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method: 250 数据后台回复的信息 0 --->>> 多条查询记录 , type=" + type1 + ",method=" + method1);
                        break;
                    case 1://单条查询记录
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method: 250 数据后台回复的信息 1 --->>> 单条查询记录 , type=" + type1 + ",method=" + method1);
                        break;
                    case 2://无返回记录
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method: 250 数据后台回复的信息 2 --->>> 无返回记录 , type=" + type1 + ",method=" + method1);
                        break;
                    case 3://操作成功
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method: 250 数据后台回复的信息 3 --->>> 操作成功 , type=" + type1 + ",method=" + method1);
                        break;
                    case 4://请求失败
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method: 250 数据后台回复的信息 4 --->>> 请求失败 , type=" + type1 + ",method=" + method1);
                        break;
                    case 5://数据库异常
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method: 250 数据后台回复的信息 5 --->>> 数据库异常 , type=" + type1 + ",method=" + method1);
                        break;
                    case 6://服务器异常
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method: 250 数据后台回复的信息 6 --->>> 服务器异常 , type=" + type1 + ",method=" + method1);
                        break;
                    case 7://权限限制
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method: 250 数据后台回复的信息 7 --->>> 权限限制 , type=" + type1 + ",method=" + method1);
                        break;
                    case 8://密码错误
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method: 250 数据后台回复的信息 8 --->>> 密码错误 , type=" + type1 + ",method=" + method1);
                        break;
                    case 9://创建会议有冲突
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method: 250 数据后台回复的信息 9 --->>> 创建会议有冲突 , type=" + type1 + ",method=" + method1);
                        break;
                }
                break;
            case 45:
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  媒体播放通知 --->>> ");
                InterfacePlaymedia.pbui_Type_MeetMediaPlay object11 = InterfacePlaymedia.pbui_Type_MeetMediaPlay.parseFrom(data);
                EventBus.getDefault().post(new EventMessage(EventType.MEDIA_PLAY_INFORM, object11));
                break;
            case 46:
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  263 流播放通知 --->>> ");
                EventBus.getDefault().post(new EventMessage(EventType.PLAY_STREAM_NOTIFY, InterfaceStream.pbui_Type_MeetStreamPlay.parseFrom(data)));
                break;
            case 47:
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CLOSE.getNumber()) {
                    if (datalen > 0) {
                        InterfaceStop.pbui_Type_MeetStopResWork object1 = InterfaceStop.pbui_Type_MeetStopResWork.parseFrom(data);
                        EventBus.getDefault().post(new EventMessage(EventType.STOP_STRAM_INFORM, object1));
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  246 停止资源通知 --->>> ");
                    } else {
                        LogUtil.e(CASE_TAG, "NativeUtil.callback_method :  停止资源通知数组长度为0 --> ");
                    }
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    InterfaceStop.pbui_Type_MeetStopPlay pbui_type_meetStopPlay = InterfaceStop.pbui_Type_MeetStopPlay.parseFrom(data);
                    int createdeviceid = pbui_type_meetStopPlay.getCreatedeviceid();
                    int res = pbui_type_meetStopPlay.getRes();
                    int triggerid = pbui_type_meetStopPlay.getTriggerid();
                    LogUtil.e(CASE_TAG, "callback_method :  停止播放通知 --> createdeviceid：" + createdeviceid
                            + ", res:" + res + ", triggerid:" + triggerid + ", 本机ID：" + Values.localDevId);
//                    /** **** **  自己发送的停止通知,自己不需要处理  ** **** **/
//                    if (pbui_type_meetStopPlay.getCreatedeviceid() != Values.localDevId) {
                    EventBus.getDefault().post(new EventMessage(EventType.STOP_PLAY, pbui_type_meetStopPlay));
//                    }
                }
                break;
            case 48://239
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
//                    InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  239 参会人员白板颜色变更通知 --->>> ");
                }
                break;
            case 51://266-268
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    InterfaceStatistic.pbui_Type_MeetStatisticInfo.parseFrom(data);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  266 返回查询会议统计通知 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ASK.getNumber()) {
                    InterfaceStatistic.pbui_Type_MeetQuarterStatisticInfo.parseFrom(data);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  268 返回按时间段查询的会议统计通知 --->>> ");
                }
                break;
            case 52://270
                EventBus.getDefault().post(new EventMessage(EventType.ICC_changed_inform, InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data)));
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  270 界面配置变更通知 --->>> ");
                break;
            case 54://154
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber()) {
                    InterfaceFile.pbui_Type_MeetFileScore.parseFrom(data);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  154 返回查询会议文件评分 --->>> ");
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ASK.getNumber()) {
                    InterfaceFile.pbui_Type_QueryAverageFileScore.parseFrom(data);
                    LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  155 返回查询会议文件评分的平均分 --->>> ");
                }
                break;
            case 55://161
//                InterfaceFile.pbui_Type_MeetingFileEvaluate.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  161 返回查询会议文件评价  --->>> ");
                break;
            case 56://165
//                InterfaceMeet.pbui_Type_MeetingMeetEvaluate.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  165 返回查询会议评价 --->>> ");
                break;
            case 57://169
//                InterfaceSystemlog.pbui_Type_MeetingMeetSystemLog.parseFrom(data);
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  169 返回查询管理员日志 --->>> ");
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEVALIDATE_VALUE://平台登陆验证返回
                LogUtil.e(CASE_TAG, "NativeUtil.callback_method:  平台登陆验证返回 --->>> " + method);
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    InterfaceBase.pbui_Type_DeviceValidate deviceValidate = InterfaceBase.pbui_Type_DeviceValidate.parseFrom(data);
                    EventBus.getDefault().post(new EventMessage(EventType.platform_initialization_failed, deviceValidate));
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_UPDATE_VALUE:
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_UPDATE.getNumber()) {
                    LogUtil.e(CASE_TAG, "callback_method :  软件升级通知 --> ");
//                    InterfaceBase.pbui_Type_MeetUpdateNotify updateNotify = InterfaceBase.pbui_Type_MeetUpdateNotify.parseFrom(data);
//                    EventBus.getDefault().post(new EventMessage(EventType.update_client, updateNotify));
                    Intent intent = new Intent();
                    intent.setAction(BroadCaseAction.UPDATE_APP);
                    intent.putExtra("type", type);
                    intent.putExtra("data", data);
                    lbm.sendBroadcast(intent);
                }
                break;
            default:
                LogUtil.i("NativeUtil", "callback_method: type: " + type + ", method: " + method);
                break;
        }
        return 0;
    }

    public void error_ret(int type, int method, int ret) {
        LogUtil.v(TAG, "error_ret type=" + type + ",method=" + method + "ret=" + ret);
    }

    public native byte[] NV21ToI420(byte[] data, int w, int h);

    public native byte[] NV21ToNV12(byte[] data, int w, int h);

    public native byte[] YV12ToNV12(byte[] data, int w, int h);

    /**
     * 解码后的视频数据,这里对应的是YUV2420每个平面的大小,如{1920, 960, 960}
     */
    public int callback_yuvdisplay(int res, int w, int h, byte[] y, byte[] u, byte[] v) {
        EventBus.getDefault().post(new EventMessage(CALLBACK_YUVDISPLAY, res, w, h, y, u, v));
        LogUtil.e(CASE_TAG, "callback_yuvdisplay :   --> res:" + res + "\n w:" + w + ",h:" + h
                + "\n y.length:" + y.length + ", \n u.length:" + u.length + ",\n v.length:" + v.length);
        return 0;
    }

    /**
     * @param res     播放资源ID
     * @param codecid 解码器类型 (h264=27,h265=173,mpeg4=12,vp8=139,vp9=167)
     * @param w       视屏的宽
     * @param h       视屏的高
     * @param packet  数据包
     * @param pts     时间戳
     */
    public int callback_videodecode(int isKeyframe, int res, int codecid, int w, int h, byte[] packet, long pts, byte[] codecdata) {
//        LogUtil.d("callBackData", "callback_videodecode -->" + "收到后台播放数据");
        LogUtil.v(TAG, "callback_videodecode res:" + res + ",size:" + packet.length + ", pts:" + (pts / 1000));
        EventBus.getDefault().post(new EventMessage(EventType.CALLBACK_VIDEO_DECODE,
                res, codecid, w, h, packet, codecdata, pts, isKeyframe));
        return 0;
    }

    public int callback_startdisplay(int res) {
        LogUtil.e(CASE_TAG, "callback_startdisplay :  res --> " + res);
        EventBus.getDefault().post(new EventMessage(EventType.CALLBACK_STARTDISPLAY, res));
        return 0;
    }

    public int callback_stopdisplay(int res) {
        LogUtil.e(CASE_TAG, "callback_stopdisplay :  停止指定资源ID的播放器 --> " + res);
        EventBus.getDefault().post(new EventMessage(EventType.CALLBACK_STOPDISPLAY, res));
        return 0;
    }

}
