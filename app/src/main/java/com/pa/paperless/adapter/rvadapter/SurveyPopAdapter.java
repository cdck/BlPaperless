package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceVote;
import com.pa.boling.paperless.R;
import com.pa.paperless.listener.ItemClickListener;

import java.util.List;

/**
 * Created by xlk on 2018/11/3.
 */

public class SurveyPopAdapter extends RecyclerView.Adapter<SurveyPopAdapter.ViewHolder> {


    private Context mContext;
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> mData;
    private ItemClickListener mListener;
    private int mPosition;

    public SurveyPopAdapter(Context context, List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> data) {
        mContext = context;
        mData = data;
    }

    public void setListener(ItemClickListener listener) {
        mListener = listener;
    }

    public void setCheck(int position) {
        mPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public SurveyPopAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.survey_pop_item, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(SurveyPopAdapter.ViewHolder holder, int position) {
        holder.survey_popitem_id.setText(mData.get(position).getVoteid() + "");
        holder.survey_popitem_content.setText(mData.get(position).getContent().toStringUtf8());
        String surveyType = getSurveyType(mData.get(position).getType());
        holder.survey_popitem_type.setText(surveyType);
        holder.survey_popitem_mode.setText(mData.get(position).getMode() == 0 ? mContext.getString(R.string.no) : mContext.getString(R.string.yes));
        String voteState = getVoteState(mData.get(position).getVotestate());
        holder.survey_popitem_state.setText(voteState);
        List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = mData.get(position).getItemList();
        holder.survey_popitem_a.setText("");
        holder.survey_popitem_b.setText("");
        holder.survey_popitem_c.setText("");
        holder.survey_popitem_d.setText("");
        holder.survey_popitem_e.setText("");
        for (int i = 0; i < optionInfo.size(); i++) {
            if (i == 0) {
                holder.survey_popitem_a.setText(optionInfo.get(i).getText().toStringUtf8());
            }
            if (i == 1) {
                holder.survey_popitem_b.setText(optionInfo.get(i).getText().toStringUtf8());
            }
            if (i == 2) {
                holder.survey_popitem_c.setText(optionInfo.get(i).getText().toStringUtf8());
            }
            if (i == 3) {
                holder.survey_popitem_d.setText(optionInfo.get(i).getText().toStringUtf8());
            }
            if (i == 4) {
                holder.survey_popitem_e.setText(optionInfo.get(i).getText().toStringUtf8());
            }
        }
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(holder.itemView, position);
            }
        });
        /** ************ ******  item设置选中效果  ****** ************ **/
        if (position == mPosition) {
            int color = mContext.getResources().getColor(R.color.select_item_bg);
            setViewColor(holder, color);
//            holder.rootView.setBackgroundColor(color);
        } else {
            setViewColor(holder, Color.WHITE);
//            holder.rootView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void setViewColor(ViewHolder holder, int color) {
        holder.survey_popitem_id.setBackgroundColor(color);
        holder.survey_popitem_content.setBackgroundColor(color);
        holder.survey_popitem_type.setBackgroundColor(color);
        holder.survey_popitem_mode.setBackgroundColor(color);
        holder.survey_popitem_state.setBackgroundColor(color);
        holder.survey_popitem_a.setBackgroundColor(color);
        holder.survey_popitem_b.setBackgroundColor(color);
        holder.survey_popitem_c.setBackgroundColor(color);
        holder.survey_popitem_d.setBackgroundColor(color);
        holder.survey_popitem_e.setBackgroundColor(color);
    }


    private String getVoteState(int votestate) {
        String str = "";
        switch (votestate) {
            case 0:
                str = mContext.getString(R.string.pb_vote_notvote);
                break;
            case 1:
                str = mContext.getString(R.string.pb_vote_voteing);
                break;
            case 2:
                str = mContext.getString(R.string.pb_vote_endvote);
                break;
        }
        return str;
    }

    private String getSurveyType(int type) {
        String str = "";
        switch (type) {
            case 0:
                str = mContext.getString(R.string.pb_vote_type_many);
                break;
            case 1:
                str = mContext.getString(R.string.pb_vote_type_single);
                break;
            case 2:
                str = mContext.getString(R.string.pb_vote_type_4_5);
                break;
            case 3:
                str = mContext.getString(R.string.pb_vote_type_3_5);
                break;
            case 4:
                str = mContext.getString(R.string.pb_vote_type_2_5);
                break;
            case 5:
                str = mContext.getString(R.string.pb_vote_type_2_3);
                break;
        }
        return str;
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public TextView survey_popitem_id;
        public TextView survey_popitem_content;
        public TextView survey_popitem_type;
        public TextView survey_popitem_mode;
        public TextView survey_popitem_state;
        public TextView survey_popitem_a;
        public TextView survey_popitem_b;
        public TextView survey_popitem_c;
        public TextView survey_popitem_d;
        public TextView survey_popitem_e;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.survey_popitem_id = rootView.findViewById(R.id.survey_popitem_id);
            this.survey_popitem_content = rootView.findViewById(R.id.survey_popitem_content);
            this.survey_popitem_type = rootView.findViewById(R.id.survey_popitem_type);
            this.survey_popitem_mode = rootView.findViewById(R.id.survey_popitem_mode);
            this.survey_popitem_state = rootView.findViewById(R.id.survey_popitem_state);
            this.survey_popitem_a = rootView.findViewById(R.id.survey_popitem_a);
            this.survey_popitem_b = rootView.findViewById(R.id.survey_popitem_b);
            this.survey_popitem_c = rootView.findViewById(R.id.survey_popitem_c);
            this.survey_popitem_d = rootView.findViewById(R.id.survey_popitem_d);
            this.survey_popitem_e = rootView.findViewById(R.id.survey_popitem_e);
        }

    }
}
