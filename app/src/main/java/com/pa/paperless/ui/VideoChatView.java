package com.pa.paperless.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.video.opengles.MyGLSurfaceView;
import com.pa.paperless.video.opengles.WlOnGlSurfaceViewOncreateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xlk
 * @date 2020/4/11
 * @desc 会议交流中视频聊天
 */
public class VideoChatView extends ViewGroup {
    private final String TAG = "VideoChatView-->";
    private int widthMeasureSpec;
    private int heightMeasureSpec;
    List<Integer> resids = new ArrayList<>();

    public VideoChatView(Context context) {
        this(context, null);
    }

    public VideoChatView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.widthMeasureSpec = widthMeasureSpec;
        this.heightMeasureSpec = heightMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        measureChild(getWidth(), getHeight());
    }

    private void measureChild(int parentWidth, int parentHeight) {
        LogUtil.i(TAG, "measureChild -->viewGroup的宽高：" + parentWidth + ", " + parentHeight);
        View view1 = getChildAt(0);
        View view2 = getChildAt(1);
        LayoutParams params = new LayoutParams(parentWidth, parentHeight);
        LayoutParams params2_1 = new LayoutParams(parentWidth / 2, parentHeight);
        switch (getChildCount()) {
            case 1:
                view1.setLayoutParams(params);
                break;
            case 2:
                view1.setLayoutParams(params2_1);
                view2.setLayoutParams(params2_1);
                break;
            default:
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //注意：左、上、右、下  表示的是与原点（0,0）的距离不是与该方向的距离
        LogUtil.i(TAG, "onLayout :   --> l=" + l + ", t=" + t + ", r=" + r + ", b=" + b
                + ", getChildCount= " + getChildCount() + ", 宽高：" + getWidth() + "," + getHeight());
        switch (getChildCount()) {
            case 1:
                layout1();
                break;
            case 2:
                layout2();
                break;
        }
    }

    private void layout1() {
        View view = getChildAt(0);
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        LogUtil.d(TAG, "layout1 :   --> " + measuredWidth + "," + measuredHeight);
        view.layout(0, 0, measuredWidth, measuredHeight);
    }

    private void layout2() {
        View view1 = getChildAt(0);
        int measuredWidth = view1.getMeasuredWidth();
        int measuredHeight = view1.getMeasuredHeight();
        LogUtil.d(TAG, "layout2 0:   --> " + measuredWidth + "," + measuredHeight);
        int pading = 5;
        view1.layout(pading, pading, measuredWidth - pading, measuredHeight - pading);

        View view2 = getChildAt(1);
        int measuredWidth1 = view2.getMeasuredWidth();
        int measuredHeight1 = view2.getMeasuredHeight();
        LogUtil.d(TAG, "layout2 1:   --> " + measuredWidth + "," + measuredHeight);
        view2.layout(measuredWidth + pading, pading, measuredWidth + measuredWidth1 - pading, measuredHeight1 - pading);
    }

    private void reDraw() {
        LogUtil.i(TAG, "reDraw --> 重新绘制---");
        invalidate();
        measureChild(getWidth(), getHeight());
        requestLayout();
    }

    public void createDefaultView(int count) {
        clearAll();
        for (int i = 0; i < count; i++) {
            ImageView iv1 = new ImageView(getContext());
            iv1.setScaleType(ImageView.ScaleType.CENTER);
            iv1.setImageResource(R.drawable.icon_camera);
            addView(iv1);
        }
        reDraw();
    }

    public void createPlayView(List<Integer> resIds) {
        clearAll();
        resids.clear();
        resids.addAll(resIds);
        for (int i = 0; i < resIds.size(); i++) {
            MyGLSurfaceView surfaceView = new MyGLSurfaceView(getContext());
            surfaceView.setResId(resIds.get(i));
            surfaceView.setClickable(true);
            surfaceView.setOnGlSurfaceViewOncreateListener(new WlOnGlSurfaceViewOncreateListener() {
                @Override
                public void onGlSurfaceViewOnCreate(Surface surface) {
                    LogUtil.e(TAG, "createPlayView onGlSurfaceViewOncreate :   --> ");
                    surfaceView.setSurface(surface);
                }

                @Override
                public void onCutVideoImg(Bitmap bitmap) {
                    LogUtil.e(TAG, "createPlayView onCutVideoImg :   --> ");
                }
            });
            addView(surfaceView);
        }
        reDraw();
    }

    public void clearAll() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof MyGLSurfaceView) {
                MyGLSurfaceView v = (MyGLSurfaceView) view;
                v.destroy();
            }
        }
        removeAllViews();
    }

    public void setYuv(Object[] yuvdisplay) {
        int resid = (int) yuvdisplay[0];
        if (!resids.contains(resid)) {
            //处理已经停止播放的资源，后台还有一两个通知现在才发过来的问题
            return;
        }
        int w = (int) yuvdisplay[1];
        int h = (int) yuvdisplay[2];
        byte[] y = (byte[]) yuvdisplay[3];
        byte[] u = (byte[]) yuvdisplay[4];
        byte[] v = (byte[]) yuvdisplay[5];
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view != null) {
                if (view instanceof MyGLSurfaceView) {
                    MyGLSurfaceView surfaceView = (MyGLSurfaceView) view;
                    if (surfaceView.getResId() == resid) {
                        surfaceView.stopTimeThread();
                        surfaceView.setCodecType(0);
                        surfaceView.setFrameData(w, h, y, u, v);
                        break;
                    }
                }
            }
        }
    }

    public void setVideoDecode(Object[] videoDecode) {
        int resid = (int) videoDecode[1];
        if (!resids.contains(resid)) {
            //处理已经停止播放的资源，后台还有一两个通知现在才发过来的问题
            return;
        }
        int isKeyframe = (int) videoDecode[0];
        int codecid = (int) videoDecode[2];
        int w = (int) videoDecode[3];
        int h = (int) videoDecode[4];
        byte[] packet = (byte[]) videoDecode[5];
        long pts = (long) videoDecode[6];
        byte[] codecdata = (byte[]) videoDecode[7];
        String mimeType = Macro.getMimeType(codecid);
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof MyGLSurfaceView) {
                MyGLSurfaceView surfaceView = (MyGLSurfaceView) view;
                if (surfaceView.getResId() == resid) {
                    //设置播放类型为decode
                    surfaceView.setCodecType(1);
                    //收到通知后，可能surface还是空
                    if (surfaceView.getSurface() != null) {
                        if (packet != null) {
                            surfaceView.setLastPushTime(System.currentTimeMillis());
                            surfaceView.initCodec(mimeType, w, h, codecdata);
                        }
                        surfaceView.mediaCodecDecode(packet, pts, isKeyframe);
                        surfaceView.startTimeThread();
                    }
                    break;
                }
            }
        }
    }
}
