package com.example.myqoutes;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private PreferenceHelper preferenceHelper;
    private DatabaseHelper databaseHelper;

    private View settingsRoot;
    private SwitchMaterial switchNotifications;
    private TextView tvReminderTime;
    private TextView tvTotalQuotes;
    private TextView tvPresetQuotes;
    private TextView tvUserQuotes;
    private TextView tvAppVersion;
    private TextView tvExactAlarmStatus;
    private View rowExactAlarm;
    private MaterialButton btnTestNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_settings);

        preferenceHelper = new PreferenceHelper(this);
        databaseHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupEdgeToEdge();
        setupToolbar();
        loadSettingsAndStats();
        loadAppInfo();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateExactAlarmStatus();
    }

    private void initViews() {
        settingsRoot = findViewById(R.id.settings_root);
        switchNotifications = findViewById(R.id.switch_notifications);
        tvReminderTime = findViewById(R.id.tv_reminder_time);
        tvTotalQuotes = findViewById(R.id.tv_total_quotes);
        tvPresetQuotes = findViewById(R.id.tv_preset_quotes);
        tvUserQuotes = findViewById(R.id.tv_user_quotes);
        tvAppVersion = findViewById(R.id.tv_app_version);
        tvExactAlarmStatus = findViewById(R.id.tv_exact_alarm_status);
        rowExactAlarm = findViewById(R.id.row_exact_alarm);
        btnTestNotification = findViewById(R.id.btn_test_notification);
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(settingsRoot, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSettingsAndStats() {
        boolean enabled = preferenceHelper.isNotificationsEnabled();
        switchNotifications.setChecked(enabled);

        updateTimeDisplay(preferenceHelper.getNotificationHour(), preferenceHelper.getNotificationMinute());

        int total = databaseHelper.getTotalQuotesCount();
        int preset = databaseHelper.getPresetQuotesCount();
        int user = databaseHelper.getUserQuotesCount();

        tvTotalQuotes.setText(String.valueOf(total));
        tvPresetQuotes.setText(String.valueOf(preset));
        tvUserQuotes.setText(String.valueOf(user));

        updateExactAlarmStatus();
    }

    private void updateExactAlarmStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (rowExactAlarm != null) rowExactAlarm.setVisibility(View.VISIBLE);
            boolean hasPermission = NotificationHelper.hasExactAlarmPermission(this);
            if (tvExactAlarmStatus != null) {
                if (hasPermission) {
                    tvExactAlarmStatus.setText(R.string.notification_status_granted);
                    tvExactAlarmStatus.setTextColor(ContextCompat.getColor(this, R.color.secondary));
                } else {
                    tvExactAlarmStatus.setText(R.string.notification_status_action_needed);
                    tvExactAlarmStatus.setTextColor(ContextCompat.getColor(this, R.color.badge_text));
                }
            }
        } else {
            if (rowExactAlarm != null) rowExactAlarm.setVisibility(View.GONE);
        }
    }

    private void loadAppInfo() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            tvAppVersion.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting package version info", e);
            tvAppVersion.setText("1.0.0");
        }
    }

    private void setupListeners() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceHelper.setNotificationsEnabled(isChecked);
            if (isChecked) {
                NotificationHelper.scheduleDailyNotification(
                        this,
                        preferenceHelper.getNotificationHour(),
                        preferenceHelper.getNotificationMinute()
                );
            } else {
                NotificationHelper.cancelDailyNotification(this);
            }
        });

        View rowTimePicker = findViewById(R.id.row_time_picker);
        if (rowTimePicker != null) {
            rowTimePicker.setOnClickListener(v -> showTimePicker());
        }

        if (rowExactAlarm != null) {
            rowExactAlarm.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    } catch (Exception e) {
                        openAppNotificationSettings();
                    }
                }
            });
        }

        if (btnTestNotification != null) {
            btnTestNotification.setOnClickListener(v -> {
                boolean sent = NotificationHelper.showTestNotification(this);
                if (sent) {
                    Toast.makeText(this, R.string.test_notification_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.test_notification_failed, Toast.LENGTH_LONG).show();
                    openAppNotificationSettings();
                }
            });
        }

        View rowContact = findViewById(R.id.row_contact);
        if (rowContact != null) {
            rowContact.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:03280841432"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "0328-0841432", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View rowEmail = findViewById(R.id.row_email);
        if (rowEmail != null) {
            rowEmail.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:syedmuhammadsajawalhussain@gmail.com"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "My Quote App Feedback");
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "syedmuhammadsajawalhussain@gmail.com", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openAppNotificationSettings() {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            } else {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
            }
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening app settings", e);
        }
    }

    private void showTimePicker() {
        int currentHour = preferenceHelper.getNotificationHour();
        int currentMinute = preferenceHelper.getNotificationMinute();

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setTitleText(R.string.select_time)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();

            preferenceHelper.setNotificationTime(hour, minute);
            updateTimeDisplay(hour, minute);

            if (preferenceHelper.isNotificationsEnabled()) {
                NotificationHelper.scheduleDailyNotification(this, hour, minute);
            }
        });

        picker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");
    }

    private void updateTimeDisplay(int hour, int minute) {
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        String amPm = (hour >= 12) ? "PM" : "AM";

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
        tvReminderTime.setText(timeFormatted);
    }
}
