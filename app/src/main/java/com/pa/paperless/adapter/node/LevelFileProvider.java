package com.pa.paperless.adapter.node;

import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.utils.FileSizeUtil;
import com.pa.paperless.utils.FileUtil;
import com.wind.myapplication.NativeUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by xlk on 2021/1/14.
 * @desc
 */
public class LevelFileProvider extends BaseNodeProvider {
    private final String TAG = "LevelFileProvider-->";
    LevelFileNode selectedFileNode;

    @Override
    public int getItemViewType() {
        return FileNodeAdapter.NODE_TYPE_FILE;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_expandable_file;
    }

    @Override
    public void convert(@NotNull BaseViewHolder helper, BaseNode node) {
        LevelFileNode fileNode = (LevelFileNode) node;
        ImageView item_file_iv = helper.getView(R.id.item_file_iv);
        String fileName = fileNode.getName();

        View item_file_root = helper.getView(R.id.item_file_root);
        item_file_root.setSelected(selectedFileNode == fileNode);

        if (FileUtil.isPicture(fileName)) {
            item_file_iv.setImageResource(R.drawable.ic_file_type_picture);
        } else if (FileUtil.isVideo(fileName)) {
            item_file_iv.setImageResource(R.drawable.ic_file_type_video);
        } else if (FileUtil.isPPT(fileName)) {
            item_file_iv.setImageResource(R.drawable.ic_file_type_ppt);
        } else if (FileUtil.isXLS(fileName)) {
            item_file_iv.setImageResource(R.drawable.ic_file_type_wps);
        } else if (FileUtil.isDocument(fileName)) {
            item_file_iv.setImageResource(R.drawable.ic_file_type_word);
        } else {
            item_file_iv.setImageResource(R.drawable.ic_file_type_other);
        }
        helper.setText(R.id.item_file_name, fileName)
                .setText(R.id.item_file_size, FileSizeUtil.FormatFileSize(fileNode.getSize()))
                .setText(R.id.item_file_uploader, fileNode.getUploaderName());

        helper.getView(R.id.item_file_preview).setOnClickListener(v -> {
            LogUtils.i(TAG, "onChildClick fileName=" + fileName);
            if (FileUtil.isVideo(fileName)) {
                //如果是音频或视频则在线播放
                List<Integer> devIds = new ArrayList<>();
                devIds.add(Values.localDevId);
                NativeUtil.getInstance().mediaPlayOperate(fileNode.getMediaId(),
                        devIds, 0, 0, 0,
                        InterfaceMacro.Pb_MeetPlayFlag.Pb_MEDIA_PLAYFLAG_ZERO_VALUE);
            } else {
                FileUtil.openFile(Macro.CACHE_ALL_FILE, fileName,
                        NativeUtil.getInstance(), fileNode.getMediaId(), getContext(), fileNode.getSize());
            }
        });
    }

    @Override
    public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, BaseNode data, int position) {
        LevelFileNode fileNode = (LevelFileNode) data;
        selectedFileNode = selectedFileNode != fileNode ? fileNode : null;
        if (getAdapter() != null) {
            getAdapter().notifyDataSetChanged();
        }
    }

    public int getSelectedId() {
        if (selectedFileNode != null) {
            return selectedFileNode.getMediaId();
        }
        return 0;
    }
}
