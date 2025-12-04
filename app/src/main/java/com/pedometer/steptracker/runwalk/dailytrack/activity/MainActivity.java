package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.activity.nativefull.ActivityFullCallback;
import com.pedometer.steptracker.runwalk.dailytrack.activity.nativefull.ActivityLoadNativeFullV2;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.AchievementFragment;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.ActivityFragment;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.HomeFragment;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.ReportFragment;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.SettingsFragment;
import com.pedometer.steptracker.runwalk.dailytrack.service.StepCounterService;
import com.pedometer.steptracker.runwalk.dailytrack.utils.BottomNavigationHelper;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME = "home";
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_REPORT = "report";
    private static final String TAG_ACHIEVEMENT = "achievement";
    private static final String TAG_SETTINGS = "settings";

    private FrameLayout frAdsBanner;

    private Handler interHandler = new Handler();
    private Runnable interRunnable;
    private boolean isShowingInter = false;
    private static final long INTER_REPEAT_DELAY_MS = 15_000L;
    private long nextInterAllowedAt = 0L;
    private boolean hasShownInterOnce = false;

    // NEW: flag to avoid showing interstitial on initial activity load
    private boolean isInitialLoad = true;

    private int currentNav = BottomNavigationHelper.NAV_STEPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PermissionActivity.hasAllRequiredPermissions(this)) {
            Intent intent = new Intent(this, PermissionActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (!com.pedometer.steptracker.runwalk.dailytrack.utils.ProfileDataManager.isProfileCompleted(this)) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        frAdsBanner = findViewById(R.id.fr_ads_banner);

        startStepServiceIfNeeded();

        int initialNav = getIntent().getIntExtra("nav", BottomNavigationHelper.NAV_STEPS);
        currentNav = initialNav;

        // Reset any scheduled cooldown so first navigation can show interstitial on user click
        cancelScheduledInter();
        nextInterAllowedAt = 0L;
        hasShownInterOnce = false;

        // Load initial fragment but DO NOT treat this as a "nav click"
        loadFragment(initialNav);
        // After initial load, subsequent loadFragment(...) calls count as user navigation
        isInitialLoad = false;

        setupBottomNavigation();
        loadAdsBanner();
    }

    private void startStepServiceIfNeeded() {
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationHelper.setupBottomNavigation(this, currentNav);
    }

    /**
     * Load fragment for given nav.
     * NOTE: showInterOnNavChange() is only called when not the initial load (i.e., when user actually navigates).
     */
    public void loadFragment(int nav) {
        Fragment fragment = null;
        String tag = null;

        switch (nav) {
            case BottomNavigationHelper.NAV_STEPS:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_HOME);
                if (fragment == null) {
                    fragment = new HomeFragment();
                }
                tag = TAG_HOME;
                break;
            case BottomNavigationHelper.NAV_ACTIVITY:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_ACTIVITY);
                if (fragment == null) {
                    fragment = ActivityFragment.newInstance(false);
                }
                tag = TAG_ACTIVITY;
                break;
            case BottomNavigationHelper.NAV_REPORT:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_REPORT);
                if (fragment == null) {
                    fragment = new ReportFragment();
                }
                tag = TAG_REPORT;
                break;
            case BottomNavigationHelper.NAV_ACHIEVEMENT:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_ACHIEVEMENT);
                if (fragment == null) {
                    fragment = new AchievementFragment();
                }
                tag = TAG_ACHIEVEMENT;
                break;
            case BottomNavigationHelper.NAV_SETTINGS:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS);
                if (fragment == null) {
                    fragment = new SettingsFragment();
                }
                tag = TAG_SETTINGS;
                break;
        }

        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment, tag);
            transaction.commit();
            currentNav = nav;
            setupBottomNavigation();

            // Only show interstitial when this navigation is user-initiated (i.e., not initial load)
            if (!isInitialLoad) {
                showInterOnNavChange();
            }
        }
    }

    private void showInterOnNavChange() {
        if (SharePreferenceUtils.isOrganic(MainActivity.this)) return;

        long now = System.currentTimeMillis();

        // Nếu đã show ít nhất 1 lần, áp dụng cooldown như trước
        if (hasShownInterOnce) {
            if (now < nextInterAllowedAt) return;
            if (isShowingInter) return;
        } else {
            // lần đầu (do user click), cho phép show ngay cả khi nextInterAllowedAt bị set (chúng ta reset ở onCreate)
            if (isShowingInter) return;
        }

        isShowingInter = true;

        final String interAdUnit = getString(R.string.inter_home);

        Admob.getInstance().loadAndShowInter(
                MainActivity.this,
                interAdUnit,
                0,
                30000,
                new com.mallegan.ads.callback.InterCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        isShowingInter = false;
                        // chỉ mark đã show 1 lần khi ad thực sự được shown và user đóng nó
                        hasShownInterOnce = true;
                        openNativeFullThenScheduleNext();
                    }

                    @Override
                    public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        isShowingInter = false;
                        // KHÔNG set hasShownInterOnce = true ở đây để tránh bị block bởi cooldown
                        // Nếu muốn fallback UI, mở native-full nhưng KHÔNG schedule cooldown
                        openNativeFullFallbackNoSchedule();
                    }

                    @Override
                    public void onAdFailedToShow(com.google.android.gms.ads.AdError adError) {
                        super.onAdFailedToShow(adError);
                        isShowingInter = false;
                        // KHÔNG set hasShownInterOnce = true
                        // fallback nhưng không schedule cooldown
                        openNativeFullFallbackNoSchedule();
                    }
                }
        );
    }

    /**
     * Mở native-full và sau khi hoàn tất sẽ gọi scheduleNextInter() như cũ.
     * Dùng khi interstitial đã được show và user đóng nó.
     */
    private void openNativeFullThenScheduleNext() {
        ActivityLoadNativeFullV2.open(MainActivity.this, getString(R.string.native_full_inter_finish), new ActivityFullCallback() {
            @Override
            public void onResultFromActivityFull() {
                scheduleNextInter();
            }
        });
    }

    /**
     * Mở native-full fallback **nhưng không** schedule cooldown khi fallback do interstitial fail.
     */
    private void openNativeFullFallbackNoSchedule() {
        ActivityLoadNativeFullV2.open(MainActivity.this, getString(R.string.native_full_inter_finish), new ActivityFullCallback() {
            @Override
            public void onResultFromActivityFull() {
                // intentionally do nothing: fallback UI completed but do NOT schedule next interstitial cooldown
            }
        });
    }

    private void scheduleNextInter() {
        if (SharePreferenceUtils.isOrganic(MainActivity.this)) return;

        nextInterAllowedAt = System.currentTimeMillis() + INTER_REPEAT_DELAY_MS;

        if (interRunnable != null) {
            interHandler.removeCallbacks(interRunnable);
        }

        interRunnable = new Runnable() {
            @Override
            public void run() {
                interRunnable = null;
                nextInterAllowedAt = 0L;
            }
        };

        interHandler.postDelayed(interRunnable, INTER_REPEAT_DELAY_MS);
    }

    private void cancelScheduledInter() {
        if (interRunnable != null) {
            interHandler.removeCallbacks(interRunnable);
            interRunnable = null;
        }
        nextInterAllowedAt = 0L;
    }

    private void loadAdsBanner() {
        if(!SharePreferenceUtils.isOrganic(MainActivity.this)) {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_home), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(MainActivity.this).inflate(R.layout.ad_native_admob_banner_1, null);
                    frAdsBanner.setVisibility(View.VISIBLE);
                    frAdsBanner.removeAllViews();
                    frAdsBanner.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    frAdsBanner.setVisibility(View.GONE);
                }
            });

        } else {
            frAdsBanner.removeAllViews();
            frAdsBanner.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelScheduledInter();
        interHandler.removeCallbacksAndMessages(null);
    }

}
