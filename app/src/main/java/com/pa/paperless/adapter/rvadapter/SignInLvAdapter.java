package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.SignInBean;
import com.pa.paperless.utils.ConvertUtil;
import com.pa.paperless.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/11/1.
 * 签到信息adapter
 */

public class SignInLvAdapter extends BaseAdapter {

    private List<SignInBean> mDataes;
    private click mListener;
    public List<Integer> clickIds = new ArrayList<>();

    @Override
    public int getCount() {
        return mDataes != null ? mDataes.size() : 0;
    }

    public SignInLvAdapter(Context context, List<SignInBean> data) {
        super(context);
        mDataes = data;
    }

    public void setListener(click click) {
        mListener = click;
    }

    public interface click {
        void showPic(int posion, ByteString picdata);
    }

    public void setSelected(int id) {
        LogUtil.i("xlklog", "选中人员id=" + id);
        if (clickIds.contains(id))
            clickIds.remove(clickIds.indexOf(id));
        else clickIds.add(id);
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedIds() {
        return clickIds;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.right_signin_item, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        SignInBean bean = mDataes.get(i);
        boolean contains = clickIds.contains(bean.getId());
        int textColor = contains ? Color.WHITE : Color.BLACK;
        int backColor = contains ? Color.argb(100, 82, 156, 240) :
                Color.WHITE;
        holder.signin_item_number.setText(String.valueOf(i + 1));

        holder.rootView.setBackgroundColor(contains ? backColor : Color.argb(100, 184, 187, 190));

        holder.signin_item_number.setTextColor(textColor);
        holder.signin_item_number.setBackgroundColor(backColor);

        holder.signin_item_name.setTextColor(textColor);
        holder.signin_item_name.setBackgroundColor(backColor);

        holder.signin_item_signtime.setTextColor(textColor);
        holder.signin_item_signtime.setBackgroundColor(backColor);

        holder.signin_item_signstate.setTextColor(textColor);
        holder.signin_item_signstate.setBackgroundColor(backColor);


        holder.signin_item_name.setText(bean.getSignin_name());
        holder.signin_item_signtime.setText(bean.getSignin_date());
        holder.signin_item_signtype.setBackgroundColor(Color.WHITE);
        ByteString pic_data = bean.getPic_data();
        if (!(holder.signin_item_signtime.getText().toString().trim().isEmpty())) {
            holder.signin_item_signstate.setText(R.string.signed_in);
            if (pic_data != null) {
                holder.signin_item_signtype.setOnClickListener(v -> {
                    if (mListener != null) {
                        mListener.showPic(i, pic_data);
                    }
                });
                Bitmap bitmap = ConvertUtil.bs2bmp(pic_data);
                holder.signin_item_signtype.setBackground(new BitmapDrawable(bitmap));
            } else {
                holder.signin_item_signtype.setText(getSignType(bean.getSign_in()));
                holder.signin_item_signtype.setTextColor(textColor);
                holder.signin_item_signtype.setBackgroundColor(backColor);
            }
        } else {
            holder.signin_item_signstate.setText(R.string.not_sign_in);
            holder.signin_item_signtype.setText("");
        }
        return view;
    }

    private int getSignType(int sign_in) {
        int strRes = R.string.default_str;
        switch (sign_in) {
            case 0:
                strRes = R.string.direct_sign_in;
//                strRes = "直接签到";
                break;
            case 1:
                strRes = R.string.local_pwd_sign_in;
//                strRes = "个人密码签到";
                break;
            case 2:
                strRes = R.string.photo_sign_in;
//                strRes = "拍照(手写)签到";
                break;
            case 3:
                strRes = R.string.meeting_pwd_sign_in;
//                strRes = "会议密码签到";
                break;
            case 4:
                strRes = R.string.meeting_pwd_and_photo_sign_in;
//                strRes = "会议密码+(手写)签到";
                break;
            case 5:
                strRes = R.string.local_pwd_and_photo_sign_in;
//                strRes = "个人密码+(手写)签到";
                break;
        }
        return strRes;
    }

    class ViewHolder {
        public View rootView;
        public LinearLayout signin_item_root;
        public TextView signin_item_number;
        public TextView signin_item_name;
        public TextView signin_item_signtime;
        public Button signin_item_signtype;
        public TextView signin_item_signstate;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.signin_item_root = rootView.findViewById(R.id.signin_item_root);
            this.signin_item_number = rootView.findViewById(R.id.signin_item_number);
            this.signin_item_name = rootView.findViewById(R.id.signin_item_name);
            this.signin_item_signtime = rootView.findViewById(R.id.signin_item_signtime);
            this.signin_item_signtype = rootView.findViewById(R.id.signin_item_signtype);
            this.signin_item_signstate = rootView.findViewById(R.id.signin_item_signstate);
        }
    }
}
