package com.pa.paperless.adapter;

import android.graphics.Color;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.Level0Item;
import com.pa.paperless.data.bean.Level1Item;

import java.util.List;

/**
 * Created by xlk on 2019/8/8.
 */
public class OffDirAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {

    public static final int TYPE_LEVEL_0 = 0;
    public static final int TYPE_LEVEL_1 = 1;
    public static final int TYPE_LEVEL_2 = 2;
    private int selectPosition;

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public OffDirAdapter(List<MultiItemEntity> data) {
        super(data);
        addItemType(TYPE_LEVEL_0, R.layout.item_meet_name_lv0);
        addItemType(TYPE_LEVEL_1, R.layout.item_file_type_name_lv1);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItemEntity item) {
        int layoutPosition = helper.getLayoutPosition();
        switch (helper.getItemViewType()) {
            case TYPE_LEVEL_0:
                final Level0Item lv0 = (Level0Item) item;
                String filepath = lv0.getFilepath();
                helper.setText(R.id.item_meet_name_tv, filepath.substring(filepath.lastIndexOf("/") + 1, filepath.length()));
                TextView view = helper.getView(R.id.item_meet_name_tv);
                view.setTextColor((selectPosition == layoutPosition) ? Color.argb(255, 181, 48, 47) : Color.BLACK);
                helper.addOnClickListener(R.id.item_meet_name_tv);
                break;
            case TYPE_LEVEL_1:
                final Level1Item lv1 = (Level1Item) item;
                String typeName = lv1.getFilepath();
                helper.setText(R.id.item_file_type_name_tv, typeName.substring(typeName.lastIndexOf("/") + 1, typeName.length()));
                TextView view1 = helper.getView(R.id.item_file_type_name_tv);
                view1.setTextColor((selectPosition == layoutPosition) ? Color.argb(255, 181, 48, 47) : Color.BLACK);
                helper.addOnClickListener(R.id.item_file_type_name_tv);
                break;
        }
    }

    public void setItemSelect(int position) {
        selectPosition = position;
    }

    public int getSelectPosition() {
        return selectPosition;
    }
}
