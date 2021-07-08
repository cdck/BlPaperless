package com.pa.paperless.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.DevControlAdapter;
import com.pa.paperless.data.bean.DevControlBean;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.MyUtils;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.pa.paperless.data.constant.EventType.DEV_REGISTER_INFORM;

/**
 * Created by xlk on 2017/10/31.
 * 设备控制
 */

public class DeviceControlFragment extends BaseFragment implements View.OnClickListener {
    private DevControlAdapter devControlAdapter;
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> pdevList = new ArrayList<>();
    private List<DevControlBean> devControlBeen = new ArrayList<>();
    private ArrayAdapter<String> spAdapter;
    private AlertDialog dialog;

    RecyclerView devRv;
    CheckBox all_check_cb;
    private LinearLayout top_view;
    private Button rising;
    private Button stop;
    private Button falling;
    private Button restart;
    private Button shutdown;
    private Button auxiliary_sign_in;
    private Button role_set;
    private Button boot;
    private Button document_open;
    private Button restart_app;
    List<Integer> memberIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.device_control_layout, container, false);
        initView(inflate);
        String[] stringArray = getResources().getStringArray(R.array.role_spinner);
        spAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, stringArray);
        EventBus.getDefault().register(this);
        queryMember();
        return inflate;
    }

    private void queryMember() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo info = jni.queryAttendPeople();
            memberIds.clear();
            if (info != null) {
                List<InterfaceMember.pbui_Item_MemberDetailInfo> itemList = info.getItemList();
                for (int i = 0; i < itemList.size(); i++) {
                    memberIds.add(itemList.get(i).getPersonid());
                }
            }
            queryDeviceInfo();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryDeviceInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo object = jni.queryDeviceInfo();
            pdevList.clear();
            if (object != null) {
                pdevList.addAll(object.getPdevList());
            }
            queryVenueDevice();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryVenueDevice() {
        InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo object = jni.placeDeviceRankingInfo(Values.roomId);
        List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> firsts = new ArrayList<>();
        List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> temps = new ArrayList<>();
        List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> devSeatInfos = new ArrayList<>();
        devControlBeen.clear();
        if (object != null) {
            List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> itemList = object.getItemList();
            for (int j = 0; j < itemList.size(); j++) {
                InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo info = itemList.get(j);
                if (info.getMemberid() != 0) {
                    firsts.add(info);
                } else {
                    temps.add(info);
                }
            }
        }
        for (int i = 0; i < memberIds.size(); i++) {
            for (int j = 0; j < firsts.size(); j++) {
                InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item = firsts.get(j);
                if (item.getMemberid() == memberIds.get(i)) {
                    devSeatInfos.add(item);
                    break;
                }
            }
        }
        devSeatInfos.addAll(temps);
        for (int i = 0; i < devSeatInfos.size(); i++) {
            InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seat = devSeatInfos.get(i);
            for (int j = 0; j < pdevList.size(); j++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo dev = pdevList.get(j);
                if (dev.getDevcieid() == seat.getDevid()) {
                    devControlBeen.add(new DevControlBean(dev, seat.getMembername().toStringUtf8(), seat.getRole()));
                    break;
                }
            }
        }
        for (int i = 0; i < pdevList.size(); i++) {
            InterfaceDevice.pbui_Item_DeviceDetailInfo dev = pdevList.get(i);
            int devId = dev.getDevcieid();
            boolean isDatabases = (devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_DB;
            boolean isTea = (devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_SERVICE;
            boolean isStream = (devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_CAPTURE;
            if (isDatabases || isTea || isStream) {
                LogUtils.e("最后添加:" + dev.getDevname().toStringUtf8() + ", isDatabases=" + isDatabases + ",isTea=" + isTea + ",isStream=" + isStream);
                devControlBeen.add(new DevControlBean(dev));
            }
        }
        if (devControlAdapter == null) {
            devControlAdapter = new DevControlAdapter(getContext(), devControlBeen);
            devRv.setLayoutManager(new LinearLayoutManager(getContext()));
            devRv.setAdapter(devControlAdapter);
            devControlAdapter.setItemClick((view, posion) -> {
                DevControlBean item = devControlBeen.get(posion);
                int devcieid = item.getDevice().getDevcieid();
                devControlAdapter.setChecks(devcieid);
                devControlAdapter.notifyDataSetChanged();
                all_check_cb.setChecked(devControlAdapter.isCheckAll());
            });
        } else {
            devControlAdapter.notifyChecks();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.SIGNIN_SEAT_INFORM: {
                queryVenueDevice();
                break;
            }
            //设备寄存器变更通知
            case EventType.DEV_REGISTER_INFORM:
                //界面状态变更通知
            case EventType.FACESTATUS_CHANGE_INFORM: {
                queryDeviceInfo();
                break;
            }
            case EventType.MEMBER_CHANGE_INFORM: {
                queryMember();
                break;
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            queryVenueDevice();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void initView(View inflate) {
        devRv = inflate.findViewById(R.id.dev_rv);

        all_check_cb = inflate.findViewById(R.id.all_check_cb);
        all_check_cb.setOnClickListener(this);
        rising = inflate.findViewById(R.id.rising);
        rising.setOnClickListener(this);

        stop = inflate.findViewById(R.id.stop);
        stop.setOnClickListener(this);

        falling = inflate.findViewById(R.id.falling);
        falling.setOnClickListener(this);
        restart_app = inflate.findViewById(R.id.restart_app);
        restart_app.setOnClickListener(this);
        restart = inflate.findViewById(R.id.restart);
        restart.setOnClickListener(this);
        shutdown = inflate.findViewById(R.id.shutdown);
        shutdown.setOnClickListener(this);
        auxiliary_sign_in = inflate.findViewById(R.id.auxiliary_sign_in);
        auxiliary_sign_in.setOnClickListener(this);
        role_set = inflate.findViewById(R.id.role_set);
        role_set.setOnClickListener(this);
        boot = inflate.findViewById(R.id.boot);
        boot.setOnClickListener(this);
        document_open = inflate.findViewById(R.id.document_open);
        document_open.setOnClickListener(this);
    }

    //如果使用PopupWindow则 Spinner的模式在xml文件中需要设置成dialog模式才有用
    private void showRolePop(List<Integer> devids) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.set_role_pop, null);
        Spinner spinner = inflate.findViewById(R.id.spinner);
        spinner.setAdapter(spAdapter);
        inflate.findViewById(R.id.ensure).setOnClickListener(v -> {
            // TODO: 2018/10/31 修改参会人员角色身份
            String selectedItem = (String) spinner.getSelectedItem();
            int index = spinner.getSelectedItemPosition();
            int role = 0;
            if (index == 1)
                role = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal.getNumber();
            else if (index == 2)
                role = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere.getNumber();
            else if (index == 3)
                role = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary.getNumber();
            else if (index == 4) role = InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin.getNumber();
            for (int i = 0; i < devids.size(); i++) {
                Integer devid = devids.get(i);
                for (int j = 0; j < devControlBeen.size(); j++) {
                    if (devControlBeen.get(j).getDevice().getDevcieid() == devid) {
                        if (devControlBeen.get(j).getDevice().getMemberid() != 0) {
                            //证明是参会人
                            jni.modifyMeetRanking(devControlBeen.get(j).getDevice().getMemberid(), role, devid);
                            if (dialog != null) dialog.dismiss();
                            break;
                        }
                    }
                }
            }
        });
        inflate.findViewById(R.id.cancel).setOnClickListener(v -> {
            if (dialog != null) dialog.dismiss();
        });
        builder.setView(inflate);
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        if (devControlAdapter == null) return;
        switch (v.getId()) {
            case R.id.all_check_cb: {
                boolean checked = all_check_cb.isChecked();
                all_check_cb.setChecked(checked);
                devControlAdapter.setCheckAll(checked);
                break;
            }
            case R.id.rising://升
                jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTUP.getNumber(), 0, 0, devControlAdapter.getChecks());
                break;
            case R.id.stop://停止
                jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTSTOP.getNumber(), 0, 0, devControlAdapter.getChecks());
                break;
            case R.id.falling://下降
                jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTDOWN.getNumber(), 0, 0, devControlAdapter.getChecks());
                break;
            case R.id.restart_app://软件重启
                jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_PROGRAMRESTART.getNumber(), 0, 0, devControlAdapter.getChecks());
                break;
            case R.id.restart://重启
                jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_REBOOT.getNumber(), 0, 0, devControlAdapter.getChecks());
                break;
            case R.id.shutdown://关机
                jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_SHUTDOWN.getNumber(), 0, 0, devControlAdapter.getChecks());
                break;
            //辅助签到
            case R.id.auxiliary_sign_in: {
                List<Integer> checks = devControlAdapter.getChecks();
                if (!checks.isEmpty()) {
                    jni.signAlterationOperate(checks);
                } else {
                    ToastUtils.showShort(R.string.please_choose_device);
                }
                break;
            }
            //角色设定
            case R.id.role_set: {
                List<Integer> checks = devControlAdapter.getChecks();
                if (!checks.isEmpty()) {
                    showRolePop(checks);
                } else {
                    ToastUtils.showShort(R.string.please_choose_device);
                }
                break;
            }
            case R.id.boot:
                break;
            case R.id.document_open: {
                List<Integer> devids = devControlAdapter.getChecks();
                if (devids.isEmpty()) {
                    ToastUtils.showShort(R.string.please_choose_device);
                    break;
                } else if (devids.size() > 1) {
                    ToastUtils.showShort(R.string.most_choose_one);
                    break;
                }
                Integer devid = devids.get(0);
                for (int i = 0; i < devControlBeen.size(); i++) {
                    int devcieid = devControlBeen.get(i).getDevice().getDevcieid();
                    if (devcieid == devid) {
                        InterfaceDevice.pbui_Item_DeviceDetailInfo device = devControlBeen.get(i).getDevice();
                        List<InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo> ipinfoList = device.getIpinfoList();
                        jni.modifyDeviceInfo(InterfaceMacro.Pb_DeviceModifyFlag.Pb_DEVICE_MODIFYFLAG_DEVICEFLAG.getNumber(),
                                devid, device.getDevname(), ipinfoList.get(0), device.getLiftgroupres0(), device.getLiftgroupres1(),
                                InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_OPENOUTSIDE.getNumber());
                        break;
                    }
                }
                break;
            }
        }
    }
}
