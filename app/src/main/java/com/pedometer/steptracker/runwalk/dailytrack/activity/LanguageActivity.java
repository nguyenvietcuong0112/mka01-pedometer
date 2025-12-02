package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.MyApplication;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.activity.fragmentIntro.IntroActivityNew;
import com.pedometer.steptracker.runwalk.dailytrack.activity.nativefull.ActivityLoadNativeFullV2;
import com.pedometer.steptracker.runwalk.dailytrack.base.BaseActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivityLanguageBinding;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivityLanguageStartBinding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.NativeFullLanguage;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SystemConfiguration;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SystemUtil;
import com.pedometer.steptracker.runwalk.dailytrack.utils.language.ConstantLangage;
import com.pedometer.steptracker.runwalk.dailytrack.utils.language.UILanguageCustom;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;


public class  LanguageActivity extends BaseActivity implements UILanguageCustom.OnItemClickListener {

    String codeLang = "";
    String langDevice = "en";
    ActivityLanguageStartBinding binding;
    private boolean loadNativeSelected = true;

    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        SystemUtil.setLocale(this);
        binding = ActivityLanguageStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemUtil.setLocale(this);
        Configuration config = new Configuration();
        Locale locale = Locale.getDefault();
        langDevice = locale.getLanguage();
        this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
        Locale.setDefault(locale);
        config.locale = locale;
//        checkFullAds();

        setUpLayoutLanguage();
        binding.btnSave.setOnClickListener(v -> {
            if (loadNativeSelected) {
                Toast.makeText(this, R.string.please_select_language_to_continue, Toast.LENGTH_SHORT).show();
            } else {
                if (!SharePreferenceUtils.isOrganic(LanguageActivity.this)) {
                    ActivityLoadNativeFullV2.open(LanguageActivity.this, getString(R.string.native_full_language), this::gotoIntro);
                } else {
                    gotoIntro();
                }
                ((MyApplication) getApplication()).updateShortcuts(codeLang);

            }

        });

        if (SystemUtil.isNetworkConnected(LanguageActivity.this)) {
            binding.frAds.setVisibility(View.VISIBLE);
        }

        binding.btnSave.setAlpha(0.3f);
        loadAds();
    }

    private void gotoIntro() {
        SystemUtil.saveLocale(this, codeLang);
        startActivity(new Intent(LanguageActivity.this, IntroActivityNew.class));
        finish();
    }


    private void setUpLayoutLanguage() {
        binding.tvTitle.setText(getString(R.string.languages));
        binding.tvSubtitle.setText(getString(R.string.please_select_language_to_continue));
        binding.uiLanguage.upDateData(ConstantLangage.getLanguage4(this));
        binding.uiLanguage.setOnItemClickListener(this);
    }

//    private void checkFullAds() {
//        if (SharePreferenceUtils.isOrganicNoti(this)) {
//            AppsFlyerLib.getInstance().registerConversionListener(this, new AppsFlyerConversionListener() {
//                @Override
//                public void onConversionDataSuccess(Map<String, Object> conversionData) {
//                    String mediaSource = (String) conversionData.get("media_source");
//                    if (mediaSource == null || mediaSource.isEmpty() || "organic".equals(mediaSource)) {
//                        SharePreferenceUtils.setOrganicNoti(getApplicationContext(), true);
//                    } else {
//                        SharePreferenceUtils.setOrganicNoti(getApplicationContext(), false);
//                    }
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


    private void loadAds() {
        Admob.getInstance().loadNativeAd(LanguageActivity.this, getString(R.string.native_language), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                new NativeAdView(LanguageActivity.this);
                NativeAdView adView;
                if (!SharePreferenceUtils.isOrganic(LanguageActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(LanguageActivity.this).inflate(R.layout.layout_native_language_non_organic, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(LanguageActivity.this).inflate(R.layout.layout_native_language, null);
                }
                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAds.removeAllViews();
            }
        });
    }

    public void loadAdsNativeLanguageSelect() {
        NativeAdView adView;
        if (SharePreferenceUtils.isOrganic(this)) {
            adView = (NativeAdView) LayoutInflater.from(this).inflate(R.layout.layout_native_language, null);
        } else {
            adView = (NativeAdView) LayoutInflater.from(this).inflate(R.layout.layout_native_language_non_organic, null);
        }

        Admob.getInstance().loadNativeAdFloor(LanguageActivity.this, new ArrayList<>(Arrays.asList(getString(R.string.native_language_select_high), getString(R.string.native_language_select))), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                binding.handAnim.setVisibility(View.VISIBLE);
                binding.progressSave.setVisibility(View.GONE);

            }

            @Override
            public void onAdFailedToLoad() {
                binding.frAds.removeAllViews();
                binding.handAnim.setVisibility(View.VISIBLE);
                binding.progressSave.setVisibility(View.GONE);
            }
        });
        loadNativeSelected = false;

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onItemClickListener(int position, boolean itemseleted, String codeLang2) {
        if (!codeLang2.isEmpty()) {
            codeLang = codeLang2;
            SystemUtil.saveLocale(getBaseContext(), codeLang);
            updateLocale(codeLang);
        }
        if (itemseleted) {
            binding.btnSave.setAlpha(1.0f);
        }
        binding.handAnim.setVisibility(View.GONE);
        binding.progressSave.setVisibility(View.VISIBLE);
        loadAdsNativeLanguageSelect();
    }

    private void updateLocale(String langCode) {
        Locale newLocale = new Locale(langCode);
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.locale = newLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        binding.tvTitle.setText(getString(R.string.languages));
        binding.tvSubtitle.setText(getString(R.string.please_select_language_to_continue));
        binding.btnSave.setText(getString(R.string.done));
        binding.uiLanguage.upDateData(ConstantLangage.getLanguage4(this));
    }

    @Override
    public void onPreviousPosition(int pos) {

    }
}