package com.pa.paperless.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.helper.MaxLengthFilter;
import com.pa.paperless.service.App;
import com.pa.paperless.utils.LogUtil;

import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.NoticeAdapter;
import com.pa.paperless.adapter.rvadapter.OnLineProjectorAdapter;
import com.pa.paperless.adapter.rvadapter.ScreenControlAdapter;
import com.pa.paperless.data.bean.DevMember;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.PopUtils;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xlk on 2018/11/6.
 * 会议公告
 */

public class NoticeFragment extends BaseFragment implements View.OnClickListener {
    private RecyclerView notice_rv;
    private EditText notice_title_edt;
    private EditText content_edt;
    private Button add;
    private Button del;
    private Button modif;
    private Button push_inform;
    private Button close_inform;
    private NoticeAdapter adapter;
    private List<InterfaceBullet.pbui_Item_BulletDetailInfo> itemList;
    private final String TAG = "NoticeFragment-->";
    private int mPosion;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos;
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos;
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onLinerPro;
    private List<DevMember> onLineMember;
    private OnLineProjectorAdapter onLineProjectorAdapter;
    private ScreenControlAdapter onlineMemberAdapter;
    private boolean release;
    private boolean clicked;
    private boolean isFirst = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.notice_layout, container, false);
        initView(inflate);
        fun_queryNotice();
        return inflate;
    }

    private void fun_queryNotice() {
        try {
            InterfaceBullet.pbui_BulletDetailInfo pbui_bulletDetailInfo = jni.queryNotice();
            if (pbui_bulletDetailInfo == null) {
                if (itemList != null) itemList.clear();
                if (adapter != null) adapter.notifyDataSetChanged();
                return;
            }
            if (itemList == null) itemList = new ArrayList<>();
            else itemList.clear();
            itemList.addAll(pbui_bulletDetailInfo.getItemList());
            LogUtil.e(TAG, "NoticeFragment.fun_queryNotice :  查找到的公告数 --> " + itemList.size());
            if (adapter == null) {
                adapter = new NoticeAdapter(getContext(), itemList);
                notice_rv.setLayoutManager(new LinearLayoutManager(getContext()));
                notice_rv.setAdapter(adapter);
            } else adapter.notifyDataSetChanged();
            adapter.setItemClick((view, posion) -> {
                adapter.setSelect(posion);
                mPosion = posion;
                notice_title_edt.setText(MyUtils.b2s(itemList.get(posion).getTitle()));
                content_edt.setText(MyUtils.b2s(itemList.get(posion).getContent()));
            });
            if (isFirst && !itemList.isEmpty()) {
                isFirst = false;
                adapter.setSelect(0);
                notice_title_edt.setText(MyUtils.b2s(itemList.get(0).getTitle()));
                content_edt.setText(MyUtils.b2s(itemList.get(0).getContent()));
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.NOTICE_CHANGE_INFO://公告变更通知
                fun_queryNotice();
                break;
            case EventType.MEMBER_CHANGE_INFORM://参会人员变更通知
                fun_queryAttendPeople();
                break;
            case EventType.DEV_REGISTER_INFORM://设备寄存器变更通知（监听参会人退出）
                fun_queryAttendPeople();
                break;
            case EventType.FACESTATUS_CHANGE_INFORM://界面状态变更通知（参会人退出和加入）
                fun_queryAttendPeople();
                break;
        }
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
        notice_rv = inflate.findViewById(R.id.notice_rv);
        notice_title_edt = inflate.findViewById(R.id.notice_title_edt);
        content_edt = inflate.findViewById(R.id.content_edt);
        notice_title_edt.setFilters(new InputFilter[]{new MaxLengthFilter(Macro.title_max_length)});
        content_edt.setFilters(new InputFilter[]{new MaxLengthFilter(Macro.content_max_length)});
        add = inflate.findViewById(R.id.add);
        del = inflate.findViewById(R.id.del);
        modif = inflate.findViewById(R.id.modif);
        push_inform = inflate.findViewById(R.id.push_inform);
        close_inform = inflate.findViewById(R.id.close_inform);

        add.setOnClickListener(this);
        del.setOnClickListener(this);
        modif.setOnClickListener(this);
        push_inform.setOnClickListener(this);
        close_inform.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                addNotice();
                break;
            case R.id.del:
                delNotice();
                break;
            case R.id.modif:
                modifyNotice();
                break;
            case R.id.push_inform:
                release = true;
                clicked = true;
                fun_queryAttendPeople();
                break;
            case R.id.close_inform:
                release = false;
                clicked = true;
                fun_queryAttendPeople();
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) fun_queryNotice();
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
            } else {
                onLineProjectorAdapter.notifyDataSetChanged();
                onLineProjectorAdapter.notifyChecks();
            }
            /** **** **  参会人  ** **** **/
            if (onlineMemberAdapter == null) {
                onlineMemberAdapter = new ScreenControlAdapter(onLineMember);
            } else {
                onlineMemberAdapter.notifyDataSetChanged();
                onlineMemberAdapter.notifyChecks();
            }
            if (clicked) {
                clicked = false;
                chooseDevice();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void chooseDevice() {
        PopUtils.PopBuilder.createPopupWindow(R.layout.pop_choose_member, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, App.getRootView(),
                Gravity.CENTER, 0, 0, true, new PopUtils.ClickListener() {
                    @Override
                    public void setUplistener(PopUtils.PopBuilder builder) {
                        LogUtil.i(TAG, "setUplistener 进入方法...");
                        TextView title_tv = builder.getView(R.id.title);
                        title_tv.setText(release ? getString(R.string.choose_push) : getString(R.string.choose_stop_notice));
                        CheckBox member_cb = builder.getView(R.id.players_all_cb);
                        CheckBox cb_pro_mandatory = builder.getView(R.id.cb_mandatory);
                        CheckBox pro_cb = builder.getView(R.id.projector_all_cb);
                        member_cb.setText(getString(R.string.allchoose_count, onLineMember.size() + ""));
                        cb_pro_mandatory.setVisibility(View.GONE);
                        pro_cb.setText(getString(R.string.allchoose_count, onLinerPro.size() + ""));

                        RecyclerView players_rv = builder.getView(R.id.players_rl);
                        players_rv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
                        players_rv.setAdapter(onlineMemberAdapter);
                        onlineMemberAdapter.setItemClick((view, posion) -> {
                            onlineMemberAdapter.setCheck(onLineMember.get(posion).getDevId());
                            member_cb.setChecked(onlineMemberAdapter.isAllCheck());
                            onlineMemberAdapter.notifyDataSetChanged();
                        });
                        RecyclerView projector_rv = builder.getView(R.id.projector_rl);
                        projector_rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                        projector_rv.setAdapter(onLineProjectorAdapter);
                        onLineProjectorAdapter.setItemClick((view, posion) -> {
                            onLineProjectorAdapter.setCheck(onLinerPro.get(posion).getDevcieid());
                            pro_cb.setChecked(onLineProjectorAdapter.isAllCheck());
                            onLineProjectorAdapter.notifyDataSetChanged();
                        });

                        //全选按钮状态监听
                        member_cb.setOnClickListener(v -> {
                            boolean checked = member_cb.isChecked();
                            member_cb.setChecked(checked);
                            onlineMemberAdapter.setAllCheck(checked);
                        });
                        // 投影机全选 选择框
                        pro_cb.setOnClickListener(v -> {
                            boolean checked = pro_cb.isChecked();
                            pro_cb.setChecked(checked);
                            onLineProjectorAdapter.setAllCheck(checked);
                        });

                        builder.getView(R.id.ensure).setOnClickListener(v -> {
                            List<Integer> checks = onlineMemberAdapter.getChecks();
                            checks.addAll(onLineProjectorAdapter.getChecks());
                            if (!checks.isEmpty()) {
                                if (release) {//发布
                                    if (itemList != null && itemList.size() > mPosion) {
                                        jni.pushNotice(itemList.get(mPosion), checks);
                                        builder.dismiss();
                                    }
                                } else {//停止
                                    if (itemList != null && itemList.size() > mPosion) {
                                        jni.stopNotice(itemList.get(mPosion).getBulletid(), checks);
                                        builder.dismiss();
                                    }
                                }
                            } else {
                                ToastUtils.showShort(R.string.please_choose_device);
                            }
                        });
                        builder.getView(R.id.cancel).setOnClickListener(v -> builder.dismiss());
                    }

                    @Override
                    public void setOnDismissListener(PopUtils.PopBuilder builder) {

                    }
                });
    }

    private void delNotice() {
        if (itemList != null && itemList.size() > mPosion) {
            List<InterfaceBullet.pbui_Item_BulletDetailInfo> items = new ArrayList<>();
            items.add(itemList.get(mPosion));
            jni.deleteNotice(items);
            content_edt.setText("");
            notice_title_edt.setText("");
        } else {
            ToastUtils.showShort(R.string.please_choose_notice);
        }
    }

    private void addNotice() {
        String content_str = content_edt.getText().toString().trim();
        String title_str = notice_title_edt.getText().toString().trim();
        if (content_str.isEmpty()) {
            ToastUtils.showShort(R.string.please_input_content);
            return;
        }
        if (title_str.isEmpty()) {
            ToastUtils.showShort(R.string.please_input_title);
            return;
        }
//        if (content_str.length() > Macro.content_max_length) {
//            ToastUtils.showShort(R.string.err_bulletin_max_length, Macro.content_max_length);
//            return;
//        }
//        if (title_str.length() > Macro.title_max_length) {
//            ToastUtils.showShort(getString(R.string.err_title_max_length, Macro.title_max_length));
//            return;
//        }
        InterfaceBullet.pbui_Item_BulletDetailInfo.Builder builder = InterfaceBullet.pbui_Item_BulletDetailInfo.newBuilder();
        builder.setTitle(MyUtils.s2b(title_str));
        builder.setContent(MyUtils.s2b(content_str));
        InterfaceBullet.pbui_Item_BulletDetailInfo build = builder.build();
        List<InterfaceBullet.pbui_Item_BulletDetailInfo> notices = new ArrayList<>();
        notices.add(build);
        jni.addNotice(notices);
    }

    private void modifyNotice() {
        if (itemList != null && itemList.size() > mPosion) {
            InterfaceBullet.pbui_Item_BulletDetailInfo info = itemList.get(mPosion);
            String content_str = content_edt.getText().toString().trim();
            String title_str = notice_title_edt.getText().toString().trim();
            if (content_str.isEmpty()) {
                ToastUtils.showShort(R.string.please_input_content);
                return;
            }
            if (title_str.isEmpty()) {
                ToastUtils.showShort(R.string.please_input_title);
                return;
            }
//            if (content_str.length() > Macro.content_max_length) {
//                ToastUtils.showShort(R.string.err_bulletin_max_length, Macro.content_max_length);
//                return;
//            }
//            if (title_str.length() > Macro.title_max_length) {
//                ToastUtils.showShort(getString(R.string.err_title_max_length, Macro.title_max_length));
//                return;
//            }
            InterfaceBullet.pbui_Item_BulletDetailInfo.Builder builder = InterfaceBullet.pbui_Item_BulletDetailInfo.newBuilder();
            builder.setBulletid(info.getBulletid());
            builder.setContent(MyUtils.s2b(content_str));
            builder.setTitle(MyUtils.s2b(title_str));
            InterfaceBullet.pbui_Item_BulletDetailInfo build = builder.build();
            jni.modifNotice(build);
        } else {
            ToastUtils.showShort(R.string.please_choose_notice);
        }
    }
}
