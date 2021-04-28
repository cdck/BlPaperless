package com.pa.paperless.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.node.FileNodeAdapter;
import com.pa.paperless.adapter.node.LevelDirNode;
import com.pa.paperless.adapter.node.LevelFileNode;
import com.pa.paperless.adapter.rvadapter.DownloadFileAdapter;
import com.pa.paperless.data.bean.MeetDirFileInfo;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.PopUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * @author Administrator
 * @date 2017/10/31
 * 会议资料
 * 进入页面：1.查询文件目录 2.根据目录ID查找文件 3.先展示出第一个item 的文档类的数据
 */

public class MeetingFileFragment extends BaseFragment implements View.OnClickListener {

    private List<Button> mBtns;
    private final String TAG = "MeetingFileFragment-->";

    private RecyclerView rv_file;
    private Button btn_document, btn_picture, btn_video, btn_other, btn_push, btn_download;
    private LinearLayout right_meetingfilelayout;
    private FileNodeAdapter fileNodeAdapter;
    List<BaseNode> allData = new ArrayList<>();
    List<BaseNode> showFiles = new ArrayList<>();
    /**
     * 存放目录的展开和收缩状态
     */
    Map<Integer, Boolean> isExpandedMap = new HashMap<>();
    Map<Integer, Boolean> isSelectedMap = new HashMap<>();
    /**
     * =0文档，=1图片，=2视频，=3其它，=-1全部
     */
    private int fileFilterType = -1;
    private DownloadFileAdapter downloadFileAdapter;
    private PopupWindow downloadPop;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.right_mettingfile, container, false);
        initView(inflate);
        EventBus.getDefault().register(this);
        initButtons();
        //会议目录权限
//        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYRIGHT.getNumber(), 1, InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO_VALUE);
        queryMeetDir();
        return inflate;
    }

    private void initView(View inflate) {
        right_meetingfilelayout = inflate.findViewById(R.id.right_meetingfilelayout);
        btn_document = inflate.findViewById(R.id.btn_document);
        btn_picture = inflate.findViewById(R.id.btn_picture);
        btn_video = inflate.findViewById(R.id.btn_video);
        btn_other = inflate.findViewById(R.id.btn_other);
        btn_document.setOnClickListener(this);
        btn_picture.setOnClickListener(this);
        btn_video.setOnClickListener(this);
        btn_other.setOnClickListener(this);

        btn_push = inflate.findViewById(R.id.btn_push);
        btn_download = inflate.findViewById(R.id.btn_download);
        btn_push.setOnClickListener(this);
        btn_download.setOnClickListener(this);

        rv_file = inflate.findViewById(R.id.rv_file);
    }

    private void initButtons() {
        mBtns = new ArrayList<>();
        mBtns.add(btn_document);
        mBtns.add(btn_picture);
        mBtns.add(btn_video);
        mBtns.add(btn_other);
    }

    /**
     * 保存当前所有的目录的展开状态
     */
    private void saveCurrentExpandStatus() {
        isExpandedMap.clear();
//        isSelectedMap.clear();
        for (int i = 0; i < allData.size(); i++) {
            BaseNode baseNode = allData.get(i);
            if (baseNode instanceof LevelDirNode) {
                LevelDirNode dirNode = (LevelDirNode) baseNode;
                int dirId = dirNode.getDirId();
                isExpandedMap.put(dirId, dirNode.isExpanded());
//                List<BaseNode> childNode = dirNode.getChildNode();
//                if (childNode != null) {
//                    for (int j = 0; j < childNode.size(); j++) {
//                        LevelFileNode node = (LevelFileNode) childNode.get(j);
//                        isSelectedMap.put(node.getMediaId(), node.isSelected());
//                    }
//                }
            }
        }
    }

    /**
     * 获取之前的目录是否是展开状态
     *
     * @param dirId 目录id
     */
    private boolean beforeIsExpanded(int dirId) {
        if (isExpandedMap.containsKey(dirId)) {
            return isExpandedMap.get(dirId);
        }
        return false;
    }

    private boolean beforeIsSelected(int mediaId) {
        if (isSelectedMap.containsKey(mediaId)) {
            return isSelectedMap.get(mediaId);
        }
        return false;
    }

    private void queryMeetDir() {
        try {
            InterfaceFile.pbui_Type_MeetDirDetailInfo dirInfo = jni.queryMeetDir();
            saveCurrentExpandStatus();
            allData.clear();
            if (dirInfo != null) {
                List<InterfaceFile.pbui_Item_MeetDirDetailInfo> itemList = dirInfo.getItemList();
                for (int i = 0; i < itemList.size(); i++) {
                    InterfaceFile.pbui_Item_MeetDirDetailInfo info = itemList.get(i);
                    int dirId = info.getId();
                    String dirName = info.getName().toStringUtf8();
                    LogUtils.i(TAG, "当前目录=" + dirName);
                    if (info.getParentid() != 0) {
                        LogUtils.e(TAG, "过滤掉有父目录的目录" + dirName);
                        continue;
                    }
                    if (dirId != Macro.SHARED_FILE_DIRECTORY_ID && dirId != Macro.ANNOTATION_FILE_DIRECTORY_ID) {
                        InterfaceFile.pbui_Type_MeetDirRightDetailInfo dirPermission = jni.queryDirPermission(dirId);
                        if (dirPermission != null) {
                            List<Integer> memberidList = dirPermission.getMemberidList();
                            if (memberidList != null && memberidList.contains(Values.localMemberId)) {
                                LogUtils.d("没有目录权限=" + dirName);
                                continue;
                            }
                        }
                        LevelDirNode levelDirItem = new LevelDirNode(new ArrayList<>(), dirId, dirName);
                        levelDirItem.setExpanded(beforeIsExpanded(dirId));
                        LogUtils.i("添加目录：" + dirName + "，id=" + dirId);
                        allData.add(levelDirItem);
                        queryMeetDirFile(dirId);
                    }
                }
            }
            showFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFiles() {
        LogUtils.i(TAG, "showFiles fileFilterType=" + fileFilterType);
        showFiles.clear();
        if (fileFilterType == -1) {
            showFiles.addAll(allData);
        } else {
            for (int i = 0; i < allData.size(); i++) {
                BaseNode node = allData.get(i);
                if (node instanceof LevelDirNode) {
                    LevelDirNode dirNode = (LevelDirNode) node;
                    int dirId = dirNode.getDirId();
                    String dirName = dirNode.getDirName();
                    List<BaseNode> childNode = dirNode.getChildNode();

                    List<BaseNode> newFileNodes = new ArrayList<>();
                    if (childNode != null) {
                        for (int j = 0; j < childNode.size(); j++) {
                            LevelFileNode fileNode = (LevelFileNode) childNode.get(j);
                            String name = fileNode.getName();
                            switch (fileFilterType) {
                                case 0:
                                    if (FileUtil.isDocument(name)) {
                                        newFileNodes.add(fileNode);
                                    }
                                    break;
                                case 1:
                                    if (FileUtil.isPicture(name)) {
                                        newFileNodes.add(fileNode);
                                    }
                                    break;
                                case 2:
                                    if (FileUtil.isVideo(name)) {
                                        newFileNodes.add(fileNode);
                                    }
                                    break;
                                case 3:
                                    if (FileUtil.isOtherFile(name)) {
                                        newFileNodes.add(fileNode);
                                    }
                                    break;
                            }
                        }
                    }
                    boolean isExpanded = beforeIsExpanded(dirId);
                    LevelDirNode newDirNode = new LevelDirNode(newFileNodes, dirId, dirName);
                    newDirNode.setExpanded(isExpanded);
                    showFiles.add(newDirNode);
                }
            }
        }
        LogUtils.d(TAG, "showFiles 刷新或创建adapter");
        if (fileNodeAdapter == null) {
            fileNodeAdapter = new FileNodeAdapter(showFiles);
            rv_file.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_file.setAdapter(fileNodeAdapter);
        } else {
            fileNodeAdapter.setList(showFiles);
            fileNodeAdapter.notifyDataSetChanged();
        }
        if (downloadPop != null && downloadPop.isShowing()) {
            updateDownloadFiles();
            downloadFileAdapter.notifyDataSetChanged();
        }
    }

    private LevelDirNode findDirNode(int dirId) {
        for (int i = 0; i < allData.size(); i++) {
            LevelDirNode dirNode = (LevelDirNode) allData.get(i);
            if (dirNode.getDirId() == dirId) {
                return dirNode;
            }
        }
        return null;
    }

    private void queryMeetDirFile(int dirId) {
        try {
            long stime = System.currentTimeMillis();
            //查询会议目录文件
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo object = jni.queryMeetDirFile(dirId);
            LevelDirNode dirNode = findDirNode(dirId);
            if (dirNode != null) {
                dirNode.setChildNode(new ArrayList<>());
                if (object != null) {
                    List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> itemList = object.getItemList();
                    List<BaseNode> fileNodes = new ArrayList<>();
                    for (int j = 0; j < itemList.size(); j++) {
                        InterfaceFile.pbui_Item_MeetDirFileDetailInfo info = itemList.get(j);
                        LevelFileNode levelFileItem = new LevelFileNode(
                                dirId, info.getMediaid(), info.getName().toStringUtf8(), info.getUploaderid(),
                                info.getUploaderRole(), info.getUploaderName().toStringUtf8(), info.getMstime(),
                                info.getSize(), info.getAttrib(), info.getFilepos());
//                        levelFileItem.setSelected(beforeIsSelected(info.getMediaid()));
                        fileNodes.add(levelFileItem);
                    }
                    dirNode.setChildNode(fileNodes);
                }
            } else {
                LogUtils.e(TAG, "找不到该目录id " + dirId);
            }
            LogUtils.i(TAG, "查找目录下文件用时 " + (System.currentTimeMillis() - stime));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            //会议目录权限变更通知
            case EventType.directory_permission_change_inform: {
                InterfaceBase.pbui_MeetNotifyMsg info = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
                int id = info.getId();
                int opermethod = info.getOpermethod();
                LogUtils.i(TAG, "会议目录权限变更通知 id=" + id + ",opermethod=" + opermethod);
                jni.queryDirPermission(id);
                queryMeetDir();
                break;
            }
            //会议目录变更通知
            case EventType.MEETDIR_CHANGE_INFORM:
                queryMeetDir();
                break;
            //会议目录文件变更通知
            case EventType.MEETDIR_FILE_CHANGE_INFORM: {
                InterfaceBase.pbui_MeetNotifyMsgForDouble info = (InterfaceBase.pbui_MeetNotifyMsgForDouble) message.getObject();
                int id = info.getId();
                int subid = info.getSubid();
                int opermethod = info.getOpermethod();
                LogUtils.d(TAG, "会议目录文件变更通知 id=" + id + ",subid=" + subid + ",opermethod=" + opermethod);
                if (id != Macro.SHARED_FILE_DIRECTORY_ID && id != Macro.ANNOTATION_FILE_DIRECTORY_ID) {//过滤去除批注和共享资料
                    //文件变更,相对的目录中的文件数量也需要更新
                    queryMeetDir();
                }
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_document: {
                saveCurrentExpandStatus();
                fileFilterType = fileFilterType == 0 ? -1 : 0;
                showFiles();
                break;
            }
            case R.id.btn_picture: {
                saveCurrentExpandStatus();
                fileFilterType = fileFilterType == 1 ? -1 : 1;
                showFiles();
                break;
            }
            case R.id.btn_video: {
                saveCurrentExpandStatus();
                fileFilterType = fileFilterType == 2 ? -1 : 2;
                showFiles();
                break;
            }
            case R.id.btn_other: {
                saveCurrentExpandStatus();
                fileFilterType = fileFilterType == 3 ? -1 : 3;
                showFiles();
                break;
            }
            case R.id.btn_download: {//下载文件
                if (showFiles.isEmpty()) {
                    ToastUtils.showShort(R.string.no_data_download);
                    return;
                }
                if (MyUtils.isHasPermission(Macro.permission_code_download)) {
                    updateDownloadFiles();
                    downloadFilePop();
                } else {
                    ToastUtils.showShort(R.string.no_permission);
                }
                break;
            }
            case R.id.btn_push://文件推送
                int mediaId = fileNodeAdapter.getSelectedFileMediaId();
                if (mediaId == 0) {
                    ToastUtils.showShort(R.string.please_choose_file);
                    return;
                }
                EventBus.getDefault().post(new EventMessage(EventType.INFORM_PUSH_FILE, mediaId));
                break;
        }
    }

    private void updateDownloadFiles() {
        downloadFiles.clear();
        for (int i = 0; i < showFiles.size(); i++) {
            BaseNode node = showFiles.get(i);
            if (node instanceof LevelDirNode) {
                LevelDirNode dirNode = (LevelDirNode) node;
                List<BaseNode> childNode = dirNode.getChildNode();
                if (childNode != null) {
                    for (int j = 0; j < childNode.size(); j++) {
                        LevelFileNode fileNode = (LevelFileNode) childNode.get(j);
                        MeetDirFileInfo info = new MeetDirFileInfo(
                                fileNode.getDirId(), fileNode.getMediaId(), fileNode.getName(),
                                fileNode.getUploaderId(), fileNode.getUploaderRole(), fileNode.getMstime(),
                                fileNode.getSize(), fileNode.getAttrib(), fileNode.getFilepos(), fileNode.getUploaderName()
                        );
                        downloadFiles.add(info);
                    }
                }
            }
        }
    }

    List<MeetDirFileInfo> downloadFiles = new ArrayList<>();

    private void downloadFilePop() {
        LogUtils.i(TAG, "downloadFilePop 数据个数=" + downloadFiles.size());
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_download_file, null, false);
        downloadPop = PopUtil.createHalfPop(inflate, btn_push);
        RecyclerView rv_download_file = inflate.findViewById(R.id.rv_download_file);
        downloadFileAdapter = new DownloadFileAdapter(R.layout.item_download_file, downloadFiles);
        rv_download_file.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_download_file.setAdapter(downloadFileAdapter);
        downloadFileAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                MeetDirFileInfo info = downloadFiles.get(position);
                downloadFileAdapter.setSelectedFile(info);
            }
        });
        inflate.findViewById(R.id.btn_back).setOnClickListener(v -> downloadPop.dismiss());
        inflate.findViewById(R.id.btn_download).setOnClickListener(v -> {
            List<MeetDirFileInfo> selectedFiles = downloadFileAdapter.getSelectedFiles();
            if (selectedFiles.isEmpty()) {
                ToastUtils.showShort(R.string.please_choose_file_first);
                return;
            }
            for (int i = 0; i < selectedFiles.size(); i++) {
                MeetDirFileInfo info = selectedFiles.get(i);
                FileUtil.downloadFile(info, Macro.MEET_MATERIAL);
            }
        });
        inflate.findViewById(R.id.btn_saved_offline).setOnClickListener(v -> {
            List<MeetDirFileInfo> selectedFiles = downloadFileAdapter.getSelectedFiles();
            if (selectedFiles.isEmpty()) {
                ToastUtils.showShort(R.string.please_choose_file_first);
                return;
            }
            for (int i = 0; i < selectedFiles.size(); i++) {
                MeetDirFileInfo info = selectedFiles.get(i);
                FileUtil.downOfflineFile(info);
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        /** **** **  如果是隐藏状态，则表示不在显示  ** **** **/
        LogUtil.e(TAG, "MeetingFileFragment.onHiddenChanged :   --> 是否隐藏：" + hidden);
        if (!hidden) {
            queryMeetDir();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (!EventBus.getDefault().isRegistered(this)) {
//            LogUtils.e(TAG,"注册EventBus");
//            EventBus.getDefault().register(this);
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
