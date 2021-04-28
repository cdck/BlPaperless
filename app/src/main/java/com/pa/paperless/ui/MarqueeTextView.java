package com.pa.paperless.ui;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import android.text.TextUtils;
import android.util.AttributeSet;

import com.pa.paperless.helper.SharedPreferenceHelper;

/**
 * Created by xlk on 2019/8/9.
 * 用于实现需要多个TextView实现跑马灯效果，获取不到焦点
 */
public class MarqueeTextView extends AppCompatTextView {
    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (SharedPreferenceHelper.isMarquee(context)) {
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
            setSingleLine(true);
            //设置无限循环
            setMarqueeRepeatLimit(-1);
        } else {
            setSingleLine(true);
            setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
