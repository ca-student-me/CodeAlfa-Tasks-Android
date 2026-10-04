package com.example.myquiz;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Activity managing Flashcard Quiz study session, 3D card flips, deck navigation,
 * persistent card index across sessions/backgrounding, and zero-lag background splash overlay.
 */
public class MainActivity extends AppCompatActivity {

    private static final String KEY_CURRENT_INDEX = "key_current_index";
    private static final String KEY_IS_ANSWER_REVEALED = "key_is_answer_revealed";
    private static final String PREFS_QUIZ = "quiz_session_prefs";
    private static final String KEY_SAVED_CARD_INDEX = "key_saved_card_index";
    private static final int ANIMATION_DURATION_MS = 650;
    private static final int SPLASH_BACKGROUND_DELAY_MS = 3000;

    // Data Layer
    private DatabaseHelper dbHelper;
    private final List<Flashcard> flashcards = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isAnswerRevealed = false;

    // App Background State Tracking
    private boolean isAppInBackground = false;

    // Concurrency / Threading
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Views
    private MaterialToolbar toolbar;
    private LinearProgressIndicator progressBarDeck;
    private ConstraintLayout layoutTopHeader;
    private TextView tvCardProgress;
    private MaterialButton btnEditCard;
    private MaterialButton btnDeleteCard;

    private FrameLayout cardContainer;
    private MaterialCardView cardFrontView;
    private MaterialCardView cardBackView;
    private TextView tvQuestionHeader;
    private TextView tvQuestionText;
    private TextView tvAnswerText;
    private MaterialButton btnFrontFlipCard;
    private MaterialButton btnBackFlipCard;

    private LinearLayout layoutNavigation;
    private MaterialButton btnPreviousCard;
    private MaterialButton btnNextCard;

    private FloatingActionButton fabAddCard;
    private ConstraintLayout layoutEmptyState;
    private MaterialButton btnEmptyAddCard;
    private ConstraintLayout layoutSplashOverlay;

    // Card Flip Animators
    private AnimatorSet animLeftOut;
    private AnimatorSet animLeftIn;
    private AnimatorSet animRightOut;
    private AnimatorSet animRightIn;
    private boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enforce Light Theme globally across the application
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DatabaseHelper.getInstance(this);

        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt(KEY_CURRENT_INDEX, 0);
            isAnswerRevealed = savedInstanceState.getBoolean(KEY_IS_ANSWER_REVEALED, false);
        } else {
            // Restore last opened card index from SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREFS_QUIZ, MODE_PRIVATE);
            currentIndex = prefs.getInt(KEY_SAVED_CARD_INDEX, 0);
        }

        initViews();
        setupCameraDistance();
        setupAnimators();
        setupListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If returning from background, display Splash Overlay for 3 seconds with zero freeze
        if (isAppInBackground) {
            isAppInBackground = false;
            showBackgroundSplashOverlay();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCardsFromDatabase(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isAppInBackground = true;
        saveCurrentCardIndex();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_INDEX, currentIndex);
        outState.putBoolean(KEY_IS_ANSWER_REVEALED, isAnswerRevealed);
        saveCurrentCardIndex();
    }

    private void saveCurrentCardIndex() {
        SharedPreferences prefs = getSharedPreferences(PREFS_QUIZ, MODE_PRIVATE);
        prefs.edit().putInt(KEY_SAVED_CARD_INDEX, currentIndex).apply();
    }

    private void showBackgroundSplashOverlay() {
        if (layoutSplashOverlay != null) {
            layoutSplashOverlay.setVisibility(View.VISIBLE);
            mainHandler.postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    layoutSplashOverlay.setVisibility(View.GONE);
                }
            }, SPLASH_BACKGROUND_DELAY_MS);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBarDeck = findViewById(R.id.progressBarDeck);
        layoutTopHeader = findViewById(R.id.layoutTopHeader);
        tvCardProgress = findViewById(R.id.tvCardProgress);
        btnEditCard = findViewById(R.id.btnEditCard);
        btnDeleteCard = findViewById(R.id.btnDeleteCard);

        cardContainer = findViewById(R.id.cardContainer);
        cardFrontView = findViewById(R.id.cardFrontView);
        cardBackView = findViewById(R.id.cardBackView);
        tvQuestionHeader = findViewById(R.id.tvQuestionHeader);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        tvAnswerText = findViewById(R.id.tvAnswerText);
        btnFrontFlipCard = findViewById(R.id.btnFrontFlipCard);
        btnBackFlipCard = findViewById(R.id.btnBackFlipCard);

        layoutNavigation = findViewById(R.id.layoutNavigation);
        btnPreviousCard = findViewById(R.id.btnPreviousCard);
        btnNextCard = findViewById(R.id.btnNextCard);

        fabAddCard = findViewById(R.id.fabAddCard);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnEmptyAddCard = findViewById(R.id.btnEmptyAddCard);
        layoutSplashOverlay = findViewById(R.id.layoutSplashOverlay);
    }

    private void setupCameraDistance() {
        float scale = getResources().getDisplayMetrics().density;
        float cameraDistance = scale * 8000;
        cardFrontView.setCameraDistance(cameraDistance);
        cardBackView.setCameraDistance(cameraDistance);
    }

    private void setupAnimators() {
        animLeftOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_out);
        animLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_in);
        animRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_out);
        animRightIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_in);
    }

    private void setupListeners() {
        // Direct Settings Action on Toolbar
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        // Tapping card or show/hide button on bottom of flashcard flips card
        cardFrontView.setOnClickListener(v -> performCardFlip());
        cardBackView.setOnClickListener(v -> performCardFlip());
        btnFrontFlipCard.setOnClickListener(v -> performCardFlip());
        btnBackFlipCard.setOnClickListener(v -> performCardFlip());

        // Deck Navigation below flashcard
        btnPreviousCard.setOnClickListener(v -> navigatePrevious());
        btnNextCard.setOnClickListener(v -> navigateNext());

        // CRUD actions
        fabAddCard.setOnClickListener(v -> showAddEditCardDialog(null));
        btnEmptyAddCard.setOnClickListener(v -> showAddEditCardDialog(null));
        btnEditCard.setOnClickListener(v -> {
            if (!flashcards.isEmpty() && currentIndex < flashcards.size()) {
                showAddEditCardDialog(flashcards.get(currentIndex));
            }
        });
        btnDeleteCard.setOnClickListener(v -> {
            if (!flashcards.isEmpty() && currentIndex < flashcards.size()) {
                showDeleteConfirmationDialog(flashcards.get(currentIndex));
            }
        });
    }

    private void loadCardsFromDatabase(boolean preserveViewState) {
        executor.execute(() -> {
            List<Flashcard> list = dbHelper.getActiveCards();
            mainHandler.post(() -> {
                flashcards.clear();
                flashcards.addAll(list);

                if (flashcards.isEmpty()) {
                    currentIndex = 0;
                    isAnswerRevealed = false;
                } else {
                    SharedPreferences prefs = getSharedPreferences(PREFS_QUIZ, MODE_PRIVATE);
                    int savedIndex = prefs.getInt(KEY_SAVED_CARD_INDEX, currentIndex);

                    if (savedIndex >= 0 && savedIndex < flashcards.size()) {
                        currentIndex = savedIndex;
                    } else if (currentIndex >= flashcards.size()) {
                        currentIndex = flashcards.size() - 1;
                    } else if (currentIndex < 0) {
                        currentIndex = 0;
                    }

                    if (!preserveViewState) {
                        isAnswerRevealed = false;
                    }
                }
                updateUiState();
            });
        });
    }

    private void updateUiState() {
        if (flashcards.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            layoutTopHeader.setVisibility(View.GONE);
            cardContainer.setVisibility(View.GONE);
            layoutNavigation.setVisibility(View.GONE);
            progressBarDeck.setVisibility(View.GONE);
            tvCardProgress.setText(R.string.empty_state_title);
            return;
        }

        layoutEmptyState.setVisibility(View.GONE);
        layoutTopHeader.setVisibility(View.VISIBLE);
        cardContainer.setVisibility(View.VISIBLE);
        layoutNavigation.setVisibility(View.VISIBLE);
        progressBarDeck.setVisibility(View.VISIBLE);

        int total = flashcards.size();
        progressBarDeck.setMax(total);
        progressBarDeck.setProgress(currentIndex + 1);
        tvCardProgress.setText(getString(R.string.card_progress_format, currentIndex + 1, total));

        btnPreviousCard.setEnabled(currentIndex > 0);
        btnNextCard.setEnabled(currentIndex < total - 1);

        Flashcard card = flashcards.get(currentIndex);
        if (!TextUtils.isEmpty(card.getCategoryName())) {
            tvQuestionHeader.setText(card.getCategoryName().toUpperCase());
        } else {
            tvQuestionHeader.setText(R.string.question_label);
        }

        tvQuestionText.setText(card.getQuestion());
        tvAnswerText.setText(card.getAnswer());

        applyStaticFlipState();
    }

    private void performCardFlip() {
        if (flashcards.isEmpty() || isAnimating) {
            return;
        }

        isAnimating = true;

        if (!isAnswerRevealed) {
            cardBackView.setVisibility(View.VISIBLE);
            animLeftOut.setTarget(cardFrontView);
            animLeftIn.setTarget(cardBackView);

            animLeftOut.start();
            animLeftIn.start();

            isAnswerRevealed = true;
        } else {
            cardFrontView.setVisibility(View.VISIBLE);
            animRightOut.setTarget(cardBackView);
            animRightIn.setTarget(cardFrontView);

            animRightOut.start();
            animRightIn.start();

            isAnswerRevealed = false;
        }

        mainHandler.postDelayed(() -> {
            if (isAnswerRevealed) {
                cardFrontView.setVisibility(View.GONE);
            } else {
                cardBackView.setVisibility(View.GONE);
            }
            isAnimating = false;
        }, ANIMATION_DURATION_MS);
    }

    private void applyStaticFlipState() {
        cardFrontView.clearAnimation();
        cardBackView.clearAnimation();

        if (isAnswerRevealed) {
            cardFrontView.setVisibility(View.GONE);
            cardFrontView.setRotationY(180f);
            cardFrontView.setAlpha(0f);

            cardBackView.setVisibility(View.VISIBLE);
            cardBackView.setRotationY(0f);
            cardBackView.setAlpha(1.0f);
        } else {
            cardBackView.setVisibility(View.GONE);
            cardBackView.setRotationY(-180f);
            cardBackView.setAlpha(0f);

            cardFrontView.setVisibility(View.VISIBLE);
            cardFrontView.setRotationY(0f);
            cardFrontView.setAlpha(1.0f);
        }
    }

    private void navigatePrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            isAnswerRevealed = false;
            saveCurrentCardIndex();
            updateUiState();
        }
    }

    private void navigateNext() {
        if (currentIndex < flashcards.size() - 1) {
            currentIndex++;
            isAnswerRevealed = false;
            saveCurrentCardIndex();
            updateUiState();
        }
    }

    private void showAddEditCardDialog(@Nullable Flashcard cardToEdit) {
        boolean isEditing = cardToEdit != null;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_card, null);
        AutoCompleteTextView actvCategorySelect = dialogView.findViewById(R.id.actvCategorySelect);
        TextInputLayout tilQuestion = dialogView.findViewById(R.id.tilQuestion);
        TextInputLayout tilAnswer = dialogView.findViewById(R.id.tilAnswer);
        TextInputEditText etQuestion = dialogView.findViewById(R.id.etQuestion);
        TextInputEditText etAnswer = dialogView.findViewById(R.id.etAnswer);

        final List<Category> categoriesList = new ArrayList<>();
        final long[] selectedCategoryId = {1};

        executor.execute(() -> {
            List<Category> cats = dbHelper.getAllCategories();
            mainHandler.post(() -> {
                categoriesList.clear();
                categoriesList.addAll(cats);
                if (categoriesList.isEmpty()) {
                    categoriesList.add(new Category(1, "General", true));
                }

                ArrayAdapter<Category> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoriesList);
                actvCategorySelect.setAdapter(catAdapter);

                int defaultIndex = 0;
                if (isEditing) {
                    for (int i = 0; i < categoriesList.size(); i++) {
                        if (categoriesList.get(i).getId() == cardToEdit.getCategoryId()) {
                            defaultIndex = i;
                            break;
                        }
                    }
                }
                actvCategorySelect.setText(categoriesList.get(defaultIndex).getName(), false);
                selectedCategoryId[0] = categoriesList.get(defaultIndex).getId();

                actvCategorySelect.setOnItemClickListener((parent, view, position, id) -> {
                    Category selectedCat = (Category) parent.getItemAtPosition(position);
                    selectedCategoryId[0] = selectedCat.getId();
                });
            });
        });

        if (isEditing) {
            etQuestion.setText(cardToEdit.getQuestion());
            etAnswer.setText(cardToEdit.getAnswer());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEditing ? R.string.edit_flashcard_title : R.string.add_flashcard_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String question = etQuestion.getText() != null ? etQuestion.getText().toString().trim() : "";
            String answer = etAnswer.getText() != null ? etAnswer.getText().toString().trim() : "";

            boolean questionEmpty = TextUtils.isEmpty(question);
            boolean answerEmpty = TextUtils.isEmpty(answer);

            if (questionEmpty) {
                tilQuestion.setError(getString(R.string.error_empty_question));
            } else {
                tilQuestion.setError(null);
            }

            if (answerEmpty) {
                tilAnswer.setError(getString(R.string.error_empty_answer));
            } else {
                tilAnswer.setError(null);
            }

            if (!questionEmpty && !answerEmpty) {
                if (isEditing) {
                    performUpdateCard(cardToEdit.getId(), question, answer, selectedCategoryId[0]);
                } else {
                    performInsertCard(question, answer, selectedCategoryId[0]);
                }
                dialog.dismiss();
            }
        });
    }

    private void performInsertCard(String question, String answer, long categoryId) {
        executor.execute(() -> {
            long newId = dbHelper.insertCard(question, answer, categoryId);
            mainHandler.post(() -> {
                if (newId != -1) {
                    Toast.makeText(this, R.string.msg_card_added, Toast.LENGTH_SHORT).show();
                    loadCardsFromDatabase(false);
                }
            });
        });
    }

    private void performUpdateCard(long id, String question, String answer, long categoryId) {
        executor.execute(() -> {
            int rowsAffected = dbHelper.updateCard(id, question, answer, categoryId);
            mainHandler.post(() -> {
                if (rowsAffected > 0) {
                    Toast.makeText(this, R.string.msg_card_updated, Toast.LENGTH_SHORT).show();
                    loadCardsFromDatabase(true);
                }
            });
        });
    }

    private void showDeleteConfirmationDialog(Flashcard cardToDelete) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_dialog_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> performDeleteCard(cardToDelete.getId()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performDeleteCard(long id) {
        executor.execute(() -> {
            int rowsDeleted = dbHelper.deleteCard(id);
            mainHandler.post(() -> {
                if (rowsDeleted > 0) {
                    Toast.makeText(this, R.string.msg_card_deleted, Toast.LENGTH_SHORT).show();
                    loadCardsFromDatabase(true);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
