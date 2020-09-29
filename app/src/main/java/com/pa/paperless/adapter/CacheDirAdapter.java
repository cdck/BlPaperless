package com.pa.paperless.adapter;

import androidx.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.List;

/**
 * Created by xlk on 2019/8/28.
 */
public class CacheDirAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirDetailInfo, BaseViewHolder> {
    public CacheDirAdapter(int layoutResId, @Nullable List<InterfaceFile.pbui_Item_MeetDirDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFile.pbui_Item_MeetDirDetailInfo item) {

    }
}
