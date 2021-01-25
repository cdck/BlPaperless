package com.pa.paperless.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.Values;
import com.pa.paperless.utils.LogUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.TypeFileAdapter;
import com.pa.paperless.data.bean.MeetDirFileInfo;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.Dispose;
import com.pa.paperless.utils.FileUtil;

import com.pa.paperless.utils.UriUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import static com.pa.paperless.utils.MyUtils.isHasPermission;


/**
 * Created by Administrator on 2017/10/31.
 * 共享资料
 */

public class SharedFileFragment extends BaseFragment implements View.OnClickListener {

    private final String TAG = "ShareFileFragment-->";
    private Button btn_document;
    private Button btn_picture;
    private Button btn_video;
    private Button btn_other;
    private ListView sharedfile_lv;
    private Button share_prepage;
    private Button share_nextpage;
    private Button share_import;
    private Button download, push_file;
    private Button share_saved_offline_btn;
    private List<Button> mBtns;
    private TypeFileAdapter mAllAdapter;
    private List<MeetDirFileInfo> mData;//用于临时存放不同类型的文件
    private List<MeetDirFileInfo> meetDirFileInfos;
    private TextView page_tv;
    private int page_count;
    private boolean isFirstIn;
    public static String shareFileName;
    public static int shardFileType = -1;//保存显示类别

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.right_sharedfile, container, false);
        initView(inflate);
        isFirstIn = true;
        initBtns();
        fun_queryMeetDirFile(Macro.SHARED_FILE_DIRECTORY_ID);
        return inflate;
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

    private void fun_queryMeetDirFile(int dirid) {
        try {
            //143.查询会议目录文件（直接查询 共享资料(id 是固定为 1 )的文件）
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo object = jni.queryMeetDirFile(dirid);
            if (object == null) {
                clear();
                return;
            }
            if (meetDirFileInfos == null) meetDirFileInfos = new ArrayList<>();
            else meetDirFileInfos.clear();
            meetDirFileInfos.addAll(Dispose.MeetDirFile(object));
            if (!meetDirFileInfos.isEmpty()) {
                checkBtn(true);
                if (mData == null) mData = new ArrayList<>();
                mData.clear();
                for (int i = 0; i < meetDirFileInfos.size(); i++) {
                    MeetDirFileInfo meetDirFileInfo = meetDirFileInfos.get(i);
                    String fileName = meetDirFileInfo.getFileName();
                    if (shardFileType == -1) {
                        mData.add(meetDirFileInfos.get(i));
                    } else {
                        boolean isAdd = false;
                        switch (shardFileType) {
                            case 0:
                                isAdd = FileUtil.isDocument(fileName);
                                break;
                            case 1:
                                isAdd = FileUtil.isPicture(fileName);
                                break;
                            case 2:
                                isAdd = FileUtil.isVideo(fileName);
                                break;
                            case 3:
                                isAdd = FileUtil.isOtherFile(fileName);
                                break;
                        }
                        if (isAdd) {
                            mData.add(meetDirFileInfo);
                        }
                    }
                }
                if (mAllAdapter == null) {
                    mAllAdapter = new TypeFileAdapter(getContext(), mData);
                    sharedfile_lv.setAdapter(mAllAdapter);
                }
                mAllAdapter.notifyDataSetChanged();
                if (!isFirstIn) {
                    int pagecount = 0;
                    if (mData.size() % mAllAdapter.ITEM_COUNT == 0) {
                        pagecount = mData.size() / mAllAdapter.ITEM_COUNT;
                    } else {
                        pagecount = mData.size() / mAllAdapter.ITEM_COUNT + 1;
                    }
                    if (mAllAdapter.PAGE_NOW >= pagecount) {
                        mAllAdapter.PAGE_NOW = pagecount;
                    }
                }
                updataPageTv();
                checkButton();
                setBtnSelect(shardFileType);
                mAllAdapter.setLookListener((fileInfo, mediaid, filename, filesize) -> {
                    if (FileUtil.isVideo(filename)) {
                        //如果是音频或视频则在线播放
                        List<Integer> devIds = new ArrayList<Integer>();
                        devIds.add(Values.localDevId);
                        jni.mediaPlayOperate(mediaid, devIds, 0, 0, 0, InterfaceMacro.Pb_MeetPlayFlag.Pb_MEDIA_PLAYFLAG_ZERO.getNumber());
                    } else {
                        //Macro.SHARE_MATERIAL
                        FileUtil.openFile(Macro.CACHE_ALL_FILE, filename, jni, mediaid, getContext(), filesize);
                    }
                });
//                mAllAdapter.setDownListener((posion, filename, filesize) -> MyUtils.downLoadFile(Macro.SHARE_MATERIAL, filename, getContext(), posion, nativeUtil, filesize));
                //item选中事件
                mAllAdapter.setItemSelectListener((posion, view) -> {
                    mAllAdapter.setCheck(mData.get(posion).getMediaId());
                });
            } else {
                clear();
                checkBtn(false);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.MEETDIR_FILE_CHANGE_INFORM://142 会议目录文件变更通知
                InterfaceBase.pbui_MeetNotifyMsgForDouble object1 = (InterfaceBase.pbui_MeetNotifyMsgForDouble) message.getObject();
                int id = object1.getId();
                if (id == Macro.SHARED_FILE_DIRECTORY_ID) {
                    fun_queryMeetDirFile(id);
                }
                break;
        }
    }

    private void clear() {
        LogUtil.d(TAG, "clear: 清空数据.. ");
        if (meetDirFileInfos != null)
            meetDirFileInfos.clear();
        if (mData != null)
            mData.clear();
        if (mAllAdapter != null)
            mAllAdapter.notifyDataSetChanged();
    }

    private void checkBtn(boolean b) {
        btn_document.setClickable(b);
        btn_picture.setClickable(b);
        btn_video.setClickable(b);
        btn_other.setClickable(b);
    }

    private void initBtns() {
        mBtns = new ArrayList<>();
        mBtns.add(btn_document);
        mBtns.add(btn_picture);
        mBtns.add(btn_video);
        mBtns.add(btn_other);
    }

    private void initView(View inflate) {
        btn_document = inflate.findViewById(R.id.rightsharefile_document);
        btn_picture = inflate.findViewById(R.id.rightsharefile_picture);
        btn_video = inflate.findViewById(R.id.rightsharefile_video);
        btn_other = inflate.findViewById(R.id.rightsharefile_other);
        sharedfile_lv = inflate.findViewById(R.id.rightsharefile_lv);
        share_prepage = inflate.findViewById(R.id.rightsharefile_prepage);
        share_nextpage = inflate.findViewById(R.id.rightsharefile_nextpage);
        share_import = inflate.findViewById(R.id.rightsharefile_import);
        page_tv = inflate.findViewById(R.id.page_tv);
        push_file = inflate.findViewById(R.id.push_file);
        download = inflate.findViewById(R.id.download);
        share_saved_offline_btn = inflate.findViewById(R.id.share_saved_offline_btn);
        share_saved_offline_btn.setOnClickListener(this);
        push_file.setOnClickListener(this);
        download.setOnClickListener(this);
        btn_document.setOnClickListener(this);
        btn_picture.setOnClickListener(this);
        btn_video.setOnClickListener(this);
        btn_other.setOnClickListener(this);
        share_prepage.setOnClickListener(this);
        share_nextpage.setOnClickListener(this);
        share_import.setOnClickListener(this);
    }

    //设置选中状态
    private void setBtnSelect(int index) {
        shardFileType = index;
        for (int i = 0; i < mBtns.size(); i++) {
            mBtns.get(i).setSelected(i == index);
        }
    }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rightsharefile_document:
                if (meetDirFileInfos != null) {
                    mData.clear();
                    for (int i = 0; i < meetDirFileInfos.size(); i++) {
                        MeetDirFileInfo documentBean = meetDirFileInfos.get(i);
                        String fileName = documentBean.getFileName();
                        if (FileUtil.isDocument(fileName)) {
                            mData.add(documentBean);
                        }
                    }
                }
                if (mAllAdapter != null) {
                    mAllAdapter.notifyDataSetChanged();
                    mAllAdapter.PAGE_NOW = 0;
                    updataPageTv();
                    checkButton();
                    setBtnSelect(0);
                }
                break;
            case R.id.rightsharefile_picture:
                if (meetDirFileInfos != null) {
                    mData.clear();
                    for (int i = 0; i < meetDirFileInfos.size(); i++) {
                        MeetDirFileInfo documentBean = meetDirFileInfos.get(i);
                        String fileName = documentBean.getFileName();
                        if (FileUtil.isPicture(fileName)) {
                            mData.add(documentBean);
                        }
                    }
                }
                if (mAllAdapter != null) {
                    mAllAdapter.notifyDataSetChanged();
                    mAllAdapter.PAGE_NOW = 0;
                    updataPageTv();
                    checkButton();
                    setBtnSelect(1);
                }
                break;
            case R.id.rightsharefile_video:
                if (meetDirFileInfos != null) {
                    mData.clear();
                    for (int i = 0; i < meetDirFileInfos.size(); i++) {
                        MeetDirFileInfo documentBean = meetDirFileInfos.get(i);
                        String fileName = documentBean.getFileName();
                        if (FileUtil.isVideo(fileName)) {
                            mData.add(documentBean);
                        }
                    }
                }
                if (mAllAdapter != null) {
                    mAllAdapter.notifyDataSetChanged();
                    mAllAdapter.PAGE_NOW = 0;
                    updataPageTv();
                    checkButton();
                    setBtnSelect(2);
                }
                break;
            case R.id.rightsharefile_other:
                if (meetDirFileInfos != null) {
                    mData.clear();
                    for (int i = 0; i < meetDirFileInfos.size(); i++) {
                        MeetDirFileInfo documentBean = meetDirFileInfos.get(i);
                        String fileName = documentBean.getFileName();
                        if (FileUtil.isOtherFile(fileName)) {
                            mData.add(documentBean);
                        }
                    }
                }
                if (mAllAdapter != null) {
                    mAllAdapter.notifyDataSetChanged();
                    mAllAdapter.PAGE_NOW = 0;
                    updataPageTv();
                    checkButton();
                    setBtnSelect(3);
                }
                break;
            case R.id.rightsharefile_prepage://上一页
                if (mAllAdapter != null) {
                    prePage();
                }
                break;
            case R.id.rightsharefile_nextpage://下一页
                if (mAllAdapter != null) {
                    nextPage();
                }
                break;
            case R.id.rightsharefile_import://导入文件
                //是否有上传权限
                if (isHasPermission(Macro.permission_code_upload)) {
                    showPop();
                } else {
                    ToastUtils.showShort(R.string.no_permission);
                }
                break;
            case R.id.share_saved_offline_btn:
                if (mAllAdapter == null) break;
                MeetDirFileInfo data = mAllAdapter.getCheckedFile();
                if (data == null) {
                    ToastUtils.showShort(R.string.please_choose_downloadfile);
                    break;
                }
                FileUtil.downOfflineFile(data);
                break;
            case R.id.download:
                if (mAllAdapter == null) break;
                MeetDirFileInfo data1 = mAllAdapter.getCheckedFile();
                if (data1 == null) {
                    ToastUtils.showShort(R.string.please_choose_downloadfile);
                    break;
                }
                FileUtil.downloadFile(data1, Macro.SHARE_MATERIAL);
                break;
            case R.id.push_file:
                if (mAllAdapter == null) break;
                MeetDirFileInfo data2 = mAllAdapter.getCheckedFile();
                if (data2 == null) {
                    ToastUtils.showShort(R.string.please_choose_file);
                    break;
                }
                EventBus.getDefault().post(new EventMessage(EventType.INFORM_PUSH_FILE, data2.getMediaId()));
                break;
        }
    }

    private void showPop() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");//无类型限制
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 1);
        } catch (Exception e) {
            ToastUtils.showShort(R.string.open_fileSystem_failed);
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) return;
            String path = "";
            try {
                path = UriUtil.getFilePath(getContext(), uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (path == null || path.isEmpty()) {
                ToastUtils.showShort(R.string.get_file_path_fail);
            } else {
                LogUtil.e(TAG, "SharedFileFragment.onActivityResult 370行:  选中文件的路径 --->>> " + path);
                showDialog(path);
            }
        }
    }

    /**
     * 上传文件
     *
     * @param path
     */
    public void showDialog(final String path) {
        File file = new File(path);
        if (!file.exists()) {
            ToastUtils.showShort(R.string.file_acquisition_exception);
            return;
        }
        LogUtil.e(TAG, "SharedFileFragment.showDialog :截取的文件 file --> " + file.getAbsolutePath());
        String name = file.getName();
        String bStr = name;
        if (name.contains(".")) {
            bStr = name.substring(0, name.lastIndexOf("."));// 123
        }
        final EditText editText = new EditText(getContext());
        //给输入框设置默认文件名
        editText.setText(bStr);
        new AlertDialog.Builder(getContext()).setTitle(getResources().getString(R.string.Please_enter_valid_file_name))
                .setView(editText)
                .setPositiveButton(getResources().getString(R.string.ensure), (dialogInterface, i) -> {
                    if (!(TextUtils.isEmpty(editText.getText().toString().trim()))) {
                        shareFileName = editText.getText().toString();
                        //计算出 媒体ID
//                        int mediaid = MyUtils.getMediaid(path);
                        String fileEnd = FileUtil.getCutStr(path, 0);
                        String newFileName;
                        if (!fileEnd.isEmpty()) {
                            newFileName = shareFileName + "." + fileEnd;
                        } else {
                            newFileName = shareFileName;
                        }
                        jni.uploadFile(InterfaceMacro.Pb_Upload_Flag.Pb_MEET_UPLOADFLAG_ONLYENDCALLBACK.getNumber(),
                                Macro.SHARED_FILE_DIRECTORY_ID, 0, newFileName, path, 0, Macro.upload_local_file);
                        dialogInterface.dismiss();
                    } else {
                        ToastUtils.showShort(R.string.Please_enter_valid_file_name);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    //下一页
    private void nextPage() {
        mAllAdapter.PAGE_NOW++;
        mAllAdapter.notifyDataSetChanged();
        updataPageTv();
        checkButton();
    }

    //上一页
    private void prePage() {
        mAllAdapter.PAGE_NOW--;
        mAllAdapter.notifyDataSetChanged();
        updataPageTv();
        checkButton();
    }

    private void updataPageTv() {
        int count = mData.size();
        LogUtil.e(TAG, "SharedFileFragment.updataPageTv :   --> " + mData.size() + " ,  mAllAdapter.getCount() : " + mAllAdapter.getCount());
        if (count != 0) {
            if (count % mAllAdapter.ITEM_COUNT == 0) {
                page_count = mData.size() / mAllAdapter.ITEM_COUNT;
            } else {
                page_count = (mData.size() / mAllAdapter.ITEM_COUNT) + 1;
            }
            page_tv.setText(mAllAdapter.PAGE_NOW + 1 + " / " + page_count);
        } else {
            page_tv.setText(mAllAdapter.PAGE_NOW + " / " + count);
        }
    }

    //设置两个按钮是否可用
    public void checkButton() {
        //如果页码已经是第一页了
        if (mAllAdapter.PAGE_NOW <= 0) {
            share_prepage.setEnabled(false);
            //如果不设置的话，只要进入一次else if ，那么下一页按钮就一直是false，不可点击状态
            if (mData.size() > mAllAdapter.ITEM_COUNT) {
                share_nextpage.setEnabled(true);
            } else {
                share_nextpage.setEnabled(false);
            }
        }
        //值的长度减去前几页的长度，剩下的就是这一页的长度，如果这一页的长度比View_Count小，表示这是最后的一页了，后面在没有了。
        else if (mData.size() - mAllAdapter.PAGE_NOW * mAllAdapter.ITEM_COUNT <= mAllAdapter.ITEM_COUNT) {
            share_nextpage.setEnabled(false);
            share_prepage.setEnabled(true);
        } else {
            //否则两个按钮都设为可用
            share_prepage.setEnabled(true);
            share_nextpage.setEnabled(true);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        isFirstIn = false;
        LogUtil.e(TAG, "SharedFileFragment.onHiddenChanged :  是否隐藏 --> " + hidden);
        if (!hidden) {
            fun_queryMeetDirFile(Macro.SHARED_FILE_DIRECTORY_ID);
        }
    }
}
