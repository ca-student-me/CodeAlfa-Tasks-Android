package com.example.myqoutes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "AlarmReceiver triggered");

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;

        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "MyQuote:AlarmReceiverWakeLock"
            );
            wakeLock.acquire(5000);
        }

        try {
            PreferenceHelper preferenceHelper = new PreferenceHelper(context);
            if (!preferenceHelper.isNotificationsEnabled()) {
                Log.d(TAG, "Notifications are disabled in preferences. Skipping notification.");
                return;
            }

            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
            Quote quote = dbHelper.getRandomQuote(-1);

            if (quote != null) {
                NotificationHelper.showNotification(context, quote);
            } else {
                Log.w(TAG, "No quote found in database to display in notification.");
            }

            // Reschedule alarm for the next day
            NotificationHelper.scheduleDailyNotification(
                    context,
                    preferenceHelper.getNotificationHour(),
                    preferenceHelper.getNotificationMinute()
            );
        } finally {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }
}
