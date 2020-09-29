package com.pa.paperless.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pa.boling.paperless.R;

import java.util.ArrayList;

/**
 * Created by xlk on 2019/7/25.
 */
public class OffMeetNameAdapter extends BaseAdapter {
    private Context mContext;
    private int checkIndex;

    /**
     * 构造器
     *
     * @param context
     */
    public OffMeetNameAdapter(Context context) {
        super(context);
        mContext = context;
    }

    public OffMeetNameAdapter(Context context, ArrayList<String> data) {
        super(context);
        mContext = context;
        setDatas(data);
    }

    public void setCheck(int index) {
        this.checkIndex = index;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_off_dir, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String o = (String) mDatas.get(position);
        holder.textView5.setText(o.substring(o.lastIndexOf("/") + 1, o.length()));
        holder.textView5.setSelected(checkIndex == position);
        holder.textView5.setTextColor(checkIndex == position
                ? mContext.getResources().getColor(R.color.btn_fill_color)
                : mContext.getResources().getColor(R.color.text_color_n));
        return convertView;
    }

    public static class ViewHolder {
        public View rootView;
        public TextView textView5;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.textView5 = (TextView) rootView.findViewById(R.id.textView5);
        }
    }
}
