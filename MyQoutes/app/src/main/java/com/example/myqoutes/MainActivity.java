package com.example.myqoutes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long SPLASH_DURATION_MS = 2500L; // 2.5 seconds

    private PreferenceHelper preferenceHelper;
    private DatabaseHelper databaseHelper;

    private View mainLayout;
    private MaterialCardView cardQuote;
    private TextView tvQuoteText;
    private TextView tvAuthor;
    private TextView tvBadge;
    private MaterialButton btnNewQuote;
    private View splashOverlay;

    private Quote currentQuote;
    private boolean wasInBackground = false;
    private boolean keepSplashOnScreen = true;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted.");
                    scheduleDefaultNotificationIfNeeded();
                } else {
                    Log.w(TAG, "Notification permission denied.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplashOnScreen);

        new Handler(Looper.getMainLooper()).postDelayed(() -> keepSplashOnScreen = false, SPLASH_DURATION_MS);

        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        preferenceHelper = new PreferenceHelper(this);
        databaseHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupToolbar();
        setupEdgeToEdgeInsets();

        NotificationHelper.createNotificationChannel(this);
        checkAndRequestNotificationPermission();
        scheduleDefaultNotificationIfNeeded();

        boolean isFreshLaunch = (savedInstanceState == null);
        handleIntentOrLoadQuote(getIntent(), isFreshLaunch);

        btnNewQuote.setOnClickListener(v -> loadNextRandomQuote());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (wasInBackground) {
            runSplashScreenSequence();
            handleIntentOrLoadQuote(getIntent(), false);
            wasInBackground = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        wasInBackground = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentOrLoadQuote(intent, false);
    }

    private void initViews() {
        mainLayout = findViewById(R.id.main_layout);
        cardQuote = findViewById(R.id.card_quote);
        tvQuoteText = findViewById(R.id.tv_quote_text);
        tvAuthor = findViewById(R.id.tv_author);
        tvBadge = findViewById(R.id.tv_badge);
        btnNewQuote = findViewById(R.id.btn_new_quote);
        splashOverlay = findViewById(R.id.splash_overlay);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());

            v.setPadding(insets.left, insets.top, insets.right, 0);

            ViewGroup.MarginLayoutParams buttonParams = (ViewGroup.MarginLayoutParams) btnNewQuote.getLayoutParams();
            int baseBottomMargin = (int) (36 * getResources().getDisplayMetrics().density);
            buttonParams.bottomMargin = baseBottomMargin + insets.bottom;
            btnNewQuote.setLayoutParams(buttonParams);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void runSplashScreenSequence() {
        if (splashOverlay == null) return;
        splashOverlay.setAlpha(1.0f);
        splashOverlay.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splashOverlay.animate()
                    .alpha(0.0f)
                    .setDuration(400)
                    .withEndAction(() -> splashOverlay.setVisibility(View.GONE))
                    .start();
        }, SPLASH_DURATION_MS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            showAddQuoteBottomSheet();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleIntentOrLoadQuote(Intent intent, boolean isFresh) {
        int notificationQuoteId = -1;
        if (intent != null && intent.hasExtra(NotificationHelper.EXTRA_QUOTE_ID)) {
            notificationQuoteId = intent.getIntExtra(NotificationHelper.EXTRA_QUOTE_ID, -1);
            intent.removeExtra(NotificationHelper.EXTRA_QUOTE_ID);
        }

        if (notificationQuoteId > 0) {
            Quote quote = databaseHelper.getQuoteById(notificationQuoteId);
            if (quote != null) {
                displayQuote(quote, false);
                return;
            }
        }

        if (!isFresh) {
            int lastQuoteId = preferenceHelper.getLastViewedQuoteId();
            if (lastQuoteId > 0) {
                Quote quote = databaseHelper.getQuoteById(lastQuoteId);
                if (quote != null) {
                    displayQuote(quote, false);
                    return;
                }
            }
        }

        loadNextRandomQuote();
    }

    private void loadNextRandomQuote() {
        int currentId = currentQuote != null ? currentQuote.getId() : -1;
        Quote quote = databaseHelper.getRandomQuote(currentId);
        if (quote != null) {
            displayQuote(quote, true);
        }
    }

    private void displayQuote(Quote quote, boolean animate) {
        this.currentQuote = quote;
        preferenceHelper.setLastViewedQuoteId(quote.getId());

        if (animate) {
            cardQuote.animate()
                    .alpha(0.0f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        updateQuoteUi(quote);
                        cardQuote.animate()
                                .alpha(1.0f)
                                .setDuration(250)
                                .start();
                    })
                    .start();
        } else {
            updateQuoteUi(quote);
        }
    }

    private void updateQuoteUi(Quote quote) {
        tvQuoteText.setText(String.format("“%s”", quote.getQuoteText()));
        tvAuthor.setText(String.format("— %s", quote.getAuthor()));

        if (quote.isCustom()) {
            tvBadge.setText(R.string.custom_quote_badge);
            tvBadge.setVisibility(View.VISIBLE);
        } else {
            tvBadge.setVisibility(View.GONE);
        }
    }

    private void showAddQuoteBottomSheet() {
        AddQuoteBottomSheet bottomSheet = AddQuoteBottomSheet.newInstance();
        bottomSheet.setOnQuoteSaveListener(newQuote -> {
            displayQuote(newQuote, true);
            Snackbar.make(mainLayout, R.string.quote_saved_success, Snackbar.LENGTH_SHORT).show();
        });
        bottomSheet.show(getSupportFragmentManager(), "ADD_QUOTE_BOTTOM_SHEET");
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void scheduleDefaultNotificationIfNeeded() {
        if (preferenceHelper.isNotificationsEnabled()) {
            NotificationHelper.scheduleDailyNotification(
                    this,
                    preferenceHelper.getNotificationHour(),
                    preferenceHelper.getNotificationMinute()
            );
        }
    }
}
