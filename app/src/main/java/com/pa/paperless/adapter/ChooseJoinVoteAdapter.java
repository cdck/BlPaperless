package com.pa.paperless.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.listener.ItemClickListener;
import com.pa.paperless.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xlk on 2018/11/7.
 */

public class ChooseJoinVoteAdapter extends RecyclerView.Adapter<ChooseJoinVoteAdapter.ViewHolder> {

    private List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> mData;
    private Context cxt;
    private ItemClickListener mListener;
    private List<Integer> checks;

    public ChooseJoinVoteAdapter(Context context, List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> data) {
        cxt = context;
        mData = data;
        checks = new ArrayList<>();
    }

    public void notifyChecks() {
        List<Integer> mediaIds = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            InterfaceMember.pbui_Item_MeetMemberDetailInfo info = mData.get(i);
            int memberid = info.getMemberid();
            if (checks.contains(memberid)) mediaIds.add(memberid);
        }
        checks = mediaIds;
        notifyDataSetChanged();
    }

    public List<Integer> getChecks() {
        return checks;
    }

    public void setChecks(int memberid) {
        if (checks.contains(memberid)) checks.remove(checks.indexOf(memberid));
        else checks.add(memberid);
    }

    public boolean isCheckAll() {
        return checks.size() == mData.size();
    }

    public void setCheckAll(boolean b) {
        checks.clear();
        if (b) {
            for (int i = 0; i < mData.size(); i++) {
                for (InterfaceMember.pbui_Item_MeetMemberDetailInfo info : mData) {
//                    int memberdetailflag = info.getMemberdetailflag();
//                    boolean isonline = memberdetailflag == InterfaceMember.Pb_MemberDetailFlag.Pb_MEMBERDETAIL_FLAG_ONLINE.getNumber();
//                    int state = info.getFacestatus();
                    boolean havePermission = MyUtils.isHavePermission(info.getMemberid(), Macro.permission_code_vote);
//                    /** **** **  在线并且有权限且界面在参会人界面或则投票界面  ** **** **/
//                    if (isonline && havePermission && (state == 1 || state == 3)) {
                    if (havePermission) {
                        checks.add(info.getMemberid());
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setListener(ItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public ChooseJoinVoteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(cxt).inflate(R.layout.choose_join_vote_item, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(ChooseJoinVoteAdapter.ViewHolder holder, int position) {
        holder.number_cb.setText(String.valueOf(position + 1));
        InterfaceMember.pbui_Item_MeetMemberDetailInfo info = mData.get(position);
        holder.mamber_name_tv.setText(MyUtils.b2s(info.getMembername()));
        holder.dev_name_tv.setText(MyUtils.b2s(info.getDevname()));
        int devid = info.getDevid();
        int state = info.getFacestatus();
        int memberdetailflag = info.getMemberdetailflag();
        boolean isonline = memberdetailflag == InterfaceMember.Pb_MemberDetailFlag.Pb_MEMBERDETAIL_FLAG_ONLINE.getNumber();
        boolean canjoin = true;//一层层的判断是否可以加入投票
        if (devid == 0) {
//            canjoin = false;
            holder.dev_state_tv.setText(cxt.getString(R.string.unbind_seat));
        } else {
            if (isonline) {
                boolean b = ((state == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MemFace.getNumber()) || (state == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_VoteFace.getNumber()));
//                canjoin = b;
                holder.dev_state_tv.setText(b ? cxt.getString(R.string.online) : cxt.getString(R.string.online_but));
            } else holder.dev_state_tv.setText(cxt.getString(R.string.offline));
        }
//        if (canjoin) canjoin = isonline;
        //是否有投票权限，只要大于15就说明有，因为如果有投票的权限最小值都是16
//        boolean havePermission = info.getPermission() > 15;
        boolean havePermission = MyUtils.isHavePermission(info.getMemberid(), Macro.permission_code_vote);
        if (canjoin) canjoin = havePermission;
        holder.permission_tv.setText(havePermission ? cxt.getString(R.string.default_str) : cxt.getString(R.string.not_permission));
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) mListener.onItemClick(holder.itemView, position);
        });
        /** **** **  用户已经选中了，但是在还没有提交的时候出现离线等情况则设置不选中  ** **** **/
        if (checks.contains(info.getMemberid()) && !canjoin) {
            checks.remove(checks.indexOf(info.getMemberid()));
        }
        holder.number_cb.setChecked(checks.contains(info.getMemberid()));
        holder.number_cb.setTextColor(isonline ? Color.BLUE : Color.BLACK);
        holder.mamber_name_tv.setTextColor(isonline ? Color.BLUE : Color.BLACK);
        holder.dev_name_tv.setTextColor(isonline ? Color.BLUE : Color.BLACK);
        holder.dev_state_tv.setTextColor(isonline ? Color.BLUE : Color.BLACK);
        holder.permission_tv.setTextColor(isonline ? Color.BLUE : Color.BLACK);
    }


    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public CheckBox number_cb;
        public TextView mamber_name_tv;
        public TextView dev_name_tv;
        public TextView dev_state_tv;
        public TextView permission_tv;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.number_cb = (CheckBox) rootView.findViewById(R.id.number_cb);
            this.mamber_name_tv = (TextView) rootView.findViewById(R.id.mamber_name_tv);
            this.dev_name_tv = (TextView) rootView.findViewById(R.id.dev_name_tv);
            this.dev_state_tv = (TextView) rootView.findViewById(R.id.dev_state_tv);
            this.permission_tv = (TextView) rootView.findViewById(R.id.permission_tv);
        }
    }
}
