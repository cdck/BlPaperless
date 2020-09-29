package com.pa.paperless.video;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pa.boling.paperless.R;
import com.pa.paperless.activity.BaseActivity;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.service.FabService;
import com.pa.paperless.utils.ConvertUtil;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.video.opengles.MyGLSurfaceView;
import com.pa.paperless.video.opengles.WlOnGlSurfaceViewOncreateListener;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import androidx.constraintlayout.widget.ConstraintLayout;

import static com.pa.paperless.data.constant.Values.isMandatory;
import static com.pa.paperless.service.NativeService.haveNewPlayInform;


/**
 * @author xlk
 */
public class VideoActivity extends BaseActivity implements View.OnClickListener, IVideo, WlOnGlSurfaceViewOncreateListener {

    private MyGLSurfaceView surfaceView;
    private final String TAG = "VideoActivity-->";
    private TextView videoCurrentTimeTv, videoTotalTimeTv;
    private SeekBar videoSeekBar;
    private VideoPresenter presenter;
    private Button videoPlayOrPauseBtn;
    private Button videoStopBtn;
    private Button videoScreenShotBtn;
    private Button videoStartScreenBtn;
    private Button videoStopScreenBtn;
    private PopupWindow bottomPop;
    /**
     * 保存当前播放的进度和时长
     */
    private int lastPer;
    private String lastSec = "";
    private String lastTotal = "";
    private View mView;
    private LinearLayout video_progress_linear;
    private int playAction, subtype;
    private ImageView opticalDisk;
    private ImageView plectrum;
    private RelativeLayout play_mp3_view;
    private TextView video_top_title;
    private ConstraintLayout video_root;
    private ObjectAnimator plectrumAnimator;
    private ObjectAnimator opticalDiskAnimator;
    private int mStatus = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initView();
        presenter = new VideoPresenter(this, this);
        presenter.registerEventBus();
        showVideoOrMusicUI(getIntent());
        if (isMandatory) {
            setCanNotExit();
        }
    }

    private void showVideoOrMusicUI(Intent intent) {
        playAction = intent.getIntExtra("action", 0);
        subtype = intent.getIntExtra("subtype", 0);
        Values.videoIsShowing = true;
        if (subtype == Macro.MEDIA_FILE_TYPE_MP3) {
            //如果当前播放的是mp3文件，则只显示MP3控件
            play_mp3_view.setVisibility(View.VISIBLE);
            surfaceView.setVisibility(View.GONE);
            presenter.releasePlay();
        } else {
            play_mp3_view.setVisibility(View.GONE);
            surfaceView.setVisibility(View.VISIBLE);
            surfaceView.setOnGlSurfaceViewOncreateListener(this);
            if (playAction == EventType.PLAY_STREAM_NOTIFY) {
                int deivceid = intent.getIntExtra("deivceid", -1);
                String devName = presenter.queryDevName(deivceid);
                LogUtil.i(TAG, "showVideoOrMusicUI devName=" + devName);
                video_top_title.setText(devName);
            }
        }
        if (bottomPop != null && bottomPop.isShowing()) {
            video_progress_linear.setVisibility(playAction == EventType.PLAY_STREAM_NOTIFY ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        showVideoOrMusicUI(intent);
        super.onNewIntent(intent);
    }

    @Override
    public void close() {
        isMandatory = false;//将强制性播放重置为false
        haveNewPlayInform = false;
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".close :   --->>> ");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!haveNewPlayInform) {
                    finish();
                } else {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 500);
    }

    @Override
    protected void onDestroy() {
        if (bottomPop != null && bottomPop.isShowing()) {
            bottomPop.dismiss();
        }
        releaseAll();
        super.onDestroy();
    }

    private void releaseAll() {
        Values.videoIsShowing = false;
        bottomPop = null;
        presenter.unregisterEventBus();
        presenter.stopScreen();
        presenter.releaseMediaRes();
        presenter.releasePlay();
        surfaceView.destroy();
    }

    @Override
    public void setCodecType(int codecType) {
        surfaceView.setCodecType(codecType);
    }

    @Override
    public void setFrameData(int w, int h, byte[] y, byte[] u, byte[] v) {
        setCodecType(0);
        surfaceView.setFrameData(w, h, y, u, v);
    }

    @Override
    public void updateProgressUi(int per, String sec, String total) {
        if (videoSeekBar != null && videoCurrentTimeTv != null && videoTotalTimeTv != null) {
            lastPer = per;
            lastSec = sec;
            lastTotal = total;
            videoSeekBar.setProgress(per);
            videoCurrentTimeTv.setText(sec);
            videoTotalTimeTv.setText(total);
        }
    }

    @Override
    public void updateTopTitle(String topTitle) {
        video_top_title.setText(topTitle);
    }

    @Override
    public void onBackPressed() {
        if (!isMandatory) {
            super.onBackPressed();
        }
    }

    @Override
    public void setCanNotExit() {
        LogUtil.d(TAG, "setCanNotExit -->" + "设置不能操作");
        if (bottomPop != null && bottomPop.isShowing()) {
            bottomPop.dismiss();
        }
        video_root.setClickable(false);
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public void onClick(View v) {
        mView = v;
        if (bottomPop != null && bottomPop.isShowing()) {
            video_top_title.setVisibility(View.GONE);
            bottomPop.dismiss();
            return;
        }
        video_top_title.setVisibility(View.VISIBLE);
        createBottomPop(mView);
    }

    private void createBottomPop(View parent) {
        LogUtil.e(TAG, "createBottomPop :  创建PopupWindow --> ");
        View inflate = LayoutInflater.from(this).inflate(R.layout.video_bottom_pop, null);
        initPopView(inflate);
        //如果是播放流则隐藏掉进度和时间控件
        LogUtil.i(TAG, "createBottomPop playAction=" + playAction);
        video_progress_linear.setVisibility(playAction == EventType.PLAY_STREAM_NOTIFY ? View.GONE : View.VISIBLE);
        bottomPop = new PopupWindow(inflate, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        bottomPop.setTouchable(true);
        bottomPop.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bottomPop.setOutsideTouchable(true);
        // 设置焦点
        bottomPop.setFocusable(true);
        bottomPop.setAnimationStyle(R.style.Anim_PopupWindow);
        bottomPop.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        bottomPop.setOnDismissListener(() -> video_top_title.setVisibility(View.GONE));
        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                presenter.setPlayPlace(progress);
            }
        });
    }

    private void initPopView(View inflate) {
        video_progress_linear = inflate.findViewById(R.id.video_progress_linear);
        videoCurrentTimeTv = inflate.findViewById(R.id.video_current_time_tv);
        videoTotalTimeTv = inflate.findViewById(R.id.video_total_time_tv);
        videoSeekBar = inflate.findViewById(R.id.video_seek_bar);
        videoPlayOrPauseBtn = inflate.findViewById(R.id.video_play_or_pause_btn);
        videoStopBtn = inflate.findViewById(R.id.video_stop_btn);
        videoScreenShotBtn = inflate.findViewById(R.id.video_screen_shot_btn);
        videoStartScreenBtn = inflate.findViewById(R.id.video_start_screen_btn);
        videoStopScreenBtn = inflate.findViewById(R.id.video_stop_screen_btn);

        /**  手动设置隐藏PopupWindow时保存的信息  */
        videoSeekBar.setProgress(lastPer);
        videoCurrentTimeTv.setText(lastSec);
        videoTotalTimeTv.setText(lastTotal);

        videoPlayOrPauseBtn.setOnClickListener(v -> presenter.playOrPause());
        videoStopBtn.setOnClickListener(v -> {
            bottomPop.dismiss();
            presenter.stopScreen();
            finish();
        });
        videoScreenShotBtn.setOnClickListener(v -> {
            presenter.pause();
            surfaceView.cutVideoImg();
            bottomPop.dismiss();
        });
        videoStartScreenBtn.setOnClickListener(v -> presenter.startScreen());
        videoStopScreenBtn.setOnClickListener(v -> presenter.stopScreen());
    }

    private void initView() {
        video_root = (ConstraintLayout) findViewById(R.id.video_root);
        surfaceView = (MyGLSurfaceView) findViewById(R.id.video_surfaceview);
        opticalDisk = (ImageView) findViewById(R.id.opticalDisk);
        plectrum = (ImageView) findViewById(R.id.plectrum);
        play_mp3_view = (RelativeLayout) findViewById(R.id.play_mp3_view);
        video_top_title = (TextView) findViewById(R.id.video_top_title);
        video_root.setOnClickListener(this);
    }

    private void startAnimator() {
        LogUtil.i(TAG, "startAnimator ");
        plectrum(0f, 30f, 500);
        opticalDiskAnimator = ObjectAnimator.ofFloat(opticalDisk, "rotation", 0f, 360f);
        opticalDiskAnimator.setDuration(3000);
        opticalDiskAnimator.setRepeatCount(ValueAnimator.INFINITE);
        opticalDiskAnimator.setRepeatMode(ValueAnimator.RESTART);
        opticalDiskAnimator.setInterpolator(new LinearInterpolator());
        opticalDiskAnimator.start();
    }

    private void stopAnimator() {
        LogUtil.i(TAG, "stopAnimator ");
        if (opticalDiskAnimator != null) {
            opticalDiskAnimator.cancel();
            opticalDiskAnimator = null;
        }
        plectrum(30f, 0f, 500L);
    }

    private void plectrum(float from, float to, long duration) {
        plectrumAnimator = ObjectAnimator.ofFloat(plectrum, "rotation", from, to);
        plectrum.setPivotX(1);
        plectrum.setPivotY(1);
        plectrumAnimator.setDuration(duration);
        plectrumAnimator.start();
    }

    @Override
    public void updateAnimator(int status) {
        if (mStatus == status) return;
        mStatus = status;
        LogUtil.i(TAG, "updateAnimator 新的状态：" + mStatus);
        //0=播放中，1=暂停，2=停止,3=恢复
        switch (mStatus) {
            case 0:
                startAnimator();
                break;
            case 1:
                stopAnimator();
                break;
        }
    }

    @Override
    public void onGlSurfaceViewOncreate(Surface surface) {
        LogUtil.e(TAG, "onGlSurfaceViewOncreate :   --> ");
        presenter.setSurface(surface);
    }

    @Override
    public void onCutVideoImg(Bitmap bitmap) {
        FabService.bytes = ConvertUtil.Bitmap2bytes(bitmap);
        EventBus.getDefault().post(new EventMessage(EventType.CUT_VIDEO_IMAGE));
        LogUtil.e(TAG, "onCutVideoImg :   --> ");
    }
}
