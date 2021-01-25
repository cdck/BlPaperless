package com.pa.paperless.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.OnLineProjectorAdapter;
import com.pa.paperless.adapter.rvadapter.ScreenControlAdapter;
import com.pa.paperless.data.bean.DevMember;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by xlk on 2018/11/5.
 * 屏幕管理
 */

public class ScreenManageFragment extends BaseFragment implements View.OnClickListener {

    private CheckBox projector_device_cb;
    private CheckBox target_device_cb;
    private RecyclerView screen_source_rv;
    private RecyclerView projector_device_rv;
    private RecyclerView target_device_rv;
    private Button preview_btn;
    private Button stop_preview_btn;
    private CheckBox mandatory_screen_cb;
    private Button launch_screen;
    private Button stop_task;
    private Button refresh;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos;
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos;
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onLinerPro;
    private List<DevMember> onLineMember;
    private OnLineProjectorAdapter onLineProjectorAdapter;
    private ScreenControlAdapter onlineMemberAdapter, onlineMemberRightAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.screen_manage_layout, container, false);
        initView(inflate);
        fun_queryAttendPeople();
        return inflate;
    }

    private void fun_queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo o = jni.queryAttendPeople();
            if (o == null) return;
            if (memberInfos == null) memberInfos = new ArrayList<>();
            else memberInfos.clear();
            memberInfos.addAll(o.getItemList());
            fun_queryDeviceInfo();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryDeviceInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo pbui_type_deviceDetailInfo = jni.queryDeviceInfo();
            if (pbui_type_deviceDetailInfo == null) return;
            if (deviceInfos == null) deviceInfos = new ArrayList<>();
            else deviceInfos.clear();
            deviceInfos.addAll(pbui_type_deviceDetailInfo.getPdevList());
            if (onLinerPro == null) onLinerPro = new ArrayList<>();
            else onLinerPro.clear();
            if (onLineMember == null) onLineMember = new ArrayList<>();
            else onLineMember.clear();
            for (int i = 0; i < deviceInfos.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo deviceInfo = deviceInfos.get(i);
                int devId = deviceInfo.getDevcieid();
                int memberId = deviceInfo.getMemberid();
                String devName = deviceInfo.getDevname().toStringUtf8();
                int netState = deviceInfo.getNetstate();
                int faceState = deviceInfo.getFacestate();
                if (netState == 1) {//在线
                    if ((devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_PROJECTIVE) {
                        onLinerPro.add(deviceInfo);
                    } else {
                        if (memberInfos != null && !memberInfos.isEmpty()) {
                            for (int j = 0; j < memberInfos.size(); j++) {
                                if (memberInfos.get(j).getPersonid() == memberId /*&& memberId!= Values.localMemberId*/) {
                                    onLineMember.add(new DevMember(memberInfos.get(j), devId));
                                }
                            }
                        }
                    }
                }
            }
            /** **** **  投影机  ** **** **/
            if (onLineProjectorAdapter == null) {
                onLineProjectorAdapter = new OnLineProjectorAdapter(onLinerPro);
                projector_device_rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                projector_device_rv.setAdapter(onLineProjectorAdapter);
            } else {
                onLineProjectorAdapter.notifyDataSetChanged();
                onLineProjectorAdapter.notifyChecks();
            }
            onLineProjectorAdapter.setItemClick((view, posion) -> {
                onLineProjectorAdapter.setCheck(onLinerPro.get(posion).getDevcieid());
                projector_device_cb.setChecked(onLineProjectorAdapter.isAllCheck());
                onLineProjectorAdapter.notifyDataSetChanged();
            });
            /** **** **  屏幕源  ** **** **/
            if (onlineMemberAdapter == null) {
                onlineMemberAdapter = new ScreenControlAdapter(onLineMember);
                screen_source_rv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
                screen_source_rv.setAdapter(onlineMemberAdapter);
            } else {
                onlineMemberAdapter.notifyDataSetChanged();
                onlineMemberAdapter.notifyChecks();
            }
            onlineMemberAdapter.setItemClick((view, posion) -> {
                onlineMemberAdapter.setSingleCheck(onLineMember.get(posion).getDevId());
                onlineMemberAdapter.notifyDataSetChanged();
            });
            /** **** **  目标设备  ** **** **/
            if (onlineMemberRightAdapter == null) {
                onlineMemberRightAdapter = new ScreenControlAdapter(onLineMember);
                target_device_rv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
                target_device_rv.setAdapter(onlineMemberRightAdapter);
            } else {
                onlineMemberRightAdapter.notifyDataSetChanged();
                onlineMemberRightAdapter.notifyChecks();
            }
            onlineMemberRightAdapter.setItemClick((view, posion) -> {
                onlineMemberRightAdapter.setCheck(onLineMember.get(posion).getDevId());
                target_device_cb.setChecked(onlineMemberRightAdapter.isAllCheck());
                onlineMemberRightAdapter.notifyDataSetChanged();
            });
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.FACESTATUS_CHANGE_INFORM:
                LogUtil.e("testTag -->", "getEventMessage :  界面状态变更通知 --> ");
                fun_queryAttendPeople();
                break;
            case EventType.MEMBER_CHANGE_INFORM:
                LogUtil.e("testTag -->", "getEventMessage :  参会人员变更通知 --> ");
                fun_queryAttendPeople();
                break;
            case EventType.DEV_REGISTER_INFORM://设备寄存器变更通知（监听参会人退出）
                LogUtil.e("testTag -->", "getEventMessage :  设备寄存器变更通知 --> ");
                fun_queryDeviceInfo();
//                fun_queryAttendPeople();
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) fun_queryAttendPeople();
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void initView(View inflate) {
        projector_device_cb = inflate.findViewById(R.id.projector_device_cb);
        target_device_cb = inflate.findViewById(R.id.target_device_cb);
        screen_source_rv = inflate.findViewById(R.id.screen_source_rv);
        projector_device_rv = inflate.findViewById(R.id.projector_device_rv);
        target_device_rv = inflate.findViewById(R.id.target_device_rv);
        preview_btn = inflate.findViewById(R.id.preview_btn);
        stop_preview_btn = inflate.findViewById(R.id.stop_preview_btn);
        mandatory_screen_cb = inflate.findViewById(R.id.mandatory_screen_cb);
        launch_screen = inflate.findViewById(R.id.launch_screen);
        stop_task = inflate.findViewById(R.id.stop_task);
        refresh = inflate.findViewById(R.id.refresh);

        projector_device_cb.setOnClickListener(this);
        target_device_cb.setOnClickListener(this);
        preview_btn.setOnClickListener(this);
        stop_preview_btn.setOnClickListener(this);
        launch_screen.setOnClickListener(this);
        stop_task.setOnClickListener(this);
        refresh.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        List<Integer> res = new ArrayList<>();
        res.add(0);
        switch (v.getId()) {
            case R.id.projector_device_cb:
                boolean checked = projector_device_cb.isChecked();
                projector_device_cb.setChecked(checked);
                if (onLineProjectorAdapter != null) {
                    onLineProjectorAdapter.setAllCheck(checked);
                }
                break;
            case R.id.target_device_cb:
                boolean ischecked = target_device_cb.isChecked();
                target_device_cb.setChecked(ischecked);
                if (onlineMemberRightAdapter != null) {
                    onlineMemberRightAdapter.setAllCheck(ischecked);
                }
                break;
            case R.id.preview_btn:
                if (onlineMemberAdapter == null) break;
                List<Integer> checks = onlineMemberAdapter.getChecks();
                if (checks.isEmpty()) {
                     ToastUtils.showShort(R.string.please_choose_screen_source);
                } else {
                    Integer devid = checks.get(0);
                    if (devid == Values.localDevId) {
                         ToastUtils.showShort(R.string.no_watch_oneself);
                    } else {
                        ArrayList<Integer> ids = new ArrayList<>();
                        ids.add(Values.localDevId);
                        jni.streamPlay(devid, 2, 0, res, ids);
                    }
                }
                break;
            case R.id.stop_preview_btn:

                break;
            case R.id.launch_screen:
                boolean isMandatory = mandatory_screen_cb.isChecked();
                if (onlineMemberAdapter == null) break;
                int sourceid = onlineMemberAdapter.getChecks().get(0);
                ArrayList<Integer> checks1 = onLineProjectorAdapter.getChecks();
                checks1.addAll(onlineMemberRightAdapter.getChecks());
                if (checks1.contains(Values.localDevId)) {
                    checks1.remove(checks1.indexOf(Values.localDevId));
                }
                int triggeruserval = 0;
                if (isMandatory) {//是否强制同屏
                    triggeruserval = InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER.getNumber();
                }
                jni.streamPlay(sourceid, 2, triggeruserval, res, checks1);
                break;
            case R.id.stop_task:
                if (onLineProjectorAdapter == null) break;
                if (onlineMemberRightAdapter == null) break;
                List<Integer> checks2 = onLineProjectorAdapter.getChecks();
                checks2.addAll(onlineMemberRightAdapter.getChecks());
                if (checks2.isEmpty())  ToastUtils.showShort(R.string.please_choose_target);
                else jni.stopResourceOperate(res, checks2);
                break;
            case R.id.refresh:
                if (onlineMemberAdapter == null) break;
                if (onLineProjectorAdapter == null) break;
                if (onlineMemberRightAdapter == null) break;
                onlineMemberAdapter.refresh();
                onLineProjectorAdapter.refresh();
                onlineMemberRightAdapter.refresh();
                break;
        }
    }
}
