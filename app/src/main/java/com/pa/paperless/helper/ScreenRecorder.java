package com.pa.paperless.helper;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;

import com.blankj.utilcode.util.LogUtils;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.CodecUtil;
import com.pa.paperless.utils.LogUtil;

import android.util.Log;
import android.util.Range;
import android.view.Surface;

import com.pa.paperless.utils.MyUtils;
import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.pa.paperless.service.App.isDebug;
import static com.pa.paperless.service.App.read2file;

/**
 * @author Gowcage
 */
public class ScreenRecorder extends Thread {
    private final String TAG = "ScreenRecorder-->";
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;// h.264编码
    private static final int FRAME_RATE = 18;// 帧率
    private static final int I_FRAME_INTERVAL = 2;// 关键帧间隔  两关键帧之间的其它帧 = 18*2
    private static final int TIMEOUT_US = 10 * 1000;// 超时

    private int width;
    private int height;
    private int bitrate;
    private int dpi;
    private String savePath;
    private AtomicBoolean quit = new AtomicBoolean(false);

    private NativeUtil jni = NativeUtil.getInstance();

    private MediaProjection projection;
    private VirtualDisplay display;
    private Surface mSurface;
    private MediaCodec encoder;
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    private final int channelIndex = 2;//屏幕
    private MediaMuxer mediaMuxer;

    public ScreenRecorder(int width, int height, int bitrate, int dpi, MediaProjection projection, String savePath) {
        LogUtil.d(TAG, "ScreenRecorder: 宽:" + width + "，高:" + height + " bitrate: " + bitrate);
        jni.InitAndCapture(0, channelIndex);
        checkSize(width, height);
        this.bitrate = bitrate;
        this.dpi = dpi;
        this.projection = projection;
        this.savePath = savePath;
    }

    public void quit() {
        quit.set(true);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        try {
            try {
                // 初始化编码器
                prepareEncoder();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                throw new RuntimeException(e);
            }
            // 4:创建VirtualDisplay实例,DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC / DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
            display = projection.createVirtualDisplay("MainScreen", width, height, dpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null);
            LogUtil.d(TAG, "created virtual display: " + display);
            // 录制虚拟屏幕
            recordVirtualDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            release();
        }
    }

    /**
     * 检查宽高，如果宽高是奇数，则会抛出异常：android.media.MediaCodec$CodecException: Error 0xfffffc0e
     *
     * @param width
     * @param height
     */
    private void checkSize(int width, int height) {
        this.width = CodecUtil.getSupportSize(width);
        this.height = CodecUtil.getSupportSize(height);
        LogUtils.i(TAG, "checkSize 宽高=" + this.width + "," + this.height);
    }

    // 初始化编码器
    private void prepareEncoder() throws IOException {
        LogUtil.e(TAG, "prepareEncoder---------------------------");
        // 创建MediaCodec实例 这里创建的是编码器
        encoder = MediaCodec.createEncoderByType(MIME_TYPE);
        MediaCodecInfo codecInfo = encoder.getCodecInfo();
        MediaCodecInfo.CodecCapabilities capabilitiesForType = codecInfo.getCapabilitiesForType(MIME_TYPE);
        MediaCodecInfo.VideoCapabilities videoCapabilities = capabilitiesForType.getVideoCapabilities();
        Range<Integer> supportedWidths = videoCapabilities.getSupportedWidths();
        Range<Integer> supportedHeights = videoCapabilities.getSupportedHeights();
        // TODO: 2020/9/26 解决宽高不适配的问题 Fix:android.media.MediaCodec$CodecException: Error 0xfffffc0e
        width = supportedWidths.clamp(width);
        height = supportedHeights.clamp(height);
        Log.e(TAG, "prepareEncoder 录制时使用的宽高：width=" + width + ",height=" + height + ",bitrate=" + bitrate);
        checkSize(width, height);
        boolean sizeSupported = videoCapabilities.isSizeSupported(width, height);
        LogUtils.i(TAG, "sizeSupported=" + sizeSupported);
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        // 码率 越高越清晰 仅编码器需要设置
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        format.setInteger("max-bitrate", bitrate);
        // 颜色格式
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        // COLOR_FormatSurface这里表明数据将是一个graphicBuffer元数据
        // 将一个Android surface进行mediaCodec编码
        // 帧数 越高越流畅,24以下会卡顿
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        //画面静止时不会发送数据，屏幕内容有变化才会刷新
        //仅在以“表面输入”模式配置视频编码器时适用。相关值为long，并给出以微秒为单位的时间，
        //设置如果之后没有新帧可用，则先前提交给编码器的帧在 1000000 / FRAME_RATE 微秒后重复（一次）
        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / FRAME_RATE);
        //某些设备不支持设置Profile和Level，而应该采用默认设置
//        format.setInteger(MediaFormat.KEY_PROFILE, 8);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            format.setInteger(MediaFormat.KEY_LEVEL, 65536);
//            format.setInteger(MediaFormat.KEY_STRIDE, width);
//            format.setInteger(MediaFormat.KEY_SLICE_HEIGHT, height);
//        }
        //设置CBR模式
//        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        //format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
        // 关键帧间隔时间s
        // IFRAME_INTERVAL是指的帧间隔，它指的是，关键帧的间隔时间。通常情况下，设置成多少问题都不大。
        // 比如设置成10，那就是10秒一个关键帧。但是，如果有需求要做视频的预览，那最好设置成1
        // 因为如果设置成10，会发现，10秒内的预览都是一个截图
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        LogUtil.d(TAG, "created video format: " + format);
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // 这一步非常关键，它设置的，是MediaCodec的编码源，也就是说，要告诉Encoder解码哪些流。
        mSurface = encoder.createInputSurface();
        LogUtil.d(TAG, "created input surface: " + mSurface);
        encoder.start();// 开始编码
    }

    private byte[] configbyte;
    private int count = 0;
    private int allCount = 0;

    // 录制虚拟屏幕
    private void recordVirtualDisplay() {
        try {
            LogUtil.d(TAG, "recordVirtualDisplay---------------------------");
            EventBus.getDefault().post(new EventMessage(EventType.SEND_SCREEN_TIME, 0));
            while (!quit.get()) {
                //从输出队列中取出编码操作之后的数据
                //输出流队列中取数据索引,返回已成功解码的输出缓冲区的索引
                int index = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
                while (index >= 0) {
                    ByteBuffer outputBuffer = encoder.getOutputBuffer(index);
                    byte[] outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);
                    //这表示带有此标记的缓存包含编解码器初始化或编解码器特定的数据而不是多媒体数据media data
                    if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                        LogUtil.v(TAG, "get config byte!");
                        configbyte = new byte[bufferInfo.size];
                        configbyte = outData;
                        //这表示带有此标记的（编码的）缓存包含关键帧数据
                    } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                        byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                        System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                        read2File(keyframe);
//                    timePush(keyframe, 1, bufferInfo.presentationTimeUs);
                        jni.call(channelIndex, 1, bufferInfo.presentationTimeUs, keyframe);
                        if (isDebug) {
                            allCount++;
                            LogUtil.e(TAG, "发送方 发送关键帧数据 关键帧之间的个数=" + count + ", 发送数据总量=" + allCount + ", pts= " + bufferInfo.presentationTimeUs + ", size= " + keyframe.length);
                            if (count != FRAME_RATE * I_FRAME_INTERVAL) {
                                Log.v(TAG, "发送方 普通帧个数异常 count=" + count);
                            }
                            count = 0;
                        }
                    } else {
                        read2File(outData);
//                    timePush(outData, 0, bufferInfo.presentationTimeUs);
                        jni.call(channelIndex, 0, bufferInfo.presentationTimeUs, outData);
                        LogUtil.d(TAG, "发送方 发送普通帧数据 pts= " + bufferInfo.presentationTimeUs + ", size= " + outData.length);
                        if (isDebug) {
                            count++;
                            allCount++;
                        }
                    }
                    LogUtil.i(TAG, "发送方 发送数据总量：" + allCount);
//                outputBuffer.position(bufferInfo.offset);//2019年9月27日14:14:55
//                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                    encoder.releaseOutputBuffer(index, false);
                    index = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createMp4File() throws IOException {
        if (mediaMuxer == null) {
            File file = new File(Macro.ROOT + "ScreenRecord.mp4");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            // Muxer需要传入一个文件路径来保存输出的视频，并传入输出格式
            mediaMuxer = new MediaMuxer(Macro.ROOT + "ScreenRecord.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        }
    }

    private int m_videoTrackIndex;
    private boolean m_bMuxerStarted;

    private void resetOutputFormat() {
        if (m_bMuxerStarted) {
            return;
        }
        //将MediaCodec的Format设置给MediaMuxer
        MediaFormat newFormat = encoder.getOutputFormat();

        //获取m_videoTrackIndex，这个值是每一帧画面要放置的顺序
        m_videoTrackIndex = mediaMuxer.addTrack(newFormat);
        mediaMuxer.start();
        m_bMuxerStarted = true;
        Log.e(TAG, "resetOutputFormat mediaMuxer start");
    }

    long lastTime = 0;

    private void timePush(byte[] data, int keyFrame, long presentationTimeUs) {
        //最小使用的
        int hm = (int) MyUtils.divide(MyUtils.divide(1000, FRAME_RATE, 0), 2, 0);
        // 20 = 120 -100
        long useTime = System.currentTimeMillis() - lastTime;
        boolean isKey = keyFrame == 1;
        if (useTime <= hm) {
            try {
                long millis = hm - useTime;
                Thread.sleep(millis);
                lastTime = System.currentTimeMillis();
                read2File(data);
                jni.call(channelIndex, keyFrame, presentationTimeUs, data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            lastTime = System.currentTimeMillis();
            read2File(data);
            jni.call(channelIndex, keyFrame, presentationTimeUs, data);
        }
    }

    // 释放资源
    private void release() {
        LogUtil.d(TAG, "release---------------------------");
        if (encoder != null) {
            encoder.stop();
            encoder.release();
            encoder = null;
        }
//        if (projection != null) {
//            projection.stop();
//        }
        if (display != null) {
            display.release();
        }
        EventBus.getDefault().post(new EventMessage(EventType.SEND_SCREEN_TIME, 1));
        try {
            if (mediaMuxer != null) {
                mediaMuxer.stop();
                mediaMuxer.release();
                mediaMuxer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BufferedOutputStream outputStream;

    private void read2File(byte[] outData) {
        if (!read2file) return;
        try {
            if (outputStream == null) {
                File file = new File(Macro.ROOT + "/ScreenRecorder.mp4");
                savePath = file.getAbsolutePath();
                if (file.exists()) {
                    file.delete();
                }
                outputStream = new BufferedOutputStream(new FileOutputStream(file));
            }
            outputStream.write(outData, 0, outData.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
