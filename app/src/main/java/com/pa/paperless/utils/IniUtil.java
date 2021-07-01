package com.pa.paperless.utils;

import com.blankj.utilcode.util.LogUtils;
import com.pa.paperless.data.constant.Macro;

import org.ini4j.Config;
import org.ini4j.Ini;

import java.io.File;

/**
 * @author xlk
 * @date 2019/7/23
 * @desc ini文件操作
 */
public class IniUtil {
    private final String TAG = "IniUtil-->";
    private static IniUtil instance = new IniUtil();
    public static File inifile = new File(Macro.iniFilePath);
    private Ini ini;
    private File file = null;

    private IniUtil() {
        ini = new Ini();
        Config config = new Config();
        //不允许出现重复的部分和选项
        config.setMultiSection(false);
        config.setMultiOption(false);
        ini.setConfig(config);
    }

    public static IniUtil getInstance() {
        return instance;
    }

    public boolean load(File file) {
        if (this.file != null) return true;
        try {
            ini.load(file);
            this.file = file;
            LogUtils.i(TAG, "成功加载ini文件： " + file.exists());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String get(String sectionName, String optionName) {
        if (ini == null || file == null) return null;
        return ini.get(sectionName, optionName);
    }

    public void put(String sectionName, String optionName, Object value) {
        if (ini != null && file != null) {
            ini.put(sectionName, optionName, value);
        }
    }

    public void store() {
        if (file != null && ini != null) {
            try {
                ini.store(file);
                LogUtils.file("成功提交到ini");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
