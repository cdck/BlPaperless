package com.pa.paperless.adapter;


import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.paperless.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pa.boling.paperless.R;
import com.pa.paperless.listener.ItemClickListener;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by Administrator on 2018/7/12.
 */

public class MemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = "MemberAdapter-->";
    private List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> mData;
    private List<Integer> checks;
    private ItemClickListener mListener;

    public MemberAdapter(List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> datas) {
        mData = datas;
        checks = new ArrayList<>();
    }

    public void notifyChecks() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (checks.contains(mData.get(i).getMemberid())) {
                ids.add(mData.get(i).getMemberid());
            }
        }
        checks = ids;
        notifyDataSetChanged();
    }

    public List<Integer> getChecks() {
        return checks;
    }

    public void setCheck(Integer personid) {
        LogUtil.d(TAG, "setSelect: personid=" + personid);
        if (checks.contains(personid)) {
            checks.remove(checks.indexOf(personid));
        } else {
            checks.clear();
            checks.add(personid);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paly_rl, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).palyer_name.setText(mData.get(position).getMembername().toStringUtf8());
        ((ViewHolder) holder).palyer_name.setSelected(checks.contains(mData.get(position).getMemberid()));
        ((ViewHolder) holder).palyer_name.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onItemClick(holder.itemView, holder.getLayoutPosition());
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
