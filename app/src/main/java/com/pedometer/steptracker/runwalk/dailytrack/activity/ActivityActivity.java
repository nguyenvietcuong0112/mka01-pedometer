package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.utils.BottomNavigationHelper;

public class ActivityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity);
        BottomNavigationHelper.setupBottomNavigation(this, BottomNavigationHelper.NAV_ACTIVITY);
    }
}

