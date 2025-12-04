package com.pedometer.steptracker.runwalk.dailytrack.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.model.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AchievementFragment extends Fragment {

    private static final int MODE_STEPS = 0;
    private static final int MODE_COMBO = 1;

    private TextView tabSteps;
    private TextView tabCombo;
    private TextView labelCurrent;
    private TextView textCurrentValue;
    private GridLayout gridAchievements;

    private DatabaseHelper databaseHelper;
    private int currentMode = MODE_STEPS;

    private final int[] stepThresholds = {3000, 7000, 10000, 14000, 20000, 30000, 40000, 60000};
    private final int[] comboThresholds = {1, 3, 7, 13, 25, 50, 100, 365};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_achievement, container, false);


        databaseHelper = new DatabaseHelper(requireContext());
        initViews(view);
        setupTabs();
        setupShare(view);
        buildAchievementGrid();
        refreshData();

        return view;
    }

    private void initViews(View root) {
        tabSteps = root.findViewById(R.id.tab_steps);
        tabCombo = root.findViewById(R.id.tab_combo);
        labelCurrent = root.findViewById(R.id.label_current);
        textCurrentValue = root.findViewById(R.id.text_current_value);
        gridAchievements = root.findViewById(R.id.grid_achievements);
    }

    private void setupTabs() {
        tabSteps.setOnClickListener(v -> {
            if (currentMode != MODE_STEPS) {
                currentMode = MODE_STEPS;
                updateTabUI();
                refreshData();
            }
        });

        tabCombo.setOnClickListener(v -> {
            if (currentMode != MODE_COMBO) {
                currentMode = MODE_COMBO;
                updateTabUI();
                refreshData();
            }
        });

        updateTabUI();
    }

    private void updateTabUI() {
        if (getContext() == null) return;

        boolean isSteps = currentMode == MODE_STEPS;

        tabSteps.setBackgroundResource(isSteps ? R.drawable.bg_tab_left_selected : R.drawable.bg_tab_right_unselected);
        tabSteps.setTextColor(ContextCompat.getColor(requireContext(), isSteps ? android.R.color.white : android.R.color.darker_gray));

        tabCombo.setBackgroundResource(isSteps ? R.drawable.bg_tab_right_unselected : R.drawable.bg_tab_left_selected);
        tabCombo.setTextColor(ContextCompat.getColor(requireContext(), isSteps ? android.R.color.darker_gray : android.R.color.white));

        labelCurrent.setText(isSteps ? R.string.current_steps_label : R.string.current_combo_label);
    }

    private void setupShare(View root) {
        TextView btnShare = root.findViewById(R.id.btn_share);
        btnShare.setOnClickListener(v -> shareAchievements());
    }

    private void buildAchievementGrid() {
        if (getContext() == null) return;

        gridAchievements.removeAllViews();

        int itemCount = 8;
        for (int i = 0; i < itemCount; i++) {
            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_achievement, gridAchievements, false);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            item.setLayoutParams(params);
            gridAchievements.addView(item);
        }
    }

    private void refreshData() {
        if (currentMode == MODE_STEPS) {
            updateStepAchievements();
        } else {
            updateComboAchievements();
        }
    }

    private void updateStepAchievements() {
        int todaySteps = databaseHelper.getTodayStepData().steps;
        textCurrentValue.setText(String.valueOf(todaySteps));

        List<Boolean> unlocked = new ArrayList<>();
        for (int threshold : stepThresholds) {
            unlocked.add(todaySteps >= threshold);
        }

        applyAchievementsToGrid(unlocked, true);
    }

    private void updateComboAchievements() {
        int comboDays = calculateCurrentStreak();
        textCurrentValue.setText(comboDays + "D");

        List<Boolean> unlocked = new ArrayList<>();
        for (int threshold : comboThresholds) {
            unlocked.add(comboDays >= threshold);
        }

        applyAchievementsToGrid(unlocked, false);
    }

    private void applyAchievementsToGrid(List<Boolean> unlocked, boolean isStepsMode) {
        if (getContext() == null) return;

        int itemCount = gridAchievements.getChildCount();
        for (int i = 0; i < itemCount && i < unlocked.size(); i++) {
            View item = gridAchievements.getChildAt(i);
            ImageView icon = item.findViewById(R.id.img_badge);
            TextView title = item.findViewById(R.id.text_badge_title);

            boolean isUnlocked = unlocked.get(i);
            icon.setImageResource(isUnlocked ? R.drawable.ic_step_achievement : R.drawable.ic_step_achievement_unselected);

            String text;
            if (isStepsMode) {
                text = getString(R.string.badge_steps_format, stepThresholds[i]);
            } else {
                text = getString(R.string.badge_combo_format, comboThresholds[i]);
            }
            title.setText(text);

            float alpha = isUnlocked ? 1.0f : 0.4f;
            icon.setAlpha(alpha);
            title.setAlpha(alpha);
        }
    }

    private int calculateCurrentStreak() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        String today = sdf.format(calendar.getTime());

        String query = "SELECT date, steps FROM steps WHERE date <= ? ORDER BY date DESC LIMIT 400";
        Cursor cursor = db.rawQuery(query, new String[]{today});

        int streak = 0;
        String expectedDate = today;

        if (cursor.moveToFirst()) {
            int dateIndex = cursor.getColumnIndex("date");
            int stepsIndex = cursor.getColumnIndex("steps");

            do {
                String date = cursor.getString(dateIndex);
                int steps = cursor.getInt(stepsIndex);

                if (!TextUtils.equals(date, expectedDate)) {
                    break;
                }

                if (steps > 0) {
                    streak++;
                } else {
                    break;
                }

                calendar.add(Calendar.DAY_OF_YEAR, -1);
                expectedDate = sdf.format(calendar.getTime());

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return streak;
    }

    private void shareAchievements() {
        String text = getString(R.string.share_achievement_text, textCurrentValue.getText());
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
    }
}

