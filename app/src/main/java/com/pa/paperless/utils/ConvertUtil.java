package com.pa.paperless.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;

/**
 * Created by xlk on 2018/11/1.
 * 转换工具类
 */

public class ConvertUtil {

    /**
     * 将bitmap转换成ByteString
     * @param bitmap
     * @return
     */
    public static ByteString bmp2bs(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] datas = baos.toByteArray();
        return ByteString.copyFrom(datas);
    }

    /**
     * 将ByteString数据转换成bitmap
     * @param bs com.google.protobuf.ByteString
     * @return bitmap
     */
    public static Bitmap bs2bmp(ByteString bs) {
        byte[] bytes = bs.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
        //byte[] bytes = picdata.toByteArray();
        //ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        //return BitmapFactory.decodeStream(inputStream);
    }

    /**
     * 将BitMap转为byte数组
     *
     * @param bitmap
     * @return
     */
    public static byte[] Bitmap2bytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * byte数组转bitmap
     *
     * @param bytes
     * @return
     */
    public static Bitmap bytes2Bitmap(byte[] bytes) {
        if (bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    /**
     * 将字符串转换成Bitmap类型
     * @param string
     * @return
     */
    public static Bitmap s2bmp(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 将Bitmap转换成字符串
     * @param bitmap
     * @return
     */
    public static String bmp2s(Bitmap bitmap) {
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes, Base64.DEFAULT);
        return string;
    }


}
