package com.pa.paperless.utils;

import android.widget.Toast;

import com.mogujie.tt.protobuf.InterfaceMacro;
import com.pa.boling.paperless.R;

import static com.pa.paperless.service.ShotApplication.applicationContext;

/**
 * @author xlk
 * @date 2019/8/19
 */
public class ToastUtil {

    private static Toast toast;
    private static long oneTime;

    public static void showToast(String msg) {
        try {
            LogUtil.d("ToastUtil", "showToast： " + msg);
            if (toast == null) {
                toast = Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG);
                toast.show();
                oneTime = System.currentTimeMillis();
            } else {
                if (System.currentTimeMillis() - oneTime >= 3000) {
                    toast.cancel();
                    toast = Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG);
                    toast.show();
                    oneTime = System.currentTimeMillis();
                } else {
                    toast.setText(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showToast(int resId) {
        showToast(applicationContext.getString(resId));
    }

    public static void showToast(int resId, Object... values) {
        showToast(applicationContext.getString(resId, values));
    }

    //初始化返回结果提示
    public static void initResultToast(int code) {
        String msg;
        switch (code) {
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_NONE_VALUE:
                msg = applicationContext.getString(R.string.error_0);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_EXPIRATION_VALUE:
                msg = applicationContext.getString(R.string.error_1);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_OPER_VALUE:
                msg = applicationContext.getString(R.string.error_2);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_ENTERPRISE_VALUE:
                msg = applicationContext.getString(R.string.error_3);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_NODEVICEID_VALUE:
                msg = applicationContext.getString(R.string.error_4);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_NOALLOWIN_VALUE:
                msg = applicationContext.getString(R.string.error_5);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_FILEERROR_VALUE:
                msg = applicationContext.getString(R.string.error_6);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_INVALID_VALUE:
                msg = applicationContext.getString(R.string.error_7);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_IDOCCUPY_VALUE:
                msg = applicationContext.getString(R.string.error_8);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_NOTBEING_VALUE:
                msg = applicationContext.getString(R.string.error_9);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_ONLYDEVICEID_VALUE:
                msg = applicationContext.getString(R.string.error_10);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_DEVICETYPENOMATCH_VALUE:
                msg = applicationContext.getString(R.string.error_11);
                break;
            default:
                msg = "返回码：" + code;
                break;
        }
        if (!msg.isEmpty()) {
            showToast(msg);
        }
    }
}
