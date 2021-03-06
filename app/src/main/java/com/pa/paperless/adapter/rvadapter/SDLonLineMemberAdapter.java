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
 * Created by Administrator on 2018/7/12.
 * SDL在线参会人adapter
 */

public class SDLonLineMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DevMember> mData;
    private List<Integer> checks;
    private ItemClickListener mListener;

    public SDLonLineMemberAdapter(List<DevMember> datas) {
        mData = datas;
        checks = new ArrayList<>();
    }

    public void notifyChecks() {
        notifyDataSetChanged();
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (checks.contains(mData.get(i).getDevId())) {
                ids.add(mData.get(i).getDevId());
            }
        }
        checks = ids;
        notifyDataSetChanged();
    }

    public List<Integer> getChecks() {
        return checks;
    }

    public void setCheck(Integer devid) {
        if (checks.contains(devid)) checks.remove(devid);
        else checks.add(devid);
    }

    public boolean isAllCheck() {
        return checks.size() == mData.size();
    }

    public void setAllCheck(boolean all) {
        checks.clear();
        if (all) {
            for (int i = 0; i < mData.size(); i++) {
                checks.add(mData.get(i).getDevId());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paly_rl, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).palyer_name.setText(mData.get(position).getMemberDetailInfo().getName().toStringUtf8());
        ((ViewHolder) holder).palyer_name.setSelected(checks.contains(mData.get(position).getDevId()));
        ((ViewHolder) holder).palyer_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(holder.itemView, holder.getLayoutPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public Button palyer_name;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.palyer_name = (Button) rootView.findViewById(R.id.palyer_name);
        }
    }

    public void setItemClick(ItemClickListener listener) {
        mListener = listener;
    }

}
