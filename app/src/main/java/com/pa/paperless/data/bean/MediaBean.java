package com.pa.paperless.data.bean;

/**
 * Created by xlk on 2019/9/12.
 */
public class MediaBean {
    byte[] bytes;
    int size;
    long pts;
    int iskeyframe;

    public MediaBean(byte[] bytes, int size, long pts, int iskeyframe) {
        this.bytes = bytes;
        this.size = size;
        this.pts = pts;
        this.iskeyframe = iskeyframe;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getSize() {
        return size;
    }

    public long getPts() {
        return pts;
    }

    public int getIskeyframe() {
        return iskeyframe;
    }
}
