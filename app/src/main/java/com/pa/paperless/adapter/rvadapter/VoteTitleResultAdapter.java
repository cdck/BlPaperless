package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import com.pa.paperless.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceVote;
import com.pa.boling.paperless.R;
import com.pa.paperless.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xlk on 2018/10/16.
 */

public class VoteTitleResultAdapter extends RecyclerView.Adapter<VoteTitleResultAdapter.ViewHolder> {

    private String TAG = "VoteTitleResultAdapter-->";
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> mData;
    private Context cxt;
    private ItemSelectListener mSelectListener;
    private final List<Integer> checks;

    public VoteTitleResultAdapter(Context context, List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteinfo) {
        cxt = context;
        mData = voteinfo;
        checks = new ArrayList<>();
    }

    public void setItemSelectListener(ItemSelectListener listener) {
        mSelectListener = listener;
    }

    public interface ItemSelectListener {
        void ItemSelect(int posion, View view);
    }

    public void setSelect(int voteid) {
        LogUtil.e(TAG, "VoteTitleResultAdapter.setSelect :  voteid --> " + voteid);
        checks.clear();
        checks.add(voteid);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(cxt).inflate(R.layout.vote_result_item, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.voteTitleNumber.setText(position + 1 + "");
        holder.voteTitleContent.setText(MyUtils.b2s(mData.get(position).getContent()));
        holder.voteTitleId.setText(mData.get(position).getVoteid() + "");
        holder.itemView.setOnClickListener(v -> {
            if (mSelectListener != null) {
                mSelectListener.ItemSelect(position, holder.itemView);
            }
        });
        if (checks.contains(mData.get(position).getVoteid())) {
            holder.voteTitleNumber.setBackgroundColor(Color.RED);
            holder.voteTitleContent.setBackgroundColor(Color.RED);
            holder.voteTitleId.setBackgroundColor(Color.RED);
            holder.voteTitleNumber.setTextColor(Color.WHITE);
            holder.voteTitleContent.setTextColor(Color.WHITE);
            holder.voteTitleId.setTextColor(Color.WHITE);
        } else {
            holder.voteTitleNumber.setBackgroundColor(Color.WHITE);
            holder.voteTitleContent.setBackgroundColor(Color.WHITE);
            holder.voteTitleId.setBackgroundColor(Color.WHITE);
            holder.voteTitleNumber.setTextColor(Color.BLACK);
            holder.voteTitleContent.setTextColor(Color.BLACK);
            holder.voteTitleId.setTextColor(Color.BLACK);
        }

    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView voteTitleNumber;
        TextView voteTitleContent;
        TextView voteTitleId;

        ViewHolder(View view) {
            super(view);
            voteTitleNumber =view.findViewById(R.id.vote_title_number);
            voteTitleContent =view.findViewById(R.id.vote_title_content);
            voteTitleId =view.findViewById(R.id.vote_title_id);
        }
    }
}
