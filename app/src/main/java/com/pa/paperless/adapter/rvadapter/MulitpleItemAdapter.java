package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import com.pa.paperless.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.ReceiveMeetIMInfo;
import com.pa.paperless.utils.DateUtil;

import java.util.List;

/**
 * Created by Administrator on 2017/11/13.
 * 会议聊天 消息列表
 */

public class MulitpleItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final Context mContext;
    private List<ReceiveMeetIMInfo> data;


    public enum ITEM_TYPE {
        SEND,       //发送
        RECEIVW     //接收
    }

    public MulitpleItemAdapter(Context context, List<ReceiveMeetIMInfo> data) {
        this.data = data;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.RECEIVW.ordinal()) {
            return new LeftViewHolder(LayoutInflater.from(mContext).inflate(R.layout.chat_rlleft_item, parent, false));
        } else {
            return new RightViewHolder(LayoutInflater.from(mContext).inflate(R.layout.chat_rlright_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //根据
        ReceiveMeetIMInfo receiveMeetIMInfo = data.get(position);
        if (holder instanceof LeftViewHolder) {
            //消息类型
            int msgtype = receiveMeetIMInfo.getMsgtype();
            //参会人员角色
            int role = receiveMeetIMInfo.getRole();
            String msg = receiveMeetIMInfo.getMsg();
            int memberid = receiveMeetIMInfo.getMemberid();
            long utcsecond = receiveMeetIMInfo.getUtcsecond();
            String time = DateUtil.getTim(utcsecond);
            String name = receiveMeetIMInfo.getMemberName();
            LogUtil.e("MyLog", "MulitpleItemAdapter.onBindViewHolder 59行:  发送人的名字 --->>> " + name);
            ((LeftViewHolder) holder).left_name.setText(name + "  " + time);
            ((LeftViewHolder) holder).left_send_message.setText(msg);
        } else {
            List<String> strings = receiveMeetIMInfo.getNames();
            String time = DateUtil.getTim(receiveMeetIMInfo.getUtcsecond());
            String receiveNames = "";
            if (strings != null) {
                for (int i = 0; i < strings.size(); i++) {
                    if (i < strings.size() - 1) {
                        receiveNames += strings.get(i) + "，";
                    } else {
                        receiveNames += strings.get(i);
                    }
                }
            }
            ((RightViewHolder) holder).right_name.setText("发给 【" + receiveNames + "】 " + time);
            ((RightViewHolder) holder).right_send_message.setText(receiveMeetIMInfo.getMsg());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        //true 为接收的消息
        ReceiveMeetIMInfo receiveMeetIMInfo = data.get(position);
        return receiveMeetIMInfo.isType() ? ITEM_TYPE.RECEIVW.ordinal() : ITEM_TYPE.SEND.ordinal();
    }

    //接收 --->>> 其他人发送的消息
    class LeftViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView left_name;
        TextView left_send_message;

        public LeftViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            left_name = itemView.findViewById(R.id.left_name);
            left_send_message = itemView.findViewById(R.id.left_send_message);
        }
    }

    //发送 --->>> 第一人称发送的消息
    class RightViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView right_name;
        TextView right_send_message;

        public RightViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            right_name = itemView.findViewById(R.id.right_name);
            right_send_message = itemView.findViewById(R.id.right_send_message);
        }
    }
}
