package com.example.myqoutes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "quotes.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_QUOTES = "quotes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_QUOTE_TEXT = "quote_text";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_IS_CUSTOM = "is_custom";

    private final Context appContext;
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_QUOTES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_QUOTE_TEXT + " TEXT NOT NULL, " +
                COLUMN_AUTHOR + " TEXT NOT NULL, " +
                COLUMN_IS_CUSTOM + " INTEGER DEFAULT 0)";
        db.execSQL(createTableQuery);

        seedPresetQuotes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUOTES);
        onCreate(db);
    }

    private void seedPresetQuotes(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            InputStream is = appContext.getAssets().open("preset_quotes.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            int readBytes = is.read(buffer);
            is.close();

            if (readBytes > 0) {
                String jsonStr = new String(buffer, StandardCharsets.UTF_8);
                JSONArray jsonArray = new JSONArray(jsonStr);

                for (int idx = 0; idx < jsonArray.length(); idx++) {
                    JSONObject obj = jsonArray.getJSONObject(idx);
                    String text = obj.getString("text");
                    String author = obj.optString("author", "Unknown");

                    ContentValues values = new ContentValues();
                    values.put(COLUMN_QUOTE_TEXT, text);
                    values.put(COLUMN_AUTHOR, author);
                    values.put(COLUMN_IS_CUSTOM, 0);

                    db.insert(TABLE_QUOTES, null, values);
                }
                db.setTransactionSuccessful();
                Log.d(TAG, "Successfully seeded " + jsonArray.length() + " quotes into database.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error seeding preset quotes", e);
        } finally {
            db.endTransaction();
        }
    }

    public long insertQuote(String text, String author, boolean isCustom) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUOTE_TEXT, text);
        values.put(COLUMN_AUTHOR, (author == null || author.trim().isEmpty()) ? "Anonymous" : author.trim());
        values.put(COLUMN_IS_CUSTOM, isCustom ? 1 : 0);

        return db.insert(TABLE_QUOTES, null, values);
    }

    public Quote getRandomQuote(int excludeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Quote quote = null;

        try {
            if (excludeId > 0) {
                String query = "SELECT * FROM " + TABLE_QUOTES + " WHERE " + COLUMN_ID + " != ? ORDER BY RANDOM() LIMIT 1";
                cursor = db.rawQuery(query, new String[]{String.valueOf(excludeId)});
            }

            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                String fallbackQuery = "SELECT * FROM " + TABLE_QUOTES + " ORDER BY RANDOM() LIMIT 1";
                cursor = db.rawQuery(fallbackQuery, null);
            }

            if (cursor.moveToFirst()) {
                quote = cursorToQuote(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching random quote", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return quote;
    }

    public Quote getQuoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Quote quote = null;

        try {
            String query = "SELECT * FROM " + TABLE_QUOTES + " WHERE " + COLUMN_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
            if (cursor != null && cursor.moveToFirst()) {
                quote = cursorToQuote(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching quote by ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return quote;
    }

    public int getTotalQuotesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_QUOTES, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total quotes count", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public int getUserQuotesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_QUOTES + " WHERE " + COLUMN_IS_CUSTOM + " = 1", null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user quotes count", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public int getPresetQuotesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_QUOTES + " WHERE " + COLUMN_IS_CUSTOM + " = 0", null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting preset quotes count", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    private Quote cursorToQuote(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUOTE_TEXT));
        String author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR));
        boolean isCustom = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_CUSTOM)) == 1;

        return new Quote(id, text, author, isCustom);
    }
}
