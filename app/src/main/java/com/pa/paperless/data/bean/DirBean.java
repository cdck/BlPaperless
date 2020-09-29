package com.pa.paperless.data.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.pa.paperless.adapter.OffDirAdapter;

/**
 * Created by xlk on 2019/8/8.
 */
public class DirBean implements MultiItemEntity {
    private String filepath;
    private String filesize;

    public DirBean(String filepath, String filesize) {
        this.filepath = filepath;
        this.filesize = filesize;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getFilesize() {
        return filesize;
    }

    @Override
    public int getItemType() {
        return OffDirAdapter.TYPE_LEVEL_2;
    }
}
