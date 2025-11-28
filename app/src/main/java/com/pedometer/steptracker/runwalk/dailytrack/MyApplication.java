package com.pedometer.steptracker.runwalk.dailytrack;


import com.appsflyer.AppsFlyerConversionListener;
import com.facebook.FacebookSdk;
import com.google.firebase.FirebaseApp;
import com.mallegan.ads.util.AdsApplication;
import com.mallegan.ads.util.AppOpenManager;
import com.mallegan.ads.util.AppsFlyer;
import com.pedometer.steptracker.runwalk.dailytrack.activity.IntroActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.LanguageActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.SplashActivity;
import com.pedometer.steptracker.runwalk.dailytrack.utils.AppActivityTracker;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;
//import com.stepcounter.healthapplines.pedometer.steptracker.com.pedometer.steptracker.runwalk.dailytrack.utils.TimerManager;


import java.util.List;
import java.util.Map;

public class MyApplication extends AdsApplication {

    @Override
    public boolean enableAdsResume() {
        return true;
    }

    @Override
    public List<String> getListTestDeviceId() {
        return null;
    }

    @Override
    public String getResumeAdId() {
        return getString(R.string.open_resume);
    }

    @Override
    public Boolean buildDebug() {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(LanguageActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(IntroActivity.class);

        FacebookSdk.setClientToken(getString(R.string.facebook_client_token));

        if (!SharePreferenceUtils.isOrganic(getApplicationContext())) {
            AppsFlyer.getInstance().initAppFlyer(this, getString(R.string.AF_DEV_KEY), true);

        } else {
            AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {
                    String mediaSource = (String) conversionData.get("media_source");

                    SharePreferenceUtils.setOrganicValue(getApplicationContext(), mediaSource == null || mediaSource.isEmpty() || mediaSource.equals("organic"));
                }

                @Override
                public void onConversionDataFail(String errorMessage) {
                    // Handle conversion data failure
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> attributionData) {
                    // Handle app open attribution
                }

                @Override
                public void onAttributionFailure(String errorMessage) {
                    // Handle attribution failure
                }
            };

            AppsFlyer.getInstance().initAppFlyer(this, getString(R.string.AF_DEV_KEY), true, conversionListener);

        }
        AppActivityTracker.getInstance().register(this);

    }
    @Override
    public void onTerminate() {
        super.onTerminate();
//        TimerManager.getInstance().stopTimer();
    }


}