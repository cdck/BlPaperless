package com.pa.paperless.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acker.simplezxing.activity.CaptureActivity;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceContext;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.MemberAdapter;
import com.pa.paperless.broadcase.NetWorkReceiver;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.helper.SharedPreferenceHelper;
import com.pa.paperless.service.App;
import com.pa.paperless.ui.DrawBoard;
import com.pa.paperless.utils.CodecUtil;
import com.pa.paperless.utils.ConvertUtil;
import com.pa.paperless.utils.DateUtil;
import com.pa.paperless.utils.DiaLogUtil;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.IniUtil;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.NetworkUtil;
import com.pa.paperless.utils.PopUtils;

import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import static com.pa.paperless.data.constant.Values.isOver;
import static com.pa.paperless.service.App.isDebug;
import static com.pa.paperless.utils.MyUtils.s2b;

/**
 * @author xlk
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private final String TAG = "MainActivity-->";
    private long millis;
    private PopupWindow ipEdtPop;
    private boolean toSetting;
    private boolean hasNetWork;
    private AlertDialog alertDialog;
    private TimerTask netTask;
    private Timer netTimer;
    private List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> chooseMember = new ArrayList<>();
    private MemberAdapter adapter;
    private boolean ischoosing, createNewshowing, ScanPopshowing;
    private boolean fromMeetId;
    private NativeUtil jni = NativeUtil.getInstance();
    private int parseInt;
    private PopUtils.PopBuilder ScanPop;
    private int pos;
    private float lx, ly, bx, by;
    private AlertDialog pwdDialog;
    private DrawBoard board;
    private Bitmap canvasBmp;
    private final int REQUEST_MEDIA_PROJECTION = 1;
    private final int REQUEST_SCAN = 2;
    private IniUtil iniUtil = IniUtil.getInstance();
    private NetWorkReceiver netWorkReceiver;
    private int signinType;//Interface_Macro.Pb_MeetSignType
    private boolean queryMember;
    private PopupWindow chooseMemberPop;
    private PopupWindow createMemberPop;
    private boolean isCached;

    private TextView tv_dev_online;
    private ImageView main_close_iv;
    private ImageView main_win_iv;

    private ImageView main_logo_iv;
    private TextView company_name;
    private TextView main_meetName;
    private TextView main_memberName;
    private Button main_secretary_manage;
    private Button mian_into_meeting;
    private TextView device_name_id;
    private TextView main_memberJob;
    private TextView main_unit;
    private TextView main_now_time;
    private TextView main_now_date;
    private TextView main_now_week;
    private LinearLayout main_date_ll;
    private RelativeLayout main_date_layout;
    private ConstraintLayout root_layout_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.absolute_main);
        initView();
        EventBus.getDefault().register(this);
        netWorkReceiver = new NetWorkReceiver();
        registerNetwork();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initPermissions();
        } else start();
    }

    @Override
    protected void onRestart() {
        LogUtil.e("A_life", "onRestart :   --->>> ");
        if (toSetting) {//是否从设置界面返回
            LogUtil.d(TAG, "onRestart: 从设置界面返回..");
            toSetting = false;
            CheckNet();
        } else {
            if (isOver) {//是否平台初始化完毕
                LogUtil.d(TAG, "onRestart: 平台初始化完毕,进行查询上下文..");
                MainStart();
            } else {
                LogUtil.d(TAG, "onRestart: 未初始化完毕,重新检查网络..");
                CheckNet();
            }
        }
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        unregisterNetwork();
        unregisterEventBus();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.PLATFORM_INITIALIZATION://平台初始化完毕
                LogUtil.e(TAG, "getEventMessage: 平台初始化完毕");
                MainStart();
                break;
            case EventType.platform_initialization_failed:
                initFailed(message);
                break;
            case EventType.MEET_DATE://获得后台回调时间
                long object = (long) message.getObject();
                String[] date = DateUtil.getGTMDate(object);
                upDateMainTimeUI(date);
                break;
            case EventType.DEVMEETINFO_CHANGE_INFORM://设备会议信息变更通知
                LogUtil.d(TAG, "getEventMessage: 设备会议信息变更通知");
                //缓存会议目录
                jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY.getNumber(), 0, 0);
                //缓存参会人信息(不然收不到参会人变更通知)
                jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(), 1, 0);
                //缓存排位信息(不然收不到排位变更通知)
                jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT.getNumber(), 1, 0);
                //会议目录权限
                jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYRIGHT_VALUE, 1, 0);
                fun_queryDevMeetInfo();
                break;
            case EventType.MEETDIR_CHANGE_INFORM://会议目录变更通知
                LogUtil.i(TAG, "getEventMessage 会议目录变更通知 isCached=" + isCached);
                if (!isCached) {
                    isCached = true;
                    cacheAllFile();
                }
                break;
            case EventType.MEMBER_CHANGE_INFORM://参会人员变更通知
                fun_queryAttendPeople();
                break;
//            case EventType.MeetSeat_Change_Inform://会议排位变更通知
//                LogUtil.d(TAG, "getEventMessage: 会议排位变更通知,查询会议排位");
//                fun_queryMeetRanking();
//                break;
            case EventType.MEETINFO_CHANGE_INFORM://会议信息变更通知
                InterfaceBase.pbui_MeetNotifyMsg object1 = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
                int id = object1.getId();
                int opermethod = object1.getOpermethod();
                if (fromMeetId) fun_queryMeetFromId();
                break;
            case EventType.PLACE_DEVINFO_CHANGEINFORM://会场设备信息变更通知
                fun_queryDevMeetInfo();
                break;
            case EventType.CHANGE_LOGO_IMG://下载logo图片完成
                String filepath = (String) message.getObject();
                main_logo_iv.setImageDrawable(Drawable.createFromPath(filepath));
                break;
            case EventType.CHANGE_MAIN_BG://主页背景变更
                String object2 = (String) message.getObject();
                File mainBgFile = new File(object2);
                if (!mainBgFile.exists()) break;
                Drawable drawable = Drawable.createFromPath(object2);
                root_layout_id.setBackground(drawable);
                break;
            case EventType.BUS_MAINSTART:
                MainStart();
                break;
            case EventType.DEV_REGISTER_INFORM://设备寄存器变更通知
                updateOnline();
                setDevName();
                break;
            case EventType.SIGN_EVENT://辅助签到通知
                LogUtil.d(TAG, "getEventMessage: 辅助签到通知");
                gotoMeet();
                break;
            case EventType.ICC_changed_inform://界面配置变更通知
                fun_queryInterFaceConfiguration();
                break;
            case EventType.SIGNIN_BACK://签到结果返回
                InterfaceBase.pbui_Type_MeetDBServerOperError results = (InterfaceBase.pbui_Type_MeetDBServerOperError) message.getObject();
                int type = results.getType();
                int method = results.getMethod();
                int status = results.getStatus();
                if (status == InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_DONE.getNumber()) {
                    ToastUtils.showShort(R.string.signin_ture);
                    jump2Meet();
                } else if (status == InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_PSWFAILED.getNumber()) {
                    ToastUtils.showShort(R.string.pwd_is_error);
                    showEdtPop(false);
                }
                break;
            case EventType.NETWORK_CHANGE:
                int isonline = (int) message.getObject();
                if (isonline == 1) {
                    updateOnline();
                } else {
                    tv_dev_online.setText(getString(R.string.offline));
                }
                break;
            default:
                break;
        }
    }

    private void initFailed(EventMessage message) {
        InterfaceBase.pbui_Type_DeviceValidate deviceValidate = (InterfaceBase.pbui_Type_DeviceValidate) message.getObject();
        int valflag = deviceValidate.getValflag();
        List<Integer> valList = deviceValidate.getValList();
        List<Long> user64BitdefList = deviceValidate.getUser64BitdefList();
        String binaryString = Integer.toBinaryString(valflag);
        LogUtil.i(TAG, "initFailed valflag=" + valflag + ",二进制：" + binaryString + ", valList=" + valList.toString() + ", user64List=" + user64BitdefList.toString());
        int count = 0, index;
        char[] chars = binaryString.toCharArray();
        int onlineDevCount = 0, onLineDevThreshold = 0;
        for (int i = 0; i < chars.length; i++) {
            if ((chars[chars.length - 1 - i]) == '1') {
                count++;//有效位个数+1
                index = count - 1;//有效位当前位于valList的索引（跟i是无关的）
                int code = valList.get(index);
                switch (i) {
                    case 0:
                        LogUtil.e(TAG, "initFailed 区域服务器ID：" + code);
                        break;
                    case 1:
                        LogUtil.e(TAG, "initFailed 设备ID：" + code);
                        Values.localDevId = code;
                        break;
                    case 2:
                        LogUtil.e(TAG, "initFailed 状态码：" + code);
                        String errorMessage = Macro.getErrorMessageByCode(this, code);
                        if (!TextUtils.isEmpty(errorMessage)) {
                            ToastUtils.showShort(errorMessage);
                        }
                        break;
                    case 3:
                        LogUtil.e(TAG, "initFailed 到期时间：" + code);
                        break;
                    case 4:
                        LogUtil.e(TAG, "initFailed 企业ID：" + code);
                        break;
                    case 5:
                        LogUtil.e(TAG, "initFailed 协议版本：" + code);
                        break;
                    case 6:
                        LogUtil.e(TAG, "initFailed 注册时自定义的32位整数值：" + code);
                        break;
                    case 7:
                        onlineDevCount = code;
                        LogUtil.e(TAG, "initFailed 当前在线设备数：" + code);
                        break;
                    case 8:
                        onLineDevThreshold = code;
                        LogUtil.e(TAG, "initFailed 最大在线设备数：" + code);
                        break;
                }
            }
        }
        if (onlineDevCount > onLineDevThreshold) {
            ToastUtils.showShort(getString(R.string.max_online_device, onLineDevThreshold));
        }
    }

    /**
     * 跳转到会议界面
     */
    private void gotoMeet() {
        //解决从离线界面回来，登录进去后无法查询到会议文件的问题
        jni.setContextProperty(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_LOGONMODE.getNumber(), 0);
        String memberName = main_memberName.getText().toString();
        String meetName = main_meetName.getText().toString();
        if (!meetName.isEmpty()) {
            if (!memberName.isEmpty()) {
                if (!XXPermissions.hasPermission(getApplicationContext(), Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                    showDig(getString(R.string.goto_open_permissions), 1);
                } else {
                    signIn();
                }
            } else {
                ToastUtils.showShort(R.string.unbounded_attendees);
                queryMember = true;
                fun_queryAttendPeople();
            }
        } else {
            ToastUtils.showShort(R.string.no_searched_meeting);
            if (!ScanPopshowing) {
                LogUtil.i(TAG, "gotoMeet -->打开扫码--");
                showScanPop();
            }
        }
    }

    /**
     * 签到
     */
    private void signIn() {
        try {
            if (signinType == InterfaceMacro.Pb_MeetSignType.Pb_signin_direct.getNumber()) {
                //直接签到
                jni.sendSign(0, InterfaceMacro.Pb_MeetSignType.Pb_signin_direct.getNumber(), "", s2b(""));
            } else if (signinType == InterfaceMacro.Pb_MeetSignType.Pb_signin_psw.getNumber()) {
                //个人密码签到
                showEdtPop(false);
            } else if (signinType == InterfaceMacro.Pb_MeetSignType.Pb_signin_onepsw.getNumber()) {
                //会议密码签到:和个人密码签到一样，不同的是签到类型，后台会自己判断
                showEdtPop(false);
            } else if (signinType == InterfaceMacro.Pb_MeetSignType.Pb_signin_photo.getNumber()) {
                //拍照手写签到:展示画板，点击确定后将画板保存为图片数据进行签到
                showDrawBoard("");
            } else if (signinType == InterfaceMacro.Pb_MeetSignType.Pb_signin_onepsw_photo.getNumber()) {
                //会议密码+拍照(手写):需要先输入密码，再绘制签名
                showEdtPop(true);
            } else if (signinType == InterfaceMacro.Pb_MeetSignType.Pb_signin_psw_photo.getNumber()) {
                //个人密码+拍照(手写)
                showEdtPop(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDrawBoard(String pwd) {
        PopUtils.PopBuilder.createPopupWindow(R.layout.small_draw_board, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                App.getRootView(), Gravity.CENTER, 0, 0, false, new PopUtils.ClickListener() {
                    @Override
                    public void setUplistener(PopUtils.PopBuilder builder) {
                        board = builder.getView(R.id.small_drawboard);
                        builder.getView(R.id.ensure).setOnClickListener(v -> {
                            if (board != null) {
                                canvasBmp = board.getCanvasBmp();
                                if (pwd.isEmpty()) {
                                    jni.sendSign(0, signinType, "", ConvertUtil.bmp2bs(canvasBmp));
                                } else {
                                    jni.sendSign(0, signinType, pwd, ConvertUtil.bmp2bs(canvasBmp));
                                }
                                builder.dismiss();
                                canvasBmp.recycle();
                                canvasBmp = null;
                                jump2Meet();
                            }
                        });
                        builder.getView(R.id.back).setOnClickListener(v -> board.undo());
                        builder.getView(R.id.clean).setOnClickListener(v -> board.clear());
                        builder.getView(R.id.cancel).setOnClickListener(v -> builder.dismiss());
                    }

                    @Override
                    public void setOnDismissListener(PopUtils.PopBuilder builder) {
                        board.clear();
                    }
                });
    }

    private void showEdtPop(boolean haspic) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View inflate = LayoutInflater.from(this).inflate(R.layout.edt_pwd_pop, null);
        EditText pwdedt = inflate.findViewById(R.id.pwd_edt);
        inflate.findViewById(R.id.ensure).setOnClickListener(v -> {
            String pwd = pwdedt.getText().toString().trim();
            if (pwd.isEmpty()) {
                ToastUtils.showShort(R.string.please_input_pwd);
            } else {
                if (haspic) {
                    showDrawBoard(pwd);
                } else {
                    jni.sendSign(0, signinType, pwd, s2b(""));
                }
                if (pwdDialog != null) pwdDialog.dismiss();
            }
        });
        inflate.findViewById(R.id.cancel).setOnClickListener(v -> {
            if (pwdDialog != null) pwdDialog.dismiss();
        });
        builder.setView(inflate);
        pwdDialog = builder.create();
        pwdDialog.show();
    }

    /**
     * 跳转到会议界面
     */
    private void jump2Meet() {
        unregisterEventBus();
        if (board != null) board = null;
        unreceiverUpdate();
        Intent intent = new Intent(MainActivity.this, MeetingActivity.class);
        startActivity(intent);
//        App application = (App) getApplication();
        finish();
    }

    private void unregisterEventBus() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fun_queryMeetFromId() {
        try {
            InterfaceMeet.pbui_Type_MeetMeetInfo o = jni.queryMeetFromId(parseInt);
            if (o == null) return;
            List<InterfaceMeet.pbui_Item_MeetMeetInfo> itemList = o.getItemList();
            InterfaceMeet.pbui_Item_MeetMeetInfo info = itemList.get(0);
            int roomId = info.getRoomId();
            jni.addPlaceDevice(roomId, Values.localDevId);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void updateBtn(int resid, int fontflag, int flag, int fontsize, int color, int align, String fontName) {
        Button btn = findViewById(resid);
        btn.setTextColor(color);
        btn.setTextSize(fontsize);
        update(resid);
        boolean b = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        btn.setVisibility(b ? View.VISIBLE : View.GONE);
        //字体样式
        if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD.getNumber()) {//加粗
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_LEAN.getNumber()) {//倾斜
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_UNDERLINE.getNumber()) {//下划线
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));//暂时用倾斜加粗
        } else {//正常文本
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
        //对齐方式
        if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT.getNumber()) {//左对齐
            btn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT.getNumber()) {//右对齐
            btn.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER.getNumber()) {//水平对齐
            btn.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP.getNumber()) {//上对齐
            btn.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM.getNumber()) {//下对齐
            btn.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER.getNumber()) {//垂直对齐
            btn.setGravity(Gravity.CENTER_VERTICAL);
        } else {
            btn.setGravity(Gravity.CENTER);
        }
        //字体类型
        Typeface kt_typeface;
        if (!TextUtils.isEmpty(fontName)) {
            if (fontName.equals("楷体")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "kt.ttf");
            } else if (fontName.equals("宋体")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "fs.ttf");
            } else if (fontName.equals("隶书")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "ls.ttf");
            } else if (fontName.equals("微软雅黑")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "wryh.ttf");
            } else {
                kt_typeface = Typeface.createFromAsset(getAssets(), "fs.ttf");
            }
            btn.setTypeface(kt_typeface);
        }
    }

    /**
     * 更新日期时间控件信息
     */
    private void updateDate(int resid, int fontflag, int flag, int fontsize, int color, int align, String fontName) {
        update(resid);
        main_now_time.setTextColor(color);
        main_now_time.setTextSize(fontsize);
        main_now_date.setTextColor(color);
        main_now_date.setTextSize(fontsize);
        main_now_week.setTextColor(color);
        main_now_week.setTextSize(fontsize);
        boolean b = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        main_date_layout.setVisibility(b ? View.VISIBLE : View.GONE);
        Typeface typeface = null;
        //字体样式
        if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD.getNumber()) {//加粗
            typeface = Typeface.defaultFromStyle(Typeface.BOLD);
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_LEAN.getNumber()) {//倾斜
            typeface = Typeface.defaultFromStyle(Typeface.ITALIC);
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_UNDERLINE.getNumber()) {//下划线
            typeface = Typeface.defaultFromStyle(Typeface.BOLD_ITALIC);
        } else {//正常文本
            typeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        }
        main_now_time.setTypeface(typeface);
        main_now_date.setTypeface(typeface);
        main_now_week.setTypeface(typeface);
        //对齐方式
        if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT.getNumber()) {//左对齐
            main_date_layout.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            main_now_time.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            main_now_date.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            main_now_week.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT.getNumber()) {//右对齐
            main_date_layout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            main_now_time.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            main_now_date.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            main_now_week.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER.getNumber()) {//水平对齐
            main_date_layout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            main_now_time.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            main_now_date.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            main_now_week.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP.getNumber()) {//上对齐
            main_date_layout.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            main_now_time.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            main_now_date.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            main_now_week.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM.getNumber()) {//下对齐
            main_date_layout.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            main_now_time.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            main_now_date.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            main_now_week.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER.getNumber()) {//垂直对齐
            main_date_layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            main_now_time.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            main_now_date.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            main_now_week.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        } else {
            main_date_layout.setGravity(Gravity.CENTER);
            main_now_time.setGravity(Gravity.CENTER);
            main_now_date.setGravity(Gravity.CENTER);
            main_now_week.setGravity(Gravity.CENTER);
        }
        //字体类型
        Typeface kt_typeface;
        if (!TextUtils.isEmpty(fontName)) {
            if (fontName.equals("楷体")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "kt.ttf");
            } else if (fontName.equals("宋体")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "fs.ttf");
            } else if (fontName.equals("隶书")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "ls.ttf");
            } else if (fontName.equals("微软雅黑")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "wryh.ttf");
            } else {
                kt_typeface = Typeface.createFromAsset(getAssets(), "fs.ttf");
            }
            main_now_time.setTypeface(kt_typeface);
            main_now_date.setTypeface(kt_typeface);
            main_now_week.setTypeface(kt_typeface);
        }
    }

    /**
     * 更新TextView信息
     */
    private void updateTv(int resid, int fontflag, int flag, int fontsize, int color, int align, String fontName) {
        update(resid);
        TextView tv = findViewById(resid);
        tv.setTextColor(color);
        tv.setTextSize(fontsize);
        boolean b = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        tv.setVisibility(b ? View.VISIBLE : View.GONE);
        //字体样式
        if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD.getNumber()) {//加粗
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_LEAN.getNumber()) {//倾斜
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_UNDERLINE.getNumber()) {//下划线
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));//暂时用倾斜加粗
        } else {//正常文本
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
        //对齐方式
        if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT.getNumber()) {//左对齐
            tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT.getNumber()) {//右对齐
            tv.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER.getNumber()) {//水平对齐
            tv.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP.getNumber()) {//上对齐
            tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM.getNumber()) {//下对齐
            tv.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER.getNumber()) {//垂直对齐
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        } else {
            tv.setGravity(Gravity.CENTER);
        }
        //字体类型
        Typeface kt_typeface;
        if (!TextUtils.isEmpty(fontName)) {
            if (fontName.equals("楷体")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "kt.ttf");
            } else if (fontName.equals("宋体")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "fs.ttf");
            } else if (fontName.equals("隶书")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "ls.ttf");
            } else if (fontName.equals("微软雅黑")) {
                kt_typeface = Typeface.createFromAsset(getAssets(), "wryh.ttf");
            } else {
                kt_typeface = Typeface.createFromAsset(getAssets(), "fs.ttf");
            }
            tv.setTypeface(kt_typeface);
        }
    }

    private void update(int resid) {
        ConstraintSet set = new ConstraintSet();
        set.clone(root_layout_id);
        //设置控件的大小
        float width = (bx - lx) / 100 * App.screenWidth;
        float height = (by - ly) / 100 * App.screenHeight;
        set.constrainWidth(resid, (int) width);
        set.constrainHeight(resid, (int) height);
//        LogUtil.d(TAG, "update: 控件大小 当前控件宽= " + width + ", 当前控件高= " + height);
        float biasX, biasY;
        float halfW = (bx - lx) / 2 + lx;
        float halfH = (by - ly) / 2 + ly;

        if (lx == 0) biasX = 0;
        else if (lx > 50) biasX = bx / 100;
        else biasX = halfW / 100;

        if (ly == 0) biasY = 0;
        else if (ly > 50) biasY = by / 100;
        else biasY = halfH / 100;
        if (resid == R.id.main_unit) {
            LogUtil.i(TAG, "update:单位控件 控件大小 当前控件宽= " + width + ", 当前控件高= " + height);
            LogUtil.i(TAG, "update:单位控件 biasX= " + biasX + ",biasY= " + biasY);
        } else if (resid == R.id.main_logo_iv) {
            LogUtil.d(TAG, "update:logo图标控件 控件大小 当前控件宽= " + width + ", 当前控件高= " + height);
            LogUtil.d(TAG, "update:logo图标控件 biasX= " + biasX + ",biasY= " + biasY);
        }
        set.setHorizontalBias(resid, biasX);
        set.setVerticalBias(resid, biasY);
        set.applyTo(root_layout_id);
    }

    private void chooseMember() {
        View inflate = LayoutInflater.from(this).inflate(R.layout.bind_member_layout, null);
        chooseMemberPop = new PopupWindow(inflate, ScreenUtils.getScreenWidth() / 2, ScreenUtils.getScreenHeight() / 2);
        chooseMemberPop.setBackgroundDrawable(new BitmapDrawable());
        chooseMemberPop.setAnimationStyle(R.style.Anim_PopupWindow);
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        chooseMemberPop.setTouchable(true);
        // true:设置触摸外面时消失
        chooseMemberPop.setOutsideTouchable(true);
        chooseMemberPop.setFocusable(true);
        chooseMemberPop.showAtLocation(mian_into_meeting, Gravity.CENTER, 0, 0);
        ChooseViewHolder holder = new ChooseViewHolder(inflate);
        holder.member_rl.setLayoutManager(new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL));
        holder.member_rl.setAdapter(adapter);
        //第一次进来时设置默认选中
        if (!chooseMember.isEmpty()) {
            List<Integer> checks = adapter.getChecks();
            int personid = chooseMember.get(0).getMemberid();
            if (!checks.contains(personid)) {
                adapter.setCheck(personid);
                adapter.notifyDataSetChanged();
            }
        }
        adapter.setItemClick((view, posion) -> {
            pos = posion;
            LogUtil.d(TAG, "setItemClick: pos= " + pos);
            int personid = chooseMember.get(posion).getMemberid();
            adapter.setCheck(personid);
            adapter.notifyDataSetChanged();
        });
        holder.ensure.setOnClickListener(v -> {
            if (fromMeetId) {
                fromMeetId = false;
                int memberid = chooseMember.get(pos).getMemberid();
                InterfaceMember.pbui_Type_MemberDetailInfo pbui_type_memberDetailInfo = jni.queryMemberById(memberid);
                if (pbui_type_memberDetailInfo == null) return;
                InterfaceMember.pbui_Item_MemberDetailInfo info = pbui_type_memberDetailInfo.getItem(0);
                InterfaceMember.pbui_Item_MemberDetailInfo.Builder b = InterfaceMember.pbui_Item_MemberDetailInfo.newBuilder();
                b.setJob(info.getJob());
                b.setEmail(info.getEmail());
                b.setName(info.getName());
                b.setCompany(info.getCompany());
                b.setComment(info.getComment());
                b.setPassword(info.getPassword());
                b.setPersonid(info.getPersonid());
                b.setPhone(info.getPhone());
                /* **** **  加入会议  ** **** */
                jni.scan(parseInt, InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE, b);
                chooseMemberPop.dismiss();
            } else {
                //获取选中的参会人ID
                List<Integer> checks = adapter.getChecks();
                if (!checks.isEmpty()) {
                    Integer personid = checks.get(0);
                    jni.modifyMeetRanking(personid, InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal.getNumber(), Values.localDevId);
                    fun_queryDevMeetInfo();
                    chooseMemberPop.dismiss();
                } else {
                    ToastUtils.showShort(R.string.please_choose_member);
                }
            }
        });
        holder.create_member.setOnClickListener(v -> {
            createNewMember();
            chooseMemberPop.dismiss();
        });
        holder.cancel.setOnClickListener(v -> chooseMemberPop.dismiss());
        chooseMemberPop.setOnDismissListener(() -> ischoosing = false);
    }

    private void createNewMember() {
        createNewshowing = true;
        View inflate = LayoutInflater.from(getApplicationContext()).inflate(R.layout.new_member_layout, null);
        createMemberPop = new PopupWindow(inflate, App.screenWidth / 2, App.screenHeight / 2);
        createMemberPop.setAnimationStyle(R.style.Anim_PopupWindow);
        createMemberPop.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        // 设置触摸外面时消失
        createMemberPop.setOutsideTouchable(true);
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        createMemberPop.setFocusable(true);
        createMemberPop.setTouchable(true);
        createMemberPop.showAtLocation(App.getRootView(), Gravity.CENTER, 0, 0);
        final CreateMemberViewHolder holder = new CreateMemberViewHolder(inflate);
        holder.ensure.setOnClickListener(v -> {
            String company = holder.edt_company.getText().toString();
            String name = holder.edt_name.getText().toString();
            String job = holder.edt_job.getText().toString();
            String phone = holder.edt_phone.getText().toString();
            String email = holder.edt_email.getText().toString();
            String pwd = holder.edt_pwd.getText().toString();
            InterfaceMember.pbui_Item_MemberDetailInfo build = InterfaceMember.pbui_Item_MemberDetailInfo.newBuilder()
                    .setPassword(s2b(pwd))
                    .setEmail(s2b(email))
                    .setPhone(s2b(phone))
                    .setJob(s2b(job))
                    .setCompany(s2b(company))
                    .setName(s2b(name))
                    .setPersonid(0)
                    .setComment(s2b("")).build();
            jni.addAttendPeople(build);
            createMemberPop.dismiss();
        });
        holder.cancel.setOnClickListener(v -> {
            createMemberPop.dismiss();
            fun_queryAttendPeople();
        });
        createMemberPop.setOnDismissListener(() -> {
            createNewshowing = false;
//            isOpenChoose = false;
            queryMember = true;
        });
    }

    /**
     * 网络检查
     */
    private void CheckNet() {
        if (NetworkUtil.isNetworkAvailable(getApplicationContext())) {
            LogUtils.d(TAG, "CheckNet: 有网络,开始初始化方法..." + isOver);
            if (isOver) {
                //线程问题所以用EventBus到UI线程
                EventBus.getDefault().post(new EventMessage(EventType.BUS_MAINSTART));
            } else {
                if (IniUtil.inifile.exists() && iniUtil.load(IniUtil.inifile)) {
                    String ip = iniUtil.get("areaaddr", "area0ip");
                    String port = iniUtil.get("areaaddr", "area0port");
                    if (ip == null || ip.isEmpty() || port == null || port.isEmpty()) {
                        if (IniUtil.inifile.exists()) {
                            IniUtil.inifile.delete();
                        }
                        reSetIniFile();
                    } else {
                        new Thread(() -> {
                            // 初始化无纸化网络平台
                            jni.javaInitSys(MyUtils.getUniqueId(getApplicationContext()));
                        }).start();
                    }
                }
            }
        } else {
            LogUtils.d(TAG, "CheckNet: 没有网络,前往设置...");
            showDig(getString(R.string.goto_open_network), 0);
            /** **** **  定时检测网络,如果突然有网络了则重新进入 CheckNet 进行初始化  ** **** **/
            if (netTimer == null) {
                netTimer = new Timer();
            }
            if (netTask == null) {
                netTask = new TimerTask() {
                    @Override
                    public void run() {
                        hasNetWork = NetworkUtil.isNetworkAvailable(getApplicationContext());
                        LogUtils.d(TAG, "CheckNet.run: 是否有网络:" + hasNetWork);
                        if (hasNetWork) {
                            LogUtils.d(TAG, "CheckNet.run: 检测到网络,关闭定时检测...");
                            netTask.cancel();
                            netTimer.purge();
                            netTimer.cancel();
                            netTask = null;
                            netTimer = null;
                            if (alertDialog != null && alertDialog.isShowing()) {
                                alertDialog.dismiss();
                            }
                            CheckNet();
                        }
                    }
                };
                LogUtils.d(TAG, "CheckNet: 开启定时网络检测...");
                netTimer.schedule(netTask, 0, 2000);
            }
        }
    }

    /**
     * @param type =0前往设置界面，=1申请悬浮窗权限
     */
    private void showDig(String title, int type) {
        LogUtils.i(TAG, "showDig title=" + title + ",type=" + type);
        alertDialog = DiaLogUtil.createDialog(this, title, getString(R.string.to_open), getString(R.string.exit_app), new DiaLogUtil.DiaLogListener() {
            @Override
            public void ensure(DialogInterface dialog) {
                try {
                    if (type == 0) {
                        toSetting = true;
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    } else {
                        applyPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void cancel(DialogInterface dialog) {
                dialog.dismiss();
                exit();
            }
        });
    }

    private void showScanPop() {
//        ScanPopshowing = true;
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle(R.string.title_whether_to_scan_join);
//        builder.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                if (mainMeetName.getText().toString().trim().isEmpty()) {
//                    jumpToScanPage();
//                } else {
//                    ToastUtils.showShort( R.string.binding_meeting);
//                }
//            }
//        });
//        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                ScanPopshowing = false;
//            }
//        });
//        builder.create().show();
        try {
//            defaultOpen = false;
            ScanPopshowing = true;
            ScanPop = PopUtils.PopBuilder.createPopupWindow(R.layout.scan_layout, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    App.getRootView(), Gravity.CENTER, 0, 0, false, new PopUtils.ClickListener() {
                        @Override
                        public void setUplistener(final PopUtils.PopBuilder builder) {
                            builder.getView(R.id.scan_btn).setOnClickListener(v -> {
                                //这里注意：需要先隐藏掉弹框
                                ScanPop.dismiss();
                                if (main_meetName.getText().toString().trim().isEmpty())
                                    jumpToScanPage();
                                else {
                                    ToastUtils.showShort(R.string.binding_meeting);
                                }
                            });
                            builder.getView(R.id.cancel).setOnClickListener(v -> ScanPop.dismiss());
                        }

                        @Override
                        public void setOnDismissListener(PopUtils.PopBuilder builder) {
                            LogUtil.i(TAG, "setOnDismissListener -->" + ScanPopshowing);
                            ScanPopshowing = false;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void MainStart() {
        jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_ROLE.getNumber(),
                InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MainFace.getNumber());
        updateOnline();
        LogUtil.d(TAG, "MainStart: 直接申请读取帧缓存权限");
        startIntent(REQUEST_MEDIA_PROJECTION);
    }

    private void next_step() {
        LogUtils.e(TAG, "next_step 平台初始化完毕 -->");
        int format = CodecUtil.selectColorFormat(CodecUtil.selectCodec(MediaFormat.MIMETYPE_VIDEO_AVC), MediaFormat.MIMETYPE_VIDEO_AVC);
        switch (format) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                NativeUtil.COLOR_FORMAT = 0;
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                NativeUtil.COLOR_FORMAT = 1;
                break;
        }
        // 初始化流通道
        jni.InitAndCapture(0, 2);
        jni.InitAndCapture(0, 3);
        fun_queryContext();
    }

    private void fun_queryContext() {
        try {
            InterfaceContext.pbui_MeetContextInfo contextInfo = jni.queryContextProperty(
                    InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_SELFID.getNumber());
            if (contextInfo == null) {
                return;
            }
            Values.localDevId = contextInfo.getPropertyval();
            LogUtils.i(TAG, "本机设备id=" + Values.localDevId);
            //会议目录权限
            jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYRIGHT_VALUE, 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);
            setDevName();
            fun_queryDevMeetInfo();
            fun_queryInterFaceConfiguration();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void setDevName() throws InvalidProtocolBufferException {
        if (Values.localDevId == 0) return;
        InterfaceDevice.pbui_Type_DeviceDetailInfo devInfoById = jni.queryDevInfoById(Values.localDevId);
        if (devInfoById == null) return;
        InterfaceDevice.pbui_Item_DeviceDetailInfo info = devInfoById.getPdevList().get(0);
        device_name_id.setText(getResources().getString(R.string.local_device_name, MyUtils.b2s(info.getDevname())));
        Values.localDevName = MyUtils.b2s(info.getDevname());
    }

    private void fun_queryDevMeetInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceFaceShowDetail DevMeetInfo = jni.queryDeviceMeetInfo();
            if (DevMeetInfo == null) return;
//            //缓存会议目录
//            jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY.getNumber(), 0, 0);
//            //缓存参会人信息(不然收不到参会人变更通知)
//            jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(), 1, 0);
//            //缓存排位信息(不然收不到排位变更通知)
//            jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT.getNumber(), 1, 0);
            queryMember = false;
            String company = MyUtils.b2s(DevMeetInfo.getCompany());
            String job = MyUtils.b2s(DevMeetInfo.getJob());
            String memberName = MyUtils.b2s(DevMeetInfo.getMembername());
            String meetingName = MyUtils.b2s(DevMeetInfo.getMeetingname());
            Values.localDevId = DevMeetInfo.getDeviceid();
            Values.localMemberId = DevMeetInfo.getMemberid();
            LogUtil.i(TAG, "fun_queryDevMeetInfo 设置本机参会人ID" + Values.localMemberId);
            signinType = DevMeetInfo.getSigninType();

            company_name.setText(getString(R.string.unit, company));
            main_memberJob.setText(getString(R.string.job, job));
            main_meetName.setText(meetingName);
            main_memberName.setText(memberName);
            Values.localMemberName = memberName;
            Values.meetingId = DevMeetInfo.getMeetingid();
            Values.roomId = DevMeetInfo.getRoomid();
            LogUtil.i(TAG, "fun_queryDevMeetInfo --> deviceid=" + DevMeetInfo.getDeviceid() + ",meetingid=" + DevMeetInfo.getMeetingid() + ",memberid=" + DevMeetInfo.getMemberid()
                    + ",roomid=" + DevMeetInfo.getRoomid() + ",meetingname=" + meetingName + ",membername= " + memberName + ",company=" + DevMeetInfo.getCompany().toStringUtf8() + ",job=" + DevMeetInfo.getJob().toStringUtf8());
            if (!TextUtils.isEmpty(meetingName) && TextUtils.isEmpty(memberName)) {
                if (ScanPop != null) ScanPop.dismiss();
                queryMember = true;
                fun_queryAttendPeople();
            } else if (!TextUtils.isEmpty(memberName)) {
                //已经绑定参会人了
                LogUtil.i(TAG, "fun_queryDevMeetInfo 已经绑定参会人了");
                if (chooseMemberPop != null && chooseMemberPop.isShowing()) {
                    chooseMemberPop.dismiss();
                }
                if (createMemberPop != null && createMemberPop.isShowing()) {
                    createMemberPop.dismiss();
                }
            }
            queryAdmins();
//            if (!isCached) {
//                isCached = true;
//                cacheAllFile();
//            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void cacheAllFile() {
        LogUtil.i(TAG, "cacheAllFile 缓存所有的文档和图片文件");
        try {
            InterfaceFile.pbui_Type_MeetDirDetailInfo dirInfo = jni.queryMeetDir();
            if (dirInfo == null) {
                isCached = false;
                return;
            }
            List<InterfaceFile.pbui_Item_MeetDirDetailInfo> itemList = dirInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceFile.pbui_Item_MeetDirDetailInfo item = itemList.get(i);
                //过滤掉子目录
                if (item.getParentid() != 0) continue;
                LogUtils.e(TAG,"当前目录="+item.getName().toStringUtf8());
                int dirId = item.getId();
                jni.queryDirPermission(dirId);
                if (dirId != Macro.ANNOTATION_FILE_DIRECTORY_ID) {
                    FileUtil.cacheDirFile(dirId);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryAdmins() {
//        InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo object = jni.placeDeviceRankingInfo(NativeService.roomId);
//        if (object == null) return;
//        filterMembers.clear();
//        List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> itemList = object.getItemList();
//        for (InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item : itemList) {
//            int role = item.getRole();
//            if (role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE) {
//                filterMembers.put(item.getMemberid(), item);
//            }
//        }
    }

    private void fun_queryInterFaceConfiguration() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo faceConfigInfo = jni.queryInterFaceConfiguration();
            if (faceConfigInfo == null) return;
            List<InterfaceFaceconfig.pbui_Item_FacePictureItemInfo> pictureList = faceConfigInfo.getPictureList();
            List<InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo> onlytextList = faceConfigInfo.getOnlytextList();
            List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = faceConfigInfo.getTextList();
            boolean logoIsHide = false;
            // TODO: 2018/10/15 收到所有界面配置的信息
            for (int i = 0; i < pictureList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FacePictureItemInfo itemInfo = pictureList.get(i);
                int faceid = itemInfo.getFaceid();
                int flag = itemInfo.getFlag();
                int mediaid = itemInfo.getMediaid();
                String userStr = "";
                if (itemInfo.getFaceid() == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MAINBG.getNumber()) {//主界面背景
                    userStr = Macro.DOWNLOAD_MAIN_BG;
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_LOGO.getNumber()) {//logo图标
                    boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
                    LogUtil.d(TAG, "更改logo显示  pictureList-->" + isShow);
                    if (isShow) {
                        main_logo_iv.setVisibility(View.VISIBLE);
                    } else {
                        main_logo_iv.setVisibility(View.GONE);
                        logoIsHide = true;
                    }
                    userStr = Macro.DOWNLOAD_MAIN_LOGO;
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SUBBG.getNumber()) {//子界面背景
                    userStr = Macro.DOWNLOAD_SUB_BG;
                }
                if (!TextUtils.isEmpty(userStr)) {
                    FileUtil.createDir(Macro.ROOT);
                    jni.creationFileDownload(Macro.ROOT + userStr + ".png", mediaid, 1, 0, userStr);
                }
            }
            for (int i = 0; i < onlytextList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo itemInfo = onlytextList.get(i);
                int faceid = itemInfo.getFaceid();
                int flag = itemInfo.getFlag();
                String text = MyUtils.b2s(itemInfo.getText());
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COLTDTEXT.getNumber()) {
                    main_unit.setText(text);
                }
            }
            for (int i = 0; i < textList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo = textList.get(i);
                int faceid = itemInfo.getFaceid();
                int flag = itemInfo.getFlag();
                int fontsize = itemInfo.getFontsize();
                int color = itemInfo.getColor();
                int align = itemInfo.getAlign();
                int fontflag = itemInfo.getFontflag();
                String fontName = MyUtils.b2s(itemInfo.getFontname());
                lx = itemInfo.getLx();
                ly = itemInfo.getLy();
                bx = itemInfo.getBx();
                by = itemInfo.getBy();
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEETNAME.getNumber()) {//会议名称
                    updateTv(R.id.main_meetName, fontflag, flag, fontsize, color, align, fontName);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERCOMPANY.getNumber()) {//参会人单位
                    updateTv(R.id.company_name, fontflag, flag, fontsize, color, align, fontName);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERNAME.getNumber()) {//参会人名称
                    updateTv(R.id.main_memberName, fontflag, flag, fontsize, color, align, fontName);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERJOB.getNumber()) {//参会人职业
                    updateTv(R.id.main_memberJob, fontflag, flag, fontsize, color, align, fontName);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SEATNAME.getNumber()) {//座席名称
                    updateTv(R.id.device_name_id, fontflag, flag, fontsize, color, align, fontName);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_TIMER.getNumber()) {//日期时间
                    updateDate(R.id.main_date_layout, fontflag, flag, fontsize, color, align, fontName);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COMPANY.getNumber()) {//单位名称
                    LogUtil.i(TAG, "单位名称控件");
                    updateTv(R.id.main_unit, fontflag, flag, fontsize, color, align, fontName);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_LOGO_GEO.getNumber()) {//Logo图标,只需要更新位置坐标
                    LogUtil.i(TAG, "logo图标控件");
                    update(R.id.main_logo_iv);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_checkin_GEO.getNumber()) {//进入会议按钮 text
                    updateBtn(R.id.mian_into_meeting, fontflag, flag, fontsize, color, align, fontName);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_manage_GEO.getNumber()) {//进入后台 text
                    updateBtn(R.id.main_secretary_manage, fontflag, flag, fontsize, color, align, fontName);
                }
            }
            if (logoIsHide) {
                moveMain_unit();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void moveMain_unit() {
        LogUtil.i(TAG, "更改logo显示  是否正确隐藏-->" + (main_logo_iv.getVisibility() == View.GONE));
//        logoIv.setVisibility(View.GONE);
        ConstraintSet set = new ConstraintSet();
        set.clone(root_layout_id);
        set.setHorizontalBias(R.id.main_unit, 0);
        set.applyTo(root_layout_id);
    }

    private void fun_queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MeetMemberDetailInfo pbui_type_meetMemberDetailInfo = jni.queryAttendPeopleDetailed();
            if (pbui_type_meetMemberDetailInfo == null) return;
            List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> itemList = pbui_type_meetMemberDetailInfo.getItemList();
            chooseMember.clear();
            for (InterfaceMember.pbui_Item_MeetMemberDetailInfo item : itemList) {
                LogUtil.i(TAG, "fun_queryAttendPeople 参会人员详细信息:人员ID：" + item.getMemberid() + ",设备ID：" + item.getDevid());
                if (item.getDevid() == 0) {
                    chooseMember.add(item);
                }
            }
            if (adapter == null) {
                adapter = new MemberAdapter(chooseMember);
            } else {
                adapter.notifyDataSetChanged();
                adapter.notifyChecks();
            }
            if (!queryMember) return;
            queryMember = false;
            if (!ischoosing) {
                ischoosing = true;
                chooseMember();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void upDateMainTimeUI(String[] strings) {
        if (strings != null) {
            main_now_date.setText(strings[0]);
            main_now_week.setText(strings[1]);
            main_now_time.setText(strings[2]);
        }
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
        if (!isOver) return;
        try {
            byte[] bytes = jni.queryDevicePropertiesById(InterfaceMacro.Pb_MeetDevicePropertyID.Pb_MEETDEVICE_PROPERTY_NETSTATUS_VALUE,
                    0);
            if (bytes == null) {
                tv_dev_online.setText(getString(R.string.Offline));
                return;
            }
            InterfaceDevice.pbui_DeviceInt32uProperty pbui_deviceInt32uProperty = InterfaceDevice.pbui_DeviceInt32uProperty.parseFrom(bytes);
            int propertyval = pbui_deviceInt32uProperty.getPropertyval();
            tv_dev_online.setText((propertyval == 1) ? R.string.online : R.string.Offline);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            initPermissions();
        }
    }

    // 复制ini、dev文件
    private void initConfFile() {
        FileUtils.createOrExistsDir(Macro.root_dir);
        boolean exists = FileUtils.isFileExists(Macro.root_dir + "/client.ini");
        //拷贝配置文件
        if (!exists) {
            copyTo("client.ini", Macro.root_dir, "client.ini");
        } else {
            if (iniUtil.load(IniUtil.inifile)) {//要确保load成功
                LogUtils.i(TAG, "加载ini文件成功");
                String streamprotol = iniUtil.get("selfinfo", "streamprotol");
                String disablemulticast = iniUtil.get("Audio", "disablemulticast");
                if (streamprotol == null || streamprotol.isEmpty() ||
                        disablemulticast == null || disablemulticast.isEmpty()) {
                    iniUtil.put("selfinfo", "streamprotol", 1);
                    iniUtil.put("Audio", "disablemulticast", 1);
                    iniUtil.store();//修改后提交
                }
                //设置版本信息
                setVersion();
                /** **** **  没有配置项则进行添加，且都是默认值  ** **** **/
                String disablebsf = iniUtil.get("nosdl", "disablebsf");
                String forcedecoce = iniUtil.get("nosdl", "forcedecoce");
                if (disablebsf == null || forcedecoce == null || disablebsf.isEmpty() || forcedecoce.isEmpty()) {
                    iniUtil.put("nosdl", "disablebsf", 0);
                    iniUtil.put("nosdl", "forcedecoce", 0);
                    iniUtil.store();//修改后提交
                }
            }
        }
        boolean devFileExists = FileUtils.isFileExists(Macro.root_dir + "/client.dev");
        if (devFileExists) {
            FileUtils.delete(Macro.root_dir + "/client.dev");
        }
        copyTo("client.dev", Macro.root_dir, "client.dev");
    }

    /**
     * 复制文件
     */
    private void copyTo(String fromPath, String toPath, String fileName) {
        // 复制位置
        // opPath：mnt/sdcard/lcuhg/health/
        // mnt/sdcard：表示sdcard
        File toFile = new File(toPath);
        // 如果不存在，创建文件夹
        if (!toFile.exists()) {
            boolean isCreate = toFile.mkdirs();
            // 打印创建结果
            LogUtil.i("create dir", String.valueOf(isCreate));
        }
        try {
            // 根据文件名获取assets文件夹下的该文件的inputstream
            InputStream fromFileIs = getResources().getAssets().open(fromPath);
            int length = fromFileIs.available(); // 获取文件的字节数
            byte[] buffer = new byte[length]; // 创建byte数组
            FileOutputStream fileOutputStream = new FileOutputStream(toFile + "/" + fileName); // 字节输入流
            BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    fromFileIs);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    fileOutputStream);
            int len = bufferedInputStream.read(buffer);
            while (len != -1) {
                bufferedOutputStream.write(buffer, 0, len);
                len = bufferedInputStream.read(buffer);
            }
            bufferedInputStream.close();
            bufferedOutputStream.close();
            fromFileIs.close();
            fileOutputStream.close();
            LogUtils.i(TAG, "copyTo方法 拷贝" + fromPath + "完成------");
            //确保有ini文件
            if (fromPath.equals("client.ini")) {
                LogUtil.d(TAG, "进入设置版本信息。。。。");
                setVersion();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 展示修改ini文件IP地址弹出框
     */
    private void showInputIniIp(String nowIp, String nowPort, String streamprotol, String disablemulticast) {
        View popupView = getLayoutInflater().inflate(R.layout.pop_ipfilter, null);
        ipEdtPop = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        MyUtils.setPopAnimal(ipEdtPop);
        ipEdtPop.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        ipEdtPop.setTouchable(true);
        ipEdtPop.setOutsideTouchable(true);
        IpViewHolder holder = new IpViewHolder(popupView);
        IPHolderEvent(holder, nowIp, nowPort, streamprotol, disablemulticast);
        ipEdtPop.showAtLocation(findViewById(R.id.root_layout_id), Gravity.CENTER, 0, 0);
    }

    private long lastTime = 0;
    private int clickCount = 0;

    private void IPHolderEvent(final IpViewHolder holder, String nowIp, String nowPort, String streamprotol, String disablemulticast) {
        holder.pop_version_tv.setOnClickListener(v -> {
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
        holder.btn_clear_file.setOnClickListener(v -> {
            if (FileUtil.deleteAllFile(new File(Macro.CACHE_ALL_FILE))) {
                ToastUtils.showShort(getString(R.string.clear_cache_file_successful));
            }
        });
        String ipStr = "";
        String portStr = "";
        if (streamprotol == null || streamprotol.isEmpty() || disablemulticast == null || disablemulticast.isEmpty()) {
            LogUtil.e(TAG, "IPHolderEvent :  没有则进行添加 --> ");
            iniUtil.put("selfinfo", "streamprotol", 1);
            iniUtil.put("Audio", "disablemulticast", 1);
            iniUtil.store();//修改后提交
        }
        //获取版本号
        String hardver = iniUtil.get("selfinfo", "hardver");
        String softver = iniUtil.get("selfinfo", "softver");
        holder.pop_version_tv.setText(getString(R.string.current_version, hardver + "." + softver));
        //获取最大码率值
        String maxBitRateStr = iniUtil.get("OtherConfiguration", "maxBitRate");
        int defaultMax = 100;
        if (maxBitRateStr != null && !maxBitRateStr.isEmpty()) {
            defaultMax = Integer.parseInt(maxBitRateStr);
            if (defaultMax < 100) defaultMax = 100;
            else if (defaultMax > 10000) defaultMax = 10000;
        }
        holder.edt_max_code_rate.setText(defaultMax + "");

        /** **** **  获取IP地址和端口号  ** **** **/
        if (nowIp != null && !nowIp.isEmpty()) {
            String[] ipSplit = nowIp.split("\\.");
            for (int i = 0; i < ipSplit.length; i++) {
                if (i == 0) ipStr = ipSplit[i];
                else ipStr += "." + ipSplit[i];
            }
        }
        if (nowPort != null && !nowPort.isEmpty()) {
            portStr = nowPort;
        }
        holder.ip_pop_edt1.setText(ipStr);
        holder.port_pop_edt.setText(portStr);

        /** **** **  获取TCP模式、禁用组播和编码过滤设置  ** **** **/
        streamprotol = iniUtil.get("selfinfo", "streamprotol");
        disablemulticast = iniUtil.get("Audio", "disablemulticast");
        String disablebsf = iniUtil.get("nosdl", "disablebsf");
        boolean use = false;
        boolean mulitcast = false;
        boolean bsf = false;
        try {
            int enable = Integer.parseInt(disablebsf);
            //=0表示开启编码过滤
            bsf = enable == 0;
            //等于1表示使用TCP模式
            int i1 = Integer.parseInt(streamprotol);
            use = i1 == 1;
            //等于1表示禁用组播
            int i2 = Integer.parseInt(disablemulticast);
            mulitcast = i2 == 1;
        } catch (NumberFormatException e) {
            LogUtil.e(TAG, " IPHolderEvent NumberFormatException 转换异常");
            e.printStackTrace();
        }
        holder.cb_marquee.setChecked(SharedPreferenceHelper.isMarquee(this));
        holder.disablebsf_cb.setChecked(bsf);
        holder.use_cb.setChecked(use);
        holder.disable_mulitcast_cb.setChecked(mulitcast);

        holder.btn_jump2off.setOnClickListener(v -> {
            jump2Login();
        });
        holder.ip_pop_confirm.setOnClickListener(v -> {
            String newIP = "";
            String newPort = "";
            newIP = holder.ip_pop_edt1.getText().toString().trim();
            newPort = holder.port_pop_edt.getText().toString().trim();
            String newMaxBitRate = holder.edt_max_code_rate.getText().toString().trim();
            boolean enable = holder.disablebsf_cb.isChecked();
            boolean useTCP = holder.use_cb.isChecked();
            boolean disableMulitcast = holder.disable_mulitcast_cb.isChecked();
            if (!newIP.isEmpty() && !newPort.isEmpty() && !newMaxBitRate.isEmpty()) {
                int maxRate = Integer.parseInt(newMaxBitRate);
                if (maxRate < 100) {
                    ToastUtils.showShort(R.string.error_less_then_100);
                    return;
                }
                if (maxRate > 10000) {
                    ToastUtils.showShort(R.string.error_more_then_10000);
                    return;
                }
                iniUtil.put("OtherConfiguration", "maxBitRate", maxRate);
                iniUtil.put("areaaddr", "area0ip", newIP);
                iniUtil.put("areaaddr", "area0port", newPort);
                iniUtil.put("nosdl", "disablebsf", enable ? 0 : 1);
                iniUtil.put("selfinfo", "streamprotol", useTCP ? 1 : 0);
                iniUtil.put("Audio", "disablemulticast", disableMulitcast ? 1 : 0);
                SharedPreferenceHelper.setData(this, SharedPreferenceHelper.key_marquee, holder.cb_marquee.isChecked());
                LogUtil.i(TAG, " 点击确定 maxBitRate= " + maxRate);
                iniUtil.store();//修改后提交
                /** **** **  app重启  ** **** **/
                ipEdtPop.dismiss();
                AppUtils.relaunchApp(true);
            } else {
                ToastUtils.showShort(R.string.tip_input_content);
            }
        });
        holder.ip_pop_cancel.setOnClickListener(v -> ipEdtPop.dismiss());
    }

    private void reSetIniFile() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title_restart_app)
                .setPositiveButton(R.string.ensure, (dialog, which) -> {
                    AppUtils.relaunchApp(true);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - millis > 2000) {
            ToastUtils.showShort(R.string.click_quit_the_application_again);
            millis = System.currentTimeMillis();
        } else {
            exit();
        }
    }

    private void exit() {
        unreceiverUpdate();
        /* **** **  清空临时存放的文件  ** **** */
        File f = new File(Macro.CACHE_FILE);
        FileUtil.deleteAllFile(f);
        finish();
    }

    private void initPermissions() {
        XXPermissions.with(this)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.RECORD_AUDIO
                        , Manifest.permission.CAMERA
                        , Manifest.permission.READ_PHONE_STATE
                )
                .constantRequest()//被拒绝后继续申请直到用户授权或者永久拒绝
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        LogUtils.i(TAG, "initPermissions :   --> hasPermission： " + granted.toString() + ",isAll=" + isAll);
                        //用户同意所有权限才开始初始化
                        if (isAll) {
                            start();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        LogUtils.e(TAG, "initPermissions :   --> 还未获取的权限有： " + denied.toString());
                        initPermissions();
                    }
                });
    }

    private void start() {
        if (App.CameraW == 0 || App.CameraH == 0) {
            new Thread(() -> initCameraSize(1)).start();
        }
        LogUtils.i(TAG, "start :  拥有了权限，进入开始方法 --> ");
        System.out.println("log日志写入文件路径=" + LogUtils.getCurrentLogFilePath());
        initConfFile();
        myApp.openNat(true);
        CheckNet();
    }

    private void setVersion() {
        try {
            PackageInfo packageInfo = getApplication().getPackageManager().getPackageInfo(getPackageName(), 0);
            iniUtil.load(IniUtil.inifile);
            String hardver = "";
            String softver = "";
            String versionName = packageInfo.versionName;
            LogUtil.e(TAG, "当前版本号： " + versionName);
            if (versionName.contains(".")) {
                hardver = versionName.substring(0, versionName.indexOf("."));
                softver = versionName.substring(versionName.indexOf(".") + 1);
            }
            String maxCodeRateStr = iniUtil.get("OtherConfiguration", "maxBitRate");
            int defaultMax = 500;
            if (maxCodeRateStr != null && !maxCodeRateStr.isEmpty()) {
                defaultMax = Integer.parseInt(maxCodeRateStr);
                if (defaultMax < 100) defaultMax = 100;
                else if (defaultMax > 10000) defaultMax = 10000;
            }
            App.setMaxBitRate(defaultMax);
            iniUtil.put("selfinfo", "hardver", hardver);
            iniUtil.put("selfinfo", "softver", softver);
            iniUtil.put("OtherConfiguration", "maxBitRate", defaultMax);
            iniUtil.store();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initCameraSize(int type) {
        PackageManager packageManager = getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                || packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            try {
                int numberOfCameras = Camera.getNumberOfCameras();//获取摄像机的个数 一般是前/后置两个
                if (numberOfCameras < 2) {
                    type = 0;//如果没有2个则说明只有后置像头
                    if (numberOfCameras < 1) {
                        return;
                    }
                }
                ArrayList<Integer> supportW = new ArrayList<>();
                ArrayList<Integer> supportH = new ArrayList<>();
                int largestW = 0, largestH = 0;
                Camera c = Camera.open(type);
                Camera.Parameters param = null;
                if (c != null)
                    param = c.getParameters();
                if (param == null) return;
                for (int i = 0; i < param.getSupportedPreviewSizes().size(); i++) {
                    int w = param.getSupportedPreviewSizes().get(i).width, h = param.getSupportedPreviewSizes().get(i).height;
                    supportW.add(w);
                    supportH.add(h);
                }
                for (int i = 0; i < supportH.size(); i++) {
                    try {
                        largestW = supportW.get(i);
                        largestH = supportH.get(i);
                        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", largestW, largestH);
                        if (MediaCodec.createEncoderByType("video/avc").getCodecInfo().getCapabilitiesForType("video/avc").isFormatSupported(mediaFormat)) {
                            if (largestW * largestH > App.CameraW * App.CameraH) {
                                App.CameraW = largestW;
                                App.CameraH = largestH;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (c != null) {
                            c.setPreviewCallback(null);
                            c.stopPreview();
                            c.release();
                            c = null;
                        }
                    }
                }
                LogUtil.e(TAG, "initCameraSize: 前置像素: cameraW=" + App.CameraW + " cameraH=" + App.CameraH);
                if (App.CameraW * App.CameraH > 1280 * 720) {
                    App.CameraW = 1280;
                    App.CameraH = 720;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startIntent(int action) {
        App.mediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (App.intent != null && App.result != 0) {
            LogUtil.i(TAG, "startIntent :  用户同意捕获屏幕 --->>> ");
            next_step();
        } else {
            startActivityForResult(App.mediaProjectionManager.createScreenCaptureIntent(), action);
        }
    }

    private void jump2Login() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    private void jumpToScanPage() {
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(CaptureActivity.KEY_NEED_BEEP, CaptureActivity.VALUE_BEEP);
        bundle.putBoolean(CaptureActivity.KEY_NEED_VIBRATION, CaptureActivity.VALUE_VIBRATION);
        bundle.putBoolean(CaptureActivity.KEY_NEED_EXPOSURE, CaptureActivity.VALUE_NO_EXPOSURE);
        bundle.putByte(CaptureActivity.KEY_FLASHLIGHT_MODE, CaptureActivity.VALUE_FLASHLIGHT_OFF);
        bundle.putByte(CaptureActivity.KEY_ORIENTATION_MODE, CaptureActivity.VALUE_ORIENTATION_AUTO);
        bundle.putBoolean(CaptureActivity.KEY_SCAN_AREA_FULL_SCREEN, CaptureActivity.VALUE_SCAN_AREA_FULL_SCREEN);
        bundle.putBoolean(CaptureActivity.KEY_NEED_SCAN_HINT_TEXT, CaptureActivity.VALUE_SCAN_HINT_TEXT);
        intent.putExtra(CaptureActivity.EXTRA_SETTING_BUNDLE, bundle);
        startActivityForResult(intent, REQUEST_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    App.result = resultCode;
                    App.intent = data;
                    //保存 MediaProjection 对象,解决每次录制屏幕时需要权限的问题
                    App.mediaProjection = App.mediaProjectionManager.getMediaProjection(resultCode, data);
                    next_step();
                }
            } else {
                this.finish();
            }
        } else if (requestCode == REQUEST_SCAN && resultCode == RESULT_OK) {
            if (null != data) {
                String stringExtra = data.getStringExtra(CaptureActivity.EXTRA_SCAN_RESULT);
//                {"meetid":"128","roomid":"12"}
                String a = stringExtra.substring(11);// 128","roomid":"12"}
                String meetingid = a.substring(0, a.indexOf("\""));// 128
                String roomid = a.substring(a.indexOf(":") + 2, a.lastIndexOf("\""));// :"12
                LogUtil.e(TAG, "扫码二维码结果 --> " + stringExtra + ",meetingid= " + meetingid + ",roomid=  " + roomid);
                bindingMeeting(meetingid);
            }
        }
    }

    private void bindingMeeting(String result) {
        try {
            parseInt = Integer.parseInt(result);
            fromMeetId = true;
            jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURMEETINGID.getNumber(),
                    parseInt);
            fun_queryMeetFromId();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        main_logo_iv = (ImageView) findViewById(R.id.main_logo_iv);
        company_name = (TextView) findViewById(R.id.company_name);
        main_close_iv = (ImageView) findViewById(R.id.main_close_iv);
        main_win_iv = (ImageView) findViewById(R.id.main_win_iv);
        main_win_iv.setOnClickListener(this);
        main_close_iv.setOnClickListener(this);
        tv_dev_online = (TextView) findViewById(R.id.tv_dev_online);
        main_meetName = (TextView) findViewById(R.id.main_meetName);
        main_memberName = (TextView) findViewById(R.id.main_memberName);
        main_secretary_manage = (Button) findViewById(R.id.main_secretary_manage);
        mian_into_meeting = (Button) findViewById(R.id.mian_into_meeting);
        device_name_id = (TextView) findViewById(R.id.device_name_id);
        main_memberJob = (TextView) findViewById(R.id.main_memberJob);
        main_unit = (TextView) findViewById(R.id.main_unit);
        main_now_time = (TextView) findViewById(R.id.main_now_time);
        main_now_date = (TextView) findViewById(R.id.main_now_date);
        main_now_week = (TextView) findViewById(R.id.main_now_week);
        main_date_ll = (LinearLayout) findViewById(R.id.main_date_ll);
        main_date_layout = (RelativeLayout) findViewById(R.id.main_date_layout);
        root_layout_id = (ConstraintLayout) findViewById(R.id.root_layout_id);

        main_secretary_manage.setOnClickListener(this);
        mian_into_meeting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mian_into_meeting:
                if (!isOver) {
                    ToastUtils.showShort(R.string.platform_is_not_yet_initialized);
                    break;
                }
                gotoMeet();
                break;
            case R.id.main_secretary_manage://修改IP地址|
                if (IniUtil.inifile.exists()) {
                    String nowIp = iniUtil.get("areaaddr", "area0ip");
                    String nowPort = iniUtil.get("areaaddr", "area0port");
                    String streamprotol = iniUtil.get("selfinfo", "streamprotol");
                    String disablemulticast = iniUtil.get("Audio", "disablemulticast");
                    showInputIniIp(nowIp, nowPort, streamprotol, disablemulticast);
                } else {
                    reSetIniFile();
                }
                break;
            case R.id.main_win_iv:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.main_close_iv:
                exit();
                break;
        }
    }

    public static class ChooseViewHolder {
        public View rootView;
        public RecyclerView member_rl;
        public Button ensure;
        public Button create_member;
        public Button cancel;

        public ChooseViewHolder(View rootView) {
            this.rootView = rootView;
            this.member_rl = (RecyclerView) rootView.findViewById(R.id.member_rl);
            this.ensure = (Button) rootView.findViewById(R.id.ensure);
            this.create_member = (Button) rootView.findViewById(R.id.create_member);
            this.cancel = (Button) rootView.findViewById(R.id.cancel);
        }

    }

    public static class IpViewHolder {
        public View rootView;
        public TextView title;
        public TextView pop_version_tv;
        public EditText ip_pop_edt1;
        public EditText port_pop_edt;
        public EditText edt_max_code_rate;
        public CheckBox disablebsf_cb;
        public CheckBox disable_mulitcast_cb;
        public CheckBox use_cb;
        public CheckBox cb_marquee;
        public Button btn_clear_file;
        public Button btn_jump2off;
        public Button ip_pop_cancel;
        public Button ip_pop_confirm;

        public IpViewHolder(View rootView) {
            this.rootView = rootView;
            this.title = (TextView) rootView.findViewById(R.id.title);
            this.pop_version_tv = (TextView) rootView.findViewById(R.id.pop_version_tv);
            this.ip_pop_edt1 = (EditText) rootView.findViewById(R.id.ip_pop_edt1);
            this.port_pop_edt = (EditText) rootView.findViewById(R.id.port_pop_edt);
            this.edt_max_code_rate = (EditText) rootView.findViewById(R.id.edt_max_code_rate);
            this.disablebsf_cb = (CheckBox) rootView.findViewById(R.id.disablebsf_cb);
            this.disable_mulitcast_cb = (CheckBox) rootView.findViewById(R.id.disable_mulitcast_cb);
            this.use_cb = (CheckBox) rootView.findViewById(R.id.use_cb);
            this.cb_marquee = (CheckBox) rootView.findViewById(R.id.cb_marquee);
            this.btn_clear_file = (Button) rootView.findViewById(R.id.btn_clear_file);
            this.btn_jump2off = (Button) rootView.findViewById(R.id.btn_jump2off);
            this.ip_pop_cancel = (Button) rootView.findViewById(R.id.ip_pop_cancel);
            this.ip_pop_confirm = (Button) rootView.findViewById(R.id.ip_pop_confirm);
        }

    }

    public static class CreateMemberViewHolder {
        public View rootView;
        public EditText edt_company;
        public EditText edt_name;
        public EditText edt_job;
        public EditText edt_phone;
        public EditText edt_email;
        public EditText edt_pwd;
        public Button ensure;
        public Button cancel;

        public CreateMemberViewHolder(View rootView) {
            this.rootView = rootView;
            this.edt_company = (EditText) rootView.findViewById(R.id.edt_company);
            this.edt_name = (EditText) rootView.findViewById(R.id.edt_name);
            this.edt_job = (EditText) rootView.findViewById(R.id.edt_job);
            this.edt_phone = (EditText) rootView.findViewById(R.id.edt_phone);
            this.edt_email = (EditText) rootView.findViewById(R.id.edt_email);
            this.edt_pwd = (EditText) rootView.findViewById(R.id.edt_pwd);
            this.ensure = (Button) rootView.findViewById(R.id.ensure);
            this.cancel = (Button) rootView.findViewById(R.id.cancel);
        }

    }
}
