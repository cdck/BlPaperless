package com.pa.paperless.data.bean;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.pa.paperless.adapter.OffDirAdapter;

/**
 * Created by xlk on 2019/8/8.
 */
public class Level0Item extends AbstractExpandableItem<Level1Item> implements MultiItemEntity {

    private String filepath;

    public Level0Item(String filepath) {
        this.filepath = filepath;
    }

    public String getFilepath() {
        return filepath;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getItemType() {
        return OffDirAdapter.TYPE_LEVEL_0;
    }
}
