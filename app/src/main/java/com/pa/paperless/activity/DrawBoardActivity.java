package com.pa.paperless.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceWhiteboard;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.BoardAdapter;
import com.pa.paperless.data.bean.DevMember;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.service.FabService;
import com.pa.paperless.ui.ColorPickerDialog;
import com.pa.paperless.ui.DrawBoard;
import com.pa.paperless.ui.DrawBoardSeekBar;
import com.pa.paperless.utils.ConvertUtil;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.PopUtils;

import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static com.pa.paperless.ui.DrawBoard.LocalPathList;
import static com.pa.paperless.ui.DrawBoard.LocalSharingPathList;
import static com.pa.paperless.ui.DrawBoard.pathList;
import static com.pa.paperless.ui.DrawBoard.points;
import static com.pa.paperless.utils.ConvertUtil.bmp2bs;
import static com.pa.paperless.utils.ConvertUtil.bs2bmp;
import static com.pa.paperless.utils.MyUtils.getMediaid;

public class DrawBoardActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "DrawBoardActivity-->";
    private List<ImageView> colors;
    private String screenshot, have_pic;
    private List<TextView> tools;
    private int mWidth, mHeight;
    private Resources resources;
    private Bitmap screenshotBmp;
    private DrawBoard drawBoard;
    private boolean isAddScreenShot;//是否在发起共享时,添加截图图片
    public static boolean sharing;
    public static int launchPersonId = Values.localMemberId;//默认发起的人员ID是本机
    public static int mSrcmemid = Values.localMemberId;//发起的人员ID默认是本机
    public static long mSrcwbid;//发起人的白板标识
    private int IMAGE_CODE = 1;
    public static List<Integer> localOperids = new ArrayList<>();//存放本机的操作ID
    public static String newName;
    public File uploadPicFile;
    private boolean clicked;//是否点击发起批注
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos;
    private List<DevMember> onLineMembers;
    private BoardAdapter onLineBoardMemberAdapter;
    private PopupWindow mMemberPop, mChooseMemberPop;
    private List<Integer> wandTogetherIDs;

    public static int disposePicOpermemberid;
    public static int disposePicSrcmemid;
    public static long disposePicSrcwbidd;
    public static List<Integer> togetherIDs = new ArrayList<>();
    public static ByteString savePicData;//图片数据
    public static ByteString tempPicData;//临时图片数据
    private PointF pointF;
    private DrawBoardActivity cxt;
    private long launchSrcwbid;
    private int launchSrcmemid;
    private NativeUtil jni = NativeUtil.getInstance();

    DrawBoardSeekBar seb;
    private TextView width_tv;
    private ImageView color_black;
    private ImageView color_red;
    private ImageView color_yellow;
    private ImageView color_gray;
    private ImageView color_blue;
    private ImageView color_violet;
    private ImageView color_green;
    private ImageView color_orange;
    private FrameLayout board_fl;
    private TextView pelette_exit;
    private TextView pelette_save;
    private TextView type_face;
    private Button share_start;
    private Button share_stop;
    private LinearLayout root_layout_id;
    private TextView clean;
    private TextView pic;
    private TextView back;
    private TextView palette;
    private TextView round;
    private TextView rect;
    private TextView linee;
    private TextView sline;
    private TextView paint;
    private TextView text;
    private TextView eraser;
    private TextView drag_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_board);
        initView();
        cxt = this;
        resources = getResources();
        initColorImg();
        initToolsTv();
        getIntentData();
        board_fl.post(() -> {
            mWidth = board_fl.getWidth();
            mHeight = board_fl.getHeight();
            LogUtil.e(TAG, "DrawBoardActivity.run :  画板的宽高: --> " + mWidth + "," + mHeight);
            drawBoard = new DrawBoard(getApplicationContext(), mWidth, mHeight);
            board_fl.addView(drawBoard);
            begin();
        });
        EventBus.getDefault().register(this);
    }

    private void begin() {
        if (screenshot != null && screenshot.equals("postilpic")) {
            if (FabService.bytes != null) {
                screenshotBmp = ConvertUtil.bytes2Bitmap(FabService.bytes);
                Bitmap b = screenshotBmp;
                LogUtil.e(TAG, "DrawBoardActivity.draw 808行:  图片的宽高 --->>> " + screenshotBmp.getWidth() + "," + screenshotBmp.getHeight());
                drawBoard.drawZoomBmp(b);
                isAddScreenShot = true;
            }
        }
        if (have_pic != null && have_pic.equals("have_pic") && savePicData != null) {
            drawBoard.drawZoomBmp(bs2bmp(savePicData));
        }
        //点击绘制文本时的回调
        drawBoard.setListener((x, y) -> {
            //创建编辑文本框PopupWindow
            PopUtils.PopBuilder.createPopupWindow(R.layout.edt_txt,
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                    board_fl, Gravity.CENTER, 0, 0, true, new PopUtils.ClickListener() {
                        @Override
                        public void setUplistener(PopUtils.PopBuilder builder) {

                        }

                        @Override
                        public void setOnDismissListener(PopUtils.PopBuilder builder) {
                            //pop隐藏时获取文本并调用画文本方法
                            LogUtil.e(TAG, "DrawBoardActivity.setOnDismissListener :   --> ");
                            EditText edt = builder.getView(R.id.edt_txt);
                            String s = edt.getText().toString();
                            if (!s.equals("")) {
                                //当PopupWindow隐藏时调用绘制文本方法
                                drawBoard.drawText(x, y, s);
                                edt.setText("");
                            }
                        }
                    });
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.MEMBER_CHANGE_INFORM:// 参会人员变更通知
                fun_queryAttendPeople();
                break;
            case EventType.PLACE_DEVINFO_CHANGEINFORM:// 会场设备信息变更通知
                //6.查询设备信息
                if (this.memberInfos != null) {
                    fun_queryDeviceInfo();
                }
                break;
            case EventType.OPEN_BOARD://收到白板打开操作
                receiveOpenWhiteBoard(message);
                break;
            case EventType.AGREED_JOIN://同意加入通知
                agreedJoin(message);
                break;
            case EventType.REJECT_JOIN://拒绝加入通知
                InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper object1 = (InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper) message.getObject();
                long srcwbid1 = object1.getSrcwbid();
                int srcmemid1 = object1.getSrcmemid();
                if (mSrcmemid == 0) {
                    if (srcmemid1 == launchSrcwbid && srcwbid1 == launchSrcwbid)
                        whoToast(object1.getOpermemberid(), resources.getString(R.string.tip_repulse_join));
                } else {
                    if (srcwbid1 == mSrcwbid && srcmemid1 == mSrcmemid) //发起人的白板标识和人员ID一致
                        whoToast(object1.getOpermemberid(), resources.getString(R.string.tip_repulse_join));
                }
                break;
            case EventType.EXIT_WHITE_BOARD://参会人员退出白板通知
                exitDrawBoard(message);
                break;
            case EventType.ADD_DRAW_INFORM://添加矩形、直线、圆形通知
                receiveAddLine(message);
                break;
            case EventType.ADD_INK_INFORM://添加墨迹通知
                receiveAddInk(message);
                break;
            case EventType.ADD_DRAW_TEXT://添加文本通知
                receiveAddText(message);
                break;
            case EventType.is_share_pic://添加图片通知
                receiveAddPic(message);
            case EventType.WHITEBROADE_DELETE_RECOREINFORM://白板删除记录通知
                receiveDeleteEmptyRecore(message, 1);
                break;
            case EventType.WHITEBOARD_EMPTY_RECORDINFORM://白板清空记录通知
                receiveDeleteEmptyRecore(message, 2);
                break;
            case EventType.seetbar_progress:
                int progress = seb.getProgress();
                drawBoard.setPaintWidth(progress);
                width_tv.setText(progress + "");
                break;
            default:
                break;
        }
    }

    private void receiveDeleteEmptyRecore(EventMessage message, int type) {
        if (!sharing) {
            LogUtil.e(TAG, "DrawBoardActivity.receiveDeleteEmptyRecore :  不在共享中 --> ");
            return;
        }
        InterfaceWhiteboard.pbui_Type_MeetClearWhiteBoard object = (InterfaceWhiteboard.pbui_Type_MeetClearWhiteBoard) message.getObject();
        int operid = object.getOperid();
        int opermemberid = object.getOpermemberid();
        long srcwbid = object.getSrcwbid();
        if (togetherIDs.contains(opermemberid)) {
            LogUtil.e(TAG, "DrawBoardActivity.receiveDeleteEmptyRecore :  白板删除记录通知EventBus --->>> ");
            if (type == 1) { //删除
                //1.先清空画板
                drawBoard.initCanvas();
                //2.删除指定的路径
                for (int i = 0; i < pathList.size(); i++) {
                    if (pathList.get(i).operid == operid && pathList.get(i).opermemberid == opermemberid
                            && pathList.get(i).srcwbid == srcwbid) {
                        LogUtil.e(TAG, "DrawBoardActivity.receiveDeleteEmptyRecore :  确认过眼神 --> ");
                        pathList.remove(i);
                        i--;
                    }
                }
            } else if (type == 2) {//清空
                drawBoard.initCanvas();
                //遍历删除多个，for循环不能删除多个，因为pathList删除后长度会改变，而 i 作为索引一直在增加
                Iterator<DrawBoard.DrawPath> iterator = pathList.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().opermemberid == opermemberid /*&& iterator.next().srcwbid == srcwbid*/) {
                        LogUtil.d(TAG, "receiveDeleteEmptyRecore: 删除全部..");
                        iterator.remove();
                    }
                }
            }
            //删除后重新绘制不需要删除的
            drawBoard.drawAgain(pathList);
            //因为清空了画板，所以自己绘制的要重新再绘制
            drawBoard.drawAgain(LocalPathList);
        }
    }

    private void receiveAddInk(EventMessage message) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardInkItem object = (InterfaceWhiteboard.pbui_Type_MeetWhiteBoardInkItem) message.getObject();
        int operid = object.getOperid();
        //当前发送端的人员ID,用来判断是否是正在一起同屏的对象发的墨迹操作
        int opermemberid = object.getOpermemberid();
        int figuretype = object.getFiguretype();
        int srcmemid = object.getSrcmemid();
        long srcwbid = object.getSrcwbid();
        int linesize = object.getLinesize();
        int argb = object.getArgb();
        List<Float> pinklistList = object.getPinklistList();
        LogUtil.e(TAG, "DrawBoardActivity.receiveAddInk :  收到添加墨迹操作EventBus --->>> 白板标识=" + srcwbid + ",  发起人ID=" + srcmemid + "; 对比=" + mSrcwbid + "," + mSrcmemid);
        if (pinklistList.size() > 0) {
            if (srcmemid == mSrcmemid && srcwbid == mSrcwbid) {
                //自己不是发起人的时候,每次收到绘画通知都要判断是不是同一个发起人和白板标识
                //并且集合中没有这一号人,将其添加进集合中
                if (!togetherIDs.contains(opermemberid))
                    togetherIDs.add(opermemberid);
            }
            if (!sharing) {
                LogUtil.e(TAG, "DrawBoardActivity.receiveAddInk :  不在共享中 --> ");
                return;
            }
            if (togetherIDs.contains(opermemberid)) {
                LogUtil.e(TAG, "DrawBoardActivity.receiveAddInk 266行:   接收到的xy个数--->>> " + object.getPinklistCount() + " , " + pinklistList.size());
                points.clear();
                for (int i = 0; i < pinklistList.size(); i++) {
                    Float aFloat = pinklistList.get(i);
                    if (i % 2 == 0) {
                        pointF = new PointF();
                        pointF.x = aFloat;
                    } else {
                        pointF.y = aFloat;
                        drawBoard.setCanvasSize((int) pointF.x, (int) pointF.y);
                        points.add(pointF);
                    }
                }
                //新建 paint 和 path
                Paint newPaint = getNewPaint(linesize, argb);
                Path allInkPath = new Path();
                PointF p1 = new PointF();
                PointF p2 = new PointF();
                //绘画
                float sx, sy;
                if (figuretype == InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_INK.getNumber()) {
                    p1.x = points.get(0).x;
                    p1.y = points.get(0).y;
                    Path newPath = new Path();
                    sx = p1.x;
                    sy = p1.y;
                    newPath.moveTo(p1.x, p1.y);
                    for (int i = 1; i < points.size() - 1; i++) {
                        p2.x = points.get(i).x;
                        p2.y = points.get(i).y;
                        float dx = Math.abs(p2.x - sx);
                        float dy = Math.abs(p2.y - sy);
                        if (dx >= 3 || dy >= 3) {
                            float cx = (p2.x + sx) / 2;
                            float cy = (p2.y + sy) / 2;
                            newPath.quadTo(sx, sy, cx, cy);
                        }
                        drawBoard.mCanvas.drawPath(newPath, newPaint);
                        drawBoard.invalidate();
                        sx = p2.x;
                        sy = p2.y;
                        allInkPath.addPath(newPath);
                    }
                    DrawBoard.DrawPath drawPath = new DrawBoard.DrawPath();
                    drawPath.paint = newPaint;
                    drawPath.path = allInkPath;
                    drawPath.operid = operid;
                    drawPath.srcwbid = srcwbid;
                    drawPath.srcmemid = srcmemid;
                    drawPath.opermemberid = opermemberid;
                    //将路径保存到共享中绘画信息
                    pathList.add(drawPath);
                    points.clear();
                }
            }
        }
    }

    private void receiveAddPic(EventMessage message) {
        InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail object = (InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail) message.getObject();
        LogUtil.e(TAG, "DrawBoardActivity.receiveAddPic :  收到添加图片操作EventBus --->>> ");
        int operid = object.getOperid();
        int srcmemid = object.getSrcmemid();
        long srcwbid = object.getSrcwbid();
        ByteString rPicData = object.getPicdata();
        int opermemberid = object.getOpermemberid();
        if (object.getSrcmemid() == mSrcmemid && object.getSrcwbid() == mSrcwbid) {
            //自己不是发起人的时候,每次收到绘画通知都要判断是不是同一个发起人和白板标识
            //并且集合中没有这一号人,将其添加进集合中
            if (!togetherIDs.contains(opermemberid))
                togetherIDs.add(opermemberid);
        }
        if (togetherIDs.contains(opermemberid)) {
            drawBoard.drawZoomBmp(bs2bmp(rPicData));
            /** **** **  保存  ** **** **/
            DrawBoard.DrawPath drawPath = new DrawBoard.DrawPath();
            drawPath.operid = operid;
            drawPath.srcwbid = srcwbid;
            drawPath.srcmemid = srcmemid;
            drawPath.opermemberid = opermemberid;
            drawPath.picdata = rPicData;
            //将路径保存到共享中绘画信息
            pathList.add(drawPath);
        }
    }

    private void receiveAddText(EventMessage message) {
        if (!sharing) {
            LogUtil.e(TAG, "DrawBoardActivity.receiveAddText :  不在共享中 --> ");
            return;
        }
        InterfaceWhiteboard.pbui_Item_MeetWBTextDetail object = (InterfaceWhiteboard.pbui_Item_MeetWBTextDetail) message.getObject();
        LogUtil.e(TAG, "DrawBoardActivity.receiveAddText :  添加文本通知EventBus --->>> ");
        int operid = object.getOperid();
        int opermemberid = object.getOpermemberid();
        int srcmemid = object.getSrcmemid();
        long srcwbid = object.getSrcwbid();
        long utcstamp = object.getUtcstamp();
        int figuretype = object.getFiguretype();
        int fontsize = object.getFontsize();
        int fontflag = object.getFontflag();// Normal/Bold/Italic/Bold Italic
        LogUtil.e(TAG, "DrawBoardActivity.receiveAddText 394行:  文本大小 --->>> " + fontsize);
        int argb = object.getArgb();
        String fontname = MyUtils.b2s(object.getFontname());// 宋体/黑体...
        float lx = object.getLx();  // 获取到文本起点x
        float ly = object.getLy();  // 获取到文本起点y
        String ptext = MyUtils.b2s(object.getPtext());  // 文本内容
        LogUtil.e(TAG, "DrawBoardActivity.receiveAddText :  收到的文本内容 --->>> " + ptext);
        if (object.getSrcmemid() == mSrcmemid && object.getSrcwbid() == mSrcwbid) {
            //自己不是发起人的时候,每次收到绘画通知都要判断是不是同一个发起人和白板标识
            //并且集合中没有这一号人,将其添加进集合中
            if (!togetherIDs.contains(opermemberid)) {
                togetherIDs.add(opermemberid);
            }
        }
        if (togetherIDs.contains(opermemberid)) {
            if (figuretype == InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_FREETEXT.getNumber()) {
                if (lx < 0) {
                    lx = 0;
                }
                if (ly < 0) {
                    ly = 0;
                }
                if (lx > mWidth) {
                    lx = mWidth;
                }
                if (ly > mHeight) {
                    ly = mHeight;
                }
                int x = (int) (lx);
                int y = (int) (ly);
                Paint newPaint = getNewPaint(3, argb);
                if (fontsize < 30) fontsize = 30;//设置一个最小值,不然文字会太小
                newPaint.setTextSize(fontsize);
                newPaint.setFlags(fontflag);
                newPaint.setStyle(Paint.Style.FILL_AND_STROKE);

                Rect rect = new Rect();
                newPaint.getTextBounds(ptext, 0, ptext.length(), rect);
                int width = rect.width();//所有文本的宽度
                int height = rect.height();//文本的高度（用于换行显示）
                int remainWidth = mWidth - x;//可容许显示文本的宽度
                int ilka = width / ptext.length();//每个文本的宽度
                int canSee = remainWidth / ilka;//可以显示的文本个数

                if (remainWidth < width) {// 小于所有文本的宽度（不够显示）
                    drawBoard.funDraw(newPaint, height, canSee - 1, x, y, ptext);
                } else {//足够空间显示则直接画出来
                    drawBoard.mCanvas.drawText(ptext, lx, ly, newPaint);
                }
                drawBoard.invalidate();
                DrawBoard.DrawPath drawPath = new DrawBoard.DrawPath();
                drawPath.opermemberid = opermemberid;
                drawPath.operid = operid;
                drawPath.srcwbid = srcwbid;
                drawPath.srcmemid = srcmemid;
                drawPath.paint = newPaint;
                drawPath.height = height;
                drawPath.text = ptext;
                drawPath.pointF = new PointF(x, y);
                drawPath.cansee = canSee;
                drawPath.lw = remainWidth;
                drawPath.rw = width;
                pathList.add(drawPath);
            }
        }
    }

    private void receiveAddLine(EventMessage message) {
        if (!sharing) {
            LogUtil.e(TAG, "DrawBoardActivity.receiveAddLine :  不在共享中 --> ");
            return;
        }
        InterfaceWhiteboard.pbui_Item_MeetWBRectDetail object = (InterfaceWhiteboard.pbui_Item_MeetWBRectDetail) message.getObject();
        LogUtil.e(TAG, "DrawBoardActivity.receiveAddLine : 收到添加矩形、直线、圆形通知EventBus--->>> 发起的人员ID： " + object.getSrcmemid());
        int operid = object.getOperid();
        int opermemberid = object.getOpermemberid();
        int srcmemid2 = object.getSrcmemid();
        long srcwbid2 = object.getSrcwbid();
        long utcstamp = object.getUtcstamp();
        int figuretype = object.getFiguretype();
        int linesize = object.getLinesize();
        int color = object.getArgb();
        List<Float> ptList = object.getPtList();
        if (object.getSrcmemid() == mSrcmemid && object.getSrcwbid() == mSrcwbid) {
            //自己不是发起人的时候,每次收到绘画通知都要判断是不是同一个发起人和白板标识
            //并且集合中没有这一号人,将其添加进集合中
            if (!togetherIDs.contains(opermemberid)) {
                togetherIDs.add(opermemberid);
            }
        }

        if (togetherIDs.contains(opermemberid)) {
            Paint newPaint = getNewPaint(linesize, color);
            Path newPath = new Path();
            float[] allPoint = getFloats(ptList);
            float maxX = Math.max(allPoint[0], allPoint[2]);
            float maxY = Math.max(allPoint[1], allPoint[3]);
            drawBoard.setCanvasSize((int) maxX, (int) maxY);
            LogUtil.d(TAG, "receiveAddLine: 收到的四个点: 长度:" + allPoint.length + ",起点: "
                    + allPoint[0] + " , " + allPoint[1] + ",  终点:  " + allPoint[2] + " , " + allPoint[3]);
            //根据图形类型绘制
            if (figuretype == InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_RECTANGLE.getNumber()) {
                //矩形
                newPath.addRect(allPoint[0], allPoint[1], allPoint[2], allPoint[3], Path.Direction.CW);
            } else if (figuretype == InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_LINE.getNumber()) {
                //直线
                newPath.moveTo(allPoint[0], allPoint[1]);
                newPath.lineTo(allPoint[2], allPoint[3]);
            } else if (figuretype == InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_ELLIPSE.getNumber()) {
                //圆
                newPath.addOval(allPoint[0], allPoint[1], allPoint[2], allPoint[3], Path.Direction.CW);
            }
            drawBoard.mCanvas.drawPath(newPath, newPaint);
            drawBoard.invalidate();
            DrawBoard.DrawPath drawPath = new DrawBoard.DrawPath();
            drawPath.paint = newPaint;
            drawPath.path = newPath;
            drawPath.operid = operid;
            drawPath.srcwbid = srcwbid2;
            drawPath.srcmemid = srcmemid2;
            drawPath.opermemberid = opermemberid;
            //将路径保存到共享中绘画信息
            pathList.add(drawPath);
        }
    }

    private float[] getFloats(List<Float> ptList) {
        float[] allPoint = new float[4];
        for (int i = 0; i < ptList.size(); i++) {
            Float aFloat = ptList.get(i);
            switch (i) {
                case 0:
                    allPoint[0] = aFloat;
                    break;
                case 1:
                    allPoint[1] = aFloat;
                    break;
                case 2:
                    allPoint[2] = aFloat;
                    break;
                case 3:
                    allPoint[3] = aFloat;
                    break;
            }
        }
        return allPoint;
    }

    @NonNull
    private Paint getNewPaint(int linesize, int color) {
        Paint newPaint = new Paint();
        newPaint.setColor(color);
        newPaint.setStrokeWidth(linesize);
        newPaint.setStyle(Paint.Style.STROKE);// 画笔样式：实线
        PorterDuffXfermode mode2 = new PorterDuffXfermode(
                PorterDuff.Mode.DST_OVER);
        newPaint.setXfermode(null);// 转换模式
        newPaint.setAntiAlias(true);// 抗锯齿
        newPaint.setDither(true);// 防抖动
        newPaint.setStrokeJoin(Paint.Join.ROUND);// 设置线段连接处的样式为圆弧连接
        newPaint.setStrokeCap(Paint.Cap.ROUND);// 设置两端的线帽为圆的
        return newPaint;
    }

    private void exitDrawBoard(EventMessage message) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper object = (InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper) message.getObject();
        int srcmemid = object.getSrcmemid();
        long srcwbid = object.getSrcwbid();
        int opermemberid = object.getOpermemberid();
        if (srcmemid == mSrcmemid && srcwbid == mSrcwbid) {//如果是一起同屏的才操作
            if (togetherIDs.contains(opermemberid)) {//集合中有当前这个人
                for (int i = 0; i < togetherIDs.size(); i++) {
                    if (togetherIDs.get(i) == opermemberid) {
                        togetherIDs.remove(i);//删除
                        i--;
                        whoToast(opermemberid, resources.getString(R.string.tip_exit_the_shared));
                        if (togetherIDs.size() == 0 && mSrcmemid == Values.localMemberId) {
                            //自己发起的时才退出,因为如果是本人是发起人,
                            //可能还有其他人在共享中但是没有操作,所以你只是没有添加到集合中而已
                            LogUtil.e(TAG, "DrawBoardActivity.getEventMessage :  没有人在共享了,退出共享 --> ");
                            stopShare();
                        }
                    }
                }
            }
        }
    }

    private void whoToast(int opermemberid, String something) {
        if (memberInfos != null) {
            for (int i = 0; i < memberInfos.size(); i++) {
                if (memberInfos.get(i).getPersonid() == opermemberid) {
                    ToastUtils.showShort(memberInfos.get(i).getName().toStringUtf8() + something);
                }
            }
        }
    }

    private void agreedJoin(EventMessage message) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper object3 = (InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper) message.getObject();
        int opermemberid = object3.getOpermemberid();
        long srcwbid = object3.getSrcwbid();
        int srcmemid = object3.getSrcmemid();
        if (srcwbid == launchSrcwbid && srcmemid == launchSrcmemid) {
            mSrcmemid = launchSrcmemid;
            mSrcwbid = launchSrcwbid;
            launchSrcmemid = 0;
            launchSrcwbid = 0;
            togetherIDs.clear();
        }
        LogUtil.e(TAG, "DrawBoardActivity.agreedJoin :收到同意加入通知  srcwbid: " + srcwbid + ",mSrcwbid: " + mSrcwbid + "    || mSrcmemid:  " + mSrcmemid + " , srcmemid : " + srcmemid);
        if (srcwbid == mSrcwbid && srcmemid == mSrcmemid) {//发起人的白板标识和人员ID一致
            togetherIDs.add(opermemberid);//添加到正在一起共享的人员ID集合中
            LogUtil.e(TAG, "DrawBoardActivity.agreedJoin :   --->>>  ID: " + opermemberid + " 同意加入,  大小: " + togetherIDs.size());
            whoToast(opermemberid, resources.getString(R.string.tip_join_the_sharing));
            sharing = true;
        }
    }

    private void receiveOpenWhiteBoard(EventMessage message) {
        InterfaceWhiteboard.pbui_Type_MeetStartWhiteBoard object1 = (InterfaceWhiteboard.pbui_Type_MeetStartWhiteBoard) message.getObject();
        LogUtil.e(TAG, "DrawBoardActivity.receiveOpenWhiteBoard 612行:   --->>> 画板中收到白板打开操作");
        int operflag = object1.getOperflag();
        ByteString medianame = object1.getMedianame();
        disposePicOpermemberid = object1.getOpermemberid();
        disposePicSrcmemid = object1.getSrcmemid();
        disposePicSrcwbidd = object1.getSrcwbid();
        LogUtil.e(TAG, "DrawBoardActivity.receiveOpenWhiteBoard :  收到白板打开操作 --> 人员ID:" + disposePicSrcmemid + ",白板标识:" + disposePicSrcwbidd);
        if (operflag == InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_FORCEOPEN.getNumber()) {
            LogUtil.e(TAG, "DrawBoardActivity.receiveOpenWhiteBoard 619行:   --->>> 这是强制式打开白板");
            togetherIDs.clear();
            //强制打开白板  直接强制同意加入
            jni.agreeJoin(Values.localMemberId, disposePicSrcmemid, disposePicSrcwbidd);
            sharing = true;//如果同意加入就设置已经在共享中
            mSrcwbid = disposePicSrcwbidd;//发起人的白板标识
            mSrcmemid = disposePicSrcmemid;//设置发起的人员ID
            togetherIDs.add(mSrcmemid);//添加到同屏人员集合中
            if (tempPicData != null) {
                savePicData = tempPicData;
                tempPicData = null;
                drawBoard.drawZoomBmp(bs2bmp(savePicData));
            }
        } else if (operflag == InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_REQUESTOPEN.getNumber()) {
            //询问打开白板
            WhetherOpen(disposePicSrcmemid, disposePicSrcwbidd, MyUtils.b2s(medianame), disposePicOpermemberid);
        }
    }

    private void WhetherOpen(final int srcmemid, final long srcwbId, String mediaName, final int opermemberid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
        builder.setTitle(getString(R.string.title_whether_agree_join, mediaName));
        builder.setPositiveButton(getString(R.string.agree), (dialog, which) -> {
            togetherIDs.clear();
            //同意加入
            jni.agreeJoin(Values.localMemberId, srcmemid, srcwbId);
            sharing = true;//如果同意加入就设置已经在共享中
            mSrcmemid = srcmemid;//设置发起的人员ID
            mSrcwbid = srcwbId;//设置发起人的白板标识
            togetherIDs.add(mSrcmemid);//添加到同屏人员集合中
            LogUtil.e(TAG, "DrawBoardActivity.WhetherOpen :   --> 添加: 人员ID为 " + srcmemid + " 的参会人后集合大小:" + togetherIDs.size());
            if (tempPicData != null) {
                savePicData = tempPicData;
                tempPicData = null;
                drawBoard.drawZoomBmp(bs2bmp(savePicData));
                /** **** **  保存  ** **** **/
                DrawBoard.DrawPath drawPath = new DrawBoard.DrawPath();
                drawPath.operid = Values.operid;
                Values.operid = 0;
                drawPath.srcwbid = srcwbId;
                drawPath.srcmemid = srcmemid;
                drawPath.opermemberid = opermemberid;
                drawPath.picdata = savePicData;
                //将路径保存到共享中绘画信息
                pathList.add(drawPath);
            }
            dialog.dismiss();
        });
        builder.setNegativeButton(getString(R.string.reject), (dialog, which) -> {
            jni.rejectJoin(Values.localMemberId, srcmemid, srcwbId);
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void showOlLineMember() {
        View popupView = getLayoutInflater().inflate(R.layout.board_pop_member, null);
        mMemberPop = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        MyUtils.setPopAnimal(mMemberPop);
        mMemberPop.setBackgroundDrawable(new BitmapDrawable());
        mMemberPop.setTouchable(true);
        mMemberPop.setOutsideTouchable(true);
        ViewHolder holder = new ViewHolder(popupView);
        holder_event(holder);
        mMemberPop.showAtLocation(findViewById(R.id.root_layout_id), Gravity.CENTER, 0, 0);
    }

    private void holder_event(final ViewHolder holder) {
        holder.all_mer_cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && onLineBoardMemberAdapter != null && onLineMembers != null) {
                onLineBoardMemberAdapter.setAllCheck(true);
            }
        });
        holder.choose_mer_cb.setOnClickListener(view -> {
            showChooseOnLineMember();
            mMemberPop.dismiss();
        });
        holder.ensure.setOnClickListener(view -> {
            if (wandTogetherIDs == null) wandTogetherIDs = new ArrayList<Integer>();
            else wandTogetherIDs.clear();
            if (onLineBoardMemberAdapter != null)
                wandTogetherIDs = onLineBoardMemberAdapter.getChecks();
            if (wandTogetherIDs.size() > 0) {
                // 发起共享批注  强制：Pb_MEETPOTIL_FLAG_FORCEOPEN  Pb_MEETPOTIL_FLAG_REQUESTOPEN
                if (mSrcmemid == 0) {
                    //当前已经在同屏中,并且自己是发起人;则操作ID不需要重新获取
                    launchSrcwbid = System.currentTimeMillis();
                    launchSrcmemid = Values.localMemberId;
                } else {
                    launchSrcwbid = mSrcwbid;
                    launchSrcmemid = mSrcmemid;
                }
                LogUtil.e(TAG, "DrawBoardActivity.onClick 1846:  发起时的白板标识和人员ID --->>> " + launchSrcwbid + " , " + launchSrcmemid);
                jni.coerceStartWhiteBoard(InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_REQUESTOPEN.getNumber(),
                        Values.localMemberName, Values.localMemberId,
                        launchSrcmemid, launchSrcwbid, wandTogetherIDs);
                LogUtil.i(TAG, "DrawBoardActivity.onClick :1847行  是否从截图批注端启动的画板.. --> " + isAddScreenShot);
                if (isAddScreenShot) {//从截图批注端启动的画板
                    isAddScreenShot = false;
                    addScreenShot();
                }
            }
            mMemberPop.dismiss();
        });
        holder.cancel.setOnClickListener(view -> mMemberPop.dismiss());
    }

    private void addScreenShot() {
//        /** **** **  发送添加图片  ** **** **/
//        int width = screenshotBmp.getWidth();
//        int height = screenshotBmp.getHeight();
//        float cw = 1;
//        float ch = 1;
//        float newWidth = mWidth / 2 + (mWidth / 3);
//        float newHeight = mHeight / 2 + (mHeight / 3);
//        if (width > mWidth && height > mHeight) {
//            cw = newWidth / width;
//            ch = newHeight / height;
//        } else if (width > mWidth) {
//            cw = newWidth / width;
//            ch = cw;
//        } else if (height > mHeight) {
//            ch = newHeight / height;
//            cw = ch;
//        }
//        // 取得想要缩放的matrix参数
//        Matrix matrix = new Matrix();
//        matrix.postScale(cw, ch);
//        // 得到新的图片
//        screenshotBmp = Bitmap.createBitmap(screenshotBmp, 0, 0, width, height, matrix, true);
//        width = screenshotBmp.getWidth();
//        height = screenshotBmp.getHeight();
//        int x = (mWidth / 2) - (width / 2);
//        int y = (mHeight / 2) - (height / 2);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            screenshotBmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bytes = baos.toByteArray();
            ByteString picdata = ByteString.copyFrom(bytes);
            long time = System.currentTimeMillis();
            int operid = (int) (time / 10);
            localOperids.add(operid);
            DrawBoard.DrawPath drawPath = new DrawBoard.DrawPath();
            drawPath.picdata = picdata;
            LocalPathList.add(drawPath);
            LocalSharingPathList.add(drawPath);
            LogUtil.d(TAG, "onTouch: LocalPathList.size() : " + LocalPathList.size());
            jni.addPicture(operid, Values.localMemberId, launchSrcmemid, launchSrcwbid, time,
                    InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_PICTURE.getNumber(), 0, 0, picdata);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!screenshotBmp.isRecycled()) screenshotBmp.recycle();
        }
    }

    private void showChooseOnLineMember() {
        View popupView = getLayoutInflater().inflate(R.layout.board_choose_member, null);
        mChooseMemberPop = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mChooseMemberPop.setClippingEnabled(false);
        MyUtils.setPopAnimal(mChooseMemberPop);
        mChooseMemberPop.setBackgroundDrawable(new BitmapDrawable());
        mChooseMemberPop.setTouchable(true);
        mChooseMemberPop.setOutsideTouchable(true);
        ChooseMemberViewHolder holder = new ChooseMemberViewHolder(popupView);
        choose_holder(holder);
        mChooseMemberPop.showAtLocation(findViewById(R.id.root_layout_id), Gravity.CENTER, 0, 0);
    }

    private void choose_holder(final ChooseMemberViewHolder holder) {
        onLineBoardMemberAdapter.setItemClick((view, posion) -> {
            onLineBoardMemberAdapter.setCheck(onLineMembers.get(posion).getMemberDetailInfo().getPersonid());
            holder.board_all_check.setChecked(onLineBoardMemberAdapter.isAllCheck());
            onLineBoardMemberAdapter.notifyDataSetChanged();
        });
        holder.board_all_check.setOnClickListener(v -> {
            boolean checked = holder.board_all_check.isChecked();
            holder.board_all_check.setChecked(checked);
            onLineBoardMemberAdapter.setAllCheck(checked);
        });
        holder.ensure.setOnClickListener(view -> {
            if (wandTogetherIDs == null) wandTogetherIDs = new ArrayList<Integer>();
            else wandTogetherIDs.clear();
            if (onLineBoardMemberAdapter != null)
                wandTogetherIDs = onLineBoardMemberAdapter.getChecks();
            if (wandTogetherIDs.size() > 0) {
                // 发起共享批注  强制：Pb_MEETPOTIL_FLAG_FORCEOPEN  Pb_MEETPOTIL_FLAG_REQUESTOPEN
                if (mSrcmemid == 0) {
                    //当前已经在同屏中,并且自己是发起人;则操作ID不需要重新获取
                    launchSrcwbid = System.currentTimeMillis();
                    launchSrcmemid = Values.localMemberId;
                } else {
                    launchSrcwbid = mSrcwbid;
                    launchSrcmemid = mSrcmemid;
                }
                LogUtil.e(TAG, "DrawBoardActivity.onClick 1980行:  发起时的白板标识人员ID --->>> " + launchSrcwbid + "," + launchSrcmemid);
                LogUtil.i(TAG, "DrawBoardActivity.onClick :1981行  是否从截图批注端启动的画板.. --> " + isAddScreenShot);
                jni.coerceStartWhiteBoard(InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_REQUESTOPEN.getNumber(),
                        Values.localMemberName, Values.localMemberId,
                        launchSrcmemid, launchSrcwbid, wandTogetherIDs);
                if (isAddScreenShot) {//从截图批注端启动的画板
                    isAddScreenShot = false;
                    addScreenShot();
                }
            }
            mChooseMemberPop.dismiss();
        });
        holder.cancel.setOnClickListener(v -> mChooseMemberPop.dismiss());
    }

    private void getIntentData() {
        Intent intent = getIntent();
        screenshot = intent.getStringExtra("postilpic");
        have_pic = intent.getStringExtra("have_pic");
    }

    private void initToolsTv() {
        tools = new ArrayList<>();
        tools.add(round);
        tools.add(rect);
        tools.add(linee);
        tools.add(sline);
        tools.add(paint);
        tools.add(text);
        tools.add(eraser);
        tools.add(drag_tv);
    }

    private void initColorImg() {
        colors = new ArrayList<>();
        colors.add(color_black);
        colors.add(color_red);
        colors.add(color_yellow);
        colors.add(color_gray);
        colors.add(color_blue);
        colors.add(color_violet);
        colors.add(color_green);
        colors.add(color_orange);
    }

    private void setImgColorSelect(int index) {
        for (int i = 0; i < colors.size(); i++) {
            colors.get(i).setSelected(i == index);
        }
    }

    private void setToolsSelect(int index) {
        for (int i = 0; i < tools.size(); i++) {
            if (i == index) {
                tools.get(i).setSelected(true);
                drawBoard.drag = index == 7;
            } else {
                tools.get(i).setSelected(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy: ......");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        LocalPathList.clear();
        LocalSharingPathList.clear();
        localOperids.clear();
        togetherIDs.clear();
        pathList.clear();
        tempPicData = null;
        savePicData = null;
        FabService.bytes = null;
        mSrcmemid = 0;
        mSrcwbid = 0;
        disposePicOpermemberid = 0;
        disposePicSrcmemid = 0;
        disposePicSrcwbidd = 0;
        uploadPicFile = null;
        if (sharing) {
            stopShare();
        }
        drawBoard.destroyDrawingCache();
        drawBoard = null;
        cxt = null;
    }

    private void stopShare() {
        togetherIDs.clear();
        List<Integer> alluserid = new ArrayList<>();
        alluserid.add(Values.localMemberId);
        jni.broadcastStopWhiteBoard(InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_EXIT.getNumber(),
                getString(R.string.exit_white_board), Values.localMemberId, mSrcmemid, mSrcwbid, alluserid);
        sharing = false;
        mSrcwbid = 0;
    }

    private void openChooseFile() {
        Intent i = new Intent(ACTION_OPEN_DOCUMENT);//打开图片
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e(TAG, "onActivityResult: ....1...");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            // 获取选中文件的uri
            LogUtil.d(TAG, "onActivityResult: data.toString : " + data.toString());
            Uri uri = data.getData();
            File file = UriUtils.uri2File(uri);
            if (file != null) {
                // 执行操作
                Bitmap dstbmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                //将图片绘制到画板中
                Bitmap bitmap = drawBoard.drawZoomBmp(dstbmp);
                //保存图片信息
                DrawBoard.DrawPath drawPath = new DrawBoard.DrawPath();
                ByteString picdata = bmp2bs(bitmap);
                drawPath.picdata = picdata;
                LocalPathList.add(drawPath);
                if (sharing) {
                    try {
                        long time = System.currentTimeMillis();
                        int operid = (int) (time / 10);
                        localOperids.add(operid);
                        LocalSharingPathList.add(drawPath);
                        jni.addPicture(operid, Values.localMemberId, mSrcmemid, mSrcwbid, time,
                                InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_PICTURE.getNumber(), 0, 0, bmp2bs(bitmap));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void fun_queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo object1 = jni.queryAttendPeople();
            if (object1 == null) return;
            if (memberInfos == null) memberInfos = new ArrayList<>();
            else memberInfos.clear();
            memberInfos.addAll(object1.getItemList());
            if (memberInfos != null) {
                fun_queryDeviceInfo();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryDeviceInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo object2 = jni.queryDeviceInfo();
            if (object2 == null) return;
            List<InterfaceDevice.pbui_Item_DeviceDetailInfo> pdevList = object2.getPdevList();
            if (onLineMembers == null) onLineMembers = new ArrayList<>();
            else onLineMembers.clear();
            for (int i = 0; i < pdevList.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo detailInfo = pdevList.get(i);
                int netState = detailInfo.getNetstate();
                int devId = detailInfo.getDevcieid();
                int memberId = detailInfo.getMemberid();
                if (netState == 1) {//在线状态
                    for (int j = 0; j < memberInfos.size(); j++) {
                        InterfaceMember.pbui_Item_MemberDetailInfo memberInfo = memberInfos.get(j);
                        if (memberInfo.getPersonid() == memberId && devId != Values.localDevId)
                            onLineMembers.add(new DevMember(memberInfo, devId));
                    }
                }
            }
            if (onLineBoardMemberAdapter == null) {
                onLineBoardMemberAdapter = new BoardAdapter(onLineMembers);
            } else {
                onLineBoardMemberAdapter.notifyDataSetChanged();
                onLineBoardMemberAdapter.notifyChecks();
            }
            if (clicked) {
                clicked = false;
                if (onLineMembers.size() > 0) {//不在共享中并且拥有数据才能打开弹出框
                    showOlLineMember();
                } else if (onLineMembers.size() == 0) {
                    ToastUtils.showShort(R.string.no_mamber);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void showDig() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText edt = new EditText(this);
        builder.setTitle(resources.getString(R.string.please_enter_file_name));
        edt.setHint(resources.getString(R.string.please_enter_file_name));
        edt.setText(System.currentTimeMillis() + "");
        edt.setSelection(edt.getText().toString().length());
        builder.setView(edt);
        builder.setPositiveButton(resources.getString(R.string.save_server), (dialog, which) -> {
            newName = edt.getText().toString().trim();
            if (newName.equals("")) {
                ToastUtils.showShort(R.string.please_enter_file_name);
            } else if (!FileUtil.isLegalName(newName)) {
                ToastUtils.showShort(R.string.tip_file_name_unlawfulness);
            } else {
                savePicLocal(newName, true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(resources.getString(R.string.save_local), (dialog, which) -> {
            final String s = edt.getText().toString().trim();
            if (s.equals("")) {
                ToastUtils.showShort(R.string.please_enter_file_name);
            } else if (!FileUtil.isLegalName(s)) {
                ToastUtils.showShort(R.string.tip_file_name_unlawfulness);
            } else {
                savePicLocal(s, false);
                ToastUtils.showShort(R.string.tip_save_as, Macro.POSTIL_FILE);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void savePicLocal(final String fileName, final boolean isUpload) {
//        drawBoard.setDrawingCacheEnabled(true);
//        Bitmap bitmap = Bitmap.createBitmap(drawBoard.getDrawingCache());
//        drawBoard.setDrawingCacheEnabled(false);
        Bitmap bitmap1 = drawBoard.getCanvasBmp();
        //重新创建一个，画板获取的bitmap对象会自动回收掉
        Bitmap bitmap = Bitmap.createBitmap(bitmap1);
        FileUtil.createDir(Macro.POSTIL_FILE);
        File uploadPicFile = new File(Macro.POSTIL_FILE, fileName + ".png");
        FileUtil.saveBitmap(bitmap, uploadPicFile);
        Timer tupload = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isUpload) {
                    /** **** **  上传到服务器  ** **** **/
                    String path = uploadPicFile.getPath();
                    int mediaid = getMediaid(path);
                    String fileEnd = path.substring(path.lastIndexOf(".") + 1, path.length()).toLowerCase();
                    jni.uploadFile(InterfaceMacro.Pb_Upload_Flag.Pb_MEET_UPLOADFLAG_ONLYENDCALLBACK.getNumber(),
                            2, 0, fileName + "." + fileEnd, path, 0, Macro.upload_drawBoard_pic);
                }
            }
        };
        tupload.schedule(timerTask, 2000);//  5秒后运行上传
    }

    private void initView() {
        width_tv = (TextView) findViewById(R.id.width_tv);
        seb = (DrawBoardSeekBar) findViewById(R.id.seb);

        color_black = (ImageView) findViewById(R.id.color_black);
        color_black.setOnClickListener(this);
        color_red = (ImageView) findViewById(R.id.color_red);
        color_red.setOnClickListener(this);
        color_yellow = (ImageView) findViewById(R.id.color_yellow);
        color_yellow.setOnClickListener(this);
        color_gray = (ImageView) findViewById(R.id.color_gray);
        color_gray.setOnClickListener(this);
        color_blue = (ImageView) findViewById(R.id.color_blue);
        color_blue.setOnClickListener(this);
        color_violet = (ImageView) findViewById(R.id.color_violet);
        color_violet.setOnClickListener(this);
        color_green = (ImageView) findViewById(R.id.color_green);
        color_green.setOnClickListener(this);
        color_orange = (ImageView) findViewById(R.id.color_orange);
        color_orange.setOnClickListener(this);

        board_fl = (FrameLayout) findViewById(R.id.board_fl);
        pelette_exit = (TextView) findViewById(R.id.pelette_exit);
        pelette_exit.setOnClickListener(this);
        pelette_save = (TextView) findViewById(R.id.pelette_save);
        pelette_save.setOnClickListener(this);
        type_face = (TextView) findViewById(R.id.type_face);
        type_face.setOnClickListener(this);
        share_start = (Button) findViewById(R.id.share_start);
        share_start.setOnClickListener(this);
        share_stop = (Button) findViewById(R.id.share_stop);
        share_stop.setOnClickListener(this);
        root_layout_id = (LinearLayout) findViewById(R.id.root_layout_id);

        clean = (TextView) findViewById(R.id.clean);
        clean.setOnClickListener(this);
        pic = (TextView) findViewById(R.id.pic);
        pic.setOnClickListener(this);
        back = (TextView) findViewById(R.id.back);
        back.setOnClickListener(this);
        palette = (TextView) findViewById(R.id.palette);
        palette.setOnClickListener(this);
        round = (TextView) findViewById(R.id.round);
        round.setOnClickListener(this);
        rect = (TextView) findViewById(R.id.rect);
        rect.setOnClickListener(this);
        linee = (TextView) findViewById(R.id.linee);
        linee.setOnClickListener(this);
        sline = (TextView) findViewById(R.id.sline);
        sline.setOnClickListener(this);
        paint = (TextView) findViewById(R.id.paint);
        paint.setOnClickListener(this);
        text = (TextView) findViewById(R.id.text);
        text.setOnClickListener(this);
        eraser = (TextView) findViewById(R.id.eraser);
        eraser.setOnClickListener(this);
        drag_tv = (TextView) findViewById(R.id.drag_tv);
        drag_tv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.color_black:
                setImgColorSelect(0);
                drawBoard.setPaintColor(Color.BLACK);
                break;
            case R.id.color_red:
                setImgColorSelect(1);
                drawBoard.setPaintColor(Color.RED);
                break;
            case R.id.color_yellow:
                setImgColorSelect(2);
                drawBoard.setPaintColor(Color.YELLOW);
                break;
            case R.id.color_gray:
                setImgColorSelect(3);
                drawBoard.setPaintColor(Color.GRAY);
                break;
            case R.id.color_blue:
                setImgColorSelect(4);
                drawBoard.setPaintColor(Color.BLUE);
                break;
            case R.id.color_violet:
                setImgColorSelect(5);
                drawBoard.setPaintColor(Color.rgb(123, 62, 213));
                break;
            case R.id.color_green:
                setImgColorSelect(6);
                drawBoard.setPaintColor(Color.GREEN);
                break;
            case R.id.color_orange:
                setImgColorSelect(7);
                drawBoard.setPaintColor(Color.rgb(255, 157, 0));
                break;
            case R.id.pelette_exit:
                if (sharing) {
                    stopShare();
                    finish();
                } else {
                    finish();
                }
                break;
            case R.id.clean:
                drawBoard.clear();
                break;
            case R.id.pelette_save:
                showDig();
                break;
            case R.id.pic:
                openChooseFile();
                break;
            case R.id.back:
                drawBoard.undo();
                break;
            case R.id.palette:
                new ColorPickerDialog(this, color -> drawBoard.setPaintColor(color), Color.BLACK).show();
                break;
            case R.id.type_face://字体
                break;
            case R.id.sline:
                drawBoard.setDrawMode(1);
                setToolsSelect(3);
                break;
            case R.id.paint:
                drawBoard.setDrawMode(1);
                setToolsSelect(4);
                break;
            case R.id.round:
                drawBoard.setDrawMode(2);
                setToolsSelect(0);
                break;
            case R.id.rect:
                drawBoard.setDrawMode(3);
                setToolsSelect(1);
                break;
            case R.id.linee:
                drawBoard.setDrawMode(4);
                setToolsSelect(2);
                break;
            case R.id.text:
                drawBoard.setDrawMode(5);
                setToolsSelect(5);
                break;
            case R.id.eraser:
                drawBoard.setDrawMode(6);
                setToolsSelect(6);
                break;
            case R.id.drag_tv:
                setToolsSelect(7);
                break;
            case R.id.share_start:
                clicked = true;
                fun_queryAttendPeople();
                break;
            case R.id.share_stop:
                if (sharing) stopShare();
                else {
                    ToastUtils.showShort(R.string.out_of_share);
                }
                break;
            case R.id.ensure:
                break;
            case R.id.cancel:
                break;
        }
    }


    public static class ChooseMemberViewHolder {
        public View rootView;
        public CheckBox board_all_check;
        public RecyclerView board_member_rl;
        public Button ensure;
        public Button cancel;

        public ChooseMemberViewHolder(View rootView) {
            this.rootView = rootView;
            this.board_all_check = (CheckBox) rootView.findViewById(R.id.board_all_check);
            this.board_member_rl = (RecyclerView) rootView.findViewById(R.id.board_member_rl);
            this.ensure = (Button) rootView.findViewById(R.id.ensure);
            this.cancel = (Button) rootView.findViewById(R.id.cancel);
        }

    }

    public static class ViewHolder {
        public View rootView;
        public RadioButton all_mer_cb;
        public RadioButton choose_mer_cb;
        public Button ensure;
        public Button cancel;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.all_mer_cb = (RadioButton) rootView.findViewById(R.id.all_mer_cb);
            this.choose_mer_cb = (RadioButton) rootView.findViewById(R.id.choose_mer_cb);
            this.ensure = (Button) rootView.findViewById(R.id.ensure);
            this.cancel = (Button) rootView.findViewById(R.id.cancel);
        }

    }
}
