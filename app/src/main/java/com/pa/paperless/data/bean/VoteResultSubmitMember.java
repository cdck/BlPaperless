package com.pa.paperless.data.bean;



/**
 * Created by xlk on 2018/10/17.
 */

public class VoteResultSubmitMember {
    int memberId;
    String memberName;
    String optionStr;


    public VoteResultSubmitMember(int memberId, String memberName, String optionStr) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.optionStr = optionStr;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getOptionStr() {
        return optionStr;
    }

    public void setOptionStr(String optionStr) {
        this.optionStr = optionStr;
    }
}
