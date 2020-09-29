package com.pa.paperless.data.bean;

import androidx.annotation.Nullable;

/**
 * @author by xlk
 * @date 2020/8/10 18:41
 * @desc
 */
public class PictureInfo implements Comparable<PictureInfo> {
    private int mediaId;
    private String filePath;

    public PictureInfo(int mediaId, String filePath) {
        this.mediaId = mediaId;
        this.filePath = filePath;
    }

    public int getMediaId() {
        return mediaId;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "PictureInfo{" +
                "mediaId=" + mediaId +
                ", filePath='" + filePath + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PictureInfo) {
            PictureInfo pictureInfo = (PictureInfo) obj;
            return this.filePath.equals(pictureInfo.filePath);
//            return this.mediaId == pictureInfo.mediaId && this.filePath.equals(pictureInfo.filePath);
        }
        return super.equals(obj);
    }

    @Override
    public int compareTo(PictureInfo o) {
        /**
         * compareTo()：大于0表示前一个数据比后一个数据大， 0表示相等，小于0表示前一个数据小于后一个数据
         * 相等时会走到equals()，这里讲姓名年龄都一样的对象当作一个对象
         */
        int i = mediaId - o.getMediaId();
        System.out.println("对比结果：" + i);
        return i;
    }
}
