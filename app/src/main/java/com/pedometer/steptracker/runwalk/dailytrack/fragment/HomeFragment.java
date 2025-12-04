package com.pedometer.steptracker.runwalk.dailytrack.fragment;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Color;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.activity.MainActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.StepGoalActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.WeightActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.nativefull.ActivityFullCallback;
import com.pedometer.steptracker.runwalk.dailytrack.activity.nativefull.ActivityLoadNativeFullV2;
import com.pedometer.steptracker.runwalk.dailytrack.model.DatabaseHelper;
import com.pedometer.steptracker.runwalk.dailytrack.model.WeightHistoryHelper;
import com.pedometer.steptracker.runwalk.dailytrack.utils.ArcProgressView;
import com.pedometer.steptracker.runwalk.dailytrack.utils.CustomBottomSheetDialogExitFragment;
import com.pedometer.steptracker.runwalk.dailytrack.utils.CustomCircularProgressBar;
import com.pedometer.steptracker.runwalk.dailytrack.utils.ProfileDataManager;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 45;
    private static final int DEFAULT_GOAL = 6000;
    private static final double KCAL_PER_STEP = 0.04;
    private static final double KM_PER_STEP = 0.0008;
    private static final float STEP_THRESHOLD = 5.0f;
    private static final int STEP_DELAY_MS = 250;
    private static final int PEAK_COUNT = 4;
    private static final float DIRECTION_THRESHOLD = 1.0f;

    private ArcProgressView arcView;
    private FrameLayout badgeStar;

    private boolean isFirstLoad = true;


    private TextView stepCountText, targetText, remainingText;
    private TextView kcalText, timeText, distanceText;
    private TextView goalStepsText;
    private ImageView startStopButtonCircle;
    private ImageView editGoalButton;
    private LinearLayout viewReportButton;

    private View progressBar; // Đổi từ ProgressBar -> View
    private CustomCircularProgressBar customProgressBar;
    private ImageView settingDailyStep;
    private boolean isTracking = false;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor stepCounterSensor;
    private boolean stepSensorIsCounter = false;
    private boolean stepSensorIsDetector = false;

    private int stepCount = 0;
    private int detectorAccumulatedSteps = 0;
    private int stepGoal;
    private long startTime;
    private long elapsedTime = 0;
    private DatabaseHelper databaseHelper;
    private Runnable timeUpdater;
    private ImageView mondayGoal, tuesdayGoal, wednesdayGoal, thursdayGoal, fridayGoal, saturdayGoal, sundayGoal;
    private TextView stepsTextView, kcalTextView, kmTextView, hoursTextView;
    private TextView tvCurrentWeight, tvWeightGoal;
    private LinearLayout weightSection;

    private FrameLayout frAds;

    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private long lastStepTime = 0;
    private float[] lastValues = new float[PEAK_COUNT];
    private int valueIndex = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable delayedLoadExpandTask;

    private Runnable loadTask = new Runnable() {
        @Override
        public void run() {
            loadNativeExpnad();
            handler.postDelayed(this, 10000);
        }
    };

    // Ads & utils
    private FrameLayout frAdsHomeTop;
    private FrameLayout frAdsCollap;
    private SharePreferenceUtils sharePreferenceUtils;
    private View rootView;

    private int totalSensorSteps = 0;
    private int stepsBaselineForToday = 0;
    private boolean waitingForFirstCounterEvent = false;

    private static final String PREFS_BASELINE = "pedometer_baselines";

    // animation
    private ValueAnimator progressAnimator;
    private int lastAnimatedProgress = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.home_activity, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharePreferenceUtils = new SharePreferenceUtils(requireContext());
        sharePreferenceUtils.incrementCounter();

        initializeViews();
        setupSensor();
        setupClickListeners();
        loadTodayData();
        setupTimeUpdater();
        checkAndRequestPermissions();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                CustomBottomSheetDialogExitFragment dialog = CustomBottomSheetDialogExitFragment.newInstance();
                dialog.show(getParentFragmentManager(), "ExitDialog");
            }
        });
    }

    private void initializeViews() {
        stepCountText = rootView.findViewById(R.id.stepCountText);
        targetText = rootView.findViewById(R.id.targetText);
        remainingText = rootView.findViewById(R.id.remainingText);
        kcalText = rootView.findViewById(R.id.kcalText);
        timeText = rootView.findViewById(R.id.timeText);
        distanceText = rootView.findViewById(R.id.distanceText);
        progressBar = rootView.findViewById(R.id.progressBar);
        settingDailyStep = rootView.findViewById(R.id.settingsDailyStep);

        frAds = rootView.findViewById(R.id.frAds);
        frAdsHomeTop = rootView.findViewById(R.id.frAdsHomeTop);
        frAdsCollap = rootView.findViewById(R.id.frAdsCollap);

        mondayGoal = rootView.findViewById(R.id.dayMonday);
        tuesdayGoal = rootView.findViewById(R.id.dayTuesday);
        wednesdayGoal = rootView.findViewById(R.id.dayWednesday);
        thursdayGoal = rootView.findViewById(R.id.dayThursday);
        fridayGoal = rootView.findViewById(R.id.dayFriday);
        saturdayGoal = rootView.findViewById(R.id.daySaturday);
        sundayGoal = rootView.findViewById(R.id.daySunday);

        stepsTextView = rootView.findViewById(R.id.stepsTextView);
        hoursTextView = rootView.findViewById(R.id.hoursTextView);
        kcalTextView = rootView.findViewById(R.id.kcalTextView);
        kmTextView = rootView.findViewById(R.id.kmTextView);
        tvCurrentWeight = rootView.findViewById(R.id.tvCurrentWeight);
        tvWeightGoal = rootView.findViewById(R.id.tvWeightGoal);
        weightSection = rootView.findViewById(R.id.weightSection);

        arcView = rootView.findViewById(R.id.arcView);
        if (arcView != null) arcView.setSidePaddingDp(50f);

        badgeStar = rootView.findViewById(R.id.badgeStar);

        if (arcView != null && badgeStar != null) {
            arcView.setBadgeView(badgeStar);
        }

        startStopButtonCircle = rootView.findViewById(R.id.startStopButton);
        editGoalButton = rootView.findViewById(R.id.editGoalButton);
        viewReportButton = rootView.findViewById(R.id.viewReportButton);
        goalStepsText = rootView.findViewById(R.id.goalStepsText);

        stepGoal = getStepGoalForToday();

        // ensure progressBar set up
        progressBar = rootView.findViewById(R.id.progressBar);
        if (progressBar instanceof CustomCircularProgressBar) {
            customProgressBar = (CustomCircularProgressBar) progressBar;
        }

        if (customProgressBar != null) {
            customProgressBar.setProgress(0);
        } else if (progressBar instanceof ProgressBar) {
            ProgressBar pb = (ProgressBar) progressBar;
            pb.setMax(stepGoal);
            pb.setProgress(0);
            pb.setIndeterminate(false);
        }
        // =============================

        updateTargetText();
        databaseHelper = new DatabaseHelper(requireContext());

        updateDailyGoals();
        showMonthlyReport();
        setupWeightSection();
    }

    private void setupWeightSection() {
        if (weightSection != null) {

            weightSection.setOnClickListener(v -> {

                if (!SharePreferenceUtils.isOrganic(requireActivity())) {
                    Admob.getInstance().loadAndShowInter((AppCompatActivity) getContext(),getString(R.string.inter_steps), 0 , 30000, new InterCallback() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            ActivityLoadNativeFullV2.open(getActivity(), getString(R.string.native_full_inter_steps), new ActivityFullCallback() {
                                @Override
                                public void onResultFromActivityFull() {
                                    Intent intent = new Intent(requireContext(), WeightActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            ActivityLoadNativeFullV2.open(getActivity(), getString(R.string.native_full_inter_steps), new ActivityFullCallback() {
                                @Override
                                public void onResultFromActivityFull() {
                                    Intent intent = new Intent(requireContext(), WeightActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    } );

                } else {
                    Intent intent = new Intent(requireContext(), WeightActivity.class);
                    startActivity(intent);
                }

            });
        }
        float prevCurrent = 0f;
        if (arcView != null) {
            try {
                prevCurrent = arcView.getCurrentWeight();
            } catch (Exception ignored) {
                prevCurrent = 0f;
            }
        } else {
            try {
                prevCurrent = Float.parseFloat(tvCurrentWeight.getText().toString());
            } catch (Exception ignored) {
                prevCurrent = 0f;
            }
        }

        float weight = ProfileDataManager.getWeight(requireContext());
        float weightGoal = ProfileDataManager.getWeightGoal(requireContext());


        if (weightGoal <= 0f) {
            if (weight > 0f) {
                weightGoal = weight;
            } else {
                weightGoal = 100f;
            }
        }

        tvWeightGoal.setText(String.format(Locale.getDefault(), "%.1fkg", weightGoal));


        if (Float.isNaN(weight)) weight = 0f;
        if (Float.isNaN(weightGoal) || weightGoal <= 0f) weightGoal = 100f;

        if (arcView != null) {
            arcView.setGoalWeight(weightGoal);

            prevCurrent = Math.max(0f, prevCurrent);
            weight = Math.max(0f, weight);

            boolean hasPrev = prevCurrent > 0f && prevCurrent != weight;

            if (!hasPrev) {
                arcView.setCurrentWeight(weight);
                tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f", weight));
            } else {
                arcView.animateCurrentWeight(prevCurrent, weight, 700);
                animateCurrentWeightText(prevCurrent, weight, 700);
            }
        } else {
            tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f", weight));
        }

    }

    private void animateCurrentWeightText(float from, float to, long duration) {
        ValueAnimator numberAnim = ValueAnimator.ofFloat(from, to);
        numberAnim.setDuration(duration);
        numberAnim.addUpdateListener(anim -> {
            float v = (float) anim.getAnimatedValue();
            tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f", v));
        });
        numberAnim.start();
    }




    private void loadNativeCollap(@Nullable final Runnable onLoaded) {
        if (!isAdded() || getContext() == null) return;

        if (frAdsHomeTop != null) {
            frAdsHomeTop.removeAllViews();
        }

        Admob.getInstance().loadNativeAd(requireContext(), getString(R.string.native_collap_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                if (getContext() == null || !isAdded() || nativeAd == null) {
                    return;
                }

                NativeAdView adView = (NativeAdView) LayoutInflater.from(requireContext()).inflate(R.layout.layout_native_home_collap, null);
                if (frAdsCollap != null) {
                    frAdsCollap.removeAllViews();
                    frAdsCollap.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                if (onLoaded != null) {
                    onLoaded.run();
                }
            }

            @Override
            public void onAdFailedToLoad() {
                if (!isAdded() || getContext() == null) return;

                if (frAdsCollap != null) {
                    frAdsCollap.removeAllViews();
                }

                if (onLoaded != null) {
                    onLoaded.run();
                }
            }
        });
    }

    private void loadNativeExpnad() {
        if (!isAdded()) return;
        Context context = requireContext();

        Admob.getInstance().loadNativeAd(context, getString(R.string.native_expand_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                if (!isAdded()) return;

                Context context = requireContext();
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
                if (isAdded()) {
                    frAdsHomeTop.removeAllViews();
                }
            }
        });
    }

    private int getStepGoalForToday() {
        Calendar calendar = Calendar.getInstance();
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String today = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];

        SharedPreferences prefs = requireContext().getSharedPreferences("StepGoals", Context.MODE_PRIVATE);
        return prefs.getInt(today, DEFAULT_GOAL);
    }

    private void updateDailyGoals() {
        SharedPreferences prefs = requireContext().getSharedPreferences("StepGoals", Context.MODE_PRIVATE);
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
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        String monthStr = String.format(Locale.getDefault(), "%04d-%02d", year, month);

        Cursor cursor = db.rawQuery("SELECT * FROM " + "steps" + " WHERE " + "date" + " LIKE ?",
                new String[]{monthStr + "%"});

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

        stepsTextView.setText(totalSteps + " Steps");
        kcalTextView.setText(String.format(Locale.getDefault(), "%.1f Kcal", totalCalories));
        kmTextView.setText(String.format(Locale.getDefault(), "%.2f Km", totalDistance));
        hoursTextView.setText(formatMillisToHMS(totalTime));
    }


    private String formatMillisToHMS(long totalMillis) {
        long totalSeconds = totalMillis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }


    private void setupSensor() {
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            showSensorNotAvailableDialog();
            return;
        }

        Sensor counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (counter != null) {
            stepCounterSensor = counter;
            stepSensorIsCounter = true;
            stepSensorIsDetector = false;
            Log.d("HomeFragment", "Using TYPE_STEP_COUNTER");
            return;
        }

        Sensor detector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (detector != null) {
            stepCounterSensor = detector;
            stepSensorIsCounter = false;
            stepSensorIsDetector = true;
            Log.d("HomeFragment", "Using TYPE_STEP_DETECTOR");
            return;
        }

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            showSensorNotAvailableDialog();
        } else {
            Log.d("HomeFragment", "Using accelerometer fallback");
        }
    }

    private void setupClickListeners() {
        if (startStopButtonCircle != null) {
            startStopButtonCircle.setOnClickListener(v -> {
                isTracking = !isTracking;
                if (isTracking) {
                    startStepTracking();
                    startStopButtonCircle.setImageResource(R.drawable.ic_pause);
                } else {
                    stopStepTracking();
                    startStopButtonCircle.setImageResource(R.drawable.ic_play);

                }
            });
        }


        settingDailyStep.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), StepGoalActivity.class);
            startActivity(intent);
        });

        if (editGoalButton != null) {
            editGoalButton.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), StepGoalActivity.class);
                startActivity(intent);
            });
        }

        if (viewReportButton != null) {
            viewReportButton.setOnClickListener(v -> {
                if (requireActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).loadFragment(com.pedometer.steptracker.runwalk.dailytrack.utils.BottomNavigationHelper.NAV_REPORT);
                }
            });
        }

        rootView.findViewById(R.id.details_report).setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).loadFragment(com.pedometer.steptracker.runwalk.dailytrack.utils.BottomNavigationHelper.NAV_REPORT);
            }
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
        ensureBaselineForToday();

        startTime = System.currentTimeMillis() - elapsedTime;

        if (stepSensorIsCounter || stepSensorIsDetector) {
            if (stepCounterSensor != null) {
                sensorManager.registerListener(stepListener, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
            }
        } else if (accelerometer != null) {
            sensorManager.registerListener(stepListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            showSensorNotAvailableDialog();
            return;
        }

        handler.post(timeUpdater);
    }


    private void stopStepTracking() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepListener);
        }
        handler.removeCallbacks(timeUpdater);

        saveCurrentData();
    }

    private final SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                totalSensorSteps = (int) event.values[0];

                if (waitingForFirstCounterEvent) {
                    stepsBaselineForToday = totalSensorSteps;
                    saveBaselineForToday(stepsBaselineForToday);
                    waitingForFirstCounterEvent = false;
                }

                stepCount = Math.max(0, totalSensorSteps - getBaselineForToday());
                updateUI();
            } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                for (float v : event.values) {
                    if (v == 1.0f) {
                        detectorAccumulatedSteps++;
                    }
                }
                stepCount = detectorAccumulatedSteps;
                updateUI();
            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                detectStep(event);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
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
                detectorAccumulatedSteps++;
                lastStepTime = currentTime;
                stepCount = detectorAccumulatedSteps;
                updateUI();
            }
        }
    }

    private boolean isStepPattern() {
        float sum = 0;
        int count = 0;
        for (float v : lastValues) {
            if (v != 0f) {
                sum += v;
                count++;
            }
        }
        if (count < PEAK_COUNT) return false;

        float avg = sum / count;

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
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {

            // ====== 1. UPDATE STEP TEXT ======
            if (stepCountText != null) {
                stepCountText.setText(String.valueOf(stepCount));
            }

            // ====== 2. ANIMATE PROGRESS ======
            int targetProgress = Math.min(stepCount, stepGoal);

            // Chỉ animate nếu có thay đổi
            if (targetProgress != lastAnimatedProgress) {
                animateProgress(lastAnimatedProgress, targetProgress);
                lastAnimatedProgress = targetProgress;
            }

            // ====== 3. UPDATE TARGET (6000 steps, etc.) ======
            updateTargetText();

            // ====== 4. REMAINING STEPS ======
            if (remainingText != null) {
                int remainingSteps = Math.max(0, stepGoal - stepCount);
                remainingText.setText(
                        getString(R.string.remaining_steps_format, remainingSteps)
                );
            }

            // ====== 5. KCAL & DISTANCE ======
            double kcal = stepCount * KCAL_PER_STEP;
            double distance = stepCount * KM_PER_STEP;

            if (kcalText != null) {
                kcalText.setText(String.format(Locale.getDefault(), "%.2f", kcal));
            }

            if (distanceText != null) {
                distanceText.setText(String.format(Locale.getDefault(), "%.2f", distance));
            }

            // ====== 6. TIME DISPLAY ======
            updateTimeDisplay();
        });
    }


    /**
     * Animate progressBar from -> to (values are steps count, not percent).
     * Cancels previous animator if running.
     */
    // Trong animateProgress()
    private void animateProgress(int from, int to) {
        if (progressBar == null) return;

        if (customProgressBar != null) {
            // Dùng custom progress bar
            float fromPercent = (from * 100f) / stepGoal;
            float toPercent = (to * 100f) / stepGoal;

            if (progressAnimator != null && progressAnimator.isRunning()) {
                progressAnimator.cancel();
            }

            progressAnimator = ValueAnimator.ofFloat(fromPercent, toPercent);
            progressAnimator.setDuration(500);
            progressAnimator.addUpdateListener(animation -> {
                float val = (float) animation.getAnimatedValue();
                customProgressBar.setProgress(val);
            });
            progressAnimator.start();
        } else if (progressBar instanceof ProgressBar) {
            // Dùng ProgressBar thông thường
            ProgressBar pb = (ProgressBar) progressBar;
            from = Math.max(0, Math.min(pb.getMax(), from));
            to = Math.max(0, Math.min(pb.getMax(), to));

            if (progressAnimator != null && progressAnimator.isRunning()) {
                progressAnimator.cancel();
            }

            progressAnimator = ValueAnimator.ofInt(from, to);
            progressAnimator.setDuration(500);
            progressAnimator.addUpdateListener(animation -> {
                int val = (int) animation.getAnimatedValue();
                pb.setProgress(val);
            });
            progressAnimator.start();
        }

        lastAnimatedProgress = to;
    }

    private void updateTargetText() {
        if (targetText != null) {
            targetText.setText(String.format(Locale.getDefault(), "%d/%d steps", stepCount, stepGoal));
        }
        if (goalStepsText != null) {
            goalStepsText.setText(String.format(Locale.getDefault(), "%d steps", stepGoal));
        }
    }

    private void updateTimeDisplay() {
        timeText.setText(String.format("%02d:%02d:%02d",
                elapsedTime / 3600000,
                (elapsedTime / 60000) % 60,
                (elapsedTime / 1000) % 60));
    }


    private void loadTodayData() {
        if (stepSensorIsCounter) {
            stepsBaselineForToday = getBaselineForToday();
            if (stepsBaselineForToday == Integer.MIN_VALUE) {
                waitingForFirstCounterEvent = true;
                stepCount = 0;
            } else {
                DatabaseHelper.StepData todayData = databaseHelper.getTodayStepData();
                stepCount = Math.max(todayData.steps, 0);
            }
        } else {
            DatabaseHelper.StepData todayData = databaseHelper.getTodayStepData();
            stepCount = todayData.steps;
            elapsedTime = todayData.time;
        }

        stepGoal = getStepGoalForToday();

        if (customProgressBar != null) {
            float initialPercent = (stepCount * 100f) / stepGoal;
            customProgressBar.setProgress(initialPercent);
            lastAnimatedProgress = stepCount;
        } else if (progressBar instanceof ProgressBar) {
            ProgressBar pb = (ProgressBar) progressBar;
            pb.setMax(stepGoal);
            lastAnimatedProgress = Math.min(stepCount, stepGoal);
            pb.setProgress(lastAnimatedProgress);
        }

        updateTargetText();
        updateUI();
    }


    private void saveCurrentData() {
        if (stepSensorIsCounter) {
            saveBaselineForToday(getBaselineForToday() == Integer.MIN_VALUE
                    ? stepsBaselineForToday
                    : getBaselineForToday());
        }

        DatabaseHelper.StepData todayData = databaseHelper.getTodayStepData();
        int extraSteps = stepCount - todayData.steps;
        long extraTime = elapsedTime - todayData.time;

        if (extraSteps < 0) extraSteps = 0;
        if (extraTime < 0) extraTime = 0;

        if (extraSteps > 0 || extraTime > 0) {
            databaseHelper.addToToday(
                    Math.max(0, extraSteps),
                    Math.max(0, extraSteps * KCAL_PER_STEP),
                    Math.max(0, extraSteps * KM_PER_STEP),
                    Math.max(0L, extraTime)
            );
        }
    }

    private void showSensorNotAvailableDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sensor Not Available")
                .setMessage("Accelerometer / Step sensor is not available on this device.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isTracking) {
            stopStepTracking();
        }
        saveCurrentData();
        handler.removeCallbacks(loadTask);
        if (delayedLoadExpandTask != null) {
            handler.removeCallbacks(delayedLoadExpandTask);
            delayedLoadExpandTask = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodayData();
        setupWeightSection();
        updateTargetText();
        if (isTracking) {
            startStepTracking();
            if (startStopButtonCircle != null) {
                startStopButtonCircle.setImageResource(R.drawable.ic_pause);
            }
        } else {
            if (startStopButtonCircle != null) {
                startStopButtonCircle.setImageResource(R.drawable.ic_play);
            }
        }

        if (!SharePreferenceUtils.isOrganic(requireContext())) {
            if (isFirstLoad) {
                loadNativeCollap(() -> {
                    delayedLoadExpandTask = new Runnable() {
                        @Override
                        public void run() {
                            loadNativeExpnad();
                            isFirstLoad = false;
                        }
                    };
                    handler.postDelayed(delayedLoadExpandTask, 1000);
                });
            } else {
                loadNativeCollap(() -> {
                    delayedLoadExpandTask = new Runnable() {
                        @Override
                        public void run() {
                            loadNativeExpnad();
                        }
                    };
                    handler.postDelayed(delayedLoadExpandTask, 10000);
                });
            }
        } else {
            frAdsCollap.removeAllViews();
            frAdsHomeTop.removeAllViews();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(timeUpdater);
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
            } else {
            }
        } else {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupSensor();
            } else {
                Toast.makeText(requireContext(), "Ứng dụng cần quyền theo dõi hoạt động để đếm bước chân",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    private String getTodayKey() {
        Calendar c = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.getTime());
    }

    private void saveBaselineForToday(int baseline) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_BASELINE, Context.MODE_PRIVATE);
        prefs.edit().putInt(getTodayKey(), baseline).apply();
    }

    private int getBaselineForToday() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_BASELINE, Context.MODE_PRIVATE);
        if (!prefs.contains(getTodayKey())) return Integer.MIN_VALUE;
        return prefs.getInt(getTodayKey(), Integer.MIN_VALUE);
    }


    private void ensureBaselineForToday() {
        if (!stepSensorIsCounter) return;

        int saved = getBaselineForToday();
        if (saved == Integer.MIN_VALUE) {
            if (totalSensorSteps > 0) {
                stepsBaselineForToday = totalSensorSteps;
                saveBaselineForToday(stepsBaselineForToday);
                waitingForFirstCounterEvent = false;
            } else {
                waitingForFirstCounterEvent = true;
            }
        } else {
            stepsBaselineForToday = saved;
            waitingForFirstCounterEvent = false;
        }
    }

}
