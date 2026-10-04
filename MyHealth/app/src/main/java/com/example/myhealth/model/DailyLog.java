package com.example.myhealth.model;

public class DailyLog {
    private long id;
    private String date;
    private int waterMl;
    private int steps;
    private int caloriesConsumed;
    private int caloriesBurned;

    public DailyLog() {
    }

    public DailyLog(long id, String date, int waterMl, int steps, int caloriesConsumed, int caloriesBurned) {
        this.id = id;
        this.date = date;
        this.waterMl = waterMl;
        this.steps = steps;
        this.caloriesConsumed = caloriesConsumed;
        this.caloriesBurned = caloriesBurned;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getWaterMl() { return waterMl; }
    public void setWaterMl(int waterMl) { this.waterMl = waterMl; }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public int getCaloriesConsumed() { return caloriesConsumed; }
    public void setCaloriesConsumed(int caloriesConsumed) { this.caloriesConsumed = caloriesConsumed; }

    public int getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(int caloriesBurned) { this.caloriesBurned = caloriesBurned; }
}
