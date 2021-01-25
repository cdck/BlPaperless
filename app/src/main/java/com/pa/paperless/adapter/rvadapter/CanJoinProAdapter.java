package com.pa.paperless.adapter.rvadapter;

/**
 * Created by Administrator on 2018/6/15.
 */

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.pa.boling.paperless.R;
import com.pa.paperless.listener.ItemClickListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2018/3/3.
 * 可加入同屏的投影机adapter
 */

public class CanJoinProAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> mData;
    private ItemClickListener mListener;
    private List<Integer> checks;

    public CanJoinProAdapter(List<InterfaceDevice.pbui_Item_DeviceDetailInfo> datas) {
        mData = datas;
        checks = new ArrayList<>();
    }

    public void setCheck(Integer devid) {
        //观看只能看一个
        checks.clear();
        checks.add(devid);
        notifyDataSetChanged();
    }

    public int getChecks() {
        int checkedId = 0;
        for (int i = 0; i < mData.size(); i++)
            if (checks.contains(mData.get(i).getDevcieid()))
                checkedId = mData.get(i).getDevcieid();
        return checkedId;
    }

    public void notifyChecks() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++)
            if (checks.contains(mData.get(i).getDevcieid()))
                ids.add(mData.get(i).getDevcieid());
        checks = ids;
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
        ((ViewHolder) holder).play_btn.setText(mData.get(position).getDevname().toStringUtf8());
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