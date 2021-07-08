package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.DevMember;

import java.util.List;


/**
 * Created by Administrator on 2018/5/21.
 * 批注页面左边的参会人列表
 */

public class PostilMemberAdapter extends BaseAdapter {
    private final Context mContext;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> mDatas;
    private int SelePosion;

    public PostilMemberAdapter(Context context, List<InterfaceMember.pbui_Item_MemberDetailInfo> data) {
        mContext = context;
        mDatas = data;
    }

    @Override
    public int getCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mDatas != null ? mDatas.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public void setCheck(int index) {
        SelePosion = index;
        //不刷新则无效
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_postil, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        InterfaceMember.pbui_Item_MemberDetailInfo member = mDatas.get(position);
        holder.postil_member_name.setText(member.getName().toStringUtf8());
        if (position == SelePosion) {
            holder.postil_member_name.setSelected(true);
            holder.postil_member_name.setTextColor(mContext.getResources().getColor(R.color.btn_fill_color));
        } else {
            holder.postil_member_name.setSelected(false);
            holder.postil_member_name.setTextColor(mContext.getResources().getColor(R.color.text_color_n));
        }
        return convertView;
    }

    public static class ViewHolder {
        public View rootView;
        public TextView postil_member_name;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.postil_member_name = rootView.findViewById(R.id.postil_member_name);
        }

    }
}
