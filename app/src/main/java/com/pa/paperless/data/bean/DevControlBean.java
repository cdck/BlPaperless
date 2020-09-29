package com.pa.paperless.data.bean;

import com.mogujie.tt.protobuf.InterfaceDevice;

/**
 * Created by xlk on 2018/10/31.
 */

public class DevControlBean {
    InterfaceDevice.pbui_Item_DeviceDetailInfo device;
    String memberName;
    int role; //Pb_MeetMemberRole

    public DevControlBean(InterfaceDevice.pbui_Item_DeviceDetailInfo device, String memberName, int role) {
        this.device = device;
        this.memberName = memberName;
        this.role = role;
    }

    public DevControlBean(InterfaceDevice.pbui_Item_DeviceDetailInfo device) {
        this.device = device;
    }

    public InterfaceDevice.pbui_Item_DeviceDetailInfo getDevice() {
        return device;
    }

    public void setDevice(InterfaceDevice.pbui_Item_DeviceDetailInfo device) {
        this.device = device;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
