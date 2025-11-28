package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.model.DatabaseHelper;
import com.pedometer.steptracker.runwalk.dailytrack.utils.BottomNavigationHelper;
import com.pedometer.steptracker.runwalk.dailytrack.utils.CustomBottomSheetDialogExitFragment;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;

import java.util.Calendar;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 45;

    private static final int DEFAULT_GOAL = 6000;
    private static final double KCAL_PER_STEP = 0.04;
    private static final double KM_PER_STEP = 0.0008;
    private static final float STEP_THRESHOLD = 5.0f;
    private static final int STEP_DELAY_MS = 250;
    private static final int PEAK_COUNT = 4;
    private static final float DIRECTION_THRESHOLD = 1.0f;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isFirstLoad = true;

    private Runnable loadTask = new Runnable() {
        @Override
        public void run() {
            loadNativeExpnad();
            handler.postDelayed(this, 15000);
        }
    };

    private TextView stepCountText, targetText, remainingText;
    private TextView kcalText, timeText, distanceText;
    private LinearLayout startStopButton;

    private ProgressBar progressBar;
    private ImageButton settingsButton;
    private ImageView settingDailyStep;

    private boolean isTracking = false;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int stepCount = 0;
    private long startTime;
    private long elapsedTime = 0;
    private DatabaseHelper databaseHelper;
    private Runnable timeUpdater;
    private ImageView mondayGoal, tuesdayGoal, wednesdayGoal, thursdayGoal, fridayGoal, saturdayGoal, sundayGoal;

    private  TextView stepsTextView,kcalTextView,kmTextView,hoursTextView;

    private TextView tvPlay;
    private FrameLayout frAds,frAdsBanner;
    private ImageView icPlay;

    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private long lastStepTime = 0;

    private float[] lastValues = new float[PEAK_COUNT];
    private int valueIndex = 0;
    private int stepGoal;
    FrameLayout frAdsHomeTop;

    FrameLayout frAdsCollap;
    private SharePreferenceUtils sharePreferenceUtils;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        sharePreferenceUtils = new SharePreferenceUtils(this);
        sharePreferenceUtils.incrementCounter();

        initializeViews();
        setupSensor();
        setupClickListeners();
        loadTodayData();
        setupTimeUpdater();
        checkAndRequestPermissions();
        BottomNavigationHelper.setupBottomNavigation(this, BottomNavigationHelper.NAV_STEPS);


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                CustomBottomSheetDialogExitFragment dialog = CustomBottomSheetDialogExitFragment.newInstance();
                dialog.show(getSupportFragmentManager(), "ExitDialog");
            }
        });
    }




    private void initializeViews() {
        stepCountText = findViewById(R.id.stepCountText);
        targetText = findViewById(R.id.targetText);
        remainingText = findViewById(R.id.remainingText);
        kcalText = findViewById(R.id.kcalText);
        timeText = findViewById(R.id.timeText);
        distanceText = findViewById(R.id.distanceText);
        startStopButton = findViewById(R.id.startStopButton);
        progressBar = findViewById(R.id.progressBar);
        settingsButton = findViewById(R.id.settingsButton);
        settingDailyStep = findViewById(R.id.settingsDailyStep);

        frAds = findViewById(R.id.frAds);
        frAdsBanner = findViewById(R.id.fr_ads_banner);
        frAdsHomeTop = findViewById(R.id.frAdsHomeTop);
        frAdsCollap = findViewById(R.id.frAdsCollap);

        mondayGoal = findViewById(R.id.dayMonday);
        tuesdayGoal = findViewById(R.id.dayTuesday);
        wednesdayGoal = findViewById(R.id.dayWednesday);
        thursdayGoal = findViewById(R.id.dayThursday);
        fridayGoal = findViewById(R.id.dayFriday);
        saturdayGoal = findViewById(R.id.daySaturday);
        sundayGoal = findViewById(R.id.daySunday);

        stepsTextView = findViewById(R.id.stepsTextView);
        hoursTextView = findViewById(R.id.hoursTextView);
        kcalTextView = findViewById(R.id.kcalTextView);
        kmTextView = findViewById(R.id.kmTextView);

        tvPlay = findViewById(R.id.tv_play);
        icPlay = findViewById(R.id.ic_play);

        stepGoal = getStepGoalForToday();
        progressBar.setMax(stepGoal);
        targetText.setText(getString(R.string.target_steps_format, stepGoal));
        databaseHelper = new DatabaseHelper(this);

        updateDailyGoals();
        showMonthlyReport();
    }

    private void loadNativeBanner() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.ad_native_admob_banner_1, null);
                frAdsBanner.setVisibility(View.VISIBLE);
                frAdsBanner.removeAllViews();
                frAdsBanner.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                frAdsBanner.setVisibility(View.GONE);
            }
        });
    }




    private void loadNativeCollap(@Nullable final Runnable onLoaded) {
        Log.d("Truowng", "loadNativeCollapA: ");
        frAdsHomeTop.removeAllViews();
        Admob.getInstance().loadNativeAd(getApplicationContext(), getString(R.string.native_collap_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = (NativeAdView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_native_home_collap, null);
                frAdsCollap.removeAllViews();
                frAdsCollap.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                if (onLoaded != null) {
                    onLoaded.run();
                }

            }

            @Override
            public void onAdFailedToLoad() {
                frAdsCollap.removeAllViews();
                if (onLoaded != null) {
                    onLoaded.run();
                }
            }
        });
    }


    private void loadNativeExpnad() {

        Log.d("Truong", "loadNativeCollapB: ");
        Context context = getApplicationContext();

        Admob.getInstance().loadNativeAd(context, getString(R.string.native_expand_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {

                Context context = getApplicationContext();
                NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.layout_native_home_expnad, null);

                frAdsHomeTop.removeAllViews();

                MediaView mediaView = adView.findViewById(R.id.ad_media);
                ImageView closeButton = adView.findViewById(R.id.close);
                closeButton.setOnClickListener(v -> {
                    mediaView.performClick();
                });

                Log.d("Truong", "onNativeAdLoaded: ");
                frAdsHomeTop.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                frAdsHomeTop.removeAllViews();
            }
        });
    }

    private int getStepGoalForToday() {
        Calendar calendar = Calendar.getInstance();
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String today = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];

        SharedPreferences prefs = getSharedPreferences("StepGoals", MODE_PRIVATE);
        return prefs.getInt(today, DEFAULT_GOAL);
    }

    private void updateDailyGoals() {
        SharedPreferences prefs = getSharedPreferences("StepGoals", MODE_PRIVATE);
        Calendar calendar = Calendar.getInstance();
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK);

        updateGoalStatus(sundayGoal, prefs.getInt("Sunday", DEFAULT_GOAL), Calendar.SUNDAY, todayIndex);
        updateGoalStatus(mondayGoal, prefs.getInt("Monday", DEFAULT_GOAL), Calendar.MONDAY, todayIndex);
        updateGoalStatus(tuesdayGoal, prefs.getInt("Tuesday", DEFAULT_GOAL), Calendar.TUESDAY, todayIndex);
        updateGoalStatus(wednesdayGoal, prefs.getInt("Wednesday", DEFAULT_GOAL), Calendar.WEDNESDAY, todayIndex);
        updateGoalStatus(thursdayGoal, prefs.getInt("Thursday", DEFAULT_GOAL), Calendar.THURSDAY, todayIndex);
        updateGoalStatus(fridayGoal, prefs.getInt("Friday", DEFAULT_GOAL), Calendar.FRIDAY, todayIndex);
        updateGoalStatus(saturdayGoal, prefs.getInt("Saturday", DEFAULT_GOAL), Calendar.SATURDAY, todayIndex);
    }
    private void updateGoalStatus(ImageView dayView, int goal, int dayIndex, int todayIndex) {
        if (dayIndex < todayIndex) {
            if (stepCount >= goal) {
                dayView.setImageResource(R.drawable.ic_circle_checked);
            } else {
                dayView.setImageResource(R.drawable.ic_circle_failed);
            }
        } else if (dayIndex == todayIndex) {
            dayView.setImageResource(R.drawable.ic_circle_calendar);
        } else {
            dayView.setImageResource(R.drawable.ic_circle_disabled);
        }
    }

    private void showMonthlyReport() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH đếm từ 0

        String monthStr = String.format(Locale.getDefault(), "%04d-%02d", year, month);

        Cursor cursor = db.rawQuery("SELECT * FROM " + "steps" + " WHERE " + "date" + " LIKE ?",
                new String[]{monthStr + "%"}); // tìm các ngày có tiền tố là "2025-03-%"

        int totalSteps = 0;
        double totalCalories = 0;
        double totalDistance = 0;
        long totalTime = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                totalSteps += cursor.getInt(cursor.getColumnIndexOrThrow("steps"));
                totalCalories += cursor.getDouble(cursor.getColumnIndexOrThrow("calories"));
                totalDistance += cursor.getDouble(cursor.getColumnIndexOrThrow("distance"));
                totalTime += cursor.getLong(cursor.getColumnIndexOrThrow("time"));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();

        // Hiển thị lên UI
        stepsTextView.setText(totalSteps + " Steps");
        kcalTextView.setText(String.format(Locale.getDefault(), "%.1f Kcal", totalCalories));
        kmTextView.setText(String.format(Locale.getDefault(), "%.2f Km", totalDistance));
        hoursTextView.setText(formatSecondsToHMS(totalTime));
    }

    private String formatSecondsToHMS(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void setupSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
                showSensorNotAvailableDialog();
            }
        } else {
            showSensorNotAvailableDialog();
        }
    }

    private void setupClickListeners() {
        startStopButton.setOnClickListener(v -> {
            isTracking = !isTracking;
            if (isTracking) {
                startStepTracking();
                tvPlay.setText(R.string.stop);
                icPlay.setImageResource(R.drawable.ic_pause);
            } else {
                stopStepTracking();
                tvPlay.setText(R.string.continue_text);
                icPlay.setImageResource(R.drawable.ic_play);
            }
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        });

        settingDailyStep.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StepGoalActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.details_report).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DetailsReportActivity.class);
            startActivity(intent);
        });
    }

    private void setupTimeUpdater() {
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    elapsedTime = System.currentTimeMillis() - startTime;
                    updateTimeDisplay();
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void startStepTracking() {
        if (accelerometer != null) {
            startTime = System.currentTimeMillis() - elapsedTime;
            sensorManager.registerListener(stepListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            handler.post(timeUpdater);
        } else {
            showSensorNotAvailableDialog();
        }
    }

    private void stopStepTracking() {
        if (accelerometer != null) {
            sensorManager.unregisterListener(stepListener);
            handler.removeCallbacks(timeUpdater);
            saveCurrentData();
        }
    }

    private final SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                detectStep(event);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void detectStep(SensorEvent event) {
        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        float acceleration = (float) Math.sqrt(
                linear_acceleration[0] * linear_acceleration[0] +
                        linear_acceleration[1] * linear_acceleration[1] +
                        linear_acceleration[2] * linear_acceleration[2]
        );

        lastValues[valueIndex] = acceleration;
        valueIndex = (valueIndex + 1) % PEAK_COUNT;

        long currentTime = System.currentTimeMillis();

        if (isStepPattern() && (currentTime - lastStepTime) > STEP_DELAY_MS) {
            float verticalRatio = Math.abs(linear_acceleration[1]) /
                    (Math.abs(linear_acceleration[0]) + Math.abs(linear_acceleration[2]) + 0.1f);

            if (verticalRatio > DIRECTION_THRESHOLD) {
                stepCount++;
                lastStepTime = currentTime;
                updateUI();
            }
        }
    }

    private boolean isStepPattern() {
        if (valueIndex < 3) return false;

        float sum = 0;
        for (float value : lastValues) {
            sum += value;
        }
        float avg = sum / PEAK_COUNT;

        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        for (float value : lastValues) {
            if (value > max) max = value;
            if (value < min) min = value;
        }

        float amplitude = max - min;

        return amplitude > STEP_THRESHOLD;
    }

    private void updateUI() {
        runOnUiThread(() -> {
            stepCountText.setText(String.valueOf(stepCount));
            progressBar.setProgress(stepCount);

            int remainingSteps = Math.max(0, stepGoal - stepCount);
            remainingText.setText(getString(R.string.remaining_steps_format, remainingSteps));

            double kcal = stepCount * KCAL_PER_STEP;
            double distance = stepCount * KM_PER_STEP;

            kcalText.setText(String.format("%.2f", kcal));
            distanceText.setText(String.format("%.2f", distance));

            updateTimeDisplay();
        });
    }

    private void updateTimeDisplay() {
        timeText.setText(String.format("%02d:%02d:%02d",
                elapsedTime / 3600000,
                (elapsedTime / 60000) % 60,
                (elapsedTime / 1000) % 60));
    }

    private void loadTodayData() {
        DatabaseHelper.StepData todayData = databaseHelper.getTodayStepData();
        stepCount = todayData.steps;
        elapsedTime = todayData.time;
        stepGoal = getStepGoalForToday();
        progressBar.setMax(stepGoal);
        targetText.setText(getString(R.string.target_steps_format, stepGoal));
        updateUI();
    }

    private void saveCurrentData() {
        databaseHelper.saveStepData(
                stepCount,
                stepGoal,
                stepCount * KCAL_PER_STEP,
                stepCount * KM_PER_STEP,
                elapsedTime
        );
    }

    private void showSensorNotAvailableDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sensor Not Available")
                .setMessage("Accelerometer is not available on this device.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTracking) {
            stopStepTracking();
        }
        saveCurrentData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNativeBanner();
        loadTodayData();
        if (isTracking) {
            startStepTracking();
        }

        if (!SharePreferenceUtils.isOrganic(this)) {
            if (isFirstLoad) {
                loadNativeCollap(() -> handler.postDelayed(() -> {
                    loadNativeExpnad();
                    handler.postDelayed(loadTask, 15000);
                    isFirstLoad = false;
                }, 1000));
            } else {
                loadNativeCollap(null);
                handler.postDelayed(loadTask, 15000);
            }
        } else {
            frAdsHomeTop.removeAllViews();
            frAdsCollap.removeAllViews();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timeUpdater);
    }

    private void checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
            } else {
                setupSensor();
            }
        } else {
            setupSensor();
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupSensor();
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền theo dõi hoạt động để đếm bước chân",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


}