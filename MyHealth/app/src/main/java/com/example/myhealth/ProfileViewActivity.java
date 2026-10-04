package com.example.myhealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import com.example.myhealth.data.DatabaseHelper;
import com.example.myhealth.data.SharedPrefManager;
import com.example.myhealth.model.UserProfile;

import java.util.Locale;

public class ProfileViewActivity extends AppCompatActivity {

    private TextView tvViewName, tvViewContact, tvViewEmail, tvViewCountry,
            tvViewGender, tvViewAge, tvViewHeight, tvViewWeight,
            tvViewTargetWeight, tvViewActivity, tvViewGoal;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        loadProfileData();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarProfileView);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvViewName = findViewById(R.id.tvViewName);
        tvViewContact = findViewById(R.id.tvViewContact);
        tvViewEmail = findViewById(R.id.tvViewEmail);
        tvViewCountry = findViewById(R.id.tvViewCountry);
        tvViewGender = findViewById(R.id.tvViewGender);
        tvViewAge = findViewById(R.id.tvViewAge);
        tvViewHeight = findViewById(R.id.tvViewHeight);
        tvViewWeight = findViewById(R.id.tvViewWeight);
        tvViewTargetWeight = findViewById(R.id.tvViewTargetWeight);
        tvViewActivity = findViewById(R.id.tvViewActivity);
        tvViewGoal = findViewById(R.id.tvViewGoal);

        MaterialButton btnEditProfilePrompt = findViewById(R.id.btnEditProfilePrompt);
        btnEditProfilePrompt.setOnClickListener(v -> showEditConfirmationDialog());
    }

    private void loadProfileData() {
        UserProfile profile = databaseHelper.getUserProfile();
        if (profile != null) {
            tvViewName.setText(profile.getName());
            tvViewContact.setText(profile.getContact());
            tvViewEmail.setText(profile.getEmail());
            tvViewCountry.setText(profile.getCountry());
            tvViewGender.setText(profile.getGender());
            tvViewAge.setText(String.valueOf(profile.getAge()));

            String unitSystem = SharedPrefManager.getInstance(this).getUnitSystem();
            if ("Imperial".equals(unitSystem)) {
                float heightInches = profile.getHeight() / 2.54f;
                float weightLbs = profile.getCurrentWeight() / 0.45359237f;
                float targetLbs = profile.getTargetWeight() / 0.45359237f;
                tvViewHeight.setText(String.format(Locale.getDefault(), "%.1f in", heightInches));
                tvViewWeight.setText(String.format(Locale.getDefault(), "%.1f lbs", weightLbs));
                tvViewTargetWeight.setText(String.format(Locale.getDefault(), "%.1f lbs", targetLbs));
            } else {
                tvViewHeight.setText(String.format(Locale.getDefault(), "%.1f cm", profile.getHeight()));
                tvViewWeight.setText(String.format(Locale.getDefault(), "%.1f kg", profile.getCurrentWeight()));
                tvViewTargetWeight.setText(String.format(Locale.getDefault(), "%.1f kg", profile.getTargetWeight()));
            }

            tvViewActivity.setText(profile.getActivityLevel());
            tvViewGoal.setText(profile.getGoal());
        }
    }

    private void showEditConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setMessage("Do you want to open the profile editor?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }
}
