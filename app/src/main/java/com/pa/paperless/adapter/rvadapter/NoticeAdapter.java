package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceBullet;
import com.pa.boling.paperless.R;
import com.pa.paperless.listener.ItemClickListener;
import com.pa.paperless.utils.MyUtils;

import java.util.List;

/**
 * Created by xlk on 2018/11/6.
 */

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    private List<InterfaceBullet.pbui_Item_BulletDetailInfo> mData;
    private Context cxt;
    private int mPosition;
    private ItemClickListener mListener;

    public NoticeAdapter(Context context, List<InterfaceBullet.pbui_Item_BulletDetailInfo> data) {
        cxt = context;
        mData = data;
    }

    public void setSelect(int posion) {
        mPosition = posion;
        notifyDataSetChanged();
    }

    public void setItemClick(ItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public NoticeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_item, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(NoticeAdapter.ViewHolder holder, int position) {
        holder.number.setText(position + 1 + "");
        holder.title.setText(MyUtils.b2s(mData.get(position).getTitle()));
        holder.content.setText(MyUtils.b2s(mData.get(position).getContent()));
        holder.rootView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(holder.itemView, position);
            }
        });
        /** ************ ******  item设置选中效果  ****** ************ **/
        if (position == mPosition) {
            int color = cxt.getResources().getColor(R.color.select_item_bg);
            holder.itemView.setBackgroundColor(color);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public TextView number;
        public TextView title;
        public TextView content;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.number = rootView.findViewById(R.id.number);
            this.title = rootView.findViewById(R.id.title);
            this.content = rootView.findViewById(R.id.content);
        }
    }
}
