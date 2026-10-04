package com.example.myhealth.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.myhealth.MainActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID_MEALS = "MealRemindersChannel";
    private static final String CHANNEL_ID_WATER = "WaterRemindersChannel";

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel mealChannel = new NotificationChannel(
                        CHANNEL_ID_MEALS,
                        "Meal Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                );
                mealChannel.setDescription("Reminders for Breakfast, Lunch, and Dinner");

                NotificationChannel waterChannel = new NotificationChannel(
                        CHANNEL_ID_WATER,
                        "Water & Hydration Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                );
                waterChannel.setDescription("Reminders to drink water after every 1000 steps");

                manager.createNotificationChannel(mealChannel);
                manager.createNotificationChannel(waterChannel);
            }
        }
    }

    public static void showNotification(Context context, String title, String message, int notificationId, boolean isMeal) {
        createChannels(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = isMeal ? CHANNEL_ID_MEALS : CHANNEL_ID_WATER;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }
}
