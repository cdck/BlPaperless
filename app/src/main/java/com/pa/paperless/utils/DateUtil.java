package com.pa.paperless.utils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Administrator on 2017/12/26.
 */

public class DateUtil {


    /**
     * 将秒数转换成 00:04:58  用于倒计时
     * 时间小于1小时显示分秒，显示样式 00:20
     * 时间大于1小时显示时分秒，显示样式 01:11:12
     * %02d 就是说长度不够2位的时候前面补0，主要是解决05:00这样的显示问题, 不进行补0的话会出现5:0的结果
     *
     * @param seconds 秒数
     * @return
     */
    public static String formatSeconds(long seconds) {
        String standardTime;
        if (seconds <= 0) {
            standardTime = "00:00";
        } else if (seconds < 60) {
            standardTime = String.format(Locale.getDefault(), "00:%02d", seconds % 60);
        } else if (seconds < 3600) {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        } else {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
        }
        return standardTime;
    }


    /**
     * 秒数转化为日期
     */
    public static String formatSecond2Date(long seconds, String pattern) {
        Date date = new Date();
        try {
            date.setTime(seconds * 1000);
        } catch (NumberFormatException nfe) {

        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static String getTim(long time) {
        Date date = new Date(time);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(date);
    }

    /**
     * 转换成时间格式   8:30
     *
     * @param time
     * @return
     */
    public static String getTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
        return timeFormat.format(date);
    }

    /**
     * 转成时分秒 00::00:00
     *
     * @param ms 单位：毫秒
     * @return
     */
    public static String convertTime(long ms) {
        String ret = "";
        Date date = new Date(ms);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
        ret = timeFormat.format(date);
        return ret;
    }

    /**
     * Android 音乐播放器应用里，读出的音乐时长为 long 类型以毫秒数为单位，例如：将 234736 转化为分钟和秒应为 03:55 （包含四舍五入）
     *
     * @param duration 音乐时长
     * @return
     */
    public static String timeParse(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round((float) seconds / 1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }

    //time 单位 秒
    public static String convertTime(int sec) {
        return convertTime((long) sec * 1000);
    }

    /**
     * day :01月01日  week:  星期四   time:  08:00
     * 大写HH是24小时制，小写hh是12小时制
     *
     * @param time 时间 单位秒
     * @return eg: 2021-2-23 14:06:17
     */
    public static String getSignInTime(long time) {
        Date date = new Date(time * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = dateFormat.format(date);
        return dateTime;
    }

    /**
     * @param time 单位 毫秒
     *             时区设置：SimpleDateFormat对象.setTimeZone(TimeZone.getTimeZone("GTM"));
     * @return
     */
    public static String[] getGTMDate(long time) {

        Date tTime = new Date(time);

        SimpleDateFormat day = new SimpleDateFormat("MM月dd日");
        day.setTimeZone(TimeZone.getTimeZone("GTM"));
        String dayt = day.format(tTime);

        SimpleDateFormat week = new SimpleDateFormat("EEEE");//只有一个E 则解析出来是 周几，4个E则是星期几
        week.setTimeZone(TimeZone.getTimeZone("GTM"));
        String weekt = week.format(tTime);

        SimpleDateFormat tim = new SimpleDateFormat("HH:mm");
        tim.setTimeZone(TimeZone.getTimeZone("GTM"));
        String timt = tim.format(tTime);

        String[] date = {dayt, weekt, timt};
        return date;
    }
}
