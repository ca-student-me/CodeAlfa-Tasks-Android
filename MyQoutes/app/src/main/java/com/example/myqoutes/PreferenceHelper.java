package com.example.myqoutes;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {

    private static final String PREF_NAME = "my_quote_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_NOTIFICATION_HOUR = "notification_hour";
    private static final String KEY_NOTIFICATION_MINUTE = "notification_minute";
    private static final String KEY_LAST_VIEWED_QUOTE_ID = "last_viewed_quote_id";

    private final SharedPreferences prefs;

    public PreferenceHelper(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public int getNotificationHour() {
        return prefs.getInt(KEY_NOTIFICATION_HOUR, 8);
    }

    public int getNotificationMinute() {
        return prefs.getInt(KEY_NOTIFICATION_MINUTE, 0);
    }

    public void setNotificationTime(int hour, int minute) {
        prefs.edit()
                .putInt(KEY_NOTIFICATION_HOUR, hour)
                .putInt(KEY_NOTIFICATION_MINUTE, minute)
                .apply();
    }

    public int getLastViewedQuoteId() {
        return prefs.getInt(KEY_LAST_VIEWED_QUOTE_ID, -1);
    }

    public void setLastViewedQuoteId(int quoteId) {
        prefs.edit().putInt(KEY_LAST_VIEWED_QUOTE_ID, quoteId).apply();
    }
}
