package com.pa.paperless.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author by xlk
 * @date 2020/8/24 18:42
 * @desc SharedPreferences工具类
 * SharedPreferences 的创建模式：
 * MODE_PRIVATE：默认模式，该模式下创建的文件只能被当前应用或者与该应用具有相同SharedUserID的应用访问。
 * MODE_WORLD_READABLE：允许其他应用读取这个模式创建的文件。在Android N上使用该模式将抛出SecurityException异常。
 * MODE_WORLD_WRITEABLE：允许其他应用写入这个模式创建的文件。在Android N上使用该模式将抛出SecurityException异常。
 * MODE_APPEND：如果文件已经存在了，则在文件的尾部添加数据。
 * MODE_MULTI_PROCESS：SharedPreferences加载标志，当设置了该标志，则在磁盘上的文件将会被检查是否修改了，尽管SharedPreferences实例已经在该进程中被加载了。（鸡肋，不要用，推荐用ContentProvider）
 * <p>
 * apply 和 commit 的区别：
 * apply：异步执行，没有返回值
 * commit：同步执行，有返回值
 * 如果不考虑结果并且是在主线程执行推荐使用 apply；
 * 需要确保操作成功且有后续操作的话，用 commit()
 */
public class SpHelper {

    private final static String sp_name = "boling_paperless";
    private final SharedPreferences preferences;

    public SpHelper(Context context) {
        preferences = context.getSharedPreferences(sp_name, Context.MODE_PRIVATE);
    }

    public void save(String key, Object value) {
        SharedPreferences.Editor edit = preferences.edit();
        if (value instanceof Integer) {
            edit.putInt(key, (int) value);
        } else if (value instanceof String) {
            edit.putString(key, (String) value);
        } else if (value instanceof Float) {
            edit.putFloat(key, (float) value);
        } else if (value instanceof Long) {
            edit.putLong(key, (long) value);
        } else if (value instanceof Boolean) {
            edit.putBoolean(key, (boolean) value);
        }
        edit.apply();
    }


    public String readString(String key) {
        return preferences.getString(key, null);
    }

    public boolean readBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public int readInt(String key) {
        return preferences.getInt(key, -1);
    }

    public long readLong(String key) {
        return preferences.getLong(key, -1L);
    }

    public float readFloat(String key) {
        return preferences.getFloat(key, -1F);
    }
}
