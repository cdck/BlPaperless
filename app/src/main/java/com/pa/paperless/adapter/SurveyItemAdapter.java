package com.pa.paperless.adapter;

import android.content.Context;
import android.content.res.Resources;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mogujie.tt.protobuf.InterfaceVote;
import com.pa.boling.paperless.R;
import com.pa.paperless.listener.ItemClickListener;
import java.util.List;

/**
 * Created by Administrator on 2018/5/26.
 * 问卷调查 页面下方的序号索引列表.
 */

public class SurveyItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> mData;
    private ItemClickListener mListener;
    private int mPosition;
    private final Resources resources;

    public SurveyItemAdapter(Context c, List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> data) {
        mContext = c;
        resources = c.getResources();
        mData = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_count_survey, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    //添加item
    public void addItem(int position, InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo) {
        mData.add(position, voteInfo);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, mData.size() - position);
    }

    //删除item
    public void delItem(int position) {
        mData.remove(position);
        notifyItemInserted(position);
        notifyItemRangeRemoved(position, mData.size() - position);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((ViewHolder) holder).item_choose.setText(String.valueOf(position + 1));
        ((ViewHolder) holder).item_choose.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(holder.itemView, holder.getLayoutPosition());
            }
        });
        /** **** **  设置item点击效果  ** **** **/
        if (position == mPosition) {
            ((ViewHolder) holder).item_choose.setTextColor(resources.getColor(R.color.white));
            ((ViewHolder) holder).item_choose.setBackground(resources.getDrawable(R.drawable.btn_s));
            if (mData.get(position).getSelcnt() != 0) {
                ((ViewHolder) holder).item_choose.setBackground(resources.getDrawable(R.drawable.btn_a_s));
            }
        } else {
            ((ViewHolder) holder).item_choose.setTextColor(resources.getColor(R.color.black));
            ((ViewHolder) holder).item_choose.setBackground(resources.getDrawable(R.drawable.btn_n));
            if (mData.get(position).getSelcnt() != 0) {
                ((ViewHolder) holder).item_choose.setBackground(resources.getDrawable(R.drawable.btn_a_n));
            }
        }
    }

    public void setSelcectPosition(int p) {
        mPosition = p;
        notifyDataSetChanged();
    }

    public void setItemClick(ItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public Button item_choose;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.item_choose = rootView.findViewById(R.id.item_choose);
        }
    }
}
