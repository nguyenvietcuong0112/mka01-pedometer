package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.model.DatabaseHelper;
import com.pedometer.steptracker.runwalk.dailytrack.utils.BottomNavigationHelper;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailsReportActivity extends AppCompatActivity {

    private TextView stepsText, kcalText, timeText, distanceText;
    private LineChart chart;
    private DatabaseHelper databaseHelper;

    private Spinner statisticSpinner;

    private static final int STATISTIC_STEPS = 0;
    private static final int STATISTIC_DISTANCE = 1;
    private static final int STATISTIC_CALORIES = 2;

    private int currentStatisticType = STATISTIC_STEPS;

private FrameLayout frAdsBanner;
    private TextView tvCurrentMonth;
    private FrameLayout frAds;
    private ImageView btnPreviousMonth, btnNextMonth;
    private int currentMonthIndex;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_report);

        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupBackButton();
        setupSpinners();
        loadMonthlyData();
        setupMonthNavigation();
        setupChart();
        loadAds();
        loadNativeBanner();
    }

    private void loadNativeBanner() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_daily_report), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(DetailsReportActivity.this).inflate(R.layout.ad_native_admob_banner_1, null);
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

    private void loadAds() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_daily_report), new NativeCallback() {

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = new NativeAdView(DetailsReportActivity.this);
                if (!SharePreferenceUtils.isOrganic(DetailsReportActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(DetailsReportActivity.this).inflate(R.layout.layout_native_language_non_organic, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(DetailsReportActivity.this).inflate(R.layout.layout_native_language, null);
                }
                frAds.removeAllViews();
                frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }


            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                frAds.setVisibility(View.GONE);
            }
        });
    }

    private void setupMonthNavigation() {
        tvCurrentMonth = findViewById(R.id.tv_current_month);
        btnPreviousMonth = findViewById(R.id.btn_previous_month);
        btnNextMonth = findViewById(R.id.btn_next_month);

        Calendar cal = Calendar.getInstance();
        currentMonthIndex = cal.get(Calendar.MONTH);
        updateMonthDisplay();

        btnPreviousMonth.setOnClickListener(v -> {
            currentMonthIndex = (currentMonthIndex - 1 + 12) % 12;
            updateMonthDisplay();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonthIndex = (currentMonthIndex + 1) % 12;
            updateMonthDisplay();
        });
    }

    private void updateMonthDisplay() {
        String[] monthNames = getResources().getStringArray(R.array.month_options);
        tvCurrentMonth.setText(monthNames[currentMonthIndex]);

        setupChart();
        updateMonthlyDataDisplay(currentMonthIndex);
    }

    private void initializeViews() {
        stepsText = findViewById(R.id.stepsText);
        kcalText = findViewById(R.id.kcalText);
        timeText = findViewById(R.id.timeText);
        distanceText = findViewById(R.id.distanceText);
        chart = findViewById(R.id.chart);
        frAds = findViewById(R.id.frAds);
        frAdsBanner = findViewById(R.id.fr_ads_banner);

        statisticSpinner = findViewById(R.id.statisticSpinner);

    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> statisticAdapter = ArrayAdapter.createFromResource(this,
                R.array.statistic_options, R.layout.spinner_item);
        statisticAdapter.setDropDownViewResource(R.layout.spinner_item);
        statisticSpinner.setAdapter(statisticAdapter);

        statisticSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStatisticType = position;
                setupChart();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void setupBackButton() {
        ImageView backButton = findViewById(R.id.iv_back);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadMonthlyData() {
        DatabaseHelper.MonthlyStepData monthlyData = databaseHelper.getMonthlyStepData();
        stepsText.setText(getString(R.string.monthly_steps_format, monthlyData.totalSteps));
        kcalText.setText(getString(R.string.monthly_kcal_format, monthlyData.totalCalories));
        timeText.setText(getString(R.string.monthly_time_format, formatTime(monthlyData.totalTime)));
        distanceText.setText(getString(R.string.monthly_distance_format, monthlyData.totalDistance));
    }

    private String formatTime(long totalMilliseconds) {
        long totalSeconds = totalMilliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void setupChart() {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int selectedMonth = currentMonthIndex;

        List<DatabaseHelper.DailyStepData> dailyData = databaseHelper.getMonthData(selectedMonth);

        Map<String, Integer> stepsMap = new HashMap<>();
        Map<String, Float> distanceMap = new HashMap<>();
        Map<String, Float> caloriesMap = new HashMap<>();

        for (DatabaseHelper.DailyStepData data : dailyData) {
            stepsMap.put(data.date, data.steps);
            distanceMap.put(data.date, data.distance);
            caloriesMap.put(data.date, data.calories);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        cal.set(currentYear, selectedMonth, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int todayIndex = -1;
        for (int i = 0; i < maxDay; i++) {
            String date = sdf.format(cal.getTime());

            float value = 0f;
            switch (currentStatisticType) {
                case STATISTIC_STEPS:
                    value = stepsMap.getOrDefault(date, 0);
                    break;
                case STATISTIC_DISTANCE:
                    value = distanceMap.getOrDefault(date, 0f);
                    break;
                case STATISTIC_CALORIES:
                    value = caloriesMap.getOrDefault(date, 0f);
                    break;
            }

            entries.add(new Entry(i, value));
            labels.add(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

            if (cal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                    cal.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                todayIndex = i;
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        String datasetLabel;
        int chartColor;
        switch (currentStatisticType) {
            case STATISTIC_DISTANCE:
                datasetLabel = "Daily Distance (km)";
                chartColor = getResources().getColor(R.color.colorAccent);
                break;
            case STATISTIC_CALORIES:
                datasetLabel = "Daily Calories (kcal)";
                chartColor = Color.RED;
                break;
            case STATISTIC_STEPS:
            default:
                datasetLabel = "Daily Steps";
                chartColor = getResources().getColor(R.color.colorPrimary);
                break;
        }

        LineDataSet dataSet = new LineDataSet(entries, datasetLabel);
        dataSet.setColor(chartColor);
        dataSet.setCircleColor(chartColor);
        dataSet.setDrawCircles(true);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(0);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setGridLineWidth(0.5f);
        leftAxis.setDrawAxisLine(false);

        float maxYValue = 50f; // Default
        for (Entry entry : entries) {
            if (entry.getY() > maxYValue) {
                maxYValue = entry.getY();
            }
        }
        leftAxis.setAxisMaximum(maxYValue * 1.2f);

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.getDescription().setEnabled(false);

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setVisibleXRangeMaximum(7);

        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);

        if (todayIndex != -1 && selectedMonth == Calendar.getInstance().get(Calendar.MONTH)) {
            chart.moveViewToX(todayIndex - 3);
        } else {
            chart.moveViewToX(0);
        }

        chart.invalidate();
    }

    private void updateMonthlyDataDisplay(int month) {
        DatabaseHelper.MonthlyStepData monthlyData = databaseHelper.getMonthlyStatData(month, currentStatisticType);
        stepsText.setText(getString(R.string.monthly_steps_format, monthlyData.totalSteps));
        kcalText.setText(getString(R.string.monthly_kcal_format, monthlyData.totalCalories));
        timeText.setText(getString(R.string.monthly_time_format, formatTime(monthlyData.totalTime)));
        distanceText.setText(getString(R.string.monthly_distance_format, monthlyData.totalDistance));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAds();
    }
}