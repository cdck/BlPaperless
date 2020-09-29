package com.pa.paperless.video;

import android.view.View;

/**
 * @author xlk
 * @date 2019/6/27
 */
public interface IVideo {

    /**
     * 更新播放进度相关UI
     *
     * @param per   百分比
     * @param sec   当前播放时长
     * @param total 总时长
     */
    void updateProgressUi(int per, String sec, String total);

    /**
     * 获取PopupWindow需要的父控件
     * @return view
     */
    View getView();

    void setFrameData(int w, int h, byte[] y, byte[] u, byte[] v);

    void setCodecType(int codecType);

    void close();

    void setCanNotExit();

    void updateTopTitle(String topTitle);

    /**
     * 根据播放状态更新动画状态
     * @param status 0=播放中，1=暂停，2=停止,3=恢复
     */
    void updateAnimator(int status);
}
