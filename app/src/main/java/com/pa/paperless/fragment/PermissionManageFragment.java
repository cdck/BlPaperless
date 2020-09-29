package com.pa.paperless.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pa.paperless.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.PermissionAdapter;
import com.pa.paperless.data.bean.PermissionBean;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/**
 * Created by xlk on 2019/7/27.
 */
public class PermissionManageFragment extends BaseFragment implements View.OnClickListener {
    private CheckBox permission_cb;
    private RecyclerView permission_rv;
    private Button add_same_screen_btn;
    private Button add_projection_btn;
    private Button add_upload_btn;
    private Button add_download_btn;
    private Button add_vote_btn;
    private Button save_btn;
    private Button cancel_same_screen_btn;
    private Button cancel_projection_btn;
    private Button cancel_upload_btn;
    private Button cancel_download_btn;
    private Button cancel_vote_btn;
    private Button permission_refresh;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos;
    private List<PermissionBean> datas;
    private PermissionAdapter adapter;
    HashMap<Integer, Integer> hashMap = new HashMap<>();
    private List<InterfaceMember.pbui_Item_MemberPermission> saves;
    private final String TAG = "PermissionFragment-->";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.permission_manage_flayout, container, false);
        initView(inflate);
        queryAttend();
        return inflate;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.MEMBER_PERMISSION_INFORM:
                LogUtil.e(TAG, "getEventMessage :  参会人权限变更通知 --> ");
                queryAttendPeoplePermissions();
                break;
            case EventType.MEMBER_CHANGE_INFORM:
                LogUtil.e(TAG, "getEventMessage :  参会人员变更通知 --> ");
                queryAttend();
                break;
        }
    }

    private void queryAttend() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo attendPeople = jni.queryAttendPeople();
            if (attendPeople == null) return;
            memberInfos = attendPeople.getItemList();
            queryAttendPeoplePermissions();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryAttendPeoplePermissions() {
        try {
            InterfaceMember.pbui_Type_MemberPermission permissions = jni.queryAttendPeoplePermissions();
            if (permissions == null) return;
            if (memberInfos == null) return;
            if (datas == null) {
                datas = new ArrayList<>();
            } else {
                datas.clear();
            }
            hashMap.clear();
            List<InterfaceMember.pbui_Item_MemberPermission> itemList = permissions.getItemList();
            for (int i = 0; i < memberInfos.size(); i++) {
                InterfaceMember.pbui_Item_MemberDetailInfo detailInfo = memberInfos.get(i);
                int personid = detailInfo.getPersonid();
                for (int j = 0; j < itemList.size(); j++) {
                    InterfaceMember.pbui_Item_MemberPermission permission = itemList.get(j);
                    int memberid = permission.getMemberid();
                    int permission1 = permission.getPermission();
                    if (personid == memberid) {
                        LogUtil.e(TAG, "queryAttendPeoplePermissions :   --> memberid:" + memberid + ", permission1:" + permission1);
                        datas.add(new PermissionBean(detailInfo, permission1));
                        hashMap.put(personid, permission1);
                        break;
                    }
                }
            }
            if (adapter == null) {
                adapter = new PermissionAdapter(datas);
                permission_rv.setLayoutManager(new LinearLayoutManager(getContext()));
                permission_rv.setAdapter(adapter);
            } else {
                adapter.notifychecks();
            }
            adapter.setItemClickListener(new PermissionAdapter.ItemClickListener() {
                @Override
                public void itemClick(int personid) {
                    adapter.setCheck(personid);
                    permission_cb.setChecked(adapter.isCheckAll());
                }
            });
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            queryAttend();
        }
    }

    private void initView(View inflate) {
        permission_cb = (CheckBox) inflate.findViewById(R.id.permission_cb);
        permission_rv = (RecyclerView) inflate.findViewById(R.id.permission_rv);
        add_same_screen_btn = (Button) inflate.findViewById(R.id.add_same_screen_btn);
        add_projection_btn = (Button) inflate.findViewById(R.id.add_projection_btn);
        add_upload_btn = (Button) inflate.findViewById(R.id.add_upload_btn);
        add_download_btn = (Button) inflate.findViewById(R.id.add_download_btn);
        add_vote_btn = (Button) inflate.findViewById(R.id.add_vote_btn);
//        save_btn = (Button) inflate.findViewById(R.id.save_btn);
        cancel_same_screen_btn = (Button) inflate.findViewById(R.id.cancel_same_screen_btn);
        cancel_projection_btn = (Button) inflate.findViewById(R.id.cancel_projection_btn);
        cancel_upload_btn = (Button) inflate.findViewById(R.id.cancel_upload_btn);
        cancel_download_btn = (Button) inflate.findViewById(R.id.cancel_download_btn);
        cancel_vote_btn = (Button) inflate.findViewById(R.id.cancel_vote_btn);
//        permission_refresh = (Button) inflate.findViewById(R.id.permission_refresh);

        permission_cb.setOnClickListener(this);
        add_same_screen_btn.setOnClickListener(this);
        add_projection_btn.setOnClickListener(this);
        add_upload_btn.setOnClickListener(this);
        add_download_btn.setOnClickListener(this);
        add_vote_btn.setOnClickListener(this);
//        save_btn.setOnClickListener(this);
        cancel_same_screen_btn.setOnClickListener(this);
        cancel_projection_btn.setOnClickListener(this);
        cancel_upload_btn.setOnClickListener(this);
        cancel_download_btn.setOnClickListener(this);
        cancel_vote_btn.setOnClickListener(this);
//        permission_refresh.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (adapter == null) return;
        switch (v.getId()) {
            case R.id.permission_cb:
                permission_cb.setChecked(permission_cb.isChecked());
                adapter.setCheckAll(permission_cb.isChecked());
                break;
            case R.id.add_same_screen_btn:
                update(true, 1, 1);
                break;
            case R.id.add_projection_btn:
                update(true, 2, 2);
                break;
            case R.id.add_upload_btn:
                update(true, 3, 4);
                break;
            case R.id.add_download_btn:
                update(true, 4, 8);
                break;
            case R.id.add_vote_btn:
                update(true, 5, 16);
                break;
            case R.id.cancel_same_screen_btn:
                update(false, 1, 1);
                break;
            case R.id.cancel_projection_btn:
                update(false, 2, 2);
                break;
            case R.id.cancel_upload_btn:
                update(false, 3, 4);
                break;
            case R.id.cancel_download_btn:
                update(false, 4, 8);
                break;
            case R.id.cancel_vote_btn:
                update(false, 5, 16);
                break;
//            case R.id.save_btn:
//
//                break;
//            case R.id.permission_refresh:
//
//                break;
        }
    }

    private void update(boolean isadd, int code, int value) {
        List<Integer> checks = adapter.getChecks();
        if (checks.isEmpty()) {
             ToastUtil.showToast(R.string.tip_select_member);
            return;
        }
        if (saves == null) {
            saves = new ArrayList<>();
        } else {
            saves.clear();
        }
        for (int i = 0; i < checks.size(); i++) {
            Integer personid = checks.get(i);
            Integer permission = hashMap.get(personid);
            List<Integer> choose = MyUtils.getChoose(permission);
            if (isadd) {
                if (!choose.contains(code)) {
                    permission += value;
                }
            } else {
                if (choose.contains(code)) {
                    permission -= value;
                }
            }
            InterfaceMember.pbui_Item_MemberPermission.Builder builder = InterfaceMember.pbui_Item_MemberPermission.newBuilder();
            builder.setMemberid(personid);
            builder.setPermission(permission);
            InterfaceMember.pbui_Item_MemberPermission build = builder.build();
            saves.add(build);
        }
        if (!saves.isEmpty()) {
            jni.saveMemberPermission(saves);
        }
    }
}
