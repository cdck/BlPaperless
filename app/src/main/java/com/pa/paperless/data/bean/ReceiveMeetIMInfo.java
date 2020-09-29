package com.pa.paperless.data.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/1/22.
 * 接收到的会议交流信息
 */

public class ReceiveMeetIMInfo {
    int msgtype;//消息类型 参见 Pb_MeetIMMSG_TYPE
    int role;//发送者角色 参见 Pb_MeetMemberRole
    int memberid;//发送者ID
    String msg;//消息文本 长度 Pb_MEETIM_CHAR_MSG_MAXLEN
    long utcsecond;//发送UTC时间 单位:秒
    boolean type; // true：接收  false：发送
    String meetName ;//会议名称
    String roomName ;//会议室名
    String memberName ;//人员名称
    String seateName;//席位名
    //自定义
    List<String> names;//要发送给的人员名称集合

    public ReceiveMeetIMInfo() {

    }

    //发送时需要的参数
    public ReceiveMeetIMInfo(String msg, List<String> names) {
        this.msg = msg;
        this.names = names;
    }
    //接收到的数据
    public ReceiveMeetIMInfo(int msgtype, int role, int memberid, String msg, long utcsecond, boolean type,
                             String meetName, String roomName, String memberName, String seateName) {
        this.msgtype = msgtype;
        this.role = role;
        this.memberid = memberid;
        this.msg = msg;
        this.utcsecond = utcsecond;
        this.type = type;
        this.meetName = meetName;
        this.roomName = roomName;
        this.memberName = memberName;
        this.seateName = seateName;
    }

    public int getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(int msgtype) {
        this.msgtype = msgtype;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getMemberid() {
        return memberid;
    }

    public void setMemberid(int memberid) {
        this.memberid = memberid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getUtcsecond() {
        return utcsecond;
    }

    public void setUtcsecond(long utcsecond) {
        this.utcsecond = utcsecond;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }


    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public String getMeetName() {
        return meetName;
    }

    public void setMeetName(String meetName) {
        this.meetName = meetName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getSeateName() {
        return seateName;
    }

    public void setSeateName(String seateName) {
        this.seateName = seateName;
    }
}
