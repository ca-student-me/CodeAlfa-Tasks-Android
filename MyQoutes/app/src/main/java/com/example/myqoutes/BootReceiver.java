package com.example.myqoutes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        Log.d(TAG, "BootReceiver triggered with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
                "com.htc.intent.action.QUICKBOOT_POWERON".equals(action)) {

            PreferenceHelper preferenceHelper = new PreferenceHelper(context);
            if (preferenceHelper.isNotificationsEnabled()) {
                NotificationHelper.scheduleDailyNotification(
                        context,
                        preferenceHelper.getNotificationHour(),
                        preferenceHelper.getNotificationMinute()
                );
                Log.d(TAG, "Rescheduled daily notification after reboot/upgrade.");
            }
        }
    }
}
