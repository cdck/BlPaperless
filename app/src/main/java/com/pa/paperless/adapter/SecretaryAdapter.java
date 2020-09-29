package com.pa.paperless.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.listener.ItemClickListener;

import java.util.List;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import static com.pa.paperless.service.ShotApplication.isRedTheme;

/**
 * Created by Administrator on 2018/5/15.
 * 会议功能adapter
 */

public class SecretaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Integer> mData;
    private ItemClickListener mListener;
    private int mCheckedPosion;

    public SecretaryAdapter(Context c, List<Integer> data) {
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
            case Macro.PB_MEET_FUN_CODE_DEV_CONTROL:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_dev_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.dev_control));
                break;
            case Macro.PB_MEET_FUN_CODE_CAMERA_CONTROL:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_camera_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.camera__control));
                break;
            case Macro.PB_MEET_FUN_CODE_VOTE_MANAGE:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_vote_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.vote_manage));
                break;
            case Macro.PB_MEET_FUN_CODE_ELECTORAL_MANAGE:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_electionctrl_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.electoral_manage));
                break;
            case Macro.PB_MEET_FUN_CODE_VOTE_RESULT:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_voteresult_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.vote_result));
                break;
            case Macro.PB_MEET_FUN_CODE_ELECTORAL_RESULT:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_electionreview_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.electoral_result));
                break;
            case Macro.PB_MEET_FUN_CODE_SCREEN_MANAGE:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_screenctrl_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.screen_manage));
                break;
            case Macro.PB_MEET_FUN_CODE_PERMISSION_MANAGE:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_screenrecord_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.permission_manage));
                break;
            case Macro.PB_MEET_FUN_CODE_COMMUNIQUE:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_bulletin_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.communique));
                break;
            case Macro.PB_MEET_FUN_CODE_OPEN_BACKGROUND:
                ((ViewHolder) holder).item_fun_iv.setBackground(mContext.getResources().getDrawable(R.drawable.menu_subwin_select));
                ((ViewHolder) holder).item_fun_iv.setText(mContext.getString(R.string.open_the_background));
                break;
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
