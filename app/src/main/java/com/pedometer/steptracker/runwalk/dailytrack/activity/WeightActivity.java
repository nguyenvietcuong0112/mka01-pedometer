package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.model.WeightHistoryHelper;
import com.pedometer.steptracker.runwalk.dailytrack.utils.ProfileDataManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightActivity extends AppCompatActivity {

    private TextView tvCurrentWeight, tvWeightGoal, tvWeightDate;
    private TextView tvRecentWeight, tvRecentDate, tvRecentWeightValue;
    private Button btnEditGoalMain, btnReset;
    private ImageView ivBack, btnEditGoal;
    private LineChart weightChart;

    private WeightHistoryHelper weightHistoryHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        weightHistoryHelper = new WeightHistoryHelper(getApplicationContext());

        initializeViews();
        loadWeightData();
        loadChartData();
        setupClickListeners();
    }

    private void initializeViews() {
        ivBack = findViewById(R.id.ivBack);
        btnReset = findViewById(R.id.btnReset);
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvWeightGoal = findViewById(R.id.tvWeightGoal);
        tvWeightDate = findViewById(R.id.tvWeightDate);
        btnEditGoalMain = findViewById(R.id.btnEditGoalMain);
        btnEditGoal = findViewById(R.id.btnEditGoal);
        tvRecentWeight = findViewById(R.id.tvRecentWeight);
        tvRecentDate = findViewById(R.id.tvRecentDate);
        tvRecentWeightValue = findViewById(R.id.tvRecentWeightValue);
        weightChart = findViewById(R.id.weightChart);
    }

    private void loadWeightData() {
        float weight = ProfileDataManager.getWeight(getApplicationContext());
        float weightGoal = ProfileDataManager.getWeightGoal(getApplicationContext());
        long updatedAt = ProfileDataManager.getWeightUpdatedAt(getApplicationContext());

        WeightHistoryHelper.WeightEntry latestEntry = weightHistoryHelper.getLatestEntry();
        if (weight <= 0f && latestEntry != null) {
            weight = latestEntry.weight;
        }
        if (updatedAt == 0L && latestEntry != null) {
            updatedAt = latestEntry.timestamp;
        }

        tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f", weight));
        tvWeightGoal.setText(String.format(Locale.getDefault(), "%.1fkg", weightGoal));
        tvWeightDate.setText(formatDate(updatedAt > 0 ? updatedAt : System.currentTimeMillis()));

        updateRecentSection(latestEntry, weightGoal);
    }

    private void updateRecentSection(WeightHistoryHelper.WeightEntry latestEntry, float goalWeight) {
        if (latestEntry != null) {
            tvRecentWeight.setText(String.format(Locale.getDefault(), "%.1f", latestEntry.weight));
            tvRecentWeightValue.setText(String.format(Locale.getDefault(), "%.1f", latestEntry.weight));
            tvRecentDate.setText(formatDateTime(latestEntry.timestamp));
        } else {
            tvRecentWeight.setText("0");
            tvRecentWeightValue.setText("0.0");
            tvRecentDate.setText("-");
        }
    }

    private void loadChartData() {
        List<WeightHistoryHelper.WeightEntry> entries = weightHistoryHelper.getRecentEntries(10);
        List<Entry> chartEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (entries.isEmpty()) {
            weightChart.clear();
            weightChart.setNoDataText("No weight history yet");
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            WeightHistoryHelper.WeightEntry entry = entries.get(i);
            chartEntries.add(new Entry(i, entry.weight));
            labels.add(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date(entry.timestamp)));
        }

        LineDataSet dataSet = new LineDataSet(chartEntries, "Weight");
        dataSet.setColor(0xFF5F7DED);
        dataSet.setCircleColor(0xFF5F7DED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        weightChart.setData(lineData);
        weightChart.getDescription().setEnabled(false);
        weightChart.getLegend().setEnabled(false);

        XAxis xAxis = weightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(0xFF5A5A5A);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new LabelFormatter(labels));

        YAxis leftAxis = weightChart.getAxisLeft();
        leftAxis.setTextColor(0xFF5A5A5A);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(0xFFE0E0E0);
        weightChart.getAxisRight().setEnabled(false);

        weightChart.invalidate();
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnEditGoalMain.setOnClickListener(v -> showWeightInputSheet());
        btnEditGoal.setOnClickListener(v -> showWeightInputSheet());
        btnReset.setOnClickListener(v -> showResetConfirmSheet());
    }

    private void showWeightInputSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_weight_input, null, false);
        EditText etCurrent = view.findViewById(R.id.etCurrentWeight);
        EditText etGoal = view.findViewById(R.id.etGoalWeight);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        float currentWeight = ProfileDataManager.getWeight(this);
        float goalWeight = ProfileDataManager.getWeightGoal(this);
        if (currentWeight > 0f) etCurrent.setText(String.valueOf(currentWeight));
        if (goalWeight > 0f) etGoal.setText(String.valueOf(goalWeight));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            float weight = parseFloat(etCurrent.getText().toString());
            float goal = parseFloat(etGoal.getText().toString());

            if (weight > 0f) {
                ProfileDataManager.saveWeight(this, weight);
                weightHistoryHelper.addEntry(weight);
            }
            ProfileDataManager.saveWeightGoal(this, goal);

            loadWeightData();
            loadChartData();
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showResetConfirmSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_reset_confirm, null, false);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            ProfileDataManager.saveWeight(this, 0f);
            ProfileDataManager.saveWeightGoal(this, 0f);
            weightHistoryHelper.clearHistory();
            loadWeightData();
            loadChartData();
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private String formatDate(long timeMillis) {
        if (timeMillis <= 0) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    private String formatDateTime(long timeMillis) {
        if (timeMillis <= 0) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    private float parseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWeightData();
        loadChartData();
    }

    private static class LabelFormatter extends ValueFormatter {
        private final List<String> labels;

        LabelFormatter(List<String> labels) {
            this.labels = labels;
        }

        @Override
        public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
            int index = (int) value;
            if (index >= 0 && index < labels.size()) {
                return labels.get(index);
            }
            return "";
        }
    }
}

