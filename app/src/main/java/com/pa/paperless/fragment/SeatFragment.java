package com.pa.paperless.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceSignin;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.ui.CustomAbsoluteLayout;
import com.pa.paperless.ui.CustomConstraintLayout;
import com.pa.paperless.utils.DateUtil;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.MyUtils;
import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xlk on 2018/10/22.
 */

public class SeatFragment extends BaseFragment implements View.OnClickListener {

    private final String TAG = "SeatFragment-->";
    private LinearLayout seat_root_ll;
    private CustomAbsoluteLayout absolute;
    private CustomConstraintLayout seat_view;
    private int width = 1300, height = 760, viewWidth, viewHeight;
    private TextView count_tv, signed_in_tv, not_sign_in_tv;
    Button view_details;
    private NativeUtil nativeUtil = NativeUtil.getInstance();
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos = new ArrayList<>();
    private List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatInfos = new ArrayList<>();
    private SeatHandler handler;
    private Timer timer;
    private TimerTask task;
    private List<Integer> filterMembers = new ArrayList<>();

    public static class SeatHandler extends Handler {

        private final WeakReference<SeatFragment> fm;

        public SeatHandler(SeatFragment fragment) {
            fm = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                fm.get().fun_placeDeviceRankingInfo(Values.roomId);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.seat_layout, container, false);
        handler = new SeatHandler(this);
        seat_root_ll = inflate.findViewById(R.id.seat_root_ll);
        seat_view = inflate.findViewById(R.id.seat_view);
        absolute = inflate.findViewById(R.id.seat_layout);
        count_tv = inflate.findViewById(R.id.count_tv);
        signed_in_tv = inflate.findViewById(R.id.signed_in_tv);
        not_sign_in_tv = inflate.findViewById(R.id.not_sign_in_tv);
        view_details = inflate.findViewById(R.id.view_details);
        view_details.setOnClickListener(this);
        seat_root_ll.post(() -> {
            viewWidth = seat_root_ll.getWidth();
            viewHeight = seat_root_ll.getHeight();
            absolute.setScreen(viewWidth, viewHeight);
            queryMeetRoombg();
        });
        return inflate;
    }

    private void queryMeetRoombg() {
        try {
            int roombgpicid = nativeUtil.queryMeetRoomProperty(Values.roomId);
            if (roombgpicid != 0) {
                FileUtil.createDir(Macro.ROOT);
                nativeUtil.creationFileDownload(Macro.ROOT + Macro.DOWNLOAD_ROOM_BG + ".png", roombgpicid, 1, 0, Macro.DOWNLOAD_ROOM_BG);
            } else {
                fun_placeDeviceRankingInfo(Values.roomId);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo o = nativeUtil.queryAttendPeople();
            memberInfos.clear();
            if (o == null) return;
            List<InterfaceMember.pbui_Item_MemberDetailInfo> itemList = o.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                if (!filterMembers.contains((itemList.get(i).getPersonid()))) {
                    memberInfos.add(itemList.get(i));
                }
            }
            fun_querySign();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_querySign() {
        try {
            InterfaceSignin.pbui_Type_MeetSignInDetailInfo object1 = nativeUtil.querySign();
            if (object1 == null) return;
            if (memberInfos.isEmpty()) return;
            List<InterfaceSignin.pbui_Item_MeetSignInDetailInfo> itemList = object1.getItemList();
            int isSignIn = 0;
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceSignin.pbui_Item_MeetSignInDetailInfo item = itemList.get(i);
                int nameId = item.getNameId();
                if (filterMembers.contains(nameId)) continue;
                int signinType = item.getSigninType();
                long utcseconds = item.getUtcseconds();
                String[] gtmDate = DateUtil.getDate(utcseconds * 1000);
                String dateTime = gtmDate[0] + "  " + gtmDate[2];
                for (int j = 0; j < memberInfos.size(); j++) {
                    InterfaceMember.pbui_Item_MemberDetailInfo bean = memberInfos.get(j);
                    if (bean.getPersonid() == nameId) {
                        if (!TextUtils.isEmpty(dateTime)) {
                            isSignIn++;
                        }
                    }
                }
            }
            count_tv.setText(getString(R.string.should_be_to_count, memberInfos.size() + ""));
            signed_in_tv.setText(getString(R.string.already_signed_in_count, isSignIn + ""));
            not_sign_in_tv.setText(getString(R.string.no_sign_in_count, memberInfos.size() - isSignIn + ""));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void executeLater() {
        if (handler == null) return;
        //解决短时间内收到很多通知，查询很多次的问题
        if (timer == null) {
            timer = new Timer();
            LogUtil.i(TAG, "创建timer");
            task = new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(0);
                    task.cancel();
                    timer.cancel();
                    task = null;
                    timer = null;
                }
            };
            LogUtil.i(TAG, "500毫秒之后查询");
            timer.schedule(task, 500);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.SIGN_CHANGE_INFORM://签到变更通知
                fun_querySign();
                break;
            case EventType.SIGNIN_SEAT_INFORM://会场设备信息变更通知
                InterfaceBase.pbui_MeetNotifyMsgForDouble object1 = (InterfaceBase.pbui_MeetNotifyMsgForDouble) message.getObject();
                int opermethod1 = object1.getOpermethod();
                int id1 = object1.getId();
                int subid = object1.getSubid();
                LogUtil.i(TAG, "getEventMessage 会场设备信息变更通知 opermethod1= " + opermethod1 + ", id= " + id1 + ", subid= " + subid);
                executeLater();
                break;
            case EventType.PLACEINFO_CHANGE_INFORM://会场信息变更通知
                InterfaceBase.pbui_MeetNotifyMsg object = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
                int opermethod = object.getOpermethod();
                int id = object.getId();
                LogUtil.i(TAG, "会场信息变更通知 opermethod=" + opermethod + ",id=" + id);
                queryMeetRoombg();
                break;
            case EventType.ROOM_BG_PIC_ID:
                String filepath = (String) message.getObject();
                Drawable drawable = Drawable.createFromPath(filepath);
//                seat_view.setBackground(drawable);
                absolute.setBackground(drawable);
                Bitmap bitmap = BitmapFactory.decodeFile(filepath);
                if (bitmap != null) {
                    width = bitmap.getWidth();
                    height = bitmap.getHeight();
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
//                    seat_view.setLayoutParams(params);
                    absolute.setLayoutParams(params);
                    LogUtil.e(TAG, "updateBg 图片宽高 -->" + width + ", " + height);
                    fun_placeDeviceRankingInfo(Values.roomId);
                    bitmap.recycle();
                }
                break;
        }
    }

    private void fun_placeDeviceRankingInfo(int roomId) {
        LogUtil.i(TAG, "fun_placeDeviceRankingInfo ");
        InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo info = nativeUtil.placeDeviceRankingInfo(roomId);
        if (info == null) return;
        seatInfos.clear();
        seatInfos.addAll(info.getItemList());
        absolute.removeAllViews();
        filterMembers.clear();
        for (InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item : seatInfos) {
            if (item.getRole() == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE) {
                filterMembers.add(item.getMemberid());
            }
            addSeat(item);
        }
        fun_queryAttendPeople();
    }

    private void addSeat(InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.seat_item_layout, null);
//        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(30, 30);
        RelativeLayout.LayoutParams seatLinearParams = new RelativeLayout.LayoutParams(120, 40);
        ImageView iv = inflate.findViewById(R.id.seat_iv);
        LinearLayout seat_ll = inflate.findViewById(R.id.seat_item_ll);
        TextView seat_dev_tv = inflate.findViewById(R.id.seat_dev_tv);
        TextView seat_member_tv = inflate.findViewById(R.id.seat_member_tv);
        int issignin = item.getIssignin();
        switch (item.getDirection()) {
            case 1://朝下 (文本控件在下)
                if (issignin == 1) {
                    iv.setImageResource(R.drawable.seat_t_bottom);
                } else {
                    iv.setImageResource(R.drawable.seat_f_bottom);
                }
                seatLinearParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                ivParams.addRule(RelativeLayout.BELOW, seat_ll.getId());
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case 0://朝上
                if (issignin == 1) {
                    iv.setImageResource(R.drawable.seat_t_top);
                } else {
                    iv.setImageResource(R.drawable.seat_f_top);
                }
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                seatLinearParams.addRule(RelativeLayout.BELOW, iv.getId());
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case 3://朝右
                if (issignin == 1) {
                    iv.setImageResource(R.drawable.seat_t_right);
                } else {
                    iv.setImageResource(R.drawable.seat_f_right);
                }
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                seatLinearParams.addRule(RelativeLayout.BELOW, iv.getId());
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case 2://朝左
                if (issignin == 1) {
                    iv.setImageResource(R.drawable.seat_t_left);
                } else {
                    iv.setImageResource(R.drawable.seat_f_left);
                }
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                seatLinearParams.addRule(RelativeLayout.BELOW, iv.getId());
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
        }
        String devName = MyUtils.b2s(item.getDevname());
        String memberName = MyUtils.b2s(item.getMembername());

        if (!TextUtils.isEmpty(devName)) seat_dev_tv.setText(devName);
        else seat_dev_tv.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(memberName)) {
            seat_member_tv.setText(memberName);
            seat_member_tv.setTextColor((issignin == 1) ? Color.GREEN : Color.BLACK);
        } else {
            seat_member_tv.setVisibility(View.GONE);
        }

        iv.setLayoutParams(ivParams);
        seat_ll.setLayoutParams(seatLinearParams);
        float x1 = item.getX();
        float y1 = item.getY();
        if (x1 > 1) x1 = 1;
        else if (x1 < 0) x1 = 0;

        if (y1 > 1) y1 = 1;
        else if (y1 < 0) y1 = 0;
        int x = (int) (x1 * width);
        int y = (int) (y1 * height);
        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                120, 70,
                x, y);
        inflate.setLayoutParams(params);
        absolute.addView(inflate);
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
        if (!hidden) {
            if (handler == null) handler = new SeatHandler(this);
            queryMeetRoombg();
//            fun_placeDeviceRankingInfo(NativeService.roomId);
        } else {
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
                handler = null;
            }
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_details:
                EventBus.getDefault().post(new EventMessage(EventType.SIGNIN_DETAILS));
                break;
        }
    }
}
