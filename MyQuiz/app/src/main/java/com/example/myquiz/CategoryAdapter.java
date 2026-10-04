package com.example.myquiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryChangeListener {
        void onToggleEnabled(Category category, boolean isEnabled);
        void onEditCategory(Category category);
        void onDeleteCategory(Category category);
    }

    private final List<Category> categories = new ArrayList<>();
    private final OnCategoryChangeListener listener;

    public CategoryAdapter(OnCategoryChangeListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> newCategories) {
        categories.clear();
        if (newCategories != null) {
            categories.addAll(newCategories);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvCategoryName;
        private final TextView tvCategoryCount;
        private final ImageButton btnEditCategory;
        private final ImageButton btnDeleteCategory;
        private final SwitchMaterial swCategoryEnabled;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryCount = itemView.findViewById(R.id.tvCategoryCount);
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
            btnDeleteCategory = itemView.findViewById(R.id.btnDeleteCategory);
            swCategoryEnabled = itemView.findViewById(R.id.swCategoryEnabled);
        }

        public void bind(Category category, OnCategoryChangeListener listener) {
            tvCategoryName.setText(category.getName());
            tvCategoryCount.setText(String.format(Locale.getDefault(), "%d Flashcard%s", category.getCardCount(), category.getCardCount() == 1 ? "" : "s"));

            // Clear listener before setting checked state to avoid unwanted callback loop
            swCategoryEnabled.setOnCheckedChangeListener(null);
            swCategoryEnabled.setChecked(category.isEnabled());

            swCategoryEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                category.setEnabled(isChecked);
                if (listener != null) {
                    listener.onToggleEnabled(category, isChecked);
                }
            });

            btnEditCategory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditCategory(category);
                }
            });

            btnDeleteCategory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteCategory(category);
                }
            });
        }
    }
}
