package com.pa.paperless.ui;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * Created by xlk on 2019/8/9.
 * 用于实现需要多个TextView实现跑马灯效果，获取不到焦点
 */
public class MarqueeTextView extends AppCompatTextView {
    public MarqueeTextView(Context context) {
        this(context,null);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setSingleLine(true);
        //设置无限循环
        setMarqueeRepeatLimit(-1);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
