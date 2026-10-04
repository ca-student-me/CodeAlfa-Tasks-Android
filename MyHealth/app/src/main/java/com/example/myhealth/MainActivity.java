package com.example.myhealth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import com.example.myhealth.data.DatabaseHelper;
import com.example.myhealth.data.SharedPrefManager;
import com.example.myhealth.model.DailyLog;
import com.example.myhealth.model.UserProfile;
import com.example.myhealth.service.StepCounterService;
import com.example.myhealth.util.PdfReportGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserName, tvBmiValue, tvBmiCategory, tvMetricsDetails;
    private TextView tvSummarySteps, tvSummaryWater, tvSummaryCalories, tvSummaryBurned;
    private TextView tvSummaryBp, tvSummarySugar, tvSummaryPushups, tvSummaryPullups, tvSummaryRunning;
    private ImageView ivAvatar;
    private DatabaseHelper databaseHelper;
    private String todayDate;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    private final BroadcastReceiver stepUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadUserData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handle edge-to-edge window insets for gesture & 3-button navigation bars across resolutions
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        databaseHelper = new DatabaseHelper(this);
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        setupImagePicker();
        initViews();
        loadUserData();
        checkAndRequestPermissions();
        startStepCounterService();
        checkMonthlyReportAutomation();
    }

    private void checkMonthlyReportAutomation() {
        SharedPrefManager prefs = SharedPrefManager.getInstance(this);
        long lastReportTime = prefs.getLastMonthlyReportTime();
        long currentTime = System.currentTimeMillis();
        long twentyEightDays = 28L * 24L * 60L * 60L * 1000L;

        if (lastReportTime == 0) {
            prefs.setLastMonthlyReportTime(currentTime);
        } else if (currentTime - lastReportTime >= twentyEightDays) {
            prefs.setLastMonthlyReportTime(currentTime);
            try {
                Uri pdfUri = PdfReportGenerator.generateMonthlyReportPdf(this);
                new AlertDialog.Builder(this)
                        .setTitle("Monthly PDF Report Ready 📄")
                        .setMessage("Congratulations on completing 4 weeks of health tracking! Your automated monthly health progress report PDF has been generated.")
                        .setPositiveButton("Share Report", (dialog, which) -> {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("application/pdf");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(shareIntent, "Share Monthly Health PDF Report"));
                        })
                        .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            } catch (Exception ignored) {
            }
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(selectedImageUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                            }
                            SharedPrefManager.getInstance(this).setAvatarUri(selectedImageUri.toString());
                            ivAvatar.setImageURI(selectedImageUri);
                            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchImagePicker();
                    } else {
                        Toast.makeText(this, "Storage permission is required to select a profile picture.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 100);
            }
        }
    }

    private void startStepCounterService() {
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        tvUserName = findViewById(R.id.tvUserName);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiCategory = findViewById(R.id.tvBmiCategory);
        tvMetricsDetails = findViewById(R.id.tvMetricsDetails);

        tvSummarySteps = findViewById(R.id.tvSummarySteps);
        tvSummaryWater = findViewById(R.id.tvSummaryWater);
        tvSummaryCalories = findViewById(R.id.tvSummaryCalories);
        tvSummaryBurned = findViewById(R.id.tvSummaryBurned);

        tvSummaryBp = findViewById(R.id.tvSummaryBp);
        tvSummarySugar = findViewById(R.id.tvSummarySugar);
        tvSummaryPushups = findViewById(R.id.tvSummaryPushups);
        tvSummaryPullups = findViewById(R.id.tvSummaryPullups);
        tvSummaryRunning = findViewById(R.id.tvSummaryRunning);

        ivAvatar = findViewById(R.id.ivAvatar);
        ivAvatar.setClickable(true);
        ivAvatar.setFocusable(true);
        ivAvatar.setOnClickListener(v -> onAvatarClicked());

        MaterialCardView cardBmi = findViewById(R.id.cardBmi);
        cardBmi.setOnClickListener(v -> showBmiEducationDialog());

        MaterialButton btnLogWater = findViewById(R.id.btnLogWater);
        MaterialButton btnLogSteps = findViewById(R.id.btnLogSteps);
        MaterialButton btnLogFood = findViewById(R.id.btnLogFood);
        MaterialButton btnLogMeal = findViewById(R.id.btnLogMeal);
        MaterialButton btnLogGym = findViewById(R.id.btnLogGym);
        MaterialButton btnLogMedical = findViewById(R.id.btnLogMedical);

        btnLogWater.setOnClickListener(v -> showLogWaterDialog());
        btnLogSteps.setOnClickListener(v -> showLogStepsDialog());
        btnLogFood.setOnClickListener(v -> showLogFoodDialog());
        btnLogMeal.setOnClickListener(v -> showLogMealDialog());
        btnLogGym.setOnClickListener(v -> showLogGymWorkoutDialog());
        btnLogMedical.setOnClickListener(v -> showLogMedicalDialog());
    }

    private void showBmiEducationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Body Mass Index (BMI) Info")
                .setMessage("What is BMI?\n"
                        + "BMI is a measure of body fat based on height and weight that applies to adult men and women.\n\n"
                        + "Optimal / Best BMI Range:\n"
                        + "• Normal Weight: 18.5 – 24.9 (Best range for health and longevity).\n\n"
                        + "Other Categories:\n"
                        + "• Underweight: < 18.5\n"
                        + "• Overweight: 25.0 – 29.9\n"
                        + "• Obese: >= 30.0")
                .setPositiveButton("Got it", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogMealDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_log_meal, null);
        AutoCompleteTextView actvMealType = dialogView.findViewById(R.id.actvMealType);
        AutoCompleteTextView actvAsianFood = dialogView.findViewById(R.id.actvAsianFood);
        EditText etCustomCalories = dialogView.findViewById(R.id.etCustomCalories);
        etCustomCalories.setText("300");

        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                new String[]{"Breakfast (Morning)", "Lunch", "Dinner"});
        actvMealType.setAdapter(mealAdapter);
        actvMealType.setText("Breakfast (Morning)", false);
        actvMealType.setOnClickListener(v -> actvMealType.showDropDown());
        actvMealType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actvMealType.showDropDown();
        });

        String[] asianMenu = {
                "Boiled Egg (~75 kcal)",
                "Paratha (~300 kcal)",
                "Roti / Chapati (~100 kcal)",
                "Plain Rice (1 bowl) (~200 kcal)",
                "Chicken Biryani (1 plate) (~500 kcal)",
                "Daal / Lentils (1 bowl) (~150 kcal)",
                "Chai with Milk & Sugar (~120 kcal)",
                "Apple / Banana (~95 kcal)"
        };
        ArrayAdapter<String> foodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, asianMenu);
        actvAsianFood.setAdapter(foodAdapter);
        actvAsianFood.setText(asianMenu[1], false);
        actvAsianFood.setOnClickListener(v -> actvAsianFood.showDropDown());
        actvAsianFood.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actvAsianFood.showDropDown();
        });

        actvAsianFood.setOnItemClickListener((parent, view, position, id) -> {
            String selected = asianMenu[position];
            int cals = 300;
            if (selected.contains("Boiled Egg")) cals = 75;
            else if (selected.contains("Paratha")) cals = 300;
            else if (selected.contains("Roti")) cals = 100;
            else if (selected.contains("Rice")) cals = 200;
            else if (selected.contains("Biryani")) cals = 500;
            else if (selected.contains("Daal")) cals = 150;
            else if (selected.contains("Chai")) cals = 120;
            else if (selected.contains("Apple")) cals = 95;
            etCustomCalories.setText(String.valueOf(cals));
        });

        new AlertDialog.Builder(this)
                .setTitle("Log Daily Meal")
                .setView(dialogView)
                .setPositiveButton("Log Meal", (dialog, which) -> {
                    String calStr = etCustomCalories.getText() != null && !etCustomCalories.getText().toString().trim().isEmpty() ?
                            etCustomCalories.getText().toString().trim() : "300";
                    try {
                        int cals = Integer.parseInt(calStr);
                        if (cals > 0) {
                            databaseHelper.addCalories(todayDate, cals);
                            loadUserData();
                            Toast.makeText(this, "Logged meal: +" + cals + " kcal", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid calorie amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogFoodDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_log_food, null);
        AutoCompleteTextView actvFastFood = dialogView.findViewById(R.id.actvFastFood);
        EditText etFoodCalories = dialogView.findViewById(R.id.etFoodCalories);
        etFoodCalories.setText("550");

        String[] fastFoodMenu = {
                "Zinger Burger (~550 kcal)",
                "French Fries (Medium) (~360 kcal)",
                "Pizza Slice (~280 kcal)",
                "Crispy Samosa (~260 kcal)",
                "Chicken Roll / Shawarma (~450 kcal)",
                "Panipuri / Golgappa (~200 kcal)",
                "Fried Chicken (1 pc) (~320 kcal)",
                "Cold Drink / Soda (Can) (~150 kcal)"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, fastFoodMenu);
        actvFastFood.setAdapter(adapter);
        actvFastFood.setText(fastFoodMenu[0], false);
        actvFastFood.setOnClickListener(v -> actvFastFood.showDropDown());
        actvFastFood.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actvFastFood.showDropDown();
        });

        actvFastFood.setOnItemClickListener((parent, view, position, id) -> {
            String selected = fastFoodMenu[position];
            int cals = 550;
            if (selected.contains("Zinger Burger")) cals = 550;
            else if (selected.contains("French Fries")) cals = 360;
            else if (selected.contains("Pizza Slice")) cals = 280;
            else if (selected.contains("Samosa")) cals = 260;
            else if (selected.contains("Shawarma")) cals = 450;
            else if (selected.contains("Panipuri")) cals = 200;
            else if (selected.contains("Fried Chicken")) cals = 320;
            else if (selected.contains("Cold Drink")) cals = 150;
            etFoodCalories.setText(String.valueOf(cals));
        });

        new AlertDialog.Builder(this)
                .setTitle("Log Fast Food & Street Food")
                .setView(dialogView)
                .setPositiveButton("Log Food", (dialog, which) -> {
                    String calStr = etFoodCalories.getText() != null && !etFoodCalories.getText().toString().trim().isEmpty() ?
                            etFoodCalories.getText().toString().trim() : "550";
                    try {
                        int cals = Integer.parseInt(calStr);
                        if (cals > 0) {
                            databaseHelper.addCalories(todayDate, cals);
                            loadUserData();
                            Toast.makeText(this, "Logged fast food: +" + cals + " kcal", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid calorie amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogGymWorkoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_log_gym, null);
        AutoCompleteTextView actvGymExercise = dialogView.findViewById(R.id.actvGymExercise);
        EditText etWorkoutMinutes = dialogView.findViewById(R.id.etWorkoutMinutes);
        EditText etCaloriesBurned = dialogView.findViewById(R.id.etCaloriesBurned);

        String[] exercises = {
                "Bench Press (Chest) (~8 kcal/min)",
                "Squats (Legs) (~10 kcal/min)",
                "Deadlifts (Back) (~10 kcal/min)",
                "Bicep Curls (Arms) (~6 kcal/min)",
                "Shoulder Press (Shoulders) (~7 kcal/min)",
                "Lat Pulldown (Back) (~7 kcal/min)",
                "Treadmill Cardio (Running) (~12 kcal/min)",
                "Cycling (Endurance) (~10 kcal/min)"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, exercises);
        actvGymExercise.setAdapter(adapter);
        actvGymExercise.setText(exercises[0], false);
        actvGymExercise.setOnClickListener(v -> actvGymExercise.showDropDown());
        actvGymExercise.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actvGymExercise.showDropDown();
        });

        etWorkoutMinutes.setText("30");
        etCaloriesBurned.setText("240");

        etWorkoutMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int mins = Integer.parseInt(s.toString().trim());
                    int rate = 8;
                    String sel = actvGymExercise.getText().toString();
                    if (sel.contains("Squats") || sel.contains("Deadlifts") || sel.contains("Treadmill")) rate = 10;
                    else if (sel.contains("Bicep")) rate = 6;
                    etCaloriesBurned.setText(String.valueOf(mins * rate));
                } catch (Exception ignored) {}
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Log Gym Workout")
                .setView(dialogView)
                .setPositiveButton("Log Workout", (dialog, which) -> {
                    String burnedStr = etCaloriesBurned.getText() != null && !etCaloriesBurned.getText().toString().trim().isEmpty() ?
                            etCaloriesBurned.getText().toString().trim() : "240";
                    try {
                        int burned = Integer.parseInt(burnedStr);
                        if (burned > 0) {
                            databaseHelper.addCaloriesBurned(todayDate, burned);
                            loadUserData();
                            Toast.makeText(this, "Gym logged: Burned " + burned + " kcal 🔥", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid calorie amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogMedicalDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_log_medical, null);
        EditText etBp = dialogView.findViewById(R.id.etBp);
        EditText etSugar = dialogView.findViewById(R.id.etSugar);
        EditText etPushups = dialogView.findViewById(R.id.etPushups);
        EditText etPullups = dialogView.findViewById(R.id.etPullups);
        EditText etRunning = dialogView.findViewById(R.id.etRunning);

        Cursor cursor = databaseHelper.getTodayMedicalReport(todayDate);
        if (cursor != null && cursor.moveToFirst()) {
            String bpVal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_BP));
            String sugarVal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_SUGAR));
            int pushupsVal = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_PUSHUPS));
            int pullupsVal = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_PULLUPS));
            float runningVal = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_RUNNING));

            if (bpVal != null && !bpVal.isEmpty()) etBp.setText(bpVal);
            if (sugarVal != null && !sugarVal.isEmpty()) etSugar.setText(sugarVal);
            etPushups.setText(String.valueOf(pushupsVal));
            etPullups.setText(String.valueOf(pullupsVal));
            etRunning.setText(String.valueOf(runningVal));
            cursor.close();
        }

        new AlertDialog.Builder(this)
                .setTitle("Log Daily Medical & Workout")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String bp = etBp.getText() != null && !etBp.getText().toString().trim().isEmpty() ? etBp.getText().toString().trim() : "120/80";
                    String sugarStr = etSugar.getText() != null && !etSugar.getText().toString().trim().isEmpty() ? etSugar.getText().toString().trim() : "90";

                    if (!bp.matches("\\d{2,3}/\\d{2,3}")) {
                        Toast.makeText(this, "Invalid BP format! Please enter as Systolic/Diastolic (e.g. 120/80)", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        float sugar = Float.parseFloat(sugarStr);
                        if (sugar < 30 || sugar > 600) {
                            Toast.makeText(this, "Please enter a realistic blood sugar value (30 - 600 mg/dL)", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid blood sugar value", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int pushups = parseOrDefault(etPushups.getText() != null ? etPushups.getText().toString() : "", 20);
                    int pullups = parseOrDefault(etPullups.getText() != null ? etPullups.getText().toString() : "", 5);
                    float running = parseFloatOrDefault(etRunning.getText() != null ? etRunning.getText().toString() : "", 2.0f);

                    if (pushups < 0 || pullups < 0 || running < 0.0f) {
                        Toast.makeText(this, "Workout counts and distance cannot be negative", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    databaseHelper.insertOrUpdateMedicalReport(todayDate, bp, sugarStr, pushups, pullups, running);
                    loadUserData();
                    Toast.makeText(this, "Medical & workout data logged successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private int parseOrDefault(String val, int def) {
        try {
            if (val == null || val.trim().isEmpty()) return def;
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private float parseFloatOrDefault(String val, float def) {
        try {
            if (val == null || val.trim().isEmpty()) return def;
            return Float.parseFloat(val.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private void onAvatarClicked() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            launchImagePicker();
        } else {
            if (shouldShowRequestPermissionRationale(permission)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("Storage/Media permission is required to select and set your custom profile picture from your device gallery.")
                        .setPositiveButton("Grant Permission", (dialog, which) -> permissionLauncher.launch(permission))
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                permissionLauncher.launch(permission);
            }
        }
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadUserData() {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
        String userName = prefManager.getUserName();
        tvUserName.setText(userName);

        String avatarUriStr = prefManager.getAvatarUri();
        if (avatarUriStr != null) {
            try {
                ivAvatar.setImageURI(Uri.parse(avatarUriStr));
            } catch (Exception e) {
            }
        }

        UserProfile profile = databaseHelper.getUserProfile();
        if (profile != null) {
            float heightCm = profile.getHeight();
            float weightKg = profile.getCurrentWeight();

            tvMetricsDetails.setText(String.format(Locale.getDefault(), "Height: %.1f cm | Weight: %.1f kg", heightCm, weightKg));

            if (heightCm > 0) {
                float heightM = heightCm / 100.0f;
                float bmi = weightKg / (heightM * heightM);
                tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));

                String category;
                if (bmi < 18.5f) {
                    category = "Underweight";
                } else if (bmi < 25.0f) {
                    category = "Normal Weight";
                } else if (bmi < 30.0f) {
                    category = "Overweight";
                } else {
                    category = "Obese";
                }
                tvBmiCategory.setText(category);
            }
        }

        DailyLog todayLog = databaseHelper.getTodayLog(todayDate);
        if (todayLog != null) {
            tvSummarySteps.setText(String.format(Locale.getDefault(), "%d steps", todayLog.getSteps()));
            tvSummaryWater.setText(String.format(Locale.getDefault(), "%d ml", todayLog.getWaterMl()));
            tvSummaryCalories.setText(String.format(Locale.getDefault(), "%d kcal", todayLog.getCaloriesConsumed()));
        }

        int stepsBurned = todayLog != null ? (int) (todayLog.getSteps() * 0.04f) : 0;
        int gymBurned = todayLog != null ? todayLog.getCaloriesBurned() : 0;

        Cursor cursor = databaseHelper.getTodayMedicalReport(todayDate);
        float runningKm = 0.0f;
        int pushups = 0;
        int pullups = 0;
        if (cursor != null && cursor.moveToFirst()) {
            String bp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_BP));
            String sugar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_SUGAR));
            pushups = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_PUSHUPS));
            pullups = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_PULLUPS));
            runningKm = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_RUNNING));

            tvSummaryBp.setText(String.format(Locale.getDefault(), "• Blood Pressure: %s mmHg", bp));
            tvSummarySugar.setText(String.format(Locale.getDefault(), "• Blood Sugar: %s mg/dL", sugar));
            tvSummaryPushups.setText(String.format(Locale.getDefault(), "• Push-ups: %d reps", pushups));
            tvSummaryPullups.setText(String.format(Locale.getDefault(), "• Pull-ups: %d reps", pullups));
            tvSummaryRunning.setText(String.format(Locale.getDefault(), "• Running Distance: %.1f km", runningKm));
            cursor.close();
        } else {
            tvSummaryBp.setText("• Blood Pressure: Not logged");
            tvSummarySugar.setText("• Blood Sugar: Not logged");
            tvSummaryPushups.setText("• Push-ups: 0 reps");
            tvSummaryPullups.setText("• Pull-ups: 0 reps");
            tvSummaryRunning.setText("• Running Distance: 0.0 km");
        }

        int runningBurned = (int) (runningKm * 60.0f);
        int pushupsBurned = (int) (pushups * 0.5f);
        int pullupsBurned = (int) (pullups * 1.0f);

        int totalBurned = gymBurned + stepsBurned + runningBurned + pushupsBurned + pullupsBurned;
        tvSummaryBurned.setText(String.format(Locale.getDefault(), "%d kcal", totalBurned));
    }

    private void showLogWaterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_log_simple, null);
        TextView tvLabel = dialogView.findViewById(R.id.tvSimpleLabel);
        if (tvLabel != null) tvLabel.setText("Water (ml)");
        EditText input = dialogView.findViewById(R.id.etSimpleInput);
        input.setHint("250");

        new AlertDialog.Builder(this)
                .setTitle("Log Water Intake (ml)")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String valStr = input.getText() != null ? input.getText().toString().trim() : "";
                    if (valStr.isEmpty()) valStr = "250";
                    try {
                        int amount = Integer.parseInt(valStr);
                        if (amount > 0) {
                            databaseHelper.addWater(todayDate, amount);
                            loadUserData();
                            Toast.makeText(this, "Added " + amount + " ml of water", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogStepsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_log_simple, null);
        TextView tvLabel = dialogView.findViewById(R.id.tvSimpleLabel);
        if (tvLabel != null) tvLabel.setText("Steps Count");
        EditText input = dialogView.findViewById(R.id.etSimpleInput);
        input.setHint("1000");

        new AlertDialog.Builder(this)
                .setTitle("Log Steps")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String valStr = input.getText() != null ? input.getText().toString().trim() : "";
                    if (valStr.isEmpty()) valStr = "1000";
                    try {
                        int stepsCount = Integer.parseInt(valStr);
                        if (stepsCount > 0) {
                            databaseHelper.addSteps(todayDate, stepsCount);
                            loadUserData();
                            Toast.makeText(this, "Added " + stepsCount + " steps", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        String avatarUriStr = SharedPrefManager.getInstance(this).getAvatarUri();
        if (avatarUriStr != null) {
            try {
                ivAvatar.setImageURI(Uri.parse(avatarUriStr));
            } catch (Exception e) {
            }
        }
        loadUserData();
        startStepCounterService();

        IntentFilter filter = new IntentFilter("com.example.myhealth.STEP_UPDATE");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stepUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(stepUpdateReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(stepUpdateReceiver);
        } catch (Exception ignored) {}
    }
}
