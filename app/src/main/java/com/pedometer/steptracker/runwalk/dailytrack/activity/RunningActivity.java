package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.ActivityFragment;

public class RunningActivity extends AppCompatActivity {

    private ActivityFragment activityFragment;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> handleBackAction());

        if (savedInstanceState == null) {
            activityFragment = ActivityFragment.newInstance(true);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.runningFragmentContainer, activityFragment)
                    .commit();
        } else {
            activityFragment = (ActivityFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.runningFragmentContainer);
        }

        // Chặn nút back của hệ thống và gesture back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackAction();
            }
        });
    }

    private void handleBackAction() {
        if (activityFragment != null && activityFragment.isTracking()) {
            // Hiển thị dialog xác nhận
            showExitConfirmationDialog();
        } else {
            // Không đang tracking, cho phép back bình thường
            finish();
        }
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Running Session?")
                .setMessage("You are currently tracking your run. If you exit now, your progress will be lost. Do you want to continue?")
                .setPositiveButton("Stay", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    if (activityFragment != null) {
                        activityFragment.forceStopTracking();
                    }
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    // Method để ẩn/hiện nút back từ Fragment
    public void setBackButtonVisible(boolean visible) {
        if (btnBack != null) {
            btnBack.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
}