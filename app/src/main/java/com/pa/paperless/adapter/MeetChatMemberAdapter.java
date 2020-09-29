package com.pa.paperless.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.ChatVideoMemberBean;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;


/**
 * @author xlk
 * @date 2020/3/17
 * @desc 会议交流界面中 左边的在线参会人列表
 */
public class MeetChatMemberAdapter extends BaseQuickAdapter<ChatVideoMemberBean, BaseViewHolder> {
    List<Integer> ids = new ArrayList<>();

    public MeetChatMemberAdapter(int layoutResId, @Nullable List<ChatVideoMemberBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ChatVideoMemberBean item) {
        helper.setText(R.id.i_m_c_m_name, item.getMemberDetailInfo().getName().toStringUtf8());
        helper.getView(R.id.i_m_c_m_name).setSelected(ids.contains(item.getMemberDetailInfo().getPersonid()));
        helper.getView(R.id.i_m_c_m_ll).setSelected(ids.contains(item.getMemberDetailInfo().getPersonid()));
        helper.getView(R.id.i_m_c_m_iv).setSelected(ids.contains(item.getMemberDetailInfo().getPersonid()));
    }

    public void notifyCheck() {
        List<Integer> temps = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (ids.contains(mData.get(i).getMemberDetailInfo().getPersonid())) {
                temps.add(mData.get(i).getMemberDetailInfo().getPersonid());
            }
        }
        ids.clear();
        ids.addAll(temps);
        temps.clear();
    }

    public List<Integer> getChooseDevid() {
        List<Integer> temps = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo = mData.get(i).getDeviceDetailInfo();
            if (ids.contains(deviceDetailInfo.getMemberid())) {
                temps.add(deviceDetailInfo.getDevcieid());
            }
        }
        return temps;
    }

    public List<Integer> getCheck() {
        return ids;
    }

    public void setCheck(int memberId) {
        if (ids.contains(memberId)) {
            ids.remove(ids.indexOf(memberId));
        } else {
            ids.add(memberId);
        }
        notifyDataSetChanged();
    }

    public boolean isCheckAll() {
        return mData.size() == ids.size();
    }

    public void setCheckAll(boolean isAll) {
        ids.clear();
        if (isAll) {
            for (int i = 0; i < mData.size(); i++) {
                ids.add(mData.get(i).getMemberDetailInfo().getPersonid());
            }
        }
        notifyDataSetChanged();
    }
}
