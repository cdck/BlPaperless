package com.pa.paperless.data.constant;

import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author by xlk
 * @date 2020/7/22 11:21
 * @desc 存放所有的static值
 */
public class Values {
    public static int isOnline;//=0无网络，=1有网络
    public static int localPermission;//本机权限
    public static boolean hasAllPermissions;//本机是否拥有全部权限（管理员、秘书、主讲人 为true）
    public static int localDevId;//本机设备ID
    public static String localDevName;//本机设备名称
    public static int localMemberId;//本机参会人ID
    public static String localMemberName;//本机参会人名称
    public static String meetingName;//本机会议名称
    public static int meetingId;//会议ID
    public static int roomId;//会场ID
    //存放是秘书身份的人员
    public static final ConcurrentHashMap<Integer, InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> filterMembers = new ConcurrentHashMap<>();
    public static final List<InterfaceMember.pbui_Item_MemberPermission> mPermissionsList = new ArrayList<>();//所有的参会人权限集合
    public static boolean isOpenFile;//点击查看文件按钮时为true,为了实现用户点击下载时,只下载,点击查看时下载完后就打开文件
    public static boolean isOver;//平台初始化是否完成
    public static int operid;
    public static boolean can_open_camera = true;
    public static boolean videoIsShowing = false;
    public static boolean isMandatory;//当前播放的流是否是强制性的，如果是强制性的则进入播放界面后不能返回
}
