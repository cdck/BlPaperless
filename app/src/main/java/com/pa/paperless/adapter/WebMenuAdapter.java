package com.pa.paperless.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * @author Created by xlk on 2020/9/8.
 * @desc 网页菜单
 */
public class WebMenuAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public WebMenuAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {

    }
}
