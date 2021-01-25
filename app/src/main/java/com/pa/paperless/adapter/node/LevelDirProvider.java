package com.pa.paperless.adapter.node;

import android.annotation.TargetApi;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.pa.boling.paperless.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.core.view.ViewCompat;

/**
 * @author Created by xlk on 2021/1/14.
 * @desc
 */
public class LevelDirProvider extends BaseNodeProvider {
    private final String TAG = "LevelDirProvider-->";

    @Override
    public int getItemViewType() {
        return FileNodeAdapter.NODE_TYPE_DIR;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_expandable_dir;
    }

    @Override
    public void convert(@NotNull BaseViewHolder helper, BaseNode node) {
        LevelDirNode dirNode = (LevelDirNode) node;
        helper.setText(R.id.item_dir_tv, dirNode.getDirName());
        setArrowSpin(helper, node, false);
    }

    @Override
    public void convert(@NotNull BaseViewHolder helper, @NotNull BaseNode data, @NotNull List<?> payloads) {
        for (Object payload : payloads) {
            if (payload instanceof Integer && (int) payload == FileNodeAdapter.EXPAND_COLLAPSE_PAYLOAD) {
                // 增量刷新，使用动画变化箭头
                setArrowSpin(helper, data, true);
            }
        }
    }

    private void setArrowSpin(BaseViewHolder helper, BaseNode data, boolean isAnimate) {
        LevelDirNode entity = (LevelDirNode) data;

        ImageView imageView = helper.getView(R.id.item_dir_iv);

        if (entity.isExpanded()) {
            if (isAnimate) {
                ViewCompat.animate(imageView).setDuration(200)
                        .setInterpolator(new DecelerateInterpolator())
                        .rotation(90f)
                        .start();
            } else {
                imageView.setRotation(90f);
            }
        } else {
            if (isAnimate) {
                ViewCompat.animate(imageView).setDuration(200)
                        .setInterpolator(new DecelerateInterpolator())
                        .rotation(0f)
                        .start();
            } else {
                imageView.setRotation(0f);
            }
        }
    }

    @Override
    public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, BaseNode data, int position) {
        // 这里使用payload进行增量刷新（避免整个item刷新导致的闪烁，不自然）
        if (getAdapter() != null) {
            getAdapter().expandOrCollapse(position, true, true, FileNodeAdapter.EXPAND_COLLAPSE_PAYLOAD);
        } else {
            LogUtils.e(TAG, "onClick 方法中getAdapter为null");
        }
    }
}
