package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.AchievementFragment;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.ActivityFragment;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.HomeFragment;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.ReportFragment;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.SettingsFragment;
import com.pedometer.steptracker.runwalk.dailytrack.service.StepCounterService;
import com.pedometer.steptracker.runwalk.dailytrack.utils.BottomNavigationHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME = "home";
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_REPORT = "report";
    private static final String TAG_ACHIEVEMENT = "achievement";
    private static final String TAG_SETTINGS = "settings";

    private int currentNav = BottomNavigationHelper.NAV_STEPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu chưa đủ quyền thì chuyển sang màn Permission và dừng Main
        if (!PermissionActivity.hasAllRequiredPermissions(this)) {
            Intent intent = new Intent(this, PermissionActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        startStepServiceIfNeeded();

        // Get initial fragment from intent
        int initialNav = getIntent().getIntExtra("nav", BottomNavigationHelper.NAV_STEPS);
        currentNav = initialNav;

        loadFragment(initialNav);
        setupBottomNavigation();
    }

    private void startStepServiceIfNeeded() {
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationHelper.setupBottomNavigationForFragment(this, currentNav);
    }

    public void loadFragment(int nav) {
        Fragment fragment = null;
        String tag = null;

        switch (nav) {
            case BottomNavigationHelper.NAV_STEPS:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_HOME);
                if (fragment == null) {
                    fragment = new HomeFragment();
                }
                tag = TAG_HOME;
                break;
            case BottomNavigationHelper.NAV_ACTIVITY:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_ACTIVITY);
                if (fragment == null) {
                    fragment = new ActivityFragment();
                }
                tag = TAG_ACTIVITY;
                break;
            case BottomNavigationHelper.NAV_REPORT:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_REPORT);
                if (fragment == null) {
                    fragment = new ReportFragment();
                }
                tag = TAG_REPORT;
                break;
            case BottomNavigationHelper.NAV_ACHIEVEMENT:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_ACHIEVEMENT);
                if (fragment == null) {
                    fragment = new AchievementFragment();
                }
                tag = TAG_ACHIEVEMENT;
                break;
            case BottomNavigationHelper.NAV_SETTINGS:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS);
                if (fragment == null) {
                    fragment = new SettingsFragment();
                }
                tag = TAG_SETTINGS;
                break;
        }

        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment, tag);
            transaction.commit();
            currentNav = nav;
            setupBottomNavigation();
        }
    }
}

