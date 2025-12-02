package com.pedometer.steptracker.runwalk.dailytrack.activity.nativefull;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.base.BaseActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivityNativeFullBinding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SystemConfiguration;

import java.util.Random;

public class ActivityLoadNativeFullV5 extends BaseActivity {
    ActivityNativeFullBinding binding;
    public static final String NATIVE_FUll_AD_ID_HIGH = "native_full_ad_id_high";

    public static final String NATIVE_FUll_AD_ID = "native_full_ad_id";

    private static ActivityFullCallback callback;

    public static void open(Context context, String high,String low, ActivityFullCallback cb) {
        callback = cb;
        Intent intent = new Intent(context, ActivityLoadNativeFullV5.class);
        intent.putExtra(NATIVE_FUll_AD_ID_HIGH,high);
        intent.putExtra(NATIVE_FUll_AD_ID,low);
        context.startActivity(intent);
    }

    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String adIdHigh,adIdLow;
        if (getIntent().hasExtra(NATIVE_FUll_AD_ID_HIGH)) {
            adIdHigh = getIntent().getStringExtra(NATIVE_FUll_AD_ID_HIGH);
        } else {
            adIdHigh = getString(Integer.parseInt(""));
        }
        if (getIntent().hasExtra(NATIVE_FUll_AD_ID)) {
            adIdLow = getIntent().getStringExtra(NATIVE_FUll_AD_ID);
        } else {
            adIdLow = getString(Integer.parseInt(""));
        }
        loadNativeFull(adIdHigh,adIdLow);
    }


    private void loadNativeFull(String adIdHigh,String adIdLow) {
        Admob.getInstance().loadNativeAds(this, adIdHigh, 1, new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                Admob.getInstance().loadNativeAds(ActivityLoadNativeFullV5.this, adIdLow, 1, new NativeCallback() {
                    @Override
                    public void onAdFailedToLoad() {
                        super.onAdFailedToLoad();
                        binding.frAdsFull.setVisibility(View.GONE);
                        if (callback != null) {
                            callback.onResultFromActivityFull();
                        }
                        finish();
                    }

                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        super.onNativeAdLoaded(nativeAd);
                        NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeFullV5.this)
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
                        binding.frAdsFull.addView(adView);
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                    }
                });
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeFullV5.this)
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
                            }
                            finish();
                        });
                    }
                }.start();
                binding.frAdsFull.removeAllViews();
                binding.frAdsFull.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }
    int count = 0;

    @Override
    protected void onResume() {
        super.onResume();
        count++;
        if (count >= 2) {
            if (callback != null) {
                callback.onResultFromActivityFull();
            }
            finish();
        }
    }

}

