package com.example.myhealth.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myhealth.model.DailyLog;
import com.example.myhealth.model.UserProfile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "my_health.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_USER_PROFILE = "user_profile";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CONTACT = "contact";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_CURRENT_WEIGHT = "current_weight";
    public static final String COLUMN_TARGET_WEIGHT = "target_weight";
    public static final String COLUMN_ACTIVITY_LEVEL = "activity_level";
    public static final String COLUMN_GOAL = "goal";
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String TABLE_DAILY_LOGS = "daily_logs";
    public static final String COL_LOG_ID = "id";
    public static final String COL_LOG_DATE = "date";
    public static final String COL_LOG_WATER = "water_ml";
    public static final String COL_LOG_STEPS = "steps";
    public static final String COL_LOG_CALORIES = "calories_consumed";
    public static final String COL_LOG_BURNED = "calories_burned";

    public static final String TABLE_MEDICAL_REPORTS = "medical_reports";
    public static final String COL_MED_ID = "id";
    public static final String COL_MED_DATE = "date";
    public static final String COL_MED_BP = "blood_pressure";
    public static final String COL_MED_SUGAR = "blood_sugar";
    public static final String COL_MED_PUSHUPS = "pushups";
    public static final String COL_MED_PULLUPS = "pullups";
    public static final String COL_MED_RUNNING = "running_km";

    private static final String CREATE_TABLE_USER_PROFILE = "CREATE TABLE " + TABLE_USER_PROFILE + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT NOT NULL,"
            + COLUMN_CONTACT + " TEXT,"
            + COLUMN_EMAIL + " TEXT,"
            + COLUMN_COUNTRY + " TEXT,"
            + COLUMN_GENDER + " TEXT,"
            + COLUMN_AGE + " INTEGER,"
            + COLUMN_HEIGHT + " REAL,"
            + COLUMN_CURRENT_WEIGHT + " REAL,"
            + COLUMN_TARGET_WEIGHT + " REAL,"
            + COLUMN_ACTIVITY_LEVEL + " TEXT,"
            + COLUMN_GOAL + " TEXT,"
            + COLUMN_CREATED_AT + " TEXT"
            + ")";

    private static final String CREATE_TABLE_DAILY_LOGS = "CREATE TABLE " + TABLE_DAILY_LOGS + "("
            + COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_LOG_DATE + " TEXT UNIQUE,"
            + COL_LOG_WATER + " INTEGER DEFAULT 0,"
            + COL_LOG_STEPS + " INTEGER DEFAULT 0,"
            + COL_LOG_CALORIES + " INTEGER DEFAULT 0,"
            + COL_LOG_BURNED + " INTEGER DEFAULT 0"
            + ")";

    private static final String CREATE_TABLE_MEDICAL_REPORTS = "CREATE TABLE " + TABLE_MEDICAL_REPORTS + "("
            + COL_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_MED_DATE + " TEXT UNIQUE,"
            + COL_MED_BP + " TEXT,"
            + COL_MED_SUGAR + " TEXT,"
            + COL_MED_PUSHUPS + " INTEGER DEFAULT 0,"
            + COL_MED_PULLUPS + " INTEGER DEFAULT 0,"
            + COL_MED_RUNNING + " REAL DEFAULT 0.0"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER_PROFILE);
        db.execSQL(CREATE_TABLE_DAILY_LOGS);
        db.execSQL(CREATE_TABLE_MEDICAL_REPORTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICAL_REPORTS);
        onCreate(db);
    }

    public long insertUserProfile(String name, String contact, String email, String country,
                                    String gender, int age, float height, float currentWeight,
                                    float targetWeight, String activityLevel, String goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CONTACT, contact);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_COUNTRY, country);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_CURRENT_WEIGHT, currentWeight);
        values.put(COLUMN_TARGET_WEIGHT, targetWeight);
        values.put(COLUMN_ACTIVITY_LEVEL, activityLevel);
        values.put(COLUMN_GOAL, goal);

        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(COLUMN_CREATED_AT, currentDate);

        db.delete(TABLE_USER_PROFILE, null, null);

        long id = db.insert(TABLE_USER_PROFILE, null, values);
        db.close();
        return id;
    }

    public UserProfile getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile profile = null;

        Cursor cursor = db.query(TABLE_USER_PROFILE, null, null, null, null, null, null, "1");
        if (cursor != null && cursor.moveToFirst()) {
            profile = new UserProfile();
            profile.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            profile.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            profile.setContact(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT)));
            profile.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            profile.setCountry(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY)));
            profile.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)));
            profile.setAge(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)));
            profile.setHeight(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_HEIGHT)));
            profile.setCurrentWeight(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_WEIGHT)));
            profile.setTargetWeight(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TARGET_WEIGHT)));
            profile.setActivityLevel(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY_LEVEL)));
            profile.setGoal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL)));
            profile.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
            cursor.close();
        }
        db.close();
        return profile;
    }

    public boolean updateUserProfile(long id, String name, String contact, String email, String country,
                                     String gender, int age, float height, float currentWeight,
                                     float targetWeight, String activityLevel, String goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CONTACT, contact);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_COUNTRY, country);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_CURRENT_WEIGHT, currentWeight);
        values.put(COLUMN_TARGET_WEIGHT, targetWeight);
        values.put(COLUMN_ACTIVITY_LEVEL, activityLevel);
        values.put(COLUMN_GOAL, goal);

        int rowsAffected = db.update(TABLE_USER_PROFILE, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    public DailyLog getTodayLog(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        DailyLog log = null;
        Cursor cursor = db.query(TABLE_DAILY_LOGS, null, COL_LOG_DATE + " = ?", new String[]{date}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            log = new DailyLog();
            log.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOG_ID)));
            log.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_DATE)));
            log.setWaterMl(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_WATER)));
            log.setSteps(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_STEPS)));
            log.setCaloriesConsumed(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_CALORIES)));
            log.setCaloriesBurned(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_BURNED)));
            cursor.close();
        } else {
            log = new DailyLog(-1, date, 0, 0, 0, 0);
        }
        db.close();
        return log;
    }

    public void addWater(String date, int amountMl) {
        SQLiteDatabase db = this.getWritableDatabase();
        DailyLog existing = getTodayLogInternal(db, date);
        if (existing.getId() == -1) {
            ContentValues values = new ContentValues();
            values.put(COL_LOG_DATE, date);
            values.put(COL_LOG_WATER, amountMl);
            values.put(COL_LOG_STEPS, 0);
            values.put(COL_LOG_CALORIES, 0);
            values.put(COL_LOG_BURNED, 0);
            db.insert(TABLE_DAILY_LOGS, null, values);
        } else {
            ContentValues values = new ContentValues();
            values.put(COL_LOG_WATER, existing.getWaterMl() + amountMl);
            db.update(TABLE_DAILY_LOGS, values, COL_LOG_DATE + " = ?", new String[]{date});
        }
        db.close();
    }

    public void addSteps(String date, int count) {
        SQLiteDatabase db = this.getWritableDatabase();
        DailyLog existing = getTodayLogInternal(db, date);
        if (existing.getId() == -1) {
            ContentValues values = new ContentValues();
            values.put(COL_LOG_DATE, date);
            values.put(COL_LOG_WATER, 0);
            values.put(COL_LOG_STEPS, count);
            values.put(COL_LOG_CALORIES, 0);
            values.put(COL_LOG_BURNED, 0);
            db.insert(TABLE_DAILY_LOGS, null, values);
        } else {
            ContentValues values = new ContentValues();
            values.put(COL_LOG_STEPS, existing.getSteps() + count);
            db.update(TABLE_DAILY_LOGS, values, COL_LOG_DATE + " = ?", new String[]{date});
        }
        db.close();
    }

    public void addCalories(String date, int count) {
        SQLiteDatabase db = this.getWritableDatabase();
        DailyLog existing = getTodayLogInternal(db, date);
        if (existing.getId() == -1) {
            ContentValues values = new ContentValues();
            values.put(COL_LOG_DATE, date);
            values.put(COL_LOG_WATER, 0);
            values.put(COL_LOG_STEPS, 0);
            values.put(COL_LOG_CALORIES, count);
            values.put(COL_LOG_BURNED, 0);
            db.insert(TABLE_DAILY_LOGS, null, values);
        } else {
            ContentValues values = new ContentValues();
            values.put(COL_LOG_CALORIES, existing.getCaloriesConsumed() + count);
            db.update(TABLE_DAILY_LOGS, values, COL_LOG_DATE + " = ?", new String[]{date});
        }
        db.close();
    }

    public void addCaloriesBurned(String date, int count) {
        SQLiteDatabase db = this.getWritableDatabase();
        DailyLog existing = getTodayLogInternal(db, date);
        if (existing.getId() == -1) {
            ContentValues values = new ContentValues();
            values.put(COL_LOG_DATE, date);
            values.put(COL_LOG_WATER, 0);
            values.put(COL_LOG_STEPS, 0);
            values.put(COL_LOG_CALORIES, 0);
            values.put(COL_LOG_BURNED, count);
            db.insert(TABLE_DAILY_LOGS, null, values);
        } else {
            ContentValues values = new ContentValues();
            values.put(COL_LOG_BURNED, existing.getCaloriesBurned() + count);
            db.update(TABLE_DAILY_LOGS, values, COL_LOG_DATE + " = ?", new String[]{date});
        }
        db.close();
    }

    private DailyLog getTodayLogInternal(SQLiteDatabase db, String date) {
        DailyLog log = null;
        Cursor cursor = db.query(TABLE_DAILY_LOGS, null, COL_LOG_DATE + " = ?", new String[]{date}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            log = new DailyLog();
            log.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOG_ID)));
            log.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_DATE)));
            log.setWaterMl(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_WATER)));
            log.setSteps(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_STEPS)));
            log.setCaloriesConsumed(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_CALORIES)));
            log.setCaloriesBurned(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_BURNED)));
            cursor.close();
        } else {
            log = new DailyLog(-1, date, 0, 0, 0, 0);
        }
        return log;
    }

    public void insertOrUpdateMedicalReport(String date, String bp, String sugar, int pushups, int pullups, float runningKm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MED_DATE, date);
        values.put(COL_MED_BP, bp);
        values.put(COL_MED_SUGAR, sugar);
        values.put(COL_MED_PUSHUPS, pushups);
        values.put(COL_MED_PULLUPS, pullups);
        values.put(COL_MED_RUNNING, runningKm);

        int rows = db.update(TABLE_MEDICAL_REPORTS, values, COL_MED_DATE + " = ?", new String[]{date});
        if (rows == 0) {
            db.insert(TABLE_MEDICAL_REPORTS, null, values);
        }
        db.close();
    }

    public Cursor getTodayMedicalReport(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MEDICAL_REPORTS, null, COL_MED_DATE + " = ?", new String[]{date}, null, null, null);
    }
}
