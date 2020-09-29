package com.pa.paperless.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/15 0015.
 * 实现网络请求的方式：HttpUrlConnection/HttpClient/VolleyokHttp...
 */

public class NetworkUtil {

    /**
     * 检查网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }
        return true;
    }

    /**
     * 通过HttpURLConnection实现一个get请求
     * @param urlStr
     * @param params
     * @return
     */
    public static String doGet(String urlStr, HashMap<String,String> params) {
        try {
            String paramsStr = parseParams(params);
            //拿到
            URL url = new URL(urlStr+"?"+paramsStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置POST请求
            conn.setRequestMethod("GET");

            //因为开关默认是关闭的，所以要先设置启动开关
            if(conn.getResponseCode()==200){
                //判断请求是否成功
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                return reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 通过HttpURLConnection实现一个post请求
     * @param urlStr
     * @param params
     * @return
     */
    public static String doPost(String urlStr, HashMap<String, String> params) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置POST请求
            conn.setRequestMethod("POST");
            //向服务器写数据（）写出去  params--->转换成paramsStr
            String paramsStr = parseParams(params);
            //因为开关默认是关闭的，所以要先设置启动开关
            conn.setDoOutput(true);
            conn.getOutputStream().write(paramsStr.getBytes());
            if(conn.getResponseCode()==200){
                //判断请求是否成功
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                return reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     *
     * @param params
     * @return
     */
    @NonNull
    private static String parseParams(HashMap<String, String> params) {
        String paramsStr = "";
        if(params==null||params.isEmpty()){
            return "";
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsStr += entry.getKey() + "=" + entry.getValue() + "&";
        }
        paramsStr.substring(0, paramsStr.length() - 1);
        return paramsStr;
    }


}
