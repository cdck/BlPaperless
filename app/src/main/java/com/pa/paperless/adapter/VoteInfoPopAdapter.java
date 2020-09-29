package com.pa.paperless.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceVote;
import com.pa.boling.paperless.R;
import com.pa.paperless.listener.ItemClickListener;

import java.util.List;

/**
 * Created by Administrator on 2018/2/8.
 * 导出查看某一投票结果adapter
 */

public class VoteInfoPopAdapter extends RecyclerView.Adapter<VoteInfoPopAdapter.ViewHolder> {

    private final String TAG = "VoteInfoPopAdapter-->";
    private Context mContext;
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> mData;
    private ItemClickListener mListener;
    private int mPosition;

    public VoteInfoPopAdapter(Context context, List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> data) {
        mContext = context;
        mData = data;
    }

    public void setListener(ItemClickListener listener) {
        mListener = listener;
    }

    public void setCheck(int position) {
        mPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.vote_pop_item, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.id_tv.setText(mData.get(position).getVoteid() + "");
        holder.vote_content_tv.setText(mData.get(position).getContent().toStringUtf8());
        holder.registered_tv.setText(mData.get(position).getMode() == 0 ? mContext.getString(R.string.no) : mContext.getString(R.string.yes));
        holder.state_tv.setText(getVoteStateStr(mData.get(position).getVotestate()));
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(holder.itemView, position);
            }
        });
        /** ************ ******  item设置选中效果  ****** ************ **/
        if (position == mPosition) {
            int color = mContext.getResources().getColor(R.color.select_item_bg);
            holder.itemView.setBackgroundColor(color);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private String getVoteStateStr(int votestate) {
        if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote.getNumber()) {
            return mContext.getString(R.string.pb_vote_notvote);
        } else if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing.getNumber()) {
            return mContext.getString(R.string.pb_vote_voteing);
        } else {
            return mContext.getString(R.string.pb_vote_endvote);
        }
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public TextView id_tv;
        public TextView vote_content_tv;
        public TextView registered_tv;
        public TextView state_tv;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.id_tv = rootView.findViewById(R.id.id_tv);
            this.vote_content_tv = rootView.findViewById(R.id.vote_content_tv);
            this.registered_tv = rootView.findViewById(R.id.registered_tv);
            this.state_tv = rootView.findViewById(R.id.state_tv);
        }
    }
}
