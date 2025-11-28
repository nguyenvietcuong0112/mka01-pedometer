package com.pedometer.steptracker.runwalk.dailytrack.utils;


import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.base.BaseActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivityNativeFullBinding;


public class ActivityLoadNativeFull extends BaseActivity {
    ActivityNativeFullBinding binding;
    public static final String EXTRA_NATIVE_AD_ID = "extra_native_ad_id";

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
                NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeFull.this)
                        .inflate(R.layout.native_full_language, null);

                ImageView closeButton = adView.findViewById(R.id.close);
                closeButton.setOnClickListener(v -> finish());

                new Handler().postDelayed(() -> {
                    closeButton.setVisibility(View.VISIBLE);
                }, 5000);

                binding.frAdsFull.removeAllViews();
                binding.frAdsFull.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }
}
