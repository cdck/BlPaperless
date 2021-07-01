package com.pa.paperless.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.utils.LogUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceSignin;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.SignInLvAdapter;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.bean.SignInBean;
import com.pa.paperless.utils.ConvertUtil;
import com.pa.paperless.utils.DateUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/11/1.
 * 签到状态Fragment
 */
 public class SignInFragment extends BaseFragment implements View.OnClickListener {
    private final String TAG = "SigninFragment-->";
    private ListView mSigninLv;
    private List<SignInBean> mDatas = new ArrayList<>();
    private SignInLvAdapter signinLvAdapter;
    private TextView count_tv, signed_in_tv, not_sign_in_tv;
    private Button back_btn;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.right_signin, container, false);
        initView(inflate);
        /** **** **  将签到信息先缓存下来  ** **** **/
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN.getNumber(), 1, 0);
        fun_placeDeviceRankingInfo(Values.roomId);
        return inflate;
    }

    private void fun_placeDeviceRankingInfo(int roomId) {
        LogUtil.i(TAG, "fun_placeDeviceRankingInfo ");
        InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo info = jni.placeDeviceRankingInfo(roomId);
        if (info == null) return;
        fun_queryAttendPeople();
    }

    private void fun_queryAttendPeople() {
        try {
            /** **** **  查询参会人员  ** **** **/
            InterfaceMember.pbui_Type_MemberDetailInfo o = jni.queryAttendPeople();
            if (o == null) {
                clean();
                return;
            }
            memberInfos.clear();
            memberInfos.addAll(o.getItemList());
            if (!memberInfos.isEmpty()) {
                mDatas.clear();
                /** ************ ******  206.查询签到信息  ****** ************ **/
                for (int i = 0; i < memberInfos.size(); i++) {
                    mDatas.add(new SignInBean(memberInfos.get(i).getPersonid(), String.valueOf(mDatas.size() + 1), memberInfos.get(i).getName().toStringUtf8(), "", 0));
                }
                if (!fun_querySign()) {
                    int isSignIn = 0;
                    signed_in_tv.setText(getString(R.string.already_signed_in_count, isSignIn + ""));
                    not_sign_in_tv.setText(getString(R.string.no_sign_in_count, mDatas.size() - isSignIn + ""));
                    count_tv.setText(getString(R.string.should_be_to_count, mDatas.size() + ""));
                    initAdapter();
                }
            } else {
                clean();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private boolean fun_querySign() {
        try {
            InterfaceSignin.pbui_Type_MeetSignInDetailInfo object1 = jni.querySign();
            if (object1 == null) {
                return false;
            }
            List<InterfaceSignin.pbui_Item_MeetSignInDetailInfo> itemList = object1.getItemList();
            int isSignIn = 0;
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceSignin.pbui_Item_MeetSignInDetailInfo item = itemList.get(i);
                int nameId = item.getNameId();
                int signinType = item.getSigninType();
                long utcseconds = item.getUtcseconds();
                int type = item.getSigninType();
                String dateTime = DateUtil.getSignInTime(utcseconds);
                LogUtil.e(TAG, "nameId=" + nameId + ",时间=" + dateTime);
                ByteString psigndata = item.getPsigndata();
                for (int j = 0; j < mDatas.size(); j++) {
                    SignInBean bean = mDatas.get(j);
                    if (bean.getId() == nameId) {
                        if (type == InterfaceMacro.Pb_MeetSignType.Pb_signin_photo.getNumber()) {
                            bean.setPic_data(psigndata);
                        }
                        bean.setSignin_date(dateTime);
                        bean.setSign_in(signinType);
                        if (!TextUtils.isEmpty(dateTime)) {
                            isSignIn++;
                        }
                    }
                }
            }
            signed_in_tv.setText(getString(R.string.already_signed_in_count, isSignIn + ""));
            not_sign_in_tv.setText(getString(R.string.no_sign_in_count, mDatas.size() - isSignIn + ""));
            count_tv.setText(getString(R.string.should_be_to_count, mDatas.size() + ""));
            initAdapter();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void initAdapter() {
        if (signinLvAdapter == null) {
            signinLvAdapter = new SignInLvAdapter(getActivity(), mDatas);
            mSigninLv.setAdapter(signinLvAdapter);
            signinLvAdapter.setListener((posion, picdata) -> {
                LogUtil.e(TAG, "SignInFragment.fun_querySign :  点击的索引 --> " + posion);
                showPicPop(picdata);
            });
            mSigninLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    signinLvAdapter.setSelected(mDatas.get(position).getId());
                }
            });
        } else {
            signinLvAdapter.notifyDataSetChanged();
        }
    }

    private void showPicPop(ByteString picdata) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pic_pop, null);
        ImageView iv = inflate.findViewById(R.id.show_pic_iv);
        iv.setImageBitmap(ConvertUtil.bs2bmp(picdata));
        builder.setView(inflate);
        builder.create().show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.SIGN_CHANGE_INFORM://签到变更通知
                fun_queryAttendPeople();
                break;
            case EventType.SIGNIN_SEAT_INFORM://会场设备信息变更通知
                fun_placeDeviceRankingInfo(Values.roomId);
                break;
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
        mSigninLv = inflate.findViewById(R.id.signin_lv);
        back_btn = inflate.findViewById(R.id.back_btn);
        count_tv = inflate.findViewById(R.id.count_tv);
        signed_in_tv = inflate.findViewById(R.id.signed_in_tv);
        not_sign_in_tv = inflate.findViewById(R.id.not_sign_in_tv);
        back_btn.setOnClickListener(this);
        inflate.findViewById(R.id.btn_delete).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                EventBus.getDefault().post(new EventMessage(EventType.SIGNIN_SEAT_FRAG));
                break;
            case R.id.btn_delete:
                if (signinLvAdapter != null) {
                    List<Integer> selectedIds = signinLvAdapter.getSelectedIds();
                    if (selectedIds.isEmpty()) {
                        ToastUtils.showShort(R.string.please_choose_member);
                        return;
                    }
                    jni.deleteSign(selectedIds);
                }
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtil.e(TAG, "SignInFragment.onHiddenChanged :  hidden --> " + hidden);
        if (!hidden) {
//            fun_queryAttendPeople();
            fun_placeDeviceRankingInfo(Values.roomId);
        }
    }

    private void clean() {
        if (mDatas != null) mDatas.clear();
        if (signinLvAdapter != null) signinLvAdapter.notifyDataSetChanged();
    }
}