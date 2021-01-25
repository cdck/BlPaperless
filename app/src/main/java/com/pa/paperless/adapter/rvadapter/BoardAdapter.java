package com.pa.paperless.adapter.rvadapter;


import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.DevMember;
import com.pa.paperless.listener.ItemClickListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/11/9.
 * 同屏控制Adapter 参与人 和 投影机
 */

public class BoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Integer> checks;
    private List<DevMember> mData;
    private ItemClickListener mListener;


    public BoardAdapter(List<DevMember> datas) {
        mData = datas;
        checks = new ArrayList<>();
    }

    public void notifyChecks() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (checks.contains(mData.get(i).getMemberDetailInfo().getPersonid())) {
                ids.add(mData.get(i).getMemberDetailInfo().getPersonid());
            }
        }
        checks = ids;
        notifyDataSetChanged();
    }

    public void setCheck(Integer id) {
        if (checks.contains(id)) checks.remove(id);
        else checks.add(id);
    }

    public List<Integer> getChecks() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (checks.contains(mData.get(i).getMemberDetailInfo().getPersonid()))
                ids.add(mData.get(i).getMemberDetailInfo().getPersonid());
        }
        checks = ids;
        notifyDataSetChanged();
        return checks;
    }

    public boolean isAllCheck() {
        return checks.size() == mData.size();
    }

    public void setAllCheck(boolean all) {
        checks.clear();
        if (all) {
            for (int i = 0; i < mData.size(); i++) {
                checks.add(mData.get(i).getMemberDetailInfo().getPersonid());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paly_rl, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).play_btn.setText(mData.get(position).getMemberDetailInfo().getName().toStringUtf8());
        ((ViewHolder) holder).play_btn.setSelected(checks.contains(mData.get(position).getMemberDetailInfo().getPersonid()));
        ((ViewHolder) holder).play_btn.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onItemClick(holder.itemView, holder.getLayoutPosition());
            }
        });
    }


    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button play_btn;
        public View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.play_btn = (Button) itemView.findViewById(R.id.palyer_name);
        }
    }

    public void setItemClick(ItemClickListener listener) {
        mListener = listener;
    }
}
