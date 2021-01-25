package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import com.pa.paperless.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.DevMember;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/22.
 * 聊天参会人列表
 */

public class MemberListAdapter extends BaseAdapter {
    private final String TAG = "MemberListAdapter-->";
    private final Context mContext;
    private final List<DevMember> datas;
    private List<Integer> checks;

    public MemberListAdapter(Context context, List<DevMember> data) {
        mContext = context;
        datas = data;
        checks = new ArrayList<>();
    }

    public void notifyChecks() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            if (checks.contains(datas.get(i).getMemberDetailInfo().getPersonid())) {
                ids.add(datas.get(i).getMemberDetailInfo().getPersonid());
            }
        }
        checks = ids;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas != null ? datas.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return datas != null ? datas.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * 获取全部选中的参会人ID
     */
    public List<Integer> getCheckedId() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            if (checks.contains(datas.get(i).getMemberDetailInfo().getPersonid())) {
                ids.add(datas.get(i).getMemberDetailInfo().getPersonid());
            }
        }
        checks = ids;
        LogUtil.e(TAG, "MemberListAdapter.getCheckedId :  选中的人员 --> " + checks.toString());
        return checks;
    }

    public List<String> getCheckedName() {
        List<String> checkName = new ArrayList<>();
        if (checks.size() == datas.size()) {
            //全部选中
            checkName.add("全体人员");
            return checkName;
        }
        for (int i = 0; i < datas.size(); i++) {
            //获取选中的人员的名字
            if (checks.contains(datas.get(i).getMemberDetailInfo().getPersonid())) {
                String name = datas.get(i).getMemberDetailInfo().getName().toStringUtf8();
                checkName.add(name);
                if (checkName.size() > 3) {
                    //如果 checkName 中的名字(选中的个数)超过3个 就在最后添加省略号并直接 return
                    checkName.set(checkName.size() - 1, "... 等人");
                    return checkName;
                }
            }
        }
        return checkName;
    }

    public void setCheck(Integer o) {
        if (checks.contains(o)) checks.remove(checks.indexOf(o));
        else checks.add(o);
        notifyDataSetChanged();
    }

    public boolean isAllCheck() {
        return checks.size() == datas.size();
    }

    /**
     * 全选 按钮监听
     */
    public boolean setAllChecked() {
        boolean isall;
        if (checks.size() == datas.size()) {
            checks.clear();
            isall = false;
        } else {
            checks.clear();
            for (int i = 0; i < datas.size(); i++) {
                checks.add(datas.get(i).getMemberDetailInfo().getPersonid());
            }
            isall = true;
        }
        notifyDataSetChanged();
        return isall;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_chat, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final DevMember memberInfo = datas.get(i);
        holder.member_tv.setText(memberInfo.getMemberDetailInfo().getName().toStringUtf8());
        //设置names集合中的值
        holder.member_tv.setSelected(checks.contains(memberInfo.getMemberDetailInfo().getPersonid()));
        return view;
    }

    public static class ViewHolder {
        public View rootView;
        public TextView member_tv;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.member_tv = (TextView) rootView.findViewById(R.id.member_tv);
        }
    }
}
