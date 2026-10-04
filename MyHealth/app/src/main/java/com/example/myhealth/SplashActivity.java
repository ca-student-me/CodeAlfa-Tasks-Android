package com.example.myhealth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myhealth.data.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkAndRequestPermissionsConsistently();
    }

    private void checkAndRequestPermissionsConsistently() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions = new String[]{
                        Manifest.permission.ACTIVITY_RECOGNITION,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.READ_MEDIA_IMAGES
                };
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions = new String[]{
                        Manifest.permission.ACTIVITY_RECOGNITION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };
            } else {
                permissions = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };
            }

            boolean allGranted = true;
            for (String perm : permissions) {
                if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                proceedAfterPermissions();
            } else {
                showPermissionRationaleDialog(permissions);
            }
        } else {
            proceedAfterPermissions();
        }
    }

    private void showPermissionRationaleDialog(String[] permissions) {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("My Health requires Activity Recognition, Notifications, and Storage permissions to track your daily steps 24/7, send meal reminders, and set your profile avatar.\n\nPlease allow permissions to continue.")
                .setCancelable(false)
                .setPositiveButton("Grant Permissions", (dialog, which) -> {
                    requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                proceedAfterPermissions();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Mandatory")
                        .setMessage("Without these permissions, My Health cannot track steps or send vital reminders. Please grant permissions to proceed.")
                        .setCancelable(false)
                        .setPositiveButton("Try Again", (dialog, which) -> {
                            checkAndRequestPermissionsConsistently();
                        })
                        .show();
            }
        }
    }

    private void proceedAfterPermissions() {
        boolean isProfileCompleted = SharedPrefManager.getInstance(SplashActivity.this).isProfileCompleted();
        Intent intent;
        if (isProfileCompleted) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, ProfileActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
