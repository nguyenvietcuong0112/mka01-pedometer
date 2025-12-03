package com.pedometer.steptracker.runwalk.dailytrack.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ProfileDataManager {
    private static final String PREFS_NAME = "user_profile";
    private static final String KEY_GENDER = "gender"; // "male" or "female"
    private static final String KEY_NAME = "name";
    private static final String KEY_HEIGHT = "height"; // in cm
    private static final String KEY_WEIGHT = "weight"; // in kg
    private static final String KEY_WEIGHT_GOAL = "weight_goal"; // in kg
    private static final String KEY_WEIGHT_UPDATED_AT = "weight_updated_at";
    private static final String KEY_PROFILE_COMPLETED = "profile_completed";

    public static void saveGender(Context context, String gender) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_GENDER, gender).apply();
    }

    public static String getGender(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_GENDER, "");
    }

    public static void saveName(Context context, String name) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_NAME, name).apply();
    }

    public static String getName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_NAME, "");
    }

    public static void saveHeight(Context context, float height) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat(KEY_HEIGHT, height).apply();
    }

    public static float getHeight(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_HEIGHT, 0f);
    }

    public static void saveWeight(Context context, float weight) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat(KEY_WEIGHT, weight)
                .putLong(KEY_WEIGHT_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    public static float getWeight(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_WEIGHT, 0f);
    }

    public static void saveWeightGoal(Context context, float weightGoal) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat(KEY_WEIGHT_GOAL, weightGoal).apply();
    }

    public static float getWeightGoal(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_WEIGHT_GOAL, 0f);
    }

    public static long getWeightUpdatedAt(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_WEIGHT_UPDATED_AT, 0L);
    }

    public static void setProfileCompleted(Context context, boolean completed) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_PROFILE_COMPLETED, completed).apply();
    }

    public static boolean isProfileCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_PROFILE_COMPLETED, false);
    }

    public static void clearProfile(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}

