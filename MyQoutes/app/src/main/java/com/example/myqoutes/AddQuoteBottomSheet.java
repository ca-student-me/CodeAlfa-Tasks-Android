package com.example.myqoutes;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddQuoteBottomSheet extends BottomSheetDialogFragment {

    public interface OnQuoteSaveListener {
        void onQuoteSaved(Quote quote);
    }

    private OnQuoteSaveListener listener;
    private TextInputLayout tilQuoteText;
    private TextInputEditText etQuoteText;
    private TextInputEditText etAuthorName;

    public static AddQuoteBottomSheet newInstance() {
        return new AddQuoteBottomSheet();
    }

    public void setOnQuoteSaveListener(OnQuoteSaveListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_quote, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilQuoteText = view.findViewById(R.id.til_quote_text);
        etQuoteText = view.findViewById(R.id.et_quote_text);
        etAuthorName = view.findViewById(R.id.et_author_name);
        Button btnSaveQuote = view.findViewById(R.id.btn_save_quote);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime() | WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), imeInsets.bottom + 16);
            return windowInsets;
        });

        btnSaveQuote.setOnClickListener(v -> saveQuote());
    }

    private void saveQuote() {
        if (etQuoteText.getText() == null) return;

        String quoteText = etQuoteText.getText().toString().trim();
        String authorName = etAuthorName.getText() != null ? etAuthorName.getText().toString().trim() : "";

        if (TextUtils.isEmpty(quoteText)) {
            tilQuoteText.setError(getString(R.string.quote_required_error));
            return;
        } else {
            tilQuoteText.setError(null);
        }

        if (TextUtils.isEmpty(authorName)) {
            authorName = "Anonymous";
        }

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        long newId = dbHelper.insertQuote(quoteText, authorName, true);

        if (newId != -1) {
            Quote newQuote = new Quote((int) newId, quoteText, authorName, true);
            if (listener != null) {
                listener.onQuoteSaved(newQuote);
            }
            dismiss();
        }
    }
}
