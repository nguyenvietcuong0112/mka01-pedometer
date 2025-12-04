package com.pedometer.steptracker.runwalk.dailytrack;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.appsflyer.AppsFlyerConversionListener;
import com.facebook.FacebookSdk;
import com.google.firebase.FirebaseApp;
import com.mallegan.ads.util.AdsApplication;
import com.mallegan.ads.util.AppOpenManager;
import com.mallegan.ads.util.AppsFlyer;
import com.pedometer.steptracker.runwalk.dailytrack.activity.LanguageActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.MainActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.PermissionActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.SplashActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.SplashActivityUninstall;
import com.pedometer.steptracker.runwalk.dailytrack.activity.fragmentIntro.IntroActivityNew;
import com.pedometer.steptracker.runwalk.dailytrack.utils.AppActivityTracker;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;
//import com.stepcounter.healthapplines.pedometer.steptracker.com.pedometer.steptracker.runwalk.dailytrack.utils.TimerManager;


import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
        AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(IntroActivityNew.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(ProfileActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(PermissionActivity.class);

        FacebookSdk.setClientToken(getString(R.string.facebook_client_token));

        AppsFlyer.getInstance().initAppFlyer(this, getString(R.string.AF_DEV_KEY), true);
        AppActivityTracker.getInstance().register(this);

    }
    @Override
    public void onTerminate() {
        super.onTerminate();
//        TimerManager.getInstance().stopTimer();
    }

    public static Context getLocalizedContext2(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    public void updateShortcuts(String langCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            if (shortcutManager == null) return;

            Context localizedContext = getLocalizedContext2(getApplicationContext(), langCode);

            Intent uninstallIntent = new Intent(this, SplashActivityUninstall.class);
            uninstallIntent.setAction(Intent.ACTION_VIEW);
            uninstallIntent.putExtra("shortcut", "uninstall_fake");
            uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            ShortcutInfo uninstallShortcut = new ShortcutInfo.Builder(this, "id_uninstall_fake")
                    .setShortLabel(localizedContext.getString(R.string.uninstall))
                    .setLongLabel(localizedContext.getString(R.string.uninstall))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_uninstall))
                    .setIntent(uninstallIntent)
                    .build();

            shortcutManager.removeAllDynamicShortcuts();
            shortcutManager.setDynamicShortcuts(Collections.singletonList(uninstallShortcut));
        }
    }


}