package com.pa.paperless.adapter.rvadapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.MeetDirFileInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by xlk on 2021/1/15.
 * @desc
 */
public class DownloadFileAdapter extends BaseQuickAdapter<MeetDirFileInfo, BaseViewHolder> {
    List<MeetDirFileInfo> selectedFiles = new ArrayList<>();

    public DownloadFileAdapter(int layoutResId, @Nullable List<MeetDirFileInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, MeetDirFileInfo meetDirFileInfo) {
        holder.setText(R.id.item_view_1, String.valueOf(holder.getLayoutPosition() + 1))
                .setText(R.id.item_view_2, meetDirFileInfo.getFileName())
                .setText(R.id.item_view_3, String.valueOf(meetDirFileInfo.getMediaId()));
        boolean isSelected = selectedFiles.contains(meetDirFileInfo);
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.black);
        holder.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        holder.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor);
    }

    public List<MeetDirFileInfo> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFile(MeetDirFileInfo info) {
        if (selectedFiles.contains(info)) {
            selectedFiles.remove(info);
        } else {
            selectedFiles.add(info);
        }
        notifyDataSetChanged();
    }
}
