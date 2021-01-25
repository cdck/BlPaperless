package com.pa.paperless.fragment;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.pa.paperless.utils.LogUtil;

import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.MyUtils;


import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by xlk on 2017/10/31.
 * 外部文档
 */

public class CameraFragment extends BaseFragment implements SurfaceHolder.Callback, View.OnClickListener, Camera.PreviewCallback {

    private SurfaceView mCameraView;
    private SurfaceHolder surfaceHolder;
    private final String TAG = "CameraFragment-->";
    public static boolean cameraIsShowing;//当前页面是否显示
    private Button take_photo_btn, pre_be, pre_back;
    public static boolean can_take;
    private Camera camera;
    private Camera.Parameters parameters;
    private int imageFormat;
    private int width = 0, height = 0;
    private static int yuvqueuesize = 10;
    public static final ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(
            yuvqueuesize);
    private int CAMERA_TYPE = 1;//1:前置,0: 后置
    private Bitmap mBitmap;
    private CameraFragment context;
    public static boolean isOPenCamera = true;//进入Camera后是否正常开启完毕

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        LogUtil.e(TAG, "CameraFragment.onCreateView :   --> ");
        View inflate = inflater.inflate(R.layout.overviewdoc_fragment, container, false);
        initView(inflate);
        cameraIsShowing = true;
        can_take = true;
        context = this;
        surfaceHolder = mCameraView.getHolder();
        surfaceHolder.addCallback(this);
        return inflate;
    }


    private void initView(View inflate) {
        mCameraView = inflate.findViewById(R.id.camera_surfaceView);
        take_photo_btn = inflate.findViewById(R.id.take_photo);
        pre_be = inflate.findViewById(R.id.pre_be);
        pre_back = inflate.findViewById(R.id.pre_back);
        take_photo_btn.setOnClickListener(this);
        pre_be.setOnClickListener(this);
        pre_back.setOnClickListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtil.e(TAG, "CameraFragment.surfaceCreated :   --> ");
        isOPenCamera = false;
        camera = getBackCamera(CAMERA_TYPE);
        startcamera(camera);
    }

    private Camera getBackCamera(int type) {
        LogUtil.e(TAG, "CameraFragment.getBackCamera :   --> ");
        int cameras = Camera.getNumberOfCameras();
        LogUtil.i(TAG, "camera num:" + cameras);
        Camera c = null;
        try {
            c = Camera.open(type);
            setCameraOrientation(c);
//            c.cancelAutoFocus();//取消自动对焦
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public void setCameraOrientation(Camera camera) {
        LogUtil.e(TAG, "CameraFragment.setCameraOrientation :   --> ");
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay()
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
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void initCameraSize(Camera c) {
        width = 0;
        height = 0;
        ArrayList<Integer> supportW = new ArrayList<>();
        ArrayList<Integer> supportH = new ArrayList<>();
        int largestW = 0, largestH = 0;
        if (c != null)
            parameters = c.getParameters();
        if (parameters == null) return;
        for (int i = 0; i < parameters.getSupportedPreviewSizes().size(); i++) {
            int w = parameters.getSupportedPreviewSizes().get(i).width, h = parameters.getSupportedPreviewSizes().get(i).height;
            LogUtil.v(TAG, w + "*" + h);
            supportW.add(w);
            supportH.add(h);
        }
        for (int i = 0; i < supportH.size(); i++) {
            try {
                largestW = supportW.get(i);
                largestH = supportH.get(i);
                LogUtil.i(TAG, "loopSupportSize: largest w=" + largestW + " h=" + largestH);
                MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", largestW, largestH);
                if (MediaCodec.createEncoderByType("video/avc").getCodecInfo().getCapabilitiesForType("video/avc").isFormatSupported(mediaFormat)) {
                    if (largestW * largestH > width * height) {
                        width = largestW;
                        height = largestH;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LogUtil.e(TAG, "loopSupportSize: 当前合适像素: width=" + width + " height=" + height);
    }

    private void startcamera(final Camera mCamera) {
        new Thread(()->{
            if (mCamera != null) try {
                mCamera.setPreviewCallback(context);
                initCameraSize(mCamera);
                if (parameters == null) parameters = mCamera.getParameters();
                checkSupportColorFormat();
                if (CAMERA_TYPE == 0)
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//设置自动聚焦
                LogUtil.i(TAG, "Camera PreviewFormat=" + (imageFormat == ImageFormat.NV21 ? "NV21" : imageFormat == ImageFormat.YV12 ? "YV12" : imageFormat)
                        + ",预览宽高:" + width + ", " + height);
                parameters.setPreviewSize(width, height);
                parameters.setPictureSize(width, height);
                parameters.setPreviewFormat(imageFormat);//NV21 YV12
                parameters.setPreviewFrameRate(20);
                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
                isOPenCamera = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void checkSupportColorFormat() {
        LogUtil.e(TAG, "CameraFragment.checkSupportColorFormat :   --> ");
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

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtil.e(TAG, "CameraFragment.surfaceChanged :   --> ");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.e(TAG, "CameraFragment.surfaceDestroyed :   --> ");
        //如果不添加下面这句则会报错:java.lang.RuntimeException: Camera is being used after Camera.release() was called
        holder.removeCallback(this);
        exitCamera();
    }

    /**
     * 照相按钮事件监听
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_photo:
                take_photo_Event();
                break;
            case R.id.pre_be:
                setCameraOri(1);
                break;
            case R.id.pre_back:
                setCameraOri(0);
                break;
        }
    }

    private void setCameraOri(int ori) {
        int numberOfCameras = Camera.getNumberOfCameras();//获取摄像机的个数 前/后置
        if (numberOfCameras < 2 && (ori == 1)) {
            ToastUtils.showShort(R.string.tip_no_camera);
            return;
        }
        if (CAMERA_TYPE != ori) {
            CAMERA_TYPE = ori;
            camera.setPreviewCallback(null);//调用camera.release();前必须先加这行
            camera.release();
            camera = getBackCamera(CAMERA_TYPE);
            startcamera(camera);
        }
    }

    private void take_photo_Event() {
        LogUtil.e(TAG, "CameraFragment.onClick :  can_take --> " + can_take);
        if (!can_take) return;
        can_take = false;
        Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, final Camera camera) {
                // TODO Auto-generated method stub
                LogUtil.e(TAG, "CameraFragment.onPictureTaken :  data.length() --> " + data.length);
                LogUtil.e(TAG, "CameraFragment.onPictureTaken :  正在保存...  ");
                mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                File file = FileUtil.createFile(Macro.CACHE_FILE + System.currentTimeMillis() + ".png");
                BufferedOutputStream os = null;
                try {
                    os = new BufferedOutputStream(new FileOutputStream(file));
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LogUtil.e(TAG, "CameraFragment.onPictureTaken :  图像保存成功 --> ");
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (CAMERA_TYPE == 1)
                    bitmap = MyUtils.turnCurrentLayer(bitmap, -1, 1);
                EventBus.getDefault().post(new EventMessage(EventType.take_photo, bitmap));
                camera.startPreview();
            }
        };
        camera.takePicture(null, null, jpegCallback);
    }

    private void exitCamera() {
        if (null != camera) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        putYUVData(data, data.length);
    }

    public void putYUVData(byte[] buffer, int length) {
        if (YUVQueue.size() >= 5) {
            YUVQueue.poll();
        }
        YUVQueue.add(buffer);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtil.e(TAG, "CameraFragment.onHiddenChanged :  是否隐藏 --> " + hidden);
        cameraIsShowing = !hidden;
        if (hidden) {
            exitCamera();//隐藏
            mCameraView.setVisibility(View.GONE);
            mCameraView.setVisibility(View.INVISIBLE);
        } else {//显示
            mCameraView.setVisibility(View.VISIBLE);
            if (camera == null) camera = getBackCamera(CAMERA_TYPE);
            startcamera(camera);
        }
    }
}
