package com.pa.paperless.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsoluteLayout;

import com.pa.paperless.utils.LogUtil;

/**
 * @author by xlk
 * @date 2020/6/12 18:03
 * @desc 可以上下左右自由拖拽的AbsoluteLayout
 */
public class CustomAbsoluteLayout extends AbsoluteLayout {
    private final String TAG = "CustomAbsoluteLayout-->";
    private int width = 1300, height = 760;//底图宽高
    private int viewWidth, viewHeight;//显示区域的宽高

    /**
     * 设置显示区域的宽高
     */
    public void setScreen(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    public CustomAbsoluteLayout(Context context) {
        this(context, null);
    }

    public CustomAbsoluteLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomAbsoluteLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomAbsoluteLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 动态设置setLayoutParams后会执行
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        LogUtil.e(TAG, "onMeasure : --> width= " + width + ", height= " + height);
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.l = l;
        this.t = t;
        this.r = r;
        this.b = b;
        LogUtil.d(TAG, "更新位置 -->onLayout= " + l + "," + t + "," + r + "," + b);
    }

    private float downX, downY;//拖动时按下
    private int l = 0, t = 0, r, b;//拖动时

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //是否可以左右拖动
        boolean canMoveX = !(viewWidth >= width);
        //是否可以上下拖动
        boolean canMoveY = !(viewHeight >= height);
        LogUtil.i(TAG, "onTouchEvent 显示宽高：" + viewWidth + "," + viewHeight +
                ",底图宽高：" + width + "," + height + ",是否可以拖动：" + canMoveX + "," + canMoveY);
        if (!canMoveY && !canMoveX) {
            //已经可以完全显示底图
            LogUtil.i(TAG, "onTouchEvent 已经可以完全显示底图");
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
//                LogUtil.d(TAG, "触摸点 -->" + moveX+","+moveY+", 按压点: "+downX+","+downY);
                float dx = moveX - downX;//负数,说明是向左滑动
                float dy = moveY - downY;//负数,说明是向上滑动
                LogUtil.d(TAG, "滑动距离 -->" + dx + "," + dy);
                int left = getLeft();
                int top = getTop();
                int right = getRight();
                int bottom = getBottom();
                LogUtil.i(TAG, "四边 -->左：" + left + ",上：" + top + ",右：" + right + ",下：" + bottom);
                if (canMoveX) {
                    //左
                    if (left == 0) {//当前左边已经封顶了
                        if (dx < 0 && right >= viewWidth)//还往左边移动
                            l = (int) (left + dx);
                        else l = 0;
                    } else if (left < 0) {//当前已经超过最左边
                        if (right >= viewWidth) {
                            if (dx < 0)
                                l = (int) (left + dx);
                            else if (dx > 0)
                                l = (int) (left + dx);
                        }
                    } else l = 0;
                    //右
                    r = width + l;
                    int i1 = viewWidth - r;
                    if (i1 > 0) {
                        r = viewWidth;
                        l += i1;
                    }
                } else {//不可以拖动X轴
                    l = 0;
                    r = width;
                }
                if (canMoveY) {
                    //上
                    if (top == 0) {
                        if (dy < 0 && bottom > viewHeight)
                            t = (int) (top + dy);
                        else t = 0;
                    } else if (top < 0) {
                        if (dy < 0 && bottom > viewHeight)
                            t = (int) (top + dy);
                        else if (dy > 0)
                            t = (int) (top + dy);
                    } else t = 0;
                    //下
                    this.b = height + t;
                    int i = viewHeight - this.b;
                    if (i > 0) {
                        this.b = viewHeight;
                        t += i;
                    }
                } else {//不可以拖动Y轴
                    t = 0;
                    b = height;
                }
                LogUtil.d(TAG, "更新位置 -->" + l + "," + t + "," + r + "," + this.b);
                this.layout(l, t, r, this.b);
                break;
        }
        return true;
    }
}
