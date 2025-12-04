package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pedometer.steptracker.runwalk.dailytrack.R;

public class PermissionActivity extends AppCompatActivity {

    private static final int REQ_PERMISSIONS = 101;

    private SwitchCompat switchActivity;
    private SwitchCompat switchLocation;
    private SwitchCompat switchNotification;
    private Button btnGrant;

    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            return new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
    }

    public static boolean hasAllRequiredPermissions(AppCompatActivity activity) {
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms = new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            perms = new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        if (hasAllRequiredPermissions(this)) {
            goToMain();
            return;
        }


        btnGrant = findViewById(R.id.btnGrantPermission);
        switchActivity = findViewById(R.id.switchActivity);
        switchLocation = findViewById(R.id.switchLocation);
        switchNotification = findViewById(R.id.switchNotification);


        switchActivity.setOnClickListener(v -> requestActivityPermission());
        switchLocation.setOnClickListener(v -> requestLocationPermissions());
        switchNotification.setOnClickListener(v -> requestNotificationPermission());

        btnGrant.setOnClickListener(v -> {
            if (hasAllRequiredPermissions(this)) {
                goToMain();
            } else {
                ActivityCompat.requestPermissions(
                        PermissionActivity.this,
                        getRequiredPermissions(),
                        REQ_PERMISSIONS
                );
            }
        });

        updatePermissionUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSIONS) {
            updatePermissionUI();
            if (hasAllRequiredPermissions(this)) {
                goToMain();
            }
        }
    }

    private void requestActivityPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                REQ_PERMISSIONS
        );
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQ_PERMISSIONS
        );
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQ_PERMISSIONS
            );
        }
    }

    private void updatePermissionUI() {
        boolean hasActivity = ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
        boolean hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasLocation = hasFine || hasCoarse;
        boolean hasNotif = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        switchActivity.setChecked(hasActivity);
        switchActivity.setEnabled(!hasActivity);

        switchLocation.setChecked(hasLocation);
        switchLocation.setEnabled(!hasLocation);

        switchNotification.setChecked(hasNotif);
        switchNotification.setEnabled(!hasNotif && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU);

        boolean allGranted = hasActivity && hasLocation && hasNotif;
        btnGrant.setEnabled(allGranted);
    }

    private void goToMain() {
        // Navigate to ProfileActivity first, then it will navigate to MainActivity
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}


