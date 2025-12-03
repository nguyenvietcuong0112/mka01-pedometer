package com.pedometer.steptracker.runwalk.dailytrack.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.activity.MainActivity;

public class BottomNavigationHelper {

    public static final int NAV_STEPS = 0;
    public static final int NAV_ACTIVITY = 1;
    public static final int NAV_REPORT = 2;
    public static final int NAV_ACHIEVEMENT = 3;
    public static final int NAV_SETTINGS = 4;

    public static void setupBottomNavigation(MainActivity activity, int currentNav) {
        View bottomNavView = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavView == null) {
            return;
        }

        // Get all navigation items
        LinearLayout navSteps = bottomNavView.findViewById(R.id.nav_steps);
        LinearLayout navActivity = bottomNavView.findViewById(R.id.nav_activity);
        LinearLayout navReport = bottomNavView.findViewById(R.id.nav_report);
        LinearLayout navAchievement = bottomNavView.findViewById(R.id.nav_achievement);
        LinearLayout navSettings = bottomNavView.findViewById(R.id.nav_settings);

        // Reset all items
        setNavItemState(navSteps, false);
        setNavItemState(navActivity, false);
        setNavItemState(navReport, false);
        setNavItemState(navAchievement, false);
        setNavItemState(navSettings, false);

        // Set current item as selected
        switch (currentNav) {
            case NAV_STEPS:
                setNavItemState(navSteps, true);
                navSteps.setOnClickListener(v -> activity.loadFragment(NAV_STEPS));
                navActivity.setOnClickListener(v -> activity.loadFragment(NAV_ACTIVITY));
                navReport.setOnClickListener(v -> activity.loadFragment(NAV_REPORT));
                navAchievement.setOnClickListener(v -> activity.loadFragment(NAV_ACHIEVEMENT));
                navSettings.setOnClickListener(v -> activity.loadFragment(NAV_SETTINGS));
                break;
            case NAV_ACTIVITY:
                setNavItemState(navActivity, true);
                navSteps.setOnClickListener(v -> activity.loadFragment(NAV_STEPS));
                navActivity.setOnClickListener(v -> activity.loadFragment(NAV_ACTIVITY));
                navReport.setOnClickListener(v -> activity.loadFragment(NAV_REPORT));
                navAchievement.setOnClickListener(v -> activity.loadFragment(NAV_ACHIEVEMENT));
                navSettings.setOnClickListener(v -> activity.loadFragment(NAV_SETTINGS));
                break;
            case NAV_REPORT:
                setNavItemState(navReport, true);
                navSteps.setOnClickListener(v -> activity.loadFragment(NAV_STEPS));
                navActivity.setOnClickListener(v -> activity.loadFragment(NAV_ACTIVITY));
                navReport.setOnClickListener(v -> activity.loadFragment(NAV_REPORT));
                navAchievement.setOnClickListener(v -> activity.loadFragment(NAV_ACHIEVEMENT));
                navSettings.setOnClickListener(v -> activity.loadFragment(NAV_SETTINGS));
                break;
            case NAV_ACHIEVEMENT:
                setNavItemState(navAchievement, true);
                navSteps.setOnClickListener(v -> activity.loadFragment(NAV_STEPS));
                navActivity.setOnClickListener(v -> activity.loadFragment(NAV_ACTIVITY));
                navReport.setOnClickListener(v -> activity.loadFragment(NAV_REPORT));
                navAchievement.setOnClickListener(v -> activity.loadFragment(NAV_ACHIEVEMENT));
                navSettings.setOnClickListener(v -> activity.loadFragment(NAV_SETTINGS));
                break;
            case NAV_SETTINGS:
                setNavItemState(navSettings, true);
                navSteps.setOnClickListener(v -> activity.loadFragment(NAV_STEPS));
                navActivity.setOnClickListener(v -> activity.loadFragment(NAV_ACTIVITY));
                navReport.setOnClickListener(v -> activity.loadFragment(NAV_REPORT));
                navAchievement.setOnClickListener(v -> activity.loadFragment(NAV_ACHIEVEMENT));
                navSettings.setOnClickListener(v -> activity.loadFragment(NAV_SETTINGS));
                break;
        }
    }

    private static void setNavItemState(LinearLayout navItem, boolean isSelected) {
        if (navItem == null) return;

        ImageView icon = null;
        TextView text = null;

        // Find the correct icon and text views based on the parent ID
        int parentId = navItem.getId();
        if (parentId == R.id.nav_steps) {
            icon = navItem.findViewById(R.id.icon_steps);
            text = navItem.findViewById(R.id.text_steps);
        } else if (parentId == R.id.nav_activity) {
            icon = navItem.findViewById(R.id.icon_activity);
            text = navItem.findViewById(R.id.text_activity);
        } else if (parentId == R.id.nav_report) {
            icon = navItem.findViewById(R.id.icon_report);
            text = navItem.findViewById(R.id.text_report);
        } else if (parentId == R.id.nav_achievement) {
            icon = navItem.findViewById(R.id.icon_achievement);
            text = navItem.findViewById(R.id.text_achievement);
        } else if (parentId == R.id.nav_settings) {
            icon = navItem.findViewById(R.id.icon_settings);
            text = navItem.findViewById(R.id.text_settings);
        }

        if (icon == null || text == null) return;

        if (isSelected) {
            if (parentId == R.id.nav_steps) {
                icon.setImageResource(R.drawable.ic_nav_steps);
                text.setTextColor(0xFF5F7DED);
            } else if (parentId == R.id.nav_activity) {
                icon.setImageResource(R.drawable.ic_nav_activity);
                text.setTextColor(0xFF5F7DED);
            } else if (parentId == R.id.nav_report) {
                icon.setImageResource(R.drawable.ic_nav_report);
                text.setTextColor(0xFF5F7DED);
            } else if (parentId == R.id.nav_achievement) {
                icon.setImageResource(R.drawable.ic_nav_achievement);
                text.setTextColor(0xFF5F7DED);
            } else if (parentId == R.id.nav_settings) {
                icon.setImageResource(R.drawable.ic_nav_settings);
                text.setTextColor(0xFF5F7DED);
            }
        } else {
            if (parentId == R.id.nav_steps) {
                icon.setImageResource(R.drawable.ic_nav_steps_unselected);
                text.setTextColor(0xFFB0B0B0);
            } else if (parentId == R.id.nav_activity) {
                icon.setImageResource(R.drawable.ic_nav_activity_unselected);
                text.setTextColor(0xFFB0B0B0);
            } else if (parentId == R.id.nav_report) {
                icon.setImageResource(R.drawable.ic_nav_report_unselected);
                text.setTextColor(0xFFB0B0B0);
            } else if (parentId == R.id.nav_achievement) {
                icon.setImageResource(R.drawable.ic_step_achievement_unselected);
                text.setTextColor(0xFFB0B0B0);
            } else if (parentId == R.id.nav_settings) {
                icon.setImageResource(R.drawable.ic_nav_settings_unselected);
                text.setTextColor(0xFFB0B0B0);
            }
        }
    }
}

