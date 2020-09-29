package com.pa.paperless.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.MeetDirFileInfo;
import com.pa.paperless.ui.MarqueeTextView;
import com.pa.paperless.utils.FileSizeUtil;

import java.util.List;

/**
 * Created by Administrator on 2017/11/3.
 * 文档/图片...分类使用 adapter
 */

public class TypeFileAdapter extends BaseAdapter {

    private final Context mContext;
    public int ITEM_COUNT = 6;
    public int PAGE_NOW = 0;
    private setLookListener mLookListener;
    private ItemSelectListener mSelectListener;
    private int checkFileId = -1;

    public TypeFileAdapter(Context context, List<MeetDirFileInfo> data) {
        super(context);
        mContext = context;
        mDatas = data;
    }

    public void setCheck(int mediaid) {
        if (checkFileId == mediaid) checkFileId = -1;
        else checkFileId = mediaid;
        notifyDataSetChanged();
    }

    public MeetDirFileInfo getCheckedFile() {
        for (int i = 0; i < mDatas.size(); i++) {
            MeetDirFileInfo o = (MeetDirFileInfo) mDatas.get(i);
            if (o.getMediaId() == checkFileId) {
                return o;
            }
        }
        return null;
    }

    public void notifyChecks() {
        int temp = -1;
        for (int i = 0; i < mDatas.size(); i++) {
            MeetDirFileInfo o = (MeetDirFileInfo) mDatas.get(i);
            int mediaId = o.getMediaId();
            if (checkFileId == mediaId) {
                temp = mediaId;
                break;
            }
        }
        checkFileId = temp;
        notifyDataSetChanged();
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

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.item_document_lv, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        /**
         * 这一步是重点，括号中的值是获取到item的准确索引
         */
        final MeetDirFileInfo bean = (MeetDirFileInfo) mDatas.get(i + ITEM_COUNT * PAGE_NOW);
        holder.document_item_number.setText(String.valueOf(i + ITEM_COUNT * PAGE_NOW + 1));
        holder.document_item_filename.setText(bean.getFileName());
        holder.document_item_filesize.setText(FileSizeUtil.FormetFileSize(bean.getSize()));
        holder.document_item_uploadname.setText(bean.getUploader_name());
        final int mediaId = bean.getMediaId();
        final String fileName = bean.getFileName();
        holder.document_item_look.setOnClickListener(view1 -> {
            if (mLookListener != null) {
                //下载媒体id,文件名,文件大小
                mLookListener.onLookListener(bean, mediaId, fileName, bean.getSize());
            }
        });
        holder.rootView.setOnClickListener(v -> {
            if (mSelectListener != null) {
                mSelectListener.ItemSelect(i + ITEM_COUNT * PAGE_NOW, holder.rootView);
            }
        });
        if (checkFileId == mediaId)
            holder.rootView.setBackgroundColor(mContext.getResources().getColor(R.color.select_item_bg));
        else holder.rootView.setBackgroundColor(Color.TRANSPARENT);
        return view;
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
        void onLookListener(MeetDirFileInfo fileInfo, int mediaId, String filename, long filesize);
    }


    public static class ViewHolder {
        public View rootView;
        public TextView document_item_number;
        public MarqueeTextView document_item_filename;
        public TextView document_item_filesize;
        public TextView document_item_uploadname;
        public Button document_item_look;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.document_item_number = (TextView) rootView.findViewById(R.id.document_item_number);
            this.document_item_filename = (MarqueeTextView) rootView.findViewById(R.id.document_item_filename);
            this.document_item_filesize = (TextView) rootView.findViewById(R.id.document_item_filesize);
            this.document_item_uploadname = (TextView) rootView.findViewById(R.id.document_item_uploadname);
            this.document_item_look = (Button) rootView.findViewById(R.id.look);
        }
    }
}

