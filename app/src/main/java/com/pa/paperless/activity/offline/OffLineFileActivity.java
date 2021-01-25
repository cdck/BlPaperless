package com.pa.paperless.activity.offline;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.OffMeetFileAdapter;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.LogUtil;

import com.wind.myapplication.NativeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.pa.paperless.data.constant.Macro.root_dir;

public class OffLineFileActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "OffLineFileActivity-->";
    private NativeUtil jni;
    private RecyclerView off_dir_rv;
    private Button btn_delete_dir;
    private Button btn_document_off;
    private Button btn_picture_off;
    private Button btn_video_off;
    private Button btn_other_off;
    private ListView off_file_list;
    private Button btn_pre_page;
    private TextView tv_page_view;
    private Button btn_next_page;
    private Button btn_delete;
    private DirAdapter dirAdapter;
    private List<InterfaceFile.pbui_Item_MeetDirDetailInfo> meetDirs = new ArrayList<>();
    private List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> allFiles = new ArrayList<>();
    private List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> fileDatas = new ArrayList<>();
    private OffMeetFileAdapter fileAdapter;
    private int page_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
        jni = NativeUtil.getInstance();
        queryDir();
        off_file_list.post(() -> {
            //ListView的px高度 / 每个item的px高度 = 当前页可显示item的个数
            int height = off_file_list.getHeight();
            int measuredHeight = off_file_list.getMeasuredHeight();
            int itemCount = height / 100;
            LogUtil.e(TAG, "run :  list_file_off --> height= " + height + "px, measuredHeight= " + measuredHeight + "px ," + itemCount);
            fileAdapter = new OffMeetFileAdapter(getApplicationContext(), fileDatas, itemCount);
            off_file_list.setAdapter(fileAdapter);
            fileAdapter.setItemSelectListener((posion, view) -> {
                fileAdapter.setSelect(fileDatas.get(posion).getMediaid());
            });
            fileAdapter.setLookListener(fileDetailInfo -> {
                String name = fileDetailInfo.getName().toStringUtf8();
                File file = FileUtil.findFilePathByName(root_dir + "/meetcache", name);
                if (file != null) {
                    LogUtil.e(TAG, "查到的文件 -->" + file.getAbsolutePath() + ", 需要查找的文件：" + name);
                    FileUtil.openLocalFile(this, file);
                } else {
                    ToastUtils.showShort( R.string.error_find_file_failed);
                }
            });
            initDefaultFile();
        });
    }

    private void initDefaultFile() {
        if (dirAdapter != null && !meetDirs.isEmpty()) {
            queryDirFile(meetDirs.get(0).getId());
            dirAdapter.setSelectDir(meetDirs.get(0).getId());
        }
    }

    private void queryDir() {
        try {
            InterfaceFile.pbui_Type_MeetDirDetailInfo meetDir = jni.queryMeetDir();
            if (meetDir == null) return;
            meetDirs.clear();
            meetDirs.addAll(meetDir.getItemList());
            if (dirAdapter == null) {
                dirAdapter = new DirAdapter(R.layout.item_meet_name_lv0, meetDirs);
                off_dir_rv.setLayoutManager(new LinearLayoutManager(this));
                off_dir_rv.setAdapter(dirAdapter);
                dirAdapter.setOnItemClickListener((adapter, view, position) -> {
                    int currentDirId = meetDirs.get(position).getId();
                    dirAdapter.setSelectDir(currentDirId);
                    queryDirFile(currentDirId);
                });
            } else {
                dirAdapter.notifyDataSetChanged();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryDirFile(int dirId) {
        try {
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo meetDirFile = jni.queryMeetDirFile(dirId);
            if (meetDirFile == null) {
                allFiles.clear();
                fileDatas.clear();
                if (fileAdapter != null) {
                    fileAdapter.notifyDataSetChanged();
                    fileAdapter.PAGE_NOW = 0;
                    fileAdapter.clearSelectFiles();
                    checkButton();
                    updatePageTv();
                }
                return;
            }
            allFiles.clear();
            allFiles.addAll(meetDirFile.getItemList());
            fileDatas.clear();
            fileDatas.addAll(meetDirFile.getItemList());
            if (fileAdapter != null) {
                fileAdapter.notifyDataSetChanged();
                fileAdapter.PAGE_NOW = 0;
                fileAdapter.clearSelectFiles();
                checkButton();
                updatePageTv();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void updatePageTv() {
        if (fileAdapter == null) return;
        int count = fileDatas.size();
        if (count != 0) {
            if (count % fileAdapter.ITEM_COUNT == 0) {
                page_count = fileDatas.size() / fileAdapter.ITEM_COUNT;
            } else {
                page_count = (fileDatas.size() / fileAdapter.ITEM_COUNT) + 1;
            }
            tv_page_view.setText(fileAdapter.PAGE_NOW + 1 + " / " + page_count);
        } else {
            tv_page_view.setText(fileAdapter.PAGE_NOW + " / " + count);
        }
    }

    private void checkButton() {
        if (fileAdapter == null) return;
        if (fileAdapter.PAGE_NOW == 0) {
            btn_pre_page.setEnabled(false);
            btn_next_page.setEnabled(fileDatas.size() > fileAdapter.ITEM_COUNT);
        } else if (fileDatas.size() - (fileAdapter.PAGE_NOW * fileAdapter.ITEM_COUNT) <= fileAdapter.ITEM_COUNT) {
            btn_next_page.setEnabled(false);
            btn_pre_page.setEnabled(true);
        } else {
            btn_next_page.setEnabled(true);
            btn_pre_page.setEnabled(true);
        }
    }

    private void initView() {
        off_dir_rv = (RecyclerView) findViewById(R.id.off_dir_rv);
        btn_delete_dir = (Button) findViewById(R.id.btn_delete_dir);
        btn_document_off = (Button) findViewById(R.id.btn_document_off);
        btn_picture_off = (Button) findViewById(R.id.btn_picture_off);
        btn_video_off = (Button) findViewById(R.id.btn_video_off);
        btn_other_off = (Button) findViewById(R.id.btn_other_off);
        off_file_list = (ListView) findViewById(R.id.off_file_list);
        btn_pre_page = (Button) findViewById(R.id.btn_pre_page);
        tv_page_view = (TextView) findViewById(R.id.tv_page_view);
        btn_next_page = (Button) findViewById(R.id.btn_next_page);
        btn_delete = (Button) findViewById(R.id.btn_delete);

        btn_delete_dir.setOnClickListener(this);
        btn_document_off.setOnClickListener(this);
        btn_picture_off.setOnClickListener(this);
        btn_video_off.setOnClickListener(this);
        btn_other_off.setOnClickListener(this);
        btn_pre_page.setOnClickListener(this);
        btn_next_page.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_document_off:
                changeFileType(0);
                break;
            case R.id.btn_picture_off:
                changeFileType(1);
                break;
            case R.id.btn_video_off:
                changeFileType(2);
                break;
            case R.id.btn_other_off:
                changeFileType(3);
                break;
            case R.id.btn_pre_page:
                prePage();
                break;
            case R.id.btn_next_page:
                nextPage();
                break;
            case R.id.btn_delete_dir:
                if (dirAdapter == null) break;
                InterfaceFile.pbui_Item_MeetDirDetailInfo selectDir = dirAdapter.getSelectDir();
                if (selectDir == null) break;
                jni.deleteMeetDir(selectDir);
                dirAdapter.deleteSelect(selectDir);
                if (!meetDirs.isEmpty()) {
                    queryDir();
                    initDefaultFile();
                } else {
                    allFiles.clear();
                    fileDatas.clear();
                    if (fileAdapter != null) {
                        fileAdapter.notifyDataSetChanged();
                        fileAdapter.PAGE_NOW = 0;
                        fileAdapter.clearSelectFiles();
                        checkButton();
                        updatePageTv();
                    }
                }
                break;
            case R.id.btn_delete:
                if (fileAdapter == null) break;
                List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> selectFile = fileAdapter.getSelectFile();
                if (!selectFile.isEmpty()) {
                    jni.deleteMeetDirFile(dirAdapter.getSelectedDirId(), selectFile);
                    Iterator<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> iterator = fileDatas.iterator();
                    while (iterator.hasNext()) {
                        InterfaceFile.pbui_Item_MeetDirFileDetailInfo next = iterator.next();
                        if (fileAdapter.getChecks().contains(next.getMediaid())) {
                            iterator.remove();
                        }
                    }
                    fileAdapter.notifyDataSetChanged();
                    updatePageTv();
                    checkButton();
                }
                break;
        }
    }

    private void nextPage() {
        if (fileAdapter == null) return;
        fileAdapter.PAGE_NOW++;
        fileAdapter.notifyDataSetChanged();
        updatePageTv();
        checkButton();
    }

    private void prePage() {
        if (fileAdapter == null) return;
        fileAdapter.PAGE_NOW--;
        fileAdapter.notifyDataSetChanged();
        updatePageTv();
        checkButton();
    }

    private void changeFileType(int type) {
        fileDatas.clear();
        for (int i = 0; i < allFiles.size(); i++) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo info = allFiles.get(i);
            switch (type) {
                case 0:
                    if (FileUtil.isDocument(info.getName().toStringUtf8())) {
                        fileDatas.add(info);
                    }
                    break;
                case 1:
                    if (FileUtil.isPicture(info.getName().toStringUtf8())) {
                        fileDatas.add(info);
                    }
                    break;
                case 2:
                    if (FileUtil.isVideo(info.getName().toStringUtf8())) {
                        fileDatas.add(info);
                    }
                    break;
                case 3:
                    if (FileUtil.isOtherFile(info.getName().toStringUtf8())) {
                        fileDatas.add(info);
                    }
                    break;
            }
        }
        fileAdapter.PAGE_NOW = 0;
        fileAdapter.notifyDataSetChanged();
        updatePageTv();
        checkButton();
    }
}
