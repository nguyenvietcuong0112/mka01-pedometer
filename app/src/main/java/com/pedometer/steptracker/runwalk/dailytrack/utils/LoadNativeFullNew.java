package com.pedometer.steptracker.runwalk.dailytrack.utils;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.base.BaseActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivityNativeFullBinding;


public class LoadNativeFullNew extends BaseActivity {
    ActivityNativeFullBinding binding;
    public static final String EXTRA_NATIVE_AD_ID = "extra_native_ad_id";
    private CountDownTimer countDownTimer;
    private ValueAnimator animator;
    private boolean isAdClicked = false;
    private boolean isTimerFinished = false;


    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String adId;
        if (getIntent().hasExtra(EXTRA_NATIVE_AD_ID)) {
            adId = getIntent().getStringExtra(EXTRA_NATIVE_AD_ID);
        } else {
            adId = getString(Integer.parseInt(""));
        }

        loadNativeFull(adId);
    }

    private void loadNativeFull(String adId) {
        Admob.getInstance().loadNativeAds(this, adId, 1, new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAdsFull.setVisibility(View.VISIBLE);
                finish();
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(LoadNativeFullNew.this)
                        .inflate(R.layout.layout_native_full_new, null);
                ImageView closeButton = adView.findViewById(R.id.close);
                @SuppressLint("CutPasteId") TextView tvCountdown = adView.findViewById(R.id.tvCountdown);
                @SuppressLint("CutPasteId") CountdownView progressStroke = adView.findViewById(R.id.progressStroke);
                MediaView mediaView = adView.findViewById(R.id.ad_media);
                FrameLayout frCountDown = adView.findViewById(R.id.frCountdown);

                frCountDown.setVisibility(View.GONE);
                closeButton.setVisibility(View.VISIBLE);

                View.OnClickListener adClickListener = v -> {
                    isAdClicked = true;
                    stopCountdown();
                };

                mediaView.setOnClickListener(adClickListener);

                countDownTimer = new CountDownTimer(5000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        isTimerFinished = true;
                    }
                };
                countDownTimer.start();

                closeButton.setOnClickListener(v -> {
                    if (!isTimerFinished && !isAdClicked) {
                        mediaView.performClick();
                    } else {
                        finish();
                    }
                });

                binding.frAdsFull.removeAllViews();
                binding.frAdsFull.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }

    private void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (animator != null) {
            animator.end();
        }
    }

    private  int  isLoadNativeFullNew=0;

    @Override
    protected void onResume() {
        super.onResume();
        isLoadNativeFullNew++;
        if (isLoadNativeFullNew>=2){
            finish();
        }
    }
}
