package com.pa.paperless.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.LogUtil;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.pa.paperless.data.constant.EventType;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2018/5/23.
 */

public class DrawBoardSeekBar extends SeekBar {

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
        //宽高倒置
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //宽高倒置
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //竖着向上
        canvas.rotate(-90);
        canvas.translate(-getHeight(), 0);
        //竖着向下
//        canvas.rotate(-90);
//        canvas.translate(0, -getWidth());
        super.onDraw(canvas);
    }


    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        //宽高倒置
        onSizeChanged(getWidth(), getHeight(), 0, 0);
        EventBus.getDefault().post(new EventMessage(EventType.seetbar_progress));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                LogUtil.e("DrawBoardSeekBar", "DrawBoardSeekBar.onTouchEvent :   --> " + getWidth() + "," + getHeight());
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    public DrawBoardSeekBar(Context context) {
        super(context);
    }

    public DrawBoardSeekBar(Context context, AttributeSet attrs) {
//        ,android.R.attr.seekBarStyle
        super(context, attrs);
    }

    public DrawBoardSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
