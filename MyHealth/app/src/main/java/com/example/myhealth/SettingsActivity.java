package com.example.myhealth;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import com.example.myhealth.data.DatabaseHelper;
import com.example.myhealth.data.SharedPrefManager;
import com.example.myhealth.model.DailyLog;
import com.example.myhealth.util.PdfReportGenerator;
import com.example.myhealth.util.ReminderScheduler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private MaterialCardView cardProfileOpt, cardRemindersOpt, cardUnitOpt, cardBatteryOpt, cardWidgetOpt, cardReportOpt, cardMonthlyPdfOpt, cardClearDataOpt, cardAboutAppOpt, cardAboutDevOpt;
    private TextView tvCurrentUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSettingsInfo();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        cardProfileOpt = findViewById(R.id.cardProfileOpt);
        cardRemindersOpt = findViewById(R.id.cardRemindersOpt);
        cardUnitOpt = findViewById(R.id.cardUnitOpt);
        cardBatteryOpt = findViewById(R.id.cardBatteryOpt);
        cardWidgetOpt = findViewById(R.id.cardWidgetOpt);
        cardReportOpt = findViewById(R.id.cardReportOpt);
        cardMonthlyPdfOpt = findViewById(R.id.cardMonthlyPdfOpt);
        cardClearDataOpt = findViewById(R.id.cardClearDataOpt);
        cardAboutAppOpt = findViewById(R.id.cardAboutAppOpt);
        cardAboutDevOpt = findViewById(R.id.cardAboutDevOpt);
        tvCurrentUnit = findViewById(R.id.tvCurrentUnit);

        cardProfileOpt.setOnClickListener(v -> startActivity(new Intent(this, ProfileViewActivity.class)));

        cardRemindersOpt.setOnClickListener(v -> showRemindersDialog());

        cardUnitOpt.setOnClickListener(v -> showUnitConversionDialog());

        cardBatteryOpt.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    String packageName = getPackageName();
                    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                    if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "App is already whitelisted from smart app restrictions!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Please whitelist My Health in system battery settings.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Battery optimization is not required on this Android version.", Toast.LENGTH_SHORT).show();
            }
        });

        cardWidgetOpt.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Home Screen Widget")
                    .setMessage("To add the live health widget:\n\n1. Go to your device home screen.\n2. Long press empty space and tap 'Widgets'.\n3. Find 'My Health' and drag the live widget to your home screen.\n\nYou can also customize the widget note anytime from the app menu!")
                    .setPositiveButton("Got it", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        cardReportOpt.setOnClickListener(v -> showWeeklyReportDialog());

        cardMonthlyPdfOpt.setOnClickListener(v -> {
            try {
                Uri pdfUri = PdfReportGenerator.generateMonthlyReportPdf(this);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, pdfUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                Intent chooser = Intent.createChooser(shareIntent, "Share Monthly Health PDF Report");
                chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(chooser);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to share PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        cardClearDataOpt.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Data")
                    .setMessage("Are you sure you want to clear your profile, session, and daily health logs? This action cannot be undone.")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        SharedPrefManager.getInstance(this).clearSession();
                        DatabaseHelper db = new DatabaseHelper(this);
                        db.onUpgrade(db.getWritableDatabase(), 1, 2);

                        Toast.makeText(this, "App data cleared successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        cardAboutAppOpt.setOnClickListener(v -> {
            String versionName = "Sep 1.2.0";
            try {
                versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception ignored) {}

            String msg = "<b>Name:</b><br>"
                    + "My Health<br><br>"
                    + "<b>Version:</b><br>"
                    + versionName + "<br><br>"
                    + "<b>Why:</b><br>"
                    + "To ensure users never compromise their health through precision offline-first tracking.<br><br>"
                    + "<b>What:</b><br>"
                    + "A comprehensive health and fitness application tracking steps, hydration, nutrition, and daily wellbeing.";

            new AlertDialog.Builder(this)
                    .setTitle("About App")
                    .setMessage(Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY))
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        cardAboutDevOpt.setOnClickListener(v -> {
            String msg = "<b>Developer:</b><br>"
                    + "Syed Muhammad Sajawal Hussain<br><br>"
                    + "<b>Contact:</b><br>"
                    + "0328-0841432<br><br>"
                    + "<b>Expertise:</b><br>"
                    + "Specialized in native Java, Material Design 3, SQLite local storage, and robust cross-API Android architecture.";

            new AlertDialog.Builder(this)
                    .setTitle("About Developer")
                    .setMessage(Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY))
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void loadSettingsInfo() {
        String unit = SharedPrefManager.getInstance(this).getUnitSystem();
        if ("Imperial".equals(unit)) {
            tvCurrentUnit.setText("Imperial (in / lbs)");
        } else {
            tvCurrentUnit.setText("Metric (SI: cm / kg)");
        }
    }

    private void showUnitConversionDialog() {
        String[] units = {"Metric (SI: cm / kg)", "Imperial (in / lbs)"};
        String currentUnit = SharedPrefManager.getInstance(this).getUnitSystem();
        int checkedItem = "Imperial".equals(currentUnit) ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle("Measurement System (SI / Imperial)")
                .setSingleChoiceItems(units, checkedItem, (dialog, which) -> {
                    String selected = which == 1 ? "Imperial" : "Metric";
                    SharedPrefManager.getInstance(this).setUnitSystem(selected);
                    loadSettingsInfo();
                    Toast.makeText(this, "Measurement units updated to " + selected, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showRemindersDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reminders, null);
        TextInputEditText etTargetSleep = dialogView.findViewById(R.id.etTargetSleep);
        SwitchMaterial switchBreakfast = dialogView.findViewById(R.id.switchBreakfast);
        SwitchMaterial switchLunch = dialogView.findViewById(R.id.switchLunch);
        SwitchMaterial switchDinner = dialogView.findViewById(R.id.switchDinner);
        TextView tvBreakfastTime = dialogView.findViewById(R.id.tvBreakfastTime);
        TextView tvLunchTime = dialogView.findViewById(R.id.tvLunchTime);
        TextView tvDinnerTime = dialogView.findViewById(R.id.tvDinnerTime);

        SharedPrefManager prefs = SharedPrefManager.getInstance(this);
        etTargetSleep.setText(String.format(Locale.getDefault(), "%.1f", prefs.getTargetSleepHours()));
        switchBreakfast.setChecked(prefs.isBreakfastEnabled());
        tvBreakfastTime.setText(prefs.getBreakfastTime());
        switchLunch.setChecked(prefs.isLunchEnabled());
        tvLunchTime.setText(prefs.getLunchTime());
        switchDinner.setChecked(prefs.isDinnerEnabled());
        tvDinnerTime.setText(prefs.getDinnerTime());

        tvBreakfastTime.setOnClickListener(v -> showTimePicker(tvBreakfastTime));
        tvLunchTime.setOnClickListener(v -> showTimePicker(tvLunchTime));
        tvDinnerTime.setOnClickListener(v -> showTimePicker(tvDinnerTime));

        new AlertDialog.Builder(this)
                .setTitle("Reminders & Schedule")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String sleepStr = etTargetSleep.getText() != null ? etTargetSleep.getText().toString().trim() : "8.0";
                    float sleepHours = 8.0f;
                    try {
                        sleepHours = Float.parseFloat(sleepStr);
                    } catch (NumberFormatException ignored) {
                    }

                    prefs.setTargetSleepHours(sleepHours);
                    prefs.setBreakfastReminder(switchBreakfast.isChecked(), tvBreakfastTime.getText().toString());
                    prefs.setLunchReminder(switchLunch.isChecked(), tvLunchTime.getText().toString());
                    prefs.setDinnerReminder(switchDinner.isChecked(), tvDinnerTime.getText().toString());

                    ReminderScheduler.scheduleMealAlarms(this);

                    Toast.makeText(this, "Reminders and schedule saved!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showTimePicker(TextView timeTextView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String amPm = hourOfDay >= 12 ? "PM" : "AM";
            int hour12 = hourOfDay % 12;
            if (hour12 == 0) hour12 = 12;
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute1, amPm);
            timeTextView.setText(formattedTime);
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void showWeeklyReportDialog() {
        DatabaseHelper db = new DatabaseHelper(this);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DailyLog log = db.getTodayLog(todayDate);
        int steps = log != null ? log.getSteps() : 0;
        int water = log != null ? log.getWaterMl() : 0;
        int cals = log != null ? log.getCaloriesConsumed() : 0;

        String reportMsg = "Current Day Summary:\n\n" +
                "• Total Steps: " + steps + " steps\n" +
                "• Water Hydration: " + water + " ml\n" +
                "• Calories Consumed: " + cals + " kcal\n\n" +
                "Keep up the great work! Consistent daily tracking is key to maintaining optimal health.";

        new AlertDialog.Builder(this)
                .setTitle("Weekly Health Analytics")
                .setMessage(reportMsg)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
