package com.example.myhealth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.myhealth.data.DatabaseHelper;
import com.example.myhealth.data.SharedPrefManager;
import com.example.myhealth.model.UserProfile;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etContact, etEmail, etAge, etHeight, etCurrentWeight, etTargetWeight;
    private TextInputLayout tilHeight, tilCurrentWeight, tilTargetWeight;
    private AutoCompleteTextView actvCountry, actvActivityLevel, actvGoal;
    private RadioGroup rgGender, rgUnitSystem;
    private MaterialButton btnSaveProfile;

    private DatabaseHelper databaseHelper;
    private boolean isEditMode = false;
    private long existingProfileId = -1;
    private boolean isMetric = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupDropdowns();
        setupUnitSystemListener();
        checkExistingProfile();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(isEditMode);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etFullName = findViewById(R.id.etFullName);
        etContact = findViewById(R.id.etContact);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etCurrentWeight = findViewById(R.id.etCurrentWeight);
        etTargetWeight = findViewById(R.id.etTargetWeight);

        tilHeight = (TextInputLayout) etHeight.getParent().getParent();
        tilCurrentWeight = (TextInputLayout) etCurrentWeight.getParent().getParent();
        tilTargetWeight = (TextInputLayout) etTargetWeight.getParent().getParent();

        actvCountry = findViewById(R.id.actvCountry);
        actvActivityLevel = findViewById(R.id.actvActivityLevel);
        actvGoal = findViewById(R.id.actvGoal);

        rgGender = findViewById(R.id.rgGender);
        rgUnitSystem = findViewById(R.id.rgUnitSystem);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void setupDropdowns() {
        String[] countries = {
                "Pakistan", "India", "Bangladesh", "China", "Japan", "South Korea",
                "Indonesia", "Malaysia", "Philippines", "Vietnam", "Thailand",
                "Singapore", "Saudi Arabia", "United Arab Emirates", "Turkey",
                "Iran", "Iraq", "Nepal", "Sri Lanka", "Afghanistan",
                "United States", "United Kingdom", "Canada", "Australia",
                "Germany", "France", "Italy", "Spain", "Brazil", "Other"
        };
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, countries);
        actvCountry.setAdapter(countryAdapter);
        actvCountry.setThreshold(1);

        String[] activityLevels = {"Sedentary", "Lightly Active", "Moderately Active", "Very Active"};
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, activityLevels);
        actvActivityLevel.setAdapter(activityAdapter);

        String[] goals = {"Lose Weight", "Maintain", "Build Muscle"};
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, goals);
        actvGoal.setAdapter(goalAdapter);
    }

    private void setupUnitSystemListener() {
        rgUnitSystem.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbImperial) {
                isMetric = false;
                tilHeight.setHint("Height (in)");
                tilCurrentWeight.setHint("Current Weight (lbs)");
                tilTargetWeight.setHint("Target Weight (lbs)");
            } else {
                isMetric = true;
                tilHeight.setHint("Height (cm)");
                tilCurrentWeight.setHint("Current Weight (kg)");
                tilTargetWeight.setHint("Target Weight (kg)");
            }
        });
    }

    private void checkExistingProfile() {
        UserProfile profile = databaseHelper.getUserProfile();
        if (profile != null) {
            isEditMode = true;
            existingProfileId = profile.getId();

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            etFullName.setText(profile.getName());
            etContact.setText(profile.getContact());
            etEmail.setText(profile.getEmail());
            actvCountry.setText(profile.getCountry(), false);
            
            if ("Female".equals(profile.getGender())) {
                rgGender.check(R.id.rbFemale);
            } else if ("Other".equals(profile.getGender())) {
                rgGender.check(R.id.rbOther);
            } else {
                rgGender.check(R.id.rbMale);
            }

            etAge.setText(String.valueOf(profile.getAge()));

            String unitSystem = SharedPrefManager.getInstance(this).getUnitSystem();
            if ("Imperial".equals(unitSystem)) {
                rgUnitSystem.check(R.id.rbImperial);
                isMetric = false;
                float heightInches = profile.getHeight() / 2.54f;
                float weightLbs = profile.getCurrentWeight() / 0.45359237f;
                float targetLbs = profile.getTargetWeight() / 0.45359237f;
                etHeight.setText(String.format(Locale.getDefault(), "%.1f", heightInches));
                etCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f", weightLbs));
                etTargetWeight.setText(String.format(Locale.getDefault(), "%.1f", targetLbs));
                tilHeight.setHint("Height (in)");
                tilCurrentWeight.setHint("Current Weight (lbs)");
                tilTargetWeight.setHint("Target Weight (lbs)");
            } else {
                rgUnitSystem.check(R.id.rbMetric);
                isMetric = true;
                etHeight.setText(String.valueOf(profile.getHeight()));
                etCurrentWeight.setText(String.valueOf(profile.getCurrentWeight()));
                etTargetWeight.setText(String.valueOf(profile.getTargetWeight()));
            }

            actvActivityLevel.setText(profile.getActivityLevel(), false);
            actvGoal.setText(profile.getGoal(), false);
            btnSaveProfile.setText(R.string.btn_update_profile);
        }
    }

    private void saveProfileData() {
        String name = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String country = actvCountry.getText() != null ? actvCountry.getText().toString().trim() : "";
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
        String weightStr = etCurrentWeight.getText() != null ? etCurrentWeight.getText().toString().trim() : "";
        String targetWeightStr = etTargetWeight.getText() != null ? etTargetWeight.getText().toString().trim() : "";
        String activityLevel = actvActivityLevel.getText() != null ? actvActivityLevel.getText().toString().trim() : "";
        String goal = actvGoal.getText() != null ? actvGoal.getText().toString().trim() : "";

        // Validation & Constraints
        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(contact)) {
            etContact.setError("Contact number is required");
            etContact.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email address is required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(country)) {
            actvCountry.setError("Please select country");
            actvCountry.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(ageStr)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(heightStr)) {
            etHeight.setError("Height is required");
            etHeight.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(weightStr)) {
            etCurrentWeight.setError("Current weight is required");
            etCurrentWeight.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(targetWeightStr)) {
            etTargetWeight.setError("Target weight is required");
            etTargetWeight.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(activityLevel)) {
            actvActivityLevel.setError("Activity level is required");
            actvActivityLevel.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(goal)) {
            actvGoal.setError("Fitness goal is required");
            actvGoal.requestFocus();
            return;
        }

        int age;
        float heightInput, weightInput, targetWeightInput;
        try {
            age = Integer.parseInt(ageStr);
            heightInput = Float.parseFloat(heightStr);
            weightInput = Float.parseFloat(weightStr);
            targetWeightInput = Float.parseFloat(targetWeightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
            return;
        }

        // Strict Range Constraints Validation
        if (age < 1 || age > 120) {
            etAge.setError("Please enter a valid age (1 - 120)");
            etAge.requestFocus();
            return;
        }

        float heightCm, currentWeightKg, targetWeightKg;
        if (isMetric) {
            if (heightInput < 50.0f || heightInput > 250.0f) {
                etHeight.setError("Height must be between 50 and 250 cm");
                etHeight.requestFocus();
                return;
            }
            if (weightInput < 20.0f || weightInput > 300.0f) {
                etCurrentWeight.setError("Weight must be between 20 and 300 kg");
                etCurrentWeight.requestFocus();
                return;
            }
            if (targetWeightInput < 20.0f || targetWeightInput > 300.0f) {
                etTargetWeight.setError("Target weight must be between 20 and 300 kg");
                etTargetWeight.requestFocus();
                return;
            }
            heightCm = heightInput;
            currentWeightKg = weightInput;
            targetWeightKg = targetWeightInput;
        } else {
            if (heightInput < 20.0f || heightInput > 100.0f) {
                etHeight.setError("Height must be between 20 and 100 inches");
                etHeight.requestFocus();
                return;
            }
            if (weightInput < 44.0f || weightInput > 660.0f) {
                etCurrentWeight.setError("Weight must be between 44 and 660 lbs");
                etCurrentWeight.requestFocus();
                return;
            }
            if (targetWeightInput < 44.0f || targetWeightInput > 660.0f) {
                etTargetWeight.setError("Target weight must be between 44 and 660 lbs");
                etTargetWeight.requestFocus();
                return;
            }
            // Convert Imperial to Metric for database storage
            heightCm = heightInput * 2.54f;
            currentWeightKg = weightInput * 0.45359237f;
            targetWeightKg = targetWeightInput * 0.45359237f;
        }

        String gender;
        int checkedId = rgGender.getCheckedRadioButtonId();
        if (checkedId == R.id.rbFemale) {
            gender = "Female";
        } else if (checkedId == R.id.rbOther) {
            gender = "Other";
        } else {
            gender = "Male";
        }

        boolean success;
        if (isEditMode) {
            success = databaseHelper.updateUserProfile(existingProfileId, name, contact, email, country, gender, age, heightCm, currentWeightKg, targetWeightKg, activityLevel, goal);
        } else {
            long id = databaseHelper.insertUserProfile(name, contact, email, country, gender, age, heightCm, currentWeightKg, targetWeightKg, activityLevel, goal);
            success = id != -1;
        }

        if (success) {
            SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
            prefManager.setProfileCompleted(true);
            prefManager.setUserName(name);
            prefManager.setUserEmail(email);
            prefManager.setUnitSystem(isMetric ? "Metric" : "Imperial");

            Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
