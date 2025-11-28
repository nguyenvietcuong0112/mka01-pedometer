package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.mallegan.ads.util.AppOpenManager;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.base.BaseActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivitySettingsBinding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.BottomNavigationHelper;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SettingActivity extends BaseActivity {
    ActivitySettingsBinding binding;
    private boolean isBtnProcessing = false;

    @Override
    public void bind() {
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.btnShare.setOnClickListener(v -> {
            if (isBtnProcessing) return;
            isBtnProcessing = true;

            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String body = "có link app thì điền vào";
            String sub = "Flash Alert App";
            myIntent.putExtra(Intent.EXTRA_SUBJECT, sub);
            myIntent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(myIntent, "Share"));
            AppOpenManager.getInstance().disableAppResumeWithActivity(HomeActivity.class);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isBtnProcessing = false;
                }
            }, 1000);
        });

        binding.btnLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, LanguageActivity.class);
            intent.putExtra("from_settings", true);
            startActivity(intent);
        });

        binding.btnRateUs.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                this.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                this.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=")));
            }
        });


        binding.btnPrivacyPolicy.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://mohamedezzeldin.netlify.app/policy");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        BottomNavigationHelper.setupBottomNavigation(this, BottomNavigationHelper.NAV_SETTINGS);

    }


    private void loadAds() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_setting), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(SettingActivity.this).inflate(R.layout.layout_native_language_non_organic, null);

                binding.frAds.setVisibility(View.VISIBLE);
                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAds.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SharePreferenceUtils.isOrganic(this)) {
            loadAds();
        } else {
            binding.frAds.setVisibility(View.GONE);
            binding.frAds.removeAllViews();
        }
    }
}
