package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceFile;
import com.pa.boling.paperless.R;
import com.pa.paperless.utils.FileSizeUtil;
import com.pa.paperless.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xlk on 2019/7/25.
 */
public class OffMeetFileAdapter extends BaseAdapter {
    private Context mContext;
    private setLookListener mLookListener;
    private ItemSelectListener mSelectListener;
    public int ITEM_COUNT = 7;
    public int PAGE_NOW = 0;
    private List<Integer> checks = new ArrayList<>();

    public OffMeetFileAdapter(Context context, List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> data, int itemCount) {
        super(context);
        mContext = context;
        mDatas = data;
        ITEM_COUNT = itemCount;
    }

    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> getSelectFile() {
        List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> selFiles = new ArrayList<>();
        for (int i = 0; i < mDatas.size(); i++) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo o = (InterfaceFile.pbui_Item_MeetDirFileDetailInfo) mDatas.get(i);
            if (checks.contains(o.getMediaid())) {
                selFiles.add(o);
            }
        }
        return selFiles;
    }

    public void setSelect(int mediaid) {
        if (!checks.contains(mediaid)) {
            checks.add(mediaid);
        } else {
            checks.remove(checks.indexOf(mediaid));
        }
        notifyDataSetChanged();
    }
    public List<Integer> getChecks(){
        return checks;
    }

    public void clearSelectFiles() {
        checks.clear();
        notifyDataSetChanged();
    }

    public List<Integer> getSelected() {
        return checks;
    }

    @Override
    public int getCount() {
        //  数据的总数
        int ori = ITEM_COUNT * PAGE_NOW;
        //值的总个数-前几页的个数就是这一页要显示的个数，如果比默认的值小，说明这是最后一页，只需显示这么多就可以了
        if (mDatas.size() - ori < ITEM_COUNT) {
            return mDatas.size() - ori;
        } else {
            //如果比默认的值还要大，说明一页显示不完，还要用换一页显示，这一页用默认的值显示满就可以了。
            return ITEM_COUNT;
        }
    }

    private final String TAG = "OffMeetFileAdapter-->";

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_document_lv, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        InterfaceFile.pbui_Item_MeetDirFileDetailInfo o = (InterfaceFile.pbui_Item_MeetDirFileDetailInfo) mDatas.get(position + ITEM_COUNT * PAGE_NOW);
        holder.document_item_number.setText(position + ITEM_COUNT * PAGE_NOW + 1 + "");
        holder.document_item_filename.setText(o.getName().toStringUtf8());
        LogUtil.e(TAG, "上传者名称 -->" + o.getUploaderName().toStringUtf8());
        holder.document_item_uploadname.setText(o.getUploaderName().toStringUtf8());
        String fileSize = FileSizeUtil.FormatFileSize(o.getSize());
        holder.document_item_filesize.setText(fileSize);
        if (checks.contains(o.getMediaid())) {
            holder.rootView.setBackgroundColor(mContext.getResources().getColor(R.color.select_item_bg));
        } else {
            holder.rootView.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.rootView.setOnClickListener(v -> {
            if (mSelectListener != null) {
                mSelectListener.ItemSelect(position + ITEM_COUNT * PAGE_NOW, holder.rootView);
            }
        });
        holder.look.setOnClickListener(v -> {
            if (mLookListener != null) {
                //下载媒体id,文件名,文件大小
                mLookListener.onLookListener(o);
            }
        });
        return convertView;
    }

    public void setLookListener(setLookListener listener) {
        mLookListener = listener;
    }

    public void setItemSelectListener(ItemSelectListener listener) {
        mSelectListener = listener;
    }

    public interface ItemSelectListener {
        void ItemSelect(int posion, View view);
    }

    public interface setLookListener {
        void onLookListener(InterfaceFile.pbui_Item_MeetDirFileDetailInfo fileDetailInfo);
    }

    public static class ViewHolder {
        public View rootView;
        public TextView document_item_number;
        public TextView document_item_filename;
        public TextView document_item_filesize;
        public TextView document_item_uploadname;
        public Button look;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.document_item_number = (TextView) rootView.findViewById(R.id.document_item_number);
            this.document_item_filename = (TextView) rootView.findViewById(R.id.document_item_filename);
            this.document_item_filesize = (TextView) rootView.findViewById(R.id.document_item_filesize);
            this.document_item_uploadname = (TextView) rootView.findViewById(R.id.document_item_uploadname);
            this.look = (Button) rootView.findViewById(R.id.look);
        }

    }
}
