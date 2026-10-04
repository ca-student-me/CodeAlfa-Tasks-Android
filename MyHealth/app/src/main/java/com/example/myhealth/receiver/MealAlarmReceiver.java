package com.example.myhealth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.myhealth.util.NotificationHelper;
import com.example.myhealth.util.ReminderScheduler;

public class MealAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String mealType = intent.getStringExtra("meal_type");
        if (mealType == null) mealType = "Meal";

        NotificationHelper.showNotification(
                context,
                "Health Reminder: " + mealType,
                "Time for your " + mealType.toLowerCase() + "! Don't compromise your health, maintain healthy nutrition 🥗.",
                mealType.hashCode(),
                true
        );

        // Reschedule for next day
        ReminderScheduler.scheduleMealAlarms(context);
    }
}
