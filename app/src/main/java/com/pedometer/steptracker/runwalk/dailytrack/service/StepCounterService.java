package com.pedometer.steptracker.runwalk.dailytrack.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.activity.MainActivity;
import com.pedometer.steptracker.runwalk.dailytrack.model.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "step_counter_channel";
    private static final int NOTIFICATION_ID = 1001;

    // per-step constants used when cộng thêm delta từ sensor
    private static final double KCAL_PER_STEP = 0.04;
    private static final double KM_PER_STEP = 0.0008;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private DatabaseHelper databaseHelper;

    // cumulative steps from sensor
    private int totalSensorSteps = 0;
    // baseline (sensor value) at start of today
    private int baselineToday = 0;
    private boolean baselineInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification(0));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If device does not support STEP_COUNTER, stop service
        if (stepCounterSensor == null || sensorManager == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) return;

        totalSensorSteps = (int) event.values[0];

        // Initialize baseline for today if needed
        if (!baselineInitialized) {
            baselineToday = loadBaselineForToday();
            if (baselineToday == Integer.MIN_VALUE) {
                baselineToday = totalSensorSteps;
                saveBaselineForToday(baselineToday);
            }
            baselineInitialized = true;
        }

        int todaySteps = Math.max(0, totalSensorSteps - baselineToday);

        // Lấy bước hiện đang lưu trong DB
        DatabaseHelper.StepData todayData = databaseHelper.getTodayStepData();
        int currentDbSteps = todayData.steps;

        // Chỉ cộng thêm phần chênh lệch mới đo được từ sensor
        int extraSteps = Math.max(0, todaySteps - currentDbSteps);
        if (extraSteps > 0) {
            double extraCalories = extraSteps * KCAL_PER_STEP;
            double extraDistance = extraSteps * KM_PER_STEP;
            long extraTime = estimateTimeMillis(extraSteps); // ước lượng thời gian đi thêm

            databaseHelper.addToToday(extraSteps, extraCalories, extraDistance, extraTime);
        }

        // Update foreground notification
        Notification notification = buildNotification(todaySteps);
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // no-op
    }

    // ---------- Baseline helpers ----------

    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void saveBaselineForToday(int baseline) {
        getSharedPreferences("step_counter_baseline", MODE_PRIVATE)
                .edit()
                .putInt(getTodayKey(), baseline)
                .apply();
    }

    private int loadBaselineForToday() {
        return getSharedPreferences("step_counter_baseline", MODE_PRIVATE)
                .getInt(getTodayKey(), Integer.MIN_VALUE);
    }

    private long estimateTimeMillis(int steps) {
        // Simple heuristic: ~120 steps / minute => 0.5s per step
        return steps * 500L;
    }

    // ---------- Notification helpers ----------

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Step Counter";
            String description = "Counting your steps in background";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification(int todaySteps) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = getString(R.string.app_name);
        String text = "Today: " + todaySteps + " steps";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_steps)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
    }
}


