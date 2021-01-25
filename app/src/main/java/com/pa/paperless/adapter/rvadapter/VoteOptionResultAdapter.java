package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.VoteResultSubmitMember;

import java.util.List;


/**
 * Created by xlk on 2018/10/16.
 */

public class VoteOptionResultAdapter extends RecyclerView.Adapter<VoteOptionResultAdapter.ViewHolder> {

    private List<VoteResultSubmitMember> mData;
    private Context cxt;

    public VoteOptionResultAdapter(Context context, List<VoteResultSubmitMember> voteinfo) {
        cxt = context;
        mData = voteinfo;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(cxt).inflate(R.layout.vote_result_option_item, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.number.setText(position + 1 + "");
        holder.name.setText(mData.get(position).getMemberName());
        holder.options.setText(mData.get(position).getOptionStr());
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView number;
        TextView name;
        TextView options;

        ViewHolder(View view) {
            super(view);
            number = view.findViewById(R.id.number);
            name = view.findViewById(R.id.name);
            options = view.findViewById(R.id.options);
        }
    }
}
