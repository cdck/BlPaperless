package com.pa.paperless.data.bean;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceVideo;

/**
 * Created by Administrator on 2018/5/7.
 */

public class VideoInfo {
    InterfaceVideo.pbui_Item_MeetVideoDetailInfo VideoInfo;
    InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo;

    public VideoInfo(InterfaceVideo.pbui_Item_MeetVideoDetailInfo videoInfo, InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo) {
        VideoInfo = videoInfo;
        this.deviceDetailInfo = deviceDetailInfo;
    }

    public InterfaceVideo.pbui_Item_MeetVideoDetailInfo getVideoInfo() {
        return VideoInfo;
    }

    public InterfaceDevice.pbui_Item_DeviceDetailInfo getDeviceDetailInfo() {
        return deviceDetailInfo;
    }

    public String getName() {
        return VideoInfo.getDevicename().toStringUtf8() + "-" + VideoInfo.getName().toStringUtf8();
    }
}
