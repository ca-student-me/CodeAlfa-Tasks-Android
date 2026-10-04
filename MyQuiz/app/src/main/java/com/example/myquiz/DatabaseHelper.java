package com.example.myquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper managing 10 pre-added CSS/Competitive exam categories with 10 quizzes per category (100 total pre-added flashcards),
 * database migrations, and thread-safe CRUD operations.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "flashcards_db";
    private static final int DATABASE_VERSION = 4;

    // Categories Table
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CAT_ID = "_id";
    public static final String COL_CAT_NAME = "name";
    public static final String COL_CAT_ENABLED = "is_enabled";

    // Flashcards Table
    public static final String TABLE_FLASHCARDS = "flashcards";
    public static final String COL_ID = "_id";
    public static final String COL_QUESTION = "question";
    public static final String COL_ANSWER = "answer";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_CATEGORY_ID = "category_id";

    // Create Statements
    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
            COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_CAT_NAME + " TEXT NOT NULL UNIQUE, " +
            COL_CAT_ENABLED + " INTEGER NOT NULL DEFAULT 1" +
            ");";

    private static final String CREATE_TABLE_FLASHCARDS = "CREATE TABLE " + TABLE_FLASHCARDS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_QUESTION + " TEXT NOT NULL, " +
            COL_ANSWER + " TEXT NOT NULL, " +
            COL_CREATED_AT + " INTEGER NOT NULL, " +
            COL_CATEGORY_ID + " INTEGER NOT NULL DEFAULT 1, " +
            "FOREIGN KEY(" + COL_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_CAT_ID + ") ON DELETE CASCADE" +
            ");";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_FLASHCARDS);
        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLASHCARDS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
            onCreate(db);
        }
    }

    /**
     * Seeds 10 categories with exactly 10 quizzes each (100 total pre-added flashcards) tailored for CSS & Competitive Exams.
     */
    private void seedInitialData(SQLiteDatabase db) {
        long catCS   = insertSampleCategory(db, "Computer Science", 1);
        long catPhy  = insertSampleCategory(db, "Physics", 1);
        long catGK   = insertSampleCategory(db, "General Knowledge", 1);
        long catMath = insertSampleCategory(db, "Mathematics & Ability", 1);
        long catChem = insertSampleCategory(db, "Chemistry", 1);
        long catCA   = insertSampleCategory(db, "Current & International Affairs", 1);
        long catGeo  = insertSampleCategory(db, "Geography & World History", 1);
        long catEng  = insertSampleCategory(db, "English & Grammar", 1);
        long catIsl  = insertSampleCategory(db, "Islamic Studies", 1);
        long catPak  = insertSampleCategory(db, "Pakistan Affairs", 1);

        long now = System.currentTimeMillis();

        // 1. Computer Science (10 Quizzes)
        insertSampleCard(db, "What is the primary function of an Operating System Kernel?", "Manages system resources, CPU scheduling, memory management, and hardware communication.", catCS, now++);
        insertSampleCard(db, "What is the time complexity of Binary Search?", "O(log n) where n is the number of elements in a sorted array.", catCS, now++);
        insertSampleCard(db, "What does TCP/IP stand for?", "Transmission Control Protocol / Internet Protocol.", catCS, now++);
        insertSampleCard(db, "What is the main difference between RAM and ROM?", "RAM is volatile temporary memory; ROM is non-volatile permanent read-only memory.", catCS, now++);
        insertSampleCard(db, "What is Object-Oriented Programming (OOP) Encapsulation?", "Bundling data and methods into a single unit and restricting direct access to object components.", catCS, now++);
        insertSampleCard(db, "What is a Primary Key in relational databases?", "A column or set of columns that uniquely identifies each row in a database table.", catCS, now++);
        insertSampleCard(db, "What is the length of an IPv6 address?", "128 bits (represented as eight groups of four hexadecimal digits).", catCS, now++);
        insertSampleCard(db, "What is the primary purpose of a Compiler?", "Translates high-level source code into machine code executable by the CPU.", catCS, now++);
        insertSampleCard(db, "What is a Deadlock in operating systems?", "A situation where two or more processes are unable to proceed because each is waiting for the other to release a resource.", catCS, now++);
        insertSampleCard(db, "What does HTTP status code 404 signify?", "Not Found (indicates that the requested resource could not be found on the server).", catCS, now++);

        // 2. Physics (10 Quizzes)
        insertSampleCard(db, "What is Newton's First Law of Motion?", "An object remains at rest or in uniform motion unless acted upon by an external force (Law of Inertia).", catPhy, now++);
        insertSampleCard(db, "What is the SI unit of Electrical Resistance?", "Ohm (Ω).", catPhy, now++);
        insertSampleCard(db, "What is the speed of light in a vacuum?", "Approximately 3 × 10⁸ meters per second (300,000 km/s).", catPhy, now++);
        insertSampleCard(db, "What is Bernoulli's Principle?", "An increase in the speed of a fluid occurs simultaneously with a decrease in static pressure.", catPhy, now++);
        insertSampleCard(db, "What is the SI unit of Force?", "Newton (N), equivalent to 1 kg·m/s².", catPhy, now++);
        insertSampleCard(db, "What is Total Internal Reflection?", "Complete reflection of a light ray back into an optically denser medium when the angle of incidence exceeds the critical angle.", catPhy, now++);
        insertSampleCard(db, "What is the approximate value of Planck's constant?", "6.626 × 10⁻³⁴ Joule-seconds (J·s).", catPhy, now++);
        insertSampleCard(db, "What does Coulomb's Law state?", "Electrostatic force between two charges is directly proportional to their product and inversely proportional to the square of the distance between them.", catPhy, now++);
        insertSampleCard(db, "What is the standard acceleration due to gravity on Earth?", "Approximately 9.81 m/s².", catPhy, now++);
        insertSampleCard(db, "What is a Semiconductor?", "A material with electrical conductivity between that of a conductor and an insulator (e.g., Silicon, Germanium).", catPhy, now++);

        // 3. General Knowledge (10 Quizzes)
        insertSampleCard(db, "Which organization is headquartered at the Peace Palace in The Hague?", "The International Court of Justice (ICJ).", catGK, now++);
        insertSampleCard(db, "What is the largest ocean on Earth by surface area?", "The Pacific Ocean.", catGK, now++);
        insertSampleCard(db, "Which country is known as the Land of the Rising Sun?", "Japan.", catGK, now++);
        insertSampleCard(db, "What is the official language of Brazil?", "Portuguese.", catGK, now++);
        insertSampleCard(db, "Which continent contains the highest number of sovereign countries?", "Africa (54 sovereign countries).", catGK, now++);
        insertSampleCard(db, "What is the deepest point in Earth's oceans?", "The Challenger Deep in the Mariana Trench (~10,994 meters).", catGK, now++);
        insertSampleCard(db, "Which international organization succeeded the League of Nations in 1945?", "The United Nations (UN).", catGK, now++);
        insertSampleCard(db, "What is the capital city of Australia?", "Canberra.", catGK, now++);
        insertSampleCard(db, "Which desert is the largest hot desert in the world?", "The Sahara Desert.", catGK, now++);
        insertSampleCard(db, "Which canal connects the Mediterranean Sea to the Red Sea?", "The Suez Canal.", catGK, now++);

        // 4. Mathematics & Ability (10 Quizzes)
        insertSampleCard(db, "What is the value of Pi (π) rounded to four decimal places?", "3.1416.", catMath, now++);
        insertSampleCard(db, "What is a Prime Number?", "A natural number greater than 1 that has no positive divisors other than 1 and itself.", catMath, now++);
        insertSampleCard(db, "What is the sum of interior angles in a triangle?", "180 degrees.", catMath, now++);
        insertSampleCard(db, "What is the quadratic formula for ax² + bx + c = 0?", "x = (-b ± √(b² - 4ac)) / (2a).", catMath, now++);
        insertSampleCard(db, "What is a Hypotenuse in geometry?", "The longest side of a right-angled triangle, opposite the right angle.", catMath, now++);
        insertSampleCard(db, "What is the derivative of sin(x) with respect to x?", "cos(x).", catMath, now++);
        insertSampleCard(db, "What is the median of a statistical dataset?", "The middle value when a dataset is ordered from least to greatest.", catMath, now++);
        insertSampleCard(db, "What is the factorial of 5 (5!)?", "120 (5 × 4 × 3 × 2 × 1).", catMath, now++);
        insertSampleCard(db, "What is the Pythagorean Theorem equation?", "a² + b² = c².", catMath, now++);
        insertSampleCard(db, "What is the formula for the area of a circle?", "Area = πr² (where r is the radius).", catMath, now++);

        // 5. Chemistry (10 Quizzes)
        insertSampleCard(db, "What is the chemical formula for Ozone?", "O3.", catChem, now++);
        insertSampleCard(db, "What is Avogadro's constant approximate value?", "6.022 × 10²³ particles per mole.", catChem, now++);
        insertSampleCard(db, "What is the pH value of pure distilled water at 25°C?", "7.0 (Neutral).", catChem, now++);
        insertSampleCard(db, "What is an Isotope?", "Atoms of the same element with the same atomic number but different numbers of neutrons.", catChem, now++);
        insertSampleCard(db, "What is the most abundant element in the universe?", "Hydrogen (~75% of elemental mass).", catChem, now++);
        insertSampleCard(db, "What is the chemical symbol for Gold?", "Au (derived from Latin: Aurum).", catChem, now++);
        insertSampleCard(db, "What is the atomic number of Carbon in the periodic table?", "6.", catChem, now++);
        insertSampleCard(db, "What is an Endothermic Reaction?", "A chemical reaction that absorbs thermal energy from its surroundings.", catChem, now++);
        insertSampleCard(db, "What is the chemical name of common table salt?", "Sodium Chloride (NaCl).", catChem, now++);
        insertSampleCard(db, "What is a Catalyst in chemistry?", "A substance that increases the rate of a chemical reaction without undergoing permanent chemical change itself.", catChem, now++);

        // 6. Current & International Affairs (10 Quizzes)
        insertSampleCard(db, "Where are the headquarters of the United Nations (UN) located?", "New York City, United States.", catCA, now++);
        insertSampleCard(db, "What is the main judicial organ of the European Union?", "The Court of Justice of the European Union (CJEU).", catCA, now++);
        insertSampleCard(db, "Which country hosted the COP28 UN Climate Summit?", "United Arab Emirates (Dubai, 2023).", catCA, now++);
        insertSampleCard(db, "What is the veto power in the UN Security Council?", "The power of the 5 permanent members (P5) to block any substantive draft resolution.", catCA, now++);
        insertSampleCard(db, "What does NATO stand for and when was it founded?", "North Atlantic Treaty Organization, founded in 1949.", catCA, now++);
        insertSampleCard(db, "What is the headquarters city of the International Monetary Fund (IMF)?", "Washington, D.C., United States.", catCA, now++);
        insertSampleCard(db, "Which organ of the UN is responsible for maintaining international peace and security?", "The UN Security Council (UNSC).", catCA, now++);
        insertSampleCard(db, "What is the BRICS grouping?", "An intergovernmental organization comprising Brazil, Russia, India, China, South Africa, Egypt, Ethiopia, Iran, and UAE.", catCA, now++);
        insertSampleCard(db, "What does ASEAN stand for?", "Association of Southeast Asian Nations.", catCA, now++);
        insertSampleCard(db, "In which year was the Universal Declaration of Human Rights adopted?", "1948 (10th December by UN General Assembly).", catCA, now++);

        // 7. Geography & World History (10 Quizzes)
        insertSampleCard(db, "Which strait connects the Black Sea to the Sea of Marmara?", "The Bosporus Strait.", catGeo, now++);
        insertSampleCard(db, "Which is the longest river in the world?", "The Nile River (~6,650 km).", catGeo, now++);
        insertSampleCard(db, "In which year did World War I begin and end?", "1914 to 1918.", catGeo, now++);
        insertSampleCard(db, "Which mountain peak is the highest on Earth above sea level?", "Mount Everest (8,848.86 meters).", catGeo, now++);
        insertSampleCard(db, "What was the Silk Road?", "An ancient network of Eurasian trade routes connecting East Asia with the Mediterranean.", catGeo, now++);
        insertSampleCard(db, "Which country has the longest coastline in the world?", "Canada (202,080 km).", catGeo, now++);
        insertSampleCard(db, "In which year did the French Revolution begin?", "1789.", catGeo, now++);
        insertSampleCard(db, "What is the capital city of Turkey?", "Ankara.", catGeo, now++);
        insertSampleCard(db, "Which mountain range is traditionally considered the boundary between Europe and Asia?", "The Ural Mountains.", catGeo, now++);
        insertSampleCard(db, "In which year did World War II end?", "1945.", catGeo, now++);

        // 8. English & Grammar (10 Quizzes)
        insertSampleCard(db, "What is an Oxford Comma?", "A comma placed immediately before the coordinating conjunction in a series of three or more items.", catEng, now++);
        insertSampleCard(db, "What is a Metaphor?", "A figure of speech that directly refers to one thing by mentioning another without using 'like' or 'as'.", catEng, now++);
        insertSampleCard(db, "What is an Active Voice sentence structure?", "A sentence where the subject performs the action expressed by the verb (e.g., 'The chef cooked dinner').", catEng, now++);
        insertSampleCard(db, "What is a Gerund in English grammar?", "A verb form ending in '-ing' that functions as a noun (e.g., 'Swimming is good exercise').", catEng, now++);
        insertSampleCard(db, "What is an antonym for the word 'Ebullient'?", "Depressed, gloomy, or reserved.", catEng, now++);
        insertSampleCard(db, "What is a Tautology in speech or writing?", "Saying the same thing twice in different words (e.g., 'free gift', 'added bonus').", catEng, now++);
        insertSampleCard(db, "What is an Onomatopoeia?", "A word that phonetically imitates or resembles the sound that it describes (e.g., 'buzz', 'hiss').", catEng, now++);
        insertSampleCard(db, "What is a Transitive Verb?", "A verb that requires a direct object to complete its meaning (e.g., 'She bought a book').", catEng, now++);
        insertSampleCard(db, "What is a synonym for 'Ephemeral'?", "Transient, fleeting, or short-lived.", catEng, now++);
        insertSampleCard(db, "What is the Subject-Verb Agreement rule?", "Singular subjects require singular verbs, and plural subjects require plural verbs.", catEng, now++);

        // 9. Islamic Studies (10 Quizzes)
        insertSampleCard(db, "How many chapters (Surahs) are in the Holy Quran?", "114 Surahs.", catIsl, now++);
        insertSampleCard(db, "In which Hijri year did the Migration (Hijrah) to Madinah occur?", "1 AH (622 CE).", catIsl, now++);
        insertSampleCard(db, "What are the Five Pillars of Islam?", "Shahada (Faith), Salah (Prayer), Zakat (Almsgiving), Sawm (Fasting), and Hajj (Pilgrimage).", catIsl, now++);
        insertSampleCard(db, "Which Surah of the Quran is known as the Heart of the Quran?", "Surah Yaseen (36th Chapter).", catIsl, now++);
        insertSampleCard(db, "In which year was the Treaty of Hudaybiyyah signed?", "6 AH (628 CE).", catIsl, now++);
        insertSampleCard(db, "Who was the first Caliph of Islam?", "Hazrat Abu Bakr Siddique (R.A.).", catIsl, now++);
        insertSampleCard(db, "In which Hijri year was the Battle of Badr fought?", "2 AH (624 CE).", catIsl, now++);
        insertSampleCard(db, "How many Paras (Juz) are there in the Holy Quran?", "30 Paras.", catIsl, now++);
        insertSampleCard(db, "What is the shortest Surah in the Holy Quran?", "Surah Al-Kawthar (3 verses).", catIsl, now++);
        insertSampleCard(db, "In which year did the Conquest of Makkah (Fath-e-Makkah) occur?", "8 AH (630 CE).", catIsl, now++);

        // 10. Pakistan Affairs (10 Quizzes)
        insertSampleCard(db, "In which year was the Lahore Resolution passed?", "1940 (23rd March).", catPak, now++);
        insertSampleCard(db, "Who was the first Governor-General of Pakistan?", "Quaid-e-Azam Muhammad Ali Jinnah.", catPak, now++);
        insertSampleCard(db, "In which year was the Objectives Resolution passed by the Constituent Assembly?", "1949 (12th March).", catPak, now++);
        insertSampleCard(db, "When was the current Constitution of Pakistan enacted?", "1973 (14th August).", catPak, now++);
        insertSampleCard(db, "What is the highest mountain peak in Pakistan?", "K2 (Godwin-Austen, 8,611 meters).", catPak, now++);
        insertSampleCard(db, "In which year was the Indus Waters Treaty signed between Pakistan and India?", "1960.", catPak, now++);
        insertSampleCard(db, "Who was the Prime Minister under whom Pakistan's first 1956 Constitution was framed?", "Chaudhry Muhammad Ali.", catPak, now++);
        insertSampleCard(db, "When did Pakistan officially become an Islamic Republic?", "1956 (23rd March).", catPak, now++);
        insertSampleCard(db, "Which river is the longest river in Pakistan?", "The Indus River (~3,180 km).", catPak, now++);
        insertSampleCard(db, "In which year did Pakistan conduct its public nuclear tests (Youm-e-Takbeer)?", "1998 (28th May at Chagai).", catPak, now++);
    }

    private long insertSampleCategory(SQLiteDatabase db, String name, int isEnabled) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, name);
        cv.put(COL_CAT_ENABLED, isEnabled);
        return db.insert(TABLE_CATEGORIES, null, cv);
    }

    private void insertSampleCard(SQLiteDatabase db, String question, String answer, long categoryId, long createdAt) {
        ContentValues cv = new ContentValues();
        cv.put(COL_QUESTION, question);
        cv.put(COL_ANSWER, answer);
        cv.put(COL_CATEGORY_ID, categoryId);
        cv.put(COL_CREATED_AT, createdAt);
        db.insert(TABLE_FLASHCARDS, null, cv);
    }

    // ================= CATEGORY CRUD METHODS =================

    public long insertCategory(String name, boolean isEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, name.trim());
        cv.put(COL_CAT_ENABLED, isEnabled ? 1 : 0);
        return db.insert(TABLE_CATEGORIES, null, cv);
    }

    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT c._id, c.name, c.is_enabled, COUNT(f._id) AS card_count " +
                "FROM " + TABLE_CATEGORIES + " c " +
                "LEFT JOIN " + TABLE_FLASHCARDS + " f ON c._id = f.category_id " +
                "GROUP BY c._id, c.name, c.is_enabled " +
                "ORDER BY c.name ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndexOrThrow(COL_CAT_ID);
                int nameIdx = cursor.getColumnIndexOrThrow(COL_CAT_NAME);
                int enabledIdx = cursor.getColumnIndexOrThrow(COL_CAT_ENABLED);
                int countIdx = cursor.getColumnIndexOrThrow("card_count");

                do {
                    long id = cursor.getLong(idIdx);
                    String name = cursor.getString(nameIdx);
                    boolean isEnabled = cursor.getInt(enabledIdx) == 1;
                    int cardCount = cursor.getInt(countIdx);

                    categoryList.add(new Category(id, name, isEnabled, cardCount));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching categories", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return categoryList;
    }

    public int updateCategoryEnabled(long categoryId, boolean isEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_ENABLED, isEnabled ? 1 : 0);
        return db.update(TABLE_CATEGORIES, cv, COL_CAT_ID + " = ?", new String[]{String.valueOf(categoryId)});
    }

    public int updateCategoryName(long categoryId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, newName.trim());
        return db.update(TABLE_CATEGORIES, cv, COL_CAT_ID + " = ?", new String[]{String.valueOf(categoryId)});
    }

    public int deleteCategory(long categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FLASHCARDS, COL_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        return db.delete(TABLE_CATEGORIES, COL_CAT_ID + " = ?", new String[]{String.valueOf(categoryId)});
    }

    // ================= FLASHCARD CRUD METHODS =================

    public long insertCard(String question, String answer, long categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_QUESTION, question.trim());
        cv.put(COL_ANSWER, answer.trim());
        cv.put(COL_CATEGORY_ID, categoryId);
        cv.put(COL_CREATED_AT, System.currentTimeMillis());

        return db.insert(TABLE_FLASHCARDS, null, cv);
    }

    /**
     * Retrieves active flashcards belonging ONLY to currently enabled categories.
     */
    public List<Flashcard> getActiveCards() {
        List<Flashcard> cardList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT f._id, f.question, f.answer, f.created_at, f.category_id, c.name AS category_name " +
                "FROM " + TABLE_FLASHCARDS + " f " +
                "INNER JOIN " + TABLE_CATEGORIES + " c ON f.category_id = c._id " +
                "WHERE c.is_enabled = 1 " +
                "ORDER BY f._id ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(COL_ID);
                int questionIndex = cursor.getColumnIndexOrThrow(COL_QUESTION);
                int answerIndex = cursor.getColumnIndexOrThrow(COL_ANSWER);
                int createdIndex = cursor.getColumnIndexOrThrow(COL_CREATED_AT);
                int catIdIndex = cursor.getColumnIndexOrThrow(COL_CATEGORY_ID);
                int catNameIndex = cursor.getColumnIndexOrThrow("category_name");

                do {
                    long id = cursor.getLong(idIndex);
                    String question = cursor.getString(questionIndex);
                    String answer = cursor.getString(answerIndex);
                    long createdAt = cursor.getLong(createdIndex);
                    long categoryId = cursor.getLong(catIdIndex);
                    String categoryName = cursor.getString(catNameIndex);

                    cardList.add(new Flashcard(id, question, answer, createdAt, categoryId, categoryName));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying active flashcards", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return cardList;
    }

    public List<Flashcard> getAllCards() {
        return getActiveCards();
    }

    public int updateCard(long id, String question, String answer, long categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_QUESTION, question.trim());
        cv.put(COL_ANSWER, answer.trim());
        cv.put(COL_CATEGORY_ID, categoryId);

        return db.update(TABLE_FLASHCARDS, cv, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteCard(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_FLASHCARDS, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }
}
