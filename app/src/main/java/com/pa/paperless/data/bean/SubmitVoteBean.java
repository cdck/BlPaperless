package com.pa.paperless.data.bean;

/**
 * 提交投票结果
 * Created by Administrator on 2018/3/2.
 */

public class SubmitVoteBean {
    int voteid;//投票ID
    int selcnt;//有效选项数
    int selectItem;//选择的项 0x00000001 选择了第一项 0x00000002第二项 对应项位置1表示选择 |高8位参见 PB_VOTE_SELFLAG_CHECKIN

    public int getVoteid() {
        return voteid;
    }

    public void setVoteid(int voteid) {
        this.voteid = voteid;
    }

    public int getSelcnt() {
        return selcnt;
    }

    public void setSelcnt(int selcnt) {
        this.selcnt = selcnt;
    }

    public int getSelectItem() {
        return selectItem;
    }

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    public SubmitVoteBean(int voteid, int selcnt, int selectItem) {

        this.voteid = voteid;
        this.selcnt = selcnt;
        this.selectItem = selectItem;
    }
}
