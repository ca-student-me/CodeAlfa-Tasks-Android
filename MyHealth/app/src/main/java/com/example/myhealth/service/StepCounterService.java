package com.example.myhealth.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.myhealth.MainActivity;
import com.example.myhealth.data.DatabaseHelper;
import com.example.myhealth.model.DailyLog;
import com.example.myhealth.util.NotificationHelper;
import com.example.myhealth.widget.HealthWidgetProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "StepCounterChannel";
    private static final int NOTIFICATION_ID = 101;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private int previousStepCount = -1;
    private int lastWaterPromptSteps = 0;
    private DatabaseHelper databaseHelper;

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            Intent widgetIntent = new Intent(StepCounterService.this, HealthWidgetProvider.class);
            widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(StepCounterService.this).getAppWidgetIds(
                    new ComponentName(StepCounterService.this, HealthWidgetProvider.class));
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(widgetIntent);

            clockHandler.postDelayed(this, 1000L);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepCounterSensor != null) {
                sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
        createNotificationChannel();

        Notification notification = buildNotification(getCurrentTodaySteps());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        clockHandler.post(clockRunnable);
    }

    private int getCurrentTodaySteps() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DailyLog log = databaseHelper.getTodayLog(todayDate);
        return log != null ? log.getSteps() : 0;
    }

    private Notification buildNotification(int steps) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My Health - Step Counter")
                .setContentText("Today's Steps: " + steps)
                .setSmallIcon(android.R.drawable.ic_menu_directions)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Counter Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (sensorManager != null && stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }

        Notification notification = buildNotification(getCurrentTodaySteps());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalStepsSinceBoot = (int) event.values[0];
            
            if (previousStepCount == -1) {
                previousStepCount = totalStepsSinceBoot;
                return;
            }

            int stepsDelta = totalStepsSinceBoot - previousStepCount;
            if (stepsDelta > 0) {
                // Balanced normalization ratio (factor 0.8) for natural, accurate step counts
                int refinedDelta = Math.max(1, Math.round(stepsDelta * 0.8f));
                registerSteps(refinedDelta);
                previousStepCount = totalStepsSinceBoot;
            } else if (stepsDelta < 0) {
                previousStepCount = totalStepsSinceBoot;
            }
        }
    }

    private void registerSteps(int delta) {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        databaseHelper.addSteps(todayDate, delta);

        DailyLog log = databaseHelper.getTodayLog(todayDate);
        int totalTodaySteps = log != null ? log.getSteps() : 0;

        if (totalTodaySteps >= lastWaterPromptSteps + 1000) {
            lastWaterPromptSteps = totalTodaySteps;
            NotificationHelper.showNotification(
                    this,
                    "Hydration Reminder 💧",
                    "Amazing! You've walked " + totalTodaySteps + " steps today. Time to drink 250ml of water to stay hydrated!",
                    2001,
                    false
            );
        }

        Intent stepUpdateIntent = new Intent("com.example.myhealth.STEP_UPDATE");
        sendBroadcast(stepUpdateIntent);

        Intent widgetIntent = new Intent(this, HealthWidgetProvider.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, HealthWidgetProvider.class));
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(widgetIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(totalTodaySteps));
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmService != null) {
            alarmService.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 1000,
                    restartServicePendingIntent
            );
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        clockHandler.removeCallbacks(clockRunnable);
    }
}
