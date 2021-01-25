package com.pa.paperless.helper;

import android.media.MediaFormat;
import android.media.projection.MediaProjection;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Created by xlk on 2021/1/21.
 * @desc
 */
class ScreenRecorderThread extends Thread {
    private static final String TAG = "ScreenRecorderThread-->";
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;// h.264编码
    private static final int FRAME_RATE = 18;// 帧率
    private static final int I_FRAME_INTERVAL = 2;// 关键帧间隔  两关键帧之间的其它帧 = 18*2
    private static final int TIMEOUT_US = 10 * 1000;// 超时
    private int width;
    private int height;
    private int bitrate;
    private int dpi;
    private String savePath;
    private MediaProjection projection;
    private AtomicBoolean quit = new AtomicBoolean(false);

    public ScreenRecorderThread(int width, int height, int bitrate, int dpi, MediaProjection mp, String dstPath) {
        super(TAG);
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        this.dpi = dpi;
        this.projection = mp;
        this.savePath = dstPath;
    }

    @Override
    public void run() {
        super.run();
        try{
            prepareEncoder();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void prepareEncoder() {

    }
}
