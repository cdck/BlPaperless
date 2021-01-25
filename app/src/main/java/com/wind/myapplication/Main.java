package com.wind.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.blankj.utilcode.util.ToastUtils;
import com.pa.paperless.helper.ScreenRecorder;
import com.pa.paperless.utils.LogUtil;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;



import com.pa.boling.paperless.R;


import java.io.File;

/**
 * @author Gowcage
 */
public class Main extends Activity implements OnClickListener {

    final String TAG = "Main-->";
    final int REQUEST_CODE = 1;// >=0

    int width, height, dpi, bitrate, VideoQuality = 1;//VideoQuality:1/2/3
    float density;

    MediaProjectionManager manager;
    MediaProjection projection;
    ScreenRecorder recorder;
    File file;

    Button recordbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_main);

        if (Build.VERSION.SDK_INT >= 23) {//API>6.0
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
//            for (String str : permissions) {
//                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
//                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
//                }
//            }
        }

        recordbtn = (Button) findViewById(R.id.recordbtn);
        recordbtn.setOnClickListener(this);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        width = metric.widthPixels; // �屏幕宽度（像素）
        height = metric.heightPixels; // �屏幕高度（像素）
        LogUtil.i(TAG, "w:" + width + "/h:" + height);
        density = metric.density; // �屏幕密度（0.75 / 1.0 / 1.5）
        dpi = metric.densityDpi; // �屏幕密度DPI（120 / 160 / 240）
        bitrate = width * height * VideoQuality;//�比特率/码率
        if (bitrate > 1600 * 1000) {
            bitrate = 1600 * 1000;
        }

        // 1: 拿到 MediaProjectionManager 实例
        manager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (recorder != null) {
            recorder.quit();
            recorder = null;
            recordbtn.setText("重新开始录屏");
        } else {
            // 2: 发起屏幕捕捉请求
            Intent intent = manager.createScreenCaptureIntent();
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    // 3:通过 onActivityResult 返回结果获取 MediaProjection 实例
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
            projection = manager.getMediaProjection(resultCode, data);

        if (projection == null) {
            LogUtil.e(TAG, "media projection is null");
            return;
        }

        file = new File(Environment.getExternalStorageDirectory(), "record_"
                + width + "x" + height + "_" + System.currentTimeMillis() + ".mp4");
        recorder = new ScreenRecorder(width, height, bitrate, 1, projection, file.getAbsolutePath());
        recorder.start();//启动录屏线程
        recordbtn.setText(getString(R.string.stop_recording));
        ToastUtils.showShort(R.string.tip_screen_recording);
        moveTaskToBack(true);//将程序移到后台
    }

    /**
     * 开始录制
     */
    public void startRecordScreen() {
        if (recorder == null) {
            // 2: 创建intent，并startActivityForResult
            Intent intent = manager.createScreenCaptureIntent();
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    /**
     * 获取数据
     */
    /*public ByteBuffer getScreenRawData() {
        ByteBuffer rawData = null;
        rawData = recorder.getByteBufferData();
        return rawData;
    }*/

    /**
     * 结束录制
     */
    public void stopRecordScreen() {
        if (recorder != null) {
            recorder.quit();
            recorder = null;
            recordbtn.setText("重新开始录屏");
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (recorder != null) {
            recorder.quit();
            recorder = null;
        }
    }

}
