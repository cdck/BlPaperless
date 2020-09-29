package com.pa.paperless.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.LogUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.PermissionBean;
import com.pa.paperless.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

import static com.pa.paperless.utils.MyUtils.isHasPermission;

/**
 * Created by xlk on 2019/7/27.
 */
public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.ViewHolder> {
    private List<PermissionBean> datas;
    List<Integer> checks = new ArrayList<>();
    private ItemClickListener listener;

    public PermissionAdapter(List<PermissionBean> data) {
        this.datas = data;
    }

    @Override
    public PermissionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_permission_layout, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(PermissionAdapter.ViewHolder holder, int position) {
        PermissionBean permissionBean = datas.get(position);
        int permission = permissionBean.getPermission();
        InterfaceMember.pbui_Item_MemberDetailInfo memberInfo = permissionBean.getMemberInfo();
        int personid = memberInfo.getPersonid();
        holder.item_permission_cb.setText(String.valueOf(position + 1));
        holder.item_permission_cb.setChecked(checks.contains(memberInfo.getPersonid()));
        holder.item_permission_name.setText(MyUtils.b2s(memberInfo.getName()));
        holder.item_permission_screen.setText(isHasPermission(Macro.permission_code_screen, permission) ? "√" : "");
        holder.item_permission_projection.setText(isHasPermission(Macro.permission_code_projection, permission) ? "√" : "");
        holder.item_permission_upload.setText(isHasPermission(Macro.permission_code_upload, permission) ? "√" : "");
        holder.item_permission_download.setText(isHasPermission(Macro.permission_code_download, permission) ? "√" : "");
        holder.item_permission_vote.setText(isHasPermission(Macro.permission_code_vote, permission) ? "√" : "");
        holder.item_permission_cb.setChecked(checks.contains(personid));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.itemClick(personid);
            }
        });
    }

    public boolean isCheckAll() {
        return checks.size() == datas.size();
    }

    public void setCheckAll(boolean checked) {
        checks.clear();
        if (checked) {
            for (int i = 0; i < datas.size(); i++) {
                checks.add(datas.get(i).getMemberInfo().getPersonid());
            }
        }
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void itemClick(int personid);
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private final String TAG = "PermissionAdapter-->";

    public void notifychecks() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            int personid = datas.get(i).getMemberInfo().getPersonid();
            if (checks.contains(personid)) {
                ids.add(personid);
            }
        }
        checks = ids;
        notifyDataSetChanged();
    }

    public void setCheck(int personid) {
        if (checks.contains(personid)) {
            checks.remove(checks.indexOf(personid));
        } else {
            checks.add(personid);
        }
        LogUtil.e(TAG, "setSelect :  --> 选择的人员ID:" + checks.toString());
        notifyDataSetChanged();
    }

    public List<Integer> getChecks() {
        return checks;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public CheckBox item_permission_cb;
        public TextView item_permission_name;
        public TextView item_permission_screen;
        public TextView item_permission_projection;
        public TextView item_permission_upload;
        public TextView item_permission_download;
        public TextView item_permission_vote;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.item_permission_cb = rootView.findViewById(R.id.item_permission_cb);
            this.item_permission_name = rootView.findViewById(R.id.item_permission_name);
            this.item_permission_screen = rootView.findViewById(R.id.item_permission_screen);
            this.item_permission_projection = rootView.findViewById(R.id.item_permission_projection);
            this.item_permission_upload = rootView.findViewById(R.id.item_permission_upload);
            this.item_permission_download = rootView.findViewById(R.id.item_permission_download);
            this.item_permission_vote = rootView.findViewById(R.id.item_permission_vote);
        }

    }
}
