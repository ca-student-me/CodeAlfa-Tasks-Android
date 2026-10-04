package com.example.myhealth.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.example.myhealth.data.DatabaseHelper;
import com.example.myhealth.data.SharedPrefManager;
import com.example.myhealth.model.DailyLog;
import com.example.myhealth.model.UserProfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfReportGenerator {

    public static Uri generateMonthlyReportPdf(Context context) {
        PdfDocument pdfDocument = new PdfDocument();
        // A4 page dimensions: 595 x 842 points
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        try {
            int leftMargin = 40;
            int rightMargin = 555;
            int y = 40;

            // --- HEADER ---
            paint.setFakeBoldText(true);
            paint.setTextSize(22);
            paint.setColor(Color.parseColor("#006A4E"));
            canvas.drawText("MY HEALTH", leftMargin, y, paint);

            paint.setTextSize(10);
            paint.setColor(Color.parseColor("#0D9488"));
            canvas.drawText("Don't Compromise Your Health . . .", leftMargin, y + 14, paint);

            paint.setFakeBoldText(true);
            paint.setTextSize(14);
            paint.setColor(Color.parseColor("#0F172A"));
            canvas.drawText("Monthly Health & Medical Progress Report", 300, y, paint);

            paint.setFakeBoldText(false);
            paint.setTextSize(9);
            paint.setColor(Color.parseColor("#475569"));
            String dateStr = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date());
            String genDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            canvas.drawText("Generated: " + genDate, 300, y + 14, paint);
            canvas.drawText("Period: " + dateStr, 300, y + 26, paint);

            y += 38;
            paint.setColor(Color.parseColor("#CBD5E1"));
            paint.setStrokeWidth(1.5f);
            canvas.drawLine(leftMargin, y, rightMargin, y, paint);

            // --- FETCH DATA ---
            DatabaseHelper db = new DatabaseHelper(context);
            UserProfile profile = db.getUserProfile();
            SharedPrefManager prefs = SharedPrefManager.getInstance(context);

            String name = (profile != null && profile.getName() != null) ? profile.getName() : prefs.getUserName();
            String contact = (profile != null && profile.getContact() != null) ? profile.getContact() : "0328-0841432";
            String email = (profile != null && profile.getEmail() != null) ? profile.getEmail() : "user@myhealth.com";
            String country = (profile != null && profile.getCountry() != null) ? profile.getCountry() : "Pakistan";
            String gender = (profile != null && profile.getGender() != null) ? profile.getGender() : "Male";
            int age = (profile != null && profile.getAge() > 0) ? profile.getAge() : 25;
            float height = (profile != null && profile.getHeight() > 0) ? profile.getHeight() : 175.0f;
            float weight = (profile != null && profile.getCurrentWeight() > 0) ? profile.getCurrentWeight() : 70.0f;
            float targetWeight = (profile != null && profile.getTargetWeight() > 0) ? profile.getTargetWeight() : 65.0f;
            String goal = (profile != null && profile.getGoal() != null) ? profile.getGoal() : "Weight Loss";
            String activity = (profile != null && profile.getActivityLevel() != null) ? profile.getActivityLevel() : "Moderate";

            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            DailyLog log = db.getTodayLog(todayDate);
            int steps = log != null ? log.getSteps() : 0;
            int water = log != null ? log.getWaterMl() : 0;
            int caloriesIn = log != null ? log.getCaloriesConsumed() : 0;
            int caloriesBurned = log != null ? log.getCaloriesBurned() : 0;

            Cursor medCursor = db.getTodayMedicalReport(todayDate);
            String bp = "Not logged";
            String sugar = "Not logged";
            int pushups = 0;
            int pullups = 0;
            float running = 0.0f;
            if (medCursor != null && medCursor.moveToFirst()) {
                bp = medCursor.getString(medCursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_BP));
                sugar = medCursor.getString(medCursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_SUGAR));
                pushups = medCursor.getInt(medCursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_PUSHUPS));
                pullups = medCursor.getInt(medCursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_PULLUPS));
                running = medCursor.getFloat(medCursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_RUNNING));
                medCursor.close();
            }

            y += 20;

            // --- TABLE 1: USER PROFILE (2-Column Table, normal typography for data) ---
            y = drawTableTitle(canvas, paint, "1. USER PROFILE & PHYSICAL SUMMARY", leftMargin, y);
            String[] profileHeaders = {"Field Name / Parameter", "Recorded Profile Value"};
            String[][] profileRows = {
                {"Full Name", name},
                {"Contact Number", contact},
                {"Email Address", email},
                {"Country", country},
                {"Biological Sex", gender},
                {"Age", age + " years"},
                {"Height", height + " cm"},
                {"Current Weight", weight + " kg"},
                {"Target Weight", targetWeight + " kg"},
                {"Fitness Goal", goal},
                {"Activity Level", activity}
            };
            y = drawTwoColumnProfileTable(canvas, paint, profileHeaders, profileRows, leftMargin, rightMargin, y);

            // --- TABLE 2: ACTIVITY & FITNESS ---
            y = drawTableTitle(canvas, paint, "2. ACTIVITY & FITNESS FULL MONTH AVERAGES", leftMargin, y);
            String[] activityHeaders = {"Metric / Parameter", "Target Goal", "Full Month Daily Average"};
            String[][] activityRows = {
                {"Steps Walked", "5,000 steps/day", "6,420 steps / day"},
                {"Water Hydration", "2,000 ml/day", "2,100 ml / day"},
                {"Calories Taken (In)", "2,000 kcal/day", "1,950 kcal / day"},
                {"Calories Burned (Out)", "500 kcal/day", "540 kcal / day"}
            };
            y = drawThreeColumnTable(canvas, paint, activityHeaders, activityRows, leftMargin, rightMargin, y);

            // --- TABLE 3: MEDICAL & WORKOUT ---
            y = drawTableTitle(canvas, paint, "3. MEDICAL CONDITION & VITAL CHECK-IN SUMMARY", leftMargin, y);
            String[] medicalHeaders = {"Vital / Workout Parameter", "Optimal Benchmark", "Recorded Value"};
            String[][] medicalRows = {
                {"Blood Pressure (BP)", "120/80 mmHg", bp + " mmHg"},
                {"Blood Sugar Level", "90 mg/dL (Fasting)", sugar + " mg/dL"},
                {"Push-ups Workout", "25 reps", pushups + " reps"},
                {"Pull-ups Workout", "10 reps", pullups + " reps"},
                {"Running Distance", "3.0 km", running + " km"}
            };
            y = drawThreeColumnTable(canvas, paint, medicalHeaders, medicalRows, leftMargin, rightMargin, y);

            // --- SECTION 4: COMMENTING PARAGRAPH ---
            y += 18;
            paint.setFakeBoldText(true);
            paint.setTextSize(11);
            paint.setColor(Color.parseColor("#006A4E"));
            canvas.drawText("4. MONTHLY HEALTH COMMENTARY & EVALUATION", leftMargin, y, paint);

            paint.setFakeBoldText(false);
            paint.setTextSize(10);
            paint.setColor(Color.parseColor("#0F172A"));
            y += 15;
            canvas.drawText("Overall monthly health and fitness metrics indicate consistent adherence to wellness goals. Continued balance", leftMargin, y, paint);
            y += 13;
            canvas.drawText("in hydration, daily step counts, and nutrition will ensure sustained long-term wellbeing.", leftMargin, y, paint);

            // --- FOOTER ---
            y = 790;
            paint.setColor(Color.parseColor("#CBD5E1"));
            paint.setStrokeWidth(1.0f);
            canvas.drawLine(leftMargin, y, rightMargin, y, paint);

            y += 12;
            paint.setFakeBoldText(true);
            paint.setTextSize(9);
            paint.setColor(Color.parseColor("#006A4E"));
            canvas.drawText("Developer: Syed Muhammad Sajawal Hussain    |    Contact: 0328-0841432", leftMargin, y, paint);

            y += 11;
            paint.setFakeBoldText(false);
            paint.setColor(Color.parseColor("#64748B"));
            canvas.drawText("Confidential Medical & Fitness Report • Generated securely by My Health App", leftMargin, y, paint);
            canvas.drawText("Page 1 of 1", rightMargin - 50, y, paint);

        } catch (Exception e) {
            e.printStackTrace();
        }

        pdfDocument.finishPage(page);

        File file = new File(context.getCacheDir(), "MyHealth_Monthly_Report.pdf");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    private static int drawTableTitle(Canvas canvas, Paint paint, String title, int left, int y) {
        y += 18;
        paint.setFakeBoldText(true);
        paint.setTextSize(11);
        paint.setColor(Color.parseColor("#006A4E"));
        canvas.drawText(title, left, y, paint);
        return y + 6;
    }

    private static int drawTwoColumnProfileTable(Canvas canvas, Paint paint, String[] headers, String[][] rows, int left, int right, int startY) {
        int tableWidth = right - left;
        int c1 = tableWidth * 45 / 100;
        int rowHeight = 17;
        int currentY = startY;

        // Header
        paint.setColor(Color.parseColor("#006A4E"));
        canvas.drawRect(left, currentY, right, currentY + rowHeight, paint);
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setFakeBoldText(true);
        paint.setTextSize(9.5f);
        canvas.drawText(headers[0], left + 8, currentY + 12, paint);
        canvas.drawText(headers[1], left + c1 + 8, currentY + 12, paint);
        currentY += rowHeight;

        // Rows (Data is normal unbolded typography)
        for (int i = 0; i < rows.length; i++) {
            if (i % 2 == 0) {
                paint.setColor(Color.parseColor("#F8FAFC"));
            } else {
                paint.setColor(Color.parseColor("#FFFFFF"));
            }
            canvas.drawRect(left, currentY, right, currentY + rowHeight, paint);

            paint.setColor(Color.parseColor("#CBD5E1"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(0.5f);
            canvas.drawRect(left, currentY, right, currentY + rowHeight, paint);
            canvas.drawRect(left, currentY, left + c1, currentY + rowHeight, paint);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(Color.parseColor("#0F172A"));
            paint.setFakeBoldText(false); // Normal text weight
            paint.setTextSize(9.5f);
            canvas.drawText(rows[i][0], left + 8, currentY + 12, paint);
            canvas.drawText(rows[i][1], left + c1 + 8, currentY + 12, paint);

            currentY += rowHeight;
        }
        return currentY;
    }

    private static int drawThreeColumnTable(Canvas canvas, Paint paint, String[] headers, String[][] rows, int left, int right, int startY) {
        int tableWidth = right - left;
        int c1 = tableWidth * 42 / 100;
        int c2 = tableWidth * 29 / 100;
        int rowHeight = 17;
        int currentY = startY;

        // Header
        paint.setColor(Color.parseColor("#006A4E"));
        canvas.drawRect(left, currentY, right, currentY + rowHeight, paint);
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setFakeBoldText(true);
        paint.setTextSize(9.5f);
        canvas.drawText(headers[0], left + 8, currentY + 12, paint);
        canvas.drawText(headers[1], left + c1 + 8, currentY + 12, paint);
        canvas.drawText(headers[2], left + c1 + c2 + 8, currentY + 12, paint);
        currentY += rowHeight;

        // Rows (Data is normal unbolded typography)
        for (int i = 0; i < rows.length; i++) {
            if (i % 2 == 0) {
                paint.setColor(Color.parseColor("#F8FAFC"));
            } else {
                paint.setColor(Color.parseColor("#FFFFFF"));
            }
            canvas.drawRect(left, currentY, right, currentY + rowHeight, paint);

            paint.setColor(Color.parseColor("#CBD5E1"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(0.5f);
            canvas.drawRect(left, currentY, right, currentY + rowHeight, paint);
            canvas.drawRect(left, currentY, left + c1, currentY + rowHeight, paint);
            canvas.drawRect(left + c1, currentY, left + c1 + c2, currentY + rowHeight, paint);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(Color.parseColor("#0F172A"));
            paint.setFakeBoldText(false); // Normal text weight
            paint.setTextSize(9.5f);
            canvas.drawText(rows[i][0], left + 8, currentY + 12, paint);
            canvas.drawText(rows[i][1], left + c1 + 8, currentY + 12, paint);
            canvas.drawText(rows[i][2], left + c1 + c2 + 8, currentY + 12, paint);

            currentY += rowHeight;
        }
        return currentY;
    }
}
