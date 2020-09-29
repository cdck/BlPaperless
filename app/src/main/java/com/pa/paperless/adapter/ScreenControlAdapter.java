package com.pa.paperless.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.pa.paperless.utils.LogUtil;

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

public class ScreenControlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = "ScreenControlAdapter-->";
    private List<DevMember> mData;
    private ItemClickListener mListener;
    private ArrayList<Integer> checks;

    public ScreenControlAdapter(List<DevMember> datas) {
        mData = datas;
        checks = new ArrayList<>();
    }

    public void setCheck(Integer devid) {
        LogUtil.e(TAG, "ScreenControlAdapter.setSelect :  devid --> " + devid);
        if (checks.contains(devid)) checks.remove(checks.indexOf(devid));
        else checks.add(devid);
    }

    //只能选中一个时使用
    public void setSingleCheck(Integer devid) {
        checks.clear();
        checks.add(devid);
    }

    public void refresh() {
        checks.clear();
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getChecks() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (checks.contains(mData.get(i).getDevId())) {
                ids.add(mData.get(i).getDevId());
            }
        }
        checks = ids;
        notifyDataSetChanged();
        return checks;
    }

    public void notifyChecks() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (checks.contains(mData.get(i).getDevId())) {
                ids.add(mData.get(i).getDevId());
            }
        }
        checks = ids;
        notifyDataSetChanged();
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paly_rl, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).play_btn.setText(mData.get(position).getMemberInfos().getName().toStringUtf8());
        ((ViewHolder) holder).play_btn.setSelected(checks.contains(mData.get(position).getDevId()));
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
