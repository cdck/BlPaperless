package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pa.boling.paperless.R;
import com.pa.paperless.listener.ItemClickListener;

import java.util.List;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import static com.pa.paperless.service.App.isRedTheme;

/**
 * Created by Administrator on 2018/5/15.
 * 会议功能adapter
 */

public class FunctionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Integer> mData;
    private ItemClickListener mListener;
    private int mCheckedPosion;

    public FunctionAdapter(Context c, List<Integer> data) {
        mContext = c;
        mData = data;
    }

    public void setItenListener(ItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_function, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    private Drawable getDrawable(int attrid) {
        Resources.Theme theme = mContext.getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attrid, typedValue, true);
        Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(), typedValue.resourceId, null);
        return drawable;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (mData.get(position)) {
            case 0:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_agenda_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.agenda));
                break;
            case 1:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_meetdata_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.fun_text_meet_file_data));
                break;
            case 2:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_sharedata_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.fun_text_share_file));
                break;
            case 3:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_annotation_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.fun_text_postil_file));
                break;
            case 4:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_chat_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.fun_text_meet_chat));
                break;
            case 5:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_video_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.fun_text_live_video));
                break;
            case 6:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_board_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.fun_text_white_board));
                break;
            case 7:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_web_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.fun_text_web_net));
                break;
            case 8:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_questionnaire_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.fun_text_questionnaire_survey));
                break;
            case 9:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_signin_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.fun_text_sign_in));
                break;
//            case 10:
//                ((ViewHolder) holder).item_fun_iv.setBackground(getDrawable(R.attr.custom_attr_function_sign_info));
//                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_signin_select));
//                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.fun_text_camera_control));
//                break;
        }
        ((ViewHolder) holder).item_fun_iv.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(((ViewHolder) holder).item_fun_iv, holder.getLayoutPosition());
            }
        });
        /** ************ ******  item设置选中效果  ****** ************ **/
        if (position == mCheckedPosion) {
            ((ViewHolder) holder).item_fun_iv.setSelected(true);
            if (isRedTheme) {
                //切换修改标记 红色主题才使用
                ((ViewHolder) holder).item_fun_iv.setTextColor(mContext.getResources().getColor(R.color.fun_tv_color_p));
            }
        } else {
            ((ViewHolder) holder).item_fun_iv.setSelected(false);
            if (isRedTheme) {
                //切换修改标记 红色主题才使用
                ((ViewHolder) holder).item_fun_iv.setTextColor(mContext.getResources().getColor(R.color.fun_tv_color_n));
            }
        }
    }

    public void setCheckedId(int posion) {
        mCheckedPosion = posion;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public TextView item_fun_iv;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.item_fun_iv = rootView.findViewById(R.id.item_fun_iv);
        }
    }
}
