package com.pa.paperless.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.intrusoft.scatter.ChartData;
import com.intrusoft.scatter.PieChart;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceSignin;
import com.mogujie.tt.protobuf.InterfaceVote;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.ChooseJoinVoteAdapter;
import com.pa.paperless.adapter.rvadapter.VoteAdapter;
import com.pa.paperless.adapter.rvadapter.VoteInfoPopAdapter;
import com.pa.paperless.adapter.rvadapter.VoteOptionResultAdapter;
import com.pa.paperless.data.bean.ImportVoteBean;
import com.pa.paperless.data.bean.VoteResultSubmitMember;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.utils.ConvertUtil;
import com.pa.paperless.utils.Export;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.PopUtil;
import com.pa.paperless.utils.ScreenUtils;

import com.pa.paperless.utils.UriUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;
import static com.pa.paperless.activity.MeetingActivity.saveIndex;
import static com.pa.paperless.data.constant.Values.roomId;


/**
 * Created by xlk on 2017/10/31.
 * 投票查询
 */
public class VoteFragment extends BaseFragment implements View.OnClickListener {

    private final String TAG = "VoteFragment-->";
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> mVoteData = new ArrayList<>();
    private VoteAdapter mVoteAdapter;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos;
    private RecyclerView voteRl;
    private TextView yingdaoTv;
    private TextView yiqiandaoTv;
    private TextView weiqiandaoTv;
    private Button voteEntry;
    private TextView countdownTv;
    private Spinner countdownSpinner;
    private Button startBtn;
    private Button stopBtn;
    private ArrayAdapter spAdapter, popspAdapter;
    private int mPosion;
    private VoteInfoPopAdapter popAdapter;
    private int popPosion;
    private PopupWindow pop;
    private int JUST_OPEN_EXCEL_CODE = 1023;
    public static boolean isVoteManage = true;
    private int selectedItem = 0;
    private List<VoteResultSubmitMember> submitMemberData;
    private VoteOptionResultAdapter optionAdapter;
    private boolean open_vote_details;
    private boolean open_vote_chart;
    private PopupWindow ChartPop;
    private List<ChartData> chartDatas;
    private int countPre;
    private List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> chooseData;
    private ChooseJoinVoteAdapter chooseAdapter;
    private boolean showJoinPop;
    private PopupWindow choosePop;
    private boolean clickBeganVote;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.right_vote, container, false);
        initView(inflate);
        setBtnText();
        initAdapter();
        fun_queryAttendPeople();
        return inflate;
    }

    private void initAdapter() {
        popAdapter = new VoteInfoPopAdapter(getContext(), mVoteData);
        mVoteAdapter = new VoteAdapter(getContext(), mVoteData);
        mVoteAdapter.setHasStableIds(true);
        voteRl.setLayoutManager(new LinearLayoutManager(getContext()));
        voteRl.setAdapter(mVoteAdapter);
    }

    private void setBtnText() {
        voteEntry.setVisibility(isVoteManage ? View.VISIBLE : View.INVISIBLE);
        countdownTv.setVisibility(isVoteManage ? View.VISIBLE : View.INVISIBLE);
        countdownSpinner.setVisibility(isVoteManage ? View.VISIBLE : View.INVISIBLE);
        if (spAdapter == null) {
            String[] stringArray = getResources().getStringArray(R.array.countdown_spinner);
            spAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, stringArray);
        }
        if (popspAdapter == null) {
            String[] stringArray = getResources().getStringArray(R.array.whether);
            popspAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, stringArray);
        }
        countdownSpinner.setAdapter(spAdapter);
        countdownSpinner.setSelection(4, true);
        voteEntry.setText(getString(R.string.vote_entry));
        startBtn.setText(isVoteManage ? getString(R.string.start_vote) : getString(R.string.view_details));
        stopBtn.setText(isVoteManage ? getString(R.string.button_stop_vote) : getString(R.string.view_chart));
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
            InterfaceVote.pbui_Type_MeetVoteDetailInfo pbui_type_meetVoteDetailInfo = jni.queryVote();
            if (pbui_type_meetVoteDetailInfo == null) {
                mVoteData.clear();
                LogUtil.e(TAG, "fun_queryVote :  清空刷新adapter --> ");
                mVoteAdapter.notifyDataSetChanged();
                popAdapter.notifyDataSetChanged();
                return;
            } else {
                mVoteData.clear();
                List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> itemList = pbui_type_meetVoteDetailInfo.getItemList();
                for (InterfaceVote.pbui_Item_MeetVoteDetailInfo detailInfo : itemList) {
                    if (detailInfo.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote.getNumber()) {
                        mVoteData.add(detailInfo);
                    }
                }
                LogUtil.e(TAG, "VoteFragment.fun_queryVote :  mVoteAdapter进行刷新 --> ");
                mVoteAdapter.notifyDataSetChanged();
                //PopupWindow中的适配器
                popAdapter.notifyDataSetChanged();
                mVoteAdapter.setItemListener((view, posion) -> {
                    mVoteAdapter.setCheckedId(posion);
                    mPosion = posion;
//                    InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = mVoteData.get(posion);
//                    loeVoteInfo(voteInfo);
                });
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryVote() {
        try {
            //200.查询投票
            InterfaceVote.pbui_Type_MeetVoteDetailInfo object = jni.queryVoteByType(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote.getNumber());
            if (object == null) {
                mVoteData.clear();
                LogUtil.e(TAG, "fun_queryVote :  清空刷新adapter --> ");
                mVoteAdapter.notifyDataSetChanged();
                popAdapter.notifyDataSetChanged();
                return;
            }
            mVoteData.clear();
            List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> itemList = object.getItemList();
            mVoteData.addAll(itemList);
            LogUtil.e(TAG, "VoteFragment.fun_queryVote :  mVoteAdapter进行刷新 --> ");
            mVoteAdapter.notifyDataSetChanged();
            //PopupWindow中的适配器
            popAdapter.notifyDataSetChanged();
            mVoteAdapter.setItemListener((view, posion) -> {
                mVoteAdapter.setCheckedId(posion);
                mPosion = posion;
//                InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = mVoteData.get(posion);
//                loeVoteInfo(voteInfo);
            });
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void loeVoteInfo(InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo) {
        String content = voteInfo.getContent().toStringUtf8();
        int maintype = voteInfo.getMaintype();
        int mode = voteInfo.getMode();
        int select = voteInfo.getSelcnt();
        int selectcount = voteInfo.getSelectcount();
        int type = voteInfo.getType();
        int voteid = voteInfo.getVoteid();
        int votestate = voteInfo.getVotestate();
        List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = voteInfo.getItemList();
        for (int i = 0; i < optionInfo.size(); i++) {
            String text = optionInfo.get(i).getText().toStringUtf8();
            int selcnt = optionInfo.get(i).getSelcnt();
            LogUtil.d(TAG, "loeVoteInfo: text= " + text + ", selcnt= " + selcnt);
        }
        LogUtil.e(TAG, "VoteFragment.loeVoteInfo :   --> " + content + ",maintype= " + maintype
                + ",mode= " + mode + ",select= " + select + ",selectcount= " + selectcount
                + ",type= " + type + ",voteid= " + voteid + ",votestate= " + votestate);
        LogUtil.e(TAG, "VoteFragment.loeVoteInfo :  ------------------------> ");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.SIGN_CHANGE_INFORM://签到变更通知
                fun_querySign();
                break;
            case EventType.Vote_Change_Inform://投票变更通知
                InterfaceBase.pbui_MeetNotifyMsg object = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
                int id = object.getId();
                int opermethod = object.getOpermethod();
                LogUtil.e(TAG, "VoteFragment.getEventMessage :  投票变更通知 --> opermethod= " + opermethod + ", id= " + id);
                queryVote();
                break;
            case EventType.MEMBER_CHANGE_INFORM://参会人员变更
                fun_queryAttendPeople();
                if (mVoteData.size() > mPosion) {
                    fun_queryAttendPeopleDetailed(mVoteData.get(mPosion));
                }
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
            case EventType.FACESTATUS_CHANGE_INFORM://界面状态变更通知
                if (mVoteData.size() > mPosion) {
                    fun_queryAttendPeopleDetailed(mVoteData.get(mPosion));
                }
                break;

        }
    }

    @Override
    public void onStart() {
        LogUtil.i("F_life", "VoteFragment.onStart :   --> ");
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        LogUtil.i("F_life", "VoteFragment.onStop :   --> ");
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtil.d(TAG, "onHiddenChanged: hidden=  " + hidden);
        if (!hidden) {
            LogUtil.e(TAG, "VoteFragment.onHiddenChanged :  显示 --> ");
            fun_queryAttendPeople();
            setBtnText();
        } else {
            if (saveIndex != Macro.PB_MEET_FUN_CODE_VOTE_MANAGE) {
                if (pop != null && pop.isShowing()) {
                    pop.dismiss();
                }
            }
        }
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
                showVoteInfoPop();
                break;
            case R.id.start_btn:
                if (mVoteData != null && mVoteData.size() > mPosion) {
                    if (isVoteManage) {//开始投票
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
                        //只有运行到这一步才设置true
                        clickBeganVote = true;
                        fun_queryAttendPeopleDetailed(voteInfo);
                    } else {//投票详情
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
                } else
                    ToastUtils.showShort(R.string.please_choose_vote);
                break;
            case R.id.stop_btn:
                if (mVoteData != null && mVoteData.size() > mPosion) {
                    if (isVoteManage) {//结束投票
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
                break;
            default:
                break;
        }
    }

    private void fun_queryAttendPeopleDetailed(InterfaceVote.pbui_Item_MeetVoteDetailInfo
                                                       voteInfo) {
        try {
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
            for (InterfaceMember.pbui_Item_MeetMemberDetailInfo item : itemList) {
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
        int px_5 = ScreenUtils.dip2px(getContext(), 5);
        int px_20 = ScreenUtils.dip2px(getContext(), 20);
        LogUtil.d(TAG, "dp转px 5=" + px_5 + ",20=" + px_20);
        choosePop = new PopupWindow(inflate, meetFl.getWidth() + px_5, meetFl.getHeight() + px_20);
        choosePop.setAnimationStyle(R.style.Anim_PopupWindow);
        choosePop.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        choosePop.setTouchable(true);
        choosePop.setFocusable(true);
        ChooseViewHolder holder = new ChooseViewHolder(inflate);
        choosePopEvent(holder, voteInfo);
        choosePop.setOnDismissListener(() -> showJoinPop = false);
        choosePop.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        //使内部的EditText获取焦点输入的时候，软键盘不会遮挡住
        //SOFT_INPUT_ADJUST_PAN:把整个Layout顶上去露出获得焦点的EditText,不压缩多余空间
        choosePop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        choosePop.showAtLocation(getActivity().findViewById(R.id.meet_fl), Gravity.LEFT | Gravity.BOTTOM, 0, 0);
    }

    private void choosePopEvent(ChooseViewHolder
                                        holder, InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo) {
        holder.choose_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        holder.choose_rv.setAdapter(chooseAdapter);
        holder.all_number_cb.setChecked(true);
        chooseAdapter.setCheckAll(true);
        chooseAdapter.setListener((view, posion) -> {
            InterfaceMember.pbui_Item_MeetMemberDetailInfo info = chooseData.get(posion);
            int memberdetailflag = info.getMemberdetailflag();
            boolean isonline = memberdetailflag == InterfaceMember.Pb_MemberDetailFlag.Pb_MEMBERDETAIL_FLAG_ONLINE.getNumber();
            int state = info.getFacestatus();
            /** **** **  在线并且有权限且界面在参会人界面或则投票界面  ** **** **/
            if (isonline && info.getPermission() > 15 && (state == 1 || state == 3)) {
                chooseAdapter.setChecks(info.getMemberid());
                holder.all_number_cb.setChecked(chooseAdapter.isCheckAll());
                chooseAdapter.notifyDataSetChanged();
            } else
                ToastUtils.showShort(R.string.must_choose_online);
        });
        holder.all_number_cb.setOnClickListener(v -> {
            boolean checked = holder.all_number_cb.isChecked();
            holder.all_number_cb.setChecked(checked);
            chooseAdapter.setCheckAll(checked);
        });
        holder.ensure.setOnClickListener(v -> {
            List<Integer> checks = chooseAdapter.getChecks();
            if (checks.isEmpty()) {
                ToastUtils.showShort(R.string.please_choose);
                return;
            }
            LogUtil.e(TAG, "VoteFragment.choosePopEvent : 是否有自己的ID  --> " + checks.contains(Values.localMemberId));
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
        ChartPop = new PopupWindow(inflate, meetFl.getWidth() + px_5, meetFl.getHeight() + px_20);
        ChartPop.setAnimationStyle(R.style.Anim_PopupWindow);
        ChartPop.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        ChartPop.setTouchable(true);
        ChartPop.setFocusable(true);
        ChartViewHolder holder = new ChartViewHolder(inflate);
        ChartViewHolderEvent(holder, voteInfo);
        ChartPop.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        //使内部的EditText获取焦点输入的时候，软键盘不会遮挡住
        //SOFT_INPUT_ADJUST_PAN:把整个Layout顶上去露出获得焦点的EditText,不压缩多余空间
        ChartPop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ChartPop.showAtLocation(getActivity().findViewById(R.id.meet_fl), Gravity.START | Gravity.BOTTOM, 0, 0);
    }

    private void ChartViewHolderEvent(ChartViewHolder
                                              holder, InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo) {
        holder.imag_btn.setOnClickListener(v -> ChartPop.dismiss());
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
        /** **** **  设置图形数据  ** **** **/
        if (chartDatas == null) chartDatas = new ArrayList<>();
        else chartDatas.clear();
        int maintype = voteInfo.getMaintype();
        boolean isVote = maintype == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote.getNumber();
        holder.vote_option_color_a.setBackgroundColor(isVote ? getContext().getColor(R.color.chart_color_green) : getContext().getColor(R.color.chart_color_red));
        holder.vote_option_color_b.setBackgroundColor(isVote ? getContext().getColor(R.color.chart_color_red) : getContext().getColor(R.color.chart_color_green));
        holder.vote_option_color_c.setBackgroundColor(isVote ? getContext().getColor(R.color.chart_color_yellow) : getContext().getColor(R.color.chart_color_blue));
        if (isVote) {
            holder.vote_type_top_ll.setVisibility(View.GONE);
            holder.vote_member_count_tv.setVisibility(View.VISIBLE);
            holder.vote_member_count_tv.setText(getString(R.string.vote_member_count, yiTou));
        } else {
            holder.vote_type_top_ll.setVisibility(View.VISIBLE);
            holder.vote_member_count_tv.setVisibility(View.GONE);
        }

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
                    setChartData(count, selcnt, getContext().getColor(R.color.black), isVote ? getContext().getColor(R.color.chart_color_green) : getContext().getColor(R.color.chart_color_red));
                } else if (i == 1) {
                    holder.vote_option_b.setVisibility(View.VISIBLE);
                    holder.option_b.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, getContext().getColor(R.color.black), isVote ? getContext().getColor(R.color.chart_color_red) : getContext().getColor(R.color.chart_color_green));
                } else if (i == 2) {
                    holder.vote_option_c.setVisibility(View.VISIBLE);
                    holder.option_c.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, getContext().getColor(R.color.black), isVote ? getContext().getColor(R.color.chart_color_yellow) : getContext().getColor(R.color.chart_color_blue));
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
            chartDatas.add(new ChartData(lastChartData.getDisplayText(), lastChartData.getPartInPercent() + (100 - countPre), lastChartData.getTextColor(), lastChartData.getBackgroundColor()));
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
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.vote_entry_pop, null);
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
//      pop.setOutsideTouchable(false);
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
        holder.vote_pop_spinner.setAdapter(popspAdapter);
        if (mVoteData != null && mVoteData.size() > popPosion) {
            holder.modif_edt.setText(mVoteData.get(popPosion).getContent().toStringUtf8());
            holder.vote_pop_spinner.setSelection(mVoteData.get(popPosion).getMode());
        }
        popAdapter.setListener((view, posion) -> {
            popAdapter.setCheck(posion);
            popPosion = posion;
            holder.modif_edt.setText(mVoteData.get(popPosion).getContent().toStringUtf8());
            holder.vote_pop_spinner.setSelection(mVoteData.get(popPosion).getMode());
        });
        holder.pop_add_btn.setOnClickListener(v -> {
            String edt_str = holder.modif_edt.getText().toString();
            if (edt_str.trim().isEmpty()) {
                ToastUtils.showShort(R.string.please_input_content);
                return;
            } else if (edt_str.trim().length() > Macro.title_max_length) {
                ToastUtils.showShort(R.string.beyond_max_length);
                return;
            }
            //因为字符资源文件中的索引0就是匿名，1是记名所以可以对应起来
            int mode = holder.vote_pop_spinner.getSelectedItemPosition();
            List<ByteString> chooses = new ArrayList<>();
            chooses.add(MyUtils.s2b(getString(R.string.favour)));
            chooses.add(MyUtils.s2b(getString(R.string.against)));
            chooses.add(MyUtils.s2b(getString(R.string.waiver)));
            List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> votes = new ArrayList<>();
            InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder();
            builder.setContent(MyUtils.s2b(edt_str));
            builder.setMaintype(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote.getNumber());
            builder.setMode(mode);
            builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE.getNumber());
            builder.setSelectcount(chooses.size());
            builder.addAllText(chooses);
            InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = builder.build();
            votes.add(build);
            jni.createVote(votes);
        });
        holder.imag_btn.setOnClickListener(v -> pop.dismiss());
        holder.pop_modif_btn.setOnClickListener(v -> {
            String edt_str = holder.modif_edt.getText().toString();
            if (edt_str.trim().isEmpty()) {
                ToastUtils.showShort(R.string.please_input_content);
                return;
            }
            if (edt_str.trim().length() > Macro.title_max_length) {
                ToastUtils.showShort(R.string.beyond_max_length);
                return;
            }
            if (mVoteData != null && mVoteData.size() > popPosion) {
                List<ByteString> chooses = new ArrayList<>();
                chooses.add(MyUtils.s2b(getString(R.string.favour)));
                chooses.add(MyUtils.s2b(getString(R.string.against)));
                chooses.add(MyUtils.s2b(getString(R.string.waiver)));
                InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = mVoteData.get(popPosion);
                int mode = holder.vote_pop_spinner.getSelectedItemPosition();
                InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder();
                builder.setVoteid(voteInfo.getVoteid());
                builder.setContent(MyUtils.s2b(edt_str));
                builder.setMaintype(voteInfo.getMaintype());
                builder.setMode(mode);
                builder.setType(voteInfo.getType());
                builder.setTimeouts(voteInfo.getTimeouts());
                builder.setSelectcount(chooses.size());
                builder.addAllText(chooses);
                InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = builder.build();
                jni.modifyVote(build);
            } else {
                ToastUtils.showShort(R.string.please_choose_vote);
            }
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
                queryVote();
                holder.modif_edt.setText("");
                holder.vote_pop_spinner.setSelection(0);
            }
        });
        holder.export_excel.setOnClickListener(v -> {
            if (mVoteData != null && !mVoteData.isEmpty()) {
                String[] titles = new String[]{getString(R.string.vote_content), getString(R.string.whether_registered)};
                Export.VoteEntry(getString(R.string.vote_entry), titles, mVoteData, 0);
            } else
                ToastUtils.showShort(R.string.no_data_export);
        });
        holder.import_excel.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.ms-excel");//指定打开表格类型文件
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, JUST_OPEN_EXCEL_CODE);
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JUST_OPEN_EXCEL_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String path = "";
                try {
                    path = UriUtil.getFilePath(getContext(), uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (path == null || path.isEmpty()) {
                    ToastUtils.showShort(R.string.get_file_path_fail);
                    return;
                }
                if (!path.endsWith(".xls")) {
                    ToastUtils.showShort(R.string.only_xls_file);
                    return;
                }
                List<ImportVoteBean> importVoteBeen = Export.ReadVoteExcel(path);
                List<ByteString> chooses = new ArrayList<ByteString>();
                chooses.add(MyUtils.s2b(getString(R.string.favour)));
                chooses.add(MyUtils.s2b(getString(R.string.against)));
                chooses.add(MyUtils.s2b(getString(R.string.waiver)));
                List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> votes = new ArrayList<>();
                if (!importVoteBeen.isEmpty()) {
                    for (int i = 0; i < importVoteBeen.size(); i++) {
                        ImportVoteBean bean = importVoteBeen.get(i);
                        LogUtil.e(TAG, "VoteFragment.onActivityResult :  --> 内容= " + bean.getContent() + "，模式= " + bean.getMode());
                        InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder();
                        builder.setContent(MyUtils.s2b(bean.getContent()));
                        builder.setMaintype(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote.getNumber());
                        builder.setMode(bean.getMode());
                        builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE.getNumber());
                        builder.setTimeouts(0);
                        builder.setSelectcount(3);
                        builder.addAllText(chooses);
                        InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = builder.build();
                        votes.add(build);
                    }
                }
                jni.createVote(votes);
            }
        }
    }

    private void showDialog(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote, int time) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(getString(R.string.whether_modify_timeout));
        dialog.setNegativeButton(getString(R.string.cancel), (dialog1, which) -> dialog1.dismiss());
        dialog.setPositiveButton(getString(R.string.ensure), (dialog1, which) -> {
            List<ByteString> chooses = new ArrayList<ByteString>();
            chooses.add(MyUtils.s2b(getString(R.string.favour)));
            chooses.add(MyUtils.s2b(getString(R.string.against)));
            chooses.add(MyUtils.s2b(getString(R.string.waiver)));
            InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder();
            builder.setVoteid(vote.getVoteid());
            builder.setContent(MyUtils.s2b(vote.getContent().toStringUtf8()));
            builder.setMaintype(vote.getMaintype());
            builder.setMode(vote.getMode());
            builder.setType(vote.getType());
            builder.setTimeouts(vote.getTimeouts());
            builder.setSelectcount(chooses.size());
            builder.addAllText(chooses);
            InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = builder.build();
            jni.modifyVote(build);
        });
        dialog.create().show();
    }

    public static class ViewHolder {
        public View rootView;
        public RecyclerView rv;
        public EditText modif_edt;
        public ImageButton imag_btn;
        public Spinner vote_pop_spinner;
        public Button pop_add_btn;
        public Button pop_modif_btn;
        public Button pop_del_btn;
        public Button export_excel;
        public Button import_excel;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.rv = (RecyclerView) rootView.findViewById(R.id.rv);
            this.modif_edt = (EditText) rootView.findViewById(R.id.modif_edt);
            this.imag_btn = (ImageButton) rootView.findViewById(R.id.imag_btn);
            this.vote_pop_spinner = (Spinner) rootView.findViewById(R.id.vote_pop_spinner);
            this.pop_add_btn = (Button) rootView.findViewById(R.id.pop_add_btn);
            this.pop_modif_btn = (Button) rootView.findViewById(R.id.pop_modif_btn);
            this.pop_del_btn = (Button) rootView.findViewById(R.id.pop_del_btn);
            this.export_excel = (Button) rootView.findViewById(R.id.export_excel);
            this.import_excel = (Button) rootView.findViewById(R.id.import_excel);
        }
    }

    public static class ChartViewHolder {
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
        public ImageView vote_option_color_a;
        public ImageView vote_option_color_b;
        public ImageView vote_option_color_c;
        public ImageView vote_option_color_d;
        public ImageView vote_option_color_e;
        public LinearLayout vote_type_top_ll;
        public TextView vote_member_count_tv;

        public ChartViewHolder(View rootView) {
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
            this.vote_option_color_a = (ImageView) rootView.findViewById(R.id.vote_option_color_a);
            this.vote_option_color_b = (ImageView) rootView.findViewById(R.id.vote_option_color_b);
            this.vote_option_color_c = (ImageView) rootView.findViewById(R.id.vote_option_color_c);
            this.vote_option_color_d = (ImageView) rootView.findViewById(R.id.vote_option_color_d);
            this.vote_option_color_e = (ImageView) rootView.findViewById(R.id.vote_option_color_e);
            this.vote_type_top_ll = (LinearLayout) rootView.findViewById(R.id.vote_type_top_ll);
            this.vote_member_count_tv = (TextView) rootView.findViewById(R.id.vote_member_count_tv);
        }

    }

    public static class ChooseViewHolder {
        public View rootView;
        public CheckBox all_number_cb;
        public LinearLayout top_view;
        public RecyclerView choose_rv;
        public Button ensure;
        public Button cancel;

        public ChooseViewHolder(View rootView) {
            this.rootView = rootView;
            this.all_number_cb = (CheckBox) rootView.findViewById(R.id.all_number_cb);
            this.top_view = (LinearLayout) rootView.findViewById(R.id.top_view);
            this.choose_rv = (RecyclerView) rootView.findViewById(R.id.choose_rv);
            this.ensure = (Button) rootView.findViewById(R.id.ensure);
            this.cancel = (Button) rootView.findViewById(R.id.cancel);
        }

    }
}
