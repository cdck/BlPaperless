package com.pa.paperless.utils;

import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceIM;
import com.pa.paperless.data.bean.MeetDirFileInfo;
import com.pa.paperless.data.bean.ReceiveMeetIMInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/15.
 * 数据处理工具类
 */

public class Dispose {
    public static final String TAG = "Dispose-->";

    /**
     * 会议聊天信息
     *
     * @param o
     * @return
     */
    public static List<ReceiveMeetIMInfo> ReceiveMeetIMinfo(InterfaceIM.pbui_Type_MeetIM o) {
        List<ReceiveMeetIMInfo> rReceiveMeetIMInfo = new ArrayList<>();
        int msgtype = o.getMsgtype();
        //秒 转成毫秒
        long utcsecond = o.getUtcsecond() * 1000;
        int role = o.getRole();
        int memberid = o.getMemberid();
        String msg = o.getMsg().toStringUtf8();
        ReceiveMeetIMInfo meetIMInfo = new ReceiveMeetIMInfo(msgtype, role, memberid, msg, utcsecond, true,
                o.getMeetname().toStringUtf8(), o.getRoomname().toStringUtf8(),
                o.getMembername().toStringUtf8(), o.getSeatename().toStringUtf8());
        rReceiveMeetIMInfo.add(meetIMInfo);
        return rReceiveMeetIMInfo;
    }

    /**
     * 会议目录文件
     *
     * @param o
     * @return
     */
    public static List<MeetDirFileInfo> MeetDirFile(InterfaceFile.pbui_Type_MeetDirFileDetailInfo o) {
        List<MeetDirFileInfo> rMeetDirFileInfo = new ArrayList<>();
        for (int i = 0; i < o.getItemCount(); i++) {
            int dirid = o.getDirid();
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = o.getItem(i);
            int mediaid = item.getMediaid();
            String name = item.getName().toStringUtf8();
            int uploaderid = item.getUploaderid();
            int uploaderRole = item.getUploaderRole();
            int mstime = item.getMstime();
            long size = item.getSize();
            int attrib = item.getAttrib();
            int filepos = item.getFilepos();
            String uploader_name = item.getUploaderName().toStringUtf8();
            rMeetDirFileInfo.add(new MeetDirFileInfo(dirid, mediaid, name, uploaderid, uploaderRole, mstime, size, attrib, filepos, uploader_name));
        }
        return rMeetDirFileInfo;
    }

}
