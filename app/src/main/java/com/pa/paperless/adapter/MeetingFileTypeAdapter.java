package com.pa.paperless.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.MeetingFileTypeBean;

import java.util.List;

/**
 * Created by Administrator on 2017/11/2.
 * 会议资料左边 文件夹 item
 */

public class MeetingFileTypeAdapter extends BaseAdapter {

    public static final String TAG = "MeetingFileTypeAdapter-->";
    private int SelePosion;

    public MeetingFileTypeAdapter(Context context, List<MeetingFileTypeBean> data) {
        super(context);
        mDatas = data;
    }

    public void setCheck(int i) {
        SelePosion = i;
        //不刷新则无效
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.item_meeting_lv, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        MeetingFileTypeBean bean = (MeetingFileTypeBean) mDatas.get(i);
        holder.meeting_item_filename.setText(bean.getFileName());
        holder.meeting_filecount.setText("数量：" + bean.getFileCount() + "");
        if (i == SelePosion) {
            holder.img.setSelected(true);
        } else {
            holder.img.setSelected(false);
        }
        return view;
    }

    public static class ViewHolder {
        public View rootView;
        public TextView meeting_item_filename;
        public TextView meeting_filecount;
        public ImageView img;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.img = rootView.findViewById(R.id.img);
            this.meeting_item_filename = (TextView) rootView.findViewById(R.id.meeting_item_filename);
            this.meeting_filecount = (TextView) rootView.findViewById(R.id.meeting_filecount);
        }
    }
}
