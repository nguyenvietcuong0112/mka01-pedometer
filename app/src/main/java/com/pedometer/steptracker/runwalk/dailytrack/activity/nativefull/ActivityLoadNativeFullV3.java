package com.pedometer.steptracker.runwalk.dailytrack.activity.nativefull;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.mallegan.ads.util.AppOpenManager;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.base.BaseActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivityNativeFullScreenBinding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SystemConfiguration;

import java.util.Random;


public class ActivityLoadNativeFullV3 extends BaseActivity {
    ActivityNativeFullScreenBinding binding;
    public static final String NATIVE_FUll_AD_ID = "native_full_ad_id";
    public static final String NATIVE_FUll_HIGH_AD_ID = "native_full_high_ad_id";

    private static ActivityFullCallback callback;

    public static void open(Context context, String idHigh, String idLow,ActivityFullCallback cb) {
        callback = cb;
        Intent intent = new Intent(context, ActivityLoadNativeFullV3.class);
        intent.putExtra(NATIVE_FUll_HIGH_AD_ID, idHigh);
        intent.putExtra(NATIVE_FUll_AD_ID, idLow);
        context.startActivity(intent);
    }

    @Override
    public void bind() {
        AppOpenManager.getInstance().disableAppResumeWithActivity(ActivityLoadNativeFullV3.class);
        AppOpenManager.getInstance().disableAppResume();
        binding = ActivityNativeFullScreenBinding.inflate(getLayoutInflater());
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        setContentView(binding.getRoot());
        String adId;
        if (getIntent().hasExtra(NATIVE_FUll_AD_ID)) {
            adId = getIntent().getStringExtra(NATIVE_FUll_AD_ID);
        } else {
            adId = getString(Integer.parseInt(""));
        }

        String adIdHigh;
        if (getIntent().hasExtra(NATIVE_FUll_HIGH_AD_ID)) {
            adIdHigh = getIntent().getStringExtra(NATIVE_FUll_HIGH_AD_ID);
        } else {
            adIdHigh = getString(Integer.parseInt(""));
        }

        loadNativeFull(adIdHigh,adId);
    }

    private void loadNativeFull(String adIdHigh,String adIdLow) {
        Admob.getInstance().loadNativeAds(this, adIdHigh, 1, new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                Admob.getInstance().loadNativeAds(ActivityLoadNativeFullV3.this, adIdLow, 1, new NativeCallback() {
                    @Override
                    public void onAdFailedToLoad() {
                        super.onAdFailedToLoad();
                        binding.frAdsFull.setVisibility(View.GONE);
                        if (callback != null) {
                            callback.onResultFromActivityFull();
                        }
                        clearActivity();
                    }

                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        super.onNativeAdLoaded(nativeAd);
                        if (isFinishing() || isDestroyed()) return;
                        NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeFullV3.this)
                                .inflate(R.layout.native_full, null);
                        ImageView closeButton = adView.findViewById(R.id.close);
                        MediaView mediaView = adView.findViewById(R.id.ad_media);
                        Random random = new Random();
                        int percent = random.nextInt(100); // 0 - 99

                        if (percent < 40) { // 40% chạy action mediaView.performClick()
                            closeButton.setOnClickListener(v -> mediaView.performClick());

                        } else { // 60% chạy CountDownTimer
                            closeButton.setVisibility(View.INVISIBLE);

                            new CountDownTimer(2000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {}

                                @Override
                                public void onFinish() {
                                    closeButton.setVisibility(View.VISIBLE);

                                    closeButton.setOnClickListener(v -> {
                                        if (callback != null) {
                                            callback.onResultFromActivityFull();
                                        }
                                        finish();
                                    });
                                }
                            }.start();
                        }
                        binding.frAdsFull.removeAllViews();
                        if (!isFinishing() && !isDestroyed()) {
                            binding.frAdsFull.removeAllViews();
                            binding.frAdsFull.addView(adView);
                            Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                        }
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                    }
                });
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                if (isFinishing() || isDestroyed()) return;
                NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeFullV3.this)
                        .inflate(R.layout.native_full, null);
                ImageView closeButton = adView.findViewById(R.id.close);
                MediaView mediaView = adView.findViewById(R.id.ad_media);
                closeButton.setOnClickListener(v -> mediaView.performClick());
                new CountDownTimer(5000, 1000) {
                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        closeButton.setOnClickListener(v -> {
                            if (callback != null) {
                                callback.onResultFromActivityFull();
                                clearActivity();
                            }
                        });
                    }
                }.start();
                binding.frAdsFull.removeAllViews();
                if (!isFinishing() && !isDestroyed()) {
                    binding.frAdsFull.removeAllViews();
                    binding.frAdsFull.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void clearActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

}
