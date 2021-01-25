package com.pa.paperless.adapter.rvadapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.pa.boling.paperless.R;
import com.pa.paperless.utils.DateUtil;

import java.util.List;

/**
 * 离线会议主页的会议编辑目录
 *
 * @author xlk
 * @date 2019/8/24
 */
public class OffMeetAdapter extends BaseQuickAdapter<InterfaceMeet.pbui_Item_MeetMeetInfo, BaseViewHolder> {

    private int selectMeetId;

    public OffMeetAdapter(int layoutResId, @Nullable List<InterfaceMeet.pbui_Item_MeetMeetInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        String state;
        switch (item.getStatus()) {
            case 0:
                state = getContext().getResources().getString(R.string.Uninitiated_meeting);
                break;
            case 1:
                state = getContext().getResources().getString(R.string.Already_in_session);
                break;
            case 2:
                state = getContext().getResources().getString(R.string.is_Closed_meeting);
                break;
            default:
                state = getContext().getResources().getString(R.string.default_str);
                break;
        }
        helper.setText(R.id.item_tv_id, String.valueOf(item.getId()))
                .setText(R.id.item_tv_meet_name, item.getName().toStringUtf8())
                .setText(R.id.item_tv_meet_state, state)
                .setText(R.id.item_tv_meet_room, item.getRoomname().toStringUtf8())
                .setText(R.id.item_tv_secrecy, item.getSecrecy() == 1 ? getContext().getResources().getString(R.string.secrecy) : "")
                .setText(R.id.item_tv_start_time, DateUtil.formatSecond2Date(item.getStartTime(), "yyyy/MM/dd HH:mm"))
                .setText(R.id.item_tv_stop_time, DateUtil.formatSecond2Date(item.getEndTime(), "yyyy/MM/dd HH:mm"))
                .setText(R.id.item_tv_order_member, item.getOrdername().toStringUtf8());
        int color = (selectMeetId == item.getId()) ? getContext().getResources().getColor(R.color.btn_fill_color)
                : getContext().getResources().getColor(R.color.text_color_n);
        helper.setTextColor(R.id.item_tv_id, color)
                .setTextColor(R.id.item_tv_meet_name, color)
                .setTextColor(R.id.item_tv_meet_state, color)
                .setTextColor(R.id.item_tv_meet_room, color)
                .setTextColor(R.id.item_tv_secrecy, color)
                .setTextColor(R.id.item_tv_start_time, color)
                .setTextColor(R.id.item_tv_stop_time, color)
                .setTextColor(R.id.item_tv_order_member, color);
    }


    public void setSelected(int id) {
        this.selectMeetId = id;
        notifyDataSetChanged();
    }

    public int getSelectPosion() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getId() == selectMeetId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取选中的会议
     *
     * @return
     */
    public InterfaceMeet.pbui_Item_MeetMeetInfo getSelectedMeet() {
        for (int i = 0; i < getData().size(); i++) {
            InterfaceMeet.pbui_Item_MeetMeetInfo meetInfo = getData().get(i);
            if (meetInfo.getId() == selectMeetId) {
                return meetInfo;
            }
        }
        return null;
    }

    public void updateSelected() {
        for (int i = 0; i < getData().size(); i++) {
            if (selectMeetId == getData().get(i).getId()) {
                notifyDataSetChanged();
                return;
            }
        }
        selectMeetId = 0;
        notifyDataSetChanged();
    }
}
