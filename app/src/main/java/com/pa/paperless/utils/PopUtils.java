package com.pa.paperless.utils;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pa.boling.paperless.R;
import com.pa.paperless.service.App;

/**
 * Created by xlk on 2018/9/7.
 */
public abstract class PopUtils {

    public static class PopBuilder {

        private static PopupWindow window;

        private SparseArray<View> mViews;

        private View mItem;

        private PopBuilder(View view) {
            this.mViews = new SparseArray<>();
            this.mItem = view;
        }

        public static PopBuilder createPopupWindow(int layoutResId, int width, int height, View parent, int gravity, int x, int y, boolean outside, final ClickListener callback) {
            final Activity activity = App.currentActivity();
            // 利用layoutInflater获得View
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(layoutResId, null);

            final PopBuilder builder = new PopBuilder(view);

            window = new PopupWindow(view, width, height);

            // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
            window.setTouchable(true);

            // true:设置触摸外面时消失
            window.setOutsideTouchable(outside);
            window.setFocusable(outside);

            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            window.setAnimationStyle(R.style.Anim_PopupWindow);
            // 监听PopupWindow关闭，如果为关闭状态则设置为空
            window.setOnDismissListener(() -> {
                window = null;
                callback.setOnDismissListener(builder);
                // 主界面完全显示
                WindowManager.LayoutParams params = activity.getWindow().getAttributes();
                params.alpha = 1.0f;
                activity.getWindow().setAttributes(params);
            });

            // 实例化一个ColorDrawable颜色为透明，不设置为半透明是因为带圆角
            ColorDrawable dw = new ColorDrawable(activity.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(dw);

            window.showAtLocation(parent, gravity, x, y);

            // 主界面变暗
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.alpha = 0.8f;
            activity.getWindow().setAttributes(params);

            //点击事件回调
            if (window != null) {
                callback.setUplistener(builder);
            }
            return builder;
        }

        /**
         * 得到视图
         *
         * @param id  控件资源id
         * @param <T> 类型
         * @return T
         */
        public <T extends View> T getView(int id) {
            T t = (T) mViews.get(id);
            if (t == null) {
                t = (T) mItem.findViewById(id);
                mViews.put(id, t);
            }
            return t;
        }

        /**
         * 使窗口消失
         *
         * @return
         */
        public PopBuilder dismiss() {
            if (window != null) {
                window.dismiss();
            }
            return this;
        }

        /**
         * 设置是否可见
         *
         * @param id         控件id
         * @param visibility 是否可见
         * @return PopBuilder
         */
        public PopBuilder setVisibility(int id, int visibility) {
            getView(id).setVisibility(visibility);
            return this;
        }

        /**
         * 设置图片资源
         *
         * @param id          控件id
         * @param drawableRes drawable资源id
         * @return PopBuilder
         */
        public PopBuilder setImageResource(int id, int drawableRes) {
            View view = getView(id);
            if (view instanceof ImageView) ((ImageView) view).setImageResource(drawableRes);
            else view.setBackgroundResource(drawableRes);
            return this;
        }

        /**
         * 设置文本
         *
         * @param id   控件id
         * @param text 文本内容
         * @return PopBuilder
         */
        public PopBuilder setText(int id, CharSequence text) {
            View view = getView(id);
            if (view instanceof TextView) ((TextView) view).setText(text);
            return this;
        }
    }

    /**
     * 用于回调的接口
     */
    public interface ClickListener {
        void setUplistener(PopBuilder builder);

        //PopupWindow 隐藏时回调
        void setOnDismissListener(PopBuilder builder);
    }

}
