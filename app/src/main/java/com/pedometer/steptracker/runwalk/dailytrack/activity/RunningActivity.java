package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.ActivityFragment;

public class RunningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        if (savedInstanceState == null) {
            ActivityFragment fragment = ActivityFragment.newInstance(true);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.runningFragmentContainer, fragment)
                    .commit();
        }
    }
}

