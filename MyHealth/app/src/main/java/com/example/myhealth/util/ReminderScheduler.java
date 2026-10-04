package com.example.myhealth.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.myhealth.data.SharedPrefManager;
import com.example.myhealth.receiver.MealAlarmReceiver;

import java.util.Calendar;

public class ReminderScheduler {

    public static void scheduleMealAlarms(Context context) {
        SharedPrefManager prefs = SharedPrefManager.getInstance(context);

        if (prefs.isBreakfastEnabled()) {
            setAlarm(context, prefs.getBreakfastTime(), "Breakfast", 1001);
        }
        if (prefs.isLunchEnabled()) {
            setAlarm(context, prefs.getLunchTime(), "Lunch", 1002);
        }
        if (prefs.isDinnerEnabled()) {
            setAlarm(context, prefs.getDinnerTime(), "Dinner", 1003);
        }
    }

    private static void setAlarm(Context context, String timeStr, String mealType, int requestCode) {
        try {
            String[] parts = timeStr.split(" ");
            String[] hm = parts[0].split(":");
            int hour = Integer.parseInt(hm[0]);
            int minute = Integer.parseInt(hm[1]);
            if (parts[1].equalsIgnoreCase("PM") && hour < 12) {
                hour += 12;
            } else if (parts[1].equalsIgnoreCase("AM") && hour == 12) {
                hour = 0;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(context, MealAlarmReceiver.class);
            intent.putExtra("meal_type", mealType);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
            }
        } catch (Exception ignored) {
        }
    }
}
