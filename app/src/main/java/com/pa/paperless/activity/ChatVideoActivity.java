package com.pa.paperless.activity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.MeetChatMemberAdapter;
import com.pa.paperless.data.bean.ChatVideoMemberBean;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.ui.VideoChatView;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.ToastUtil;
import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ChatVideoActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "ChatVideoActivity-->";
    private CheckBox pop_video_chat_all;
    private RecyclerView pop_video_chat_rv;
    private RadioGroup pop_video_chat_radio;
    private RadioButton pop_video_chat_paging;
    private RadioButton pop_video_chat_intercom;
    private ImageView pop_video_chat_close;
    private CheckBox video_chat_ask_cb;
    private Button pop_video_chat_launch;
    private Button pop_video_chat_stop;
    private VideoChatView video_chat_view;
    private NativeUtil jni = NativeUtil.getInstance();
    private List<Integer> ids = new ArrayList<>();
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos = new ArrayList<>();
    private List<ChatVideoMemberBean> onlineMembers = new ArrayList<>();
    private MeetChatMemberAdapter memberAdapter;
    private int work_state = 0;//=0空闲,=1寻呼中，=2对讲中
    public static boolean isChatingOpened = false;//当前是否已打开该界面
    private int mInviteflag, mOperdeviceid;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_video);
        initView();
        isChatingOpened = true;
        mInviteflag = getIntent().getIntExtra(Macro.extra_inviteflag, -1);
        mOperdeviceid = getIntent().getIntExtra(Macro.extra_operdeviceid, -1);
        LogUtil.d(TAG, "onCreate --> 收到该设备ID的设备交互= " + mOperdeviceid);
        initial();
        EventBus.getDefault().register(this);
        queryAttendPeople();
    }

    private String getMemberName() {
        for (int i = 0; i < onlineMembers.size(); i++) {
            ChatVideoMemberBean devMember = onlineMembers.get(i);
            if (devMember.getDeviceDetailInfo().getDevcieid() == mOperdeviceid) {
                return devMember.getMemberDetailInfo().getName().toStringUtf8();
            }
        }
        return "";
    }

    private void initial() {
        pop_video_chat_paging.setOnClickListener(v -> {
            LogUtil.i(TAG, "initial -->" + "选择寻呼");
            video_chat_view.createDefaultView(1);
        });
        pop_video_chat_intercom.setOnClickListener(v -> {
            LogUtil.i(TAG, "initial -->" + "选择对讲");
            video_chat_view.createDefaultView(2);
        });
        if (mOperdeviceid == -1) {
            work_state = 0;
            setEnable();
            video_chat_view.createDefaultView(1);
            pop_video_chat_paging.setChecked(true);
        } else {
            if ((mInviteflag & InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_SIMPLEX_VALUE) ==
                    InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_SIMPLEX_VALUE) {
                createPaging();
            } else {
                createIntercom();
            }
        }
    }

    private void queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo attendPeople = jni.queryAttendPeople();
            if (attendPeople == null) {
                return;
            }
            memberInfos.clear();
            memberInfos.addAll(attendPeople.getItemList());
            queryDeviceInfo();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryDeviceInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDeviceInfo();
            if (deviceDetailInfo == null) {
                return;
            }
            List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceDetailInfos = deviceDetailInfo.getPdevList();
            onlineMembers.clear();
            for (int i = 0; i < deviceDetailInfos.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo detailInfo = deviceDetailInfos.get(i);
                int devcieid = detailInfo.getDevcieid();
                int memberid = detailInfo.getMemberid();
                int facestate = detailInfo.getFacestate();
                int netstate = detailInfo.getNetstate();
                if (facestate == 1 && netstate == 1 && devcieid != Values.localDevId) {
                    for (int j = 0; j < memberInfos.size(); j++) {
                        InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo = memberInfos.get(j);
                        int personid = memberDetailInfo.getPersonid();
                        if (personid == memberid) {
                            onlineMembers.add(new ChatVideoMemberBean(detailInfo, memberDetailInfo));
                        }
                    }
                }
            }
            updateRv();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void setRvLayoutManager(boolean canScroll) {
        layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return canScroll;
            }
        };
        pop_video_chat_rv.setLayoutManager(layoutManager);
    }

    private void updateRv() {
        if (memberAdapter == null) {
            memberAdapter = new MeetChatMemberAdapter(R.layout.item_chat_member, onlineMembers);
            if (layoutManager == null) {
                setRvLayoutManager(true);
            }
            pop_video_chat_rv.setLayoutManager(layoutManager);
            pop_video_chat_rv.setAdapter(memberAdapter);
            memberAdapter.setOnItemClickListener((adapter, view, position) -> {
                memberAdapter.setCheck(onlineMembers.get(position).getMemberDetailInfo().getPersonid());
                pop_video_chat_all.setChecked(memberAdapter.isCheckAll());
            });
            pop_video_chat_all.setOnClickListener(v -> {
                boolean checked = pop_video_chat_all.isChecked();
                pop_video_chat_all.setChecked(checked);
                memberAdapter.setCheckAll(checked);
            });
        } else {
            memberAdapter.notifyDataSetChanged();
            memberAdapter.notifyCheck();
            pop_video_chat_all.setChecked(memberAdapter.isCheckAll());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void BusEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getAction()) {
            case EventType.CALLBACK_VIDEO_DECODE://后台播放数据 DECODE
                Object[] objs = msg.getObjects();
                int obj = (int) objs[1];
                LogUtil.v(TAG, "BusEvent 收到数据 --> resid = " + obj);
                video_chat_view.setVideoDecode(objs);
                break;
            case EventType.CALLBACK_YUVDISPLAY://后台播放数据 YUV
                Object[] objs1 = msg.getObjects();
                int o3 = (int) objs1[0];
                LogUtil.v(TAG, "BusEvent 收到数据 --> resid = " + o3);
                video_chat_view.setYuv(objs1);
                break;
            case EventType.FACESTATUS_CHANGE_INFORM://界面状态变更通知
                queryAttendPeople();
                break;
            case EventType.MEMBER_CHANGE_INFORM://参会人变更通知
                queryAttendPeople();
                break;
            case EventType.DEV_REGISTER_INFORM://设备寄存器变更通知
                queryAttendPeople();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE://收到停止设备对讲
//                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_EXITCHAT_VALUE) {
//                    stopDeviceIntercomInform(msg);
//                } else if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESPONSEINVITE_VALUE) {
//                    replyDeviceIntercomInform(msg);
//                }
                break;
//            case Constant.BUS_CHAT_STATE://收到视屏聊天的工作状态
//                Object[] objs2 = msg.getObjects();
//                int inviteflag = (int) objs2[0];
//                int operdeviceid = (int) objs2[1];
//                LogUtil.i(TAG, "BusEvent -->" + "收到视屏聊天的工作状态 inviteflag= " + inviteflag + ", operdeviceid= " + operdeviceid);
//                mOperdeviceid = operdeviceid;
//                if ((inviteflag & InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_SIMPLEX_VALUE)
//                        == InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_SIMPLEX_VALUE) {
//                    LogUtil.i(TAG, "BusEvent -->" + "新的状态：寻呼");
//                    createPaging();
//                } else {
//                    LogUtil.i(TAG, "BusEvent -->" + "新的状态：对讲");
//                    createIntercom();
//                }
//                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STOPPLAY_VALUE://停止资源通知
                byte[] o2 = (byte[]) msg.getObjects()[0];
//                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CLOSE_VALUE) {
//                    //停止资源通知
//                    InterfaceStop.pbui_Type_MeetStopResWork stopResWork = InterfaceStop.pbui_Type_MeetStopResWork.parseFrom(o2);
//                    List<Integer> resList = stopResWork.getResList();
//                    for (int resid : resList) {
//                        LogUtil.i(TAG, "BusEvent -->" + "停止资源通知 resid: " + resid);
//                        if ((resid == resource_10 || resid == resource_11) && mOperdeviceid == Values.localDeviceId) {
//                            LogUtil.i(TAG, "BusEvent -->" + "工作状态下，自己是发起端，且自己的播放资源停止了");
//                            if (work_state != 0) {
//                                LogUtil.i(TAG, "BusEvent -->" + "停止设备对讲");
//                                jni.stopDeviceIntercom(mOperdeviceid);
//                            }
//                        }
//                    }
//                } else if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
//                    //停止播放通知
//                    InterfaceStop.pbui_Type_MeetStopPlay stopPlay = InterfaceStop.pbui_Type_MeetStopPlay.parseFrom(o2);
//                    int resid = stopPlay.getRes();
//                    int createdeviceid = stopPlay.getCreatedeviceid();
//                    LogUtil.i(TAG, "BusEvent -->" + "停止播放通知 resid= " + resid + ", createdeviceid= " + createdeviceid);
//                    if ((resid == resource_10 || resid == resource_11) && mOperdeviceid == Values.localDeviceId) {
//                        LogUtil.i(TAG, "BusEvent -->" + "工作状态下，自己是发起端，且自己的播放资源停止了");
//                        if (work_state != 0) {
//                            LogUtil.i(TAG, "BusEvent -->" + "停止设备对讲");
//                            jni.stopDeviceIntercom(mOperdeviceid);
//                        }
//                    }
//                }
                break;
        }
    }

    //收到回复设备对讲的通知
    private void replyDeviceIntercomInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] bytes = (byte[]) msg.getObjects()[0];
        InterfaceDevice.pbui_Type_DeviceChat info = InterfaceDevice.pbui_Type_DeviceChat.parseFrom(bytes);
        int inviteflag = info.getInviteflag();
        int operdeviceid = info.getOperdeviceid();
        LogUtil.i(TAG, "收到回复设备对讲的通知 inviteflag = " + inviteflag + ", operdeviceid= " + operdeviceid);
        if ((inviteflag & InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_DEAL_VALUE) ==
                InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_DEAL_VALUE) {
            if ((inviteflag & InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_SIMPLEX_VALUE) ==
                    InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_SIMPLEX_VALUE) {
                LogUtil.i(TAG, "收到回复设备对讲的通知 -->" + "对方同意寻呼");
                ToastUtil.showToast(getString(R.string.agree_device_paging, getMemberName()));
                if (work_state != 1) {
                    createPaging();
                }
            } else {
                LogUtil.i(TAG, "收到回复设备对讲的通知 -->" + "对方同意对讲");
                ToastUtil.showToast(getString(R.string.agree_device_intercom, getMemberName()));
                createIntercom();
            }
        } else {
            work_state = 0;
            if ((inviteflag & InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_SIMPLEX_VALUE) == InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_SIMPLEX_VALUE) {
                LogUtil.i(TAG, "收到回复设备对讲的通知 -->" + "对方拒绝寻呼");
                ToastUtil.showToast(getString(R.string.reject_device_paging, getMemberName()));
                video_chat_view.createDefaultView(1);
            } else {
                LogUtil.i(TAG, "收到回复设备对讲的通知 -->" + "对方拒绝对讲");
                ToastUtil.showToast(getString(R.string.reject_device_intercom, getMemberName()));
                video_chat_view.createDefaultView(2);
            }
            setEnable();
        }
    }

    //收到停止设备对讲通知
    private void stopDeviceIntercomInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] bytes = (byte[]) msg.getObjects()[0];
        InterfaceDevice.pbui_Type_ExitDeviceChat info = InterfaceDevice.pbui_Type_ExitDeviceChat.parseFrom(bytes);
        int exitdeviceid = info.getExitdeviceid();
        int operdeviceid = info.getOperdeviceid();
        LogUtil.i(TAG, "收到停止设备对讲通知 -->" + " exitdeviceid= " + exitdeviceid + ", operdeviceid= " + operdeviceid);
        if (work_state == 1) {//寻呼中
            if (exitdeviceid == operdeviceid || exitdeviceid == Values.localDevId) {//发起端退出了,自己才退出
                LogUtil.i(TAG, "收到停止设备对讲通知 -->" + "发起端退出了或则自己退出");
                stopAll();
                video_chat_view.createDefaultView(1);
                work_state = 0;
                setEnable();
            }
        } else if (work_state == 2) {//对讲中
            LogUtil.i(TAG, "收到停止设备对讲通知 -->" + "对讲中有人退出");
            stopAll();
            video_chat_view.createDefaultView(2);
            work_state = 0;
            setEnable();
        }
    }

    private void initView() {
        pop_video_chat_all = findViewById(R.id.pop_video_chat_all);
        pop_video_chat_rv = findViewById(R.id.pop_video_chat_rv);
        pop_video_chat_radio = findViewById(R.id.pop_video_chat_radio);
        pop_video_chat_paging = findViewById(R.id.pop_video_chat_paging);
        pop_video_chat_intercom = findViewById(R.id.pop_video_chat_intercom);
        pop_video_chat_close = findViewById(R.id.pop_video_chat_close);
        video_chat_ask_cb = findViewById(R.id.video_chat_ask_cb);
        pop_video_chat_launch = findViewById(R.id.pop_video_chat_launch);
        pop_video_chat_stop = findViewById(R.id.pop_video_chat_stop);
        video_chat_view = findViewById(R.id.video_chat_view);

        pop_video_chat_close.setOnClickListener(this);
        pop_video_chat_launch.setOnClickListener(this);
        pop_video_chat_stop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop_video_chat_launch:
                if (memberAdapter != null && !memberAdapter.getChooseDevid().isEmpty()) {
                    List<Integer> chooseDevids = memberAdapter.getChooseDevid();
                    if (pop_video_chat_paging.isChecked()) {
                        //寻呼模式
                        int flag;
                        if (video_chat_ask_cb.isChecked()) {
                            flag = InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_SIMPLEX_VALUE |//寻呼
                                    InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_VIDEO_VALUE |//视频
                                    InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_AUDIO_VALUE |//音频
                                    InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_ASK_VALUE;//询问
                        } else {
                            flag = InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_SIMPLEX_VALUE |//寻呼
                                    InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_VIDEO_VALUE |//视频
                                    InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_AUDIO_VALUE;//音频
                        }
                        LogUtil.i(TAG, "发起寻呼 -->选中的设备ID= " + chooseDevids.toString());
                        mOperdeviceid = Values.localDevId;
                        jni.deviceIntercom(chooseDevids, flag);
                        createPaging();
                    } else {
                        //对讲模式
                        if (chooseDevids.size() > 1) {
                            ToastUtil.showToast(R.string.can_only_choose_one);
                        } else {
                            LogUtil.i(TAG, "发起对讲 -->选中的设备ID= " + chooseDevids.toString());
                            int flag;
                            if (video_chat_ask_cb.isChecked()) {
                                flag = InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_VIDEO_VALUE |//视频
                                        InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_AUDIO_VALUE |//音频
                                        InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_ASK_VALUE;//询问
                            } else {
                                flag = InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_VIDEO_VALUE |//视频
                                        InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_AUDIO_VALUE;//音频
                            }
                            mOperdeviceid = Values.localDevId;
                            jni.deviceIntercom(chooseDevids, flag);
                            createIntercom();
                        }
                    }
                } else {
                    ToastUtil.showToast(R.string.please_choose_member);
                }
                break;
            case R.id.pop_video_chat_stop:
                if (work_state != 0) {
                    jni.stopDeviceIntercom(mOperdeviceid);
                }
                break;
            case R.id.pop_video_chat_close:
                onBackPressed();
                break;
        }
    }

    private void createIntercom() {
        work_state = 2;
        ids.clear();
        ids.add(10);
        ids.add(11);
        video_chat_view.createPlayView(ids);
        pop_video_chat_intercom.setChecked(true);
        setEnable();
    }

    private void createPaging() {
        work_state = 1;
        ids.clear();
        ids.add(10);
        video_chat_view.createPlayView(ids);
        pop_video_chat_paging.setChecked(true);
        setEnable();
    }

    private void setEnable() {
        boolean enabled = work_state == 0;
        video_chat_ask_cb.setEnabled(enabled);
        pop_video_chat_paging.setEnabled(enabled);
        pop_video_chat_intercom.setEnabled(enabled);
        pop_video_chat_all.setEnabled(enabled);
        setRvLayoutManager(enabled);
        pop_video_chat_launch.setEnabled(enabled);
        if (enabled) {
            pop_video_chat_stop.setBackground(getResources().getDrawable(R.drawable.shape_btn_pressed));
        } else {
            pop_video_chat_launch.setBackground(getResources().getDrawable(R.drawable.shape_btn_enable_flase));
        }
        pop_video_chat_stop.setEnabled(!enabled);
        if (enabled) {
            pop_video_chat_stop.setBackground(getResources().getDrawable(R.drawable.shape_btn_enable_flase));
        } else {
            pop_video_chat_stop.setBackground(getResources().getDrawable(R.drawable.shape_btn_pressed));
        }
    }

    @Override
    protected void onDestroy() {
        video_chat_view.clearAll();
        stopAll();
        isChatingOpened = false;
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        stopAll();
        super.onBackPressed();
    }

    private void stopAll() {
        LogUtil.d(TAG, "stopAll -->" + "对讲或寻呼停止");
//        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_COLLECT_CAMERA_STOP).build());
        List<Integer> resids = new ArrayList<>();
        resids.add(10);
        resids.add(11);
        List<Integer> devids = new ArrayList<>();
        devids.add(Values.localDevId);
        jni.stopResourceOperate(resids, devids);
    }
}
