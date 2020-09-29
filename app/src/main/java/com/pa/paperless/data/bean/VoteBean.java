package com.pa.paperless.data.bean;

/**
 * Created by Administrator on 2017/11/14.
 */

public class VoteBean {
    String name;
    String choose;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChoose() {
        return choose;
    }

    public void setChoose(String choose) {
        this.choose = choose;
    }

    public VoteBean(String name, String choose) {

        this.name = name;
        this.choose = choose;
    }
}
