package com.pa.paperless.helper;

import android.text.InputFilter;
import android.text.Spanned;

import com.pa.boling.paperless.R;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.ToastUtil;

/**
 * @author by xlk
 * @date 2020/8/11 17:37
 * @desc EditText限制输入字数限制功能类
 * 使用示例： xxx.setFilters(new InputFilter[]{new MaxLengthFilter(100)});
 */
public class MaxLengthFilter implements InputFilter {

    private final int mMax;//限制字数

    public MaxLengthFilter(int max) {
        mMax = max;
    }

    /**
     * @param source 输入的源字符
     * @param start  输入的起始位置
     * @param end    输入的结束位置
     * @param dest   当前输入框中的内容 已经存在的
     * @param dstart 输入框中的内容被替换的起始位置
     * @param dend   输入框中的内容被替换的结束位置
     * @return
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        LogUtil.d("MaxLengthFilter", "source  ==" + source + "  start=====" + start + "   end======" + end + "   dest====" + dest + "  dstart===" + dstart + "  dend==" + dend);
        if (source.length() <= 0) return null;//一般是删除
        if (source.length() + dest.length() > mMax) {
            ToastUtil.showToast(R.string.err_bulletin_max_length, mMax);
            //可输入的字数
            int canAddLength = mMax - dest.length();
            //可输入的文本
            String substring = source.toString().substring(0, canAddLength);
//            String content = dest.toString().substring(0, dstart) + substring + dest.toString().substring(dstart);
//            LogUtil.i("MaxLengthFilter", "长度：" + content.length() + ", content=" + content);
            return substring;
        }
        return null;
    }
}
