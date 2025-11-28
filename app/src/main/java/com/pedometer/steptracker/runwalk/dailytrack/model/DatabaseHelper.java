package com.pedometer.steptracker.runwalk.dailytrack.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pedometer.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "steps";

    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_STEPS = "steps";
    private static final String COLUMN_GOAL = "goal";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_TIME = "time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_DATE + " TEXT PRIMARY KEY, "
                + COLUMN_STEPS + " INTEGER, "
                + COLUMN_GOAL + " INTEGER, "
                + COLUMN_CALORIES + " REAL, "
                + COLUMN_DISTANCE + " REAL, "
                + COLUMN_TIME + " INTEGER);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void saveStepData(int steps, int goal, double calories, double distance, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_DATE, getCurrentDate());
        values.put(COLUMN_STEPS, steps);
        values.put(COLUMN_GOAL, goal);
        values.put(COLUMN_CALORIES, calories);
        values.put(COLUMN_DISTANCE, distance);
        values.put(COLUMN_TIME, time);

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public StepData getStepDataForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        StepData stepData = new StepData();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_STEPS, COLUMN_GOAL, COLUMN_CALORIES, COLUMN_DISTANCE, COLUMN_TIME},
                COLUMN_DATE + "=?",
                new String[]{date},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            stepData.steps = cursor.getInt(0);
            stepData.goal = cursor.getInt(1);
            stepData.calories = cursor.getDouble(2);
            stepData.distance = cursor.getDouble(3);
            stepData.time = cursor.getLong(4);
            cursor.close();
        }

        db.close();
        return stepData;
    }

    public StepData getTodayStepData() {
        return getStepDataForDate(getCurrentDate());
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static class StepData {
        public int steps = 0;
        public int goal = 6000;
        public double calories = 0;
        public double distance = 0;
        public long time = 0;
    }

    public MonthlyStepData getMonthlyStepData() {
        SQLiteDatabase db = this.getReadableDatabase();
        MonthlyStepData monthlyData = new MonthlyStepData();

        String query = "SELECT SUM(steps) as total_steps, SUM(calories) as total_calories, " +
                "SUM(distance) as total_distance, SUM(time) as total_time " +
                "FROM steps WHERE date >= date('now', 'start of month')";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            int totalStepsIndex = cursor.getColumnIndex("total_steps");
            int totalCaloriesIndex = cursor.getColumnIndex("total_calories");
            int totalDistanceIndex = cursor.getColumnIndex("total_distance");
            int totalTimeIndex = cursor.getColumnIndex("total_time");

            if (totalStepsIndex != -1 && totalCaloriesIndex != -1 &&
                    totalDistanceIndex != -1 && totalTimeIndex != -1) {
                monthlyData.totalSteps = cursor.getInt(totalStepsIndex);
                monthlyData.totalCalories = cursor.getDouble(totalCaloriesIndex);
                monthlyData.totalDistance = cursor.getDouble(totalDistanceIndex);
                monthlyData.totalTime = cursor.getLong(totalTimeIndex);
            }
        }
        cursor.close();
        return monthlyData;
    }

    public List<DailyStepData> getLast30DaysData() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<DailyStepData> dailyData = new ArrayList<>();

        String query = "SELECT date, steps, calories, distance FROM steps WHERE date >= date('now', '-30 days') ORDER BY date ASC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            int dateColumnIndex = cursor.getColumnIndex("date");
            int stepsColumnIndex = cursor.getColumnIndex("steps");
            int caloriesColumnIndex = cursor.getColumnIndex("calories");
            int distanceColumnIndex = cursor.getColumnIndex("distance");

            if (dateColumnIndex != -1 && stepsColumnIndex != -1 &&
                    caloriesColumnIndex != -1 && distanceColumnIndex != -1) {
                do {
                    DailyStepData data = new DailyStepData();
                    data.date = cursor.getString(dateColumnIndex);
                    data.steps = cursor.getInt(stepsColumnIndex);
                    data.calories = cursor.getFloat(caloriesColumnIndex);
                    data.distance = cursor.getFloat(distanceColumnIndex);
                    dailyData.add(data);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        return dailyData;
    }

    /**
     * Get data for a specific month
     * @param monthIndex 0 for January, 1 for February, etc.
     * @return List of DailyStepData objects for the given month
     */
    public List<DailyStepData> getMonthData(int monthIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<DailyStepData> dailyData = new ArrayList<>();

        // Get current year
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Create the start and end date of the month
        Calendar startCal = Calendar.getInstance();
        startCal.set(currentYear, monthIndex, 1, 0, 0, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(currentYear, monthIndex, startCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(startCal.getTime());
        String endDate = sdf.format(endCal.getTime());

        String query = "SELECT date, steps, calories, distance FROM " + TABLE_NAME +
                " WHERE date >= ? AND date <= ? ORDER BY date ASC";

        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            int dateColumnIndex = cursor.getColumnIndex("date");
            int stepsColumnIndex = cursor.getColumnIndex("steps");
            int caloriesColumnIndex = cursor.getColumnIndex("calories");
            int distanceColumnIndex = cursor.getColumnIndex("distance");

            if (dateColumnIndex != -1 && stepsColumnIndex != -1 &&
                    caloriesColumnIndex != -1 && distanceColumnIndex != -1) {
                do {
                    DailyStepData data = new DailyStepData();
                    data.date = cursor.getString(dateColumnIndex);
                    data.steps = cursor.getInt(stepsColumnIndex);
                    data.calories = cursor.getFloat(caloriesColumnIndex);
                    data.distance = cursor.getFloat(distanceColumnIndex);
                    dailyData.add(data);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        return dailyData;
    }

    /**
     * Get monthly data for a specific statistic type
     * @param monthIndex 0 for January, 1 for February, etc.
     * @param statisticType 0 for steps, 1 for distance, 2 for calories
     * @return MonthlyStepData containing the totals
     */
    public MonthlyStepData getMonthlyStatData(int monthIndex, int statisticType) {
        SQLiteDatabase db = this.getReadableDatabase();
        MonthlyStepData monthlyData = new MonthlyStepData();

        // Get current year
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Create the start and end date of the month
        Calendar startCal = Calendar.getInstance();
        startCal.set(currentYear, monthIndex, 1, 0, 0, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(currentYear, monthIndex, startCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(startCal.getTime());
        String endDate = sdf.format(endCal.getTime());

        String query = "SELECT SUM(steps) as total_steps, SUM(calories) as total_calories, " +
                "SUM(distance) as total_distance, SUM(time) as total_time " +
                "FROM " + TABLE_NAME + " WHERE date >= ? AND date <= ?";

        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            int totalStepsIndex = cursor.getColumnIndex("total_steps");
            int totalCaloriesIndex = cursor.getColumnIndex("total_calories");
            int totalDistanceIndex = cursor.getColumnIndex("total_distance");
            int totalTimeIndex = cursor.getColumnIndex("total_time");

            if (totalStepsIndex != -1 && totalCaloriesIndex != -1 &&
                    totalDistanceIndex != -1 && totalTimeIndex != -1) {
                monthlyData.totalSteps = cursor.getInt(totalStepsIndex);
                monthlyData.totalCalories = cursor.getDouble(totalCaloriesIndex);
                monthlyData.totalDistance = cursor.getDouble(totalDistanceIndex);
                monthlyData.totalTime = cursor.getLong(totalTimeIndex);
            }
        }
        cursor.close();
        return monthlyData;
    }

    public static class MonthlyStepData {
        public int totalSteps;
        public double totalCalories;
        public double totalDistance;
        public long totalTime;
    }

    public static class DailyStepData {
        public String date;
        public int steps;
        public float calories;
        public float distance;
    }


}