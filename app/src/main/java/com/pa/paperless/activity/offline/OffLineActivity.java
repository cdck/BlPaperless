package com.pa.paperless.activity.offline;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.OffMeetAdapter;
import com.pa.paperless.utils.LogUtil;
import com.pa.paperless.utils.ToastUtil;
import com.wind.myapplication.NativeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xlk
 * @date 2019/8/24
 */
public class OffLineActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView off_meet_rl;
    private Button off_btn_jump;
    private Button btn_off_delete;
    private NativeUtil jni;
    private final String TAG = "OffLineActivity-->";
    private List<InterfaceMeet.pbui_Item_MeetMeetInfo> meetInfos = new ArrayList<>();
    private OffMeetAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_line);
        initView();
        jni = NativeUtil.getInstance();
        queryMeeting();
    }

    private void queryMeeting() {
        try {
            InterfaceMeet.pbui_Type_MeetMeetInfo meetInfo = jni.queryMeeting();
            LogUtil.e(TAG, " meetInfo = " + meetInfo);
            if (meetInfo == null) {
                if (adapter != null) {
                    meetInfos.clear();
                    adapter.notifyDataSetChanged();
                    adapter.updateSelected();
                }
                return;
            }
            List<InterfaceMeet.pbui_Item_MeetMeetInfo> itemList = meetInfo.getItemList();
            meetInfos.clear();
            meetInfos.addAll(itemList);
            if (adapter == null) {
                adapter = new OffMeetAdapter(R.layout.item_off_meet, meetInfos);
                off_meet_rl.setLayoutManager(new LinearLayoutManager(this));
                off_meet_rl.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
                adapter.updateSelected();
            }
            adapter.setOnItemClickListener((ad, view, position) -> {
                InterfaceMeet.pbui_Item_MeetMeetInfo pbui_item_meetMeetInfo = meetInfos.get(position);
                adapter.setSelected(pbui_item_meetMeetInfo.getId());
            });
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        off_meet_rl = (RecyclerView) findViewById(R.id.off_meet_rl);
        off_btn_jump = (Button) findViewById(R.id.off_btn_jump);
        btn_off_delete = (Button) findViewById(R.id.btn_off_delete);

        off_btn_jump.setOnClickListener(this);
        btn_off_delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.off_btn_jump:
                if (adapter == null) break;
                InterfaceMeet.pbui_Item_MeetMeetInfo meetInfo = adapter.getSelectedMeet();
                if (meetInfo == null) {
                    ToastUtil.showToast( R.string.tip_choose_meeting);
                    break;
                }
                jump2meet(meetInfo.getId(), meetInfo.getName().toStringUtf8());
                break;
            case R.id.btn_off_delete:
                if (adapter == null) break;
                InterfaceMeet.pbui_Item_MeetMeetInfo selectedMeet = adapter.getSelectedMeet();
                if (selectedMeet == null) {
                    ToastUtil.showToast( R.string.tip_choose_meeting);
                    break;
                }
                jni.deleteMeeting(selectedMeet);
                adapter.remove(adapter.getSelectPosion());
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private void jump2meet(int meetId, String meetName) {
        jni.setContextProperty(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURMEETINGID.getNumber(), meetId);
        Intent intent = new Intent(OffLineActivity.this, OffLineFileActivity.class);
        intent.putExtra("meetName", meetName);
        startActivity(intent);
    }
}
