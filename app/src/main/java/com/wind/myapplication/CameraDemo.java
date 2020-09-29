package com.wind.myapplication;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.helper.AvcEncoder;
import com.pa.paperless.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;


import static com.pa.paperless.service.ShotApplication.CameraH;
import static com.pa.paperless.service.ShotApplication.CameraW;

public class CameraDemo extends Activity implements SurfaceHolder.Callback,
        PreviewCallback {

    private final String TAG = "CameraDemo -->";
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Parameters parameters;

    public static int width = CameraW;
    public static int height = CameraH;
    int framerate = 20;
    int biterate = width * height ;
    private int imageFormat = -1;
    private static int yuvqueuesize = 10;

    public static final ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(
            yuvqueuesize);
    private AvcEncoder avcCodec;
    private ArrayList allRes = new ArrayList(), devIds = new ArrayList();
    public static boolean isbusy;
    private int camera_type;
    private SurfaceTexture texture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        isbusy = true;
        Intent intent = getIntent();
        camera_type = intent.getIntExtra("camera_type", 0);//默认是0,后置摄像头
        surfaceview = findViewById(R.id.surfaceview);
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.addCallback(this);
        texture = new SurfaceTexture(10);//实现后台录制
        EventBus.getDefault().register(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = getBackCamera(camera_type);
        startcamera(camera);
        try {
            avcCodec = new AvcEncoder(width, height, framerate, biterate, imageFormat);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        avcCodec.StartEncoderThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtil.e(TAG, "CameraDemo.surfaceChanged :   --> ");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.e(TAG, "CameraDemo.surfaceDestroyed :   --> ");
        //如果不添加下面这句则会报错:java.lang.RuntimeException: Camera is being used after Camera.release() was called
        holder.removeCallback(this);
    }

    @Override
    protected void onDestroy() {
        exitCamera();
        super.onDestroy();
    }

    private void exitCamera() {
        LogUtil.e(TAG, "CameraDemo.exitCamera :   --> ");
        if (null != camera) {
            avcCodec.StopThread();
            /** ************ ******  停止资源操作  ****** ************ **/
            NativeUtil.getInstance().stopResourceOperate(allRes, devIds);
            /** ************ ******  释放播放资源  ****** ************ **/
            NativeUtil.getInstance().mediaDestroy(0);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            isbusy = false;
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.STOP_COLLECTION_STREAM_NOTIFY:
                int type = (int) message.getObject();
                if (type == 3) {
                    LogUtil.e(TAG, "CameraDemo.getEventMessage :  停止摄像通知 --> ");
                    exitCamera();
                    finish();
                }
                break;default:break;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // TODO Auto-generated method stub
        LogUtil.v(TAG, "rawDataLen=" + data.length);
        putYUVData(data, data.length);
    }

    public void putYUVData(byte[] buffer, int length) {
        if (YUVQueue.size() >= 5) {
            YUVQueue.poll();
        }
        YUVQueue.add(buffer);
    }

    @SuppressLint("NewApi")
    private boolean SupportAvcCodec() {
        if (Build.VERSION.SDK_INT >= 18) {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
                String[] types = codecInfo.getSupportedTypes();
                for (String type : types) {
                    LogUtil.i(TAG, "SupportAvcCodec:" + type);
                }
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equalsIgnoreCase("video/avc")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void startcamera(Camera mCamera) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(this);
                if (parameters == null) {
                    parameters = mCamera.getParameters();
                }
                if (camera_type == 0)
                    parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                checkSupportColorFormat();
                LogUtil.i(TAG, "Camera PreviewFormat=" + (imageFormat == ImageFormat.NV21 ? "NV21" : imageFormat == ImageFormat.YV12 ? "YV12" : imageFormat));
                parameters.setPreviewFrameRate(20);
                parameters.setPreviewFormat(imageFormat);//NV21 YV12
                parameters.setPreviewSize(width, height);
                mCamera.setParameters(parameters);
                //mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.setPreviewTexture(texture);
                mCamera.startPreview();
                //开启预览后就移动Activity到后台
                moveTaskToBack(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkSupportColorFormat() {
        List<Integer> previewFormatsSizes = parameters.getSupportedPreviewFormats();
        if (-1 != previewFormatsSizes.indexOf(ImageFormat.YV12)) {
            imageFormat = ImageFormat.YV12;
        } else if (-1 != previewFormatsSizes.indexOf(ImageFormat.NV21)) {
            imageFormat = ImageFormat.NV21;
        } else {
            imageFormat = -1;
            return;
        }
    }


    @TargetApi(9)
    private Camera getBackCamera(int type) {
        int cameras = Camera.getNumberOfCameras();
        LogUtil.i(TAG, "camera num:" + cameras);
        Camera c = null;
        try {
            c = Camera.open(type);
            setCameraOrientation(c);
            c.cancelAutoFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public void setCameraOrientation(Camera camera) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(0, info);
        int rotation = this.getWindowManager().getDefaultDisplay()
                .getRotation();
        LogUtil.i(TAG, "rotation : " + rotation);
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        LogUtil.i(TAG, "degrees : " + degrees);
        int result;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
}
