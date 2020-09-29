package com.pa.paperless.activity.offline;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.pa.boling.paperless.R;

import java.util.List;

/**
 * Created by xlk on 2019/8/29.
 */
public class DirAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirDetailInfo, BaseViewHolder> {
    private int selected;

    public DirAdapter(int layoutResId, @Nullable List<InterfaceFile.pbui_Item_MeetDirDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFile.pbui_Item_MeetDirDetailInfo item) {
        helper.setText(R.id.item_meet_name_tv, item.getName().toStringUtf8());
        helper.setTextColor(R.id.item_meet_name_tv, (item.getId() == selected)
                ? mContext.getResources().getColor(R.color.btn_fill_color)
                : mContext.getResources().getColor(R.color.text_color_n));
    }

    public void deleteSelect(InterfaceFile.pbui_Item_MeetDirDetailInfo selectDir) {
        mData.remove(selectDir);
        notifyDataSetChanged();
    }

    public InterfaceFile.pbui_Item_MeetDirDetailInfo getSelectDir() {
        for (int i = 0; i < mData.size(); i++) {
            InterfaceFile.pbui_Item_MeetDirDetailInfo detailInfo = mData.get(i);
            if (detailInfo.getId() == selected) {
                return detailInfo;
            }
        }
        return null;
    }

    public void setSelectDir(int dirid) {
        selected = dirid;
        notifyDataSetChanged();
    }

    public int getSelectedDirId() {
        return selected;
    }
}
