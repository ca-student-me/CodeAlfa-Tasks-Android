package com.example.myhealth.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "MyHealthPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_AVATAR_URI = "avatar_uri";
    private static final String KEY_UNIT_SYSTEM = "unit_system";
    private static final String KEY_WIDGET_NOTE = "widget_note";

    private static final String KEY_TARGET_SLEEP = "target_sleep";
    private static final String KEY_BREAKFAST_ENABLED = "breakfast_enabled";
    private static final String KEY_BREAKFAST_TIME = "breakfast_time";
    private static final String KEY_LUNCH_ENABLED = "lunch_enabled";
    private static final String KEY_LUNCH_TIME = "lunch_time";
    private static final String KEY_DINNER_ENABLED = "dinner_enabled";
    private static final String KEY_DINNER_TIME = "dinner_time";
    private static final String KEY_LAST_MONTHLY_REPORT = "last_monthly_report_time";

    private static SharedPrefManager instance;
    public SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveUserSession(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isProfileCompleted() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setProfileCompleted(boolean completed) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, completed).apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "User");
    }

    public void setUserName(String name) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply();
    }

    public void setUserEmail(String email) {
        sharedPreferences.edit().putString("user_email", email).apply();
    }

    public void setAvatarUri(String uri) {
        sharedPreferences.edit().putString(KEY_AVATAR_URI, uri).apply();
    }

    public String getAvatarUri() {
        return sharedPreferences.getString(KEY_AVATAR_URI, null);
    }

    public void setUnitSystem(String system) {
        sharedPreferences.edit().putString(KEY_UNIT_SYSTEM, system).apply();
    }

    public String getUnitSystem() {
        return sharedPreferences.getString(KEY_UNIT_SYSTEM, "Metric");
    }

    public void setWidgetNote(String note) {
        sharedPreferences.edit().putString(KEY_WIDGET_NOTE, note).apply();
    }

    public String getWidgetNote() {
        return sharedPreferences.getString(KEY_WIDGET_NOTE, "Stay active & hydrated!");
    }

    public String getWidgetText() {
        return sharedPreferences.getString(KEY_WIDGET_NOTE, "Stay active & hydrated!");
    }

    public float getTargetSleepHours() {
        return sharedPreferences.getFloat(KEY_TARGET_SLEEP, 8.0f);
    }

    public void setTargetSleepHours(float hours) {
        sharedPreferences.edit().putFloat(KEY_TARGET_SLEEP, hours).apply();
    }

    public boolean isBreakfastEnabled() {
        return sharedPreferences.getBoolean(KEY_BREAKFAST_ENABLED, true);
    }

    public String getBreakfastTime() {
        return sharedPreferences.getString(KEY_BREAKFAST_TIME, "08:00 AM");
    }

    public void setBreakfastReminder(boolean enabled, String time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_BREAKFAST_ENABLED, enabled);
        editor.putString(KEY_BREAKFAST_TIME, time);
        editor.apply();
    }

    public boolean isLunchEnabled() {
        return sharedPreferences.getBoolean(KEY_LUNCH_ENABLED, true);
    }

    public String getLunchTime() {
        return sharedPreferences.getString(KEY_LUNCH_TIME, "01:00 PM");
    }

    public void setLunchReminder(boolean enabled, String time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LUNCH_ENABLED, enabled);
        editor.putString(KEY_LUNCH_TIME, time);
        editor.apply();
    }

    public boolean isDinnerEnabled() {
        return sharedPreferences.getBoolean(KEY_DINNER_ENABLED, true);
    }

    public String getDinnerTime() {
        return sharedPreferences.getString(KEY_DINNER_TIME, "08:00 PM");
    }

    public void setDinnerReminder(boolean enabled, String time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DINNER_ENABLED, enabled);
        editor.putString(KEY_DINNER_TIME, time);
        editor.apply();
    }

    public long getLastMonthlyReportTime() {
        return sharedPreferences.getLong(KEY_LAST_MONTHLY_REPORT, 0);
    }

    public void setLastMonthlyReportTime(long time) {
        sharedPreferences.edit().putLong(KEY_LAST_MONTHLY_REPORT, time).apply();
    }

    public void clearSession() {
        sharedPreferences.edit().clear().apply();
    }
}
