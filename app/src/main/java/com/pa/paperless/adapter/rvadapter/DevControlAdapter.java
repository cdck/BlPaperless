package com.pa.paperless.adapter.rvadapter;

import android.content.Context;
import android.graphics.Color;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.bean.DevControlBean;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.listener.ItemClickListener;
import com.pa.paperless.utils.MyUtils;
import com.wind.myapplication.NativeUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xlk on 2018/10/27.
 */

public class DevControlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context cxt;
    private List<DevControlBean> mData;
    private ItemClickListener mListener;
    private List<Integer> checks;

    public DevControlAdapter(Context context, List<DevControlBean> deviceInfos) {
        cxt = context;
        mData = deviceInfos;
        checks = new ArrayList<>();
    }

    public void notifyChecks() {
        notifyDataSetChanged();
        List<Integer> temps = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            DevControlBean o = mData.get(i);
            int devcieid = o.getDevice().getDevcieid();
            if (checks.contains(devcieid)) {
                temps.add(devcieid);
            }
        }
        checks = temps;
        notifyDataSetChanged();
    }

    public List<Integer> getChecks() {
        return checks;
    }

    public void setChecks(int devid) {
        if (checks.contains(devid)) {
            checks.remove(checks.indexOf(devid));
        } else checks.add(devid);
    }

    public boolean isCheckAll() {
        return checks.size() == mData.size();
    }

    public void setCheckAll(boolean b) {
        checks.clear();
        if (b) {
            for (int i = 0; i < mData.size(); i++) {
                checks.add(mData.get(i).getDevice().getDevcieid());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(cxt).inflate(R.layout.item_dev_control, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    public void setItemClick(ItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).number_cb.setText(position + 1 + "");
        DevControlBean item = mData.get(position);
        int devid = item.getDevice().getDevcieid();
        ((ViewHolder) holder).number_cb.setChecked(checks.contains(devid));
        ((ViewHolder) holder).mamberTv.setText(item.getMemberName());
        ((ViewHolder) holder).nameTv.setText(item.getDevice().getDevname().toStringUtf8());
        int devId = devid;
        String devTypeStr = getDevType(devId);
        ((ViewHolder) holder).devTypeTv.setText(devTypeStr);
        boolean isOnline = item.getDevice().getNetstate() == 1;
        ((ViewHolder) holder).devStateTv.setText(isOnline ? cxt.getString(R.string.online) : cxt.getString(R.string.offline));
        int faceState = item.getDevice().getFacestate();
        String faceStateStr = getFaceState(faceState);
        int deviceflag = item.getDevice().getDeviceflag();
        boolean b = InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_OPENOUTSIDE_VALUE ==
                (deviceflag & InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_OPENOUTSIDE_VALUE);
        ((ViewHolder) holder).outDocumentTv.setText(b ? "√" : "");
        ((ViewHolder) holder).interfaceStateTv.setText(faceStateStr);
        ((ViewHolder) holder).itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(((ViewHolder) holder).itemView, position);
            }
        });
        ((ViewHolder) holder).number_cb.setTextColor(isOnline ? Color.BLUE : Color.BLACK);
        ((ViewHolder) holder).mamberTv.setTextColor(isOnline ? Color.BLUE : Color.BLACK);
        ((ViewHolder) holder).nameTv.setTextColor(isOnline ? Color.BLUE : Color.BLACK);
        ((ViewHolder) holder).devTypeTv.setTextColor(isOnline ? Color.BLUE : Color.BLACK);
        ((ViewHolder) holder).devStateTv.setTextColor(isOnline ? Color.BLUE : Color.BLACK);
        ((ViewHolder) holder).outDocumentTv.setTextColor(isOnline ? Color.BLUE : Color.BLACK);
        ((ViewHolder) holder).interfaceStateTv.setTextColor(isOnline ? Color.BLUE : Color.BLACK);
    }


    private String getFaceState(int faceState) {
        if (faceState == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MainFace.getNumber()) {
            return cxt.getString(R.string.main_interface);
        } else if (faceState == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MemFace.getNumber()) {
            return cxt.getString(R.string.mamber_interface);
        } else if (faceState == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_AdminFace.getNumber()) {
            return cxt.getString(R.string.back_manage_interface);
        } else if (faceState == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_VoteFace.getNumber()) {
            return cxt.getString(R.string.vote_interface);
        }
        return "";
    }

    private String getDevType(int devId) {
        if ((devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_SERVICE) {
            return cxt.getString(R.string.meeting_tea_device);
        } else if ((devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_PROJECTIVE) {
            return cxt.getString(R.string.meeting_projector_device);
        } else if ((devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_CAPTURE) {
            return cxt.getString(R.string.meeting_stream_device);
        } else if ((devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_CLIENT) {
            return cxt.getString(R.string.meeting_terminal_device);
        } else if ((devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_VIDEO_CLIENT) {
            return cxt.getString(R.string.meet_video_client);
        } else if ((devId & Macro.DEVICE_MEET_ID_MASK) == Macro.DEVICE_MEET_PUBLISH) {
            return cxt.getString(R.string.meet_publish);
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox number_cb;
        TextView mamberTv;
        TextView nameTv;
        TextView devTypeTv;
        TextView devStateTv;
        TextView outDocumentTv;
        TextView interfaceStateTv;
        LinearLayout topView;

        ViewHolder(View view) {
            super(view);
            number_cb = view.findViewById(R.id.number_cb);
            mamberTv = view.findViewById(R.id.mamber_tv);
            nameTv = view.findViewById(R.id.name_tv);
            devTypeTv = view.findViewById(R.id.dev_type_tv);
            devStateTv = view.findViewById(R.id.dev_state_tv);
            outDocumentTv = view.findViewById(R.id.out_document_tv);
            interfaceStateTv = view.findViewById(R.id.interface_state_tv);
            topView = view.findViewById(R.id.top_view);
        }
    }
}
