package com.pa.paperless.utils;

import android.content.Context;
import android.content.DialogInterface;

import com.pa.boling.paperless.R;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by xlk on 2019/11/30.
 */
public class DiaLogUtil {
    public static AlertDialog createDialog(Context cxt, String title, DiaLogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
        builder.setTitle(title);
        builder.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.ensure(dialog);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.cancel(dialog);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }


    public static AlertDialog createDialog(Context cxt, String title, String ensure, String cancel, DiaLogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
        builder.setTitle(title);
        builder.setPositiveButton(ensure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.ensure(dialog);
            }
        });
        builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.cancel(dialog);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }


    public interface DiaLogListener {
        void ensure(DialogInterface dialog);

        void cancel(DialogInterface dialog);
    }
}
