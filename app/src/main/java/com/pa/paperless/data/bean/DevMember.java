package com.pa.paperless.data.bean;


import com.mogujie.tt.protobuf.InterfaceMember;

/**
 * Created by Administrator on 2018/3/8.
 */

public class DevMember {
    InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo;
    int devId;

    public InterfaceMember.pbui_Item_MemberDetailInfo getMemberDetailInfo() {
        return memberDetailInfo;
    }

    public int getDevId() {
        return devId;
    }

    public DevMember(InterfaceMember.pbui_Item_MemberDetailInfo memberInfos, int devId) {
        this.memberDetailInfo = memberInfos;
        this.devId = devId;
    }

    @Override
    public String toString() {
        return "DevMember{" +
                "参会人名称=" + memberDetailInfo.getName().toStringUtf8() +
                ",参会人ID=" + memberDetailInfo.getPersonid() +
                ",设备ID=" + devId +
                '}';
    }
}
