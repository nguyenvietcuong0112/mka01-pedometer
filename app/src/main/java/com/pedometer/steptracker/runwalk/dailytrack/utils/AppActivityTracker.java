package com.pedometer.steptracker.runwalk.dailytrack.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class AppActivityTracker implements Application.ActivityLifecycleCallbacks {

    private static AppActivityTracker instance;
    private Activity currentActivity;

    private AppActivityTracker() {}

    public static AppActivityTracker getInstance() {
        if (instance == null) {
            instance = new AppActivityTracker();
        }
        return instance;
    }

    public void register(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {}

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity; // Track the current activity
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null; // Clear when activity is paused
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}
}
