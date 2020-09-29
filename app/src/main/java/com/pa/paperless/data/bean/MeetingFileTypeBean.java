package com.pa.paperless.data.bean;

/**
 * Created by Administrator on 2017/11/2.
 */

public class MeetingFileTypeBean {
    int dirId;  //目录ID
    int parentid;  //父级ID
    String fileName;//文件的名称
    int fileCount;//有多少个item就有多少个文件

    public MeetingFileTypeBean(int dirId, int parentid, String fileName, int fileCount) {
        this.dirId = dirId;
        this.parentid = parentid;
        this.fileName = fileName;
        this.fileCount = fileCount;
    }

    public int getParentid() {
        return parentid;
    }

    public void setParentid(int parentid) {
        this.parentid = parentid;
    }

    public int getDirId() {
        return dirId;
    }

    public void setDirId(int dirId) {
        this.dirId = dirId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }
}
