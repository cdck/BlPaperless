package com.pa.paperless.adapter;

import android.content.Context;
import android.view.LayoutInflater;



import java.util.List;

/**
 * Created by Administrator on 2017/5/20 0020.
 */

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {

    protected List<T> mDatas;
    protected final LayoutInflater mInflater;

    /**
     * 构造器
     *
     * @param context
     */
    public BaseAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDatas != null ? mDatas.size() : 0;

    }

    public void setDatas(List<T> datas) {
        mDatas = datas;
    }

    @Override
    public Object getItem(int position) {
        return mDatas != null ? mDatas.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
