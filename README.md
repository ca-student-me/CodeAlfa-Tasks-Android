
# CodeAlfa Android Development Internship Projects

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](#)
[![Language](https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white)](#)
[![Database](https://img.shields.io/badge/Database-SQLite-003B57?logo=sqlite&logoColor=white)](#)
[![Architecture](https://img.shields.io/badge/Architecture-Native%20MVC-blue)](#)
[![Network](https://img.shields.io/badge/Mode-100%25%20Offline-success)](#)

A centralized repository containing the three native Android applications engineered during my software development internship at **CodeAlfa**. Each application focuses on a distinct use case—active study recall, daily inspiration, and personal health tracking—implemented using **pure Java**, **Android XML layouts**, and **direct SQLite persistence** with no external network or cloud dependencies.

---

## 📌 Projects At A Glance

| Project | Domain | Core Capabilities | Persistence & System APIs |
|---|---|---|---|
| **My Quiz** | EdTech / Study Utility | Flashcard UI (flip card front/back), 10+ starter categories, 100+ starter cards, dynamic category & card additions | SQLite (Tables: `categories`, `flashcards`) |
| **My Quotes** | Daily Motivation | 400+ pre-seeded quotes with author attributions, random quote on launch, daily system notifications, custom quote entry | SQLite (`quotes`), `AlarmManager`, `NotificationCompat` |
| **My Health** | Health & Fitness | Profile management, BMI calculator, step estimation, meal/water/exercise logs, scheduled reminders, PDF report export | SQLite (`daily_logs`), `PdfDocument`, `SensorManager` |

---

## 📱 Detailed Project Breakdown

### 1. My Quiz — Interactive Flashcard Deck
A revision and self-testing utility designed around active recall and structured deck management.

- **Flashcard Interaction:** Card-like container featuring a two-sided view—question on the front and answer on the reverse—with smooth flipping animations.
- **Card Navigation:** Dedicated **Next** and **Previous** navigation controls to step through decks sequentially.
- **Hierarchical Categorization:** Pre-populated with **10+ distinct categories** covering diverse subjects.
- **Dynamic Content Management:**
  - Create and register new custom categories at runtime.
  - Dedicated **Add** button to insert custom question-and-answer pairs into any category.
  - Pre-seeded with **100+ ready-to-use quizzes** (minimum 10 flashcards per pre-built category).
- **Settings Activity:** Preference controls to manage study preferences, card viewing modes, and deck reset options.

---

### 2. My Quotes — Daily Inspiration Engine
A local daily quote delivery and personal inspiration hub with background alerts and library management.

- **Offline Quote Repository:** Pre-installed with **400+ curated quotes** across classic and modern authors stored directly in SQLite.
- **Fresh Launch Delivery:** Automatically selects and displays a new quote upon every application launch.
- **Scheduled Daily Notifications:** Built-in Android `AlarmManager` and broadcast receiver to push a scheduled daily motivational quote notification without internet access.
- **Custom Quote Creation:** Dedicated **Add Quote** form allowing users to add custom quotes and author names directly to the database.
- **Settings Activity:** Settings screen to toggle notifications on/off, set custom notification times, and adjust visual themes.

---

### 3. My Health — Fitness Tracker & Health Reports
A comprehensive personal wellness manager featuring multi-habit logging, scheduled routine reminders, and local export capabilities.

- **User Profile Management:** View and edit personal physical metrics (age, gender, height, weight, and fitness targets).
- **Diagnostic Indices & Tracking:**
  - **BMI Calculator:** Computes Body Mass Index and maps results to standard health classifications (underweight, normal, overweight, obese).
  - **Estimated Step Counter:** Motion-based tracking module to log daily walking steps against targets.
- **Daily Multi-Habit Logging:**
  - **Hydration Tracker:** Log water consumption against daily fluid intake goals.
  - **Meal Logging:** Record daily dietary meals and nutritional entries.
  - **Exercise Log:** Track physical activity types, workout duration, and routine frequency.
- **Smart Notification Reminders:** Scheduled local system alerts to keep users consistent:
  - Timely reminders to take meals.
  - Periodic reminders to drink water.
  - Goal alerts based on target daily step counts.
- **PDF Report Generation:** Native export engine built on `android.graphics.pdf.PdfDocument` that generates formatted health summary reports directly on device storage for offline sharing or printing.
- **Settings Activity:** Manage daily reminder intervals, change measurement units (metric/imperial), and adjust logging thresholds.

---

## 🛠️ Shared Technical Architecture

All three applications were built strictly following standard native Android engineering patterns:

- **Programming Language:** Java
- **UI & Layouts:** XML (`ConstraintLayout`, `MaterialCardView`, Custom Vector Drawables, Canvas drawing)
- **Local Persistence:** Direct SQLite Database via `SQLiteOpenHelper`
  - Normalized schemas with primary/foreign keys
  - Transaction-safe CRUD queries
  - Embedded raw database seeding upon creation
- **State & Preferences:** `SharedPreferences` for user session preferences and alarm configurations
- **Background Automation:** `AlarmManager` + `BroadcastReceiver` + `NotificationCompat.Builder` (with Android 8.0+ `NotificationChannel` support)
- **Document Rendering:** Native `android.graphics.pdf.PdfDocument` (canvas-based page construction and file I/O)
- **Sensor Integration:** Android `SensorManager` (`TYPE_STEP_DETECTOR` / `TYPE_ACCELEROMETER`)
- **Compatibility:** Android API Level 24 (Android 7.0 Nougat) through modern Android releases
- **100% Offline Support:** Self-contained data access layer with zero cloud, API, or internet dependencies

---

## 📁 Repository Directory Structure

```text
CodeAlfa-Tasks-Android/
├── MyQuiz/
│   └── app/src/main/
│       ├── java/.../myquiz/
│       │   ├── database/         # SQLiteOpenHelper & Flashcard DAOs
│       │   ├── models/           # Category & Flashcard models
│       │   └── ui/               # FlashcardActivity, AddCardDialog, SettingsActivity
│       └── res/layout/           # card_front.xml, card_back.xml, activity_quiz.xml
├── MyQuotes/
│   └── app/src/main/
│       ├── java/.../myquotes/
│       │   ├── database/         # DatabaseHelper (pre-seeded with 400+ quotes)
│       │   ├── receiver/         # DailyQuoteNotificationReceiver
│       │   └── ui/               # MainActivity, AddQuoteActivity, SettingsActivity
│       └── res/layout/           # activity_main.xml, activity_add_quote.xml
├── MyHealth/
│   └── app/src/main/
│       ├── java/.../myhealth/
│       │   ├── database/         # HealthDbHelper (meal, water, exercise, profile logs)
│       │   ├── pdf/              # PdfReportGenerator (PdfDocument implementation)
│       │   ├── receiver/         # Meal, water, and step reminder alarms
│       │   └── ui/               # DashboardActivity, ProfileActivity, BmiActivity
│       └── res/layout/           # activity_dashboard.xml, activity_bmi.xml
└── README.md

```

---

## 🚀 Getting Started

1. **Clone the repository:**
Clone the repository: [CodeAlfa-Tasks-Android](https://github.com/ca-student-me/CodeAlfa-Tasks-Android.git)

```bash
git clone https://github.com/ca-student-me/CodeAlfa-Tasks-Android.git

```


2. **Open in Android Studio:**
* Launch **Android Studio**.
* Click **Open** and select the root directory or individual task folder (`MyQuiz`, `MyQuotes`, or `MyHealth`).


3. **Build & Run:**
* Allow Gradle to sync dependencies and project files.
* Select your target device or emulator (Android 7.0 / API 24+).
* Press **Run** (`Shift + F10`).



---

## 👨‍💻 Author

**Syed Muhammad Sajawal Hussain**

* **GitHub:** [@ca-student-me](https://www.google.com/search?q=https://github.com/ca-student-me)
* **Role:** Android Developer Intern at CodeAlfa

```


```
