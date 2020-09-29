package com.pa.paperless.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.utils.LogUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.MeetingFileTypeAdapter;
import com.pa.paperless.adapter.TypeFileAdapter;
import com.pa.paperless.data.bean.MeetDirFileInfo;
import com.pa.paperless.data.bean.MeetingFileTypeBean;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.Dispose;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.pa.paperless.data.constant.Macro.MEET_MATERIAL;


/**
 * @author Administrator
 * @date 2017/10/31
 * 会议资料
 * 进入页面：1.查询文件目录 2.根据目录ID查找文件 3.先展示出第一个item 的文档类的数据
 */

public class MeetingFileFragment extends BaseFragment implements View.OnClickListener {

    private ListView dir_lv;
    private ListView rightmeetfile_lv;
    private Button rightmeetfile_prepage, rightmeetfile_nextpage, download, push_file,
            rightmeetfile_other, rightmeetfile_video, rightmeetfile_picture, rightmeetfile_document, meet_saved_offline_btn;
    private MeetingFileTypeAdapter mDirAdapter;
    private List<Button> mBtns;
    //  存放目录ID、目录名称
    private List<MeetingFileTypeBean> mDirData = new ArrayList<>();
    //  存放会议目录文件的详细信息
    private List<MeetDirFileInfo> meetDirFileInfos;
    //  用来临时存放不同类型的文件并展示
    private List<MeetDirFileInfo> mFileData = new ArrayList<>();
    private TypeFileAdapter dataAdapter;
    //播放时的媒体ID
    public static int mMediaid;
    private TextView tv_count_num;
    private int page_count;
    private int clickPosion;
    private int clickDirId = -1;//保存点击的目录ID
    private final String TAG = "MeetingFileFragment-->";
    public static Map<Integer, Integer> saveKey = new HashMap<>();
    private boolean isFirst;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.right_mettingfile, container, false);
        isFirst = true;
        initView(inflate);
        initBtns();
        fun_queryMeetDir();
        return inflate;
    }

    private void fun_queryMeetDir() {
        try {
            //136.查询会议目录
            InterfaceFile.pbui_Type_MeetDirDetailInfo dirInfo = jni.queryMeetDir();
            if (dirInfo == null) {
                //查询会议目录失败
                if (mDirData != null) mDirData.clear();
                if (mDirAdapter != null) mDirAdapter.notifyDataSetChanged();
                clear();
                return;
            }
            if (mDirData == null) mDirData = new ArrayList<>();
            else mDirData.clear();
            List<InterfaceFile.pbui_Item_MeetDirDetailInfo> itemList = dirInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceFile.pbui_Item_MeetDirDetailInfo info = itemList.get(i);
                int dirId = info.getId();
                LogUtil.d(TAG, "MeetingFileFragment.fun_queryMeetDir : 目录ID：" + dirId + ",父类ID=" + info.getParentid() + ",  会议资料目录名 --> " + info.getName().toStringUtf8());
                if (info.getParentid() != 0) {
                    continue;
                }
                String dirName = MyUtils.b2s(info.getName());
                if(dirId!=Macro.SHARED_FILE_DIRECTORY_ID && dirId!=Macro.ANNOTATION_FILE_DIRECTORY_ID){
//                if (!dirName.equals(getString(R.string.fun_text_share_file)) && !dirName.equals(getString(R.string.fun_text_postil_file))) {
                    LogUtil.d(TAG, "MeetingFileFragment.fun_queryMeetDir : 目录ID：" + dirId + ",  会议资料目录名 --> " + dirName);
                    mDirData.add(new MeetingFileTypeBean(dirId, info.getParentid(), dirName, info.getFilenum()));
                    if (!saveKey.containsKey(dirId)) {//确保是首次查询目录
                        saveKey.put(dirId, -1);//给每个目录ID 设置好默认显示的文件类别
                    }
                }
            }
            if (mDirAdapter == null) {
                mDirAdapter = new MeetingFileTypeAdapter(getActivity(), mDirData);
                dir_lv.setAdapter(mDirAdapter);
            } else mDirAdapter.notifyDataSetChanged();
            dir_lv.setOnItemClickListener((adapterView, view, i, l) -> {
                LogUtil.e(TAG, "MeetingFileFragment.initData:  点击了第 " + i + " 项目录");
                clickPosion = i;
                mDirAdapter.setCheck(i);//设置item选中效果
                //获取当前选中的目录ID
                MeetingFileTypeBean meetingFileTypeBean = mDirData.get(i);
                clickDirId = meetingFileTypeBean.getDirId();
                /** **** **  已经有了目录不一定就有文件数据，所以要判空处理  ** **** **/
                if (meetDirFileInfos != null) meetDirFileInfos.clear();//将之前的目录文件清空
                if (mFileData != null) mFileData.clear();//清空某一类型的文件
                if (dataAdapter != null) dataAdapter.notifyDataSetChanged();
                LogUtil.e(TAG, "MeetingFileFragment.onItemClick:  从item中传递过去的目录ID --->>> " + clickDirId);
                //每次点击的时候，就设置当前目录显示全部
                saveKey.put(clickDirId, -1);
                if (dataAdapter != null) dataAdapter.PAGE_NOW = 0;
                fun_queryMeetDirFile(clickDirId);
            });
            //当第一次进入会议资料界面时，就展示第一个目录item中的文档类信息
            if (mDirData.size() > 0) {
                if (clickDirId == -1) {
                    clickDirId = mDirData.get(0).getDirId();
                    saveKey.put(clickDirId, -1);
                    mDirAdapter.setCheck(0);
                } else {
                    mDirAdapter.setCheck(clickPosion);
                }
                fun_queryMeetDirFile(clickDirId);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fun_queryMeetDirFile(int dirId) {
        try {
            //查询会议目录文件
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo object = jni.queryMeetDirFile(dirId);
            if (object == null) {
                tv_count_num.setText("0 / 0");
                clear();
                return;
            }
            if (meetDirFileInfos == null) meetDirFileInfos = new ArrayList<>();
            else meetDirFileInfos.clear();
            meetDirFileInfos.addAll(Dispose.MeetDirFile(object));
            if (!meetDirFileInfos.isEmpty()) {
                checkBtn(true);//有数据就设置类别控件可以点击
                mFileData.clear();
                int clickTypeId = saveKey.get(clickDirId);
                boolean isShowAll = clickTypeId == -1;
                for (int i = 0; i < meetDirFileInfos.size(); i++) {
                    MeetDirFileInfo meetDirFileInfo = meetDirFileInfos.get(i);
                    String fileName = meetDirFileInfo.getFileName();
                    if (isShowAll) {
                        mFileData.add(meetDirFileInfo);
                    } else {
                        boolean documentFile = false;
                        switch (clickTypeId) {
                            case 0:
                                documentFile = FileUtil.isDocumentFile(fileName);
                                break;
                            case 1:
                                documentFile = FileUtil.isPictureFile(fileName);
                                break;
                            case 2:
                                documentFile = FileUtil.isVideoFile(fileName);
                                break;
                            case 3:
                                documentFile = FileUtil.isOtherFile(fileName);
                                break;
                        }
                        if (documentFile) {
                            mFileData.add(meetDirFileInfo);
                        }
                    }
                }
                if (dataAdapter == null) {
                    dataAdapter = new TypeFileAdapter(getContext(), mFileData);
                    rightmeetfile_lv.setAdapter(dataAdapter);
                }
                dataAdapter.notifyChecks();
                dataAdapter.notifyDataSetChanged();
                if (!isFirst) {
                    int pagecount;
                    if (mFileData.size() % dataAdapter.ITEM_COUNT == 0) {
                        pagecount = mFileData.size() / dataAdapter.ITEM_COUNT;
                    } else {
                        pagecount = mFileData.size() / dataAdapter.ITEM_COUNT + 1;
                    }
                    if (dataAdapter.PAGE_NOW >= pagecount) {
                        dataAdapter.PAGE_NOW = pagecount;
                    }
                }
                updataPageTv();
                checkButton();
                setBtnSelect(clickTypeId);
                //打开文件
                dataAdapter.setLookListener((fileInfo, mediaId, filename, filesize) -> {
                    mMediaid = mediaId;
                    if (FileUtil.isVideoFile(filename)) {
                        //媒体播放操作
                        List<Integer> devIds = new ArrayList<>();
                        devIds.add(Values.localDevId);
                        jni.mediaPlayOperate(mediaId, devIds, 0, 0, 0, InterfaceMacro.Pb_MeetPlayFlag.Pb_MEDIA_PLAYFLAG_ZERO.getNumber());
                    } else {
                        FileUtil.openFile(Macro.CACHE_ALL_FILE, filename, jni, mediaId, getContext(), filesize);
                    }
                });
                //item选中事件
                dataAdapter.setItemSelectListener((posion, view) -> {
                    dataAdapter.setCheck(mFileData.get(posion).getMediaId());
                });
            } else {
                clear();
                checkBtn(false);//没有数据就设置控件不可点击
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.MEETDIR_CHANGE_INFORM://135 会议目录变更通知
                fun_queryMeetDir();
                break;
            case EventType.MEETDIR_FILE_CHANGE_INFORM://142 会议目录文件变更通知
                InterfaceBase.pbui_MeetNotifyMsgForDouble object1 = (InterfaceBase.pbui_MeetNotifyMsgForDouble) message.getObject();
                int id = object1.getId();
                if (id != Macro.SHARED_FILE_DIRECTORY_ID && id != Macro.ANNOTATION_FILE_DIRECTORY_ID) {//过滤去除批注和共享资料
                    //文件变更,相对的目录中的文件数量也需要更新
                    fun_queryMeetDir();
                }
                break;
        }
    }

    private void checkBtn(boolean b) {
        rightmeetfile_document.setClickable(b);
        rightmeetfile_picture.setClickable(b);
        rightmeetfile_video.setClickable(b);
        rightmeetfile_other.setClickable(b);
    }

    private void clear() {
        if (meetDirFileInfos != null) meetDirFileInfos.clear();
        if (mFileData != null) mFileData.clear();
        if (dataAdapter != null) dataAdapter.notifyDataSetChanged();
        clickDirId = -1;
    }

    private void initView(View inflate) {
        dir_lv = inflate.findViewById(R.id.type_lv);
        rightmeetfile_document = inflate.findViewById(R.id.rightmeetfile_document);
        rightmeetfile_picture = inflate.findViewById(R.id.rightmeetfile_picture);
        rightmeetfile_video = inflate.findViewById(R.id.rightmeetfile_video);
        rightmeetfile_other = inflate.findViewById(R.id.rightmeetfile_other);
        rightmeetfile_lv = inflate.findViewById(R.id.rightmeetfile_lv);
        rightmeetfile_prepage = inflate.findViewById(R.id.rightmeetfile_prepage);
        rightmeetfile_nextpage = inflate.findViewById(R.id.rightmeetfile_nextpage);
        push_file = inflate.findViewById(R.id.push_file);
        download = inflate.findViewById(R.id.download);
        tv_count_num = inflate.findViewById(R.id.tv_count_num);
        meet_saved_offline_btn = inflate.findViewById(R.id.meet_saved_offline_btn);
        meet_saved_offline_btn.setOnClickListener(this);
        rightmeetfile_document.setOnClickListener(this);
        rightmeetfile_picture.setOnClickListener(this);
        rightmeetfile_video.setOnClickListener(this);
        rightmeetfile_other.setOnClickListener(this);
        rightmeetfile_prepage.setOnClickListener(this);
        rightmeetfile_nextpage.setOnClickListener(this);
        push_file.setOnClickListener(this);
        download.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (dataAdapter == null) {
            LogUtil.e(TAG, "MeetingFileFragment.onClick :  adapter 为 null --> ");
            return;
        }
        switch (v.getId()) {
            case R.id.rightmeetfile_document:
                if (meetDirFileInfos != null) {
                    mFileData.clear();
                    for (int i = 0; i < meetDirFileInfos.size(); i++) {
                        MeetDirFileInfo documentBean = meetDirFileInfos.get(i);
                        String fileName = documentBean.getFileName();
                        boolean documentFile = FileUtil.isDocumentFile(fileName);
                        if (documentFile) {
                            LogUtil.e(TAG, "MeetingFileFragment.onClick:  其中的文档类文件： --->>> " + fileName);
                            mFileData.add(documentBean);
                        }
                    }
                }
                if (dataAdapter != null) {
                    dataAdapter.notifyDataSetChanged();
                    dataAdapter.PAGE_NOW = 0;
                }
                updataPageTv();
                checkButton();
                setBtnSelect(0);
                break;
            case R.id.rightmeetfile_picture:
                if (meetDirFileInfos != null) {
                    mFileData.clear();
                    for (int i = 0; i < meetDirFileInfos.size(); i++) {
                        MeetDirFileInfo documentBean = meetDirFileInfos.get(i);
                        String fileName = documentBean.getFileName();
                        if (FileUtil.isPictureFile(fileName)) {
                            LogUtil.e(TAG, "MeetingFileFragment.onClick:  其中的图片类文件 --->>> " + fileName);
                            mFileData.add(documentBean);
                        }
                    }
                }
                dataAdapter.notifyDataSetChanged();
                dataAdapter.PAGE_NOW = 0;
                updataPageTv();
                checkButton();
                setBtnSelect(1);
                break;
            case R.id.rightmeetfile_video:
                if (meetDirFileInfos != null) {
                    mFileData.clear();
                    for (int i = 0; i < meetDirFileInfos.size(); i++) {
                        MeetDirFileInfo documentBean = meetDirFileInfos.get(i);
                        String fileName = documentBean.getFileName();
                        //过滤视频文件
                        if (FileUtil.isVideoFile(fileName)) {
                            LogUtil.e(TAG, "MeetingFileFragment.onClick:  其中的视频类文件 --->>> " + fileName);
                            mFileData.add(documentBean);
                        }
                    }
                }
                dataAdapter.notifyDataSetChanged();
                dataAdapter.PAGE_NOW = 0;
                updataPageTv();
                checkButton();
                setBtnSelect(2);
                break;
            case R.id.rightmeetfile_other:
                if (meetDirFileInfos != null) {
                    mFileData.clear();
                    for (int i = 0; i < meetDirFileInfos.size(); i++) {
                        MeetDirFileInfo documentBean = meetDirFileInfos.get(i);
                        String fileName = documentBean.getFileName();
                        //过滤视频文件
                        if (FileUtil.isOtherFile(fileName)) {
                            LogUtil.e(TAG, "MeetingFileFragment.onClick:  其它类文件 --->>> " + fileName);
                            mFileData.add(documentBean);
                        }
                    }
                }
                dataAdapter.notifyDataSetChanged();
                dataAdapter.PAGE_NOW = 0;
                updataPageTv();
                checkButton();
                setBtnSelect(3);
                break;
            case R.id.rightmeetfile_prepage://上一页
                prePage();
                break;
            case R.id.rightmeetfile_nextpage://下一页
                nextPage();
                break;
            case R.id.meet_saved_offline_btn://离线缓存
                if (dataAdapter == null) break;
                MeetDirFileInfo data = dataAdapter.getCheckedFile();
                if (data == null) {
                    ToastUtil.showToast(R.string.please_choose_downloadfile);
                    break;
                }
                FileUtil.downOfflineFile(data);
                break;
            case R.id.download://下载文件
                if (dataAdapter == null) break;
                MeetDirFileInfo data1 = dataAdapter.getCheckedFile();
                if (data1 == null) {
                    ToastUtil.showToast(R.string.please_choose_downloadfile);
                    break;
                }
                FileUtil.downFile(data1, MEET_MATERIAL);
                break;
            case R.id.push_file://文件推送
                if (dataAdapter == null) break;
                MeetDirFileInfo data2 = dataAdapter.getCheckedFile();
                if (data2 == null) {
                    ToastUtil.showToast(R.string.please_choose_file);
                    break;
                }
                EventBus.getDefault().post(new EventMessage(EventType.INFORM_PUSH_FILE, data2));
                break;
        }
    }

    @NonNull
    private List<MeetDirFileInfo> getSelectInfo(List<Integer> mediaIds) {
        List<MeetDirFileInfo> data = new ArrayList<>();
        for (int i = 0; i < mediaIds.size(); i++) {
            Integer mediaId = mediaIds.get(i);
            for (int j = 0; j < meetDirFileInfos.size(); j++) {
                MeetDirFileInfo meetDirFileInfo = meetDirFileInfos.get(j);
                if (meetDirFileInfo.getMediaId() == mediaId) {
                    data.add(meetDirFileInfo);
                }
            }
        }
        return data;
    }

    private void initBtns() {
        mBtns = new ArrayList<>();
        mBtns.add(rightmeetfile_document);
        mBtns.add(rightmeetfile_picture);
        mBtns.add(rightmeetfile_video);
        mBtns.add(rightmeetfile_other);
    }

    private void setBtnSelect(int index) {
        saveKey.put(clickDirId, index);
        for (int i = 0; i < mBtns.size(); i++) {
            mBtns.get(i).setSelected(i == index);
        }
    }

    //下一页
    private void nextPage() {
        if (dataAdapter == null) return;
        dataAdapter.PAGE_NOW++;
        dataAdapter.notifyDataSetChanged();
        updataPageTv();
        checkButton();
    }

    //上一页
    private void prePage() {
        if (dataAdapter == null) return;
        dataAdapter.PAGE_NOW--;
        dataAdapter.notifyDataSetChanged();
        updataPageTv();
        checkButton();
    }

    private void updataPageTv() {
        if (mFileData == null || dataAdapter == null) return;
        int count = mFileData.size();
        LogUtil.e(TAG, "MeetingFileFragment.updataPageTv :   --> mFileData.size()： " + count);
        if (count != 0) {
            if (count % dataAdapter.ITEM_COUNT == 0) {
                page_count = mFileData.size() / dataAdapter.ITEM_COUNT;
            } else {
                page_count = (mFileData.size() / dataAdapter.ITEM_COUNT) + 1;
            }
            tv_count_num.setText(dataAdapter.PAGE_NOW + 1 + " / " + page_count);
        } else {
            tv_count_num.setText(dataAdapter.PAGE_NOW + " / " + count);
        }
    }

    //设置两个按钮是否可用
    public void checkButton() {
        if (dataAdapter == null || mFileData == null) return;
        //如果页码已经是第一页了
        if (dataAdapter.PAGE_NOW == 0) {
            rightmeetfile_prepage.setEnabled(false);
            //如果不设置的话，只要进入一次else if ，那么下一页按钮就一直是false，不可点击状态
            if (mFileData.size() > dataAdapter.ITEM_COUNT) {
                rightmeetfile_nextpage.setEnabled(true);
            } else {
                rightmeetfile_nextpage.setEnabled(false);
            }
        }
        //值的长度减去前几页的长度，剩下的就是这一页的长度，如果这一页的长度比View_Count小，表示这是最后的一页了，后面在没有了。
        else if (mFileData.size() - (dataAdapter.PAGE_NOW) * dataAdapter.ITEM_COUNT <= dataAdapter.ITEM_COUNT) {
            rightmeetfile_nextpage.setEnabled(false);
            rightmeetfile_prepage.setEnabled(true);
        } else {
            //否则两个按钮都设为可用
            rightmeetfile_prepage.setEnabled(true);
            rightmeetfile_nextpage.setEnabled(true);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        isFirst = false;
        /** **** **  如果是隐藏状态，则表示不在显示  ** **** **/
        LogUtil.e(TAG, "MeetingFileFragment.onHiddenChanged :   --> 是否隐藏：" + hidden);
        if (!hidden) {
            fun_queryMeetDir();
        } else {
        }/* else {
            if (dataAdapter != null) page_now = dataAdapter.PAGE_NOW;
        }*/
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

}
