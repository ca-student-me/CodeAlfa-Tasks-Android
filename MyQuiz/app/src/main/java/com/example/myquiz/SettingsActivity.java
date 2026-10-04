package com.example.myquiz;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Settings Activity managing Quiz Categories, Category Toggle Filtering,
 * and About App / About Developer Information Sections.
 */
public class SettingsActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryChangeListener {

    private DatabaseHelper dbHelper;
    private CategoryAdapter adapter;
    private final List<Category> categories = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private MaterialToolbar toolbarSettings;
    private MaterialButton btnSettingsAddCategory;
    private RecyclerView rvCategories;
    private LinearLayout layoutEmptyCategories;

    private MaterialCardView cardAboutApp;
    private MaterialCardView cardAboutDev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enforce Light Theme globally
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        loadCategoriesFromDatabase();
    }

    private void initViews() {
        toolbarSettings = findViewById(R.id.toolbarSettings);
        btnSettingsAddCategory = findViewById(R.id.btnSettingsAddCategory);
        rvCategories = findViewById(R.id.rvCategories);
        layoutEmptyCategories = findViewById(R.id.layoutEmptyCategories);

        cardAboutApp = findViewById(R.id.cardAboutApp);
        cardAboutDev = findViewById(R.id.cardAboutDev);
    }

    private void setupToolbar() {
        toolbarSettings.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(this);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSettingsAddCategory.setOnClickListener(v -> showAddEditCategoryDialog(null));
        cardAboutApp.setOnClickListener(v -> showAboutAppDialog());
        cardAboutDev.setOnClickListener(v -> showAboutDeveloperDialog());
    }

    private void loadCategoriesFromDatabase() {
        executor.execute(() -> {
            List<Category> list = dbHelper.getAllCategories();
            mainHandler.post(() -> {
                categories.clear();
                categories.addAll(list);
                adapter.setCategories(categories);

                if (categories.isEmpty()) {
                    layoutEmptyCategories.setVisibility(View.VISIBLE);
                    rvCategories.setVisibility(View.GONE);
                } else {
                    layoutEmptyCategories.setVisibility(View.GONE);
                    rvCategories.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override
    public void onToggleEnabled(Category category, boolean isEnabled) {
        executor.execute(() -> dbHelper.updateCategoryEnabled(category.getId(), isEnabled));
    }

    @Override
    public void onEditCategory(Category category) {
        showAddEditCategoryDialog(category);
    }

    @Override
    public void onDeleteCategory(Category category) {
        showDeleteCategoryDialog(category);
    }

    private void showAboutAppDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(Html.fromHtml(getString(R.string.about_message), Html.FROM_HTML_MODE_LEGACY))
                .setIcon(R.drawable.ic_info)
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void showAboutDeveloperDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about_dev_title)
                .setMessage(Html.fromHtml(getString(R.string.about_dev_message), Html.FROM_HTML_MODE_LEGACY))
                .setIcon(R.drawable.ic_developer)
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void showAddEditCategoryDialog(@Nullable Category categoryToEdit) {
        boolean isEditing = categoryToEdit != null;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_category, null);
        TextInputLayout tilCategoryName = dialogView.findViewById(R.id.tilCategoryName);
        TextInputEditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);

        if (isEditing) {
            etCategoryName.setText(categoryToEdit.getName());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEditing ? R.string.edit_category_title : R.string.add_category_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etCategoryName.getText() != null ? etCategoryName.getText().toString().trim() : "";

            if (TextUtils.isEmpty(name)) {
                tilCategoryName.setError(getString(R.string.error_empty_category));
            } else {
                tilCategoryName.setError(null);
                if (isEditing) {
                    performUpdateCategory(categoryToEdit.getId(), name);
                } else {
                    performInsertCategory(name);
                }
                dialog.dismiss();
            }
        });
    }

    private void performInsertCategory(String name) {
        executor.execute(() -> {
            long newId = dbHelper.insertCategory(name, true);
            mainHandler.post(() -> {
                if (newId != -1) {
                    Toast.makeText(this, R.string.msg_category_added, Toast.LENGTH_SHORT).show();
                    loadCategoriesFromDatabase();
                }
            });
        });
    }

    private void performUpdateCategory(long id, String newName) {
        executor.execute(() -> {
            int rows = dbHelper.updateCategoryName(id, newName);
            mainHandler.post(() -> {
                if (rows > 0) {
                    Toast.makeText(this, R.string.msg_category_updated, Toast.LENGTH_SHORT).show();
                    loadCategoriesFromDatabase();
                }
            });
        });
    }

    private void showDeleteCategoryDialog(Category categoryToDelete) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_category_title)
                .setMessage(R.string.delete_category_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> performDeleteCategory(categoryToDelete.getId()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performDeleteCategory(long categoryId) {
        executor.execute(() -> {
            int rows = dbHelper.deleteCategory(categoryId);
            mainHandler.post(() -> {
                if (rows > 0) {
                    Toast.makeText(this, R.string.msg_category_deleted, Toast.LENGTH_SHORT).show();
                    loadCategoriesFromDatabase();
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
