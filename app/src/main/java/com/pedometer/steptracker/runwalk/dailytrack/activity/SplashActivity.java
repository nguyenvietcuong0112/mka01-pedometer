package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.LoadAdError;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.util.Admob;
import com.mallegan.ads.util.ConsentHelper;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.activity.nativefull.ActivityLoadNativeFullV5;
import com.pedometer.steptracker.runwalk.dailytrack.base.BaseActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivitySplashBinding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharedClass;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SystemConfiguration;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SystemUtil;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;


public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;

    private InterCallback interCallback;

    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        setContentView(binding.getRoot());

        proceedToNextActivity();
        interCallback = new InterCallback() {

            @Override
            public void onAdClosedByUser() {
                super.onAdClosedByUser();
                if (!SharePreferenceUtils.isOrganic(SplashActivity.this)) {
                    ActivityLoadNativeFullV5.open(SplashActivity.this, getString(R.string.native_full_splash_high), getString(R.string.native_full_splash), () -> {
                        proceedToNextActivity();
                    });
                } else {
                    proceedToNextActivity();
                }
                proceedToNextActivity();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
//                if (!SharePreferenceUtils.isOrganic(SplashActivity.this)) {
//                    ActivityLoadNativeFullV5.open(SplashActivity.this, getString(R.string.native_full_splash_high), getString(R.string.native_full_splash), () -> {
//                        proceedToNextActivity();
//                    });
//                } else {
//                    proceedToNextActivity();
//                }
                proceedToNextActivity();
            }
        };
//        notificationPermissionLauncher = registerForActivityResult(
//                new ActivityResultContracts.RequestPermission(),
//                isGranted -> {
//                    if (isGranted) {
//                        try {
//                            Intent serviceIntent = new Intent(this, WakeService.class);
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                startForegroundService(serviceIntent);
//                            } else {
//                                startService(serviceIntent);
//                            }
//                        } catch (Exception e) {
//                            // Silently handle any exceptions
//                        }
//                    }
//                    setUpConsentAndShowAds();
//                }
//        );
//        if (SystemUtil.checkPermissionNoty(SplashActivity.this)) {
//            setUpConsentAndShowAds();
//            startWakeService();
//        } else {
//            requestNotificationPermission();
//        }
//        checkFullAds();
        setUpConsentAndShowAds();

    }

    private void setUpConsentAndShowAds() {
        ConsentHelper consentHelper = ConsentHelper.getInstance(this);
        if (!consentHelper.canLoadAndShowAds()) {
            consentHelper.reset();
        }
        consentHelper.obtainConsentAndShow(this, this::loadAndShowInterSplash);
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

//    private void checkFullAds() {
//        if (SharePreferenceUtils.isOrganicNoti(this)) {
//            AppsFlyerLib.getInstance().registerConversionListener(this, new AppsFlyerConversionListener() {
//
//                @Override
//                public void onConversionDataSuccess(Map<String, Object> conversionData) {
//                    String mediaSource = (String) conversionData.get("media_source");
//                    SharePreferenceUtils.setOrganicNoti(getApplicationContext(), Objects.requireNonNull(mediaSource).isEmpty() || "organic".equals(mediaSource));
//                }
//
//                @Override
//                public void onConversionDataFail(String s) {
//                }
//
//                @Override
//                public void onAppOpenAttribution(Map<String, String> map) {
//
//                }
//
//                @Override
//                public void onAttributionFailure(String s) {
//
//                }
//            });
//        }
//    }


    private void loadAndShowInterSplash() {
        try {
            Admob.getInstance().loadSplashInterAdsFloor(SplashActivity.this,
                    new ArrayList<>(Arrays.asList(getString(R.string.inter_splash_high), getString(R.string.inter_splash))),
                    3000, interCallback);
        } catch (Exception e) {
            // Silently handle any exceptions
        }
    }

    private void proceedToNextActivity() {
        Intent intent = new Intent(SplashActivity.this, LanguageActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

//    private void startWakeService() {
//        try {
//            Intent serviceIntent = new Intent(this, WakeService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(serviceIntent);
//            } else {
//                startService(serviceIntent);
//            }
//        } catch (Exception e) {
//            // Silently handle any exceptions
//        }
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
        Admob.getInstance().onCheckShowSplashWhenFail(SplashActivity.this, interCallback, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Admob.getInstance().dismissLoadingDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Admob.getInstance().dismissLoadingDialog();
    }
}
