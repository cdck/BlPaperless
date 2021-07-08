package com.pa.paperless.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.ConvertUtil;
import com.pa.paperless.utils.LogUtil;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.intrusoft.scatter.ChartData;
import com.intrusoft.scatter.PieChart;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceSignin;
import com.mogujie.tt.protobuf.InterfaceVote;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.ChooseJoinVoteAdapter;
import com.pa.paperless.adapter.rvadapter.SurveyPopAdapter;
import com.pa.paperless.adapter.rvadapter.VoteAdapter;
import com.pa.paperless.adapter.rvadapter.VoteOptionResultAdapter;
import com.pa.paperless.data.bean.VoteResultSubmitMember;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.Export;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.ScreenUtils;
import com.pa.paperless.utils.ToastUtil;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.pa.paperless.data.constant.Values.roomId;


/**
 * Created by xlk on 2018/11/3.
 * 选举管理
 */
public class ElectionManageFragment extends BaseFragment implements View.OnClickListener {

    private final String TAG = "ElectionManageFragment-->";
    private RecyclerView voteRl;
    private TextView yingdaoTv;
    private TextView yiqiandaoTv;
    private TextView weiqiandaoTv;
    private Button voteEntry;
    private TextView countdownTv;
    private Spinner countdownSpinner;
    private Button startBtn;
    private Button stopBtn;
    private ArrayAdapter spAdapter, popspAdapter, spSuevetTypeAdapter;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos;
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> mVoteData;
    private VoteAdapter mVoteAdapter;
    private int mPosion;
    private SurveyPopAdapter popAdapter;
    private PopupWindow pop;
    private int popPosion;
    private int JUST_OPEN_SURVEY_EXCEL_CODE = 1024;
    public static boolean isSurveyManage = true;
    private int selectedItem;
    private List<VoteResultSubmitMember> submitMemberData;
    private VoteOptionResultAdapter optionAdapter;
    private boolean open_vote_details;
    private boolean open_vote_chart;
    private int countPre;
    private PopupWindow chartPop;
    private List<ChartData> chartDatas;
    private List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> chooseData;
    private ChooseJoinVoteAdapter chooseAdapter;
    private PopupWindow choosePop;
    private boolean showJoinPop;
    private boolean clickBeganVote;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.right_vote, container, false);
        initView(inflate);
        setBtnText();
        fun_queryAttendPeople();
        return inflate;
    }

    private void setBtnText() {
        voteEntry.setVisibility(isSurveyManage ? View.VISIBLE : View.INVISIBLE);
        countdownTv.setVisibility(isSurveyManage ? View.VISIBLE : View.INVISIBLE);
        countdownSpinner.setVisibility(isSurveyManage ? View.VISIBLE : View.INVISIBLE);
        if (spAdapter == null) {
            String[] stringArray = getResources().getStringArray(R.array.countdown_spinner);
            spAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, stringArray);
        }
        if (popspAdapter == null) {
            String[] stringArray = getResources().getStringArray(R.array.whether);
            popspAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, stringArray);
        }
        if (spSuevetTypeAdapter == null) {
            String[] stringArray = getResources().getStringArray(R.array.survey_type);
            spSuevetTypeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, stringArray);
        }
        countdownSpinner.setAdapter(spAdapter);
        countdownSpinner.setSelection(4, true);
        voteEntry.setText(getString(R.string.survey_entry));
        startBtn.setText(isSurveyManage ? getString(R.string.start_survey) : getString(R.string.view_details));
        stopBtn.setText(isSurveyManage ? getString(R.string.stop_survey) : getString(R.string.view_chart));
    }

    private void fun_queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo o = jni.queryAttendPeople();
            if (o == null) return;
            if (memberInfos == null) memberInfos = new ArrayList<>();
            else memberInfos.clear();
            memberInfos.addAll(o.getItemList());
            fun_querySign();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_querySign() {
        if (memberInfos == null || memberInfos.isEmpty()) return;
        try {
            InterfaceSignin.pbui_Type_MeetSignInDetailInfo object1 = jni.querySign();
            if (object1 == null) return;
            List<InterfaceSignin.pbui_Item_MeetSignInDetailInfo> itemList = object1.getItemList();
            LogUtil.e(TAG, "VoteFragment.fun_querySign :  itemList --> " + itemList.size());
            int signin = 0;
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceSignin.pbui_Item_MeetSignInDetailInfo info = itemList.get(i);
                int nameId = info.getNameId();
                long utcseconds = info.getUtcseconds();
                if (utcseconds != 0) {
                    for (int j = 0; j < memberInfos.size(); j++) {
                        InterfaceMember.pbui_Item_MemberDetailInfo memberInfo = memberInfos.get(j);
                        if (memberInfo.getPersonid() == nameId) {
                            signin++;
                        }
                    }
                }
            }
            yingdaoTv.setText(getString(R.string.should_be_to_count, memberInfos.size() + ""));
            weiqiandaoTv.setText(getString(R.string.no_sign_in_count, (memberInfos.size() - signin) + ""));
            yiqiandaoTv.setText(getString(R.string.already_signed_in_count, signin + ""));
            queryVote();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryVote() {
        try {
            InterfaceVote.pbui_Type_MeetVoteDetailInfo object = jni.queryVote();
            if (object == null) {
                if (mVoteData != null && mVoteAdapter != null) {
                    mVoteData.clear();
                    mVoteAdapter.notifyDataSetChanged();
                }
                return;
            }
            LogUtil.d(TAG, "fun_queryVote: 收到投票信息..");
            if (mVoteData == null) mVoteData = new ArrayList<>();
            else mVoteData.clear();
            List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> itemList = object.getItemList();
            for (InterfaceVote.pbui_Item_MeetVoteDetailInfo detailInfo : itemList) {
                if (detailInfo.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election.getNumber()) {
                    mVoteData.add(detailInfo);
                }
            }
            if (mVoteAdapter == null) {
                mVoteAdapter = new VoteAdapter(getContext(), mVoteData);
                mVoteAdapter.setHasStableIds(true);
                voteRl.setLayoutManager(new LinearLayoutManager(getContext()));
                voteRl.setAdapter(mVoteAdapter);
            } else {
                mVoteAdapter.notifyDataSetChanged();
            }
            mVoteAdapter.setItemListener((view, posion) -> {
                mVoteAdapter.setCheckedId(posion);
                mPosion = posion;
            });
            //PopupWindow中的适配器
            if (popAdapter == null) {
                popAdapter = new SurveyPopAdapter(getContext(), mVoteData);
            } else popAdapter.notifyDataSetChanged();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryVote() {
        try {
            InterfaceVote.pbui_Type_MeetVoteDetailInfo object = jni.queryVoteByType(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election.getNumber());
            if (object == null) {
                if (mVoteData != null && mVoteAdapter != null) {
                    mVoteData.clear();
                    mVoteAdapter.notifyDataSetChanged();
                }
                return;
            }
            LogUtil.d(TAG, "fun_queryVote: 收到投票信息..");
            if (mVoteData == null) mVoteData = new ArrayList<>();
            else mVoteData.clear();
            List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> itemList = object.getItemList();
            mVoteData.addAll(itemList);
            if (mVoteAdapter == null) {
                mVoteAdapter = new VoteAdapter(getContext(), mVoteData);
                mVoteAdapter.setHasStableIds(true);
                voteRl.setLayoutManager(new LinearLayoutManager(getContext()));
                voteRl.setAdapter(mVoteAdapter);
            } else {
                mVoteAdapter.notifyDataSetChanged();
            }
            mVoteAdapter.setItemListener((view, posion) -> {
                mVoteAdapter.setCheckedId(posion);
                mPosion = posion;
            });
            //PopupWindow中的适配器
            if (popAdapter == null) {
                popAdapter = new SurveyPopAdapter(getContext(), mVoteData);
            } else popAdapter.notifyDataSetChanged();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.SIGN_CHANGE_INFORM://签到变更通知
                fun_querySign();
                break;
            case EventType.MEMBER_CHANGE_INFORM:
                fun_queryAttendPeople();
                if (mVoteData.size() > mPosion) {
                    fun_queryAttendPeopleDetailed(mVoteData.get(mPosion));
                }
                break;
            case EventType.Vote_Change_Inform://投票变更通知
                queryVote();
                break;
            case EventType.MeetSeat_Change_Inform://会议排位变更
                if (mVoteData.size() > mPosion) {
                    fun_queryAttendPeopleDetailed(mVoteData.get(mPosion));
                }
                break;
            case EventType.DEV_REGISTER_INFORM://设备寄存器变更
                if (mVoteData.size() > mPosion) {
                    fun_queryAttendPeopleDetailed(mVoteData.get(mPosion));
                }
                break;
            case EventType.MEMBER_PERMISSION_INFORM://参会人权限变更
                if (mVoteData.size() > mPosion) {
                    fun_queryAttendPeopleDetailed(mVoteData.get(mPosion));
                }
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
        voteRl = inflate.findViewById(R.id.vote_rl);
        yingdaoTv = inflate.findViewById(R.id.yingdao_tv);
        yiqiandaoTv = inflate.findViewById(R.id.yiqiandao_tv);
        weiqiandaoTv = inflate.findViewById(R.id.weiqiandao_tv);
        voteEntry = inflate.findViewById(R.id.vote_entry);
        countdownTv = inflate.findViewById(R.id.countdown_tv);
        countdownSpinner = inflate.findViewById(R.id.countdown_spinner);
        startBtn = inflate.findViewById(R.id.start_btn);
        stopBtn = inflate.findViewById(R.id.stop_btn);

        voteEntry.setOnClickListener(this);
        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vote_entry:
                if (popAdapter != null) showVoteInfoPop();
                break;
            case R.id.start_btn:
                startEvent();
                break;
            case R.id.stop_btn:
                stopEvent();
                break;
        }
    }

    private void startEvent() {
        if (isSurveyManage) {//开始投票
            clickBeganVote = true;
            boolean isback = false;
            InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = mVoteData.get(mPosion);
            int votestate = voteInfo.getVotestate();
            if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote.getNumber()) {
                for (int i = 0; i < mVoteData.size(); i++) {//查看当前是否已经有选举已经发起
                    if (mVoteData.get(i).getVotestate() == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing.getNumber()) {
                        ToastUtils.showShort(R.string.has_vote_ongoing);
                        isback = true;
                        break;
                    }
                }
            }
            if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_endvote.getNumber()) {
                ToastUtils.showShort(R.string.the_vote_is_over);
                isback = true;
            }
            if (isback) return;
            fun_queryAttendPeopleDetailed(voteInfo);
        } else {//投票详情
            if (mVoteData != null && mVoteData.size() > mPosion) {
                InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = mVoteData.get(mPosion);
                List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = voteInfo.getItemList();
                boolean havedata = false;//该投票是否有人提交过数据
                for (int i = 0; i < optionInfo.size(); i++) {
                    if (optionInfo.get(i).getSelcnt() != 0) {
                        havedata = true;
                        break;//终止循环
                    }
                }
                if (voteInfo.getMode() != 1) {
                    ToastUtils.showShort(R.string.please_choose_registered_vote);
                } else if (voteInfo.getVotestate() == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote.getNumber()) {
                    ToastUtils.showShort(R.string.not_choose_notvote);
                } else if (!havedata) {
                    ToastUtils.showShort(R.string.no_data_can_show);
                } else {
                    open_vote_details = true;
                    fun_queryOneVoteSubmitter(voteInfo);
                }
            }
        }
    }

    private void stopEvent() {
        if (mVoteData != null && mVoteData.size() > mPosion) {
            if (isSurveyManage) {//结束投票
                InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo1 = mVoteData.get(mPosion);
                if (voteInfo1.getVotestate() == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing.getNumber()) {
                    jni.stopVote(voteInfo1.getVoteid());
                } else
                    ToastUtils.showShort(R.string.please_choose_ongoing_vote);
            } else {//查看图表
                InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = mVoteData.get(mPosion);
                List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = voteInfo.getItemList();
                boolean havedata = false;//该投票是否有人提交过数据
                for (int i = 0; i < optionInfo.size(); i++) {
                    if (optionInfo.get(i).getSelcnt() != 0) {
                        havedata = true;
                        break;//终止循环
                    }
                }
                if (!havedata) {
                    ToastUtils.showShort(R.string.no_data_can_show);
                } else if (voteInfo.getVotestate() == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote.getNumber()) {
                    ToastUtils.showShort(R.string.not_choose_notvote);
                } else {
                    open_vote_chart = true;
                    fun_queryOneVoteSubmitter(voteInfo);
                }
            }
        }
    }

    private void fun_queryAttendPeopleDetailed(InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo) {
        try {
            LogUtil.e(TAG, "fun_queryAttendPeopleDetailed :   --> " + voteInfo.getVoteid());
            InterfaceMember.pbui_Type_MeetMemberDetailInfo detailInfo = jni.queryAttendPeopleDetailed();
            if (detailInfo == null) return;
            if (chooseData == null) chooseData = new ArrayList<>();
            else chooseData.clear();

            InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo info = jni.placeDeviceRankingInfo(roomId);
            if (info == null) return;
            List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> itemList1 = info.getItemList();
            ArrayList<Integer> ids = new ArrayList<>();
            for (int i = 0; i < itemList1.size(); i++) {
                InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item = itemList1.get(i);
                int memberid = item.getMemberid();
                int role = item.getRole();
                if (role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE) {
                    LogUtil.i(TAG, "fun_queryAttendPeopleDetailed 过滤掉秘书：" + item.getMembername().toStringUtf8());
                    ids.add(memberid);
                }
            }

            List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> itemList = detailInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceMember.pbui_Item_MeetMemberDetailInfo item = itemList.get(i);
                if (!ids.contains(item.getMemberid())) {
                    chooseData.add(item);
                }
            }
            if (chooseAdapter == null) {
                chooseAdapter = new ChooseJoinVoteAdapter(getContext(), chooseData);
            } else {
                chooseAdapter.notifyDataSetChanged();
                chooseAdapter.notifyChecks();
            }
            if (clickBeganVote) {
                clickBeganVote = false;
                if (!showJoinPop) showJoinPop(voteInfo);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void showJoinPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo) {
        showJoinPop = true;
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.choose_join_vote_pop, null);
        View meetFl = getActivity().findViewById(R.id.meet_fl);
        choosePop = new PopupWindow(inflate, meetFl.getWidth() + 10, meetFl.getHeight() + 48);
        choosePop.setAnimationStyle(R.style.Anim_PopupWindow);
        choosePop.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        choosePop.setTouchable(true);
        choosePop.setFocusable(true);
        ChoosePopViewHolder holder = new ChoosePopViewHolder(inflate);
        choosePopEvent(holder, voteInfo);
        choosePop.setOnDismissListener(() -> showJoinPop = false);
        choosePop.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        //使内部的EditText获取焦点输入的时候，软键盘不会遮挡住
        //SOFT_INPUT_ADJUST_PAN:把整个Layout顶上去露出获得焦点的EditText,不压缩多余空间
        choosePop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        choosePop.showAtLocation(meetFl, Gravity.LEFT | Gravity.BOTTOM, 0, 0);
    }


    private void choosePopEvent(ChoosePopViewHolder holder, InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo) {
        holder.choose_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        holder.choose_rv.setAdapter(chooseAdapter);
        chooseAdapter.setListener((view, posion) -> {
            InterfaceMember.pbui_Item_MeetMemberDetailInfo info = chooseData.get(posion);
            if (chooseAdapter.isCanJoin(info)) {
                chooseAdapter.setChecks(info.getMemberid());
                holder.all_number_cb.setChecked(chooseAdapter.isCheckAll());
            } else
                ToastUtil.showShort(R.string.must_choose_online);
        });
        holder.all_number_cb.setOnClickListener(v -> {
            boolean checked = holder.all_number_cb.isChecked();
            holder.all_number_cb.setChecked(checked);
            chooseAdapter.setCheckAll(checked);
        });
        holder.ensure.setOnClickListener(v -> {
            List<Integer> checks = chooseAdapter.getChecks();
            if (checks.isEmpty()) {
                ToastUtil.showShort(R.string.please_choose);
                return;
            }
            int votestate = voteInfo.getVotestate();
            if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote.getNumber()) {
                jni.initiateVote(voteInfo.getVoteid(), InterfaceMacro.Pb_VoteStartFlag.Pb_MEET_VOTING_FLAG_AUTOEXIT.getNumber(),
                        getTimeout(), checks);
            } else if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing.getNumber()) {
                showDialog(voteInfo, getTimeout());
            }
            choosePop.dismiss();
        });
        holder.cancel.setOnClickListener(v -> choosePop.dismiss());
    }

    private int getTimeout() {
        int index = countdownSpinner.getSelectedItemPosition();
        int timeout = 0;
        if (index == 0) timeout = 10;
        else if (index == 1) timeout = 30;
        else if (index == 2) timeout = 60;
        else if (index == 3) timeout = 120;
        else if (index == 4) timeout = 300;
        else if (index == 5) timeout = 900;
        else if (index == 6) timeout = 1800;
        else if (index == 7) timeout = Integer.MAX_VALUE;
        return timeout;
    }

    private void fun_queryOneVoteSubmitter(InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo) {
        try {
            InterfaceVote.pbui_Type_MeetVoteSignInDetailInfo object3 = jni.queryOneVoteSubmitter(voteInfo.getVoteid());
            if (object3 == null) return;
            LogUtil.d(TAG, "getEventMessage: 收到指定投票提交人数据,更新投票结果adapter");
            List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = voteInfo.getItemList();
            List<Integer> ids = new ArrayList<>();
            List<InterfaceVote.pbui_Item_MeetVoteSignInDetailInfo> itemList1 = object3.getItemList();
            LogUtil.e(TAG, "FabService.receiveVoteInfo :  所有投票人员数量 --> " + itemList1.size());
            if (submitMemberData == null) submitMemberData = new ArrayList<>();
            else submitMemberData.clear();
            for (int i = 0; i < itemList1.size(); i++) {
                InterfaceVote.pbui_Item_MeetVoteSignInDetailInfo pbui_item_meetVoteSignInDetailInfo = itemList1.get(i);
                String chooseText = "";
                String name = "";
                int shidao = 0;
                int selcnt1 = pbui_item_meetVoteSignInDetailInfo.getSelcnt();
                int i1 = selcnt1 & Macro.PB_VOTE_SELFLAG_CHECKIN;
                LogUtil.e(TAG, "FabService.receiveVoteInfo :  selcnt1 --> " + selcnt1 + "， 相与的结果= " + i1);
                if (i1 == Macro.PB_VOTE_SELFLAG_CHECKIN) {
                    shidao++;
                }
                LogUtil.e(TAG, "FabService.receiveVoteInfo :  实到人数 --> " + shidao);
                int id1 = pbui_item_meetVoteSignInDetailInfo.getId();
                ids.add(id1);
                for (int k = 0; k < memberInfos.size(); k++) {
                    if (memberInfos.get(k).getPersonid() == id1) {
                        name = memberInfos.get(k).getName().toStringUtf8();
                        break;//跳出循环,只会跳出当前for循环
                    }
                }
                int selcnt = pbui_item_meetVoteSignInDetailInfo.getSelcnt();
                //int变量的二进制表示的字符串
                String string = Integer.toBinaryString(selcnt);
                //查找字符串中为1的索引位置
                int length = string.length();
                for (int j = 0; j < length; j++) {
                    char c = string.charAt(j);
                    //将 char 装换成int型整数
                    int a = c - '0';
                    if (a == 1) {
                        selectedItem = length - j - 1;//索引从0开始
                        LogUtil.e(TAG, "FabService.getEventMessage :  选中了第 " + selectedItem + " 项");
                        for (int k = 0; k < optionInfo.size(); k++) {
                            if (k == selectedItem) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(k);
                                String text = voteOptionsInfo.getText().toStringUtf8();
                                if (chooseText.length() == 0) chooseText = text;
                                else chooseText += " | " + text;
                            }
                        }
                    }
                }
                submitMemberData.add(new VoteResultSubmitMember(id1, name, chooseText));
            }
            if (optionAdapter == null)
                optionAdapter = new VoteOptionResultAdapter(getContext(), submitMemberData);
            else optionAdapter.notifyDataSetChanged();
            if (open_vote_details) showVoteDetailsPop();
            else if (open_vote_chart) showVoteChartPop(voteInfo);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void showVoteChartPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo) {
        open_vote_chart = false;
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.vote_chart_pop, null);
        View meetFl = getActivity().findViewById(R.id.meet_fl);
        int px_5 = ScreenUtils.dip2px(getContext(), 5);
        int px_20 = ScreenUtils.dip2px(getContext(), 20);
        LogUtil.d(TAG, "dp转px 5=" + px_5 + ",20=" + px_20);
        chartPop = new PopupWindow(inflate, meetFl.getWidth() + px_5, meetFl.getHeight() + px_20);
        chartPop.setAnimationStyle(R.style.Anim_PopupWindow);
        chartPop.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        chartPop.setTouchable(true);
        chartPop.setFocusable(true);
        SurveyViewHolder holder = new SurveyViewHolder(inflate);
        SurveyViewHolderEvent(holder, voteInfo);
        chartPop.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        //使内部的EditText获取焦点输入的时候，软键盘不会遮挡住
        //SOFT_INPUT_ADJUST_PAN:把整个Layout顶上去露出获得焦点的EditText,不压缩多余空间
        chartPop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        chartPop.showAtLocation(getActivity().findViewById(R.id.meet_fl), Gravity.LEFT | Gravity.BOTTOM, 0, 0);
    }

    private void SurveyViewHolderEvent(SurveyViewHolder holder, InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo) {
        holder.imag_btn.setOnClickListener(v -> chartPop.dismiss());
        /** **** **  先隐藏所有的选项  ** **** **/
        holder.vote_option_a.setVisibility(View.GONE);
        holder.vote_option_b.setVisibility(View.GONE);
        holder.vote_option_c.setVisibility(View.GONE);
        holder.vote_option_d.setVisibility(View.GONE);
        holder.vote_option_e.setVisibility(View.GONE);
        //饼状图形 需要先隐藏
        holder.pie_chart.setVisibility(View.GONE);
        countPre = 0;//一共占用的百分比数
        int voteid = voteInfo.getVoteid();
        int mode = voteInfo.getMode();
        int type = voteInfo.getType();
        String modeStr = mode == 0 ? getString(R.string.anonymous) : getString(R.string.remember);
        String typestr = getTypeStr(type);
        String votestatestr = getVoteStateStr(voteInfo.getVotestate());
        InterfaceBase.pbui_CommonInt32uProperty yingDaoInfo = jni.queryVoteSubmitterProperty(voteid, 0, InterfaceMacro.Pb_MeetVotePropertyID.Pb_MEETVOTE_PROPERTY_ATTENDNUM.getNumber());
        InterfaceBase.pbui_CommonInt32uProperty yiTouInfo = jni.queryVoteSubmitterProperty(voteid, 0, InterfaceMacro.Pb_MeetVotePropertyID.Pb_MEETVOTE_PROPERTY_VOTEDNUM.getNumber());
        InterfaceBase.pbui_CommonInt32uProperty shiDaoInfo = jni.queryVoteSubmitterProperty(voteid, 0, InterfaceMacro.Pb_MeetVotePropertyID.Pb_MEETVOTE_PROPERTY_CHECKINNUM.getNumber());
        int yingDao = yingDaoInfo == null ? 0 : yingDaoInfo.getPropertyval();
        int yiTou = yiTouInfo == null ? 0 : yiTouInfo.getPropertyval();
        int shiDao = shiDaoInfo == null ? 0 : shiDaoInfo.getPropertyval();
        String yingDaoStr = yingDao + "";
        String yiTouStr = yiTou + "";
        String shiDaoStr = shiDao + "";
        String weiTouStr = (yingDao - yiTou) + "";
        LogUtil.e(TAG, "FabService.holder_event :  应到人数 --> " + yingDao + ", 已投人数= " + yiTou);
        holder.member_count_tv.setText(getString(R.string.vote_result_count, yingDaoStr, shiDaoStr, yiTouStr, weiTouStr));
        holder.vote_type_tv.setText("( " + typestr + "  " + modeStr + "  " + votestatestr + " )");
        //设置标题
        holder.title_tv.setText(voteInfo.getContent().toStringUtf8());
        /* **** **  设置图形数据  ** **** */
        if (chartDatas == null) chartDatas = new ArrayList<>();
        else chartDatas.clear();
        int maintype = voteInfo.getMaintype();

        holder.vote_type_top_ll.setVisibility(View.VISIBLE);
        holder.vote_member_count_tv.setVisibility(View.GONE);

        List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = voteInfo.getItemList();
        int count = getCount(optionInfo);
        for (int i = 0; i < optionInfo.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo info = optionInfo.get(i);
            String text = info.getText().toStringUtf8();
            int selcnt = info.getSelcnt();
            if (!TextUtils.isEmpty(text)) {
                if (i == 0) {
                    holder.vote_option_a.setVisibility(View.VISIBLE);
                    holder.option_a.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, getContext().getColor(R.color.black), getContext().getColor(R.color.chart_color_red));
                } else if (i == 1) {
                    holder.vote_option_b.setVisibility(View.VISIBLE);
                    holder.option_b.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, getContext().getColor(R.color.black), getContext().getColor(R.color.chart_color_green));
                } else if (i == 2) {
                    holder.vote_option_c.setVisibility(View.VISIBLE);
                    holder.option_c.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, getContext().getColor(R.color.black), getContext().getColor(R.color.chart_color_blue));
                } else if (i == 3) {
                    holder.vote_option_d.setVisibility(View.VISIBLE);
                    holder.option_d.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, getContext().getColor(R.color.black), getContext().getColor(R.color.chart_color_aqua));
                } else if (i == 4) {
                    holder.vote_option_e.setVisibility(View.VISIBLE);
                    holder.option_e.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, getContext().getColor(R.color.black), getContext().getColor(R.color.chart_color_pink));
                }
            }
        }
        if (countPre > 0 && countPre < 100) {//因为没有除尽,有余下的空白区域
            ChartData lastChartData = chartDatas.get(chartDatas.size() - 1);//先获取到最后一条的数据
            chartDatas.remove(chartDatas.size() - 1);//删除掉集合中的最后一个
            //使用原数据重新添加,但是修改所占比例大小,这样就能确保不会出现空白部分
            chartDatas.add(new ChartData(lastChartData.getDisplayText(),
                    lastChartData.getPartInPercent() + (100 - countPre),
                    lastChartData.getTextColor(),
                    lastChartData.getBackgroundColor()));
        }
        //如果没有数据会报错
        if (chartDatas.isEmpty()) {
            chartDatas.add(new ChartData(getResources().getString(R.string.null_str), 100, Color.parseColor("#FFFFFF"), Color.parseColor("#676767")));
        }
        holder.pie_chart.setChartData(chartDatas);
        holder.pie_chart.setVisibility(View.VISIBLE);
    }

    private int setChartData(float count, int selcnt, int colora, int colorb) {
        if (selcnt > 0) {
            float element = (float) selcnt / count;
            LogUtil.d(TAG, "FabService.setUplistener :  element --> " + element);
            int v = (int) (element * 100);
            String str = v + "%";
            countPre += v;
            chartDatas.add(new ChartData(str, v, colora, colorb));
        }
        return countPre;
    }

    private int getCount(List<InterfaceVote.pbui_SubItem_VoteItemInfo> itemList) {
        int count = 0;
        for (int i = 0; i < itemList.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo info = itemList.get(i);
            count += info.getSelcnt();
        }
        LogUtil.e(TAG, "getCount :  当前投票票数总数 --> " + count);
        return count;
    }

    private String getVoteStateStr(int votestate) {
        if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote.getNumber()) {
            return getString(R.string.pb_vote_notvote);
        } else if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing.getNumber()) {
            return getString(R.string.pb_vote_voteing);
        } else if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_endvote.getNumber()) {
            return getString(R.string.pb_vote_endvote);
        }
        return "";
    }

    private String getTypeStr(int type) {
        if (type == 0) {
            return getString(R.string.pb_vote_type_many);
        } else if (type == 1) {
            return getString(R.string.pb_vote_type_single);
        } else if (type == 2) {
            return getString(R.string.pb_vote_type_4_5);
        } else if (type == 3) {
            return getString(R.string.pb_vote_type_3_5);
        } else if (type == 4) {
            return getString(R.string.pb_vote_type_2_5);
        } else if (type == 5) {
            return getString(R.string.pb_vote_type_2_3);
        }
        return "";
    }

    private void showVoteDetailsPop() {
        open_vote_details = false;
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.vote_details_pop, null);
        View meetFl = getActivity().findViewById(R.id.meet_fl);
        int px_5 = ScreenUtils.dip2px(getContext(), 5);
        int px_20 = ScreenUtils.dip2px(getContext(), 20);
        LogUtil.d(TAG, "dp转px 5=" + px_5 + ",20=" + px_20);
        PopupWindow voteDetailspop = new PopupWindow(inflate, meetFl.getWidth() + px_5, meetFl.getHeight() + px_20);
        voteDetailspop.setAnimationStyle(R.style.Anim_PopupWindow);
        voteDetailspop.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        voteDetailspop.setTouchable(true);
        voteDetailspop.setFocusable(true);
        RecyclerView details_rv = inflate.findViewById(R.id.details_rv);
        details_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        details_rv.setAdapter(optionAdapter);
        inflate.findViewById(R.id.imag_btn).setOnClickListener(v -> voteDetailspop.dismiss());
        voteDetailspop.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        //使内部的EditText获取焦点输入的时候，软键盘不会遮挡住
        //SOFT_INPUT_ADJUST_PAN:把整个Layout顶上去露出获得焦点的EditText,不压缩多余空间
        voteDetailspop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        voteDetailspop.showAtLocation(getActivity().findViewById(R.id.meet_fl), Gravity.START | Gravity.BOTTOM, 0, 0);
    }

    private void showVoteInfoPop() {
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.survey_entry_pop, null);
        View meetFl = getActivity().findViewById(R.id.meet_fl);
        int px_5 = ScreenUtils.dip2px(getContext(), 5);
        int px_20 = ScreenUtils.dip2px(getContext(), 20);
        LogUtil.d(TAG, "dp转px 5=" + px_5 + ",20=" + px_20);
        pop = new PopupWindow(inflate, meetFl.getWidth() + px_5, meetFl.getHeight() + px_20);
        pop.setAnimationStyle(R.style.Anim_PopupWindow);
        pop.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        pop.setTouchable(true);
        pop.setFocusable(true);
//      setFocusable 的值为false的时候
//      setOutsideTouchable才有效果
//      不然设置任何值都无效
//        pop.setOutsideTouchable(false);
        ViewHolder holder = new ViewHolder(inflate);
        holderEvent(holder);
        pop.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        //使内部的EditText获取焦点输入的时候，软键盘不会遮挡住
        //SOFT_INPUT_ADJUST_PAN:把整个Layout顶上去露出获得焦点的EditText,不压缩多余空间
        pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        pop.showAtLocation(getActivity().findViewById(R.id.meet_fl), Gravity.LEFT | Gravity.BOTTOM, 0, 0);
    }

    private void holderEvent(ViewHolder holder) {
        holder.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        holder.rv.setAdapter(popAdapter);
        holder.survey_registered_spinner.setAdapter(popspAdapter);
        holder.survey_type_spinner.setAdapter(spSuevetTypeAdapter);
        if (mVoteData != null && mVoteData.size() > popPosion) {
            holder.survey_content_edt.setText(mVoteData.get(popPosion).getContent().toStringUtf8());
            holder.survey_registered_spinner.setSelection(mVoteData.get(popPosion).getMode());
            holder.survey_type_spinner.setSelection(mVoteData.get(popPosion).getType());
            List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo1 = mVoteData.get(popPosion).getItemList();
            if (optionInfo1.size() > 0)
                holder.edt_option_a.setText(optionInfo1.get(0).getText().toStringUtf8());
            if (optionInfo1.size() > 1)
                holder.edt_option_b.setText(optionInfo1.get(1).getText().toStringUtf8());
            if (optionInfo1.size() > 2)
                holder.edt_option_c.setText(optionInfo1.get(2).getText().toStringUtf8());
            if (optionInfo1.size() > 3)
                holder.edt_option_d.setText(optionInfo1.get(3).getText().toStringUtf8());
            if (optionInfo1.size() > 4)
                holder.edt_option_e.setText(optionInfo1.get(4).getText().toStringUtf8());
        }
        popAdapter.setListener((view, posion) -> {
            popAdapter.setCheck(posion);
            popPosion = posion;
            holder.survey_content_edt.setText(mVoteData.get(popPosion).getContent().toStringUtf8());
            holder.survey_registered_spinner.setSelection(mVoteData.get(popPosion).getMode());
            holder.survey_type_spinner.setSelection(mVoteData.get(popPosion).getType());
            holder.edt_option_a.setText("");
            holder.edt_option_b.setText("");
            holder.edt_option_c.setText("");
            holder.edt_option_d.setText("");
            holder.edt_option_e.setText("");
            List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = mVoteData.get(popPosion).getItemList();
            for (int i = 0; i < optionInfo.size(); i++) {
                if (i == 0) holder.edt_option_a.setText(optionInfo.get(0).getText().toStringUtf8());
                if (i == 1) holder.edt_option_b.setText(optionInfo.get(1).getText().toStringUtf8());
                if (i == 2) holder.edt_option_c.setText(optionInfo.get(2).getText().toStringUtf8());
                if (i == 3) holder.edt_option_d.setText(optionInfo.get(3).getText().toStringUtf8());
                if (i == 4) holder.edt_option_e.setText(optionInfo.get(4).getText().toStringUtf8());
            }
        });
        holder.pop_add_btn.setOnClickListener(v -> {
            String edt_str = holder.survey_content_edt.getText().toString();
            if (edt_str.trim().isEmpty()) {
                ToastUtils.showShort(R.string.please_input_content);
                return;
            } else if (edt_str.length() > Macro.title_max_length) {
                ToastUtils.showShort(R.string.beyond_max_length);
                return;
            }
            String a = holder.edt_option_a.getText().toString().trim();
            String b = holder.edt_option_b.getText().toString().trim();
            String c = holder.edt_option_c.getText().toString().trim();
            String d = holder.edt_option_d.getText().toString().trim();
            String e = holder.edt_option_e.getText().toString().trim();
            //因为字符资源文件中的索引0就是匿名，1是记名所以可以对应起来
            int mode = holder.survey_registered_spinner.getSelectedItemPosition();
            int index = holder.survey_type_spinner.getSelectedItemPosition();
            LogUtil.d(TAG, "holderEvent: index= " + index + ", mode= " + mode);
            /*
             * 0 多选
             * 1 单选
             * 2 5选4
             * 3 5选3
             * 4 5选2
             * 5 3选2
             */
            List<ByteString> chooses = new ArrayList<>();
            if (index == 2 || index == 3 || index == 4) {//一定要有5个选项
                if (a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty() || e.isEmpty()) {
                    ToastUtils.showShort(R.string.please_input_five_option);
                    return;
                } else {
                    chooses.add(MyUtils.s2b(a));
                    chooses.add(MyUtils.s2b(b));
                    chooses.add(MyUtils.s2b(c));
                    chooses.add(MyUtils.s2b(d));
                    chooses.add(MyUtils.s2b(e));
                }
            } else if (index == 5) {//一定要有3个选项
                if (a.isEmpty()) {//a项空
                    if (!b.isEmpty() || !c.isEmpty() || !d.isEmpty() || !e.isEmpty()) {
                        ToastUtils.showShort(R.string.please_order_input);
                        return;
                    } else if (b.isEmpty() && c.isEmpty() && d.isEmpty() && e.isEmpty()) {
                        ToastUtils.showShort(R.string.please_input_three_option);
                        return;
                    }
                } else {
                    if (b.isEmpty()) {//A项不为空，B项是空
                        if (c.isEmpty() && d.isEmpty() && e.isEmpty()) {
                            ToastUtils.showShort(R.string.please_input_three_option);
                            return;
                        } else {
                            ToastUtils.showShort(R.string.please_order_input);
                            return;
                        }
                    } else {//A项和B项不为空
                        if (c.isEmpty()) {//C项为空
                            if (d.isEmpty() && e.isEmpty()) {
                                ToastUtils.showShort(R.string.please_input_three_option);
                                return;
                            } else {//C项为空，但是D和E至少有一个不为空
                                if (!d.isEmpty() || !e.isEmpty()) {
                                    ToastUtils.showShort(R.string.please_order_input);
                                    return;
                                }
                            }
                        } else {//A项和B项和C项都不为空
                            if (!d.isEmpty() || !e.isEmpty()) {//但是多出来了选项
                                ToastUtils.showShort(R.string.max_input_three_option);
                                return;
                            } else {//A项和B项和C项都不为空,D和E为空
                                chooses.add(MyUtils.s2b(a));
                                chooses.add(MyUtils.s2b(b));
                                chooses.add(MyUtils.s2b(c));
                            }
                        }
                    }
                }
            } else {//多选和单选
                if (!a.isEmpty()) chooses.add(MyUtils.s2b(a));
                if (!b.isEmpty()) chooses.add(MyUtils.s2b(b));
                if (!c.isEmpty()) chooses.add(MyUtils.s2b(c));
                if (!d.isEmpty()) chooses.add(MyUtils.s2b(d));
                if (!e.isEmpty()) chooses.add(MyUtils.s2b(e));
            }
            LogUtil.e(TAG, "ElectionManageFragment.holderEvent :  chooses.size --> " + chooses.size());
            if (chooses.isEmpty() || chooses.size() < 2) {
                ToastUtils.showShort(R.string.please_input_option);
                return;
            }
            List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> votes = new ArrayList<>();
            InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder();
            builder.setVoteid(0);
            builder.setContent(MyUtils.s2b(edt_str));
            builder.setMaintype(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election.getNumber());
            builder.setMode(mode);
            builder.setType(index);
//            builder.setTimeouts(300);//设置默认5分钟
            builder.setSelectcount(chooses.size());
            builder.addAllText(chooses);
            InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = builder.build();
            votes.add(build);
            jni.createVote(votes);
        });
        holder.imag_btn.setOnClickListener(v -> pop.dismiss());
        holder.pop_modif_btn.setOnClickListener(v -> {
            String edt_str = holder.survey_content_edt.getText().toString();
            if (edt_str.trim().isEmpty()) {
                ToastUtils.showShort(R.string.please_input_content);
                return;
            }
            if (edt_str.trim().length() > Macro.title_max_length) {
                ToastUtils.showShort(R.string.beyond_max_length);
                return;
            }
            if (mVoteData != null && mVoteData.size() > popPosion) {
                InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = mVoteData.get(popPosion);
                int mode = holder.survey_registered_spinner.getSelectedItemPosition();
                int type = holder.survey_type_spinner.getSelectedItemPosition();
                List<ByteString> chooses = new ArrayList<>();
                String a = holder.edt_option_a.getText().toString().trim();
                String b = holder.edt_option_b.getText().toString().trim();
                String c = holder.edt_option_c.getText().toString().trim();
                String d = holder.edt_option_d.getText().toString().trim();
                String e = holder.edt_option_e.getText().toString().trim();
                if (!a.isEmpty()) chooses.add(MyUtils.s2b(a));
                if (!b.isEmpty()) chooses.add(MyUtils.s2b(b));
                if (!c.isEmpty()) chooses.add(MyUtils.s2b(c));
                if (!d.isEmpty()) chooses.add(MyUtils.s2b(d));
                if (!e.isEmpty()) chooses.add(MyUtils.s2b(e));
                String content = holder.survey_content_edt.getText().toString().trim();
                if (content.isEmpty()) {//选举的内容不能不写
                    ToastUtils.showShort(R.string.please_input_content);
                    return;
                }
                if (type == 2 || type == 3 || type == 4) {//必须是5个选项
                    if (chooses.size() < 5) {
                        ToastUtils.showShort(R.string.please_input_five_option);
                        return;
                    }
                } else if (type == 5) {//必须是前三个选项
                    if (!a.isEmpty() && !b.isEmpty() && !c.isEmpty() && d.isEmpty() && e.isEmpty()) {
                    } else {
                        ToastUtils.showShort(R.string.must_order_input_three);
                        return;
                    }
                }
                if (chooses.isEmpty()) {//必须有选项
                    ToastUtils.showShort(R.string.please_input_option);
                    return;
                }
                InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder();
                builder.setVoteid(voteInfo.getVoteid());
                builder.setContent(MyUtils.s2b(content));
                builder.setMaintype(voteInfo.getMaintype());
                builder.setMode(mode);
                builder.setType(type);
                builder.setTimeouts(voteInfo.getTimeouts());
                builder.setSelectcount(chooses.size());
                builder.addAllText(chooses);
                InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = builder.build();
                jni.modifyVote(build);
            } else
                ToastUtils.showShort(R.string.please_choose_vote);
        });
        holder.pop_del_btn.setOnClickListener(v -> {
            if (mVoteData != null && !mVoteData.isEmpty()) {
                List<Integer> voteids = new ArrayList<>();
                if (popPosion >= mVoteData.size()) {
                    ToastUtils.showShort(R.string.please_choose_vote);
                    return;
                }
                voteids.add(mVoteData.get(popPosion).getVoteid());
                jni.deleteVote(voteids);
                holder.survey_content_edt.setText("");
                holder.survey_registered_spinner.setSelection(0);
                holder.survey_type_spinner.setSelection(0);
                holder.edt_option_a.setText("");
                holder.edt_option_b.setText("");
                holder.edt_option_c.setText("");
                holder.edt_option_d.setText("");
                holder.edt_option_e.setText("");
            }
        });
        holder.export_excel.setOnClickListener(v -> {
            if (mVoteData != null && !mVoteData.isEmpty()) {
                String[] titles = new String[]{
                        getString(R.string.survet_content), getString(R.string.whether_registered), getString(R.string.option_count),
                        getString(R.string.answer_count), getString(R.string.option_a), getString(R.string.option_b),
                        getString(R.string.option_c), getString(R.string.option_d), getString(R.string.option_e)};
                Export.VoteEntry(getString(R.string.survey_entry), titles, mVoteData, 1);
            } else
                ToastUtils.showShort(R.string.no_data_export);
        });
        holder.import_excel.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.ms-excel");//指定打开表格类型文件
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, JUST_OPEN_SURVEY_EXCEL_CODE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JUST_OPEN_SURVEY_EXCEL_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                File file = UriUtils.uri2File(uri);
                if (file != null) {
                    try {
                        List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> votes = Export.importSurvey(file.getAbsolutePath());
                        if (!votes.isEmpty()) jni.createVote(votes);
                    } catch (Exception e) {
                        LogUtils.e(TAG, "导入选举异常");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void showDialog(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote, int time) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.whether_modify_timeout));
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(getString(R.string.ensure), (dialog, which) -> {
            List<ByteString> chooses = new ArrayList<ByteString>();
            List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = vote.getItemList();
            for (int i = 0; i < optionInfo.size(); i++) {
                InterfaceVote.pbui_SubItem_VoteItemInfo pbui_subItem_voteItemInfo = optionInfo.get(i);
                chooses.add(pbui_subItem_voteItemInfo.getText());
            }
            InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.Builder item = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder();
            item.setVoteid(vote.getVoteid());
            item.setContent(vote.getContent());
            item.setMaintype(vote.getMaintype());
            item.setMode(vote.getMode());
            item.setType(vote.getType());
            item.setTimeouts(time);
            item.setSelectcount(chooses.size());
            item.addAllText(chooses);
            InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = item.build();
            jni.modifyVote(build);
        });
        builder.create().show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setBtnText();
            fun_queryAttendPeople();
        }
    }

    public static class ViewHolder {
        public View rootView;
        public ImageButton imag_btn;
        public RecyclerView rv;
        public EditText survey_content_edt;
        public Spinner survey_type_spinner;
        public Spinner survey_registered_spinner;
        public EditText edt_option_a;
        public EditText edt_option_b;
        public EditText edt_option_c;
        public EditText edt_option_d;
        public EditText edt_option_e;
        public Button pop_add_btn;
        public Button pop_modif_btn;
        public Button pop_del_btn;
        public Button export_excel;
        public Button import_excel;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.imag_btn = (ImageButton) rootView.findViewById(R.id.imag_btn);
            this.rv = (RecyclerView) rootView.findViewById(R.id.rv);
            this.survey_content_edt = (EditText) rootView.findViewById(R.id.survey_content_edt);
            this.survey_type_spinner = (Spinner) rootView.findViewById(R.id.survey_type_spinner);
            this.survey_registered_spinner = (Spinner) rootView.findViewById(R.id.survey_registered_spinner);
            this.edt_option_a = (EditText) rootView.findViewById(R.id.edt_option_a);
            this.edt_option_b = (EditText) rootView.findViewById(R.id.edt_option_b);
            this.edt_option_c = (EditText) rootView.findViewById(R.id.edt_option_c);
            this.edt_option_d = (EditText) rootView.findViewById(R.id.edt_option_d);
            this.edt_option_e = (EditText) rootView.findViewById(R.id.edt_option_e);
            this.pop_add_btn = (Button) rootView.findViewById(R.id.pop_add_btn);
            this.pop_modif_btn = (Button) rootView.findViewById(R.id.pop_modif_btn);
            this.pop_del_btn = (Button) rootView.findViewById(R.id.pop_del_btn);
            this.export_excel = (Button) rootView.findViewById(R.id.export_excel);
            this.import_excel = (Button) rootView.findViewById(R.id.import_excel);
        }

    }

    public static class SurveyViewHolder {
        public View rootView;
        public ImageButton imag_btn;
        public TextView vote_type_tv;
        public TextView member_count_tv;
        public TextView title_tv;
        public PieChart pie_chart;
        public TextView option_a;
        public LinearLayout vote_option_a;
        public TextView option_b;
        public LinearLayout vote_option_b;
        public TextView option_c;
        public LinearLayout vote_option_c;
        public TextView option_d;
        public LinearLayout vote_option_d;
        public TextView option_e;
        public LinearLayout vote_option_e;
        public LinearLayout vote_type_top_ll;
        public TextView vote_member_count_tv;

        public SurveyViewHolder(View rootView) {
            this.rootView = rootView;
            this.imag_btn = (ImageButton) rootView.findViewById(R.id.imag_btn);
            this.vote_type_tv = (TextView) rootView.findViewById(R.id.vote_type_tv);
            this.member_count_tv = (TextView) rootView.findViewById(R.id.member_count_tv);
            this.title_tv = (TextView) rootView.findViewById(R.id.title_tv);
            this.pie_chart = (PieChart) rootView.findViewById(R.id.pie_chart);
            this.option_a = (TextView) rootView.findViewById(R.id.option_a);
            this.vote_option_a = (LinearLayout) rootView.findViewById(R.id.vote_option_a);
            this.option_b = (TextView) rootView.findViewById(R.id.option_b);
            this.vote_option_b = (LinearLayout) rootView.findViewById(R.id.vote_option_b);
            this.option_c = (TextView) rootView.findViewById(R.id.option_c);
            this.vote_option_c = (LinearLayout) rootView.findViewById(R.id.vote_option_c);
            this.option_d = (TextView) rootView.findViewById(R.id.option_d);
            this.vote_option_d = (LinearLayout) rootView.findViewById(R.id.vote_option_d);
            this.option_e = (TextView) rootView.findViewById(R.id.option_e);
            this.vote_option_e = (LinearLayout) rootView.findViewById(R.id.vote_option_e);
            this.vote_type_top_ll = (LinearLayout) rootView.findViewById(R.id.vote_type_top_ll);
            this.vote_member_count_tv = (TextView) rootView.findViewById(R.id.vote_member_count_tv);
        }

    }

    public static class ChoosePopViewHolder {
        public View rootView;
        public CheckBox all_number_cb;
        public RecyclerView choose_rv;
        public Button ensure;
        public Button cancel;

        public ChoosePopViewHolder(View rootView) {
            this.rootView = rootView;
            this.all_number_cb = (CheckBox) rootView.findViewById(R.id.all_number_cb);
            this.choose_rv = (RecyclerView) rootView.findViewById(R.id.choose_rv);
            this.ensure = (Button) rootView.findViewById(R.id.ensure);
            this.cancel = (Button) rootView.findViewById(R.id.cancel);
        }

    }
}
