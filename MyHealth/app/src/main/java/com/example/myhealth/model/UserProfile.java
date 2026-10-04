package com.example.myhealth.model;

public class UserProfile {
    private long id;
    private String name;
    private String contact;
    private String email;
    private String country;
    private String gender;
    private int age;
    private float height;
    private float currentWeight;
    private float targetWeight;
    private String activityLevel;
    private String goal;
    private String createdAt;

    public UserProfile() {
    }

    public UserProfile(long id, String name, String contact, String email, String country,
                       String gender, int age, float height, float currentWeight,
                       float targetWeight, String activityLevel, String goal, String createdAt) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.country = country;
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.currentWeight = currentWeight;
        this.targetWeight = targetWeight;
        this.activityLevel = activityLevel;
        this.goal = goal;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public float getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(float currentWeight) { this.currentWeight = currentWeight; }

    public float getTargetWeight() { return targetWeight; }
    public void setTargetWeight(float targetWeight) { this.targetWeight = targetWeight; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
