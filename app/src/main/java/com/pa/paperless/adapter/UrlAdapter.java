package com.pa.paperless.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.pa.boling.paperless.R;

import java.util.List;


/**
 * @author Created by xlk on 2020/11/14.
 * @desc
 */
public class UrlAdapter extends BaseQuickAdapter<InterfaceBase.pbui_Item_UrlDetailInfo, BaseViewHolder> {
    public UrlAdapter( @Nullable List<InterfaceBase.pbui_Item_UrlDetailInfo> data) {
        super(R.layout.item_web, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceBase.pbui_Item_UrlDetailInfo item) {
        helper.setText(R.id.tv_name, item.getName().toStringUtf8())
                .setText(R.id.tv_addr, item.getAddr().toStringUtf8());
    }
}
