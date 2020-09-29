package com.pa.paperless.data.bean;


import com.mogujie.tt.protobuf.InterfaceMember;

/**
 * Created by Administrator on 2018/3/8.
 */

public class DevMember {
    InterfaceMember.pbui_Item_MemberDetailInfo memberInfos;
    int devId;

    public InterfaceMember.pbui_Item_MemberDetailInfo getMemberInfos() {
        return memberInfos;
    }

    public void setMemberInfos(InterfaceMember.pbui_Item_MemberDetailInfo memberInfos) {
        this.memberInfos = memberInfos;
    }

    public int getDevId() {
        return devId;
    }

    public void setDevId(int devId) {
        this.devId = devId;
    }

    public DevMember(InterfaceMember.pbui_Item_MemberDetailInfo memberInfos, int devId) {

        this.memberInfos = memberInfos;
        this.devId = devId;
    }
}
