package com.pa.paperless.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextUtils;

import com.pa.paperless.activity.ChatVideoActivity;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.helper.MaxLengthFilter;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.utils.LogUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceIM;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.MemberListAdapter;
import com.pa.paperless.adapter.MulitpleItemAdapter;
import com.pa.paperless.data.bean.DevMember;
import com.pa.paperless.data.bean.ReceiveMeetIMInfo;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.utils.Dispose;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.pa.paperless.activity.MeetingActivity.chatisshowing;
import static com.pa.paperless.activity.MeetingActivity.mBadge;
import static com.pa.paperless.activity.MeetingActivity.mReceiveMsg;
import static com.pa.paperless.data.constant.EventType.DEV_REGISTER_INFORM;
import static com.pa.paperless.data.constant.EventType.FACESTATUS_CHANGE_INFORM;


/**
 * Created by Administrator on 2017/10/31.
 * 互动交流
 */

public class ChatFragment extends BaseFragment implements View.OnClickListener {

    private MemberListAdapter mMemberAdapter;
    private MulitpleItemAdapter chatAdapter;
    private final String TAG = "ChatFragment-->";
    private TextView mChatCountTv;
    private ListView mRightChatLv;
    private RecyclerView mRightchatOnlineRl;
    private EditText mChatMsgEdt;
    private Button mSendBtn, chat_video_btn;
    private List<DevMember> mChatonLineMember;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos;
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.right_chat, container, false);
        chatisshowing = true;
        initView(inflate);
        fun_queryAttendPeople();
        return inflate;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.meet_chat_info://收到新的聊天信息
                LogUtil.i("RTag", "ChatFragment.getEventMessage :  收到新的聊天信息 --->>> ");
                if (chatisshowing) {
                    InterfaceIM.pbui_Type_MeetIM object = (InterfaceIM.pbui_Type_MeetIM) message.getObject();
                    if (object.getMsgtype() == 0) {//证明是文本类消息
                        List<ReceiveMeetIMInfo> receiveMeetIMInfos = Dispose.ReceiveMeetIMinfo(object);
                        receiveMeetIMInfos.get(0).setType(true);//设置是接收的消息
                        mReceiveMsg.add(receiveMeetIMInfos.get(0));
                        fun_queryAttendPeopleFromId(receiveMeetIMInfos.get(0).getMemberid());
                    }
                }
                break;
            case EventType.MEMBER_CHANGE_INFORM://参会人员变更通知
                InterfaceBase.pbui_MeetNotifyMsg object = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
                int opermethod = object.getOpermethod();
                int id = object.getId();
                LogUtil.e(TAG, "ChatFragment.getEventMessage :   --> opermethod= " + opermethod + ", id= " + id);
                fun_queryAttendPeople();
                break;
            case DEV_REGISTER_INFORM:
                fun_queryAttendPeople();
                break;
            case FACESTATUS_CHANGE_INFORM:
                fun_queryAttendPeople();
                break;
        }
    }

    private void fun_queryAttendPeople() {
        try {
            //92.查询参会人员
            InterfaceMember.pbui_Type_MemberDetailInfo o = jni.queryAttendPeople();
            if (o == null) return;
            if (memberInfos == null) memberInfos = new ArrayList<>();
            else memberInfos.clear();
            List<InterfaceMember.pbui_Item_MemberDetailInfo> itemList = o.getItemList();
            memberInfos.addAll(itemList);
            LogUtil.i(TAG, "ChatFragment.getEventMessage :  得到参会人信息 --->>> ");
            fun_queryDeviceInfo();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryDeviceInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo o1 = jni.queryDeviceInfo();
            if (o1 == null) return;
            LogUtil.i(TAG, "ChatFragment.getEventMessage :  得到设备信息 --->>> ");
            if (mChatonLineMember == null) mChatonLineMember = new ArrayList<>();
            else mChatonLineMember.clear();
            if (deviceInfos == null) deviceInfos = new ArrayList<>();
            else deviceInfos.clear();
            deviceInfos.addAll(o1.getPdevList());
            for (int i = 0; i < deviceInfos.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo deviceInfo = deviceInfos.get(i);
                int netState = deviceInfo.getNetstate();
                int faceState = deviceInfo.getFacestate();
                int devId = deviceInfo.getDevcieid();
                int memberId = deviceInfo.getMemberid();
                //判断是否是在线的并且界面状态为1的参会人
                if (faceState == 1 && netState == 1) {
                    for (int j = 0; j < memberInfos.size(); j++) {
                        InterfaceMember.pbui_Item_MemberDetailInfo memberInfo = memberInfos.get(j);
                        int personid = memberInfo.getPersonid();
                        if (personid == memberId) {
                            /** **** **  过滤掉自己的设备  ** **** **/
                            if (devId != Values.localDevId) {
                                //查找到在线状态的参会人员
//                                LogUtil.i(TAG, "fun_queryDeviceInfo 添加在线状态的参会人："
//                                        + "\n参会人名称：" + memberInfo.getName().toStringUtf8()
//                                        + "\n人员ID：" + memberInfo.getPersonid()
//                                        + "\n设备名称：" + deviceInfo.getDevname().toStringUtf8()
//                                        + "\n设备ID：" + deviceInfo.getDevcieid()
//                                );
                                mChatonLineMember.add(new DevMember(memberInfo, devId));
                            }
                        }
                    }
                }
            }
            if (mMemberAdapter == null) {
                mMemberAdapter = new MemberListAdapter(getActivity(), mChatonLineMember);
                mRightChatLv.setAdapter(mMemberAdapter);
            } else {
                LogUtil.d(TAG, "更新在线参会人列表 mChatonLineMember= " + mChatonLineMember.size());
                mMemberAdapter.notifyDataSetChanged();
                mMemberAdapter.notifyChecks();
                mChatCountTv.setSelected(mMemberAdapter.isAllCheck());
            }
            mChatCountTv.setText(mMemberAdapter.getCheckedId().size() + " / " + mChatonLineMember.size());
            mRightChatLv.setOnItemClickListener((parent, view, position, id) -> {
                mMemberAdapter.setCheck(mChatonLineMember.get(position).getMemberInfos().getPersonid());
                //每次点击item的时候检查是否为全选状态
                boolean allCheck = mMemberAdapter.isAllCheck();
                mChatCountTv.setSelected(allCheck);
                /** **** **  更新控件  ** **** **/
                mChatCountTv.setText(mMemberAdapter.getCheckedId().size() + " / " + mChatonLineMember.size());
            });
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryAttendPeopleFromId(int memberid) {
        try {
            LogUtil.e(TAG, "ChatFragment.fun_queryAttendPeopleFromId :  收到指定ID的参会人 --> memberid= " + memberid);
            //查询指定ID的参会人  获取名称
            InterfaceMember.pbui_Type_MemberDetailInfo member = jni.queryAttendPeopleFromId(memberid);
            if (member == null) return;
            String name = MyUtils.b2s(member.getItemList().get(0).getName());
            // 给之前添加进去的最后一个设置参会人名字
            mReceiveMsg.get(mReceiveMsg.size() - 1).setMemberName(name);
            if (chatAdapter == null) {
                chatAdapter = new MulitpleItemAdapter(getContext(), mReceiveMsg);
                //设置RecyclerView第一条数据从底部开始显示
//                layoutManager.setStackFromEnd(true);
                mRightchatOnlineRl.setAdapter(chatAdapter);
            } else {
                chatAdapter.notifyDataSetChanged();
            }
            if (!mReceiveMsg.isEmpty()) {
                mRightchatOnlineRl.smoothScrollToPosition(mReceiveMsg.size() - 1);
            }
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

    private void initView(View inflate) {
        mChatCountTv = inflate.findViewById(R.id.chat_count_tv);
        mChatCountTv.setText("0 / 0");//默认
        mRightChatLv = inflate.findViewById(R.id.right_chat_lv);
        mRightchatOnlineRl = inflate.findViewById(R.id.rightchat_online_rl);
        mBadge.setBadgeNumber(0);
        if (chatAdapter == null) {
            chatAdapter = new MulitpleItemAdapter(getContext(), mReceiveMsg);
            mRightchatOnlineRl.setLayoutManager(new LinearLayoutManager(getContext()));
            mRightchatOnlineRl.setAdapter(chatAdapter);
        }
        if (!mReceiveMsg.isEmpty()) {
            mRightchatOnlineRl.smoothScrollToPosition(mReceiveMsg.size() - 1);
        }
        mChatMsgEdt = inflate.findViewById(R.id.chat_msg_edt);
        mChatMsgEdt.setFilters(new InputFilter[]{new MaxLengthFilter(Macro.content_max_length)});
        mSendBtn = inflate.findViewById(R.id.send_btn);
        chat_video_btn = inflate.findViewById(R.id.chat_video_btn);
        mChatCountTv.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        chat_video_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_video_btn://视频聊天
                startActivity(new Intent(getContext(), ChatVideoActivity.class));
                break;
            case R.id.chat_count_tv://全选
                if (mMemberAdapter == null) break;
                boolean b1 = mMemberAdapter.setAllChecked();
                mChatCountTv.setSelected(b1);
                /** **** **  更新控件  ** **** **/
                mChatCountTv.setText(mMemberAdapter.getCheckedId().size() + " / " + mChatonLineMember.size());
                break;
            case R.id.send_btn://发送
                if (Values.isOnline == 0) {
                    ToastUtil.showToast(R.string.error_network);
                    break;
                }
                if (mMemberAdapter == null) break;
                // 获取选中的参会人 ID
                List<Integer> ids = mMemberAdapter.getCheckedId();
                if (ids.isEmpty()) {
                    ToastUtil.showToast(R.string.tip_select_member);
                    break;
                }
                String string = mChatMsgEdt.getText().toString();
                if (!TextUtils.isEmpty(string)) {
                    //185.发送会议交流信息
                    if (string.length() <= 300) {
                        jni.sendMeetChatInfo(string, InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Message.getNumber(), ids);
                        //RecyclerView 更新聊天的界面
                        List<String> checkedName = mMemberAdapter.getCheckedName();
                        ReceiveMeetIMInfo sendInfo = new ReceiveMeetIMInfo();
                        sendInfo.setNames(checkedName);
                        sendInfo.setMsg(string);
                        sendInfo.setUtcsecond(System.currentTimeMillis());
                        // false发送的消息  true接收的消息
                        sendInfo.setType(false);
                        mReceiveMsg.add(sendInfo);
                        chatAdapter.notifyDataSetChanged();
                        if (!mReceiveMsg.isEmpty()) {
                            mRightchatOnlineRl.scrollToPosition(mReceiveMsg.size() - 1);
                        }
                        //清除输入框内容
                        mChatMsgEdt.setText("".trim());
                    } else {
                        ToastUtil.showToast(R.string.tip_too_many_words);
                    }
                } else {
                    ToastUtil.showToast(R.string.tip_please_enter_message);
                }
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtil.e(TAG, "ChatFragment.onHiddenChanged : hidden:" + hidden + "  chatisshowing  --> " + chatisshowing);
        chatisshowing = !hidden;
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
            if (!mReceiveMsg.isEmpty()) {
                mRightchatOnlineRl.scrollToPosition(mReceiveMsg.size() - 1);
            }
        }
        if (!hidden) {
            mBadge.setBadgeNumber(0);
            fun_queryAttendPeople();
        }
    }
}
