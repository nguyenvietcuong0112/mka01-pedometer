package com.pedometer.steptracker.runwalk.dailytrack.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightHistoryHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weight_history.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "weight_entries";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_WEIGHT = "weight";

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    public WeightHistoryHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_TIME + " INTEGER, "
                + COLUMN_WEIGHT + " REAL"
                + ");";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addEntry(float weight) {
        if (weight <= 0f) return;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        long now = System.currentTimeMillis();
        values.put(COLUMN_DATE, new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date(now)));
        values.put(COLUMN_TIME, now);
        values.put(COLUMN_WEIGHT, weight);
        db.insert(TABLE_NAME, null, values);
    }

    public List<WeightEntry> getRecentEntries(int limit) {
        List<WeightEntry> entries = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                COLUMN_TIME + " DESC",
                String.valueOf(limit));
        if (cursor != null) {
            while (cursor.moveToNext()) {
                WeightEntry entry = new WeightEntry();
                entry.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME));
                entry.weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT));
                entries.add(entry);
            }
            cursor.close();
        }
        List<WeightEntry> chronological = new ArrayList<>();
        for (int i = entries.size() - 1; i >= 0; i--) {
            chronological.add(entries.get(i));
        }
        return chronological;
    }

    public WeightEntry getLatestEntry() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                COLUMN_TIME + " DESC",
                "1");
        WeightEntry entry = null;
        if (cursor != null && cursor.moveToFirst()) {
            entry = new WeightEntry();
            entry.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME));
            entry.weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT));
            cursor.close();
        }
        return entry;
    }

    public void clearHistory() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public static class WeightEntry {
        public long timestamp;
        public float weight;
    }
}

