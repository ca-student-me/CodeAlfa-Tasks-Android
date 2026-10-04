# My Health 🩺🏃‍♂️🥗

> **"Don't Compromise Your Health . . ."**  
> *Version: Sep 1.2.0*

---

## 📱 Project Overview
**My Health** is a robust, offline-first native Android health and fitness tracking application developed in Java using Material Design 3. It provides a complete suite of tools to monitor daily physical activity, hydration, nutrition, workouts, and vital medical metrics—all while operating seamlessly 24/7 in the background across all Android API levels (API 24 to 37+).

---

## 🌟 Key Features & Architecture

### 1. User Profile & SI/Imperial Unit Support
- **Profile Management**: Setup and edit personal metrics including Age, Gender, Height, Current/Target Weights, and Fitness Goals.
- **BMI Calculator**: Interactive Body Mass Index (BMI) card with educational modal explaining optimal weight ranges.
- **Unit Conversion**: Seamlessly switch between Metric (SI: cm / kg) and Imperial (in / lbs) measurement systems from Settings.

### 2. 24/7 Background Step Counter Service
- **Continuous Tracking**: Runs as a persistent foreground service (`StepCounterService`) utilizing hardware `Sensor.TYPE_STEP_COUNTER` with calibrated balancing.
- **Service Resurrection**: Implements `onTaskRemoved` with `AlarmManager` wakeup to survive app swipe-aways and force-kills.
- **Foreground vs. Background**: Displays live step counts in the status bar and lock screen when the app is active, and silently records every step to SQLite when closed.

### 3. Glassmorphic Home Screen App Widget
- **Transparent Design**: Custom widget (`HealthWidgetProvider`) featuring a sleek glassmorphic aesthetic.
- **Live Sync**: Displays real-time device clock, customized notes, and live daily steps, water, and calorie summaries.

### 4. Comprehensive Health & Workout Logging
- **Water Hydration**: Quick-log water intake with customizable ml amounts and automated hydration reminders (every 1,000 steps).
- **Meal & Fast Food Logging**: Includes dropdown menus for Asian menus (Paratha, Biryani, Roti, Daal, Chai) and street/fast food items (Zinger Burger, Fries, Shawarma) with automatic calorie estimation.
- **Gym Workout Logger**: Track exercises (Bench Press, Squats, Deadlifts, Cardio, etc.) by duration in minutes with automatic calorie burn calculation.
- **Medical & Fitness Vitals**: Log Blood Pressure (with systolic/diastolic format validation), Blood Sugar, Push-ups, Pull-ups, and Running distance.

### 5. Energy Balance & Health Summary Table
- **Today's Health Summary Table**: Tabular view contrasting **Calories Taken (In)** against **Calories Burned (Out)** aggregated from steps, running, gym workouts, push-ups, and pull-ups.
- **Detailed Vitals Breakdown**: Itemized paragraph presentation for medical check-in metrics.

### 6. Automated & On-Demand Monthly PDF Reports
- **A4 Publication-Grade Reports**: Generated via `PdfReportGenerator` featuring true 2-column and 3-column professional table grid views.
- **Comprehensive Contents**: Includes User Profile summary, full-month daily averages, medical summaries, and a professional evaluation commentary.
- **Professional Footer**: Includes confidentiality disclaimers, page numbering, and developer attribution (*Syed Muhammad Sajawal Hussain*, *0328-0841432*).

### 7. Centralized Settings Hub & Background Protection
- **Reminders & Schedule**: Exact alarms (`AlarmManager`) for breakfast, lunch, and dinner notifications.
- **Smart App Restrictions**: Direct option in Settings to whitelist the app from OEM battery optimizations.
- **Data Management**: Secure session management and local SQLite database upgrade/clear utilities.

---

## 🛠️ Technical Stack & Architecture
- **Language**: Java (JDK 11)
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 37 (Android 14/15+)
- **UI Toolkit**: Material Design 3 XML Layouts with edge-to-edge window insets & soft keyboard resize handling (`adjustResize`).
- **Persistence**: SQLite (`DatabaseHelper`) with multi-table schema (`user_profile`, `daily_logs`, `medical_reports`).
- **State Management**: `SharedPrefManager` for session states, units, and custom notes.

---

## 👨‍💻 Developer Information
* **Developer**: Syed Muhammad Sajawal Hussain
* **Contact**: 0328-0841432
* **Copyright**: © 2024–2025 My Health App. All rights reserved.
