package com.pa.paperless.video;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;

import com.blankj.utilcode.util.LogUtils;
import com.mogujie.tt.protobuf.InterfaceStop;
import com.pa.paperless.data.bean.MediaBean;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.service.App;
import com.pa.paperless.utils.LogUtil;

import android.util.Log;
import android.util.Range;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePlaymedia;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.SDLonLineMemberAdapter;
import com.pa.paperless.adapter.rvadapter.SDLonLineProAdapter;
import com.pa.paperless.data.bean.DevMember;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.DateUtil;
import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import cc.shinichi.library.ImagePreview;


/**
 * @author xlk
 * @date 2019/6/27
 */
public class VideoPresenter {
    private final String TAG = "VideoPresenter-->";
    private final IVideo view;
    private final Activity cxt;
    private final NativeUtil jni;
    private String saveMimeType = "";
    private MediaFormat mediaFormat;
    private MediaCodec decoder;
    private MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
    /**
     * 初始化时的视屏宽高
     */
    private int initW, initH;
    private int status;
    /**
     * 在线投影机和参会人
     */
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onlineProjectors = new ArrayList<>();
    private List<DevMember> devMemberInfos = new ArrayList<>();
    private SDLonLineMemberAdapter memberAdapter;
    private SDLonLineProAdapter proAdapter;
    /**
     * 选择要加入同屏的参会人或投影机
     */
    private PopupWindow popup;
    /**
     * 是否正在共享
     */
    private boolean isShareing;
    /**
     * 当前的进度
     */
    private int currentPre;
    /**
     * 用来存放当前正在同屏的设备ID
     */
    private List<Integer> currentShareIds = new ArrayList<>();
    /**
     * 当前播放的媒体ID
     */
    private int mMediaId;
    private Surface surface;
    private boolean isplayStream;
    private int streamId;
    private boolean isStop = false;
    private releaseThread timeThread;
    private long lastPushTime;
    private int inputCount;
    private int outputCount;

    VideoPresenter(Activity context, IVideo view) {
        this.cxt = context;
        this.view = view;
        jni = NativeUtil.getInstance();
    }

    void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    void unregisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    private int triggeruserval = 1;

    void setIsplayStream(boolean a) {
        isplayStream = a;
    }

    /**
     * 打开查看图片
     *
     * @param piclist 图片的路径集合
     */
    private void photoViewer(List<String> piclist, int index) {
        ImagePreview.getInstance()
                .setContext(cxt)
                .setImageList(piclist)//设置图片地址集合
                .setIndex(index)//设置开始的索引
                .setShowDownButton(false)//设置是否显示下载按钮
                .setShowCloseButton(false)//设置是否显示关闭按钮
                .setEnableDragClose(true)//设置是否开启下拉图片退出
                .setEnableUpDragClose(true)//设置是否开启上拉图片退出
                .setEnableClickClose(true)//设置是否开启点击图片退出
                .setShowErrorToast(true)
                .start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.MANDATORY_PLAY://强制性播放流
                view.setCanNotExit();
                break;
            //播放进度通知
            case EventType.PLAY_PROGRESS_NOTIFY: {
                InterfacePlaymedia.pbui_Type_PlayPosCb playPos = (InterfacePlaymedia.pbui_Type_PlayPosCb) message.getObject();
                int mediaId = playPos.getMediaId();
                //0=播放中，1=暂停，2=停止,3=恢复
                int status = playPos.getStatus();
                int per = playPos.getPer();
                int sec = playPos.getSec();
                this.mMediaId = mediaId;
                int subtype = mediaId & Macro.SUB_TYPE_BITMASK;
                boolean isMP3 = subtype == Macro.MEDIA_FILE_TYPE_MP3;
                this.status = status;
                currentPre = per;
                //只有在播放中才更新进度相关UI
                if (this.status == 0) {
                    byte[] timedata = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_TIME.getNumber(),
                            this.mMediaId);
                    InterfaceBase.pbui_CommonInt32uProperty commonInt32uProperty = InterfaceBase.pbui_CommonInt32uProperty.parseFrom(timedata);
                    int propertyval = commonInt32uProperty.getPropertyval();
                    view.updateProgressUi(per, String.valueOf(DateUtil.convertTime(sec)), String.valueOf(DateUtil.convertTime((long) propertyval)));
                    byte[] fileName = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(),
                            this.mMediaId);
                    InterfaceBase.pbui_CommonTextProperty pbui_commonTextProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(fileName);
                    view.updateTopTitle(pbui_commonTextProperty.getPropertyval().toStringUtf8());
                    LogUtil.v(TAG, "播放进度通知："
                            + "\n当前播放：fileName=" + pbui_commonTextProperty.getPropertyval().toStringUtf8()
                            + "\nmediaId=" + mediaId
                            + "\nstatus=" + status
                            + "\nsubtype=" + subtype
                            + "\nisMp3=" + isMP3
                    );
                }
                if (status == 0 || status == 1) view.updateAnimator(status);
                break;
            }
            //YUV格式通常有两大类：打包（packed）格式和平面（planar）格式
            //​前者将YUV分量存放在同一个数组中，通常是几个相邻的像素组成一个宏像素（macro-pixel）；
            //​而后者使用三个数组分开存放YUV三个分量，就像是一个三维平面一样。
            case EventType.CALLBACK_YUVDISPLAY: {
                Object[] objects1 = message.getObjects();
                int resid = (int) objects1[0];
                int w = (int) objects1[1];
                int h = (int) objects1[2];
                byte[] y = (byte[]) objects1[3];
                byte[] u = (byte[]) objects1[4];
                byte[] v = (byte[]) objects1[5];
                LogUtil.v(TAG, "yuv数据大小：" + (y.length + u.length + v.length));
                view.setFrameData(w, h, y, u, v);
                //绝对不能打开，除非需要调试
//                try {
//                    read2file(y, u, v);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
            }
            //解码数据
            case EventType.CALLBACK_VIDEO_DECODE: {
                Object[] objects = message.getObjects();
                int res = (int) objects[0];
                int codecid = (int) objects[1];
                int width = (int) objects[2];
                int height = (int) objects[3];
                byte[] packet = (byte[]) objects[4];
                byte[] codecdata = (byte[]) objects[5];
                long pts = (long) objects[6];
                int iskeyframe = (int) objects[7];
                String mimeType = Macro.getMimeType(codecid);
                int length = 0;
                if (packet != null) {
                    lastPushTime = System.currentTimeMillis();
                    length = packet.length;
                    LogUtil.v(TAG, "getEventMessage :  mimeType --> " + mimeType
                            + "，宽高：" + width + "," + height + ", pts=" + pts + ", codecid=" + codecid
                            + "\ncodecdata=" + Arrays.toString(codecdata) + "\npacket长度=" + packet.length + "\npacket=" + Arrays.toString(packet)
                    );

                    if (!saveMimeType.equals(mimeType) || initW != width || initH != height || decoder == null) {
                        if (decoder != null) {
                            //调用stop方法使其进入 uninitialzed 状态，这样才可以重新配置MediaCodec
                            LogUtil.e(TAG, "getEventMessage 重新配置MediaCodec -->");
                            decoder.stop();
                        }
                        saveMimeType = mimeType;
                        initCodec(width, height, codecdata);
                    }
                    read2file(packet, codecdata);
                }
                mediaCodecDecode(packet, length, pts, iskeyframe);
                if (timeThread == null && !isStop) {
                    timeThread = new releaseThread();
                    timeThread.start();
                }
                break;
            }
            //媒体、流停止播放通知
            case EventType.STOP_PLAY: {
                InterfaceStop.pbui_Type_MeetStopPlay object = (InterfaceStop.pbui_Type_MeetStopPlay) message.getObject();
                // 创建该触发器的设备ID
                int createdeviceid = object.getCreatedeviceid();
                // 停止的资源ID
                int resid = object.getRes();
                // 停止的触发器ID 这是一个用户操作生成的ID,用来标识操作的,可以根据这个ID来判断操作,然后执行停止操作等
                int triggerid = object.getTriggerid();
                LogUtil.e(TAG, "停止播放通知 createdeviceid=" + createdeviceid + ",triggerid=" + triggerid + ",resid=" + resid);
                if (resid == 0) {
                    view.close();
                }
                break;
            }
            case EventType.ACTION_SCREEN_OFF:
            case EventType.ACTION_SCREEN_ON:
            case EventType.MeetSeat_Change_Inform://会议排位变更通知
            case EventType.MEMBER_CHANGE_INFORM://参会人员变更通知
            case EventType.FACESTATUS_CHANGE_INFORM://界面状态变更通知
            case EventType.SIGNIN_SEAT_INFORM://会场设备信息变更通知
                LogUtils.d(TAG, "重新更新在线参会人和投影机");
                queryAttendPeople();
                break;
            case EventType.DEV_REGISTER_INFORM: {// 设备寄存器变更通知
                LogUtils.d(TAG, "设备寄存器变更通知");
                InterfaceDevice.pbui_Type_MeetDeviceBaseInfo object = (InterfaceDevice.pbui_Type_MeetDeviceBaseInfo) message.getObject();
                int deviceid = object.getDeviceid();
                int attribid = object.getAttribid();
//                LogUtil.e(TAG, "getEventMessage :    --> 同屏集合：" + currentShareIds.toString()
//                        + ", mMediaId:" + mMediaId + ", deviceid:" + Values.localDevId);
                Iterator<Integer> iterator = currentShareIds.iterator();
                if (currentShareIds.contains(deviceid)) {
                    InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDevInfoById(deviceid);
                    InterfaceDevice.pbui_Item_DeviceDetailInfo detailInfo = deviceDetailInfo.getPdevList().get(0);
                    InterfaceDevice.pbui_SubItem_DeviceResInfo resinfo = detailInfo.getResinfo(0);
                    int playstatus = resinfo.getPlaystatus();
                    int val = resinfo.getVal();
                    int val2 = resinfo.getVal2();
                    int triggerId = resinfo.getTriggerId();
                    int createdeviceid = resinfo.getCreatedeviceid();
                    LogUtil.d(TAG, "getEventMessage updateInfos :   --> devcieid=" + deviceid +
                            ",  playstatus：" + playstatus + "，  val:" + val +
                            "，  val2:" + val2 + "， triggerId:" + triggerId + ", createid:" + createdeviceid);
                    if (createdeviceid == Values.localDevId) {
                        if (playstatus == 0 || (playstatus == 1 && val != mMediaId)
                                || (playstatus == 2 && val != Values.localDevId)) {
                            while (iterator.hasNext()) {
                                Integer next = iterator.next();
                                if (deviceid == next) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
                //如果同屏集合为空则设置不在共享中
                if (currentShareIds.isEmpty()) {
                    isShareing = false;
                }
                queryAttendPeople();
                break;
            }
            default:
                break;
        }
    }

    public String queryDevName(int devId) {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDevInfoById(devId);
            if (deviceDetailInfo != null) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo pdev = deviceDetailInfo.getPdev(0);
                return pdev.getDevname().toStringUtf8();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return "";
    }

    FileOutputStream yuvfos;

    private void read2file(byte[] y, byte[] u, byte[] v) throws IOException {
        if (!App.read2file) {
            return;
        }
        if (yuvfos == null) {
            File file = new File(Macro.ROOT + "/temp.yuv");
            if (file.exists()) {
                file.delete();
            }
            yuvfos = new FileOutputStream(Macro.ROOT + "/temp.yuv");
        }
        yuvfos.write(y);
        yuvfos.write(u);
        yuvfos.write(v);
    }

    private BufferedOutputStream outputStream;

    public void read2file(byte[] outData, byte[] codecdata) {
        if (!App.read2file) {
            return;
        }
        try {
            if (outputStream == null) {
                File file = new File(Macro.ROOT + "/temp.mp4");
                if (file.exists()) {
                    file.delete();
                }
                outputStream = new BufferedOutputStream(new FileOutputStream(file));
            }
            outputStream.write(outData, 0, outData.length);
            outputStream.write(codecdata, 0, codecdata.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void queryAttendPeople() {
        try {
            LogUtil.d(TAG, "queryAttendPeople");
            InterfaceMember.pbui_Type_MemberDetailInfo attendPeople = jni.queryAttendPeople();
            List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos = new ArrayList<>();
            if (attendPeople != null) {
                memberInfos.addAll(attendPeople.getItemList());
            }
            InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDeviceInfo();
            List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos = new ArrayList<>();
            if (deviceDetailInfo != null) {
                deviceInfos.addAll(deviceDetailInfo.getPdevList());
            }
            updateInfos(memberInfos, deviceInfos);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    void releaseMediaRes() {
        isStop = true;
        LogUtil.e(TAG, "releaseMediaRes :   --> ");
        List<Integer> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        a.add(0);
        b.add(Values.localDevId);
        //停止资源操作
        jni.stopResourceOperate(a, b);
    }

    /**
     * 释放资源
     */
    public void releaseMediaCodec() {
        new Thread(() -> {
            if (decoder != null) {
                try {
                    LogUtil.e(TAG, "releaseMediaCodec :   --> ");
                    decoder.reset();
                    //调用stop()方法使编解码器返回到未初始化状态（Uninitialized），此时这个编解码器可以再次重新配置
                    decoder.stop();
                    //调用flush()方法使编解码器重新返回到刷新子状态（Flushed）
                    decoder.flush();
                    //使用完编解码器后，你必须调用release()方法释放其资源
                    decoder.release();
                } catch (MediaCodec.CodecException e) {
                    LogUtil.e(TAG, "run :  CodecException --> " + e.getMessage());
                } catch (IllegalStateException e) {
                    LogUtil.e(TAG, "run :  IllegalStateException --> " + e.toString());
                } catch (Exception e) {
                    LogUtil.e(TAG, "run :  Exception --> " + e.getMessage());
                }
            }
            decoder = null;
            mediaFormat = null;
        }).start();
    }

    /**
     * 初始化解码器
     *
     * @param w         宽
     * @param h         高
     * @param codecdata pps/sps 编码配置数据
     */
    private void initCodec(int w, int h, byte[] codecdata) {
        try {
            view.setCodecType(1);
            try {
                //1.创建了一个编解码器，此时编解码器处于未初始化状态（Uninitialized）
                decoder = MediaCodec.createDecoderByType(saveMimeType);
            } catch (IOException | IllegalArgumentException e) {
                LogUtils.e(TAG, "createDecoderByType异常：" + e.toString());
                e.printStackTrace();
            }
            initMediaFormat(w, h, codecdata);
            try {
                //2.对编解码器进行配置，这将使编解码器转为配置状态（Configured）
                if (!surface.isValid()) {
                    Log.e(TAG, "initCodec :  surface是无效的 --> ");
                    return;
                }
                decoder.configure(mediaFormat, surface, null, 0);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (MediaCodec.CodecException e) {
                //可能是由于media内容错误、硬件错误、资源枯竭等原因所致
                //可恢复错误（recoverable errors）：如果isRecoverable() 方法返回true,然后就可以调用stop(),configure(...),以及start()方法进行修复
                //短暂错误（transient errors）：如果isTransient()方法返回true,资源短时间内不可用，这个方法可能会在一段时间之后重试。
                //isRecoverable()和isTransient()方法不可能同时都返回true。
                LogUtils.e(TAG, "initCodec :   -->可恢复错误： " + e.isRecoverable() + ",短暂错误：" + e.isTransient());
            }
            //3.调用start()方法使其转入执行状态（Executing）
            decoder.start();
        } catch (Exception e) {
            LogUtils.e(TAG, "异常=" + e.toString());
            e.printStackTrace();
        }
    }

    private MediaCodecInfo getSupportedMediaCodecInfo(String mimeType) {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] codecInfos = mediaCodecList.getCodecInfos();
        MediaCodecInfo supportedCodecInfo = null;
        for (MediaCodecInfo codecInfo : codecInfos) {
            String name = codecInfo.getName();
            String[] types = codecInfo.getSupportedTypes();
            if (codecInfo.isEncoder()) {
//                LogUtil.d(TAG, "getSupportedMediaCodecInfo 编码器-->" + name);
                continue;
            }
            for (String type : types) {
                LogUtil.v(TAG, "getSupportedMediaCodecInfo  支持的媒体类型= " + name + " --- " + type);
                if (type.equalsIgnoreCase(mimeType)) {
                    supportedCodecInfo = codecInfo;
                    String[] supportedTypes = supportedCodecInfo.getSupportedTypes();
                    LogUtil.i(TAG, "getSupportedMediaCodecInfo codecInfo名称=" + supportedCodecInfo.getName());
                    for (String supportedType : supportedTypes) {
                        LogUtil.d(TAG, "getSupportedMediaCodecInfo 支持的类型=" + supportedType);
                    }
                }
            }
        }
        if (supportedCodecInfo == null) {
            LogUtils.e(TAG, "getSupportedMediaCodecInfo 没有找到支持 " + mimeType + " 类型的解码器");
        }
        return supportedCodecInfo;
    }

    private void initMediaFormat(int w, int h, byte[] csddata) {
        MediaCodecInfo mediaCodecInfo//= getSupportedMediaCodecInfo(saveMimeType);
                = decoder.getCodecInfo();
        /**  颜色格式  */
        MediaCodecInfo.CodecCapabilities capabilitiesForType = mediaCodecInfo.getCapabilitiesForType(saveMimeType);
        /**  宽高要判断是否是解码器所支持的范围  */
        MediaCodecInfo.VideoCapabilities videoCapabilities = capabilitiesForType.getVideoCapabilities();
        Range<Integer> supportedWidths = videoCapabilities.getSupportedWidths();
        Range<Integer> supportedHeights = videoCapabilities.getSupportedHeights();
        initW = w;
        initH = h;
        if (w > supportedWidths.getUpper()) {
            w = supportedWidths.getUpper();
            h = videoCapabilities.getSupportedHeightsFor(w).getUpper();
        }
        if (h > supportedHeights.getUpper()) {
            h = supportedHeights.getUpper();
            w = videoCapabilities.getSupportedWidthsFor(h).getUpper();
        }
        mediaFormat = MediaFormat.createVideoFormat(saveMimeType, w, h);
        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, w);
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, h);
        if (csddata != null) {
            ByteBuffer csd_0 = ByteBuffer.wrap(csddata);
            ByteBuffer csd_1 = ByteBuffer.wrap(csddata);
            mediaFormat.setByteBuffer("csd-0", csd_0);
            mediaFormat.setByteBuffer("csd-1", csd_1);
        }
        LogUtil.e(TAG, "initMediaFormat :   --> 调整后的宽高：" + w + "," + h + ", mediaFormat=" + mediaFormat);
    }

    /**
     * index              - 缓冲区索引
     * offset             - 缓冲区提交数据的起始位置
     * size               - 提交的数据长度
     * presentationTimeUs - 时间戳 单位：微秒
     * flags              - BUFFER_FLAG_CODEC_CONFIG：配置信息；
     * BUFFER_FLAG_END_OF_STREAM：结束标志；
     * BUFFER_FLAG_KEY_FRAME：关键帧
     * void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags)
     */
    private boolean discard = false;//是否丢弃状态
    LinkedBlockingQueue<MediaBean> queue = new LinkedBlockingQueue<>();//如果不指定容量，默认为Integer.MAX_VALUE

    private void mediaCodecDecode1(byte[] bytes, int size, long pts, int iskeyframe) {
//        allCount++;
//        LogUtil.i(TAG, "接收方 接收数据总量：" + allCount);
//        if (iskeyframe != 1) {
//            count++;
//            LogUtil.d(TAG, "接收方 收到普通帧数据：pts= " + pts + ", size= " + size);
//        } else {
//            LogUtil.e(TAG, "接收方 收到关键帧数据 关键帧之间的个数=" + count + ", pts= " + pts + ", size= " + size + ", 接收数据总量：" + allCount);
//            count = 1;
//        }
        try {
            //返回要用有效数据填充的输入缓冲区的索引；如果当前没有可用的缓冲区，则返回-1。
            // 如果timeoutUs == 0，则此方法将立即返回；
            // 如果timeoutUs <0，则无限期等待输入缓冲区的可用性；
            // 如果timeoutUs> 0，则等待直至“ timeoutUs”微秒。
            int inputBufferIndex = decoder.dequeueInputBuffer(0);

//            LogUtil.i(TAG, "mediaCodecDecode -->dequeueInputBuffer index= " + inputBufferIndex);
            if (inputBufferIndex >= 0) {
                //有空闲可用的解码buffer
                ByteBuffer byteBuffer = decoder.getInputBuffer(inputBufferIndex);
                byteBuffer.clear();
                //将视频队列中的头取出送到解码队列中
                byteBuffer.put(bytes);
                decoder.queueInputBuffer(inputBufferIndex, 0, size, pts, 0);
//                LogUtil.i(TAG, "mediaCodecDecode dequeueInputBuffer  pts= " + pts + ", inputBufferIndex = " + inputBufferIndex + ", inputCount：" + (++inputCount));
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "mediaCodecDecode dequeueInputBuffer 异常 -->" + e.getMessage());
            //如果解码出错，需要提示用户或者程序自动重新初始化解码
            decoder = null;
            return;
        }
        try {
            //使输出缓冲区出队，最多阻塞“ timeoutUs”微秒。 返回已成功解码的输出缓冲区的索引
            int outputBufferIndex = decoder.dequeueOutputBuffer(info, 0);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_TRY_AGAIN_LATER:
//                    LogUtil.d(TAG, "mediaCodecDecode -->dequeueOutputBuffer 没有可用数据");
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    MediaFormat outputFormat = decoder.getOutputFormat();
                    LogUtil.e(TAG, "The output format has changed, new format:" + outputFormat);
                    mediaFormat = outputFormat;
                    break;
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
//                    LogUtil.e(TAG, "this event signals that the video scaling mode may have been reset to the default");
                    break;
                default:
                    LogUtil.d(TAG, " -->mediaCodecDecode dequeueOutputBuffer outputBufferIndex= " + outputBufferIndex);
                    ByteBuffer outputBuffer = decoder.getOutputBuffer(outputBufferIndex);
                    //position和limit方法 解决输出混乱问题
                    if (outputBuffer != null) {
                        outputBuffer.position(info.offset);
                        outputBuffer.limit(info.offset + info.size);
                    }
                    //如果配置编码器时指定了有效的surface，传true将此输出缓冲区显示在surface
                    //释放output缓冲区,否则将会导致MediaCodec输出缓冲被占用，无法继续解码。
                    decoder.releaseOutputBuffer(outputBufferIndex, true);
                    break;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "mediaCodecDecode dequeueOutputBuffer错误 -->");
            e.printStackTrace();
        }
    }

    int count = 0;
    int allCount = 0;

    /**
     * 处理逻辑：收到数据包进行解码显示，
     * queue队列的作用：在发送方因为网络原因卡顿时，后台会没有数据返回。则使用 releaseThread线程发送空数据，当接收到的数据为空时，仍然会从queue队列中取数据
     */
    private void mediaCodecDecode(byte[] bytes, int size, long pts, int iskeyframe) {
        if (isStop) {
            LogUtil.e(TAG, "mediaCodecDecode isstop");
            return;
        }
        if (bytes != null && bytes.length > 0) {
            if (App.isDebug) {
                allCount++;
                LogUtil.v(TAG, "接收方 接收数据总量：" + allCount);
                if (iskeyframe != 1) {
                    count++;
                    LogUtil.v(TAG, "接收方 收到普通帧数据：pts= " + pts + ", size= " + size);
                } else {
                    LogUtil.v(TAG, "接收方 收到关键帧数据 关键帧之间的个数=" + count + ", 发送数据总量=" + allCount + ", pts= " + pts + ", size= " + size);
                    count = 0;
                }
            }
            //把网络接收到的视频数据先加入到队列中
            queue.offer(new MediaBean(bytes, size, pts, iskeyframe));
        } else {
            //bytes为null也不能立马返回，需要处理从视频队列中送数据到解码buffer 和 解码好的视频的显示
        }
        int queuesize = queue.size();
        if (queuesize > 500) {
//            LogUtil.v(TAG, " mediaCodecDecode -->解码速度太慢 queuesize: " + queuesize);
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
                LogUtil.v(TAG, "mediaCodecDecode 其它帧在此丢掉 -->");
                //其它帧在此丢掉,不处理
                queue.poll();
            }
            //重新计算队列大小
            queuesize = queue.size();
        }
        //判断解码器是否初始化完成
        if (decoder == null) {
            LogUtil.v(TAG, "mediaCodecDecode mediacodec null");
            return;
        }
        //队列中有视频帧，检查解码队列中是否有空闲可用的buffer，有则取视频帧送进去解码
        if (queue.size() > 0) {
            LogUtil.v(TAG, "mediaCodecDecode queue.size()= " + queue.size());
            int inputBufferIndex = -1;
            try {
                //返回要用有效数据填充的输入缓冲区的索引；如果当前没有可用的缓冲区，则返回-1。
                // 如果timeoutUs == 0，则此方法将立即返回；
                // 如果timeoutUs <0，则无限期等待输入缓冲区的可用性；
                // 如果timeoutUs> 0，则等待直至“ timeoutUs”微秒。
                inputBufferIndex = decoder.dequeueInputBuffer(0);
                LogUtil.v(TAG, "mediaCodecDecode -->dequeueInputBuffer index= " + inputBufferIndex);
                if (inputBufferIndex >= 0) {
                    //有空闲可用的解码buffer
                    ByteBuffer byteBuffer = decoder.getInputBuffer(inputBufferIndex);
                    //将视频队列中的头取出送到解码队列中
                    MediaBean poll = queue.poll();
                    if (byteBuffer != null) {
                        byteBuffer.clear();
                        byteBuffer.put(poll.getBytes());
                    }
                    decoder.queueInputBuffer(inputBufferIndex, 0, poll.getSize(), poll.getPts(), 0);
                    LogUtil.v(TAG, "mediaCodecDecode queueInputBuffer  pts= " + poll.getPts() + ", index = " + inputBufferIndex + ", inputCount：" + (++inputCount));
                }
            } catch (IllegalStateException e) {
                //如果解码出错，需要提示用户或者程序自动重新初始化解码
//                decoder = null;
                e.printStackTrace();
                return;
            }
        }
        //判断解码显示buffer是否初始化完成
        if (info == null) {
            LogUtil.v(TAG, "mediaCodecDecode MediaCodec.BufferInfo为空 -->");
            return;
        }
        //判断下一帧的播放时间是否已经到了
//        if (System.currentTimeMillis() - lastplaytime < framepersecond) {
//            return;
//        }
        try {
            //使输出缓冲区出队，最多阻塞“ timeoutUs”微秒。 返回已成功解码的输出缓冲区的索引
            int index = decoder.dequeueOutputBuffer(info, 0);
            switch (index) {
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    LogUtil.v(TAG, "mediaCodecDecode -->dequeueOutputBuffer index = " + index);
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    MediaFormat outputFormat = decoder.getOutputFormat();
                    LogUtil.v(TAG, "The output format has changed, new format:" + outputFormat);
                    break;
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    LogUtil.v(TAG, "this event signals that the video scaling mode may have been reset to the default");
                    break;
                default:
                    ByteBuffer outputBuffer = decoder.getOutputBuffer(index);
                    if (outputBuffer != null) {
                        outputBuffer.position(info.offset);
                        outputBuffer.limit(info.offset + info.size);
                    }
                    LogUtil.v(TAG, "mediaCodecDecode --> dequeueOutputBuffer：查看info：" + index
                            + "\nflags：" + info.flags + ", offset：" + info.offset + ", size：" + info.size
                            + ", presentationTimeUs：" + info.presentationTimeUs);
                    LogUtil.v(TAG, "mediaCodecDecode releaseOutputBuffer  pts= " + info.presentationTimeUs
                            + ", index= " + index + " outputCount：" + (++outputCount));
//            mediaCodec.releaseOutputBuffer(index, info.presentationTimeUs);
                    //如果配置编码器时指定了有效的surface，传true将此输出缓冲区显示在surface
                    decoder.releaseOutputBuffer(index, true);
                    break;
            }
        } catch (Exception e) {
            LogUtil.v(TAG, "mediaCodecDecode dequeueOutputBuffer错误 -->");
            e.printStackTrace();
        }
    }

    long framepersecond = 80;//估计每秒的播放时间 单位：毫秒

    public void releasePlay() {
        // TODO: 2020/7/27 停止掉之前的播放信息
        if (timeThread != null) {
            timeThread.interrupt();
            timeThread = null;
        }
        if (yuvfos != null) {
            try {
                yuvfos.close();
                yuvfos.flush();
                yuvfos = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
                outputStream.flush();
                outputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        releaseMediaCodec();
    }

    public void initPlay() {
        // TODO: 2020/7/27 初始化播放
    }

    /**
     * 作用：当过了framepersecond毫秒还没有收到新的播放数据，就自动发送空数据
     */
    class releaseThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isStop) {
                if (System.currentTimeMillis() - lastPushTime >= framepersecond) {
                    LogUtil.v(TAG, "mediaCodecDe 手动发送空数据 -->" + isInterrupted());
                    if (isInterrupted()) return;
                    EventBus.getDefault().post(new EventMessage(EventType.CALLBACK_VIDEO_DECODE, 0, 0, 0, 0, null, null, 1L, 0));
                    try {
                        sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void create() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4");
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 按钮点击播放/暂停
     */
    void playOrPause() {
        List<Integer> devIds = new ArrayList<>();
        devIds.add(Values.localDevId);
        if (isShareing) {
            devIds.addAll(currentShareIds);
        }
        if (isPlaying()) {
            jni.setPlayStop(0, devIds);
        } else {
            jni.setPlayRecover(0, devIds);
        }
    }

    /**
     * 跟去播放状态判断是否播放中
     *
     * @return true 播放中
     */
    private boolean isPlaying() {
        switch (status) {
            //播放中
            case 0:
                return true;
            //暂停
            case 1:
                return false;
            //停止
            case 2:
                return false;
            //恢复
            case 3:
                return true;
            default:
                return true;
        }
    }

    /**
     * 发起同屏
     */
    void startScreen() {
        queryAttendPeople();
        showScreenPopup();
    }

    private void showScreenPopup() {
        View inflate = LayoutInflater.from(cxt).inflate(R.layout.pop_choose_member, null);
        popup = new PopupWindow(inflate, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        popup.setTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable());
        popup.setOutsideTouchable(true);
        popup.setAnimationStyle(R.style.Anim_PopupWindow);
        popup.showAtLocation(view.getView(), Gravity.CENTER, 0, 0);
        ViewHolder viewHolder = new ViewHolder(inflate);
        holderEvent(viewHolder);
    }

    private void holderEvent(ViewHolder holder) {
        holder.title.setText(cxt.getString(R.string.start_screen));
        holder.playersRl.setLayoutManager(new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL));
        holder.playersRl.setAdapter(memberAdapter);
        holder.projectorRl.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        holder.projectorRl.setAdapter(proAdapter);

        memberAdapter.setItemClick((view, posion) -> {
            int devId = devMemberInfos.get(posion).getDevId();
            memberAdapter.setCheck(devId);
            holder.playersAllCb.setChecked(memberAdapter.isAllCheck());
            memberAdapter.notifyDataSetChanged();
        });
        proAdapter.setItemClick((view, posion) -> {
            int devId = onlineProjectors.get(posion).getDevcieid();
            proAdapter.setCheck(devId);
            holder.projectorAllCb.setChecked(proAdapter.isAllCheck());
            proAdapter.notifyDataSetChanged();
        });
        holder.playersAllCb.setOnClickListener(v -> {
            boolean checked = holder.playersAllCb.isChecked();
            holder.playersAllCb.setChecked(checked);
            memberAdapter.setAllCheck(checked);
        });
        holder.projectorAllCb.setOnClickListener(v -> {
            boolean checked = holder.projectorAllCb.isChecked();
            holder.projectorAllCb.setChecked(checked);
            proAdapter.setAllCheck(checked);
        });
        holder.ensure.setOnClickListener(v -> {
            /**  点击发起同屏时，将已经正在同屏的设备进行设置播放进度  */
//            if (isShareing) {
//                List<Integer> setPlayPlaceIds = new ArrayList<>();
//                setPlayPlaceIds.addAll(currentShareIds);
//                setPlayPlaceIds.add(Values.localDevId);
//                jni.setPlayPlace(0, currentPre, setPlayPlaceIds, triggeruserval, 0);
//            }
            List<Integer> checks = memberAdapter.getChecks();
            checks.addAll(proAdapter.getChecks());
            for (Integer a : checks) {
                if (!currentShareIds.contains(a)) {
                    currentShareIds.add(a);
                }
            }
            ArrayList<Integer> temps = new ArrayList<>();
            temps.addAll(currentShareIds);
//            temps.add(Values.localDevId);
            isShareing = true;
            LogUtil.e(TAG, "holderEvent :   -->currentShareIds: " + currentShareIds.toString());
            triggeruserval = holder.cb_mandatory.isChecked() ? InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE : 0;
            if (isplayStream) {
                List<Integer> res = new ArrayList<>();
                res.add(0);
                jni.streamPlay(streamId, 2, triggeruserval, res, temps);
//                ArrayList<Integer> temps1 = new ArrayList<>();
//                temps1.add(Values.localDevId);
//                jni.streamPlay(streamId, 2, 0, res, temps1);
            } else {
                //媒体播放操作
                jni.mediaPlayOperate(mMediaId, temps, currentPre, 0, triggeruserval, 0);
//                ArrayList<Integer> temps1 = new ArrayList<>();
//                temps1.add(Values.localDevId);
//                jni.mediaPlayOperate(mMediaId, temps1, currentPre, 0, 0, 0);

                jni.setPlayPlace(0, currentPre, temps, triggeruserval, 0);
            }
            //再设置自己的播放进度
//            List<Integer> addids = new ArrayList<>();
//            addids.add(Values.localDevId);
//            jni.setPlayPlace(0, currentPre, addids, triggeruserval, 0);
            popup.dismiss();
        });
        holder.cancel.setOnClickListener(v -> {
            popup.dismiss();
        });
        holder.playersAllCb.performClick();
        holder.projectorAllCb.performClick();
    }

    /**
     * 停止同屏
     */
    void stopScreen() {
        if (isShareing) {
            List<Integer> res = new ArrayList<>();
            res.add(0);
            jni.stopResourceOperate(res, currentShareIds);
            currentShareIds.clear();
            isShareing = false;
        }
    }

    /**
     * 更新参会人和投影机
     *
     * @param memberInfos
     * @param deviceInfos
     */
    private void updateInfos(List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos, List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos) {
        LogUtils.d(TAG, "updateInfos");
        devMemberInfos.clear();
        onlineProjectors.clear();
        for (InterfaceDevice.pbui_Item_DeviceDetailInfo dev : deviceInfos) {
            int devcieid = dev.getDevcieid();
            int netstate = dev.getNetstate();
            int memberid = dev.getMemberid();
            int facestate = dev.getFacestate();
            //找到在线的投影机
            if (Macro.DEVICE_MEET_PROJECTIVE == (devcieid & Macro.DEVICE_MEET_ID_MASK) && netstate == 1) {
                onlineProjectors.add(dev);
            }
            if (!memberInfos.isEmpty() && netstate == 1 && facestate == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MemFace_VALUE) {
                for (InterfaceMember.pbui_Item_MemberDetailInfo member : memberInfos) {
                    if (member.getPersonid() == memberid && devcieid != Values.localDevId) {
                        devMemberInfos.add(new DevMember(member, devcieid));
                    }
                }
            }
        }
        if (memberAdapter == null) {
            memberAdapter = new SDLonLineMemberAdapter(devMemberInfos);
        } else {
            memberAdapter.notifyDataSetChanged();
//            memberAdapter.notifyChecks();
        }
        if (proAdapter == null) {
            proAdapter = new SDLonLineProAdapter(onlineProjectors);
        } else {
            proAdapter.notifyDataSetChanged();
//            proAdapter.notifyChecks();
        }
    }

    /**
     * 手动操作暂停播放
     */
    void pause() {
        if (isPlaying()) {
            List<Integer> devIds = new ArrayList<>();
            devIds.add(Values.localDevId);
            jni.setPlayStop(0, devIds);
        }
    }

    /**
     * 设置播放进度
     *
     * @param progress 进度
     */
    void setPlayPlace(int progress) {
        List<Integer> devIds = new ArrayList<Integer>();
        devIds.add(Values.localDevId);
        if (isShareing) {
            devIds.addAll(currentShareIds);
        }
        jni.setPlayPlace(0, progress, devIds, triggeruserval, 0);
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    public static class ViewHolder {
        public View rootView;
        public TextView title;
        public CheckBox playersAllCb;
        public CheckBox cb_mandatory;
        public RecyclerView playersRl;
        public CheckBox projectorAllCb;
        public RecyclerView projectorRl;
        public Button ensure;
        public Button cancel;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.title = (TextView) rootView.findViewById(R.id.title);
            this.cb_mandatory = (CheckBox) rootView.findViewById(R.id.cb_mandatory);
            this.playersAllCb = (CheckBox) rootView.findViewById(R.id.players_all_cb);
            this.playersRl = (RecyclerView) rootView.findViewById(R.id.players_rl);
            this.projectorAllCb = (CheckBox) rootView.findViewById(R.id.projector_all_cb);
            this.projectorRl = (RecyclerView) rootView.findViewById(R.id.projector_rl);
            this.ensure = (Button) rootView.findViewById(R.id.ensure);
            this.cancel = (Button) rootView.findViewById(R.id.cancel);
        }

    }
}