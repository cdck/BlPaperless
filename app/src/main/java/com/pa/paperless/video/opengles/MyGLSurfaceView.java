package com.pa.paperless.video.opengles;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Range;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.pa.paperless.data.bean.MediaBean;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author xlk
 * @date 2019/7/1
 * @desc https://blog.csdn.net/ywl5320/article/details/78965039?tdsourcetag=s_pcqq_aiomsg
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final String TAG = "MyGLSurfaceView-->";
    private String saveMimeType = "";
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private MediaCodec.BufferInfo info;
    private Surface surface;
    private int initW;
    private int initH;
    private MyWlGlRender glRender;
    private int resId;
    private long lastPushTime;
    private boolean isStop = false;
    private releaseThread timeThread;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        LogUtil.i(TAG, "surfaceChanged w=" + w + ",h=" + h);
        super.surfaceChanged(holder, format, w, h);
    }

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        initRender();
    }

    private void initRender() {
        LogUtil.i(TAG, "initRender -->");
        glRender = new MyWlGlRender(getContext());
        setRenderer(glRender);
        //设置为手动刷新模式  RENDERMODE_CONTINUOUSLY 为自动刷新模式
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glRender.setWlOnRenderRefreshListener(new WlOnRenderRefreshListener() {
            @Override
            public void onRefresh() {
                requestRender();
            }
        });
    }

    public void setCodecType(int codecType) {
        if (glRender == null) {
            LogUtil.i(TAG, "setCodecType -->glRender为null");
            initRender();
        }
        glRender.setCodecType(codecType);
        requestRender();
    }

    public void setFrameData(int w, int h, byte[] y, byte[] u, byte[] v) {
        glRender.setFrameData(w, h, y, u, v);
        requestRender();
    }

    public void setOnGlSurfaceViewOncreateListener(WlOnGlSurfaceViewOncreateListener onGlSurfaceViewOncreateListener) {
        if (glRender != null) {
            glRender.setWlOnGlSurfaceViewOncreateListener(onGlSurfaceViewOncreateListener);
        }
    }

    public Surface getSurface() {
        return this.surface;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    public void cutVideoImg() {
        if (glRender != null) {
            glRender.cutVideoImg();
            requestRender();
        }
    }

    public void initCodec(String mimeType, int w, int h, byte[] codecdata) {
        if (!saveMimeType.equals(mimeType) || initW != w || initH != h || mediaCodec == null) {
            if (mediaCodec != null) {
                //调用stop方法使其进入 uninitialzed 状态，这样才可以重新配置MediaCodec
                mediaCodec.stop();
            }
            saveMimeType = mimeType;
            try {
                //1.创建了一个编解码器，此时编解码器处于未初始化状态（Uninitialized）
                mediaCodec = MediaCodec.createDecoderByType(saveMimeType);
                /**  宽高要判断是否是解码器所支持的范围  */
                MediaCodecInfo.VideoCapabilities videoCapabilities = mediaCodec.getCodecInfo().getCapabilitiesForType(saveMimeType).getVideoCapabilities();
                Range<Integer> supportedWidths = videoCapabilities.getSupportedWidths();
                Integer upper = supportedWidths.getUpper();
                Integer lower = supportedWidths.getLower();
                Range<Integer> supportedHeights = videoCapabilities.getSupportedHeights();
                Integer upper1 = supportedHeights.getUpper();
                Integer lower1 = supportedHeights.getLower();
                initW = w;
                initH = h;
                LogUtil.i(TAG, "initCodec -->" + "w= " + w + ", h= " + h);
                if (w > upper) {
                    w = upper;
                } else if (w < lower) {
                    w = lower;
                }
                if (h > upper1) {
                    h = upper1;
                } else if (h < lower1) {
                    h = lower1;
                }
                initMediaFormat(w, h, codecdata);
                info = new MediaCodec.BufferInfo();
                LogUtil.i(TAG, "initCodec -->configure surface是否为空: " + (surface == null));
                //2.对编解码器进行配置，这将使编解码器转为配置状态（Configured）
                mediaCodec.configure(mediaFormat, surface, null, 0);
                //3.调用start()方法使其转入执行状态（Executing）
                mediaCodec.start();
                //要保持屏幕纵横比：此方法必须在configure和start之后执行才有效
                mediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initMediaFormat(int w, int h, byte[] codecdata) {
        LogUtil.d(TAG, "initMediaFormat :   -->mediaFormat是否为空： " + (mediaFormat == null) + ", w= " + w + ", h= " + h);
        mediaFormat = MediaFormat.createVideoFormat(saveMimeType, w, h);
        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, w);
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, h);
        //设置最大输出大小
        mediaFormat.setLong(MediaFormat.KEY_MAX_INPUT_SIZE, w * h);
        if (codecdata != null) {
            mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(codecdata));
            mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(codecdata));
        }
    }

    LinkedBlockingQueue<MediaBean> queue = new LinkedBlockingQueue<>();

    public void mediaCodecDecode(byte[] bytes, long pts, int iskeyframe) {
        if (isStop) return;
        if (bytes != null && bytes.length > 0) {
            //把网络接收到的视频数据先加入到队列中
            queue.offer(new MediaBean(bytes, bytes.length, pts, iskeyframe));
        } else {
            //bytes为null也不能立马返回，需要处理从视频队列中送数据到解码buffer 和 解码好的视频的显示
        }
        int queuesize = queue.size();
//        LogUtil.i(TAG, " mediaCodecDecode -->queuesize: " + queuesize);
        if (queuesize > 500) {
            //当解码速度太慢，导致视频数据积累太多，这种情况下要处理丢包，丢包的策略把前面的关键帧组全部丢包，保留后面两个关键帧组
            //丢帧必须按照I帧P帧连续的丢，否则会造成花屏的情况
            int keyframenum = 0;
            MediaBean poll;
            //先统计队列中有多少个关键帧
            for (int ni = 0; ni < queuesize; ++ni) {
                poll = queue.peek();
                if (poll.getIskeyframe() == 1)
                    keyframenum++;
            }
            for (int ni = 0; ni < queuesize; ++ni) {
                poll = queue.peek();
                if (poll.getIskeyframe() == 1) {
                    keyframenum--;
                    if (keyframenum < 2) {
                        //将该帧放回队列头，因为丢包已经丢了前面的关键帧组，保留后面的两个组
                        break;
                    }
                }
//                LogUtil.e(TAG, "mediaCodecDecode 其它帧在此丢掉 -->");
                //其它帧在此丢掉,不处理
                queue.poll();
            }
            //重新计算队列大小
            queuesize = queue.size();
        }
        //判断解码器是否初始化完成
        if (mediaCodec == null)
            return;
        //队列中有视频帧，检查解码队列中是否有空闲可用的buffer，有则取视频帧送进去解码
        if (queuesize > 0) {
            int inputBufferIndex = -1;
            try {
                inputBufferIndex = mediaCodec.dequeueInputBuffer(0);
                if (inputBufferIndex >= 0) {
                    //有空闲可用的解码buffer
                    ByteBuffer byteBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                    byteBuffer.clear();
                    //将视频队列中的头取出送到解码队列中
                    MediaBean poll = queue.poll();
                    byteBuffer.put(poll.getBytes());
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, poll.getSize(), poll.getPts(), 0);
                }
            } catch (IllegalStateException e) {
                //如果解码出错，需要提示用户或者程序自动重新初始化解码
                mediaCodec = null;
                return;
            }
        }
        //判断解码显示buffer是否初始化完成
        if (info == null)
            return;
        try {
            //使输出缓冲区出队，最多阻塞“ timeoutUs”微秒。 返回已成功解码的输出缓冲区的索引
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 0);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_TRY_AGAIN_LATER:
//                    LogUtil.d(TAG, "mediaCodecDecode -->dequeueOutputBuffer 没有可用数据");
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    MediaFormat outputFormat = mediaCodec.getOutputFormat();
                    LogUtil.e(TAG, "The output format has changed, new format:" + outputFormat);
                    mediaFormat = outputFormat;
                    break;
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    LogUtil.e(TAG, "this event signals that the video scaling mode may have been reset to the default");
                    break;
                default:
                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                    if (outputBuffer != null) {
                        //position和limit方法 解决输出混乱问题
                        outputBuffer.position(info.offset);
                        outputBuffer.limit(info.offset + info.size);
                    }
//                    LogUtil.d(TAG, "mediaCodecDecode releaseOutputBuffer  pts= " + info.presentationTimeUs + ", outputBufferIndex= " + outputBufferIndex);
//                    mediaCodec.releaseOutputBuffer(outputBufferIndex, info.presentationTimeUs);
                    //如果配置编码器时指定了有效的surface，传true将此输出缓冲区显示在surface
                    //释放output缓冲区,否则将会导致MediaCodec输出缓冲被占用，无法继续解码。
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    break;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "mediaCodecDecode dequeueOutputBuffer错误 -->");
            e.printStackTrace();
        }
    }

    public void destroy() {
        isStop = true;
        if (timeThread != null) {
            timeThread.interrupt();
            timeThread = null;
        }
        releaseMediaCodec();
        this.surfaceDestroyed(this.getHolder());
        this.destroyDrawingCache();
        if (glRender != null) {
            glRender.glClear();
            glRender.destory();
            glRender.setWlOnGlSurfaceViewOncreateListener(null);
            glRender.setWlOnRenderRefreshListener(null);
            glRender = null;
        }
    }

    /**
     * 释放资源
     */
    private void releaseMediaCodec() {
        new Thread(() -> {
            if (mediaCodec != null) {
                try {
                    LogUtil.e(TAG, "releaseMediaCodec :   --> ");
                    mediaCodec.reset();
                    //调用stop()方法使编解码器返回到未初始化状态（Uninitialized），此时这个编解码器可以再次重新配置
                    mediaCodec.stop();
                    //调用flush()方法使编解码器重新返回到刷新子状态（Flushed）
                    mediaCodec.flush();
                    //使用完编解码器后，你必须调用release()方法释放其资源
                    mediaCodec.release();
                } catch (MediaCodec.CodecException e) {
                    LogUtil.e(TAG, "run :  CodecException --> " + e.getMessage());
                } catch (IllegalStateException e) {
                    LogUtil.e(TAG, "run :  IllegalStateException --> " + e.getMessage());
                } catch (Exception e) {
                    LogUtil.e(TAG, "run :  Exception --> " + e.getMessage());
                }
            }
            mediaCodec = null;
            mediaFormat = null;
        }).start();
    }

    public void setResId(int resid) {
        this.resId = resid;
    }

    public int getResId() {
        return resId;
    }

    public void setLastPushTime(long timeMillis) {
        this.lastPushTime = timeMillis;
    }

    public void startTimeThread() {
        isStop = false;
        if (timeThread == null && !isStop) {
            timeThread = new releaseThread();
            timeThread.start();
        }
    }

    public void stopTimeThread() {
        isStop = true;
        if (timeThread != null) {
            timeThread = null;
        }
        releaseMediaCodec();
    }

    long framepersecond = 80;//估计每秒的播放时间 单位：毫秒

    class releaseThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isStop) {
                if (System.currentTimeMillis() - lastPushTime >= framepersecond) {
//                    LogUtil.v(TAG, "releaseThread 手动发送空数据 -->resId=" + resId);
                    EventBus.getDefault().post(new EventMessage(EventType.CALLBACK_VIDEO_DECODE, resId, 0, 0, 0, null, null, 1L, 0));
                    try {
                        sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
