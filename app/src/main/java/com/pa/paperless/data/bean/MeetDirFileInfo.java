package com.pa.paperless.data.bean;

/**
 * Created by Administrator on 2018/1/18.
 * 会议目录文件  文件
 */

public class MeetDirFileInfo {
    int dirid;
    int mediaId;
    String fileName;
    int uploaderid;
    int uploader_role;
    long mstime;
    long size;
    int attrib;
    int filepos;
    String uploader_name;

    public int getDirid() {
        return dirid;
    }

    public void setDirid(int dirid) {
        this.dirid = dirid;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getUploaderid() {
        return uploaderid;
    }

    public void setUploaderid(int uploaderid) {
        this.uploaderid = uploaderid;
    }

    public int getUploader_role() {
        return uploader_role;
    }

    public void setUploader_role(int uploader_role) {
        this.uploader_role = uploader_role;
    }

    public long getMstime() {
        return mstime;
    }

    public void setMstime(long mstime) {
        this.mstime = mstime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getAttrib() {
        return attrib;
    }

    public void setAttrib(int attrib) {
        this.attrib = attrib;
    }

    public int getFilepos() {
        return filepos;
    }

    public void setFilepos(int filepos) {
        this.filepos = filepos;
    }

    public String getUploader_name() {
        return uploader_name;
    }

    public void setUploader_name(String uploader_name) {
        this.uploader_name = uploader_name;
    }

    public MeetDirFileInfo(int dirid, int mediaId, String fileName, int uploaderid, int uploader_role, long mstime, long size, int attrib, int filepos, String uploader_name) {
        this.dirid = dirid;
        this.mediaId = mediaId;
        this.fileName = fileName;
        this.uploaderid = uploaderid;
        this.uploader_role = uploader_role;
        this.mstime = mstime;
        this.size = size;
        this.attrib = attrib;
        this.filepos = filepos;
        this.uploader_name = uploader_name;
    }
}
