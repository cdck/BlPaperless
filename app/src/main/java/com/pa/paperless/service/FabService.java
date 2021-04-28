package com.pa.paperless.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.intrusoft.scatter.ChartData;
import com.intrusoft.scatter.PieChart;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceVote;
import com.pa.boling.paperless.R;
import com.pa.paperless.activity.DrawBoardActivity;
import com.pa.paperless.activity.NoteActivity;
import com.pa.paperless.activity.NoticeActivity;
import com.pa.paperless.adapter.rvadapter.CanJoinMemberAdapter;
import com.pa.paperless.adapter.rvadapter.CanJoinProAdapter;
import com.pa.paperless.adapter.rvadapter.OnLineProjectorAdapter;
import com.pa.paperless.adapter.rvadapter.ScreenControlAdapter;
import com.pa.paperless.adapter.rvadapter.VoteOptionResultAdapter;
import com.pa.paperless.adapter.rvadapter.VoteTitleResultAdapter;
import com.pa.paperless.data.bean.DevMember;
import com.pa.paperless.data.bean.SubmitVoteBean;
import com.pa.paperless.data.bean.VideoInfo;
import com.pa.paperless.data.bean.VoteResultSubmitMember;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.fragment.CameraFragment;
import com.pa.paperless.utils.ConvertUtil;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.PopUtils;
import com.pa.paperless.utils.ScreenUtils;

import com.wind.myapplication.CameraDemo;
import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.pa.paperless.data.constant.Macro.CACHE_FILE;
import static com.pa.paperless.data.constant.Values.mPermissionsList;
import static com.pa.paperless.fragment.CameraFragment.cameraIsShowing;

import static com.pa.paperless.utils.FileUtil.createDir;
import static com.pa.paperless.utils.MyUtils.getMediaid;
import static com.pa.paperless.utils.MyUtils.isHasPermission;

/**
 * Created by xlk
 * on 2018/5/31.
 * 弹窗
 */
public class FabService extends Service {
    private final String TAG = "Fabservice-->";
    private WindowManager wm;
    private ImageView mImageView;
    private long downTime, upTime;
    private int mTouchStartX, mTouchStartY;
    private WindowManager.LayoutParams mParams, params, postilParams, notParams, serviceParams;
    private List<DevMember> faceOnLMember, onLineMember, allOnLineMember;//界面状态为1的参会人和在线状态参会人
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> allProjectors, onLineProjectors;
    public static OnLineProjectorAdapter allProjectorAdapter, onLineProjectorAdapter, pushOnLineProjectorAdapter;
    public static ScreenControlAdapter onLineMemberAdapter, pushOnLineMemberAdapter;
    public static ArrayList<Integer> tempRes, allScreenDevIds, applyProjectionIds, onlineClientIds;
    private VideoInfo videoInfo;
    private boolean openProjectpopFromPostilPop, openScreenpopFromPostilpop;//是否是从批注页面打开投影或者同屏
    private int windowWidth;
    private int windowHeight;
    private int mScreenDensity;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private long tempImgName;//截图的临时文件名
    private Context fabContext;
    public static byte[] bytes;//截图处理页面的byte数据
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos = new ArrayList<>();
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos = new ArrayList<>();
    private List<DevMember> canJoinMember;//存放可加入同屏的参会人
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> canJoinPro;//存放可加入同屏的投影机
    public static CanJoinProAdapter canJoinProAdapter;
    public static CanJoinMemberAdapter canJoinMemberAdapter;
    public static String FabPicName;
    public static File FabPicFile;
    public static boolean openScreen, openProjector;//是否从视屏页面打开的同屏/投影
    private String seriveStr;
    private static List<TextView> seriveTvs;
    private boolean startPro;
    private boolean startScr;
    private int screenWidth;
    //    private boolean query_form_fab;
//    private boolean dev_form_fab;
    private VoteTitleResultAdapter voteResultAdapter;
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteResultData;
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteResultInfo;
    private List<ChartData> chartDatas;
    private int countPre;//一共占用的百分比数
    private boolean voteResult;
    private List<VoteResultSubmitMember> submitMemberData;
    private RecyclerView optionRv;
    private int selectedItem = 0;
    private InterfaceVote.pbui_Item_MeetVoteDetailInfo currentVoteInfo;
    private boolean queryOneVoteSubmitter;
    private VoteOptionResultAdapter optionAdapter;
    private boolean clickqueryVoteResult;
    private boolean pushPopIsshowing;
    private InterfaceDevice.pbui_Type_MeetMemberCastInfo signinCountData;

    LinkedList<InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify> permissionsRequests = new LinkedList<>();

    private View pop, screenPop, choosePlayerPop, projectPop, proListPop, callServePop,
            postilPop, edtPop, canJoinPop, mChoosePop, voteResultPop, mNotice_pop,
            mRequestPermissionPop, mChooseCameraPop, mVoteEnsureView;
    private boolean mImageViewIsShow, popIsShow, screenPopIsShow, choosePlayerPopIsShow,
            projectPopIsShow, proListPopIsShow, mChoosePopIsShow, callServePopIsShow,
            postilPopIsShow, edtPopIsShow, canJoinPopIsShow, voteResultPopIsShow,
            mNotice_popIsShow, mRequestPermissionPopIsShow, mChooseCameraPopIsShow, mVoteEnsureViewIsShow;
    private int voteTimeouts;
    private int noticeId;
    private RequestPermissionViewHolder requestPermissionViewHolder;
    private Timer timeTimer;
    private int nowTime;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                String p = (String) msg.obj;
                timeButton.setText(p);
            }
        }
    };
    private boolean timeButtonIsShow;
    private Button timeButton;
    private WindowManager.LayoutParams timeParams;
    private int currentVoteId;
    private NativeUtil jni = NativeUtil.getInstance();

    @Override
    public void onCreate() {
        LogUtil.i("Fab_life", "FabService.onCreate :   --> " + this);
        super.onCreate();
        EventBus.getDefault().register(this);
        fun_queryAttendPeople();
        fun_queryAttendPeoplePermissions();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i("Fab_life", "FabService.onStartCommand :  --> ");
        showFab();
        return super.onStartCommand(intent, flags, startId);
    }

    //起点
    private void showFab() {
        fabContext = getApplicationContext();
        screenWidth = ScreenUtils.getScreenWidth(fabContext);
        //获取 WindowManager
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowWidth = wm.getDefaultDisplay().getWidth();
        windowHeight = wm.getDefaultDisplay().getHeight();
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2);
        //初始化悬浮按钮和弹出框布局
        initViews();
        // 初始化需要使用的Params
        initParams();
        if (memberInfos != null && deviceInfos != null) {
            initAdapter();
        }
        wm.addView(mImageView, mParams);
        mImageViewIsShow = true;

        mRequestPermissionPop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.permissions_request, null);
        mRequestPermissionPop.setTag("mRequestPermissionPop");
        requestPermissionViewHolder = new RequestPermissionViewHolder(mRequestPermissionPop);
    }

    /**
     * @param type =0开始，=1停止
     */
    private void openScreenTime(int type) {
        if (type == 0) {
            if (!timeButtonIsShow) {
                nowTime = 0;
                timeTimer = new Timer();
                timeTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        nowTime++;
                        String time = MyUtils.intTotime(nowTime);
                        Message message = new Message();
                        message.what = 1;
                        message.obj = time;
                        handler.sendMessage(message);
                    }
                }, 0, 1000);
                /** **** **  悬浮按钮  ** **** **/
                timeParams = new WindowManager.LayoutParams();
                //设置view不可点击且不会消费点击事件->不拦截底层view的点击事件
                timeParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                setParamsType(timeParams);
                timeParams.format = PixelFormat.RGBA_8888;
                timeParams.gravity = Gravity.LEFT | Gravity.TOP;
                timeParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
                timeParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                timeParams.x = 0;
                LogUtil.e(TAG, "FabService.openScreenTime :  windowHeight --> " + windowHeight
                        + ", 屏幕高度 = " + ScreenUtils.getScreenHeight(fabContext)
                        + ", IV高度 = " + mImageView.getHeight());
                timeParams.y = 0;
                timeParams.windowAnimations = R.style.AnimHorizontal;

                wm.addView(timeButton, timeParams);
                timeButtonIsShow = true;
            }
        } else {
            exitTiming();
        }
    }

    //退出录屏时的计时
    private void exitTiming() {
        if (timeTimer != null) {
            timeTimer.cancel();
            timeTimer = null;
        }
        if (timeButtonIsShow) {
            wm.removeView(timeButton);
            timeButtonIsShow = false;
        }
        handler.removeCallbacksAndMessages(null);
        nowTime = 0;
    }

    // 初始化 WindowManager.LayoutParams
    private void initParams() {
        /** **** **  悬浮按钮  ** **** **/
        mParams = new WindowManager.LayoutParams();
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        setParamsType(mParams);
        mParams.format = PixelFormat.RGBA_8888;
        mParams.gravity = Gravity.START | Gravity.TOP;
        mParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        mParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        mParams.x = 0;
        LogUtil.e(TAG, "FabService.initParams :  windowHeight --> " + windowHeight
                + ", 屏幕高度 = " + ScreenUtils.getScreenHeight(fabContext)
                + ", IV高度 = " + mImageView.getHeight());
        mParams.y = windowHeight - 100;
        mParams.windowAnimations = R.style.AnimHorizontal;
        /** **** **  弹框  ** **** **/
        params = new WindowManager.LayoutParams();
        setParamsType(params);
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.CENTER;
        params.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        params.windowAnimations = R.style.AnimHorizontal;
        /** **** **  充满屏幕  ** **** **/
        postilParams = new WindowManager.LayoutParams();
        setParamsType(postilParams);
        postilParams.format = PixelFormat.RGBA_8888;
        postilParams.gravity = Gravity.CENTER;
        postilParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        postilParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        postilParams.windowAnimations = R.style.AnimHorizontal;
        /** **** **  外部不可点击  ** **** **/
        notParams = new WindowManager.LayoutParams();
        setParamsType(notParams);
        notParams.format = PixelFormat.RGBA_8888;
        notParams.gravity = Gravity.CENTER;
        notParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        notParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        notParams.windowAnimations = R.style.AnimHorizontal;
        /** **** **   服务弹窗使用,点击编辑框使view能够上移不遮挡输入框 ** **** **/
        serviceParams = new WindowManager.LayoutParams();
        setParamsType(serviceParams);
        serviceParams.format = PixelFormat.RGBA_8888;
        serviceParams.gravity = Gravity.CENTER;
        serviceParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        serviceParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        serviceParams.windowAnimations = R.style.AnimHorizontal;
    }

    // 初始化View
    private void initViews() {
        tempRes = new ArrayList<>();
        tempRes.add(0);
        /** **** **  悬浮按钮  ** **** **/
        mImageView = new ImageView(fabContext);
        mImageView.setTag("mImageView");
        mImageView.setImageResource(R.drawable.fab_bg);

        /** **** **  时间控件  ** **** **/
        timeButton = new Button(fabContext);
        timeButton.setTag("timeButton");
        timeButton.setTextColor(Color.RED);
        //设置按钮的背景透明，只显示文本
        timeButton.getBackground().setAlpha(50);

        /** **** **  弹出框主页  ** **** **/
        pop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_quick_function, null);
        pop.setTag("pop");
        FabViewHolder holder = new FabViewHolder(pop);
        holderEvent(holder);
        mImageView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downTime = System.currentTimeMillis();
                    mTouchStartX = (int) event.getRawX();
                    mTouchStartY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int rawX = (int) event.getRawX();
                    int rawY = (int) event.getRawY();
                    int mx = rawX - mTouchStartX;
                    int my = rawY - mTouchStartY;
                    mParams.x += mx;
                    mParams.y += my;//相对于屏幕左上角的位置
                    wm.updateViewLayout(mImageView, mParams);
                    mTouchStartX = rawX;
                    mTouchStartY = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    upTime = System.currentTimeMillis();
                    if (upTime - downTime > 150) {
                        LogUtil.e(TAG, "FabService.onTouch :  screenWidth --> " + screenWidth + " mImageView.getWidth():" + mImageView.getWidth());
//                        mParams.x = screenWidth - mImageView.getWidth();
                        mParams.x = 0;
                        mParams.y = mTouchStartY - mImageView.getHeight();
                        wm.updateViewLayout(mImageView, mParams);
                    } else {
                        LogUtil.e(TAG, "FabService.onTouch :  打开小功能主页 --> " + (pop == null));
                        showPop(mImageView, pop);
                    }
                    break;
            }
            return true;
        });
    }

    //弹框主页事件
    private void holderEvent(FabViewHolder holder) {
        //截图批注
        holder.postil.setOnClickListener(v -> {
            tempImgName = System.currentTimeMillis();
            wm.removeView(pop);
            popIsShow = false;
            One_ScreenShot();
        });
        //呼叫服务
        holder.call_service.setOnClickListener(v -> One_CallServe_pop());
        //发起同屏
        holder.start_screen.setOnClickListener(v -> {
            if (isHasPermission(Macro.permission_code_screen)) {
                fun_queryAttendPeople();
                startScr = true;
                showPlayerPop(pop);
//                showScreenPop();
//                showPop(pop, screenPop);
            } else {
                ToastUtils.showShort(R.string.tip_no_screen_permissions);
            }
        });
        //加入同屏
        holder.join_screen.setOnClickListener(v -> {
            fun_queryCanJoin();
        });
        //结束同屏
        holder.stop_screen.setOnClickListener(v -> {
            if (isHasPermission(Macro.permission_code_screen)) {
                startScr = false;
                showPlayerPop(pop);
            } else {
                ToastUtils.showShort(R.string.tip_no_screen_permissions);
            }
        });
        //发起投影
        holder.start_pro.setOnClickListener(v -> {
            if (isHasPermission(Macro.permission_code_projection)) {
                LogUtil.v("cause_log", "FabService.onClick :  点击投影控制 :查询设备信息--> ");
                fun_queryDeviceInfo();
                startPro = true;
                showProRlPop(pop);
            } else {
                ToastUtils.showShort(R.string.tip_no_projection_permissions);
            }
        });
        //结束投影
        holder.stop_pro.setOnClickListener(v -> {
            if (isHasPermission(Macro.permission_code_projection)) {
                startPro = false;
                showProRlPop(pop);
            } else {
                ToastUtils.showShort(R.string.tip_no_projection_permissions);
            }
        });
        //会议笔记
        holder.note.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            showPop(pop, mImageView, mParams);
        });
        //投票结果
        holder.vote_result.setOnClickListener(v -> {
            LogUtil.d(TAG, "holderEvent: 点击投票结果");
            clickqueryVoteResult = true;
            fun_queryVote();
        });
        //返回
        holder.back.setOnClickListener(v -> showPop(pop, mImageView, mParams));
    }

    private void deleteAll() {
        LogUtil.e(TAG, "FabService.deleteAll :   --> ");
        if (mImageViewIsShow) {
            wm.removeView(mImageView);
            mImageViewIsShow = false;
        }
        if (popIsShow) {
            wm.removeView(pop);
            popIsShow = false;
        }
        if (screenPopIsShow) {
            wm.removeView(screenPop);
            screenPopIsShow = false;
        }
        if (choosePlayerPopIsShow) {
            wm.removeView(choosePlayerPop);
            choosePlayerPopIsShow = false;
        }
        if (projectPopIsShow) {
            wm.removeView(projectPop);
            projectPopIsShow = false;
        }
        if (proListPopIsShow) {
            wm.removeView(proListPop);
            proListPopIsShow = false;
        }
        if (callServePopIsShow) {
            wm.removeView(callServePop);
            callServePopIsShow = false;
        }
        if (postilPopIsShow) {
            wm.removeView(postilPop);
            postilPopIsShow = false;
        }
        if (edtPopIsShow) {
            wm.removeView(edtPop);
            edtPopIsShow = false;
        }
        if (canJoinPopIsShow) {
            wm.removeView(canJoinPop);
            canJoinPopIsShow = false;
        }
        if (voteResultPopIsShow) {
            wm.removeView(voteResultPop);
            voteResultPopIsShow = false;
        }
        if (mNotice_popIsShow) {
            wm.removeView(mNotice_pop);
            mNotice_popIsShow = false;
        }
        if (mRequestPermissionPopIsShow) {
            wm.removeView(mRequestPermissionPop);
            mRequestPermissionPopIsShow = false;
        }
        if (mChooseCameraPopIsShow) {
            wm.removeView(mChooseCameraPop);
            mChooseCameraPopIsShow = false;
        }
        if (mVoteEnsureViewIsShow) {
            wm.removeView(mVoteEnsureView);
            mVoteEnsureViewIsShow = false;
        }
    }

    private void setIsShowing(View removeView, View addView) {
        switch ((String) removeView.getTag()) {
            case "mImageView":
                mImageViewIsShow = false;
                break;
            case "pop":
                popIsShow = false;
                break;
            case "screenPop":
                screenPopIsShow = false;
                break;
            case "choosePlayerPop":
                choosePlayerPopIsShow = false;
                break;
            case "projectPop":
                projectPopIsShow = false;
                break;
            case "proListPop":
                proListPopIsShow = false;
                break;
            case "callServePop":
                callServePopIsShow = false;
                break;
            case "postilPop":
                postilPopIsShow = false;
                break;
            case "edtPop":
                edtPopIsShow = false;
                break;
            case "canJoinPop":
                canJoinPopIsShow = false;
                break;
            case "mChoosePop":
                mChoosePopIsShow = false;
                break;
            case "voteResultPop":
                voteResultPopIsShow = false;
                break;
            case "mNotice_pop":
                mNotice_popIsShow = false;
                break;
            case "mRequestPermissionPop":
                mRequestPermissionPopIsShow = false;
                break;
            case "mChooseCameraPop":
                mChooseCameraPopIsShow = false;
                break;
            case "mVoteEnsureView":
                mVoteEnsureViewIsShow = false;
                break;
        }
        switch ((String) addView.getTag()) {
            case "mImageView":
                mImageViewIsShow = true;
                break;
            case "pop":
                popIsShow = true;
                break;
            case "screenPop":
                screenPopIsShow = true;
                break;
            case "choosePlayerPop":
                choosePlayerPopIsShow = true;
                break;
            case "projectPop":
                projectPopIsShow = true;
                break;
            case "proListPop":
                proListPopIsShow = true;
                break;
            case "callServePop":
                callServePopIsShow = true;
                break;
            case "postilPop":
                postilPopIsShow = true;
                break;
            case "edtPop":
                edtPopIsShow = true;
                break;
            case "canJoinPop":
                canJoinPopIsShow = true;
                break;
            case "mChoosePop":
                mChoosePopIsShow = true;
                break;
            case "voteResultPop":
                voteResultPopIsShow = true;
                break;
            case "mNotice_pop":
                mNotice_popIsShow = true;
                break;
            case "mRequestPermissionPop":
                mRequestPermissionPopIsShow = true;
                break;
            case "mChooseCameraPop":
                mChooseCameraPopIsShow = true;
                break;
            case "mVoteEnsureView":
                mVoteEnsureViewIsShow = true;
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(final EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.SEND_SCREEN_TIME:
                int type = (int) message.getObject();
                LogUtil.d(TAG, "开启还是停止= " + type);
                openScreenTime(type);
                break;
            case EventType.OPEN_SCREENSPOP://从视屏页面收到打开/停止同屏
                video_screen(message);
                break;
            case EventType.OPEN_PROJECTOR://从视屏页面收到打开停止投影
                video_pro(message);
                break;
            case EventType.take_photo://获得拍摄的照片
                LogUtil.e(TAG, "FabService.getEventMessage :  收到图片通知 --> ");
                Bitmap bitmap = (Bitmap) message.getObject();
                bytes = ConvertUtil.Bitmap2bytes(bitmap);
                if (!bitmap.isRecycled()) bitmap.recycle();
                CameraFragment.can_take = true;
                showPostilPop(bytes);
                showPop(mImageView, postilPop, postilParams);
                break;
            case EventType.MEMBER_CHANGE_INFORM://参会人员变更通知
            case EventType.DEV_REGISTER_INFORM://设备寄存器变更通知（监听参会人退出）
            case EventType.FACESTATUS_CHANGE_INFORM://界面状态变更通知
                fun_queryAttendPeople();
                break;
            case EventType.click_key_home://收到点击HOME键监听
                Values.can_open_camera = false;
                deleteAll();
                wm.addView(mImageView, mParams);
                mImageViewIsShow = true;
                break;
            case EventType.newVote_launch_inform://有新的投票发起通知
                InterfaceBase.pbui_MeetNotifyMsg object3 = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
                int opermethod1 = object3.getOpermethod();
                int id1 = object3.getId();
                LogUtil.e(TAG, "FabService.getEventMessage : 有新的投票发起通知  opermethod --> " + opermethod1 + ",id1= " + id1);
                if (opermethod1 == 29 && signinCountData != null) {//发起投票操作ID
                    fun_queryInitiateVote(id1);
                }
                break;
            case EventType.Vote_Change_Inform://投票变更通知
                InterfaceBase.pbui_MeetNotifyMsg object = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
                int opermethod = object.getOpermethod();
                int id = object.getId();
                LogUtil.e(TAG, "FabService.getEventMessage : 投票变更通知  --> opermethod= " + opermethod + ", id= " + id);
                if (opermethod == 30) {//结束投票操作ID
                    LogUtil.v(TAG, "FabService.getEventMessage 投票变更通知:  操作ID是结束投票引起的 查询指定ID的投票--> ");
                    if (id == currentVoteId) {
                        if (mChoosePopIsShow) {
                            currentVoteId = -1;
                            wm.removeView(mChoosePop);
                            mChoosePopIsShow = false;
                        }
                    }
//                    fun_queryInitiateVote(id, false);
                }
                break;
            case EventType.MEMBER_PERMISSION_INFORM://参会人权限变更通知
                fun_queryAttendPeoplePermissions();
                break;
            case EventType.publish_notice://发布公告通知
                InterfaceBullet.pbui_BulletDetailInfo object2 = (InterfaceBullet.pbui_BulletDetailInfo) message.getObject();
                if (object2 == null) break;
                List<InterfaceBullet.pbui_Item_BulletDetailInfo> itemList = object2.getItemList();
                if (itemList.isEmpty()) break;
                InterfaceBullet.pbui_Item_BulletDetailInfo item = itemList.get(0);
                int bulletid = item.getBulletid();
                NoticeActivity.jump(bulletid, fabContext);
                break;
            case EventType.RECEIVE_PERMISSION_REQUEST://收到请求参会人员权限请求
                InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify permissionsRequest = (InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify) message.getObject();
                if (permissionsRequest == null) break;
                int memberid1 = permissionsRequest.getMemberid();
                int deviceid1 = permissionsRequest.getDeviceid();
                int privilege = permissionsRequest.getPrivilege();
                LogUtil.e(TAG, "FabService.getEventMessage :  收到请求参会人员权限请求 --> " + memberid1
                        + "," + Values.localMemberId);
                if (Values.localMemberId == memberid1) break;
                if (!mRequestPermissionPopIsShow) {
                    wm.addView(mRequestPermissionPop, notParams);
                    mRequestPermissionPopIsShow = true;
                    String memberName = getMemberName(permissionsRequest.getMemberid());
                    setPopText(memberName, deviceid1, privilege);
                } else {
                    permissionsRequests.addLast(permissionsRequest);
                }
                break;
            case EventType.signin_count:
                signinCountData = (InterfaceDevice.pbui_Type_MeetMemberCastInfo) message.getObject();
                int membersignsize = signinCountData.getMembersignsize();
                int membersize = signinCountData.getMembersize();
                int memberid = signinCountData.getMemberid();
                int deviceid = signinCountData.getDeviceid();
                LogUtil.e(TAG, "FabService.getEventMessage :   -->deviceid= " + deviceid + ", memberid= " + memberid + ", membersize= " + membersize + ", membersignsize= " + membersignsize);
                break;
            case EventType.INFORM_PUSH_FILE://收到开启文件推送通知
                int pushMediaid = (int) message.getObject();
                LogUtil.e(TAG, "FabService.getEventMessage :  pushMediaid --> " + pushMediaid);
//                query_form_fab = true;
                //只是用来更新adapter
                fun_queryAttendPeople();
                LogUtil.e(TAG, "getEventMessage :  pushPopIsshowing --> " + pushPopIsshowing);
                if (!pushPopIsshowing) showPushPop(pushMediaid);
                break;
//            case EventType.shot_video://截取视屏
//                One_ScreenShot();
//                break;
            case EventType.CUT_VIDEO_IMAGE:
                deleteAll();//清除所有的view
                showPostilPop(bytes);
                wm.addView(postilPop, postilParams);
                postilPopIsShow = true;
                break;
            case EventType.START_COLLECTION_STREAM_NOTIFY:
                LogUtil.e(TAG, "NativeService.getEventMessage :  收到摄像通知 --> ");
                if (!cameraIsShowing) {
                    if (Values.can_open_camera) showOpenCameraInform();
                    else {
                        ToastUtils.showShort(R.string.tip_background_open_camera);
                    }
                } else {
                    ToastUtils.showShort(R.string.tip_camera_is_busy);
                }
                break;
            default:
                break;
        }
    }

    /**
     * @param currentVoteId 投票ID
     */
    private void fun_queryInitiateVote(int currentVoteId) {
        try {
            InterfaceVote.pbui_Type_MeetOnVotingDetailInfo info = jni.queryInitiateVote();
            if (info == null) return;
            List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> itemList = info.getItemList();
            if (itemList.isEmpty()) return;
            LogUtil.e(TAG, "FabService.fun_queryInitiateVote :  得到发起的投票数量 --> " + itemList.size());
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceVote.pbui_Item_MeetOnVotingDetailInfo item = itemList.get(i);
                LogUtil.e(TAG, "FabService.fun_queryInitiateVote :  item.getVoteid() --> " + item.getVoteid());
                if (item.getVoteid() == currentVoteId) {//找到当前发起的投票
                    LogUtil.e(TAG, "FabService.fun_queryInitiateVote :  查询指定ID投票 是false才会打开 --> " + mChoosePopIsShow + ", currentVoteId= " + currentVoteId);
                    if (mChoosePopIsShow) {
                        wm.removeView(mChoosePop);
                        mChoosePopIsShow = false;
                    } else showChoose(item);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    //选择是否打开前/后置摄像头
    private void showOpenCameraInform() {
        mChooseCameraPop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.choose_camera, null);
        ChooseCameraViewHolder holder = new ChooseCameraViewHolder(mChooseCameraPop);
        mChooseCameraPop.setTag("mChooseCameraPop");
        wm.addView(mChooseCameraPop, notParams);
        mChooseCameraPopIsShow = true;
        holder.pre_btn.setOnClickListener(v -> {
            if (MyUtils.checkCamera(fabContext, 1)) {
                Intent intent = new Intent(fabContext, CameraDemo.class);
                intent.putExtra("camera_type", 1);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                fabContext.startActivity(intent);
                wm.removeView(mChooseCameraPop);
                mChooseCameraPopIsShow = false;
            } else {
                ToastUtils.showShort(R.string.tip_no_camera);
            }
        });
        holder.back_btn.setOnClickListener(v -> {
            if (MyUtils.checkCamera(fabContext, 0)) {
                Intent intent = new Intent(fabContext, CameraDemo.class);
                intent.putExtra("camera_type", 0);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                fabContext.startActivity(intent);
                wm.removeView(mChooseCameraPop);
                mChooseCameraPopIsShow = false;
            } else {
                ToastUtils.showShort(R.string.tip_no_camera);
            }
        });
        holder.cancel.setOnClickListener(v -> {
            wm.removeView(mChooseCameraPop);
            mChooseCameraPopIsShow = false;
        });

    }

    private String getMemberName(int memberId) {
        for (int i = 0; i < memberInfos.size(); i++) {
            InterfaceMember.pbui_Item_MemberDetailInfo memberInfo = memberInfos.get(i);
            if (memberInfo.getPersonid() == memberId) {
                return memberInfo.getName().toStringUtf8();
            }
        }
        return "";
    }

    private void setPopText(String memberName, int deviceId, int privilege) {
        switch (privilege) {
            case InterfaceMacro.Pb_MemberPermissionPropertyID.Pb_memperm_sscreen_VALUE://同屏权限
                requestPermissionViewHolder.textview.setText(getString(R.string.apply_screen_permissions, memberName));
                break;
            case InterfaceMacro.Pb_MemberPermissionPropertyID.Pb_memperm_projective_VALUE://投影权限
                requestPermissionViewHolder.textview.setText(getString(R.string.apply_projection_permissions, memberName));
                break;
            case InterfaceMacro.Pb_MemberPermissionPropertyID.Pb_memperm_upload_VALUE://上传权限
                requestPermissionViewHolder.textview.setText(getString(R.string.apply_upload_permissions, memberName));
                break;
            case InterfaceMacro.Pb_MemberPermissionPropertyID.Pb_memperm_download_VALUE://下载权限
                requestPermissionViewHolder.textview.setText(getString(R.string.apply_download_permissions, memberName));
                break;
            case InterfaceMacro.Pb_MemberPermissionPropertyID.Pb_memperm_vote_VALUE://投票权限
                requestPermissionViewHolder.textview.setText(getString(R.string.apply_vote_permissions, memberName));
                break;
            case InterfaceMacro.Pb_MemberPermissionPropertyID.Pb_memperm_postilview_VALUE://批注查看权限
                requestPermissionViewHolder.textview.setText(getString(R.string.permissions_request, memberName));
                break;
        }
        requestPermissionViewHolder.agree.setOnClickListener(v -> {
            jni.revertAttendPermissionsRequest(deviceId, 1);
            if (!permissionsRequests.isEmpty()) {
                InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify info = permissionsRequests.removeFirst();
                String memberName1 = getMemberName(info.getMemberid());
                int deviceid = info.getDeviceid();
                setPopText(memberName1, deviceid, privilege);
            } else {
                if (mRequestPermissionPopIsShow) {
                    wm.removeView(mRequestPermissionPop);
                    mRequestPermissionPopIsShow = false;
                }
            }
        });

        requestPermissionViewHolder.reject.setOnClickListener(v -> {
            jni.revertAttendPermissionsRequest(deviceId, 0);
            if (!permissionsRequests.isEmpty()) {
                InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify info = permissionsRequests.removeFirst();
                String memberName1 = getMemberName(info.getMemberid());
                int deviceid = info.getDeviceid();
                setPopText(memberName1, deviceid, privilege);
            } else {
                if (mRequestPermissionPopIsShow) {
                    wm.removeView(mRequestPermissionPop);
                    mRequestPermissionPopIsShow = false;
                }
            }
        });
    }

    private void showPushPop(int pushMediaid) {
        pushPopIsshowing = true;
        PopUtils.PopBuilder.createPopupWindow(R.layout.push_pop_layout, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, App.getRootView(), Gravity.CENTER, 0, 0, true, new PopUtils.ClickListener() {
                    @Override
                    public void setUplistener(PopUtils.PopBuilder builder) {
                        CheckBox all_member_cb = builder.getView(R.id.all_member_cb);
                        CheckBox all_projector_cb = builder.getView(R.id.all_projector_cb);
                        all_member_cb.setText(getString(R.string.allchoose_count, allOnLineMember.size() + ""));
                        all_projector_cb.setText(getString(R.string.allchoose_count, onLineProjectors.size() + ""));

                        RecyclerView mrv = builder.getView(R.id.member_rv);
                        mrv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
                        mrv.setAdapter(pushOnLineMemberAdapter);
                        pushOnLineMemberAdapter.setItemClick((view, posion) -> {
                            pushOnLineMemberAdapter.setCheck(allOnLineMember.get(posion).getDevId());
                            all_member_cb.setChecked(pushOnLineMemberAdapter.isAllCheck());
                            pushOnLineMemberAdapter.notifyDataSetChanged();
                        });

                        RecyclerView prv = builder.getView(R.id.peoject_rv);
                        prv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                        prv.setAdapter(pushOnLineProjectorAdapter);
                        pushOnLineProjectorAdapter.setItemClick((view, posion) -> {
                            pushOnLineProjectorAdapter.setCheck(onLineProjectors.get(posion).getDevcieid());
                            all_projector_cb.setChecked(pushOnLineProjectorAdapter.isAllCheck());
                            pushOnLineProjectorAdapter.notifyDataSetChanged();
                        });
                        all_member_cb.setOnClickListener(v -> {
                            boolean checked = all_member_cb.isChecked();
                            all_member_cb.setChecked(checked);
                            pushOnLineMemberAdapter.setAllCheck(checked);
                        });
                        all_projector_cb.setOnClickListener(v -> {
                            boolean checked = all_projector_cb.isChecked();
                            all_projector_cb.setChecked(checked);
                            pushOnLineProjectorAdapter.setAllCheck(checked);
                        });
                        builder.getView(R.id.push_btn).setOnClickListener(v -> {
                            List<Integer> checks = pushOnLineMemberAdapter.getChecks();
                            checks.addAll(pushOnLineProjectorAdapter.getChecks());
                            if (checks.isEmpty()) {
                                ToastUtils.showShort(R.string.please_choose_push);
                            } else {
//                                nativeUtil.filePush(pushMediaid,
//                                        InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOSTREAMRECORD.getNumber(), checks);
                                jni.mediaPlayOperate(pushMediaid, checks, 0, 0, 0, InterfaceMacro.Pb_MeetPlayFlag.Pb_MEDIA_PLAYFLAG_ZERO.getNumber());
                                builder.dismiss();
                            }
                        });
                        builder.getView(R.id.stop_push).setOnClickListener(v -> {
                            List<Integer> checks = pushOnLineMemberAdapter.getChecks();
                            checks.addAll(pushOnLineProjectorAdapter.getChecks());
                            if (checks.isEmpty()) {
                                ToastUtils.showShort(R.string.please_choose_push);
                            } else {
                                jni.stopResourceOperate(tempRes, checks);
                                builder.dismiss();
                            }
                        });
                        builder.getView(R.id.cancel).setOnClickListener(v -> builder.dismiss());
                    }

                    @Override
                    public void setOnDismissListener(PopUtils.PopBuilder builder) {
                        pushPopIsshowing = false;
                    }
                });
    }

    /**
     * 到了这一步 memberInfos 参会人数据不可能是空
     */
    private void showVoteResultsPop() {
        voteResultPop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.vote_result, null);
        voteResultPop.setTag("voteResultPop");
        voteResultViewHolder holder = new voteResultViewHolder(voteResultPop);
        holder_event(holder);
        showPop(pop, voteResultPop, postilParams);
    }

    private void clearResultData() {
        if (voteResultAdapter != null) voteResultAdapter = null;
        if (submitMemberData != null) submitMemberData = null;
        if (optionAdapter != null) optionAdapter = null;
        if (voteResultData != null) voteResultData = null;
        if (voteResultInfo != null) voteResultInfo = null;
        if (currentVoteInfo != null) currentVoteInfo = null;
        if (chartDatas != null) chartDatas = null;
    }

    private void setSelectBtn(int i, List<Button> btns) {
        for (int j = 0; j < btns.size(); j++) {
            btns.get(j).setSelected(j == i);
        }
    }

    private void holder_event(voteResultViewHolder holder) {
        holder.pieChart.setVisibility(View.GONE);
        holder.voteTitleRv.setLayoutManager(new LinearLayoutManager(fabContext));
        holder.voteTitleRv.setAdapter(voteResultAdapter);
        optionRv = holder.optionRv;
        optionRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        List<Button> btns = new ArrayList<>();
        btns.add(holder.vote);
        btns.add(holder.election);
        btns.add(holder.questionnaire);
        holder.closeIv.setOnClickListener(v -> {
            showPop(voteResultPop, mImageView, mParams);
            clearResultData();
        });
        //投票
        holder.vote.setOnClickListener(v -> {
            setSelectBtn(0, btns);
            voteResultData.clear();
            for (int i = 0; i < voteResultInfo.size(); i++) {
                InterfaceVote.pbui_Item_MeetVoteDetailInfo detailInfo = voteResultInfo.get(i);
                if (detailInfo.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote.getNumber()) {
                    voteResultData.add(detailInfo);
                }
            }
            voteResultAdapter.notifyDataSetChanged();
        });
        //选举
        holder.election.setOnClickListener(v -> {
            setSelectBtn(1, btns);
            voteResultData.clear();
            for (int i = 0; i < voteResultInfo.size(); i++) {
                InterfaceVote.pbui_Item_MeetVoteDetailInfo detailInfo = voteResultInfo.get(i);
                if (detailInfo.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election.getNumber()) {
                    voteResultData.add(detailInfo);
                }
            }
            voteResultAdapter.notifyDataSetChanged();
        });
        //问卷
        holder.questionnaire.setOnClickListener(v -> {
            setSelectBtn(2, btns);
            voteResultData.clear();
            for (int i = 0; i < voteResultInfo.size(); i++) {
                InterfaceVote.pbui_Item_MeetVoteDetailInfo detailInfo = voteResultInfo.get(i);
                if (detailInfo.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_questionnaire.getNumber()) {
                    voteResultData.add(detailInfo);
                }
            }
            voteResultAdapter.notifyDataSetChanged();
        });
        if (!voteResultData.isEmpty()) {
            clickVote(holder, 0);
        }
        //设置默认点击第一个投票按钮
        holder.vote.performClick();
        voteResultAdapter.setItemSelectListener((posion, view) -> {
            clickVote(holder, posion);
        });
    }

    private void clickVote(voteResultViewHolder holder, int posion) {
        //先清空数据,有可能当前这条投票没有提交数据
        if (submitMemberData == null) submitMemberData = new ArrayList<>();
        else submitMemberData.clear();
        if (optionAdapter == null) {
            optionAdapter = new VoteOptionResultAdapter(getApplicationContext(), submitMemberData);
            optionRv.setAdapter(optionAdapter);
        } else optionAdapter.notifyDataSetChanged();
        LogUtil.d(TAG, "holder_event: 点击标题item,清空投票结果数据");
        holder.voteOptionA.setVisibility(View.GONE);
        holder.voteOptionB.setVisibility(View.GONE);
        holder.voteOptionC.setVisibility(View.GONE);
        holder.voteOptionD.setVisibility(View.GONE);
        holder.voteOptionE.setVisibility(View.GONE);
        //饼状图形 需要先隐藏
        holder.pieChart.setVisibility(View.GONE);
        countPre = 0;//一共占用的百分比数
        //获取当前点击的投票的信息
        currentVoteInfo = voteResultData.get(posion);

        int voteid = currentVoteInfo.getVoteid();
        int mode = currentVoteInfo.getMode();
        int type = currentVoteInfo.getType();
        String content = currentVoteInfo.getContent().toStringUtf8();
        int votestate = currentVoteInfo.getVotestate();
        int maintype = currentVoteInfo.getMaintype();
        boolean isVote = maintype == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote.getNumber();
        String modestr = mode == 0 ? getString(R.string.anonymous) : getString(R.string.remember);

        String typestr = getTypeStr(type);
        String votestatestr = getVoteStateStr(votestate);
        holder.vote_type_tv.setText("( " + typestr + "  " + modestr + "  " + votestatestr + " )");
        holder.vote_title_tv.setText(content);
        if (mode == 1) {//记名
            LogUtil.d(TAG, "holder_event: 查询指定投票提交人");
            queryOneVoteSubmitter = true;
            fun_queryOneVoteSubmitter(voteid);
        }
        holder.member_count_tv.setVisibility(View.GONE);
        holder.vote_member_count_tv.setText(getString(R.string.vote_member_count, 0));
        /** **** **  过滤未发起的投票  ** **** **/
        if (votestate != InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote.getNumber()) {
            holder.member_count_tv.setVisibility(View.VISIBLE);
            String yingDaoStr = "";//应到
            String shiDaoStr = "";//实到
            String yiTouStr = "";//已投
            String weiTouStr = "";//未投
            InterfaceBase.pbui_CommonInt32uProperty yingDaoInfo = jni.queryVoteSubmitterProperty(voteid, 0, InterfaceMacro.Pb_MeetVotePropertyID.Pb_MEETVOTE_PROPERTY_ATTENDNUM.getNumber());
            InterfaceBase.pbui_CommonInt32uProperty yiTouInfo = jni.queryVoteSubmitterProperty(voteid, 0, InterfaceMacro.Pb_MeetVotePropertyID.Pb_MEETVOTE_PROPERTY_VOTEDNUM.getNumber());
            InterfaceBase.pbui_CommonInt32uProperty shiDaoInfo = jni.queryVoteSubmitterProperty(voteid, 0, InterfaceMacro.Pb_MeetVotePropertyID.Pb_MEETVOTE_PROPERTY_CHECKINNUM.getNumber());
            int yingDao = yingDaoInfo == null ? 0 : yingDaoInfo.getPropertyval();
            int yiTou = yiTouInfo == null ? 0 : yiTouInfo.getPropertyval();
            int shiDao = shiDaoInfo == null ? 0 : shiDaoInfo.getPropertyval();
            yingDaoStr = yingDao + "";
            yiTouStr = yiTou + "";
            shiDaoStr = shiDao + "";
            weiTouStr = (yingDao - yiTou) + "";
            LogUtil.e(TAG, "FabService.holder_event :  应到人数 --> " + yingDao + ", 已投人数= " + yiTou);
            holder.member_count_tv.setText(getString(R.string.vote_result_count, yingDaoStr, shiDaoStr, yiTouStr, weiTouStr));
            if (isVote) {
                holder.vote_type_top_ll.setVisibility(View.GONE);
                holder.vote_member_count_tv.setVisibility(View.VISIBLE);
                holder.vote_member_count_tv.setText(getString(R.string.vote_member_count, yiTou));
            } else {
                holder.vote_type_top_ll.setVisibility(View.VISIBLE);
                holder.vote_member_count_tv.setVisibility(View.GONE);
            }
        }
        /** **** **  itemList的item个数就是可显示的图表颜色个数  ** **** **/
        List<InterfaceVote.pbui_SubItem_VoteItemInfo> itemList = currentVoteInfo.getItemList();
        int count = getCount(itemList);
        if (chartDatas == null) chartDatas = new ArrayList<>();
        else chartDatas.clear();
        holder.vote_option_color_a.setBackgroundColor(isVote ? getColor(R.color.chart_color_green) : getColor(R.color.chart_color_red));
        holder.vote_option_color_b.setBackgroundColor(isVote ? getColor(R.color.chart_color_red) : getColor(R.color.chart_color_green));
        holder.vote_option_color_c.setBackgroundColor(isVote ? getColor(R.color.chart_color_yellow) : getColor(R.color.chart_color_blue));
        for (int i = 0; i < itemList.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo itemInfo = itemList.get(i);
            String option = MyUtils.b2s(itemInfo.getText());//该选项的内容
            int selcnt = itemInfo.getSelcnt();//该选项拥有的投票数
            if (!TextUtils.isEmpty(option)) {
                if (i == 0) {
                    holder.voteOptionA.setVisibility(View.VISIBLE);
                    holder.optionA.setText(getString(R.string.vote_count, option, selcnt + ""));
                    setChartData(count, selcnt, getColor(R.color.black), isVote ? getColor(R.color.chart_color_green) : getColor(R.color.chart_color_red));
                } else if (i == 1) {
                    holder.voteOptionB.setVisibility(View.VISIBLE);
                    holder.optionB.setText(getString(R.string.vote_count, option, selcnt + ""));
                    setChartData(count, selcnt, getColor(R.color.black), isVote ? getColor(R.color.chart_color_red) : getColor(R.color.chart_color_green));
                } else if (i == 2) {
                    holder.voteOptionC.setVisibility(View.VISIBLE);
                    holder.optionC.setText(getString(R.string.vote_count, option, selcnt + ""));
                    setChartData(count, selcnt, getColor(R.color.black), isVote ? getColor(R.color.chart_color_yellow) : getColor(R.color.chart_color_blue));
                } else if (i == 3) {
                    holder.voteOptionD.setVisibility(View.VISIBLE);
                    holder.optionD.setText(getString(R.string.vote_count, option, selcnt + ""));
                    setChartData(count, selcnt, getColor(R.color.black), getColor(R.color.chart_color_aqua));
                } else if (i == 4) {
                    holder.voteOptionE.setVisibility(View.VISIBLE);
                    holder.optionE.setText(getString(R.string.vote_count, option, selcnt + ""));
                    setChartData(count, selcnt, getColor(R.color.black), getColor(R.color.chart_color_pink));
                }
            }
        }
        if (countPre > 0 && countPre < 100) {//因为没有除尽,有余下的空白区域
            ChartData lastChartData = chartDatas.get(chartDatas.size() - 1);//先获取到最后一条的数据
            chartDatas.remove(chartDatas.size() - 1);//删除掉集合中的最后一个
            //使用原数据重新添加,但是修改所占比例大小,这样就能确保不会出现空白部分
            chartDatas.add(new ChartData(lastChartData.getDisplayText(),
                    lastChartData.getPartInPercent() + (100 - countPre),
                    lastChartData.getTextColor(), lastChartData.getBackgroundColor()));
        }
        //如果没有数据会报错
        if (chartDatas.isEmpty()) {
            chartDatas.add(new ChartData(getResources().getString(R.string.null_str), 100, Color.parseColor("#FFFFFF"), Color.parseColor("#676767")));
        }
        holder.pieChart.setChartData(chartDatas);
        holder.pieChart.setVisibility(View.VISIBLE);
        voteResultAdapter.setSelect(currentVoteInfo.getVoteid());
    }

    private void fun_queryOneVoteSubmitter(int voteid) {
        try {
            InterfaceVote.pbui_Type_MeetVoteSignInDetailInfo object3 = jni.queryOneVoteSubmitter(voteid);
            if (object3 == null) return;
            if (!queryOneVoteSubmitter) return;
            queryOneVoteSubmitter = false;
            LogUtil.d(TAG, "fun_queryOneVoteSubmitter: 收到指定投票提交人数据,更新投票结果adapter");
            List<InterfaceVote.pbui_SubItem_VoteItemInfo> itemList2 = currentVoteInfo.getItemList();
            List<Integer> ids = new ArrayList<>();
            List<InterfaceVote.pbui_Item_MeetVoteSignInDetailInfo> itemList1 = object3.getItemList();
            LogUtil.e(TAG, "FabService.receiveVoteInfo :  所有投票人员数量 --> " + itemList1.size());
            for (int i = 0; i < itemList1.size(); i++) {
                InterfaceVote.pbui_Item_MeetVoteSignInDetailInfo pbui_item_meetVoteSignInDetailInfo = itemList1.get(i);
                String chooseText = "";
                String name = "";
                int shidao = 0;
                int selcnt1 = pbui_item_meetVoteSignInDetailInfo.getSelcnt();
                int i1 = selcnt1 & Macro.PB_VOTE_SELFLAG_CHECKIN;
                LogUtil.e(TAG, "FabService.fun_queryOneVoteSubmitter :  selcnt1 --> " + selcnt1 + "， 相与的结果= " + i1);
                if (i1 == Macro.PB_VOTE_SELFLAG_CHECKIN) {
                    shidao++;
                }
                LogUtil.e(TAG, "FabService.fun_queryOneVoteSubmitter :  实到人数 --> " + shidao);
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
                        LogUtil.e(TAG, "FabService.fun_queryOneVoteSubmitter :  选中了第 " + selectedItem + " 项");
                        for (int k = 0; k < itemList2.size(); k++) {
                            if (k == selectedItem) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo pbui_subItem_voteItemInfo = itemList2.get(k);
                                String text = MyUtils.b2s(pbui_subItem_voteItemInfo.getText());
                                if (chooseText.length() == 0) chooseText = text;
                                else chooseText += " | " + text;
                            }
                        }
                    }
                }
                submitMemberData.add(new VoteResultSubmitMember(id1, name, chooseText));
            }
            optionAdapter.notifyDataSetChanged();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
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

    /**
     * @param count  当前投票的结果总数
     * @param selcnt 当前投票选项的总数
     * @param colora
     * @param colorb 区域的颜色
     * @return 返回当前所使用的百分比数值（用来最后计算如果是不是100%）
     */
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
        LogUtil.e(TAG, "FabService.getCount :  当前投票票数总数 --> " + count);
        return count;
    }

    private void initVoteResultAdapter(InterfaceVote.pbui_Type_MeetVoteDetailInfo voteDetailInfo) {
        voteResultInfo = voteDetailInfo.getItemList();
        if (voteResultData == null) voteResultData = new ArrayList<>();
        else voteResultData.clear();
        for (int i = 0; i < voteResultInfo.size(); i++) {
            InterfaceVote.pbui_Item_MeetVoteDetailInfo detailInfo = voteResultInfo.get(i);
            int maintype = detailInfo.getMaintype();
            if (maintype == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote.getNumber()) {
                //投票
                voteResultData.add(detailInfo);
            }
        }
        if (voteResultAdapter == null)
            voteResultAdapter = new VoteTitleResultAdapter(fabContext, voteResultData);
        else voteResultAdapter.notifyDataSetChanged();
    }

    private void showChoose(InterfaceVote.pbui_Item_MeetOnVotingDetailInfo voteInfo) {
        mChoosePop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.compere_vote_pop, null);
        mChoosePop.setTag("mChoosePop");
        ChooseViewHolder holder = new ChooseViewHolder(mChoosePop, voteInfo);
        ChooseHolderEvent(holder, voteInfo);
        wm.addView(mChoosePop, postilParams);
        mChoosePopIsShow = true;
    }

    private void ChooseHolderEvent(final ChooseViewHolder holder, final InterfaceVote.pbui_Item_MeetOnVotingDetailInfo voteInfo) {
        int type = voteInfo.getType();
        String typestr = "";
        //存放选择框
        final ArrayList<CheckBox> btns = new ArrayList<>();
        btns.add(holder.chooseA);
        btns.add(holder.chooseB);
        btns.add(holder.chooseC);
        btns.add(holder.chooseD);
        btns.add(holder.chooseE);
        int selectcount = voteInfo.getSelectcount();
        LogUtil.i(TAG, "ChooseHolderEvent 设置可选择的个数=" + selectcount);
        //根据 type 设置可选择的个数
        switch (type) {
            case 0://多选
                setMaxSelect(selectcount - 1, btns, holder);
                typestr = getString(R.string.pb_vote_type_many);
                break;
            case 1://单选
                setMaxSelect(1, btns, holder);
                typestr = getString(R.string.pb_vote_type_single);
                break;
            case 2://5 4
                setMaxSelect(4, btns, holder);
                typestr = getString(R.string.pb_vote_type_4_5);
                break;
            case 3://5 3
                setMaxSelect(3, btns, holder);
                typestr = getString(R.string.pb_vote_type_3_5);
                break;
            case 4://5 2
                setMaxSelect(2, btns, holder);
                typestr = getString(R.string.pb_vote_type_2_5);
                break;
            case 5://3 2
                setMaxSelect(2, btns, holder);
                typestr = getString(R.string.pb_vote_type_2_3);
                break;
        }
        String modestr = voteInfo.getMode() == 1 ? getString(R.string.registered) : getString(R.string.anonymous);
        holder.tvTitle.setText(getString(R.string.vote_type, MyUtils.b2s(voteInfo.getContent()), typestr, modestr));
        voteTimeouts = voteInfo.getTimeouts();
        int selectItem = 0 | Macro.PB_VOTE_SELFLAG_CHECKIN;
        LogUtil.e(TAG, "FabService.ChooseHolderEvent :  发送投票签到 --> " + selectItem);
        currentVoteId = voteInfo.getVoteid();
        SubmitVoteBean sub = new SubmitVoteBean(currentVoteId, 1/*voteInfo.getSelect()*/, selectItem);
        jni.submitVoteResult(sub);
        LogUtil.e(TAG, "FabService.ChooseHolderEvent :  投票倒计时时间 --> " + voteTimeouts);
        if (voteTimeouts == 0) voteTimeouts = 60;
        if (voteTimeouts <= 1800) {
            holder.vote_chronometer.setBase(SystemClock.elapsedRealtime());
            holder.vote_chronometer.start();
            holder.vote_chronometer.setOnChronometerTickListener(chronometer -> {
                voteTimeouts--;//单位是秒
                if (voteTimeouts <= 0) {
                    if (voteInfo.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote_VALUE) {
                        //投票倒计时结束还没有进行选择，则默认弃权
                        SubmitVoteBean submitVoteBean = new SubmitVoteBean(currentVoteId, voteInfo.getSelectcount(), 4);
                        jni.submitVoteResult(submitVoteBean);
                    }
                    chronometer.stop();
                    if (mVoteEnsureViewIsShow) {
                        wm.removeView(mVoteEnsureView);
                        mVoteEnsureViewIsShow = false;
                    }
                    wm.removeView(mChoosePop);
                    mChoosePopIsShow = false;
                    currentVoteId = -1;
                } else {
//                    String str = DateUtil.formatSeconds(voteTimeouts);
                    String str = String.valueOf(voteTimeouts);
                    chronometer.setText(str);
                }
            });
        } else {
            holder.vote_chronometer.setVisibility(View.GONE);
        }
        if (voteInfo.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote_VALUE) {
            holder.vote_favour_tv.setOnClickListener(v -> showEnsureView(1, voteInfo));
            holder.vote_against_tv.setOnClickListener(v -> showEnsureView(2, voteInfo));
            holder.vote_waiver_tv.setOnClickListener(v -> showEnsureView(4, voteInfo));
        } else {
            //取消按钮
            holder.compereVotePopCancel.setOnClickListener(view -> {
                wm.removeView(mChoosePop);
                mChoosePopIsShow = false;
                currentVoteId = -1;
            });
            //确定按钮
            holder.compereVotePopEnsure.setOnClickListener(view -> {
                List<Integer> choose = new ArrayList<>();
                int selectcount1 = voteInfo.getSelectcount();
                for (int i = 0; i < selectcount1; i++) {
                    CheckBox checkBox = btns.get(i);
                    if (checkBox.isChecked()) {
                        switch (i) {
                            case 0://第0项被选中
                                choose.add(1);
                                break;
                            case 1://第1项被选中
                                choose.add(2);
                                break;
                            case 2://第2项被选中
                                choose.add(4);
                                break;
                            case 3://第3项被选中
                                choose.add(8);
                                break;
                            case 4://第4项被选中
                                // TODO: 2018/2/27   10000 从右往左第五个  10000的十进制为 16
                                choose.add(16);
                                break;
                        }
                    }
                }
                if (choose.size() > 0) {//有选中项才执行
                    int bb = 0;
                    for (int j = 0; j < choose.size(); j++) {
                        bb = bb + choose.get(j);
                    }
                    wm.removeView(mChoosePop);
                    mChoosePopIsShow = false;
                    SubmitVoteBean submitVoteBean = new SubmitVoteBean(currentVoteId, selectcount1, bb);
                    //提交投票结果
                    jni.submitVoteResult(submitVoteBean);
                    currentVoteId = -1;
                }
            });
        }
    }

    /**
     * 弹出是否确定选择该投票进行提交
     *
     * @param answer =1赞成，=2反对，=4弃权
     * @param vote   当前投票
     */
    private void showEnsureView(int answer, InterfaceVote.pbui_Item_MeetOnVotingDetailInfo vote) {
        mVoteEnsureView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.vote_ensure_view, null);
        mVoteEnsureView.setTag("mVoteEnsureView");
        VoteSubmitViewHolder holder = new VoteSubmitViewHolder(mVoteEnsureView);
        VoteSubmitViewHolderEvent(holder, answer, vote);
        wm.addView(mVoteEnsureView, params);
        mVoteEnsureViewIsShow = true;
    }

    private void VoteSubmitViewHolderEvent(VoteSubmitViewHolder holder, int answer, InterfaceVote.pbui_Item_MeetOnVotingDetailInfo vote) {
        holder.vote_submit_ensure.setOnClickListener(v -> {
            SubmitVoteBean submitVoteBean = new SubmitVoteBean(currentVoteId, vote.getSelectcount(), answer);
            jni.submitVoteResult(submitVoteBean);
            wm.removeView(mVoteEnsureView);
            mVoteEnsureViewIsShow = false;
            wm.removeView(mChoosePop);
            mChoosePopIsShow = false;
            currentVoteId = -1;
        });
        holder.vote_submit_cancel.setOnClickListener(v -> {
            wm.removeView(mVoteEnsureView);
            mVoteEnsureViewIsShow = false;
        });
    }

    private void setMaxSelect(final int maxSelect, final ArrayList<CheckBox> btns, final ChooseViewHolder holder) {
        holder.chooseA.setOnClickListener(view -> {
            if (maxSelect == 1) chooseOne(btns, 0);
            else CheckBoxEvent(btns, maxSelect);
        });
        holder.chooseB.setOnClickListener(view -> {
            if (maxSelect == 1) chooseOne(btns, 1);
            else CheckBoxEvent(btns, maxSelect);
        });
        holder.chooseC.setOnClickListener(view -> {
            if (maxSelect == 1) chooseOne(btns, 2);
            else CheckBoxEvent(btns, maxSelect);
        });
        holder.chooseD.setOnClickListener(view -> {
            if (maxSelect == 1) chooseOne(btns, 3);
            else CheckBoxEvent(btns, maxSelect);
        });
        holder.chooseE.setOnClickListener(view -> {
            if (maxSelect == 1) chooseOne(btns, 4);
            else CheckBoxEvent(btns, maxSelect);
        });
    }

    private void chooseOne(ArrayList<CheckBox> btns, int index) {
        for (int i = 0; i < btns.size(); i++) {
            if (i == index) btns.get(i).setChecked(true);
            else btns.get(i).setChecked(false);
        }
    }

    private void CheckBoxEvent(final ArrayList<CheckBox> btns, final int maxSelect) {
        final int nowSelect = getNowSelect(btns);
        if (maxSelect > nowSelect) {
            //如果当前选中的个数 小于 可选的个数
            for (int i = 0; i < btns.size(); i++) {
                if (!(btns.get(i).isChecked())) {
                    //就将未选中的选项设置成 可点击状态
                    btns.get(i).setClickable(true);
                }
            }
        } else/* if (maxSelect == nowSelect)*/ {
            //如果当前选中的个数 等于 可选的个数
            ToastUtils.showShort(R.string.tip_most_can_choose, maxSelect + "");
            for (int i = 0; i < btns.size(); i++) {
                if (!(btns.get(i).isChecked())) {
                    //就将未选中的选项设置成 不可点击状态
                    btns.get(i).setClickable(false);
                }
            }
        }
    }

    private int getNowSelect(ArrayList<CheckBox> btns) {
        int nowSelect = 0;
        for (int i = 0; i < btns.size(); i++) {
            if (btns.get(i).isChecked()) {
                //如果未true 选中的话就将 nowSelect加1
                nowSelect++;
            }
        }
        return nowSelect;
    }

    public static class ChooseViewHolder {
        public View rootView;
        public TextView tvTitle;
        public TextView vote_type_tv;
        public Chronometer vote_chronometer;
        public LinearLayout vote_linear;
        public ImageView vote_favour_tv;
        public ImageView vote_against_tv;
        public ImageView vote_waiver_tv;

        public LinearLayout election_linear;
        public CheckBox chooseA;
        public CheckBox chooseB;
        public CheckBox chooseC;
        public CheckBox chooseD;
        public CheckBox chooseE;

        public LinearLayout election_linear_ensure;
        public Button compereVotePopEnsure;
        public Button compereVotePopCancel;

        public ChooseViewHolder(View rootView, InterfaceVote.pbui_Item_MeetOnVotingDetailInfo voteInfo) {
            this.rootView = rootView;
            this.tvTitle = rootView.findViewById(R.id.tv_title);
            this.vote_type_tv = rootView.findViewById(R.id.vote_type_tv);
            this.vote_chronometer = rootView.findViewById(R.id.vote_chronometer);
            //投票布局
            this.vote_linear = rootView.findViewById(R.id.vote_linear);
            this.vote_favour_tv = rootView.findViewById(R.id.vote_favour_tv);
            this.vote_against_tv = rootView.findViewById(R.id.vote_against_tv);
            this.vote_waiver_tv = rootView.findViewById(R.id.vote_waiver_tv);
            //选举布局
            this.election_linear = rootView.findViewById(R.id.election_linear);
            this.chooseA = rootView.findViewById(R.id.chooseA);
            this.chooseB = rootView.findViewById(R.id.chooseB);
            this.chooseC = rootView.findViewById(R.id.chooseC);
            this.chooseD = rootView.findViewById(R.id.chooseD);
            this.chooseE = rootView.findViewById(R.id.chooseE);
            this.election_linear_ensure = rootView.findViewById(R.id.election_linear_ensure);
            this.compereVotePopEnsure = rootView.findViewById(R.id.compere_votePop_ensure);
            this.compereVotePopCancel = rootView.findViewById(R.id.compere_votePop_cancel);
            if (voteInfo.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote_VALUE) {
                //投票
                vote_linear.setVisibility(View.VISIBLE);
                election_linear.setVisibility(View.GONE);
                election_linear_ensure.setVisibility(View.GONE);
            } else {//选举和问卷调查
                vote_linear.setVisibility(View.GONE);
                election_linear.setVisibility(View.VISIBLE);
                election_linear_ensure.setVisibility(View.VISIBLE);
                //获取选项信息
                List<ByteString> optionInfo = voteInfo.getTextList();
                for (int i = 0; i < optionInfo.size(); i++) {
                    String text = MyUtils.b2s(optionInfo.get(i));
                    if (i == 0) {
                        this.chooseA.setText(text);
//                        this.chooseA.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
                    } else if (i == 1) {
                        this.chooseB.setText(text);
//                        this.chooseB.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
                    } else if (i == 2) {
                        this.chooseC.setText(text);
//                        this.chooseC.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
                    } else if (i == 3) {
                        this.chooseD.setText(text);
//                        this.chooseD.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
                    } else if (i == 4) {
                        this.chooseE.setText(text);
//                        this.chooseE.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                }
                this.chooseE.setVisibility(View.GONE);
                this.chooseD.setVisibility(View.GONE);
                this.chooseC.setVisibility(View.GONE);
                this.chooseB.setVisibility(View.GONE);
                this.chooseA.setVisibility(View.GONE);
                // 有多少个选项就展示多少个
                int selectcount = voteInfo.getSelectcount();
                switch (selectcount) {
                    case 5:
                        this.chooseE.setVisibility(View.VISIBLE);
                        this.chooseD.setVisibility(View.VISIBLE);
                        this.chooseC.setVisibility(View.VISIBLE);
                        this.chooseB.setVisibility(View.VISIBLE);
                        this.chooseA.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        this.chooseD.setVisibility(View.VISIBLE);
                        this.chooseC.setVisibility(View.VISIBLE);
                        this.chooseB.setVisibility(View.VISIBLE);
                        this.chooseA.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        this.chooseC.setVisibility(View.VISIBLE);
                        this.chooseB.setVisibility(View.VISIBLE);
                        this.chooseA.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        this.chooseB.setVisibility(View.VISIBLE);
                        this.chooseA.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        this.chooseA.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    }

    private void showScreenPop() {
        screenPop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_screen, null);
        screenPop.setTag("screenPop");
        ScreenViewHolder screenHolder = new ScreenViewHolder(screenPop);
        Screen_Event(screenHolder);
    }

    private void showProjectPop() {
        projectPop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_projector, null);
        projectPop.setTag("projectPop");
        ProjectViewHolder proHolder = new ProjectViewHolder(projectPop);
        proHolder_Event(proHolder);
    }

    private void fun_queryVote() {
        try {
            InterfaceVote.pbui_Type_MeetVoteDetailInfo allVoteResults = jni.queryVote();
            if (allVoteResults == null) return;
            //确保是点击了快捷方式中的投票结果
            if (!clickqueryVoteResult) return;
            clickqueryVoteResult = false;
            LogUtil.d(TAG, "fun_queryVote: 收到所有投票数据");
            initVoteResultAdapter(allVoteResults);
            voteResult = true;
            fun_queryAttendPeople();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryCanJoin() {
        try {
            InterfaceDevice.pbui_Type_DeviceResPlay object = jni.queryCanJoin();
            if (object == null) {
                ToastUtils.showShort(R.string.tip_no_screen_can_join);
                return;
            }
            LogUtil.e(TAG, "FabService.fun_queryCanJoin :  收到可加入同屏数据 --> ");
            List<InterfaceDevice.pbui_Item_DeviceResPlay> pdevList = object.getPdevList();
            if (canJoinMember == null) canJoinMember = new ArrayList<>();
            else canJoinMember.clear();
            if (canJoinPro == null) canJoinPro = new ArrayList<>();
            else canJoinPro.clear();
            for (int i = 0; i < pdevList.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceResPlay item = pdevList.get(i);
                int devceid = item.getDevceid();//设备ID
                int n = devceid & Macro.DEVICE_MEET_ID_MASK;
                if (n == Macro.DEVICE_MEET_PROJECTIVE) {// 可加入同屏的投影机
                    for (int j = 0; j < deviceInfos.size(); j++)
                        if (deviceInfos.get(j).getDevcieid() == devceid)
                            canJoinPro.add(deviceInfos.get(j));
                } else {// 可加入同屏的参会人
                    int memberid = item.getMemberid();//参会人员ID
                    String name = MyUtils.b2s(item.getName());//参会人员名称
                    LogUtil.e(TAG, "FabService.fun_queryCanJoin :  devceid --> " + devceid + "   memberid:" + memberid + "   name:" + name);
                    for (int j = 0; j < memberInfos.size(); j++)
                        if (memberInfos.get(j).getPersonid() == memberid)
                            canJoinMember.add(new DevMember(memberInfos.get(j), devceid));
                }
                if (canJoinProAdapter == null)
                    canJoinProAdapter = new CanJoinProAdapter(canJoinPro);
                if (canJoinMemberAdapter == null)
                    canJoinMemberAdapter = new CanJoinMemberAdapter(canJoinMember);
                canJoinProAdapter.notifyDataSetChanged();
                canJoinProAdapter.notifyChecks();
                canJoinMemberAdapter.notifyDataSetChanged();
                canJoinMemberAdapter.notifyChecks();
                if (!canJoinPopIsShow) {
                    One_JoinScreen_Pop();
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    //投影机列表
    private void showProRlPop(View view) {
        proListPop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_choose_pro, null);
        proListPop.setTag("proListPop");
        ProListViewHolder proListHolder = new ProListViewHolder(proListPop);
        proListHolder_Event(proListHolder);
        if (view != null) {
            showPop(view, proListPop);
        }
    }

    //投影机列表事件
    private void proListHolder_Event(final ProListViewHolder holder) {
        holder.pro_title.setText(startPro ? getString(R.string.start_pro) : getString(R.string.button_stop_projection));
        holder.cb_pro_mandatory.setVisibility(startPro ? View.VISIBLE : View.INVISIBLE);
        allProjectorAdapter.setItemClick((view, posion) -> {
            int devId = allProjectors.get(posion).getDevcieid();
            allProjectorAdapter.setCheck(devId);
            holder.allchoose.setChecked(allProjectorAdapter.isAllCheck());
            allProjectorAdapter.notifyDataSetChanged();
        });
        holder.allchoose.setOnClickListener(v -> {
            boolean checked = holder.allchoose.isChecked();
            holder.allchoose.setChecked(checked);
            allProjectorAdapter.setAllCheck(checked);
        });
        holder.ensure.setOnClickListener(view -> {
            int srcdeviceid;
            int subid;
            if (startPro) {
                /** **** **  判断是否从视屏列表页面打开的  ** **** **/
                if (openProjector) {
                    srcdeviceid = videoInfo.getVideoInfo().getDeviceid();
                    subid = videoInfo.getVideoInfo().getSubid();
                    openProjector = false;
                } else {
                    subid = 2;
                    srcdeviceid = Values.localDevId;
                }
                if (allProjectorAdapter != null) {
                    applyProjectionIds = allProjectorAdapter.getChecks();
                    if (applyProjectionIds.size() > 0) {
                        if (applyProjectionIds.contains(srcdeviceid)) {
                            applyProjectionIds.remove(applyProjectionIds.indexOf(srcdeviceid));
                        }
                        int value = holder.cb_pro_mandatory.isChecked() ? InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE : 0;
                        jni.streamPlay(srcdeviceid, subid, value, tempRes, applyProjectionIds);
                        if (openProjectpopFromPostilPop) {//判断是否是在批注图片页面打开的
                            openProjectpopFromPostilPop = false;//重新设置为false
                            wm.removeView(proListPop);
                            proListPopIsShow = false;
                        } else {
                            showPop(proListPop, mImageView, mParams);
                        }
                    } else {
                        ToastUtils.showShort(R.string.tip_select_device);
                    }
                }
            } else {//结束投影操作
                LogUtil.e(TAG, "FabService.onClick :  结束投影操作... --> ");
                applyProjectionIds = allProjectorAdapter.getChecks();
                if (applyProjectionIds.size() > 0) {
                    jni.stopResourceOperate(tempRes, applyProjectionIds);
                }
                if (openProjectpopFromPostilPop) {
                    openProjectpopFromPostilPop = false;
                    wm.removeView(proListPop);
                    proListPopIsShow = false;
                } else {
                    showPop(proListPop, mImageView, mParams);
                }
            }
        });
        holder.cancel.setOnClickListener(view -> {
            if (openProjectpopFromPostilPop) {//判断是否是在批注页面打开的
                openProjectpopFromPostilPop = false;//重新设置为false
                wm.removeView(proListPop);
                proListPopIsShow = false;
            } else {
                showPop(proListPop, mImageView, mParams);
                if (openProjector) openProjector = false;
            }
        });
        holder.allchoose.performClick();
    }

    //选择参会人和投影机
    private void showPlayerPop(View view) {
        choosePlayerPop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_choose_member, null);
        choosePlayerPop.setTag("choosePlayerPop");
        PlayerViewHolder holder = new PlayerViewHolder(choosePlayerPop);
        PlayerEvent(holder);
        if (view != null) {
            showPop(view, choosePlayerPop);
        }
    }

    //选择参会人弹出框事件监听
    private void PlayerEvent(final PlayerViewHolder holder) {
        holder.cb_mandatory.setVisibility(startScr ? View.VISIBLE : View.INVISIBLE);
        holder.title.setText(startScr ? getString(R.string.start_screen) : getString(R.string.stop_screen));
        //参会人item点击事件
        onLineMemberAdapter.setItemClick((view, posion) -> {
            int devId = onLineMember.get(posion).getDevId();
            onLineMemberAdapter.setCheck(devId);
            holder.players_all_cb.setChecked(onLineMemberAdapter.isAllCheck());
            onLineMemberAdapter.notifyDataSetChanged();
        });
        //投影机item点击事件
        onLineProjectorAdapter.setItemClick((view, posion) -> {
            int devId = onLineProjectors.get(posion).getDevcieid();
            onLineProjectorAdapter.setCheck(devId);
            holder.projector_all_cb.setChecked(onLineProjectorAdapter.isAllCheck());
            onLineProjectorAdapter.notifyDataSetChanged();
        });
        //全选按钮状态监听
        holder.players_all_cb.setOnClickListener(v -> {
            boolean checked = holder.players_all_cb.isChecked();
            holder.players_all_cb.setChecked(checked);
            onLineMemberAdapter.setAllCheck(checked);
        });
        // 投影机全选 选择框
        holder.projector_all_cb.setOnClickListener(v -> {
            boolean checked = holder.projector_all_cb.isChecked();
            holder.projector_all_cb.setChecked(checked);
            onLineProjectorAdapter.setAllCheck(checked);
        });
        //确定按钮
        holder.player_pop_ensure.setOnClickListener(view -> {
            int subid;  // 2：屏幕 3：摄像头
            int srcdeviceid;//需要采集的设备ID
            if (startScr) {
                /* **** **  开启同屏操作  ** **** */
                allScreenDevIds = onLineMemberAdapter.getChecks();
                allScreenDevIds.addAll(onLineProjectorAdapter.getChecks());
                LogUtil.e(TAG, "FabService.onClick :  添加参会人和投影机 --> " + allScreenDevIds.toString());
                LogUtil.e(TAG, "是否从视屏直播点击 --->>> " + openScreen);
                if (openScreen) {
                    srcdeviceid = videoInfo.getVideoInfo().getDeviceid();
                    subid = videoInfo.getVideoInfo().getSubid();
                    openScreen = false;
                } else {
                    srcdeviceid = Values.localDevId;//要采集的屏幕源是自己设备的屏幕
                    subid = 2;// 2 屏幕,3 摄像
                }
                LogUtil.e(TAG, "需要采集屏幕还是摄像头 --->>> " + subid);
                LogUtil.e(TAG, "FabService.onClick :  最终 --> " + allScreenDevIds.toString());
                /* ************ ******  流播放  ******0x1080004  0x1100003************ */
                if (allScreenDevIds.size() > 0) {//避免自己观看自己的屏幕
                    if (allScreenDevIds.contains(srcdeviceid)) {
                        //避免采集端也观看自己的屏幕
                        allScreenDevIds.remove(allScreenDevIds.indexOf(srcdeviceid));
                    }
                    int value = holder.cb_mandatory.isChecked() ? InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE : 0;
                    jni.streamPlay(srcdeviceid, subid, value, tempRes, allScreenDevIds);
                    if (openScreenpopFromPostilpop) {
                        openScreenpopFromPostilpop = false;
                        wm.removeView(choosePlayerPop);
                        choosePlayerPopIsShow = false;
                    } else {
                        showPop(choosePlayerPop, mImageView, mParams);
                    }
                } else {
                    ToastUtils.showShort(R.string.tip_select_device);
                }
            } else {
                /** **** **  停止同屏操作  ** **** **/
                allScreenDevIds = onLineMemberAdapter.getChecks();
                allScreenDevIds.addAll(onLineProjectorAdapter.getChecks());
                if (allScreenDevIds.size() > 0) {
                    jni.stopResourceOperate(tempRes, allScreenDevIds);
                    showPop(choosePlayerPop, mImageView, mParams);
                } else {
                    ToastUtils.showShort(R.string.tip_select_device);
                }
            }
        });
        holder.player_pop_cancel.setOnClickListener(view -> {
            LogUtil.e(TAG, "FabService.onClick :  选择参会人点击取消 --> " + openScreen);
            if (openScreenpopFromPostilpop) {
                openScreenpopFromPostilpop = false;
                wm.removeView(choosePlayerPop);
                choosePlayerPopIsShow = false;
            } else if (openScreen) {
                //从视屏直播页面打开,但是没有点击确定同屏
                showPop(choosePlayerPop, mImageView, mParams);
                openScreen = false;
            } else {
                showPop(choosePlayerPop, mImageView, mParams);
            }
        });
        //默认全选
        holder.players_all_cb.performClick();
        holder.projector_all_cb.performClick();
    }

    //批注图片页面
    private void showPostilPop(byte[] bytes) {
        /** **** **  批注图片页面  ** **** **/
        postilPop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_screenshot_postil, null);
        postilPop.setTag("postilPop");
        PostilViewHolder postilHolder = new PostilViewHolder(postilPop);
        postilHolder_Event(postilHolder, bytes);
    }

    //批注页面事件
    private void postilHolder_Event(final PostilViewHolder postilHolder, final byte[] bytes) {
        /** **** **  将截取的屏幕图片展示到ImageView  ** **** **/
        Glide.with(getApplicationContext()).load(bytes).into(postilHolder.postil_image);
        //保存本地
        postilHolder.postil_save_local.setOnClickListener(v -> savePop(bytes, 1));
        //保存服务器
        postilHolder.postil_save_server.setOnClickListener(v -> savePop(bytes, 2));
        //截图批注
        postilHolder.postil_pic.setOnClickListener(v -> {
            Intent intent = new Intent(fabContext, DrawBoardActivity.class);
            LogUtil.e(TAG, "FabService.onClick :  数组的大小bytes --> " + bytes.length);
            intent.putExtra("postilpic", "postilpic");
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            fabContext.startActivity(intent);
            showPop(postilPop, mImageView, mParams);
        });
        //发起同屏
        postilHolder.postil_start_screen.setOnClickListener(v -> {
            openScreenpopFromPostilpop = true;
            startScr = true;
            showPlayerPop(null);
            wm.addView(choosePlayerPop, params);
            choosePlayerPopIsShow = true;
//            showScreenPop();
//            wm.addView(screenPop, notParams);
//            screenPopIsShow = true;
        });
        //停止同屏
        postilHolder.postil_stop_screen.setOnClickListener(v -> {
            List<Integer> tempRes1 = new ArrayList<>();
            tempRes1.add(0);
            if (allScreenDevIds != null) {
                /** ************ ******  停止资源操作  ****** ************ **/
                jni.stopResourceOperate(tempRes1, allScreenDevIds);
            }
        });
        // 发起投影
        postilHolder.postil_start_projection.setOnClickListener(v -> {
            openProjectpopFromPostilPop = true;
            startPro = true;
            showProRlPop(null);
            wm.addView(proListPop, params);
            proListPopIsShow = true;
//            showProjectPop();
//            wm.addView(projectPop, notParams);
//            projectPopIsShow = true;
        });
        // 停止投影
        postilHolder.postil_stop_projection.setOnClickListener(v -> {
            if (applyProjectionIds != null) {
                /** ************ ******  停止资源操作  ****** ************ **/
                jni.stopResourceOperate(tempRes, applyProjectionIds);
            }
        });
        //退出
        postilHolder.exit.setOnClickListener(v -> showPop(postilPop, mImageView, mParams));
    }

    //投影控制事件
    private void proHolder_Event(final ProjectViewHolder holder) {
        /** **** **  判断是发起还是结束投影  ** **** **/
        holder.tv_title.setText(startPro ? getResources().getString(R.string.button_start_projection) : getResources().getString(R.string.button_stop_projection));
        //所有投影机
        holder.all_projector.setOnClickListener(v -> {
            holder.all_projector.setSelected(true);
            holder.choose_projector.setSelected(false);
            if (allProjectors != null && allProjectorAdapter != null) {
                allProjectorAdapter.setAllCheck(true);
            }
        });
        holder.all_projector.setSelected(true);
        holder.choose_projector.setSelected(false);
        if (allProjectors != null && allProjectorAdapter != null) {
            allProjectorAdapter.setAllCheck(true);
        }
        //选择投影机
        holder.choose_projector.setOnClickListener(v -> {
            holder.all_projector.setSelected(false);
            holder.choose_projector.setSelected(true);
            showProRlPop(projectPop);
        });
        //申请投影或结束投影
        holder.ensure.setOnClickListener(v -> {
            int subid;
            int srcdeviceid;
            if (startPro) {//开启投影
                /** **** **  判断是否从视屏列表页面打开的  ** **** **/
                if (openProjector) {
                    srcdeviceid = videoInfo.getVideoInfo().getDeviceid();
                    subid = videoInfo.getVideoInfo().getSubid();
                } else {
                    subid = 2;
                    srcdeviceid = Values.localDevId;
                }
                if (allProjectorAdapter != null) {
                    applyProjectionIds = allProjectorAdapter.getChecks();
                    if (applyProjectionIds.size() > 0) {
                        /** ************ ******  流播放  0x1080004****** ************ **/
                        jni.streamPlay(srcdeviceid, subid, 0, tempRes, applyProjectionIds);
                        if (openProjectpopFromPostilPop) {
                            openProjectpopFromPostilPop = false;
                            wm.removeView(projectPop);
                            projectPopIsShow = false;
                        }
                        showPop(projectPop, mImageView, mParams);
                    } else {
                        ToastUtils.showShort(R.string.tip_select_device);
                    }
                }
            } else {//结束投影
                applyProjectionIds = allProjectorAdapter.getChecks();
                if (applyProjectionIds.size() > 0) {
                    jni.stopResourceOperate(tempRes, applyProjectionIds);
                    showPop(projectPop, mImageView, mParams);
                } else {
                    ToastUtils.showShort(R.string.tip_select_device);
                }
            }
        });
        //取消
        holder.cancel.setOnClickListener(v -> {
            if (openProjectpopFromPostilPop) {
                openProjectpopFromPostilPop = false;
                wm.removeView(projectPop);
                projectPopIsShow = false;
            } else {
                showPop(projectPop, mImageView, mParams);
            }
        });
    }

    private void initAdapter() {
        initDatas();
        updateDatas();
        if (allProjectorAdapter == null) {
            allProjectorAdapter = new OnLineProjectorAdapter(allProjectors);
        }
        if (onLineProjectorAdapter == null) {
            onLineProjectorAdapter = new OnLineProjectorAdapter(onLineProjectors);
        }
        if (pushOnLineProjectorAdapter == null) {//推送文件投影机
            pushOnLineProjectorAdapter = new OnLineProjectorAdapter(onLineProjectors);
        }
        if (onLineMemberAdapter == null) {
            onLineMemberAdapter = new ScreenControlAdapter(onLineMember);
        }
        if (pushOnLineMemberAdapter == null) {//推送文件参会人
            pushOnLineMemberAdapter = new ScreenControlAdapter(allOnLineMember);
        }
        /** **** **  刷新Adapter  ** **** **/
        allProjectorAdapter.notifyDataSetChanged();
        allProjectorAdapter.notifyChecks();
        onLineProjectorAdapter.notifyDataSetChanged();
        onLineProjectorAdapter.notifyChecks();
        onLineMemberAdapter.notifyDataSetChanged();
        onLineMemberAdapter.notifyChecks();
        pushOnLineProjectorAdapter.notifyDataSetChanged();
        pushOnLineProjectorAdapter.notifyChecks();
        pushOnLineMemberAdapter.notifyDataSetChanged();
        pushOnLineMemberAdapter.notifyChecks();
    }

    private void initDatas() {
        if (faceOnLMember == null) {//界面状态为1参会人
            faceOnLMember = new ArrayList<>();
        } else {
            faceOnLMember.clear();
        }
        if (onLineMember == null) {//在线状态参会人
            onLineMember = new ArrayList<>();
        } else {
            onLineMember.clear();
        }
        if (allOnLineMember == null) {//在线状态参会人
            allOnLineMember = new ArrayList<>();
        } else {
            allOnLineMember.clear();
        }
        if (onLineProjectors == null) {//在线投影机
            onLineProjectors = new ArrayList<>();
        } else {
            onLineProjectors.clear();
        }
        if (allProjectors == null) {//所有投影机
            allProjectors = new ArrayList<>();
        } else {
            allProjectors.clear();
        }
        if (onlineClientIds == null) {//存放在线状态客户端ID
            onlineClientIds = new ArrayList<>();
        } else {
            onlineClientIds.clear();
        }
    }

    private void updateDatas() {
        LogUtil.i(TAG, "updataDatas 设备个数：" + deviceInfos.size() + ",参会人个数：" + memberInfos.size());
        for (int i = 0; i < deviceInfos.size(); i++) {
            InterfaceDevice.pbui_Item_DeviceDetailInfo deviceInfo = deviceInfos.get(i);
            int netState = deviceInfo.getNetstate();
            int faceState = deviceInfo.getFacestate();
            int devId = deviceInfo.getDevcieid();
            int memberId = deviceInfo.getMemberid();
            //判断是否是投影机
            if (Macro.DEVICE_MEET_PROJECTIVE == (devId & Macro.DEVICE_MEET_ID_MASK) && netState == 1) {
                //在线状态的投影机
                onLineProjectors.add(deviceInfo);
                allProjectors.add(deviceInfo);
            }
            //判断是否是在线的并且界面状态为1的参会人
            if (memberInfos.size() > 0 && netState == 1 && faceState == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MemFace_VALUE) {
                for (int j = 0; j < memberInfos.size(); j++) {
                    InterfaceMember.pbui_Item_MemberDetailInfo memberInfo = memberInfos.get(j);
                    int personid = memberInfo.getPersonid();
                    if (personid == memberId) {
                        DevMember devMember = new DevMember(memberInfos.get(j), devId);
                        LogUtils.d(TAG, "在线参会人：" + devMember.toString());
                        allOnLineMember.add(devMember);
                        //过滤掉自己的设备
                        if (devId != Values.localDevId) {
                            //查找到在线状态的参会人员
                            onLineMember.add(devMember);
                            //查找到界面状态为1的参会人员
                            faceOnLMember.add(devMember);
                        }
                    }
                }
            }
            if ((devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_CLIENT) {//客户端
                if (netState == 1 && devId != Values.localDevId) {//过滤自己
                    onlineClientIds.add(devId);//添加在线客户端
                }
            }
        }
        LogUtils.i(TAG, "updataDatas 在线参会人个数：" + onLineMember.size() + ",在线投影机个数：" + onLineProjectors.size());
    }

    private void video_screen(EventMessage message) {
//        videoInfo = (List<VideoInfo>) message.getObject();
//        boolean whether = message.isWhether();
        Object[] objects = message.getObjects();
        videoInfo = (VideoInfo) objects[0];
        boolean whether = (boolean) objects[1];
        if (whether) {//开启同屏
            if (isHasPermission(Macro.permission_code_screen)) {//是否拥有同屏权限
                openScreen = true;
                LogUtil.d(TAG, "video_screen: 视屏直播打开同屏控制..");
                startScr = true;
                showPlayerPop(mImageView);
            } else {
                ToastUtils.showShort(R.string.tip_no_screen_permissions);
            }
        } else {//停止同屏
            if (allScreenDevIds != null && allScreenDevIds.size() > 0) {
                LogUtil.d(TAG, "video_screen: 停止同屏:" + allScreenDevIds.toString() + " tempRes:" + tempRes.toString());
                jni.stopResourceOperate(tempRes, allScreenDevIds);
                allScreenDevIds = null;
            }
        }
    }

    private void video_pro(EventMessage message) {
        Object[] objects = message.getObjects();
        videoInfo = (VideoInfo) objects[0];
        boolean whether1 = (boolean) objects[1];
        if (whether1) if (isHasPermission(Macro.permission_code_projection)) {//是否拥有投影权限
            openProjector = true;
            startPro = true;
            LogUtil.d(TAG, "video_pro: 视屏直播打开投影控制..");
            showProRlPop(mImageView);
        } else {
            ToastUtils.showShort(R.string.tip_no_projection_permissions);
        }
        else if (applyProjectionIds != null) {
            jni.stopResourceOperate(tempRes, applyProjectionIds);
            openProjector = false;
            applyProjectionIds = null;
        }
    }

    public void startVirtual() {
        if (App.mediaProjection != null) {
            LogUtil.i(TAG, "want to display virtual");
            virtualDisplay();
        } else {
            LogUtil.i(TAG, "start screen capture intent");
            LogUtil.i(TAG, "want to build mediaprojection and display virtual");
            App.mediaProjection = App.mediaProjectionManager.getMediaProjection(App.result, App.intent);
            virtualDisplay();
        }
    }


    private void virtualDisplay() {
        try {
            mVirtualDisplay = App.mediaProjection.createVirtualDisplay("screen-mirror",
                    windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
            LogUtil.i(TAG, "virtual displayed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startScreen() {
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            LogUtil.e(TAG, "startScreen :  image 为null --> ");
//            wm.addView(mImageView, params);
//            mImageViewIsShow = true;
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        LogUtil.i(TAG, "image data captured");

        if (bitmap != null) {
            try {
                createDir(CACHE_FILE);
                File fileImage = new File(CACHE_FILE + tempImgName + ".png");
                if (!fileImage.exists()) {
                    fileImage.createNewFile();
                    LogUtil.i(TAG, "image file created");
                }
                FileOutputStream out = new FileOutputStream(fileImage);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
//                Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                Uri contentUri = Uri.fromFile(fileImage);
//                media.setData(contentUri);
//                this.sendBroadcast(media);
                LogUtil.i(TAG, "screen image saved");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
        }
    }

    //同屏控制事件
    private void Screen_Event(final ScreenViewHolder holder) {
        /* **** **  判断是发起还是停止同屏  ** **** */
        holder.tv_title.setText(startScr ? getResources().getString(R.string.button_start_screen) : getResources().getString(R.string.button_stop_screen));
        holder.cb_screen_mandatory.setVisibility(startScr ? View.VISIBLE : View.INVISIBLE);
        //选中所有人
        holder.all_member.setOnClickListener(v -> {
            holder.all_member.setSelected(true);
            holder.choose_member.setSelected(false);
            if (onLineMemberAdapter != null && onLineMember != null) {
                onLineMemberAdapter.setAllCheck(true);
            }
        });
        holder.all_member.setSelected(true);
        holder.choose_member.setSelected(false);
        if (onLineMemberAdapter != null && onLineMember != null) {
            onLineMemberAdapter.setAllCheck(true);
        }
        //选择参与人
        holder.choose_member.setOnClickListener(view -> {
            //选择参与人 和 投影机
            showPlayerPop(screenPop);
            holder.choose_member.setSelected(true);
            holder.all_member.setSelected(false);
        });
        //选中所有投影机
        holder.all_projector.setOnClickListener(v -> {
            holder.all_projector.setSelected(true);
            holder.choose_projector.setSelected(false);
            if (onLineProjectors != null && onLineProjectorAdapter != null) {
                onLineProjectorAdapter.setAllCheck(true);
            }
        });
        holder.all_projector.setSelected(true);
        holder.choose_projector.setSelected(false);
        if (onLineProjectors != null && onLineProjectorAdapter != null) {
            onLineProjectorAdapter.setAllCheck(true);
        }
        //自由选择投影机
        holder.choose_projector.setOnClickListener(view -> {
            showPlayerPop(screenPop);
            holder.choose_projector.setSelected(true);
            holder.all_projector.setSelected(false);
        });
        //开始/停止同屏
        holder.ensure.setOnClickListener(view -> {
            int subid;  // 2：屏幕 3：摄像头
            int srcdeviceid;//需要采集的设备ID (源头)
            if (startScr) {//开始同屏
                allScreenDevIds = onLineMemberAdapter.getChecks();
                allScreenDevIds.addAll(onLineProjectorAdapter.getChecks());
                int triggeruserval = 0;
                if (holder.cb_screen_mandatory.isChecked()) {
                    triggeruserval = InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE;
                }
                srcdeviceid = Values.localDevId;//采集自己屏幕
                subid = 2;//屏幕
                /* **** **  流播放  ** **** */
                if (allScreenDevIds.size() > 0) {
                    jni.streamPlay(srcdeviceid, subid, triggeruserval, tempRes, allScreenDevIds);
                    if (openScreenpopFromPostilpop) {
                        openScreenpopFromPostilpop = false;
                        wm.removeView(screenPop);
                        screenPopIsShow = false;
                    } else {
                        showPop(screenPop, mImageView, mParams);
                    }
                } else {
                    ToastUtils.showShort(R.string.tip_select_device);
                }
            } else {//停止同屏
                allScreenDevIds = onLineMemberAdapter.getChecks();
                allScreenDevIds.addAll(onLineProjectorAdapter.getChecks());
                if (allScreenDevIds.size() > 0) {
                    LogUtil.d(TAG, "onClick: 停止同屏:" + allScreenDevIds.toString() + " tempRes:" + tempRes.toString());
                    /** ************ ******  停止资源操作  ****** ************ **/
                    jni.stopResourceOperate(tempRes, allScreenDevIds);
                    allScreenDevIds = null;
                    showPop(screenPop, mImageView, mParams);
                } else {
                    ToastUtils.showShort(R.string.tip_select_device);
                }
                allScreenDevIds = null;
            }
        });
        //取消
        holder.cancel.setOnClickListener(view -> {
            if (openScreenpopFromPostilpop) {
                openScreenpopFromPostilpop = false;
                wm.removeView(screenPop);
                screenPopIsShow = false;
            } else showPop(screenPop, mImageView, mParams);
        });
    }

    private void setParamsType(WindowManager.LayoutParams params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;//总是出现在应用程序窗口之上
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;//总是出现在应用程序窗口之上
        }
    }

    //截图信息保存本地/服务器
    private void savePop(final byte[] bytes, final int type) {
        /** **** **  输入框  ** **** **/
        edtPop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.edt_file_name, null);
        edtPop.setTag("edtPop");
        EdtViewHolder edtHolder = new EdtViewHolder(edtPop);
        EdtHolder_Event(edtHolder, bytes, type);
    }

    //输入文件名的输入框
    private void EdtHolder_Event(final EdtViewHolder holder, final byte[] bytes, final int type) {
        wm.addView(edtPop, notParams);
        edtPopIsShow = true;
        holder.edt_name.setHint(getResources().getString(R.string.please_enter_file_name));
        holder.edt_name.setText(String.valueOf(System.currentTimeMillis()));
        //确定
        holder.ensure.setOnClickListener(v -> {
            Bitmap bitmap = ConvertUtil.bytes2Bitmap(bytes);
            createDir(Macro.POSTIL_FILE);
            FabPicName = holder.edt_name.getText().toString();
            if (FabPicName.equals("")) {
                ToastUtils.showShort(R.string.please_enter_file_name);
            } else if (!FileUtil.isLegalName(FabPicName)) {
                ToastUtils.showShort(R.string.tip_file_name_unlawfulness);
                return;
            } else {
                FabPicFile = new File(Macro.POSTIL_FILE, FabPicName + ".png");
                try {
                    if (!FabPicFile.exists()) {
                        FabPicFile.createNewFile();
                        FileUtil.saveBitmap(bitmap, FabPicFile);
                    }
                    if (type == 2) {//上传到服务器
                        String path = FabPicFile.getPath();
                        int mediaid = getMediaid(path);
                        String fileEnd = path.substring(path.lastIndexOf(".") + 1, path.length()).toLowerCase();
                        jni.uploadFile(InterfaceMacro.Pb_Upload_Flag.Pb_MEET_UPLOADFLAG_ONLYENDCALLBACK.getNumber(), 2, 0, FabPicName + "." + fileEnd, path, 0, Macro.upload_screen_shot);
                    }
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.e(TAG, "FabService.onClick :  上传到服务器异常 --> " + e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    LogUtil.e(TAG, "FabService.onClick :  文件创建失败 --> " + e.getMessage());
                    e.printStackTrace();
                }
                wm.removeView(edtPop);
                edtPopIsShow = false;
            }
        });
        //取消
        holder.cancel.setOnClickListener(v -> {
            wm.removeView(edtPop);
            edtPopIsShow = false;
        });
    }

    //可加入同屏
    private void One_JoinScreen_Pop() {
        canJoinPop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.canjoin_pop, null);
        canJoinPop.setTag("canJoinPop");
        CanJoinViewHolder holder = new CanJoinViewHolder(canJoinPop);
        canJoin_Event(holder);
        showPop(pop, canJoinPop, notParams);
    }

    //可加入同屏事件
    private void canJoin_Event(final CanJoinViewHolder holder) {
        canJoinMemberAdapter.setItemClick((view, posion) -> {
            /** **** **  点击哪个item就设置哪个选中  ** **** **/
            canJoinMemberAdapter.setCheck(canJoinMember.get(posion).getDevId());
        });
        canJoinProAdapter.setItemClick((view, posion) -> canJoinProAdapter.setCheck(canJoinPro.get(posion).getDevcieid()));
        //确定
        holder.player_pop_ensure.setOnClickListener(v -> {
            int playID = 0;
            //获取选择播放的参会人或则投影机，只能选择一个
            playID = canJoinMemberAdapter.getChecks();
            if (playID == 0) playID = canJoinProAdapter.getChecks();
            if (playID != 0) {
                List<Integer> res = new ArrayList<>();
                res.add(0);
                ArrayList<Integer> ids = new ArrayList<>();
                ids.add(Values.localDevId);
                /* **** **  进行流播放  ** **** */
                jni.streamPlay(playID, 2, 0, res, ids);
                showPop(canJoinPop, mImageView, mParams);
            } else {
                ToastUtils.showShort(R.string.tip_select_device);
            }
        });
        holder.player_pop_cancel.setOnClickListener(v -> showPop(canJoinPop, mImageView, mParams));
    }

    //截图
    private void One_ScreenShot() {
        deleteAll();//清除所有的view
        Handler handler1 = new Handler();
        handler1.postDelayed(() -> {
            startVirtual();
            Handler handler2 = new Handler();
            handler2.postDelayed(() -> {
                startScreen();
                Handler handler3 = new Handler();
                handler3.postDelayed(() -> {
                    FileUtil.createDir(CACHE_FILE);
                    File file = new File(CACHE_FILE + tempImgName + ".png");
                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(CACHE_FILE + tempImgName + ".png");
                        bytes = ConvertUtil.Bitmap2bytes(bitmap);
                        if (!bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                        showPostilPop(bytes);
                        wm.addView(postilPop, postilParams);
                        postilPopIsShow = true;
                        //将临时图片删除
                        file.delete();
                    } else {
                        ToastUtils.showShort(R.string.tip_operation_failed);
                        wm.addView(mImageView, mParams);
                        mImageViewIsShow = true;
                    }
                }, 100);
            }, 100);
        }, 500);
    }

    /**
     * 呼叫服务弹框
     */
    private void One_CallServe_pop() {
        callServePop = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_call_service, null);
        callServePop.setTag("callServePop");
        FunViewHolder callServeHolder = new FunViewHolder(callServePop);
        callServeHolder_Event(callServeHolder);
        showPop(pop, callServePop, serviceParams);
    }

    private String getSeriveStr() {
        seriveStr = "";
        for (int i = 0; i < seriveTvs.size(); i++) {
            if (seriveTvs.get(i).isSelected()) {
                String s = "";
                switch (i) {
                    case 0:
                        s = getResources().getString(R.string.paper);
                        break;
                    case 1:
                        s = getResources().getString(R.string.pen);
                        break;
                    case 2:
                        s = getResources().getString(R.string.tea);
                        break;
                    case 3:
                        s = getResources().getString(R.string.water);
                        break;
                    case 4:
                        s = getResources().getString(R.string.calculate);
                        break;
                    case 5:
                        s = getResources().getString(R.string.waiter);
                        break;
                    case 6:
                        s = getResources().getString(R.string.sweep);
                        break;
                    case 7:
                        s = getResources().getString(R.string.teach);
                        break;
                }
                if (seriveStr.equals("")) {
                    seriveStr += s;
                } else {
                    seriveStr += "、" + s;
                }
            }
        }
        return seriveStr;
    }

    private int MSG_TYPE;

    //呼叫服务事件
    private void callServeHolder_Event(final FunViewHolder holder) {
        final List<Integer> arr = new ArrayList<>();
        arr.add(0);//会议服务类请求则为 0
        MSG_TYPE = InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Other.getNumber();
        //纸
        holder.paper_msg.setOnClickListener(view -> {
//            boolean selected = view.isSelected();
//            view.setSelected(!selected);
//            holder.edt_msg.setText(getSeriveStr());
            holder.edt_msg.setText(getString(R.string.service_pager));
            holder.edt_msg.setSelection(holder.edt_msg.getText().toString().length());
            setSelected(0);
            MSG_TYPE = InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Paper_VALUE;
        });
        //笔
        holder.pen_msg.setOnClickListener(view -> {
//            boolean selected = view.isSelected();
//            view.setSelected(!selected);
//            holder.edt_msg.setText(getSeriveStr());
            holder.edt_msg.setText(getString(R.string.service_pen));
            holder.edt_msg.setSelection(holder.edt_msg.getText().toString().length());
            setSelected(1);
            MSG_TYPE = InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Pen.getNumber();
        });
        //茶水
        holder.tea_msg.setOnClickListener(view -> {
//            boolean selected = view.isSelected();
//            view.setSelected(!selected);
//            holder.edt_msg.setText(getSeriveStr());
            holder.edt_msg.setText(getString(R.string.service_tea));
            holder.edt_msg.setSelection(holder.edt_msg.getText().toString().length());
            setSelected(2);
            MSG_TYPE = InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Tea.getNumber();
        });
        //矿泉水
        holder.water_msg.setOnClickListener(view -> {
//            boolean selected = view.isSelected();
//            view.setSelected(!selected);
//            holder.edt_msg.setText(getSeriveStr());
            holder.edt_msg.setText(getString(R.string.service_mineralWater));
            holder.edt_msg.setSelection(holder.edt_msg.getText().toString().length());
            setSelected(3);
            MSG_TYPE = InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Water.getNumber();
        });
        //计算器
        holder.calculator_msg.setOnClickListener(view -> {
//            boolean selected = view.isSelected();
//            view.setSelected(!selected);
//            holder.edt_msg.setText(getSeriveStr());
            holder.edt_msg.setText(getString(R.string.service_Calculator));
            holder.edt_msg.setSelection(holder.edt_msg.getText().toString().length());
            setSelected(4);
            MSG_TYPE = InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Other.getNumber();
        });
        //服务员
        holder.waiter_msg.setOnClickListener(view -> {
//            boolean selected = view.isSelected();
//            view.setSelected(!selected);
//            holder.edt_msg.setText(getSeriveStr());
            holder.edt_msg.setText(getString(R.string.service_waiter));
            holder.edt_msg.setSelection(holder.edt_msg.getText().toString().length());
            setSelected(5);
            MSG_TYPE = InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Waiter.getNumber();
        });
        //清扫
        holder.sweep_msg.setOnClickListener(view -> {
//            boolean selected = view.isSelected();
//            view.setSelected(!selected);
//            holder.edt_msg.setText(getSeriveStr());
            holder.edt_msg.setText(getString(R.string.service_clean));
            holder.edt_msg.setSelection(holder.edt_msg.getText().toString().length());
            setSelected(6);
            MSG_TYPE = InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Other.getNumber();
        });
        //技术员
        holder.technician_msg.setOnClickListener(view -> {
//            boolean selected = view.isSelected();
//            view.setSelected(!selected);
//            holder.edt_msg.setText(getSeriveStr());
            holder.edt_msg.setText(getString(R.string.service_technician));
            holder.edt_msg.setSelection(holder.edt_msg.getText().toString().length());
            setSelected(7);
            MSG_TYPE = InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Technical.getNumber();
        });
        //发送
        holder.send_msg.setOnClickListener(v -> {
            String other = holder.edt_msg.getText().toString().trim();
            if (!TextUtils.isEmpty(other)) {
                //全部设置成其它服务类型
                MSG_TYPE = InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Other.getNumber();
                jni.sendMeetChatInfo(other, MSG_TYPE, arr);
                //发送后清空输入框
                holder.edt_msg.setText("");
            }
            showPop(callServePop, mImageView, mParams);
        });
        holder.img_btn.setOnClickListener(v -> showPop(callServePop, mImageView, mParams));
    }

    private void fun_queryAttendPeoplePermissions() {
        try {
            InterfaceMember.pbui_Type_MemberPermission o = jni.queryAttendPeoplePermissions();
            if (o == null) return;
            mPermissionsList.clear();
            mPermissionsList.addAll(o.getItemList());
            for (int i = 0; i < mPermissionsList.size(); i++) {
                InterfaceMember.pbui_Item_MemberPermission item = mPermissionsList.get(i);
                if (item.getMemberid() == Values.localMemberId) {
                    Values.localPermission = item.getPermission();
                    break;
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo o = jni.queryAttendPeople();
            memberInfos.clear();
            if (o != null) {
                memberInfos.addAll(o.getItemList());
                StringBuilder sb = new StringBuilder();
                sb.append("参会人数量" + memberInfos.size());
                sb.append(",所有参会人:{");
                for (int i = 0; i < memberInfos.size(); i++) {
                    InterfaceMember.pbui_Item_MemberDetailInfo info = memberInfos.get(i);
                    sb.append("\n名称：" + info.getName().toStringUtf8() + ",id：" + info.getPersonid());
                }
                sb.append("\n}");
                LogUtils.i(TAG, sb.toString());
                if (voteResult) {//点击了投票结果快捷键
                    voteResult = false;
                    LogUtil.d(TAG, "fun_queryAttendPeople: 收到参会人数据,打开投票结果pop");
                    showVoteResultsPop();
                }
            }
            fun_queryDeviceInfo();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryDeviceInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo o = jni.queryDeviceInfo();
            deviceInfos.clear();
            if (o != null) {
                deviceInfos.addAll(o.getPdevList());
                StringBuilder sb = new StringBuilder();
                sb.append("所有终端:{");
                int count = 0;
                for (int i = 0; i < deviceInfos.size(); i++) {
                    InterfaceDevice.pbui_Item_DeviceDetailInfo info = deviceInfos.get(i);
                    int devcieid = info.getDevcieid();
                    //判断是否是投影机
                    if (Macro.DEVICE_MEET_CLIENT == (devcieid & Macro.DEVICE_MEET_ID_MASK)) {
                        count++;
                        sb.append("\n人员id：" + info.getMemberid() + ",名称：" + info.getDevname().toStringUtf8()
                                + ",ID：" + info.getDevcieid() + ",界面状态：" + info.getFacestate()
                                + ",在线状态：" + info.getNetstate() + ",会议id：" + info.getMeetingid());
                    }
                }
                sb.append("\n}");
                LogUtils.i(TAG, "终端数量：" + count + "," + sb.toString());
            }
            initAdapter();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        LogUtil.i("Fab_life", "FabService.unbindService :   --->>> " + this);
        super.unbindService(conn);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.e(TAG, "FabService.onBind :   --> ");
        return null;
    }

    /**
     * 展示新的弹框
     *
     * @param removeView 正在展示的view
     * @param addView    需要替换的view
     * @param params     params配置
     */
    private void showPop(View removeView, View addView, WindowManager.LayoutParams params) {
        wm.removeView(removeView);
        wm.addView(addView, params);
        setIsShowing(removeView, addView);
    }

    private void showPop(View removeView, View addView) {
        wm.removeView(removeView);
        wm.addView(addView, params);
        setIsShowing(removeView, addView);
    }

    @Override
    public void onDestroy() {
        LogUtil.i("Fab_life", "FabService.onDestroy :   --> ");
        exitTiming();
//        BaseActivity.stopRecord();
        App.stopRecord();
        App.fabServiceIsOpened = false;
        deleteAll();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public static class PlayerViewHolder {
        public View rootView;
        public TextView title;
        public CheckBox cb_mandatory;
        public CheckBox players_all_cb;
        public RecyclerView players_rl;
        public CheckBox projector_all_cb;
        public RecyclerView projector_rl;
        public Button player_pop_ensure;
        public Button player_pop_cancel;

        public PlayerViewHolder(View rootView) {
            this.rootView = rootView;
            this.title = rootView.findViewById(R.id.title);
            this.cb_mandatory = rootView.findViewById(R.id.cb_mandatory);
            this.players_all_cb = rootView.findViewById(R.id.players_all_cb);
            this.players_rl = rootView.findViewById(R.id.players_rl);
            //瀑布流布局
            this.players_rl.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
            this.players_rl.setAdapter(onLineMemberAdapter);
            this.projector_all_cb = rootView.findViewById(R.id.projector_all_cb);
            this.projector_rl = rootView.findViewById(R.id.projector_rl);
            //瀑布流布局
            this.projector_rl.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL));
            this.projector_rl.setAdapter(onLineProjectorAdapter);
            this.player_pop_ensure = rootView.findViewById(R.id.ensure);
            this.player_pop_cancel = rootView.findViewById(R.id.cancel);
        }
    }

    public static class ProListViewHolder {
        public View rootView;
        public TextView pro_title;
        public CheckBox cb_pro_mandatory;
        public CheckBox allchoose;
        public RecyclerView pro_rl;
        public Button ensure;
        public Button cancel;

        public ProListViewHolder(View rootView) {
            this.rootView = rootView;
            this.pro_title = rootView.findViewById(R.id.pro_title);
            this.cb_pro_mandatory = rootView.findViewById(R.id.cb_pro_mandatory);
            this.allchoose = rootView.findViewById(R.id.allchoose);
            this.pro_rl = rootView.findViewById(R.id.pro_rl);
            this.pro_rl.setLayoutManager(new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL));
            this.pro_rl.setAdapter(allProjectorAdapter);
            this.ensure = rootView.findViewById(R.id.ensure);
            this.cancel = rootView.findViewById(R.id.cancel);
        }
    }

    public static class FunViewHolder {
        public View rootView;
        public ImageView img_btn;
        public TextView paper_msg;
        public TextView pen_msg;
        public TextView tea_msg;
        public TextView water_msg;
        public TextView calculator_msg;
        public TextView waiter_msg;
        public TextView sweep_msg;
        public TextView technician_msg;
        public EditText edt_msg;
        public TextView send_msg;

        public FunViewHolder(View rootView) {
            this.rootView = rootView;
            this.img_btn = rootView.findViewById(R.id.img_btn);
            this.paper_msg = rootView.findViewById(R.id.paper_msg);
            this.pen_msg = rootView.findViewById(R.id.pen_msg);
            this.tea_msg = rootView.findViewById(R.id.tea_msg);
            this.water_msg = rootView.findViewById(R.id.water_msg);
            this.calculator_msg = rootView.findViewById(R.id.calculator_msg);
            this.waiter_msg = rootView.findViewById(R.id.waiter_msg);
            this.sweep_msg = rootView.findViewById(R.id.sweep_msg);
            this.technician_msg = rootView.findViewById(R.id.technician_msg);
            this.edt_msg = rootView.findViewById(R.id.edt_msg);
            this.send_msg = rootView.findViewById(R.id.send_msg);
            seriveTvs = new ArrayList<>();
            seriveTvs.add(paper_msg);
            seriveTvs.add(pen_msg);
            seriveTvs.add(tea_msg);
            seriveTvs.add(water_msg);
            seriveTvs.add(calculator_msg);
            seriveTvs.add(waiter_msg);
            seriveTvs.add(sweep_msg);
            seriveTvs.add(technician_msg);
        }
    }

    private void setSelected(int index) {
        for (int i = 0; i < seriveTvs.size(); i++) {
            seriveTvs.get(i).setSelected(i == index);
        }
    }

    public static class PostilViewHolder {
        public View rootView;
        public ImageView postil_image;
        public Button postil_save_local;
        public Button postil_save_server;
        public Button postil_pic;
        public Button postil_start_screen;
        public Button postil_stop_screen;
        public Button postil_start_projection;
        public Button postil_stop_projection;
        public Button exit;

        public PostilViewHolder(View rootView) {
            this.rootView = rootView;
            this.postil_image = rootView.findViewById(R.id.postil_image);
            this.postil_save_local = rootView.findViewById(R.id.postil_save_local);
            this.postil_save_server = rootView.findViewById(R.id.postil_save_server);
            this.postil_pic = rootView.findViewById(R.id.postil_pic);
            this.postil_start_screen = rootView.findViewById(R.id.postil_start_screen);
            this.postil_stop_screen = rootView.findViewById(R.id.postil_stop_screen);
            this.postil_start_projection = rootView.findViewById(R.id.postil_start_projection);
            this.postil_stop_projection = rootView.findViewById(R.id.postil_stop_projection);
            this.exit = rootView.findViewById(R.id.exit);
        }

    }

    public static class EdtViewHolder {
        public View rootView;
        public EditText edt_name;
        public Button ensure;
        public Button cancel;

        public EdtViewHolder(View rootView) {
            this.rootView = rootView;
            this.edt_name = rootView.findViewById(R.id.edt_name);
            this.ensure = rootView.findViewById(R.id.ensure);
            this.cancel = rootView.findViewById(R.id.cancel);
        }

    }

    public static class FabViewHolder {
        public View rootView;
        public TextView postil;
        public TextView call_service;
        public TextView start_screen;
        public TextView join_screen;
        public TextView stop_screen;
        public TextView start_pro;
        public TextView stop_pro;
        public TextView note;
        public ImageView back;
        public TextView vote_result;

        public FabViewHolder(View rootView) {
            this.rootView = rootView;
            this.postil = rootView.findViewById(R.id.postil);
            this.call_service = rootView.findViewById(R.id.call_service);
            this.start_screen = rootView.findViewById(R.id.start_screen);
            this.join_screen = rootView.findViewById(R.id.join_screen);
            this.stop_screen = rootView.findViewById(R.id.stop_screen);
            this.start_pro = rootView.findViewById(R.id.start_pro);
            this.stop_pro = rootView.findViewById(R.id.stop_pro);
            this.vote_result = rootView.findViewById(R.id.vote_result);
            this.note = rootView.findViewById(R.id.note);
            this.back = rootView.findViewById(R.id.back);
        }

    }

    public static class ScreenViewHolder {
        public View rootView;
        public CheckBox cb_screen_mandatory;
        public TextView tv_title;
        public Button all_member;
        public Button choose_member;
        public Button all_projector;
        public Button choose_projector;
        public Button ensure;
        public Button cancel;

        public ScreenViewHolder(View rootView) {
            this.rootView = rootView;
            this.cb_screen_mandatory = rootView.findViewById(R.id.cb_screen_mandatory);
            this.tv_title = rootView.findViewById(R.id.tv_title);
            this.all_member = rootView.findViewById(R.id.all_member);
            this.choose_member = rootView.findViewById(R.id.choose_member);
            this.all_projector = rootView.findViewById(R.id.all_projector);
            this.choose_projector = rootView.findViewById(R.id.choose_projector);
            this.ensure = rootView.findViewById(R.id.ensure);
            this.cancel = rootView.findViewById(R.id.cancel);
        }
    }

    public static class CanJoinViewHolder {
        public View rootView;
        public RecyclerView players_rl;
        public RecyclerView projector_rl;
        public Button player_pop_ensure;
        public Button player_pop_cancel;

        public CanJoinViewHolder(View rootView) {
            this.rootView = rootView;
            this.players_rl = rootView.findViewById(R.id.players_rl);
            this.players_rl.setLayoutManager(new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.HORIZONTAL));
            this.players_rl.setAdapter(canJoinMemberAdapter);
            this.projector_rl = rootView.findViewById(R.id.projector_rl);
            this.projector_rl.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL));
            this.projector_rl.setAdapter(canJoinProAdapter);
            this.player_pop_ensure = rootView.findViewById(R.id.ensure);
            this.player_pop_cancel = rootView.findViewById(R.id.cancel);
        }
    }

    public static class ProjectViewHolder {
        public View rootView;
        public TextView tv_title;
        public Button all_projector;
        public Button choose_projector;
        public Button ensure;
        public Button cancel;

        public ProjectViewHolder(View rootView) {
            this.rootView = rootView;
            this.tv_title = rootView.findViewById(R.id.tv_title);
            this.all_projector = rootView.findViewById(R.id.all_projector);
            this.choose_projector = rootView.findViewById(R.id.choose_projector);
            this.ensure = rootView.findViewById(R.id.ensure);
            this.cancel = rootView.findViewById(R.id.cancel);
        }

    }

    public static class voteResultViewHolder {
        Button vote;
        Button election;
        Button questionnaire;
        ImageButton closeIv;
        RecyclerView voteTitleRv;
        PieChart pieChart;
        TextView optionA;
        LinearLayout voteOptionA;
        TextView optionB;
        LinearLayout voteOptionB;
        TextView optionC;
        LinearLayout voteOptionC;
        TextView optionD;
        LinearLayout voteOptionD;
        TextView optionE;
        LinearLayout voteOptionE;
        RecyclerView optionRv;
        TextView vote_type_tv;
        TextView member_count_tv;
        TextView vote_title_tv;
        ImageView vote_option_color_a;
        ImageView vote_option_color_b;
        ImageView vote_option_color_c;
        ImageView vote_option_color_d;
        ImageView vote_option_color_e;
        LinearLayout vote_type_top_ll;
        TextView vote_member_count_tv;

        voteResultViewHolder(View view) {
            vote = view.findViewById(R.id.vote);
            election = view.findViewById(R.id.election);
            questionnaire = view.findViewById(R.id.questionnaire);
            closeIv = view.findViewById(R.id.close_iv);
            voteTitleRv = view.findViewById(R.id.vote_title_rv);
            pieChart = view.findViewById(R.id.pie_chart);
            optionA = view.findViewById(R.id.option_a);
            voteOptionA = view.findViewById(R.id.vote_option_a);
            optionB = view.findViewById(R.id.option_b);
            voteOptionB = view.findViewById(R.id.vote_option_b);
            optionC = view.findViewById(R.id.option_c);
            voteOptionC = view.findViewById(R.id.vote_option_c);
            optionD = view.findViewById(R.id.option_d);
            voteOptionD = view.findViewById(R.id.vote_option_d);
            optionE = view.findViewById(R.id.option_e);
            voteOptionE = view.findViewById(R.id.vote_option_e);
            optionRv = view.findViewById(R.id.option_rv);
            vote_type_tv = view.findViewById(R.id.vote_type_tv);
            member_count_tv = view.findViewById(R.id.member_count_tv);
            vote_title_tv = view.findViewById(R.id.vote_title_tv);
            vote_option_color_a = view.findViewById(R.id.vote_option_color_a);
            vote_option_color_b = view.findViewById(R.id.vote_option_color_b);
            vote_option_color_c = view.findViewById(R.id.vote_option_color_c);
            vote_option_color_d = view.findViewById(R.id.vote_option_color_d);
            vote_option_color_e = view.findViewById(R.id.vote_option_color_e);
            vote_type_top_ll = view.findViewById(R.id.vote_type_top_ll);
            vote_member_count_tv = view.findViewById(R.id.vote_member_count_tv);
        }
    }


    public static class RequestPermissionViewHolder {
        public View rootView;
        public TextView textview;
        public Button agree;
        public Button reject;

        public RequestPermissionViewHolder(View rootView) {
            this.rootView = rootView;
            this.textview = (TextView) rootView.findViewById(R.id.textview);
            this.agree = (Button) rootView.findViewById(R.id.agree);
            this.reject = (Button) rootView.findViewById(R.id.reject);
        }

    }

    public static class ChooseCameraViewHolder {
        public View rootView;
        public Button pre_btn;
        public Button back_btn;
        public Button cancel;

        public ChooseCameraViewHolder(View rootView) {
            this.rootView = rootView;
            this.pre_btn = (Button) rootView.findViewById(R.id.pre_btn);
            this.back_btn = (Button) rootView.findViewById(R.id.back_btn);
            this.cancel = (Button) rootView.findViewById(R.id.cancel);
        }

    }


    public static class VoteSubmitViewHolder {
        public View rootView;
        public Button vote_submit_ensure;
        public Button vote_submit_cancel;

        public VoteSubmitViewHolder(View rootView) {
            this.rootView = rootView;
            this.vote_submit_ensure = (Button) rootView.findViewById(R.id.vote_submit_ensure);
            this.vote_submit_cancel = (Button) rootView.findViewById(R.id.vote_submit_cancel);
        }

    }
}
