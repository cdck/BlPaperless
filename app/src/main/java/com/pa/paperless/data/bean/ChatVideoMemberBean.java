package com.pa.paperless.data.bean;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMember;

/**
 * @author by xlk
 * @date 2020/8/12 11:00
 * @desc
 */
public class ChatVideoMemberBean {

    InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo;
    InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo;

    public ChatVideoMemberBean(InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo, InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo) {
        this.deviceDetailInfo = deviceDetailInfo;
        this.memberDetailInfo = memberDetailInfo;
    }

    public InterfaceDevice.pbui_Item_DeviceDetailInfo getDeviceDetailInfo() {
        return deviceDetailInfo;
    }

    public void setDeviceDetailInfo(InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo) {
        this.deviceDetailInfo = deviceDetailInfo;
    }

    public InterfaceMember.pbui_Item_MemberDetailInfo getMemberDetailInfo() {
        return memberDetailInfo;
    }

    public void setMemberDetailInfo(InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo) {
        this.memberDetailInfo = memberDetailInfo;
    }
}
