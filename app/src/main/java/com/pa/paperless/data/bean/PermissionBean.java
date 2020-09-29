package com.pa.paperless.data.bean;

import com.mogujie.tt.protobuf.InterfaceMember;

/**
 * Created by xlk on 2019/7/27.
 */
public class PermissionBean {
    InterfaceMember.pbui_Item_MemberDetailInfo memberInfo;
    int permission;

    public PermissionBean(InterfaceMember.pbui_Item_MemberDetailInfo memberInfo, int permission) {
        this.memberInfo = memberInfo;
        this.permission = permission;
    }

    public InterfaceMember.pbui_Item_MemberDetailInfo getMemberInfo() {
        return memberInfo;
    }

    public int getPermission() {
        return permission;
    }
}
