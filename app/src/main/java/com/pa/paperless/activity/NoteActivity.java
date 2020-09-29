package com.pa.paperless.activity;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;

import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.LogUtil;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.Export;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;

public class NoteActivity extends BaseActivity implements View.OnClickListener {

    private final String TAG = "NoteActivity-->";
    private TextView empty;
    private EditText edt_note;
    private Button note_import;
    private Button note_save;
    private Button note_back;
    private String mNoteCentent;
    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        resources = getResources();
        initView();
        initEvent();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.is_loading:

                break;
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initEvent() {
        //获取到之前写得tempNote
        File file = new File(Macro.MEET_NOTE + "tempNote.txt");
        if (file.exists()) {
            mNoteCentent = Export.readText(file);
            edt_note.setText(mNoteCentent);
        }
        //将光标移动到末尾
        edt_note.setSelection(edt_note.getText().toString().length());
    }

    private void initView() {
        empty = (TextView) findViewById(R.id.empty);
        edt_note = (EditText) findViewById(R.id.edt_note);
        note_import = (Button) findViewById(R.id.note_import);
        note_save = (Button) findViewById(R.id.note_save);
        note_back = (Button) findViewById(R.id.note_back);
        empty.setOnClickListener(this);
        note_import.setOnClickListener(this);
        note_save.setOnClickListener(this);
        note_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.empty://清空
                edt_note.setText("");
                break;
            case R.id.note_import://导入
                List<File> files = new ArrayList<>();
                List<File> fs = FileUtil.GetFiles(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator, ".txt", true, files);
                showTxtDialog(fs, edt_note);
                break;
            case R.id.note_save://保存
                String s1 = edt_note.getText().toString();
                if (!s1.trim().equals("")) {
                    showEdtPop(s1, Macro.MEET_NOTE);
                }
                break;
            case R.id.note_back://返回
                String s2 = edt_note.getText().toString();
                Export.ToNoteText(s2, "tempNote", Macro.MEET_NOTE);
                LogUtil.e(TAG, "NoteActivity.onClick :   --> " + getCacheDir().getAbsolutePath());
                finish();
                break;
        }
    }

    //展示输入文件名弹出框
    private void showEdtPop(final String content, final String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText edt = new EditText(this);
        builder.setTitle(resources.getString(R.string.please_enter_file_name));
        edt.setHint(resources.getString(R.string.please_enter_file_name));
        edt.setText(System.currentTimeMillis() + "");
        edt.setSelection(edt.getText().toString().length());
        builder.setView(edt);
        builder.setPositiveButton(resources.getString(R.string.ensure), (dialog, which) -> {
            String filename = edt.getText().toString().trim();
            if (filename.equals("")) {
                ToastUtil.showToast( R.string.please_enter_file_name);
                return;
            } else {
                Export.ToNoteText(content, filename, path);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(resources.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    /**
     * 以弹出框的形式展示查找到的txt文档文件
     *
     * @param txtFile 存放文件路径的集合 /storage/emulated/0/游戏文本.txt
     * @param edt     获取TXT文本内容后展示到 edt中
     */
    public void showTxtDialog(List<File> txtFile, final EditText edt) {
        final List<File> fs = new ArrayList<>();
        for (int i = 0; i < txtFile.size(); i++) {
            double size = (txtFile.get(i).length() / 1024);
            /** **** **  文件过大会导致ANR，文件是小于2M的时候才加入  ** **** **/
            LogUtil.e(TAG, "MyUtils.showTxtDialog : " + txtFile.get(i).getName() + "  文件大小 -->" + size + " KB");
            if (size > 0 && size < 300) {
                fs.add(txtFile.get(i));
            }
        }
        //txt文件的名称
        final String[] txtFileName = new String[fs.size()];
        for (int i = 0; i < fs.size(); i++) {
            txtFileName[i] = fs.get(i).getName();
        }
        //只有SD卡中有该类文件的时候才展示，否则会报错
        if (txtFileName.length > 0) {
            new AlertDialog.Builder(this).setTitle("选择要导入的文本文件")
                    .setItems(txtFileName, new DialogInterface.OnClickListener() {
                        String content = "";

                        @Override
                        public void onClick(DialogInterface dialogInterface, final int i) {
                            dialogInterface.dismiss();
                            EventBus.getDefault().post(new EventMessage(EventType.is_loading, true));
                            //  读取内容获得文本
                            content = MyUtils.ReadTxtFile(fs.get(i).getAbsolutePath());
                            edt.setText(this.content);
                            EventBus.getDefault().post(new EventMessage(EventType.is_loading, false));
                        }
                    }).create().show();
        }
    }
}
