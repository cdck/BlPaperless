package com.pa.paperless.adapter.rvadapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.pa.boling.paperless.R;
import com.pa.paperless.listener.ItemClickListener;
import com.pa.paperless.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2018/3/3.
 */

public class OnLineProAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> mData;
    private ItemClickListener mListener;
    private List<Integer> checks;


    public OnLineProAdapter(List<InterfaceDevice.pbui_Item_DeviceDetailInfo> datas) {
        mData = datas;
        checks = new ArrayList<>();
    }

    public void notifyChecks() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (checks.contains(mData.get(i).getDevcieid())) {
                ids.add(mData.get(i).getDevcieid());
            }
        }
        checks = ids;
        notifyDataSetChanged();
    }

    public void setCheck(Integer devid) {
        if (checks.contains(devid)) checks.remove(checks.indexOf(devid));
        else checks.add(devid);
    }

    public List<Integer> getChecks() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (checks.contains(mData.get(i).getDevcieid())) {
                ids.add(mData.get(i).getDevcieid());
            }
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
                checks.add(mData.get(i).getDevcieid());
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
        ((ViewHolder) holder).play_btn.setText(MyUtils.b2s(mData.get(position).getDevname()));
        ((ViewHolder) holder).play_btn.setSelected(checks.contains(mData.get(position).getDevcieid()));
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