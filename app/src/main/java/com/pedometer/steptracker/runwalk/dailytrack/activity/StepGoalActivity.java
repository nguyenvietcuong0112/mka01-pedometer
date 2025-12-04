package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;

public class StepGoalActivity extends AppCompatActivity {

    private EditText mondayGoal, tuesdayGoal, wednesdayGoal, thursdayGoal, fridayGoal, saturdayGoal, sundayGoal;
    private LinearLayout saveButton;
    private FrameLayout frAds;
    private SharedPreferences prefs;

    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_goal);

        prefs = getSharedPreferences("StepGoals", MODE_PRIVATE);

        initializeViews();
        loadGoals();
        setupSaveButton();
    }

    private void initializeViews() {
        mondayGoal = findViewById(R.id.mondayGoal);
        tuesdayGoal = findViewById(R.id.tuesdayGoal);
        wednesdayGoal = findViewById(R.id.wednesdayGoal);
        thursdayGoal = findViewById(R.id.thursdayGoal);
        fridayGoal = findViewById(R.id.fridayGoal);
        saturdayGoal = findViewById(R.id.saturdayGoal);
        sundayGoal = findViewById(R.id.sundayGoal);
        saveButton = findViewById(R.id.saveButton);
        ivBack = findViewById(R.id.iv_back);
        frAds = findViewById(R.id.frAds);

        ivBack.setOnClickListener(view -> onBackPressed());

    }

    private void loadGoals() {
        mondayGoal.setText(String.valueOf(prefs.getInt("Monday", 6000)));
        tuesdayGoal.setText(String.valueOf(prefs.getInt("Tuesday", 6000)));
        wednesdayGoal.setText(String.valueOf(prefs.getInt("Wednesday", 6000)));
        thursdayGoal.setText(String.valueOf(prefs.getInt("Thursday", 6000)));
        fridayGoal.setText(String.valueOf(prefs.getInt("Friday", 6000)));
        saturdayGoal.setText(String.valueOf(prefs.getInt("Saturday", 6000)));
        sundayGoal.setText(String.valueOf(prefs.getInt("Sunday", 6000)));
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("Monday", Integer.parseInt(mondayGoal.getText().toString()));
            editor.putInt("Tuesday", Integer.parseInt(tuesdayGoal.getText().toString()));
            editor.putInt("Wednesday", Integer.parseInt(wednesdayGoal.getText().toString()));
            editor.putInt("Thursday", Integer.parseInt(thursdayGoal.getText().toString()));
            editor.putInt("Friday", Integer.parseInt(fridayGoal.getText().toString()));
            editor.putInt("Saturday", Integer.parseInt(saturdayGoal.getText().toString()));
            editor.putInt("Sunday", Integer.parseInt(sundayGoal.getText().toString()));
            editor.apply();

            Toast.makeText(this, "Goals saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }


    private void loadAds() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_step_goal), new NativeCallback() {

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = new NativeAdView(StepGoalActivity.this);
                if (!SharePreferenceUtils.isOrganic(StepGoalActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(StepGoalActivity.this).inflate(R.layout.layout_native_btn_top, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(StepGoalActivity.this).inflate(R.layout.layout_native_btn_bottom, null);
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

    @Override
    protected void onResume() {
        super.onResume();
        loadAds();
    }
}