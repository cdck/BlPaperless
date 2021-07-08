package com.pa.paperless.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.pa.paperless.data.constant.EventMessage;
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
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.pa.boling.paperless.R;
import com.pa.paperless.adapter.rvadapter.PostilMemberAdapter;
import com.pa.paperless.adapter.rvadapter.TypeFileAdapter;
import com.pa.paperless.data.bean.DevMember;
import com.pa.paperless.data.bean.MeetDirFileInfo;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;
import com.pa.paperless.utils.Dispose;
import com.pa.paperless.utils.FileUtil;
import com.pa.paperless.utils.MyUtils;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @date 2017/10/31
 * 查看批注
 */
public class PostilFragment extends BaseFragment implements View.OnClickListener {

    private final String TAG = "PostilFragment-->";
    private ListView mNotaLv;
    private TypeFileAdapter mAdapter;
    private Button mNotaPrepage, mNotaNextpage, mDocument, mPicture, download, push_file, postil_saved_offline_btn;
    private List<Button> mBtns;
    //所有批注文件
    private List<MeetDirFileInfo> meetDirFileInfos;
    //用于临时存放不同类型的文件
    private List<MeetDirFileInfo> mData = new ArrayList<>();
    private int page_count;
    private TextView mPageTv;
    private ListView mMemberLv;
    private PostilMemberAdapter memberAdapter;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos;
    private TextView default_tv;
    private String currentMemberName;//当前选中的参会人名称
    private List<Integer> permissionList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.right_notation, container, false);
        initView(inflate);
        mBtns = new ArrayList<>();
        mBtns.add(mDocument);
        mBtns.add(mPicture);
        fun_queryAttendPeople();
        return inflate;
    }

    private void fun_queryAttendPeople() {
        try {
            //92.查询参会人员
            InterfaceMember.pbui_Type_MemberDetailInfo o = jni.queryAttendPeople();
            if (o == null) return;
            if (memberInfos == null) memberInfos = new ArrayList<>();
            else memberInfos.clear();
            memberInfos.addAll(o.getItemList());
            LogUtil.e(TAG, "PostilFragment.receiveMemberInfo :  memberInfos.size --> " + memberInfos.size());
            for (int i = 0; i < memberInfos.size(); i++) {
                if (memberInfos.get(i).getPersonid() == Values.localMemberId) {
                    memberInfos.remove(i);
                    break;
                }
            }
            if (memberAdapter == null) {
                memberAdapter = new PostilMemberAdapter(getActivity(), memberInfos);
                mMemberLv.setAdapter(memberAdapter);
            } else {
                memberAdapter.notifyDataSetChanged();
            }
            /* **** **  查找到参会人数据后再查批注文件  ** **** */
            //143.查询会议目录文件（直接查询 批注文件(id 是固定为 2 )的文件）
            fun_queryMeetDirFile(Macro.ANNOTATION_FILE_DIRECTORY_ID);
            mMemberLv.setOnItemClickListener((parent, view, position, id) -> {
                memberAdapter.setCheck(position);
                LogUtil.e(TAG, "PostilFragment.onItemClick :  position --> " + position);
                default_tv.setSelected(false);
                InterfaceMember.pbui_Item_MemberDetailInfo member = memberInfos.get(position);
                currentMemberName = member.getName().toStringUtf8();
                int devId = jni.queryDeviceIdByMemberId(member.getPersonid());
                boolean isOnline = jni.deviceIsOnline(devId);
                if (devId == 0) {
                    ToastUtils.showShort(R.string.err_unbound_device);
                    return;
                } else if (!isOnline) {
                    ToastUtils.showShort(R.string.err_member_offline);
                    return;
                }
                //保存的权限集合中是否有当前设备同意的权限
                if (permissionList.contains(devId)) {
                    showCurrentFile(currentMemberName);
                } else {
                    ToastUtils.showShort(R.string.request_permission_now);
                    //发送请求查看批注文件权限
                    jni.sendAttendRequestPermissions(devId,
                            InterfaceMacro.Pb_MemberPermissionPropertyID.Pb_memperm_postilview.getNumber());
                }
            });

//            fun_queryMeetRanking();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

//    private void fun_queryMeetRanking() {
//        try {
//            InterfaceRoom.pbui_Type_MeetSeatDetailInfo object = jni.queryMeetRanking();
//            if (object == null) return;
//            if (memberInfos == null && memberInfos.isEmpty()) return;
//            List<InterfaceRoom.pbui_Item_MeetSeatDetailInfo> itemList = object.getItemList();
//            if (devMembers == null) devMembers = new ArrayList<>();
//            else devMembers.clear();
//            for (int i = 0; i < itemList.size(); i++) {
//                InterfaceRoom.pbui_Item_MeetSeatDetailInfo item = itemList.get(i);
//                int devid = item.getSeatid();
//                int memberid = item.getNameId();
//                for (int j = 0; j < memberInfos.size(); j++) {
//                    int personid = memberInfos.get(j).getPersonid();
//                    if (personid == memberid) {
//                        devMembers.add(new DevMember(memberInfos.get(j), devid));
//                    }
//                }
//            }
//            if (memberAdapter == null) {
//                memberAdapter = new PostilMemberAdapter(getActivity(), devMembers);
//                mMemberLv.setAdapter(memberAdapter);
//            } else {
//                memberAdapter.notifyDataSetChanged();
//            }
//            /* **** **  查找到参会人数据后再查批注文件  ** **** */
//            //143.查询会议目录文件（直接查询 批注文件(id 是固定为 2 )的文件）
//            fun_queryMeetDirFile(Macro.ANNOTATION_FILE_DIRECTORY_ID);
//            mMemberLv.setOnItemClickListener((parent, view, position, id) -> {
//                memberAdapter.setCheck(position);
//                LogUtil.e(TAG, "PostilFragment.onItemClick :  position --> " + position);
//                default_tv.setSelected(false);
//                DevMember devMember = devMembers.get(position);
//                currentMemberName = devMember.getMemberDetailInfo().getName().toStringUtf8();
//                int devId = devMember.getDevId();
//                //保存的权限集合中是否有当前设备同意的权限
//                if (permissionList.contains(devId)) {
//                    showCurrentFile(currentMemberName);
//                } else {
//                    ToastUtils.showShort(R.string.request_permission_now);
//                    //发送请求查看批注文件权限
//                    jni.sendAttendRequestPermissions(devId,
//                            InterfaceMacro.Pb_MemberPermissionPropertyID.Pb_memperm_postilview.getNumber());
//                }
//            });
//        } catch (InvalidProtocolBufferException e) {
//            e.printStackTrace();
//        }
//    }

    private void fun_queryMeetDirFile(int dirid) {
        InterfaceFile.pbui_Type_MeetDirRightDetailInfo dirPermission = jni.queryDirPermission(dirid);
        if (dirPermission != null && dirPermission.getMemberidList().contains(Values.localMemberId)) {
            LogUtils.e("没有批注文件的目录权限");
            clear();
            return;
        }
        try {
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo object = jni.queryMeetDirFile(dirid);
            if (object == null) {
                clear();
                return;
            }
            LogUtil.e(TAG, "PostilFragment.receiveMeetDirFile :  object.getDirid() --> " + object.getDirid());
            if (meetDirFileInfos == null) meetDirFileInfos = new ArrayList<>();
            else meetDirFileInfos.clear();
            meetDirFileInfos.addAll(Dispose.MeetDirFile(object));
            if (!meetDirFileInfos.isEmpty()) {
                ClickBtn(true);//有数据将按钮设置成可点击状态
                if (mData == null) mData = new ArrayList<>();
                else mData.clear();
                for (int i = 0; i < meetDirFileInfos.size(); i++) {
                    MeetDirFileInfo documentBean = meetDirFileInfos.get(i);
                    mData.add(documentBean);
                }
                if (mAdapter == null) {
                    mAdapter = new TypeFileAdapter(getContext(), mData);
                    mNotaLv.setAdapter(mAdapter);
                }
                mAdapter.notifyDataSetChanged();
                mAdapter.PAGE_NOW = 0;
                updatePageTv();
                checkButton();
                setSelect(0);
                mAdapter.setLookListener((fileInfo, posion, filename, filesize) -> {
                    /*if(!MyUtils.isHasPermission(Macro.permission_code_download)){
                        ToastUtils.showShort(R.string.no_permission);
                        return;
                    }*/
//                    String dir = getActivity().getCacheDir().getAbsolutePath();
                    //Macro.CACHE_FILE
                    FileUtil.openFile(Macro.CACHE_ALL_FILE, filename, jni, posion, getContext(), filesize);
                });
                //item选中事件
                mAdapter.setItemSelectListener((posion, view) -> {
                    mAdapter.setCheck(mData.get(posion).getMediaId());
                });
            } else {
                clear();
                ClickBtn(false);
            }
            default_tv.performClick();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            //会议目录权限变更通知
            case EventType.directory_permission_change_inform: {
                InterfaceBase.pbui_MeetNotifyMsg object = (InterfaceBase.pbui_MeetNotifyMsg) message.getObject();
                int id = object.getId();
                if (id == Macro.ANNOTATION_FILE_DIRECTORY_ID) {
                    fun_queryMeetDirFile(id);
                }
                break;
            }
            case EventType.MEETDIR_FILE_CHANGE_INFORM://142 会议目录文件变更通知
                InterfaceBase.pbui_MeetNotifyMsgForDouble object1 = (InterfaceBase.pbui_MeetNotifyMsgForDouble) message.getObject();
                int id = object1.getId();
                if (id == Macro.ANNOTATION_FILE_DIRECTORY_ID) {
                    fun_queryMeetDirFile(id);
                }
                break;
            case EventType.MEMBER_CHANGE_INFORM://参会人员变更通知
                fun_queryAttendPeople();
                break;
//            case EventType.MeetSeat_Change_Inform://会议排位变更通知
//                fun_queryMeetRanking();
//                break;
            case EventType.RECEIVE_REQUEST_REPLY://收到权限申请的回复
                InterfaceDevice.pbui_Type_MeetRequestPrivilegeResponse object = (InterfaceDevice.pbui_Type_MeetRequestPrivilegeResponse) message.getObject();
                int returncode = object.getReturncode();
                int deviceid = object.getDeviceid();
                int memberid = object.getMemberid();
                LogUtil.e(TAG, "PostilFragment.getEventMessage :  收到权限申请的回复 --> returncode= " + returncode + ", deviceid= " + deviceid + ", memberid= " + memberid);
                if (returncode == 1) {//查看批注文件权限有了
                    permissionList.add(deviceid);
                    for (int i = 0; i < memberInfos.size(); i++) {
                        InterfaceMember.pbui_Item_MemberDetailInfo memberInfo = memberInfos.get(i);
                        if (memberInfo.getPersonid() == memberid) {
                            if (currentMemberName.equals(memberInfo.getName().toStringUtf8())) {
                                ToastUtils.showShort(R.string.agreed_postilview, memberInfo.getName().toStringUtf8());
                                showCurrentFile(memberInfo.getName().toStringUtf8());
                            }
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < memberInfos.size(); i++) {
                        InterfaceMember.pbui_Item_MemberDetailInfo memberInfo = memberInfos.get(i);
                        if (memberInfo.getPersonid() == memberid) {
                            ToastUtils.showShort(R.string.reject_postilview, memberInfo.getName());
                            break;
                        }
                    }
                }
                break;
        }
    }

    //展示当前选中参会人的批注文件
    private void showCurrentFile(String memberName) {
        LogUtil.d(TAG, "showCurrentFile -->" + "展示当前选中参会人的批注文件");
        /** **** **  有了参会人数据，不一定就有批注文件数据，所以要判空  ** **** **/
        if (mAdapter != null && meetDirFileInfos != null && mData != null) {
            LogUtil.d(TAG, "showCurrentFile -->" + "展示当前选中参会人的批注文件 memberName= " + memberName);
            mData.clear();
            for (int i = 0; i < meetDirFileInfos.size(); i++) {
                MeetDirFileInfo meetDirFileInfo = meetDirFileInfos.get(i);
                String uploader_name = meetDirFileInfo.getUploader_name();
                if (uploader_name.equals(memberName)) {
                    mData.add(meetDirFileInfo);
                }
            }
            mAdapter.notifyDataSetChanged();
            mAdapter.PAGE_NOW = 0;
            updatePageTv();
            setSelect(0);
            checkButton();
        }
    }

    private void clear() {
        if (meetDirFileInfos != null)
            meetDirFileInfos.clear();
        if (mData != null)
            mData.clear();
        if (mAdapter != null) {
            LogUtil.e(TAG, "PostilFragment.clear :  清空批注文件数据 --> ");
            mAdapter.notifyDataSetChanged();
        }
    }

    private void ClickBtn(boolean b) {
        mDocument.setClickable(b);
        mPicture.setClickable(b);
    }

    private void setSelect(int index) {
        for (int i = 0; i < mBtns.size(); i++) {
            if (index == i) {
                mBtns.get(i).setSelected(true);
//                mBtns.get(i).setTextColor(Color.WHITE);
            } else {
                mBtns.get(i).setSelected(false);
//                mBtns.get(i).setTextColor(Color.BLACK);
            }
        }
    }

    private void initView(View inflate) {
        default_tv = inflate.findViewById(R.id.default_tv);
        default_tv.setText(Values.localMemberName);
        mNotaLv = inflate.findViewById(R.id.nota_lv);
        mMemberLv = inflate.findViewById(R.id.member_lv);
        mPageTv = inflate.findViewById(R.id.page_tv);
        mAdapter = new TypeFileAdapter(getContext(), mData);
        mNotaLv.setAdapter(mAdapter);
//        //点击查看
//        mAdapter.setLookListener((fileInfo, posion, filename, filesize) -> FileUtil.openFile(Macro.POSTIL_FILE, filename, nativeUtil, posion, getContext(), filesize));
        mNotaPrepage = inflate.findViewById(R.id.nota_prepage);
        mNotaNextpage = inflate.findViewById(R.id.nota_nextpage);
        push_file = inflate.findViewById(R.id.push_file);
        download = inflate.findViewById(R.id.download);
        postil_saved_offline_btn = inflate.findViewById(R.id.postil_saved_offline_btn);
        postil_saved_offline_btn.setOnClickListener(this);
        download.setOnClickListener(this);
        push_file.setOnClickListener(this);
        default_tv.setOnClickListener(this);
        mNotaPrepage.setOnClickListener(this);
        mNotaNextpage.setOnClickListener(this);
        mDocument = inflate.findViewById(R.id.document);
        mDocument.setOnClickListener(this);
        mPicture = inflate.findViewById(R.id.picture);
        mPicture.setOnClickListener(this);
    }

    /**
     * 根据媒体ID获取选中的文件
     */
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
        if (mAdapter == null || meetDirFileInfos == null)
            return;
        switch (v.getId()) {
            case R.id.default_tv:
                default_show();
                break;
            case R.id.nota_prepage:
                prePage();
                break;
            case R.id.nota_nextpage:
                nextPage();
                break;
            case R.id.document:
                if (meetDirFileInfos != null) {
                    mData.clear();
                    for (int i = 0; i < meetDirFileInfos.size(); i++) {
                        MeetDirFileInfo meetDirFileInfo = meetDirFileInfos.get(i);
                        String uploader_name = meetDirFileInfo.getUploader_name();
                        if (FileUtil.isDocument(meetDirFileInfo.getFileName())) {
                            if (uploader_name.equals(currentMemberName)) {
                                mData.add(meetDirFileInfo);
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    mAdapter.PAGE_NOW = 0;
                    updatePageTv();
                    setSelect(0);
                    checkButton();
                }
                break;
            case R.id.picture:
                if (meetDirFileInfos != null) {
                    mData.clear();
                    for (int i = 0; i < meetDirFileInfos.size(); i++) {
                        MeetDirFileInfo meetDirFileInfo = meetDirFileInfos.get(i);
                        String uploader_name = meetDirFileInfo.getUploader_name();
                        if (FileUtil.isPicture(meetDirFileInfo.getFileName())) {
                            if (uploader_name.equals(currentMemberName)) {
                                mData.add(meetDirFileInfo);
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    mAdapter.PAGE_NOW = 0;
                    updatePageTv();
                    setSelect(1);
                    checkButton();
                }
                break;
            case R.id.postil_saved_offline_btn://离线缓存
//                if(!MyUtils.isHasPermission(Macro.permission_code_download)){
//                    ToastUtils.showShort(R.string.no_permission);
//                    return;
//                }
                if (mAdapter == null) break;
                MeetDirFileInfo data = mAdapter.getCheckedFile();
                if (data == null) {
                    ToastUtils.showShort(R.string.please_choose_downloadfile);
                    break;
                }
                FileUtil.downOfflineFile(data);
                break;
            case R.id.download:
                if (!MyUtils.isHasPermission(Macro.permission_code_download)) {
                    ToastUtils.showShort(R.string.no_permission);
                    return;
                }
                if (mAdapter == null) break;
                MeetDirFileInfo data1 = mAdapter.getCheckedFile();
                if (data1 == null) {
                    ToastUtils.showShort(R.string.please_choose_downloadfile);
                    break;
                }
                FileUtil.downloadFile(data1, Macro.POSTIL_FILE);
                break;
            case R.id.push_file://文件推送
                if (mAdapter == null) break;
                MeetDirFileInfo data2 = mAdapter.getCheckedFile();
                if (data2 == null) {
                    ToastUtils.showShort(R.string.please_choose_file);
                    break;
                }
                EventBus.getDefault().post(new EventMessage(EventType.INFORM_PUSH_FILE, data2.getMediaId()));
                break;
        }
    }

    private void default_show() {
        default_tv.setSelected(true);
        currentMemberName = Values.localMemberName;
        if (mAdapter != null && meetDirFileInfos != null && mData != null) {
            memberAdapter.setCheck(-1);
            mData.clear();
            for (int i = 0; i < meetDirFileInfos.size(); i++) {
                MeetDirFileInfo meetDirFileInfo = meetDirFileInfos.get(i);
                String uploader_name = meetDirFileInfo.getUploader_name();
                if (uploader_name.equals(currentMemberName)) {
                    mData.add(meetDirFileInfo);
                }
            }
            mAdapter.notifyDataSetChanged();
            mAdapter.PAGE_NOW = 0;
            updatePageTv();
            setSelect(0);
            checkButton();
        }
    }

    //下一页
    private void nextPage() {
        mAdapter.PAGE_NOW++;
        mAdapter.notifyDataSetChanged();
        updatePageTv();
        checkButton();
    }

    //上一页
    private void prePage() {
        mAdapter.PAGE_NOW--;
        mAdapter.notifyDataSetChanged();
        updatePageTv();
        checkButton();
    }

    private void updatePageTv() {
        if (mData == null || mAdapter == null) return;
        int count = mData.size();
        LogUtil.e(TAG, "PostilFragment.updataPageTv :   --> " + mData.size() + " ，getCount： " + mAdapter.getCount());
        if (count != 0) {
            if (count % mAdapter.ITEM_COUNT == 0)
                page_count = mData.size() / mAdapter.ITEM_COUNT;
            else
                page_count = (mData.size() / mAdapter.ITEM_COUNT) + 1;
            mPageTv.setText(mAdapter.PAGE_NOW + 1 + " / " + page_count);
        } else {
            mPageTv.setText(mAdapter.PAGE_NOW + " / " + count);
        }
    }

    //设置两个按钮是否可用
    public void checkButton() {
        if (mData == null || mAdapter == null) return;
        //如果页码已经是第一页了
        if (mAdapter.PAGE_NOW <= 0) {
            mNotaPrepage.setEnabled(false);
            //如果不设置的话，只要进入一次else if ，那么下一页按钮就一直是false，不可点击状态
            if (mData.size() > mAdapter.ITEM_COUNT) {
                mNotaNextpage.setEnabled(true);
            } else {
                mNotaNextpage.setEnabled(false);
            }
        }
        //值的长度减去前几页的长度，剩下的就是这一页的长度，如果这一页的长度比View_Count小，表示这是最后的一页了，后面在没有了。
        else if (mData.size() - mAdapter.PAGE_NOW * mAdapter.ITEM_COUNT <= mAdapter.ITEM_COUNT) {
            mNotaNextpage.setEnabled(false);
            mNotaPrepage.setEnabled(true);
        } else {
            //否则两个按钮都设为可用
            mNotaPrepage.setEnabled(true);
            mNotaNextpage.setEnabled(true);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtil.i("F_life", "PostilFragment.onHiddenChanged :   --->>> " + hidden);
        if (!hidden) {
            fun_queryMeetDirFile(Macro.ANNOTATION_FILE_DIRECTORY_ID);
        }
    }
}
