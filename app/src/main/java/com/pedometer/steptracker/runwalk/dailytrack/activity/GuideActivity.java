package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.adapter.GuideAdapter;
import com.pedometer.steptracker.runwalk.dailytrack.base.BaseActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivityGuideBinding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SystemConfiguration;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SystemUtil;


public class GuideActivity extends BaseActivity implements View.OnClickListener {
    private ImageView[] dots = null;
    private ActivityGuideBinding binding;

    private boolean loadNative1 = true;
    private boolean loadNative2 = true;
    private boolean loadNative3 = true;
    private boolean loadNative4 = true;

    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (SystemUtil.isNetworkConnected(this)) {
            binding.frAds.setVisibility(View.VISIBLE);
        } else binding.frAds.setVisibility(View.GONE);
        dots = new ImageView[]{binding.cricle1, binding.cricle2, binding.cricle3, binding.cricle4};
        GuideAdapter adapter = new GuideAdapter(this);
        binding.viewPager2.setAdapter(adapter);
        setUpSlideIntro();
        binding.btnNext.setOnClickListener(this);
        loadNative1();
        loadNativeIntro2();
        loadNative3();
        loadNative4();
//        if(!SharePreferenceUtils.isOrganic(this)){
//            loadInterIntro();
//        }
        binding.ivBack.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void onClick(View v) {
        if (v == binding.btnNext) {
            if (binding.viewPager2.getCurrentItem() == 3) {
                goToHome();
            } else if (binding.viewPager2.getCurrentItem() == 2) {
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
            } else if (binding.viewPager2.getCurrentItem() == 1) {
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
            } else if (binding.viewPager2.getCurrentItem() == 0) {
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
            } else {
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
            }
        }
    }

//    public void goToHome() {
//            startActivity(new Intent(this, HomeActivity.class));
//    }

    public void goToHome() {
        Intent intent = new Intent(GuideActivity.this, MainActivity.class);
        intent.putExtra("nav", com.pedometer.steptracker.runwalk.dailytrack.utils.BottomNavigationHelper.NAV_STEPS);
        startActivity(intent);


    }

    public static boolean isAccessibilitySettingsOn(Context r7) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(r7.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.i("TAG", e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(r7.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(r7.getPackageName().toLowerCase());
            }
        }

        return false;
    }


    @Override
    public void onStart() {
        super.onStart();
        changeContentInit(binding.viewPager2.getCurrentItem());
    }

    private void setUpSlideIntro() {
        GuideAdapter adapter = new GuideAdapter(this);
        binding.viewPager2.setAdapter(adapter);

        binding.viewPager2.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeContentInit(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void changeContentInit(int position) {
        for (int i = 0; i < 4; i++) {
            if (i == position)
                dots[i].setImageResource(R.drawable.bg_indicator_true);
            else
                dots[i].setImageResource(R.drawable.bg_indicator);
        }
        if (position == 0) {
            if (loadNative1) {
                binding.frAds.setVisibility(View.VISIBLE);
                binding.frAds1.setVisibility(View.VISIBLE);
            } else {
                binding.frAds.setVisibility(View.GONE);
                binding.frAds1.setVisibility(View.GONE);
            }
            binding.frAds4.setVisibility(View.GONE);
            binding.frAds3.setVisibility(View.GONE);
            binding.frAds2.setVisibility(View.GONE);
            SystemUtil.setLocale(this);
        } else if (position == 1) {
            if (loadNative2) {
                binding.frAds.setVisibility(View.VISIBLE);
                binding.frAds2.setVisibility(View.VISIBLE);
            } else {
                binding.frAds.setVisibility(View.GONE);
                binding.frAds2.setVisibility(View.GONE);
            }
            binding.frAds1.setVisibility(View.GONE);
            binding.frAds3.setVisibility(View.GONE);
            binding.frAds4.setVisibility(View.GONE);
            SystemUtil.setLocale(this);
        } else if (position == 2) {
            if (loadNative3) {
                binding.frAds.setVisibility(View.VISIBLE);
                binding.frAds3.setVisibility(View.VISIBLE);
            } else {
                binding.frAds.setVisibility(View.GONE);
                binding.frAds3.setVisibility(View.GONE);
            }
            binding.frAds4.setVisibility(View.GONE);
            binding.frAds1.setVisibility(View.GONE);
            binding.frAds2.setVisibility(View.GONE);

            loadNative3();


            SystemUtil.setLocale(this);
        } else if (position == 3) {
            if (loadNative4) {
                binding.frAds.setVisibility(View.VISIBLE);
                binding.frAds4.setVisibility(View.VISIBLE);
            } else {
                binding.frAds.setVisibility(View.GONE);
                binding.frAds4.setVisibility(View.GONE);
            }
            binding.frAds1.setVisibility(View.GONE);
            binding.frAds2.setVisibility(View.GONE);
            binding.frAds3.setVisibility(View.GONE);
            SystemUtil.setLocale(this);
        }
        hideNavigationBar();
    }

    private void hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and later
            Window window = getWindow();
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeContentInit(0);
    }

    private void loadNative1() {
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding1), new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAds1.setVisibility(View.GONE);
                loadNative1 = false;
                checkNextButtonStatus(true);
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView;
                if (SharePreferenceUtils.isOrganic(GuideActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(GuideActivity.this)
                            .inflate(R.layout.layout_native_language, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(GuideActivity.this)
                            .inflate(R.layout.layout_native_language_non_organic, null);
                }
                loadNative1 = true;
                binding.frAds1.removeAllViews();
                binding.frAds1.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);

                checkNextButtonStatus(true);
            }
        });
    }


    private void checkNextButtonStatus(boolean isReady) {
        if (isReady) {
            binding.btnNext.setVisibility(View.VISIBLE);
            binding.btnNextLoading.setVisibility(View.GONE);
        } else {
            binding.btnNext.setVisibility(View.GONE);
            binding.btnNextLoading.setVisibility(View.VISIBLE);
        }
    }

    private void loadNative3() {
        if (!SharePreferenceUtils.isOrganic(GuideActivity.this)) {
            checkNextButtonStatus(false);
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding3), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    runOnUiThread(() -> {
                        NativeAdView adView = (NativeAdView) LayoutInflater.from(GuideActivity.this).inflate(R.layout.layout_native_introthree_non_organic, null);
                        binding.frAds3.removeAllViews();
                        binding.frAds3.addView(adView);
                        loadNative3 = true;
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                        new Handler().postDelayed(() -> {
                            checkNextButtonStatus(true);
                        }, 1500);

                    });
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    runOnUiThread(() -> {
                        loadNative3 = true;
                        binding.frAds3.setVisibility(View.GONE);
                        checkNextButtonStatus(true);
                    });
                }

            });
        } else {
            loadNative3 = false;
            binding.frAds3.removeAllViews();
            binding.frAds3.setVisibility(View.GONE);
        }
    }

    private void loadNativeIntro2() {
        if (!SharePreferenceUtils.isOrganic(GuideActivity.this)) {
            checkNextButtonStatus(false);
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding2), new NativeCallback() {
                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    runOnUiThread(() -> {
                        binding.frAds2.removeAllViews();
                        binding.frAds2.setVisibility(View.GONE);
                        loadNative2 = false;
                        checkNextButtonStatus(true);

                    });

                }

                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    runOnUiThread(() -> {
                        NativeAdView adView = (NativeAdView) LayoutInflater.from(GuideActivity.this).inflate(R.layout.layout_native_introtwo_non_organic, null);
                        binding.frAds2.removeAllViews();
                        binding.frAds2.addView(adView);
                        loadNative2 = true;
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                        new Handler().postDelayed(() -> {
                            checkNextButtonStatus(true);
                        }, 1500);

                    });

                }
            });
        } else {
            loadNative2 = false;
            binding.frAds2.removeAllViews();
            binding.frAds2.setVisibility(View.GONE);
        }
    }

    private void loadNative4() {
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding4), new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                runOnUiThread(() -> {
                    loadNative4 = false;
                    binding.frAds4.removeAllViews();
                    binding.frAds4.setVisibility(View.GONE);
                    checkNextButtonStatus(true);
                });

            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView;
                if (SharePreferenceUtils.isOrganic(GuideActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(GuideActivity.this)
                            .inflate(R.layout.layout_native_language, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(GuideActivity.this)
                            .inflate(R.layout.layout_native_language_non_organic, null);
                }
                runOnUiThread(() -> {
                    loadNative4 = true;
                    binding.frAds4.removeAllViews();
                    binding.frAds4.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                    new Handler().postDelayed(() -> {
                        checkNextButtonStatus(true);
                    }, 1500);
                });
            }
        });

    }

//    private void loadInterIntro() {
//        Admob.getInstance().loadInterAds(this, getString(R.string.inter_intro), new InterCallback() {
//            @Override
//            public void onInterstitialLoad(InterstitialAd interstitialAd) {
//                super.onInterstitialLoad(interstitialAd);
//                Constant.interIntro = interstitialAd;
//            }
//        });
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}