package com.pa.paperless.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.LogUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceVote;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.SurveyItemAdapter;
import com.pa.paperless.data.bean.SubmitVoteBean;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @author xlk
 * @date 2018/5/26
 * 问卷调查
 */

public class QuestionnaireFragment extends BaseFragment implements View.OnClickListener {
    private final String TAG = "QuestionnaireFragment-->";
    private Button mSurveyPreTopic;
    private Button mSurveySubitBtn;
    private Button mSurveyNexTopic;
    private RecyclerView mSurveyNumberLv;
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> mSurveyData;
    private SurveyItemAdapter adapter1;
    private TextView tv_number, tv_maintype, tv_title;
    private Button option_one, option_two, option_three, option_four, option_five;
    private List<Button> btns;
    private int mPosion;
    private InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo;
    Map<Integer, List<Integer>> saveSelcet = new HashMap<>();//用来保存用户在某个问卷中选中的选项

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.survey_layout_f, container, false);
        initView(inflate);
        initChooseBtns();
        EventBus.getDefault().register(this);
        queryVote();
        return inflate;
    }

    private void queryVote() {
        try {
            InterfaceVote.pbui_Type_MeetVoteDetailInfo pbui_type_meetVoteDetailInfo = jni.queryVote();
            if (pbui_type_meetVoteDetailInfo == null) {
                clear();
                return;
            }
            List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> itemList = pbui_type_meetVoteDetailInfo.getItemList();
            if (mSurveyData == null) mSurveyData = new ArrayList<>();
            else mSurveyData.clear();
            for (InterfaceVote.pbui_Item_MeetVoteDetailInfo detailInfo : itemList) {
                if (detailInfo.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_questionnaire.getNumber()) {
                    mSurveyData.add(detailInfo);
                }
            }
            if (adapter1 == null) {
                LogUtil.d(TAG, "reveiveVote: 设置adapter");
                adapter1 = new SurveyItemAdapter(getContext(), mSurveyData);
                adapter1.setHasStableIds(true);
                mSurveyNumberLv.setHasFixedSize(true);
                mSurveyNumberLv.setLayoutManager(new StaggeredGridLayoutManager(10, StaggeredGridLayoutManager.VERTICAL));
                mSurveyNumberLv.setAdapter(adapter1);
            }
            if (!mSurveyData.isEmpty()) {
                updataTopic(mPosion);//首次默认是0
                adapter1.setItemClick((view, posion) -> {
                    mPosion = posion;
                    updataTopic(posion);
                });
            } else {
                clear();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryVote() {
        try {
            //200.查询投票
            InterfaceVote.pbui_Type_MeetVoteDetailInfo object = jni.queryVoteByType(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_questionnaire.getNumber());
            if (object == null) {
                clear();
                return;
            }
            List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfos = object.getItemList();
            if (!voteInfos.isEmpty()) {
                if (mSurveyData == null) mSurveyData = new ArrayList<>();
                else mSurveyData.clear();
                for (int i = 0; i < voteInfos.size(); i++) {
                    LogUtil.e(TAG, "QuestionnaireFragment.reveiveVote :" + voteInfos.get(i).getContent() + " 的所选项= " + voteInfos.get(i).getSelcnt());
                    mSurveyData.add(voteInfos.get(i));
                }
                if (adapter1 == null) {
                    LogUtil.d(TAG, "reveiveVote: 设置adapter");
                    adapter1 = new SurveyItemAdapter(getContext(), mSurveyData);
                    adapter1.setHasStableIds(true);
                    mSurveyNumberLv.setLayoutManager(new StaggeredGridLayoutManager(10, StaggeredGridLayoutManager.VERTICAL));
                    mSurveyNumberLv.setAdapter(adapter1);
                }
                if (mSurveyData.size() > 0) {
                    updataTopic(mPosion);//首次默认是0
                    adapter1.setItemClick((view, posion) -> {
                        mPosion = posion;
                        updataTopic(posion);
                    });
                }
            } else clear();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            //投票变更通知
            case EventType.Vote_Change_Inform:
                queryVote();
                break;
            default:
                break;
        }
    }


    private void updataTopic(int posion) {
        setBtnSel(posion);
        adapter1.setSelcectPosition(posion);
        voteInfo = mSurveyData.get(posion);
        tv_number.setText(getString(R.string.number_question, posion + 1 + ""));
        int type = voteInfo.getType();
        String isRemember = voteInfo.getMode() == InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_signed.getNumber() ? getString(R.string.remember_name) : getString(R.string.no_remember_name);
        switch (type) {
            case 0://多选
                tv_maintype.setText(getResources().getString(R.string.multiple_choice, isRemember));
                setMaxSelect(5, posion);
                break;
            case 1://单选
                tv_maintype.setText(getResources().getString(R.string.the_radio, isRemember));
                setMaxSelect(1, posion);
                break;
            case 2://多选 5选4
                tv_maintype.setText(getResources().getString(R.string.five_select_four, isRemember));
                setMaxSelect(4, posion);
                break;
            case 3://多选 5选3
                tv_maintype.setText(getResources().getString(R.string.five_select_three, isRemember));
                setMaxSelect(3, posion);
                break;
            case 4://多选 5选2
                tv_maintype.setText(getResources().getString(R.string.five_select_two, isRemember));
                setMaxSelect(2, posion);
                break;
            case 5://多选 3选2
                tv_maintype.setText(getResources().getString(R.string.three_select_two, isRemember));
                setMaxSelect(2, posion);
                break;
        }
        tv_title.setText(voteInfo.getContent().toStringUtf8());
        List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = voteInfo.getItemList();
        option_one.setText("");
        option_two.setText("");
        option_three.setText("");
        option_four.setText("");
        option_five.setText("");
        option_one.setClickable(false);
        option_two.setClickable(false);
        option_three.setClickable(false);
        option_four.setClickable(false);
        option_five.setClickable(false);
        int size = optionInfo.size();
        for (int i = 0; i < size; i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(i);
            String text = voteOptionsInfo.getText().toStringUtf8();//文本
            boolean content = !text.trim().equals("");//文本不为空
            int selcnt = voteOptionsInfo.getSelcnt();//已经选择的个数
            if (size > 0 && content) {
                if (i == 0) {
                    option_one.setText("A. " + text);
                    option_one.setClickable(true);
                } else if (i == 1) {
                    option_two.setText("B. " + text);
                    option_two.setClickable(true);
                } else if (i == 2) {
                    option_three.setText("C. " + text);
                    option_three.setClickable(true);
                } else if (i == 3) {
                    option_four.setText("D. " + text);
                    option_four.setClickable(true);
                } else if (i == 4) {
                    option_five.setText("E. " + text);
                    option_five.setClickable(true);
                }
            }
        }
    }

    /**
     * 获取当前投票的选过的选项
     *
     * @param posion
     */
    private void setBtnSel(int posion) {
        option_one.setSelected(false);
        option_two.setSelected(false);
        option_three.setSelected(false);
        option_four.setSelected(false);
        option_five.setSelected(false);
        if (saveSelcet.containsKey(mSurveyData.get(posion).getVoteid())) {
            List<Integer> integers = saveSelcet.get(mSurveyData.get(posion).getVoteid());
            for (int i = 0; i < integers.size(); i++) {
                Integer integer = integers.get(i);
                if (integer == 0) option_one.setSelected(true);
                else if (integer == 1) option_two.setSelected(true);
                else if (integer == 2) option_three.setSelected(true);
                else if (integer == 3) option_four.setSelected(true);
                else if (integer == 4) option_five.setSelected(true);
            }
        }
    }

    private void clear() {
        if (mSurveyData != null && adapter1 != null) {
            mSurveyData.clear();
            adapter1.notifyDataSetChanged();
        }
        tv_number.setText("");
        tv_maintype.setText("");
        tv_title.setText("");
        option_one.setText("");
        option_two.setText("");
        option_three.setText("");
        option_four.setText("");
        option_five.setText("");
    }

    private void initView(View inflate) {
        mSurveyPreTopic = inflate.findViewById(R.id.survey_pre_topic);
        mSurveySubitBtn = inflate.findViewById(R.id.survey_subit_btn);
        mSurveyNexTopic = inflate.findViewById(R.id.survey_nex_topic);
        mSurveyNumberLv = inflate.findViewById(R.id.survey_number_lv);

        tv_number = inflate.findViewById(R.id.tv_number);
        tv_maintype = inflate.findViewById(R.id.tv_maintype);
        tv_title = inflate.findViewById(R.id.tv_title);
        option_one = inflate.findViewById(R.id.option_one);
        option_two = inflate.findViewById(R.id.option_two);
        option_three = inflate.findViewById(R.id.option_three);
        option_four = inflate.findViewById(R.id.option_four);
        option_five = inflate.findViewById(R.id.option_five);
        mSurveyPreTopic.setOnClickListener(this);
        mSurveySubitBtn.setOnClickListener(this);
        mSurveyNexTopic.setOnClickListener(this);
    }

    private void initChooseBtns() {
        btns = new ArrayList<>();
        btns.add(option_one);
        btns.add(option_two);
        btns.add(option_three);
        btns.add(option_four);
        btns.add(option_five);
    }

    private void setMaxSelect(int max, int posion) {
        for (int i = 0; i < btns.size(); i++) {
            int finalI = i;
            btns.get(finalI).setOnClickListener(v -> selectMax(btns, max, finalI, posion));
        }
    }

    /**
     * 设置按钮选中状态
     *
     * @param btns      按钮集合
     * @param maxSelect 最大可以选中的个数
     * @param index     当前集合中的索引
     */
    private void selectMax(final List<Button> btns, final int maxSelect, int index, int posion) {
        InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = mSurveyData.get(posion);
        int voteid = voteInfo.getVoteid();
        if (maxSelect == 1) {
            chooseOne(btns, index, posion);
        } else {
            if (btns.get(index).isSelected()) {//点击了已经是选中了的，不管最大值直接设置其未选中
                btns.get(index).setSelected(false);
                if (saveSelcet.containsKey(voteid)) {//map集合中存在当前投票ID的key
                    List<Integer> integers = saveSelcet.get(voteid);//找到key对应的list集合
                    integers.remove(integers.indexOf(index));//删除当前选中索引在integers中的索引位置
                    saveSelcet.put(voteid, integers);//再进行覆盖该key下的value
                }
            } else {//点击了没有选中的，再判断是否还可以选中
                if (maxSelect > getNowSelect(btns)) {//还可以选择
                    btns.get(index).setSelected(true);
                    if (saveSelcet.containsKey(voteid)) {//map集合中存在当前投票ID的key
                        List<Integer> integers = saveSelcet.get(voteid);//找到key对应的list集合
                        integers.add(index);//往integers中添加值
                        saveSelcet.put(voteid, integers);//再进行覆盖该key下的value
                    } else {//添加一对新的key value
                        List<Integer> sels = new ArrayList<>();
                        sels.add(index);
                        saveSelcet.put(voteid, sels);
                    }
                } else if (maxSelect == getNowSelect(btns)) {//已经是等于最大选中个数了，则设置其未选中
                    ToastUtil.showToast(R.string.tip_most_can_choose, maxSelect + "");
                    btns.get(index).setSelected(false);
                }
            }
        }
    }

    /**
     * 获取传入的Button集合中已经选中的button个数
     *
     * @param btns
     * @return
     */
    private int getNowSelect(List<Button> btns) {
        int nowSelect = 0;
        for (int i = 0; i < btns.size(); i++) {
            if (btns.get(i).isSelected()) {
                nowSelect++;
            }
        }
        return nowSelect;
    }

    /**
     * 单选投票
     *
     * @param btns
     * @param index
     */
    private void chooseOne(List<Button> btns, int index, int posion) {
        InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = mSurveyData.get(posion);
        int voteid = voteInfo.getVoteid();
        for (int i = 0; i < btns.size(); i++) {
            btns.get(i).setSelected(i == index);
            if (i == index) {
                List<Integer> ids = new ArrayList<>();
                ids.add(index);
                saveSelcet.put(voteid, ids);//因为是单选，所以直接覆盖;
            }
        }
    }

    /**
     * 获取当前选中的选项结果
     *
     * @return
     */
    private int getChoose() {
        int choose = 0;
        for (int i = 0; i < btns.size(); i++) {
            if (btns.get(i).isSelected()) {
                if (i == 0) choose += 1;//第0项被选中
                else if (i == 1) choose += 2;//第1项被选中
                else if (i == 2) choose += 4;//第2项被选中
                else if (i == 3) choose += 8;//第3项被选中
                else if (i == 4) choose += 16;//第4项被选中 10000 从右往左第五个  10000的十进制为 16
            }
        }
        return choose;
    }

    @Override
    public void onClick(View v) {
        if (mSurveyData == null || adapter1 == null) return;
        switch (v.getId()) {
            case R.id.survey_pre_topic://上一题
                inspect(0);
                break;
            case R.id.survey_nex_topic://下一题
                inspect(1);
                break;
            case R.id.survey_subit_btn://提交
                if (voteInfo == null) return;
                if (voteInfo.getVotestate() == 1) {//确保是发起状态
                    if (voteInfo.getSelcnt() == 0) subit();
                    else
                        ToastUtil.showToast(R.string.tip_you_have_submitted);
                } else
                    ToastUtil.showToast(R.string.tip_vote_must_be_initiated_state);
                break;
        }
    }

    //提交投票
    private void subit() {
        LogUtil.d(TAG, "subit: 提交投票..");
        int choose = getChoose();
        if (choose > 0) {
            SubmitVoteBean submitVoteBean = new SubmitVoteBean(voteInfo.getVoteid(), voteInfo.getSelectcount(), choose);
            /** ************ ******  196.提交投票结果  ****** ************ **/
            jni.submitVoteResult(submitVoteBean);
            adapter1.notifyDataSetChanged();
        }
    }

    private void inspect(int i) {
        if (i == 0) {//上一题
            if (mPosion > 0) {//当前索引必须大于最小索引
                mPosion--;
                updataTopic(mPosion);
            }
        } else {//下一题
            if (mPosion < mSurveyData.size() - 1) {//当前索引必须小于最大索引
                mPosion++;
                updataTopic(mPosion);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            queryVote();
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
