package com.pa.paperless.data.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.pa.paperless.adapter.OffDirAdapter;

/**
 * Created by xlk on 2019/8/8.
 */
public class Level1Item implements MultiItemEntity {

    private String filepath;

    public Level1Item(String filepath) {
        this.filepath = filepath;
    }

    public String getFilepath() {
        return filepath;
    }

    @Override
    public int getItemType() {
        return OffDirAdapter.TYPE_LEVEL_1;
    }
}
