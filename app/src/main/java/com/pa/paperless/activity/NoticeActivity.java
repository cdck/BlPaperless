package com.pa.paperless.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.LogUtil;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.MyUtils;
import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class NoticeActivity extends BaseActivity implements View.OnClickListener {
    public static HashMap<Integer, Activity> hashMap = new HashMap<>();
    private int bulletid;
    private ConstraintLayout notice_bg_view;
    private TextView notice_title_tv;
    private TextView notice_content_tv;
    private Button notice_close_btn;
    private ImageView notice_logo_iv;
    private NativeUtil jni;
    private int screenH;
    private int screenW;

    public static void jump(int bulletid, Context context) {
        if (hashMap.containsKey(bulletid)) {
            Activity activity = hashMap.get(bulletid);
            activity.finish();
            hashMap.remove(bulletid);
        } else {
            context.startActivity(new Intent(context, NoticeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("bulletid", bulletid));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        initView();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenW = metric.widthPixels; // 屏幕宽度（像素）
        screenH = metric.heightPixels; // 屏幕高度（像素）
        bulletid = getIntent().getIntExtra("bulletid", 0);
        if (bulletid != 0) {
            hashMap.put(bulletid, this);
        }
        EventBus.getDefault().register(this);
        jni = NativeUtil.getInstance();
        queryInterFaceConfiguration();
        queryAssignNotice();
    }

    private void queryAssignNotice() {
        try {
            InterfaceBullet.pbui_BulletDetailInfo notice = jni.queryAssignNotice(bulletid);
            List<InterfaceBullet.pbui_Item_BulletDetailInfo> itemList = notice.getItemList();
            InterfaceBullet.pbui_Item_BulletDetailInfo info = itemList.get(0);
            notice_title_tv.setText(MyUtils.b2s(info.getTitle()));
            notice_content_tv.setText(MyUtils.b2s(info.getContent()));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryInterFaceConfiguration() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo faceConfigInfo = jni.queryInterFaceConfiguration();
            List<InterfaceFaceconfig.pbui_Item_FacePictureItemInfo> pictureList = faceConfigInfo.getPictureList();
            for (int i = 0; i < pictureList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FacePictureItemInfo pbui_item_facePictureItemInfo = pictureList.get(i);
                int faceid = pbui_item_facePictureItemInfo.getFaceid();
                int mediaid = pbui_item_facePictureItemInfo.getMediaid();
                String userStr = "";
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinLogo_VALUE) {//公告logo
                    int flag = pbui_item_facePictureItemInfo.getFlag();
                    boolean isShow = InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE
                            == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE);
                    notice_logo_iv.setVisibility(isShow ? View.VISIBLE : View.GONE);
                    if (mediaid != 0) {
                        userStr = Macro.DOWNLOAD_NOTICE_LOGO;
                    }
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinBK_VALUE) {//公告背景图
                    if (mediaid != 0) {
                        userStr = Macro.DOWNLOAD_NOTICE_BG;
                    }
                }
                if (!userStr.isEmpty()) {
                    FileUtil.createDir(Macro.ROOT);
                    jni.creationFileDownload(Macro.ROOT + userStr + ".png", mediaid, 1, 0, userStr);
                }
            }
            List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = faceConfigInfo.getTextList();
            for (int i = 0; i < textList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info = textList.get(i);
                int faceid = info.getFaceid();
                int flag = info.getFlag();
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinBtn_VALUE) {//公告关闭按钮
                    LogUtil.e(TAG, "update :  公告关闭按钮 --> ");
                    updateBtn(R.id.notice_ac_close_btn, info);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinContent_VALUE) {//公告内容
                    LogUtil.e(TAG, "update :  公告内容 --> ");
                    updateTv(R.id.notice_ac_content_tv, info);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinTitle_VALUE) {//公告标题
                    LogUtil.e(TAG, "update :  公告标题 --> ");
                    updateTv(R.id.notice_ac_title_tv, info);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void updateTv(int resid, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info) {
        TextView tv = findViewById(resid);
        int color = info.getColor();
        int fontsize = info.getFontsize();
        int flag = info.getFlag();
        boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        tv.setVisibility(isShow ? View.VISIBLE : View.GONE);
        int fontflag = info.getFontflag();
        int align = info.getAlign();
        String fontName = MyUtils.b2s(info.getFontname());
        tv.setTextColor(color);
        tv.setTextSize(fontsize);
        update(resid, info);
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
            tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT.getNumber()) {//右对齐
            tv.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
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

    private void updateBtn(int resid, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info) {
        Button btn = findViewById(resid);
        String fontName = MyUtils.b2s(info.getFontname());
        int align = info.getAlign();
        int flag = info.getFlag();
        boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        btn.setVisibility(isShow ? View.VISIBLE : View.GONE);
        int fontflag = info.getFontflag();
        btn.setTextColor(info.getColor());
        btn.setTextSize(info.getFontsize());
        update(resid, info);
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

    private final String TAG = "NoticeActivity-->";

    private void update(int resid, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info) {
        float ly = info.getLy();
        float lx = info.getLx();
        float bx = info.getBx();
        float by = info.getBy();
        LogUtil.e(TAG, "update :   --> 左上角：(" + lx + "," + ly + ") , 右下角：(" + bx + "," + by + ")");
        ConstraintSet set = new ConstraintSet();
        set.clone(notice_bg_view);
        //设置控件的大小
        float width = (bx - lx) / 100 * screenW;
        float height = (by - ly) / 100 * screenH;
        LogUtil.e(TAG, "update :   --> width:" + width + ", height:" + height);
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
        LogUtil.d(TAG, "update: biasX= " + biasX + ",biasY= " + biasY);
        set.setHorizontalBias(resid, biasX);
        set.setVerticalBias(resid, biasY);
        set.applyTo(notice_bg_view);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(final EventMessage message) {
        switch (message.getAction()) {
            case EventType.ICC_changed_inform://界面配置变更通知
                queryInterFaceConfiguration();
                break;
            case EventType.CLOSE_NOTICE_INFORM://停止公告通知
                clearAll();
//                InterfaceBullet.pbui_Type_StopBulletMsg object = (InterfaceBullet.pbui_Type_StopBulletMsg) message.getObject();
//                int bulletid = object.getBulletid();
//                if (hashMap.containsKey(bulletid)) {
//                    Activity activity = hashMap.get(bulletid);
//                    activity.finish();
//                    hashMap.remove(bulletid);
//                }
                break;
            case EventType.NOTICE_LOGO_PNG_TAG:
                String filepath = (String) message.getObject();
                LogUtil.e(TAG, "getEventMessage :  公告logo下载完成 --> " + filepath);
                notice_logo_iv.setImageDrawable(Drawable.createFromPath(filepath));
//                Glide.with(this)
//                        .load(filepath)
//                        .skipMemoryCache(true)// 不使用内存缓存
//                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
//                        .into(notice_logo_iv);
                break;
            case EventType.NOTICE_BG_PNG_TAG:
                String filepath1 = (String) message.getObject();
                LogUtil.e(TAG, "getEventMessage :  公告背景下载完成 --> " + filepath1);
                notice_bg_view.setBackground(Drawable.createFromPath(filepath1));
                break;
        }
    }

    private void clearAll() {
        Set<Integer> integers = hashMap.keySet();
        for (int key : integers) {
            hashMap.get(key).finish();
        }
        hashMap.clear();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initView() {
        notice_bg_view = findViewById(R.id.notice_bg_view);
        notice_title_tv = findViewById(R.id.notice_ac_title_tv);
        notice_content_tv = findViewById(R.id.notice_ac_content_tv);
        notice_close_btn = findViewById(R.id.notice_ac_close_btn);
        notice_logo_iv = findViewById(R.id.notice_ac_logo_iv);

        notice_close_btn.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        clearAll();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.notice_ac_close_btn:
                hashMap.remove(bulletid);
                finish();
                break;
        }
    }
}
