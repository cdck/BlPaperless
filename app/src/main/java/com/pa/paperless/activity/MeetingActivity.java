package com.pa.paperless.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.pa.paperless.broadcase.NetWorkReceiver;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.fragment.ElectionManageFragment;
import com.pa.paperless.fragment.SigninFragment;
import com.pa.paperless.service.App;
import com.pa.paperless.utils.LogUtil;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.zxing.WriterException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceWhiteboard;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.FunctionAdapter;
import com.pa.paperless.adapter.rvadapter.SecretaryAdapter;
import com.pa.paperless.data.bean.ReceiveMeetIMInfo;
import com.pa.paperless.data.constant.BroadCaseAction;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.fragment.AgendaFragment;
import com.pa.paperless.fragment.ChatFragment;
import com.pa.paperless.fragment.DeviceControlFragment;
import com.pa.paperless.fragment.MeetingFileFragment;
import com.pa.paperless.fragment.PermissionManageFragment;
import com.pa.paperless.fragment.PostilFragment;
import com.pa.paperless.fragment.CameraFragment;
import com.pa.paperless.fragment.NoticeFragment;
import com.pa.paperless.fragment.ScreenManageFragment;
import com.pa.paperless.fragment.SeatFragment;
import com.pa.paperless.fragment.SharedFileFragment;
import com.pa.paperless.fragment.VideoFragment;
import com.pa.paperless.fragment.VoteFragment;
import com.pa.paperless.fragment.WebBrowseFragment;
import com.pa.paperless.fragment.QuestionnaireFragment;
import com.pa.paperless.utils.DateUtil;
import com.pa.paperless.utils.Export;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.NetworkUtil;
import com.pa.paperless.utils.PopUtils;
import com.pa.paperless.utils.QRCodeUtil;

import com.pa.paperless.ui.DrawBoard;
import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import cc.shinichi.library.ImagePreview;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.pa.paperless.data.constant.Macro.CACHE_FILE;
import static com.pa.paperless.data.constant.Macro.PB_MEET_FUN_CODE_MESSAGE;
import static com.pa.paperless.data.constant.Values.mPermissionsList;
import static com.pa.paperless.fragment.CameraFragment.isOPenCamera;
import static com.pa.paperless.service.App.isDebug;
import static com.pa.paperless.service.App.lbm;
import static com.pa.paperless.ui.DrawBoard.pathList;
import static com.pa.paperless.activity.DrawBoardActivity.savePicData;
import static com.pa.paperless.activity.DrawBoardActivity.tempPicData;
import static com.pa.paperless.activity.DrawBoardActivity.togetherIDs;
import static com.pa.paperless.fragment.WebBrowseFragment.webView_isshowing;
import static com.wind.myapplication.CameraDemo.isbusy;

/**
 * @author xlk
 */
public class MeetingActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "MeetingActivity-->";
    /**
     * 判断当前是否是ChatFragment
     */
    public static boolean chatisshowing = false;
    public static List<ReceiveMeetIMInfo> mReceiveMsg = new ArrayList<>();
    /**
     * 设备会议信息
     */
    private InterfaceDevice.pbui_Type_DeviceFaceShowDetail DevMeetInfo;
    private TextView member_with_role, company_name_tv, time_tv, date_tv, week_tv, /*compere_tv, */
            member, meet_title_tv, meet_tv_online;
    private ImageView qr_img, win_iv, close_iv;
    private RecyclerView meet_rl, secretary_rl;
    public static Badge mBadge;
    public static String mNoteCentent;
    private FragmentManager mFm;
    private List<Integer> funData = new ArrayList<>();
    private MeetingActivity context;
    private FunctionAdapter funAdapter;
    private SigninFragment mSigninFragment;
    private SeatFragment mSigninSeatFragment;
    private AgendaFragment mAnnAgendaFragment;
    private MeetingFileFragment mMeetingFileFragment;
    private SharedFileFragment mSharedFileFragment;
    private ChatFragment mChatFragment;
    private VideoFragment mVideoFragment;
    private VoteFragment mVoteFragment;
    private QuestionnaireFragment mSurveyFragment;
    private PostilFragment mNotationFragment;
    private WebBrowseFragment mWebbrowseFragment;
    private CameraFragment mOverViewDocFragment;
    private PermissionManageFragment mPermissionManageFragment;
    private boolean isRestart = false;
    /**
     * 存放跳转之前所展示的Fragment索引
     */
    public static int saveIndex = -1;
    private Button meet_btn, secretary;
    private ImageView logo_iv;
    private FrameLayout meet_fl;
    //    private RelativeLayout root_layout_id;
    private ConstraintLayout root_layout_id;
    /**
     * 生成的二维码图片
     */
    private Bitmap QRCode;
    private SecretaryAdapter secretaryAdapter;
    private DeviceControlFragment mDeviceControlFragment;
    private List<Integer> secretaryData;
    private ElectionManageFragment mSurveyManageFragment;
    private ScreenManageFragment mScreenManageFragment;
    private NoticeFragment mNoticeFragment;
    private StaggeredGridLayoutManager rvLayoutManager;
    private int chatIndex;
    private boolean isCanJump2Main = true;
    private NetWorkReceiver netWorkReceiver;
    private Handler handler = new Handler();
    private NativeUtil jni = NativeUtil.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //视频为了避免闪屏和透明问题，Activity在onCreate时需要设置:
//        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_meeting);
        context = this;
        initView();
        EventBus.getDefault().register(this);
        netWorkReceiver = new NetWorkReceiver();
        registerNetwork();
        FileUtil.createDir(CACHE_FILE);
        //获取到之前写得tempNote
        File file = new File(CACHE_FILE + "/tempNote.txt");
        if (file.exists() && file.isFile()) {
            mNoteCentent = Export.readText(file);
        }
        mFm = getSupportFragmentManager();
        try {
            jni.initVideoRes(0, App.screenWidth, App.screenHeight);
            jni.initVideoRes(10, App.screenWidth, App.screenHeight);
            jni.initVideoRes(11, App.screenWidth, App.screenHeight);
            //  修改本机界面状态
            jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_ROLE_VALUE,
                    InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MemFace_VALUE);
            cache();
            //首次进入界面就立马调用一次无意义的操作，避免第一次真正调用时无效
            jni.createFileCache(0, 0, 0, 0, "");
            updateOnline();
            fun_queryMeetFunction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cache() {
        // 缓存会场设备
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_FORCE_VALUE);
        //缓存会场设备
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);
        // 缓存会议排位
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);
        // 缓存参会人信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);
        //缓存会议目录
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);
        //会议目录文件
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);
        //会议目录权限
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYRIGHT_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);
        //缓存投票信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);
        //人员签到
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);
        //公告信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);

    }

    @Override
    protected void onRestart() {
        //Activity进行跳转之后回到本界面时重新设置
        LogUtil.d("A_life", "MeetingActivity.onRestart :   --->>> 重新定时查找会议功能");
        isRestart = true;
        /* **** **  回到MeetingActivity时重新设置默认点击  ** **** */
        //238.查询会议功能
        fun_queryMeetFunction();
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
//        myApp.openFab(true);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterNetwork();
        if (timer != null) {
            cancelTimer();
        }
        handler.removeCallbacksAndMessages(null);
        //关闭悬浮窗的服务
//        myApp.openFab(false);
        jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_ROLE_VALUE,
                InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MainFace_VALUE);
        jni.mediaDestroy(0);
        jni.mediaDestroy(10);
        jni.mediaDestroy(11);
        unreceiverUpdate();

        Intent intent = new Intent();
        intent.setAction(BroadCaseAction.STOP_SCREEN_SHOT);
        intent.putExtra("type", BroadCaseAction.SCREEN_SHOT_TYPE);
        lbm.sendBroadcast(intent);
        super.onDestroy();
    }

    private void registerNetwork() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkReceiver, filter);
    }

    private void unregisterNetwork() {
        unregisterReceiver(netWorkReceiver);
    }

    /**
     * 查询是否在线
     */
    private void updateOnline() {
        byte[] bytes = jni.queryDevicePropertiesById(InterfaceMacro.Pb_MeetDevicePropertyID.Pb_MEETDEVICE_PROPERTY_NETSTATUS_VALUE,
                0);
        boolean online = bytes == null;
        if (bytes != null) {
            try {
                InterfaceDevice.pbui_DeviceInt32uProperty info = InterfaceDevice.pbui_DeviceInt32uProperty.parseFrom(bytes);
                online = info.getPropertyval() == 1;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        LogUtils.e(TAG, "本机设备在线状态：" + (online ? "在线" : "离线"));
        meet_tv_online.setText(online ? R.string.online : R.string.Offline);
        if (!online) {
            ToastUtils.showShort(R.string.device_offline);
        } else {
            //  修改本机界面状态
            jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_ROLE_VALUE,
                    InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MemFace_VALUE);
        }
    }

    private void fun_queryMeetFunction() {
        try {
            InterfaceMeetfunction.pbui_Type_MeetFunConfigDetailInfo meetfun = jni.queryMeetFunction();
            funData.clear();
            if (meetfun != null) {
                List<InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo> itemList4 = meetfun.getItemList();
                for (InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo function : itemList4) {
                    funData.add(function.getFuncode());
                }
            }
            if (funAdapter == null) {
                funAdapter = new FunctionAdapter(context, funData);
                meet_rl.setAdapter(funAdapter);
            } else {
                funAdapter.notifyDataSetChanged();
            }
            for (int i = 0; i < funData.size(); i++) {
                if (funData.get(i) == 4) {
                    chatIndex = i;
                    break;
                }
            }
            /* **** **  item点击事件  ** **** */
            funAdapter.setItenListener((view, posion) -> {
                funAdapter.setCheckedId(posion);
                Integer integer = funData.get(posion);
                if (isOPenCamera) {
                    /* **** **  如果点击了画板则进行页面跳转  ** **** */
                    if (integer == Macro.PB_MEET_FUN_CODE_WHITE_BOARD) {
                        showFragment(integer);
                        startActivity(new Intent(context, DrawBoardActivity.class));
                    } else if (integer == Macro.PB_MEET_FUN_CODE_DOCUMENT) {
                        if (!isbusy) {
                            showFragment(integer);
                        } else {
                            ToastUtils.showShort(R.string.in_the_screen_recording);
                        }
                    } else {
                        showFragment(integer);
                    }
                }
            });
            try {
                InterfaceBase.pbui_CommonInt32uProperty property = jni.queryMeetRankingProperty(InterfaceMacro.Pb_MeetSeatPropertyID.Pb_MEETSEAT_PROPERTY_ROLEBYMEMBERID_VALUE);
                if (property != null) {
                    int propertyval = property.getPropertyval();
                    LogUtil.d(TAG, "receiveFunInfo: 本机的角色= " + propertyval);
                    if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE
                            || propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE
                            || propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin_VALUE) {
                        setMemberRoleTv(propertyval);
                        Values.hasAllPermissions = true;
                        secretary.setVisibility(View.VISIBLE);
                    } else {
                        Values.hasAllPermissions = false;
                        secretary.setVisibility(View.GONE);
                    }
                }
                setDefaultClick();
                fun_queryAttendPeople();
                fun_queryInterFaceConfiguration();
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void setMemberRoleTv(int propertyval) {
        if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE) {
            member_with_role.setText(getString(R.string.member_host));
        } else if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE) {
            member_with_role.setText(getString(R.string.member_secretary));
        } else if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin_VALUE) {
            member_with_role.setText(getString(R.string.member_admin));
        } else {
            member_with_role.setText(getString(R.string.member_coord));
        }
    }

    /**
     * 设置默认点击的功能，如果有批注则点击批注，之后再设置点击第0项
     */
    private void setDefaultClick() {
        if (saveIndex > 11) {//当前选中的索引在秘书功能界面
            if (secretary.getVisibility() == View.VISIBLE) {//当前仍然有秘书功能权限
                showFragment(saveIndex);
                secretaryAdapter.setCheckedId(secretaryData.indexOf(saveIndex));
            } else {//虽然之前在秘书功能界面，但是现在可能没有了秘书功能权限，只能设置默认去点击第一项
                meet_btn.performClick();
                if (funData != null && !funData.isEmpty()) {
                    //如果当前首次加载功能菜单时默认第一项是画板，则设置默认点击第二项
                    saveIndex = funData.get(0);
                    if (saveIndex == Macro.PB_MEET_FUN_CODE_WHITE_BOARD && funData.size() > 1) {
                        saveIndex = funData.get(1);
                        funAdapter.setCheckedId(1);
                    } else {
                        funAdapter.setCheckedId(0);
                    }
                    showFragment(saveIndex);
                }
            }
        } else {//当前选中的索引在参会人功能界面
            if (funData != null && !funData.isEmpty()) {
                if (isRestart) {//返回时
                    isRestart = false;
                    if (funData.size() > saveIndex) {
                        showFragment(saveIndex);
                        funAdapter.setCheckedId(funData.indexOf(saveIndex));
                    } else {//回到会议界面时，数据发生改变则设置点击第一项，如果第一项是画板则设置点击第二项
                        saveIndex = funData.get(0);
                        if (saveIndex == Macro.PB_MEET_FUN_CODE_WHITE_BOARD && funData.size() > 1) {
                            saveIndex = funData.get(1);
                            funAdapter.setCheckedId(1);
                        } else {
                            funAdapter.setCheckedId(0);
                        }
                        showFragment(saveIndex);
                    }
                } else {
                    saveIndex = funData.get(0);
                    if (saveIndex == Macro.PB_MEET_FUN_CODE_WHITE_BOARD && funData.size() > 1) {
                        saveIndex = funData.get(1);
                        funAdapter.setCheckedId(1);
                    } else {
                        funAdapter.setCheckedId(0);
                    }
                    showFragment(saveIndex);
                }
            }
        }
    }

    private void initSecretaryData() {
        secretaryData = new ArrayList<>();
        for (int i = Macro.PB_MEET_FUN_CODE_DEV_CONTROL; i < Macro.PB_MEET_FUN_CODE_OPEN_BACKGROUND + 1; i++) {
            secretaryData.add(i);
        }
        secretaryAdapter = new SecretaryAdapter(this, secretaryData);
        secretary_rl.setAdapter(secretaryAdapter);
        secretaryAdapter.setItenListener((view, posion) -> {
            secretaryAdapter.setCheckedId(posion);
            showFragment(secretaryData.get(posion));
        });
    }

    private void fun_queryInterFaceConfiguration() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo faceConfigInfo = jni.queryInterFaceConfiguration();
            if (faceConfigInfo == null) {
                return;
            }
            List<InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo> onlytextList = faceConfigInfo.getOnlytextList();
            List<InterfaceFaceconfig.pbui_Item_FacePictureItemInfo> pictureList = faceConfigInfo.getPictureList();
            List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = faceConfigInfo.getTextList();
            boolean logoIsHide = false;
            for (int i = 0; i < pictureList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FacePictureItemInfo itemInfo = pictureList.get(i);
                int faceid = itemInfo.getFaceid();
                int flag = itemInfo.getFlag();
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_LOGO_VALUE) {
                    boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
                    if (isShow) {
                        logo_iv.setVisibility(View.VISIBLE);
                        FileUtil.createDir(Macro.ROOT);
                        jni.creationFileDownload(Macro.ROOT + Macro.DOWNLOAD_MAIN_LOGO + ".png", itemInfo.getMediaid(), 1, 0, Macro.DOWNLOAD_MAIN_LOGO);
                    } else {
                        logoIsHide = true;
                        logo_iv.setVisibility(View.GONE);
                    }
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SUBBG_VALUE) {//子界面背景图
                    LogUtil.d(TAG, "fun_queryInterFaceConfiguration --> 下载子界面背景图");
                    FileUtil.createDir(Macro.ROOT);
                    jni.creationFileDownload(Macro.ROOT + Macro.DOWNLOAD_SUB_BG + ".png", itemInfo.getMediaid(), 1, 0, Macro.DOWNLOAD_SUB_BG);
                }
            }
            for (int i = 0; i < onlytextList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo info = onlytextList.get(i);
                int faceid = info.getFaceid();
                int flag = info.getFlag();
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COLTDTEXT_VALUE) {
                    company_name_tv.setText(MyUtils.b2s(info.getText()));
                    break;
                }
            }
            for (int i = 0; i < textList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info = textList.get(i);
                int faceid = info.getFaceid();
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COMPANY_VALUE) {//公司名称
                    LogUtil.i(TAG, "单位名称控件");
                    updateCompanyNameTv(info);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_LOGO_GEO_VALUE) {//Logo图标,只需要更新位置坐标
                    LogUtil.i(TAG, "logo图标控件");
                    update(R.id.meet_logo_iv, info);
                }
            }
            if (logoIsHide) {
                moveCompanyNameTv2Left();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void moveCompanyNameTv2Left() {
        LogUtils.i(TAG, "moveCompanyNameTv2Left");
        ConstraintSet set = new ConstraintSet();
        set.clone(root_layout_id);
        set.setHorizontalBias(R.id.company_name_tv, 0f);
        set.setVerticalBias(R.id.company_name_tv, 0f);
        set.applyTo(root_layout_id);
    }

    private void update(int resid, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info) {
        float bx = info.getBx();
        float by = info.getBy();
        float lx = info.getLx();
        float ly = info.getLy();
        ConstraintSet set = new ConstraintSet();
        set.clone(root_layout_id);
        //设置控件的大小
        float width = (bx - lx) / 100 * App.screenWidth;
        float height = (by - ly) / 100 * App.screenHeight;
        set.constrainWidth(resid, (int) width);
        set.constrainHeight(resid, (int) height);
        LogUtil.d(TAG, "update: 控件大小 当前控件宽= " + width + ", 当前控件高= " + height);
        float biasX, biasY;
        float halfW = (bx - lx) / 2 + lx;
        float halfH = (by - ly) / 2 + ly;

        if (lx == 0) biasX = 0;
        else if (lx > 50) biasX = bx / 100;
        else biasX = halfW / 100;

        if (ly == 0) biasY = 0;
        else if (ly > 50) biasY = by / 100;
        else biasY = halfH / 100;
        LogUtil.e(TAG, "update: biasX= " + biasX + ",biasY= " + biasY);
        set.setHorizontalBias(resid, biasX);
        set.setVerticalBias(resid, biasY);
        set.applyTo(root_layout_id);
    }

    /**
     * 更新TextView信息
     */
    private void updateCompanyNameTv(InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info) {
        update(R.id.company_name_tv, info);
        TextView tv = findViewById(R.id.company_name_tv);
        tv.setTextColor(info.getColor());
        tv.setTextSize(info.getFontsize());
        boolean b = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (info.getFlag() & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        tv.setVisibility(b ? View.VISIBLE : View.GONE);
        int fontflag = info.getFontflag();
        //字体样式
        if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD_VALUE) {//加粗
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_LEAN_VALUE) {//倾斜
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_UNDERLINE_VALUE) {//下划线
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));//暂时用倾斜加粗
        } else {//正常文本
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
        int align = info.getAlign();
        //对齐方式
        if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT_VALUE) {//左对齐
            tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT_VALUE) {//右对齐
            tv.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER_VALUE) {//水平对齐
            tv.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP_VALUE) {//上对齐
            tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM_VALUE) {//下对齐
            tv.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER_VALUE) {//垂直对齐
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        } else {
            tv.setGravity(Gravity.CENTER);
        }
        String fontName = info.getFontname().toStringUtf8();
        //字体类型
        Typeface kt_typeface;
        if (!TextUtils.isEmpty(fontName)) {
            switch (fontName) {
                case "楷体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "kt.ttf");
                    break;
                case "宋体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "fs.ttf");
                    break;
                case "隶书":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "ls.ttf");
                    break;
                case "微软雅黑":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "wryh.ttf");
                    break;
                default:
                    kt_typeface = Typeface.createFromAsset(getAssets(), "fs.ttf");
                    break;
            }
            tv.setTypeface(kt_typeface);
        }
    }


    private void fun_queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo o = jni.queryAttendPeople();
            if (o == null) {
                return;
            }
            boolean isExit = true;
            for (int i = 0; i < o.getItemList().size(); i++) {
                InterfaceMember.pbui_Item_MemberDetailInfo item = o.getItemList().get(i);
                LogUtil.i(TAG, "fun_queryAttendPeople 人员ID：" + item.getPersonid() + "，名称：" + item.getName().toStringUtf8());
                if (item.getPersonid() == Values.localMemberId) {
                    isExit = false;
                    break;
                }
            }
            if (isExit) jump2Main();
            LogUtil.e(TAG, "MeetingActivity.fun_queryAttendPeople :  收到参会人信息 --> ");
            fun_queryDeviceInfo();
            fun_queryAttendPeoplePermissions();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryAttendPeoplePermissions() {
        try {
            InterfaceMember.pbui_Type_MemberPermission o = jni.queryAttendPeoplePermissions();
            if (o == null) {
                return;
            }
            mPermissionsList.clear();
            mPermissionsList.addAll(o.getItemList());
            for (int i = 0; i < mPermissionsList.size(); i++) {
                InterfaceMember.pbui_Item_MemberPermission item = mPermissionsList.get(i);
                if (item.getMemberid() == Values.localMemberId) {
                    Values.localPermission = item.getPermission();
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryDeviceInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo o = jni.queryDeviceInfo();
            if (o == null) {
                return;
            }
            LogUtil.e(TAG, "MeetingActivity.fun_queryDeviceInfo :  收到设备信息 --> ");
            fun_queryDevMeetInfo();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.ACTION_SCREEN_OFF:
            case EventType.ACTION_SCREEN_ON:
            case EventType.DEV_REGISTER_INFORM://设备寄存器变更通知
                updateOnline();
                break;
            case EventType.NETWORK_CHANGE:
                LogUtil.d(TAG, "getEventMessage --> 网络变更");
                if (NetworkUtil.isNetworkAvailable(getApplicationContext())) {
                    updateOnline();
                } else {
//                    ToastUtils.showShort(R.string.NetWorkError);
                    LogUtil.e(TAG, "getEventMessage -->" + "网络断开");
                    meet_tv_online.setText(getString(R.string.offline));
                    TryToReconnect();
                }
                break;
            case EventType.DEVMEETINFO_CHANGE_INFORM://设备会议信息变更通知
                fun_queryDevMeetInfo();
                break;
//            case EventType.MEETINFO_CHANGE_INFORM://会议信息变更通知
//                InterfaceBase.pbui_MeetNotifyMsg object1 = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
//                int opermethod1 = object1.getOpermethod();
//                int id1 = object1.getId();
//                if (opermethod1 == 3 && id1 == NativeService.meetingId) {
//                    jump2Main();
//                }
//                break;
            case EventType.SIGNIN_SEAT_INFORM://会场设备信息变更通知
                InterfaceBase.pbui_MeetNotifyMsgForDouble object = (InterfaceBase.pbui_MeetNotifyMsgForDouble) message.getObject();
                int opermethod = object.getOpermethod();
                int id = object.getId();
                int subid = object.getSubid();
                if (opermethod == 4) {//删除
                    if (subid == Values.localDevId) {
                        if (isCanJump2Main) {
                            isCanJump2Main = false;
                            ToastUtils.showShort(R.string.exit_from_room);
                            jump2Main();
                        }
                    }
                }
                break;
            /* **** **  变更通知  ** **** */
            case EventType.MeetSeat_Change_Inform://会议排位变更通知
                fun_queryMeetRanking();
                break;
            case EventType.MEET_FUNCTION_CHANGEINFO://会议功能变更通知
                fun_queryMeetFunction();
                break;
            case EventType.MEMBER_CHANGE_INFORM://参会人员变更通知
                InterfaceBase.pbui_MeetNotifyMsg infos = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
                LogUtil.i(TAG, "getEventMessage 参会人员变更通知：" + infos.getId() + ", " + infos.getOpermethod());
                fun_queryAttendPeople();
                break;
            case EventType.MEMBER_PERMISSION_INFORM://参会人权限变更通知
                fun_queryAttendPeoplePermissions();
                break;
            case EventType.MEET_DATE://更新时间UI
                updataTimeUi(message);
                break;
            /* **** **  画板  ** **** */
            case EventType.OPEN_BOARD://收到白板打开操作
                eventOpenBoard(message);
                break;
            case EventType.CHANGE_LOGO_IMG://下载logo图片完成
                String logofile = (String) message.getObject();
                LogUtil.e(TAG, "getEventMessage :  下载logo图片完成 --> " + logofile);
                logo_iv.setImageDrawable(Drawable.createFromPath(logofile));
//                Glide.with(this).load(logofile)
//                        .signature(new StringSignature(System.currentTimeMillis() + ""))//设置TAG
//                        .skipMemoryCache(true)// 不使用内存缓存
//                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
//                        .into(logo_iv);
                break;
            case EventType.SIGNIN_SEAT_FRAG://选中签到图形fragment
                showFragment(Macro.PB_MEET_FUN_CODE_SIGN_IN_RESULT);
                funAdapter.setCheckedId(funData.indexOf(InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_SIGNINRESULT_VALUE));
                break;
            case EventType.SIGNIN_DETAILS://打开签到详情
                showFragment(Macro.PB_MEET_FUN_CODE_SIGN_IN_SEAT);
                funAdapter.setCheckedId(funData.indexOf(InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_SIGNINRESULT_VALUE));
                break;
            case EventType.ICC_changed_inform://界面配置变更通知
                LogUtil.d(TAG, "getEventMessage: 界面配置变更通知");
                fun_queryInterFaceConfiguration();
                break;
            case EventType.EXPORT_VOTEENTRY_FINISH://导出投票录入文件成功通知
                ToastUtils.showShort(R.string.export_finish);
                break;
            case EventType.SUB_BG_PNG_IMG://子界面背景下载完成
                String filepath1 = (String) message.getObject();
                Drawable drawable = Drawable.createFromPath(filepath1);
                root_layout_id.setBackground(drawable);
                break;
//            case EventType.open_picture://收到打开图片
//                try {
//                    String filepath = (String) message.getObject();
//                    System.out.println("将要打开的图片的路径 --> " + filepath);
//                    int index = 0;
//                    if (!piclist.contains(filepath)) {
//                        piclist.add(filepath);
//                        photoViewer(piclist, piclist.size() - 1);
//                    } else {//已经有了该路径的图片
////                        for (int i = 0; i < piclist.size(); i++) {
////                            if (piclist.get(i).equals(filepath)) {
////                                index = i;
////                                break;
////                            }
////                        }
//                        index = piclist.indexOf(filepath);
//                        photoViewer(piclist, index);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
        }
    }

    /**
     * 打开查看图片
     *
     * @param piclist 图片的路径集合
     */
    private void photoViewer(List<String> piclist, int index) {
        runOnUiThread(() -> {
            //        //使用方式
//        PictureConfig config = new PictureConfig.Builder()
//                .setListData((ArrayList<String>) piclist)	//图片数据List<String> list
//                .setPosition(index)	//图片下标（从第position张图片开始浏览）
////                .setDownloadPath("pictureviewer")	//图片下载文件夹地址
//                .setIsShowNumber(true)//是否显示数字下标
////                .needDownload(true)	//是否支持图片下载
////                .setPlacrHolder(R.mipmap.icon)	//占位符图片（图片加载完成前显示的资源图片，来源drawable或者mipmap）
//                .build();
//        ImagePagerActivity.startActivity(this, config);
            ImagePreview.getInstance()
                    .setContext(MeetingActivity.this)
                    .setImageList(piclist)//设置图片地址集合
                    .setIndex(index)//设置开始的索引
                    .setShowDownButton(false)//设置是否显示下载按钮
                    .setShowCloseButton(false)//设置是否显示关闭按钮
                    .setEnableDragClose(true)//设置是否开启下拉图片退出
                    .setEnableUpDragClose(true)//设置是否开启上拉图片退出
                    .setEnableClickClose(true)//设置是否开启点击图片退出
                    .setShowErrorToast(true)
                    .start();
        });
    }

    Timer timer;
    TimerTask timerTask;
    int count = 0;

    private void TryToReconnect() {
        if (timer == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    LogUtil.e(TAG, "TryToReconnect  -->isOnline= " + Values.isOnline + ", 次数：" + count);
                    if (Values.isOnline == 0) {
                        if (count >= 30) {
                            handler.post(() -> jump2Main());
                        } else {
                            count++;
                        }
                    } else {
                        handler.post(() -> updateOnline());
                        cancelTimer();
                    }
                }
            };
            timer.schedule(timerTask, 0, 1000);
        }
    }

    private void cancelTimer() {
        timerTask.cancel();
        timer.purge();
        timer.cancel();
        timerTask = null;
        timer = null;
        count = 0;
    }

    private void eventOpenBoard(EventMessage message) {
        LogUtil.d(TAG, "eventOpenBoard: 收到打开白板操作...");
        InterfaceWhiteboard.pbui_Type_MeetStartWhiteBoard object4 = (InterfaceWhiteboard.pbui_Type_MeetStartWhiteBoard) message.getObject();
        int operflag = object4.getOperflag();//指定操作标志 参见Pb_MeetPostilOperType
        ByteString medianame = object4.getMedianame();//白板操作描述
        DrawBoardActivity.disposePicOpermemberid = object4.getOpermemberid();//当前该命令的人员ID
        DrawBoardActivity.disposePicSrcmemid = object4.getSrcmemid();//发起人的人员ID 白板标识使用
        DrawBoardActivity.disposePicSrcwbidd = object4.getSrcwbid();//发起人的白板标识 取微秒级的时间作标识 白板标识使用
        if (operflag == InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_FORCEOPEN_VALUE) {
            LogUtil.d(TAG, "eventOpenBoard: 强制打开白板  直接强制同意加入..");
            jni.agreeJoin(Values.localMemberId, DrawBoardActivity.disposePicSrcmemid, DrawBoardActivity.disposePicSrcwbidd);
            startActivity(new Intent(getApplicationContext(), DrawBoardActivity.class));
            DrawBoardActivity.sharing = true;//如果同意加入就设置已经在共享中
            DrawBoardActivity.mSrcmemid = DrawBoardActivity.disposePicSrcmemid;//设置发起的人员ID
            DrawBoardActivity.mSrcwbid = DrawBoardActivity.disposePicSrcwbidd;//设置白板标识
        } else if (operflag == InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_REQUESTOPEN_VALUE) {
            LogUtil.d(TAG, "eventOpenBoard: 询问打开白板..");
            WhetherOpen(DrawBoardActivity.disposePicSrcmemid, DrawBoardActivity.disposePicSrcwbidd, MyUtils.b2s(medianame), DrawBoardActivity.disposePicOpermemberid);
        }
    }

    private void WhetherOpen(final int srcmemid, final long srcwbidd, String medianame, final int opermemberid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MeetingActivity.this);
        builder.setTitle(getString(R.string.join_white_board, medianame));
        builder.setPositiveButton(getString(R.string.agree), (dialog, which) -> {
            //同意加入
            jni.agreeJoin(Values.localMemberId, srcmemid, srcwbidd);
            DrawBoardActivity.sharing = true;//如果同意加入就设置已经在共享中
            DrawBoardActivity.mSrcmemid = srcmemid;//设置发起的人员ID
            DrawBoardActivity.mSrcwbid = srcwbidd;
            Intent intent1 = new Intent(context, DrawBoardActivity.class);
            if (tempPicData != null) {
                savePicData = tempPicData;
                intent1.putExtra("have_pic", "have_pic");
                LogUtil.d(TAG, "WhetherOpen:  putExtra 'have_pic' ");
                /* **** **  作为接收者保存  ** **** */
                DrawBoard.DrawPath drawPath = new DrawBoard.DrawPath();
                drawPath.operid = Values.operid;
                drawPath.srcwbid = srcwbidd;
                drawPath.srcmemid = srcmemid;
                drawPath.opermemberid = opermemberid;
                drawPath.picdata = savePicData;
                Values.operid = 0;
                tempPicData = null;
                //将路径保存到共享中绘画信息
                pathList.add(drawPath);
            }
            intent1.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
            //自己不是发起人的时候,每次收到绘画通知都要判断是不是同一个发起人和白板标识
            //并且集合中没有这一号人,将其添加进集合中
            if (!togetherIDs.contains(opermemberid)) {
                togetherIDs.add(opermemberid);
            }
            dialog.dismiss();
        });
        builder.setNegativeButton(getString(R.string.reject), (dialog, which) -> {
            jni.rejectJoin(Values.localMemberId, srcmemid, srcwbidd);
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void fun_queryDevMeetInfo() {
        try {
            DevMeetInfo = jni.queryDeviceMeetInfo();
            if (DevMeetInfo == null) {
                return;
            }
            //公司 会议 参会人名称
            String meetingName = MyUtils.b2s(DevMeetInfo.getMeetingname());
            String memberName = MyUtils.b2s(DevMeetInfo.getMembername());
            meet_title_tv.setText(meetingName);
            member.setText(memberName);
            Values.localMemberId = DevMeetInfo.getMemberid();
            LogUtil.i(TAG, "fun_queryDevMeetInfo 设置本机参会人ID" + Values.localMemberId);
            Values.localMemberName = DevMeetInfo.getMembername().toStringUtf8();
            LogUtil.i(TAG, "fun_queryDevMeetInfo --> deviceid=" + DevMeetInfo.getDeviceid() + ",meetingid=" + DevMeetInfo.getMeetingid() + ",memberid=" + DevMeetInfo.getMemberid()
                    + ",roomid=" + DevMeetInfo.getRoomid() + ",meetingname=" + meetingName + ",membername= " + memberName + ",company=" + DevMeetInfo.getCompany().toStringUtf8() + ",job=" + DevMeetInfo.getJob().toStringUtf8());
            if (DevMeetInfo.getDeviceid() == Values.localDevId && DevMeetInfo.getMeetingid() == 0) {
                if (isCanJump2Main) {
                    isCanJump2Main = false;
                    ToastUtils.showShort(R.string.exit_form_meeting);
                    jump2Main();
                }
            }
            fun_queryMeetRanking();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryMeetRanking() {
        LogUtil.e(TAG, "MeetingActivity.fun_queryMeetRanking :  会议排位变更通知 --> ");
        try {
            InterfaceRoom.pbui_Type_MeetSeatDetailInfo meetSeat = jni.queryMeetRanking();
            if (meetSeat == null) {
                return;
            }
            List<InterfaceRoom.pbui_Item_MeetSeatDetailInfo> itemList = meetSeat.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceRoom.pbui_Item_MeetSeatDetailInfo info = itemList.get(i);
                int seatid = info.getSeatid();//设备ID
                int nameId = info.getNameId();//参会人员ID
                int role = info.getRole();//人员身份  参见Pb_MeetMemberRole
                if (Values.localMemberId == nameId) {
                    LogUtil.d(TAG, "fun_queryMeetRanking :   --> 当前人员ID= " + nameId +
                            ", 本机人员ID= " + Values.localMemberId + ", 角色role= " + role
                            + ",NativeService.hasAllPermissions= " + Values.hasAllPermissions);
                    //本机是否有秘书管理的权限
                    if (role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE
                            || role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE
                            || role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin_VALUE) {
                        setMemberRoleTv(role);
                        Values.hasAllPermissions = true;
                        if (secretary.getVisibility() != View.VISIBLE) {
                            ToastUtils.showShort(R.string.role_change);
                            secretary.setVisibility(View.VISIBLE);
                            meet_btn.performClick();
                        }
                    } else {
                        Values.hasAllPermissions = false;
                        if (secretary.getVisibility() == View.VISIBLE) {
                            ToastUtils.showShort(R.string.role_change);
                            setMemberRoleTv(role);
                            secretary.setVisibility(View.GONE);
                            setDefaultClick();
                        }
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void updataTimeUi(EventMessage message) {
        long object1 = (long) message.getObject();
        String[] object = DateUtil.getGTMDate(object1);
        date_tv.setText(object[0]);
        week_tv.setText(object[1]);
        time_tv.setText(object[2]);
    }

    private void jump2Main() {
        LogUtil.i(TAG, "jump2Main -->");
        Intent intent1 = new Intent(this, MainActivity.class);
        intent1.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent1);
        saveIndex = -1;
        finish();
    }

    @Override
    public void onBackPressed() {
        LogUtil.e(TAG, "MeetingActivity.onBackPressed :   --> ");
        /**  判断当前网络界面是否显示  */
        if (webView_isshowing) {
            EventBus.getDefault().post(new EventMessage(EventType.go_back_html));
        } else {
            exit();
        }
    }

    public void showFragment(int index) {
        if (Macro.PB_MEET_FUN_CODE_OPEN_BACKGROUND == index) {
            ToastUtils.showShort(R.string.no_this_function);
            return;
        }
        if (/*!isRestart && */Macro.PB_MEET_FUN_CODE_WHITE_BOARD != index) {
            saveIndex = index;
        }
        FragmentTransaction ft = mFm.beginTransaction();
        hideFragment(ft);
        switch (index) {
            case Macro.PB_MEET_FUN_CODE_SIGN_IN_RESULT://签到结果图
                if (mSigninSeatFragment == null) {
                    mSigninSeatFragment = new SeatFragment();
                    ft.add(R.id.meet_fl, mSigninSeatFragment);
                }
                ft.show(mSigninSeatFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_SIGN_IN_SEAT://签到详情
                if (mSigninFragment == null) {
                    mSigninFragment = new SigninFragment();
                    ft.add(R.id.meet_fl, mSigninFragment);
                }
                ft.show(mSigninFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_AGENDA_BULLETIN:
                if (mAnnAgendaFragment == null) {
                    mAnnAgendaFragment = new AgendaFragment();
                    ft.add(R.id.meet_fl, mAnnAgendaFragment);
                }
                ft.show(mAnnAgendaFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_MATERIAL:
                if (mMeetingFileFragment == null) {
                    mMeetingFileFragment = new MeetingFileFragment();
                    ft.add(R.id.meet_fl, mMeetingFileFragment);
                }
                ft.show(mMeetingFileFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_SHARED_FILE:
                if (mSharedFileFragment == null) {
                    mSharedFileFragment = new SharedFileFragment();
                    ft.add(R.id.meet_fl, mSharedFileFragment);
                }
                ft.show(mSharedFileFragment);
                break;
            case PB_MEET_FUN_CODE_MESSAGE:
                if (mChatFragment == null) {
                    mChatFragment = new ChatFragment();
                    ft.add(R.id.meet_fl, mChatFragment);
                }
                ft.show(mChatFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_VIDEO_STREAM:
                VideoFragment.admin_operate = false;
                if (mVideoFragment == null) {
                    mVideoFragment = new VideoFragment();
                    ft.add(R.id.meet_fl, mVideoFragment);
                }
                ft.show(mVideoFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_VOTE:
                if (mSurveyFragment == null) {//问卷调查
                    mSurveyFragment = new QuestionnaireFragment();
                    ft.add(R.id.meet_fl, mSurveyFragment);
                }
                ft.show(mSurveyFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_POSTIL:
                if (mNotationFragment == null) {
                    mNotationFragment = new PostilFragment();
                    ft.add(R.id.meet_fl, mNotationFragment);
                }
                ft.show(mNotationFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_WEB_BROWSER:
                if (mWebbrowseFragment == null) {
                    mWebbrowseFragment = new WebBrowseFragment();
                    ft.add(R.id.meet_fl, mWebbrowseFragment);
                }
                ft.show(mWebbrowseFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_DOCUMENT:
                if (mOverViewDocFragment == null) {
                    mOverViewDocFragment = new CameraFragment();
                    ft.add(R.id.meet_fl, mOverViewDocFragment);
                }
                ft.show(mOverViewDocFragment);
                break;
            /* **** **  秘书管理  ** **** */
            case Macro.PB_MEET_FUN_CODE_DEV_CONTROL://设备控制
                if (mDeviceControlFragment == null) {
                    mDeviceControlFragment = new DeviceControlFragment();
                    ft.add(R.id.meet_fl, mDeviceControlFragment);
                }
                ft.show(mDeviceControlFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_CAMERA_CONTROL://摄像控制
                VideoFragment.admin_operate = true;
                if (mVideoFragment == null) {
                    mVideoFragment = new VideoFragment();
                    ft.add(R.id.meet_fl, mVideoFragment);
                }
                ft.show(mVideoFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_VOTE_MANAGE://投票管理
                VoteFragment.isVoteManage = true;
                if (mVoteFragment == null) {
                    mVoteFragment = new VoteFragment();
                    ft.add(R.id.meet_fl, mVoteFragment);
                }
                ft.show(mVoteFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_ELECTORAL_MANAGE://选举管理
                ElectionManageFragment.isSurveyManage = true;
                if (mSurveyManageFragment == null) {
                    mSurveyManageFragment = new ElectionManageFragment();
                    ft.add(R.id.meet_fl, mSurveyManageFragment);
                }
                ft.show(mSurveyManageFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_VOTE_RESULT://投票结果
                VoteFragment.isVoteManage = false;
                if (mVoteFragment == null) {
                    mVoteFragment = new VoteFragment();
                    ft.add(R.id.meet_fl, mVoteFragment);
                }
                ft.show(mVoteFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_ELECTORAL_RESULT://选举结果
                ElectionManageFragment.isSurveyManage = false;
                if (mSurveyManageFragment == null) {
                    mSurveyManageFragment = new ElectionManageFragment();
                    ft.add(R.id.meet_fl, mSurveyManageFragment);
                }
                ft.show(mSurveyManageFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_SCREEN_MANAGE://屏幕管理
                if (mScreenManageFragment == null) {
                    mScreenManageFragment = new ScreenManageFragment();
                    ft.add(R.id.meet_fl, mScreenManageFragment);
                }
                ft.show(mScreenManageFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_PERMISSION_MANAGE://权限管理
                if (mPermissionManageFragment == null) {
                    mPermissionManageFragment = new PermissionManageFragment();
                    ft.add(R.id.meet_fl, mPermissionManageFragment);
                }
                ft.show(mPermissionManageFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_COMMUNIQUE://会议公告
                if (mNoticeFragment == null) {
                    mNoticeFragment = new NoticeFragment();
                    ft.add(R.id.meet_fl, mNoticeFragment);
                }
                ft.show(mNoticeFragment);
                break;
            case Macro.PB_MEET_FUN_CODE_OPEN_BACKGROUND://打开后台
                break;
        }
        ft.commitAllowingStateLoss();//允许状态丢失，其他完全一样
//        ft.commit();//出现异常：Can not perform this action after onSaveInstanceState
    }

    /**
     * 如果Fragment不为空 就先隐藏起来
     *
     * @param ft
     */
    public void hideFragment(FragmentTransaction ft) {
        /* **** **  功能页  ** **** */
        if (mSigninFragment != null) ft.hide(mSigninFragment);//签到信息
        if (mAnnAgendaFragment != null) ft.hide(mAnnAgendaFragment);//公告议程
        if (mMeetingFileFragment != null) ft.hide(mMeetingFileFragment);//会议资料
        if (mSharedFileFragment != null) ft.hide(mSharedFileFragment);//共享文件
        if (mChatFragment != null) ft.hide(mChatFragment);//会议交流
        if (mVideoFragment != null) ft.hide(mVideoFragment);//视频直播/摄像控制
        if (mSurveyFragment != null) ft.hide(mSurveyFragment);//问卷调查
        if (mNotationFragment != null) ft.hide(mNotationFragment);//批注文件
        if (mWebbrowseFragment != null) ft.hide(mWebbrowseFragment);//网页浏览
        if (mOverViewDocFragment != null) ft.hide(mOverViewDocFragment);//外部打开
        if (mSigninSeatFragment != null) ft.hide(mSigninSeatFragment);//签到席位
        /* **** **  秘书管理页  ** **** */
        if (mDeviceControlFragment != null) ft.hide(mDeviceControlFragment);//设备控制
        if (mVoteFragment != null) ft.hide(mVoteFragment);//投票管理/结果
        if (mSurveyManageFragment != null) ft.hide(mSurveyManageFragment);//选举管理/结果
        if (mScreenManageFragment != null) ft.hide(mScreenManageFragment);//屏幕管理
        if (mNoticeFragment != null) ft.hide(mNoticeFragment);//会议公告
        if (mPermissionManageFragment != null) ft.hide(mPermissionManageFragment);//权限管理
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qr_img://生成二维码
                if (DevMeetInfo != null) {
                    try {
                        String qrcode = "{\"meetid\"" + ":\"" + DevMeetInfo.getMeetingid() + "\"," + "\"roomid\":\"" + Values.roomId + "\"}";
                        LogUtil.e(TAG, "onClick :  生成二维码 --> " + qrcode);
                        QRCode = QRCodeUtil.createQRCode(qrcode, 400);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    PopUtils.PopBuilder.createPopupWindow(R.layout.qr_code_layout, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                            App.getRootView(), Gravity.CENTER, 0, 0, true, new PopUtils.ClickListener() {
                                @Override
                                public void setUplistener(PopUtils.PopBuilder builder) {
                                    ImageView qr_code = builder.getView(R.id.qr_code_iv);
                                    if (QRCode != null) {
                                        qr_code.setImageBitmap(QRCode);
                                    }
                                }

                                @Override
                                public void setOnDismissListener(PopUtils.PopBuilder builder) {
                                    if (QRCode != null) {
                                        QRCode.recycle();
                                        QRCode = null;
                                    }
                                }
                            });
                }
                break;
            case R.id.win_iv:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.close_iv://关闭应用
                exit();
                break;
            case R.id.meet_btn://会议界面
                meet_rl.setVisibility(View.VISIBLE);
                secretary_rl.setVisibility(View.GONE);
                if (funData != null && !funData.isEmpty()) {
                    if (mBadge.getBadgeNumber() > 0) {
                        showFragment(funData.get(chatIndex));
                        funAdapter.setCheckedId(chatIndex);
                    } else {
                        showFragment(funData.get(0));
                        funAdapter.setCheckedId(0);
                    }
                }
                if (secretary.getVisibility() != View.GONE) {
                    meet_btn.setTextColor(Color.GREEN);
                    secretary.setTextColor(Color.WHITE);
                } else {
                    meet_btn.setTextColor(Color.WHITE);
                }
                break;
            case R.id.secretary://秘书管理功能页
                secretary_rl.setVisibility(View.VISIBLE);
                meet_rl.setVisibility(View.GONE);
                showFragment(Macro.PB_MEET_FUN_CODE_DEV_CONTROL);
                secretaryAdapter.setCheckedId(0);
                secretary.setTextColor(Color.GREEN);
                meet_btn.setTextColor(Color.WHITE);
                break;
            default:
                break;
        }
    }

    private void exit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MeetingActivity.this);
        builder.setTitle(R.string.Whether_to_quit);
        builder.setPositiveButton(R.string.ensure, (dialog, which) -> {
            dialog.dismiss();
            if (isCanJump2Main) {
                isCanJump2Main = false;
                jump2Main();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private long lastTime = 0;
    private int clickCount = 0;

    private void initView() {
        root_layout_id = findViewById(R.id.root_layout_id);
        meet_fl = findViewById(R.id.meet_fl);
        meet_tv_online = findViewById(R.id.meet_tv_online);
        meet_tv_online.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastTime > 1500) {
                clickCount = 1;
            } else {
                clickCount++;
            }
            lastTime = System.currentTimeMillis();
            if (clickCount >= 5) {
                isDebug = !isDebug;
                if (isDebug) {
                    ToastUtils.showShort("Debug mode turned on");
                } else {
                    ToastUtils.showShort("Debug mode turned off");
                }
            }
        });
        logo_iv = findViewById(R.id.meet_logo_iv);
        meet_btn = findViewById(R.id.meet_btn);
        secretary = findViewById(R.id.secretary);
        secretary.setVisibility(View.GONE);

        member_with_role = findViewById(R.id.member_with_role);
        company_name_tv = findViewById(R.id.company_name_tv);
        time_tv = findViewById(R.id.time_tv);
        date_tv = findViewById(R.id.date_tv);
        week_tv = findViewById(R.id.week_tv);
        qr_img = findViewById(R.id.qr_img);
        win_iv = findViewById(R.id.win_iv);
        close_iv = findViewById(R.id.close_iv);
//        compere_tv =  findViewById(R.id.compere_tv);
        member = findViewById(R.id.member);
        meet_title_tv = findViewById(R.id.meet_title_tv);
        meet_rl = findViewById(R.id.meet_rl);
        rvLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        meet_rl.setLayoutManager(rvLayoutManager);
        secretary_rl = findViewById(R.id.secretary_rl);
        secretary_rl.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        initSecretaryData();
        if (mBadge == null) {
            /** ************ ******  设置未读消息展示  ****** ************ **/
            mBadge = new QBadgeView(this).bindTarget(meet_btn);
            mBadge.setBadgeGravity(Gravity.END | Gravity.TOP);
            mBadge.setBadgeTextSize(14, true);
            mBadge.setShowShadow(true);
            mBadge.setOnDragStateChangedListener((dragState, badge, targetView) -> {
                //只需要空实现，就可以拖拽消除未读消息
            });
        }
        qr_img.setOnClickListener(this);
        win_iv.setOnClickListener(this);
        close_iv.setOnClickListener(this);
        meet_btn.setOnClickListener(this);
        secretary.setOnClickListener(this);
    }
}

