package com.pedometer.steptracker.runwalk.dailytrack.fragment;

import android.Manifest;
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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.activity.MainActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.StepGoalActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.WeightActivity;
import com.pedometer.steptracker.runwalk.dailytrack.model.DatabaseHelper;
import com.pedometer.steptracker.runwalk.dailytrack.utils.CustomBottomSheetDialogExitFragment;
import com.pedometer.steptracker.runwalk.dailytrack.utils.ProfileDataManager;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * HomeFragment - updated: prefer TYPE_STEP_COUNTER, fallback accelerometer,
 * baseline stored per-day to avoid jumps, reset at finish.
 */
public class HomeFragment extends Fragment {

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

    // UI
    private TextView stepCountText, targetText, remainingText;
    private TextView kcalText, timeText, distanceText;
    private TextView goalStepsText;
    private ImageButton startStopButton;
    private ImageButton startStopButtonCircle;
    private ImageButton editGoalButton;
    private LinearLayout viewReportButton;
    private ProgressBar progressBar;
    private ImageView settingDailyStep;
    private boolean isTracking = false;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor stepCounterSensor; // TYPE_STEP_COUNTER or STEP_DETECTOR
    private boolean stepSensorIsCounter = false; // true: TYPE_STEP_COUNTER
    private boolean stepSensorIsDetector = false; // true: TYPE_STEP_DETECTOR

    // Step bookkeeping
    private int stepCount = 0; // value shown on UI (today/session)
    private int detectorAccumulatedSteps = 0; // for STEP_DETECTOR/accelerometer fallback
    private int stepGoal;
    private long startTime;
    private long elapsedTime = 0;
    private DatabaseHelper databaseHelper;
    private Runnable timeUpdater;
    private ImageView mondayGoal, tuesdayGoal, wednesdayGoal, thursdayGoal, fridayGoal, saturdayGoal, sundayGoal;
    private TextView stepsTextView, kcalTextView, kmTextView, hoursTextView;
    private TextView tvCurrentWeight, tvWeightGoal;
    private LinearLayout weightSection;

    private FrameLayout frAds, frAdsBanner;

    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private long lastStepTime = 0;
    private float[] lastValues = new float[PEAK_COUNT];
    private int valueIndex = 0;

    // Ads & utils
    private FrameLayout frAdsHomeTop;
    private FrameLayout frAdsCollap;
    private SharePreferenceUtils sharePreferenceUtils;
    private View rootView;

    // Step counter cumulative handling
    private int totalSensorSteps = 0; // cumulative from sensor event (TYPE_STEP_COUNTER)
    private int stepsBaselineForToday = 0; // baseline cumulative value when the day/session started
    private boolean waitingForFirstCounterEvent = false;

    private static final String PREFS_BASELINE = "pedometer_baselines"; // store baseline per date
    private static final String PREFS_TODAY_DATA = "pedometer_today_data"; // optional local storage

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
        setupSensor(); // detect available sensors
        setupClickListeners();
        loadTodayData(); // load DB saved or baseline-based steps
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
        startStopButton = rootView.findViewById(R.id.startStopButton);
        progressBar = rootView.findViewById(R.id.progressBar);
        settingDailyStep = rootView.findViewById(R.id.settingsDailyStep);

        frAds = rootView.findViewById(R.id.frAds);
        frAdsBanner = rootView.findViewById(R.id.fr_ads_banner);
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


        startStopButtonCircle = rootView.findViewById(R.id.startStopButton);
        editGoalButton = rootView.findViewById(R.id.editGoalButton);
        viewReportButton = rootView.findViewById(R.id.viewReportButton);
        goalStepsText = rootView.findViewById(R.id.goalStepsText);

        stepGoal = getStepGoalForToday();
        progressBar.setMax(stepGoal);
        updateTargetText();
        databaseHelper = new DatabaseHelper(requireContext());

        updateDailyGoals();
        showMonthlyReport();
        setupWeightSection();
    }

    private void setupWeightSection() {
        float weight = ProfileDataManager.getWeight(requireContext());
        float weightGoal = ProfileDataManager.getWeightGoal(requireContext());

        if (tvCurrentWeight != null) {
            if (weight > 0) {
                tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f", weight));
            } else {
                tvCurrentWeight.setText("0.0");
            }
        }

        if (tvWeightGoal != null) {
            if (weightGoal > 0) {
                tvWeightGoal.setText(String.format(Locale.getDefault(), "Goal: %.1fkg", weightGoal));
            } else {
                tvWeightGoal.setText("Goal: 0.0kg");
            }
        }

        if (weightSection != null) {
            weightSection.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), WeightActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadNativeBanner() {
        Admob.getInstance().loadNativeAd(requireContext(), getString(R.string.native_banner_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(requireContext()).inflate(R.layout.ad_native_admob_banner_1, null);
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
        Admob.getInstance().loadNativeAd(requireContext(), getString(R.string.native_collap_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = (NativeAdView) LayoutInflater.from(requireContext()).inflate(R.layout.layout_native_home_collap, null);
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
        Context context = requireContext();

        Admob.getInstance().loadNativeAd(context, getString(R.string.native_expand_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
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
                frAdsHomeTop.removeAllViews();
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

    /**
     * totalMillis: thời gian lưu trong DB đang là milli‑seconds,
     * cần đổi sang giây trước khi format HH:mm:ss.
     */
    private String formatMillisToHMS(long totalMillis) {
        long totalSeconds = totalMillis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Detect available sensors. Prefer TYPE_STEP_COUNTER. If not available, fallback to accelerometer.
     */
    private void setupSensor() {
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            showSensorNotAvailableDialog();
            return;
        }

        // Try STEP_COUNTER first
        Sensor counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (counter != null) {
            stepCounterSensor = counter;
            stepSensorIsCounter = true;
            stepSensorIsDetector = false;
            // We will register listener only when tracking, or when we need live updates
            Log.d("HomeFragment", "Using TYPE_STEP_COUNTER");
            return;
        }

        // Try STEP_DETECTOR
        Sensor detector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (detector != null) {
            stepCounterSensor = detector;
            stepSensorIsCounter = false;
            stepSensorIsDetector = true;
            Log.d("HomeFragment", "Using TYPE_STEP_DETECTOR");
            return;
        }

        // Fallback: accelerometer algorithm
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            showSensorNotAvailableDialog();
        } else {
            Log.d("HomeFragment", "Using accelerometer fallback");
        }
    }

    private void setupClickListeners() {
        // Circular play/pause button
        if (startStopButtonCircle != null) {
            startStopButtonCircle.setOnClickListener(v -> {
                isTracking = !isTracking;
                if (isTracking) {
                    startStepTracking();
                    startStopButtonCircle.setImageResource(R.drawable.ic_pause);
                    startStopButtonCircle.setColorFilter(null); // Clear tint for pause icon (it has built-in color)
                } else {
                    stopStepTracking();
                    startStopButtonCircle.setImageResource(R.drawable.ic_play);
                    startStopButtonCircle.setColorFilter(new PorterDuffColorFilter(0xFF5F7DED, PorterDuff.Mode.SRC_IN)); // Set blue tint for play icon
                }
            });
        }



        settingDailyStep.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), StepGoalActivity.class);
            startActivity(intent);
        });

        // Edit goal button (circular button at bottom of circle)
        if (editGoalButton != null) {
            editGoalButton.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), StepGoalActivity.class);
                startActivity(intent);
            });
        }

        // View Report button
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

    /**
     * Start tracking:
     * - If STEP_COUNTER available: register for it and ensure today's baseline is loaded.
     * - Else if STEP_DETECTOR: register for detector and zero detectorAccumulatedSteps if starting new day.
     * - Else accelerometer: register accelerometer listener.
     */
    private void startStepTracking() {
        // ensure baseline for today (if using step counter)
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

    /**
     * Stop tracking:
     * - Unregister listener.
     * - Save current data (DB).
     * - For STEP_COUNTER: keep baseline as-is (or update baseline on reset/finish if you want show zero immediately).
     */
    private void stopStepTracking() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepListener);
        }
        handler.removeCallbacks(timeUpdater);

        // Save today's data to DB
        saveCurrentData();
    }

    /**
     * SensorEventListener that handles TYPE_STEP_COUNTER, TYPE_STEP_DETECTOR, and ACCELEROMETER fallback.
     */
    private final SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                // cumulative steps since boot
                totalSensorSteps = (int) event.values[0];

                // If we are waiting to set baseline for today, set it now (only if baseline not set)
                if (waitingForFirstCounterEvent) {
                    stepsBaselineForToday = totalSensorSteps;
                    saveBaselineForToday(stepsBaselineForToday);
                    waitingForFirstCounterEvent = false;
                }

                // compute visible stepCount = totalSensorSteps - baselineForToday
                stepCount = Math.max(0, totalSensorSteps - getBaselineForToday());
                // update elapsedTime kept as before
                updateUI();
            } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                // each event.values[i] == 1.0f means a step
                for (float v : event.values) {
                    if (v == 1.0f) {
                        detectorAccumulatedSteps++;
                    }
                }
                stepCount = detectorAccumulatedSteps;
                updateUI();
            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                detectStep(event); // your existing accelerometer detection logic updates stepCount and UI
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    /**
     * Accelerometer-based detection (your existing implementation) — updates detectorAccumulatedSteps and stepCount.
     * We preserve your verticalRatio check + peak detection.
     */
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
                // stepCount mirrors detectorAccumulatedSteps for accelerometer fallback
                stepCount = detectorAccumulatedSteps;
                updateUI();
            }
        }
    }

    private boolean isStepPattern() {
        // ensure we have filled the window
        float sum = 0;
        int count = 0;
        for (float v : lastValues) {
            // some positions might be zero if not filled; count non-zero
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
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                stepCountText.setText(String.valueOf(stepCount));
                progressBar.setProgress(Math.min(stepCount, stepGoal));

                updateTargetText();
                
                int remainingSteps = Math.max(0, stepGoal - stepCount);
                if (remainingText != null) {
                    remainingText.setText(getString(R.string.remaining_steps_format, remainingSteps));
                }

                double kcal = stepCount * KCAL_PER_STEP;
                double distance = stepCount * KM_PER_STEP;

                if (kcalText != null) {
                    kcalText.setText(String.format(Locale.getDefault(), "%.2f", kcal));
                }
                if (distanceText != null) {
                    distanceText.setText(String.format(Locale.getDefault(), "%.2f", distance));
                }

                updateTimeDisplay();
            });
        }
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

    /**
     * Load today's data:
     * - If using STEP_COUNTER: load baseline for today and compute stepCount = totalSensorSteps - baseline.
     * - Else (accelerometer / detector): read DB saved value for today (your existing DB helper).
     */
    private void loadTodayData() {
        // First, ensure baseline exists for today if we use step counter
        if (stepSensorIsCounter) {
            stepsBaselineForToday = getBaselineForToday();
            // If baseline absent (-1), we'll wait for first counter event and set baseline then
            if (stepsBaselineForToday == Integer.MIN_VALUE) {
                // mark waiting - baseline not set yet
                waitingForFirstCounterEvent = true;
                stepCount = 0;
            } else {
                // If we already have a baseline, we cannot compute totalSensorSteps until we get an event,
                // but UI should show saved DB value if any
                // Try to load today's saved DB data (fallback)
                DatabaseHelper.StepData todayData = databaseHelper.getTodayStepData();
                stepCount = Math.max(todayData.steps, 0);
            }
        } else {
            // accelerometer or detector: load DB value for today
            DatabaseHelper.StepData todayData = databaseHelper.getTodayStepData();
            stepCount = todayData.steps;
            elapsedTime = todayData.time;
        }

        // update goal and UI
        stepGoal = getStepGoalForToday();
        progressBar.setMax(stepGoal);
        updateTargetText();
        updateUI();
    }

    /**
     * Save current data into DB.
     * For STEP_COUNTER devices we also can save the baseline if we want to preserve exact daily counts.
     */
    private void saveCurrentData() {
        // Lưu baseline nếu đang dùng STEP_COUNTER
        if (stepSensorIsCounter) {
            saveBaselineForToday(getBaselineForToday() == Integer.MIN_VALUE
                    ? stepsBaselineForToday
                    : getBaselineForToday());
        }

        // Chỉ cộng thêm phần chênh lệch so với DB để tránh ghi đè dữ liệu service
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
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNativeBanner();
        loadTodayData();
        setupWeightSection();
        updateTargetText();
        if (isTracking) {
            startStepTracking();
            if (startStopButtonCircle != null) {
                startStopButtonCircle.setImageResource(R.drawable.ic_pause);
                startStopButtonCircle.setColorFilter(null);
            }
        } else {
            if (startStopButtonCircle != null) {
                startStopButtonCircle.setImageResource(R.drawable.ic_play);
                startStopButtonCircle.setColorFilter(new PorterDuffColorFilter(0xFF5F7DED, PorterDuff.Mode.SRC_IN));
            }
        }

        if (!SharePreferenceUtils.isOrganic(requireContext())) {
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
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(timeUpdater);
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
            } else {
                // permission ok -> sensors already detected in setupSensor()
            }
        } else {
            // older versions don't need runtime activity recognition permission
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted, okay to use step sensors
                setupSensor();
            } else {
                Toast.makeText(requireContext(), "Ứng dụng cần quyền theo dõi hoạt động để đếm bước chân",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // ---------------- Baseline persistence helpers ----------------

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
        // return Integer.MIN_VALUE if not present
        if (!prefs.contains(getTodayKey())) return Integer.MIN_VALUE;
        return prefs.getInt(getTodayKey(), Integer.MIN_VALUE);
    }

    /**
     * Ensure baseline exists for today if using step counter.
     * If baseline not present, waitingForFirstCounterEvent=true and baseline will be set when first counter event arrives.
     */
    private void ensureBaselineForToday() {
        if (!stepSensorIsCounter) return;

        int saved = getBaselineForToday();
        if (saved == Integer.MIN_VALUE) {
            // baseline not set: if we already have a totalSensorSteps reading, set baseline now
            if (totalSensorSteps > 0) {
                stepsBaselineForToday = totalSensorSteps;
                saveBaselineForToday(stepsBaselineForToday);
                waitingForFirstCounterEvent = false;
            } else {
                // wait for first onSensorChanged(TYPE_STEP_COUNTER)
                waitingForFirstCounterEvent = true;
            }
        } else {
            // baseline already present - use it
            stepsBaselineForToday = saved;
            waitingForFirstCounterEvent = false;
        }
    }

    /**
     * If user finishes a walk/activity and you want the UI to show 0 immediately after finish,
     * call resetBaselineToCurrentSensor() which sets today's baseline = current cumulative sensor value.
     */
    private void resetBaselineToCurrentSensor() {
        if (!stepSensorIsCounter) return;
        if (totalSensorSteps > 0) {
            stepsBaselineForToday = totalSensorSteps;
            saveBaselineForToday(stepsBaselineForToday);
            // update UI to zero
            stepCount = 0;
            updateUI();
        }
    }
}
