package com.pa.paperless.fragment;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceStop;
import com.mogujie.tt.protobuf.InterfaceVideo;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.MeetLiveVideoAdapter;
import com.pa.paperless.data.bean.VideoInfo;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.ui.CustomVideoView;
import com.pa.paperless.utils.LogUtil;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.pa.paperless.data.constant.EventType.DEV_REGISTER_INFORM;
import static com.pa.paperless.data.constant.EventType.STOP_STRAM_INFORM;

import static com.pa.paperless.utils.MyUtils.isHasPermission;


/**
 * @author Administrator
 * @date 2017/10/31
 * 视屏直播/摄像控制
 */
public class VideoFragment extends BaseFragment implements View.OnClickListener, CustomVideoView.ViewClickListener {

    private final String TAG = "VideoFragment-->";
    public static boolean admin_operate;
    private MeetLiveVideoAdapter adapter;
    private TextView meet_video_title;
    private RecyclerView meet_video_rv;
    private CheckBox meet_video_cb;
    private Button meet_video_watch_video;
    private Button meet_video_stop_watch;
    private Button meet_video_screen_video;
    private Button meet_video_stop_screen;
    private Button meet_video_start_projector;
    private Button meet_video_stop_projection;
    private CustomVideoView meet_video_custom;
    private int pvWidth;
    private int pvHeight;
    List<Integer> ids = new ArrayList<>();
    List<VideoInfo> videoInfos = new ArrayList<>();
    private List<InterfaceVideo.pbui_Item_MeetVideoDetailInfo> videoDetailInfos = new ArrayList<>();
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.right_video, container, false);
        initView(inflate);
        ids.add(1);
        ids.add(2);
        ids.add(3);
        ids.add(4);
        meet_video_custom.post(() -> {
            pvWidth = meet_video_custom.getWidth();
            pvHeight = meet_video_custom.getHeight();
            start();
        });
        return inflate;
    }

    private void start() {
        setVisible();
        EventBus.getDefault().register(this);
        jni.initVideoRes(1, pvWidth / 2, pvHeight / 2);
        jni.initVideoRes(2, pvWidth / 2, pvHeight / 2);
        jni.initVideoRes(3, pvWidth / 2, pvHeight / 2);
        jni.initVideoRes(4, pvWidth / 2, pvHeight / 2);
        meet_video_custom.createView(ids);
        queryMeetVedio();
    }

    private void stop() {
        for (Integer id : ids) {
            stopResource(id);
        }
        meet_video_custom.clearAll();
        jni.mediaDestroy(1);
        jni.mediaDestroy(2);
        jni.mediaDestroy(3);
        jni.mediaDestroy(4);
        EventBus.getDefault().unregister(this);
    }

    private void queryMeetVedio() {
        try {
            InterfaceVideo.pbui_Type_MeetVideoDetailInfo detailInfo = jni.queryMeetVedio();
            videoInfos.clear();
            if (detailInfo == null) {
                updateRv();
                return;
            }
            videoDetailInfos.clear();
            videoDetailInfos.addAll(detailInfo.getItemList());
            queryDevice();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryDevice() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDeviceInfo();
            videoInfos.clear();
            if (deviceDetailInfo == null) {
                updateRv();
                return;
            }
            deviceInfos.clear();
            deviceInfos.addAll(deviceDetailInfo.getPdevList());
            for (int i = 0; i < videoDetailInfos.size(); i++) {
                InterfaceVideo.pbui_Item_MeetVideoDetailInfo video = videoDetailInfos.get(i);
                int deviceid = video.getDeviceid();
                for (int j = 0; j < deviceInfos.size(); j++) {
                    InterfaceDevice.pbui_Item_DeviceDetailInfo dev = deviceInfos.get(j);
                    if (dev.getDevcieid() == deviceid) {
                        videoInfos.add(new VideoInfo(video, dev));
                    }
                }
            }
            updateRv();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void updateRv() {
        if (adapter == null) {
            adapter = new MeetLiveVideoAdapter(R.layout.item_meet_video, videoInfos);
            meet_video_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            meet_video_rv.setAdapter(adapter);
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter ad, View view, int position) {
                    if (videoInfos.get(position).getDeviceDetailInfo().getNetstate() == 1) {
                        InterfaceVideo.pbui_Item_MeetVideoDetailInfo videoDetailInfo = videoInfos.get(position).getVideoInfo();
                        LogUtil.d(TAG, "onItemClick --> Subid= " + videoDetailInfo.getSubid());
                        adapter.setSelected(videoDetailInfo.getDeviceid(), videoDetailInfo.getId());
                    }
                }
            });
        } else {
            adapter.notifyDataSetChanged();
            adapter.notifySelect();
        }
    }

    private void setVisible() {
        meet_video_screen_video.setVisibility(admin_operate ? View.VISIBLE : View.GONE);
        meet_video_stop_screen.setVisibility(admin_operate ? View.VISIBLE : View.GONE);
        meet_video_start_projector.setVisibility(admin_operate ? View.VISIBLE : View.GONE);
        meet_video_stop_projection.setVisibility(admin_operate ? View.VISIBLE : View.GONE);
        meet_video_title.setText(admin_operate ? getString(R.string.video_preview) : getString(R.string.video_live));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.Meet_vedio_changeInform://会议视频变更通知
                queryMeetVedio();
                break;
            case DEV_REGISTER_INFORM://设备寄存器变更通知
                queryDevice();
                break;
            case EventType.CALLBACK_YUVDISPLAY:
                Object[] objects = message.getObjects();
                meet_video_custom.setYuv(objects);
                break;
            case EventType.CALLBACK_VIDEO_DECODE:
                Object[] objects1 = message.getObjects();
                meet_video_custom.setVideoDecode(objects1);
                break;
            case STOP_STRAM_INFORM://停止资源通知
                InterfaceStop.pbui_Type_MeetStopResWork stopResWork = (InterfaceStop.pbui_Type_MeetStopResWork) message.getObject();
                List<Integer> resList = stopResWork.getResList();
                for (int resid : resList) {
                    LogUtil.i(TAG, "getEventMessage -->" + "停止资源通知 resid: " + resid);
                    meet_video_custom.stopResWork(resid);
                }
                break;
            case EventType.STOP_PLAY://停止播放通知
                InterfaceStop.pbui_Type_MeetStopPlay stopPlay = (InterfaceStop.pbui_Type_MeetStopPlay) message.getObject();
                int resid = stopPlay.getRes();
                LogUtil.i(TAG, "getEventMessage -->" + "停止播放通知 resid: " + resid);
                meet_video_custom.stopResWork(resid);
                break;
        }
    }

    private void screen_oper(boolean b) {
        VideoInfo info = adapter.getSelected();
        if (info != null) {
            EventBus.getDefault().post(new EventMessage(EventType.OPEN_SCREENSPOP, info, b));
//            EventBus.getDefault().post(new EventMessageNew.Builder().type(EventType.OPEN_SCREENSPOP).objs(info,b).build());
        }
    }

    private void project_oper(boolean b) {
        VideoInfo info = adapter.getSelected();
        if (info != null) {
            EventBus.getDefault().post(new EventMessage(EventType.OPEN_PROJECTOR, info, b));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            stop();
        } else {
            start();
        }
    }

    private void initView(View inflate) {
        meet_video_title = (TextView) inflate.findViewById(R.id.meet_video_title);
        meet_video_rv = (RecyclerView) inflate.findViewById(R.id.meet_video_rv);
        meet_video_cb = (CheckBox) inflate.findViewById(R.id.meet_video_cb);
        meet_video_stop_watch = (Button) inflate.findViewById(R.id.meet_video_stop_watch);
        meet_video_watch_video = (Button) inflate.findViewById(R.id.meet_video_watch_video);
        meet_video_screen_video = (Button) inflate.findViewById(R.id.meet_video_screen_video);
        meet_video_stop_screen = (Button) inflate.findViewById(R.id.meet_video_stop_screen);
        meet_video_start_projector = (Button) inflate.findViewById(R.id.meet_video_start_projector);
        meet_video_stop_projection = (Button) inflate.findViewById(R.id.meet_video_stop_projection);
        meet_video_custom = (CustomVideoView) inflate.findViewById(R.id.meet_video_custom);

        meet_video_stop_watch.setOnClickListener(this);
        meet_video_watch_video.setOnClickListener(this);
        meet_video_screen_video.setOnClickListener(this);
        meet_video_stop_screen.setOnClickListener(this);
        meet_video_start_projector.setOnClickListener(this);
        meet_video_stop_projection.setOnClickListener(this);
        meet_video_custom.setViewClickListener(this);
    }

    public void stopResource(int resId) {
        List<Integer> ids = new ArrayList<>();
        List<Integer> res = new ArrayList<>();
        res.add(resId);
        ids.add(Values.localDevId);
        jni.stopResourceOperate(res, ids);
    }

    public void watch(VideoInfo videoInfo, int resId) {
        InterfaceVideo.pbui_Item_MeetVideoDetailInfo videoDetailInfo = videoInfo.getVideoInfo();
        int deviceid = videoDetailInfo.getDeviceid();
        int subid = videoDetailInfo.getSubid();
        List<Integer> res = new ArrayList<>();
        res.add(resId);
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(Values.localDevId);
        jni.streamPlay(deviceid, subid, 0, res, ids);
    }

    @Override
    public void onClick(View v) {
        if (adapter == null) return;
        switch (v.getId()) {
            case R.id.meet_video_watch_video:
                VideoInfo videoInfo = adapter.getSelected();
                if (videoInfo != null) {
                    int selectResId = meet_video_custom.getSelectResId();
                    if (selectResId != -1) {
                        stopResource(selectResId);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                watch(videoInfo, selectResId);
                            }
                        }, 500);
                    } else {
                        ToastUtils.showShort(R.string.please_choose_view);
                    }
                } else {
                    ToastUtils.showShort(R.string.please_choose_video_show);
                }
                break;
            case R.id.meet_video_stop_watch:
                int selectResId = meet_video_custom.getSelectResId();
                if (selectResId != -1) {
                    stopResource(selectResId);
                } else {
                    ToastUtils.showShort( R.string.please_choose_stop_view);
                }
                break;
            case R.id.meet_video_screen_video:
                if (isHasPermission(Macro.permission_code_screen)) {
                    screen_oper(true);
                } else {
                    ToastUtils.showShort( R.string.no_permission);
                }
                break;
            case R.id.meet_video_stop_screen:
                if (isHasPermission(Macro.permission_code_screen)) {
                    screen_oper(false);
                } else {
                    ToastUtils.showShort( R.string.no_permission);
                }
                break;
            case R.id.meet_video_start_projector:
                if (isHasPermission(Macro.permission_code_projection)) {
                    project_oper(true);
                } else {
                    ToastUtils.showShort( R.string.no_permission);
                }
                break;
            case R.id.meet_video_stop_projection:
                if (isHasPermission(Macro.permission_code_projection)) {
                    project_oper(false);
                } else {
                    ToastUtils.showShort( R.string.no_permission);
                }
                break;
        }
    }

    long oneTime, twoTime, threeTime, fourTime;

    @Override
    public void click(int res) {
        switch (res) {
            case 1:
                meet_video_custom.setSelectResId(res);
                if (System.currentTimeMillis() - oneTime < 500) {
                    meet_video_custom.zoom(res);
                } else {
                    oneTime = System.currentTimeMillis();
                }
                break;
            case 2:
                meet_video_custom.setSelectResId(res);
                if (System.currentTimeMillis() - twoTime < 500) {
                    meet_video_custom.zoom(res);
                } else {
                    twoTime = System.currentTimeMillis();
                }
                break;
            case 3:
                meet_video_custom.setSelectResId(res);
                if (System.currentTimeMillis() - threeTime < 500) {
                    meet_video_custom.zoom(res);
                } else {
                    threeTime = System.currentTimeMillis();
                }
                break;
            case 4:
                meet_video_custom.setSelectResId(res);
                if (System.currentTimeMillis() - fourTime < 500) {
                    meet_video_custom.zoom(res);
                } else {
                    fourTime = System.currentTimeMillis();
                }
                break;
        }
    }

}
