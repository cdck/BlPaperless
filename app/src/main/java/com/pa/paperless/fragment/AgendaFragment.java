package com.pa.paperless.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceAgenda;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.ToastUtil;
import com.tencent.smtt.sdk.TbsDownloader;
import com.tencent.smtt.sdk.TbsReaderView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import androidx.annotation.Nullable;

import static com.pa.paperless.service.ShotApplication.applicationContext;
import static com.pa.paperless.service.ShotApplication.initX5Finished;

/**
 * Created by Administrator on 2017/10/31.
 * 公告议程
 */

public class AgendaFragment extends BaseFragment implements TbsReaderView.ReaderCallback {

    private final String TAG = "AgendaFragment-->";
    private TbsReaderView tbsReaderView;
    public static boolean needReStart = false;//是否需要重启；=true:是下载X5内核完成,但是加载失败
    private ProgressBar loading_bar;
    private TextView notice_text;
    private ScrollView scrollView;
    private LinearLayout test_tbsid;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.right_announce_agenda, container, false);
        initView(inflate);
        fun_queryAgenda();
        return inflate;
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {
        LogUtil.i(TAG, "onCallBackAction " + integer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tbsReaderView != null) {
            tbsReaderView.onStop();
            tbsReaderView = null;
        }
    }

    private void displayFile(String filepath) {
        scrollView.setVisibility(View.GONE);
        if (initX5Finished) {//加载完成
            if (needReStart) {//加载的是系统内核
                loading_bar.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                notice_text.setText(getString(R.string.init_x5_failure));
                return;
            }
        } else {//没有加载完成
            loading_bar.setVisibility(View.VISIBLE);
            TbsDownloader.startDownload(applicationContext);
            return;
        }
        /* **** **  加载完成，并且加载的是X5内核  ** **** */
        loading_bar.setVisibility(View.GONE);
        if (tbsReaderView == null) {
            System.out.println("tbsReaderView为null，进行创建");
            tbsReaderView = new TbsReaderView(getContext(), this);
        }
        test_tbsid.addView(tbsReaderView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filepath);
        bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());
        String suffix = filepath.substring(filepath.lastIndexOf(".") + 1);
        LogUtil.i(TAG, "打开文件 -->" + filepath + "， 后缀： " + suffix);
        try {
            boolean result = tbsReaderView.preOpen(suffix, false);
            loading_bar.setVisibility(View.GONE);
            if (result) {
                System.out.println("调用 tbsReaderView.openFile方法");
                tbsReaderView.openFile(bundle);
            } else {
                LogUtil.e(TAG, "displayFile 不支持打开该类型文件 -->");
                ToastUtil.showToast(R.string.not_supported);
            }
        } catch (Exception e) {
            LogUtil.i(TAG, "displayFile " + e.toString());
            e.printStackTrace();
        }
    }

    private void fun_queryAgenda() {
        notice_text.setText("");
        if (tbsReaderView != null) {
            test_tbsid.removeView(tbsReaderView);
            tbsReaderView.onStop();
            tbsReaderView = null;
        }
        try {
            InterfaceAgenda.pbui_meetAgenda pbui_meetAgenda = jni.queryAgenda();
            if (pbui_meetAgenda == null) return;
            int agendatype = pbui_meetAgenda.getAgendatype();
            if (agendatype == InterfaceMacro.Pb_AgendaType.Pb_MEET_AGENDA_TYPE_TEXT.getNumber()) {//文本
                String text = MyUtils.b2s(pbui_meetAgenda.getText());
                LogUtil.e(TAG, "fun_queryAgenda 获取到文本议程 --> " + text);
                loading_bar.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                notice_text.setText(text);
            } else if (agendatype == InterfaceMacro.Pb_AgendaType.Pb_MEET_AGENDA_TYPE_FILE.getNumber()) {//文件
                int mediaid = pbui_meetAgenda.getMediaid();
                byte[] bytes = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaid);
                InterfaceBase.pbui_CommonTextProperty textProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
                String fileName = textProperty.getPropertyval().toStringUtf8();
                LogUtil.i(TAG, "fun_queryAgenda 获取到文件议程 -->" + mediaid + ", 文件名：" + fileName);
                File file = new File(Macro.MEET_AGENDA + fileName);
                if (file.exists()) {
                    displayFile(file.getAbsolutePath());
                } else {
                    jni.creationFileDownload(Macro.MEET_AGENDA + fileName, mediaid, 1, 0, Macro.DOWNLOAD_AGENDA_FILE);
                }
            } else if (agendatype == InterfaceMacro.Pb_AgendaType.Pb_MEET_AGENDA_TYPE_TIME.getNumber()) {//时间轴式议程
                List<InterfaceAgenda.pbui_ItemAgendaTimeInfo> itemList = pbui_meetAgenda.getItemList();
                for (InterfaceAgenda.pbui_ItemAgendaTimeInfo item : itemList) {
                    int dirid = item.getDirid();
                    int agendaid = item.getAgendaid();
                    String content = item.getDesctext().toStringUtf8();
                    int status = item.getStatus();
                    LogUtil.i(TAG, "fun_queryAgenda 获取到时间轴式议程 -->" + dirid + ", content: " + content + ", status: " + status);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.init_x5_finished://腾讯X5内核初始化完成
            case EventType.AGENDA_CHANGE_INFO://议程变更通知
                LogUtil.e(TAG, "AgendaFragment.getEventMessage :  议程变更通知 --> ");
                fun_queryAgenda();
                break;
            case EventType.MEETING_AGENDA_FILE:
                String filepath = (String) message.getObject();
                displayFile(filepath);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            fun_queryAgenda();
        }
    }


    private void initView(View view) {
        loading_bar = (ProgressBar) view.findViewById(R.id.loading_bar);
        notice_text = (TextView) view.findViewById(R.id.notice_text);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        test_tbsid = (LinearLayout) view.findViewById(R.id.test_tbsid);
    }
}
