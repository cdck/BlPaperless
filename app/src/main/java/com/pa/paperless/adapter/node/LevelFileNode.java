package com.pa.paperless.adapter.node;

import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Created by xlk on 2021/1/14.
 * @desc
 */
public class LevelFileNode extends BaseNode {
    int dirId;
    int mediaId;
    String name;
    int uploaderId;
    int uploaderRole;
    String uploaderName;
    int mstime;//单位毫秒
    long size;
    int attrib;//文件属性
    int filepos;//文件序号

    boolean isSelected;

    public LevelFileNode(int dirId, int mediaId, String name, int uploaderId, int uploaderRole, String uploaderName, int mstime, long size, int attrib, int filepos) {
        this.dirId = dirId;
        this.mediaId = mediaId;
        this.name = name;
        this.uploaderId = uploaderId;
        this.uploaderRole = uploaderRole;
        this.uploaderName = uploaderName;
        this.mstime = mstime;
        this.size = size;
        this.attrib = attrib;
        this.filepos = filepos;
    }

    public int getDirId() {
        return dirId;
    }

    public int getMediaId() {
        return mediaId;
    }

    public String getName() {
        return name;
    }

    public int getUploaderId() {
        return uploaderId;
    }

    public int getUploaderRole() {
        return uploaderRole;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public int getMstime() {
        return mstime;
    }

    public long getSize() {
        return size;
    }

    public int getAttrib() {
        return attrib;
    }

    public int getFilepos() {
        return filepos;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return null;
    }
}
