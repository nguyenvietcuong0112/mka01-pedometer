package com.pedometer.steptracker.runwalk.dailytrack.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    private static ToastUtils instance;
    private Toast toast;

    public static ToastUtils getInstance(Context context) {
        if (instance == null) {
            instance = new ToastUtils(context);
        }
        return instance;
    }

    private ToastUtils(Context context) {
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public void showToast(String message) {
        toast.setText(message);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void release() {
        if (instance != null) {
            instance = null;
        }
    }
}
