package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import android.graphics.Color;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceVote;
import com.pa.boling.paperless.R;
import com.pa.paperless.listener.ItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/14.
 * 投票查询 adapter
 */

public class VoteAdapter extends RecyclerView.Adapter<VoteAdapter.ViewHolder> {

    private final List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> mData;
    private final Context mContext;
    private ItemClickListener mListener;
    private int mCheckedPosion = -1;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.vote_item, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    public VoteAdapter(Context context, List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = mData.get(position);
        int voteid = voteInfo.getVoteid();
        //  投票内容文本
        String content = voteInfo.getContent().toStringUtf8();
        //  投票选项个数文本
        int type = voteInfo.getType();
        switch (type) {
            case 0://多选
                content += "（多选";
                break;
            case 1://单选
                content += "（单选";
                break;
            case 2://多选 5选4
                content += "（5选4";
                break;
            case 3://多选 5选3
                content += "（5选3";
                break;
            case 4://多选 5选2
                content += "（5选2";
                break;
            case 5://多选 3选2
                content += "（3选2";
                break;
        }
        //  投票是否记名文本
        int mode = voteInfo.getMode();
        switch (mode) {
            case 0://匿名投票
                content += "，匿名）";
                break;
            case 1://记名投票
                content += "，记名）";
                break;
        }
        holder.vote_item_title.setText(voteid + ".    " + content);
        //  投票当前状态文本
        int votestate = voteInfo.getVotestate();
        switch (votestate) {
            case 0://未发起
                holder.state_tv.setText("未发起");
                break;
            case 1://正在进行
                holder.state_tv.setText("正在进行...");
                break;
            case 2://已经结束
                holder.state_tv.setText("已经结束");
                break;
        }
        //设置选项的文本内容是，需要先初始化
        holder.vote_option_1.setVisibility(View.GONE);
        holder.vote_option1_count.setVisibility(View.GONE);
        holder.vote_option_2.setVisibility(View.GONE);
        holder.vote_option2_count.setVisibility(View.GONE);
        holder.vote_option_3.setVisibility(View.GONE);
        holder.vote_option3_count.setVisibility(View.GONE);
        holder.vote_option_4.setVisibility(View.GONE);
        holder.vote_option4_count.setVisibility(View.GONE);
        holder.vote_option_5.setVisibility(View.GONE);
        holder.vote_option5_count.setVisibility(View.GONE);

//        holder.vote_option_1.setText("");
//        holder.vote_option1_count.setText("");
//        holder.vote_option_2.setText("");
//        holder.vote_option2_count.setText("");
//        holder.vote_option_3.setText("");
//        holder.vote_option3_count.setText("");
//        holder.vote_option_4.setText("");
//        holder.vote_option4_count.setText("");
//        holder.vote_option_5.setText("");
//        holder.vote_option5_count.setText("");
        List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = disposeItemList(voteInfo.getItemList());
        for (int i = 0; i < optionInfo.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(i);
            String text = voteOptionsInfo.getText().toStringUtf8();
            int selcnt = voteOptionsInfo.getSelcnt();
            String option = selcnt + "票";
            if (!text.isEmpty()) {
                if (i == 0) {
                    holder.vote_option_1.setText(text);
                    holder.vote_option1_count.setText(option);
                    holder.vote_option_1.setVisibility(View.VISIBLE);
                    holder.vote_option1_count.setVisibility(View.VISIBLE);
                } else if (i == 1) {
                    holder.vote_option_2.setText(text);
                    holder.vote_option2_count.setText(option);
                    holder.vote_option_2.setVisibility(View.VISIBLE);
                    holder.vote_option2_count.setVisibility(View.VISIBLE);
                } else if (i == 2) {
                    holder.vote_option_3.setText(text);
                    holder.vote_option3_count.setText(option);
                    holder.vote_option_3.setVisibility(View.VISIBLE);
                    holder.vote_option3_count.setVisibility(View.VISIBLE);
                } else if (i == 3) {
                    holder.vote_option_4.setText(text);
                    holder.vote_option4_count.setText(option);
                    holder.vote_option_4.setVisibility(View.VISIBLE);
                    holder.vote_option4_count.setVisibility(View.VISIBLE);
                } else if (i == 4) {
                    holder.vote_option_5.setText(text);
                    holder.vote_option5_count.setText(option);
                    holder.vote_option_5.setVisibility(View.VISIBLE);
                    holder.vote_option5_count.setVisibility(View.VISIBLE);
                }
            }
        }
        holder.itemView.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onItemClick(holder.itemView, holder.getLayoutPosition());
            }
        });
        /** ************ ******  item设置选中效果  ****** ************ **/
        if (position == mCheckedPosion) {
            int color = mContext.getResources().getColor(R.color.select_vote_item_red_bg);
            holder.itemView.setBackgroundColor(color);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    //去除掉答案是空文本的选项
    private List<InterfaceVote.pbui_SubItem_VoteItemInfo> disposeItemList(List<InterfaceVote.pbui_SubItem_VoteItemInfo> infos) {
        List<InterfaceVote.pbui_SubItem_VoteItemInfo> items = new ArrayList<>();
        for (int i = 0; i < infos.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo item = infos.get(i);
            String trim = item.getText().toStringUtf8().trim();
            if (!trim.isEmpty()) {
                items.add(item);
            }
        }
        return items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setCheckedId(int posion) {
        mCheckedPosion = posion;
        notifyDataSetChanged();
    }

    public void setItemListener(ItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public TextView vote_item_title;
        public TextView state_tv;
        public TextView vote_option_1;
        public TextView vote_option1_count;
        public TextView vote_option_2;
        public TextView vote_option2_count;
        public TextView vote_option_3;
        public TextView vote_option3_count;
        public TextView vote_option_4;
        public TextView vote_option4_count;
        public TextView vote_option_5;
        public TextView vote_option5_count;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.vote_item_title = (TextView) rootView.findViewById(R.id.vote_item_title);
            this.state_tv = (TextView) rootView.findViewById(R.id.state_tv);
            this.vote_option_1 = (TextView) rootView.findViewById(R.id.vote_option_1);
            this.vote_option1_count = (TextView) rootView.findViewById(R.id.vote_option1_count);
            this.vote_option_2 = (TextView) rootView.findViewById(R.id.vote_option_2);
            this.vote_option2_count = (TextView) rootView.findViewById(R.id.vote_option2_count);
            this.vote_option_3 = (TextView) rootView.findViewById(R.id.vote_option_3);
            this.vote_option3_count = (TextView) rootView.findViewById(R.id.vote_option3_count);
            this.vote_option_4 = (TextView) rootView.findViewById(R.id.vote_option_4);
            this.vote_option4_count = (TextView) rootView.findViewById(R.id.vote_option4_count);
            this.vote_option_5 = (TextView) rootView.findViewById(R.id.vote_option_5);
            this.vote_option5_count = (TextView) rootView.findViewById(R.id.vote_option5_count);
        }

    }
}
