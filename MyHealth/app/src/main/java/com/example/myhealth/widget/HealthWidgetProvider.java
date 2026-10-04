package com.example.myhealth.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.myhealth.MainActivity;
import com.example.myhealth.R;
import com.example.myhealth.data.DatabaseHelper;
import com.example.myhealth.data.SharedPrefManager;
import com.example.myhealth.model.DailyLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HealthWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_health);

        try {
            // Time (12-hour format with AM/PM)
            String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
            
            // Day - DD Month YYYY format (e.g. Monday - 25 Oct 2004)
            String currentDateDay = new SimpleDateFormat("EEEE - dd MMM yyyy", Locale.getDefault()).format(new Date());

            views.setTextViewText(R.id.widgetTvTime, currentTime);
            views.setTextViewText(R.id.widgetTvDateDay, currentDateDay);

            // Custom Note Text
            String customText = SharedPrefManager.getInstance(context).getWidgetText();
            views.setTextViewText(R.id.widgetTvCustomText, customText);

            // Health Data from DB safely
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            DailyLog log = dbHelper.getTodayLog(todayDate);
            int steps = log != null ? log.getSteps() : 0;
            int water = log != null ? log.getWaterMl() : 0;
            int calories = log != null ? log.getCaloriesConsumed() : 0;

            views.setTextViewText(R.id.widgetTvSteps, String.valueOf(steps));
            views.setTextViewText(R.id.widgetTvWater, water + " ml");
            views.setTextViewText(R.id.widgetTvCalories, calories + " kcal");

            // Click intent on root layout to open MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widgetRootLayout, pendingIntent);

        } catch (Exception e) {
            views.setTextViewText(R.id.widgetTvSteps, "0");
            views.setTextViewText(R.id.widgetTvWater, "0 ml");
            views.setTextViewText(R.id.widgetTvCalories, "0 kcal");
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
