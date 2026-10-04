package com.example.myquiz;

import java.io.Serializable;
import java.util.Objects;

/**
 * Data Model POJO representing a Flashcard unit linked to a Quiz Category.
 */
public class Flashcard implements Serializable {

    private long id;
    private String question;
    private String answer;
    private long createdAt;
    private long categoryId;
    private String categoryName;

    public Flashcard() {
    }

    public Flashcard(long id, String question, String answer, long createdAt, long categoryId) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt;
        this.categoryId = categoryId;
    }

    public Flashcard(long id, String question, String answer, long createdAt, long categoryId, String categoryName) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Flashcard(String question, String answer, long categoryId) {
        this.question = question;
        this.answer = answer;
        this.createdAt = System.currentTimeMillis();
        this.categoryId = categoryId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flashcard flashcard = (Flashcard) o;
        return id == flashcard.id &&
                createdAt == flashcard.createdAt &&
                categoryId == flashcard.categoryId &&
                Objects.equals(question, flashcard.question) &&
                Objects.equals(answer, flashcard.answer) &&
                Objects.equals(categoryName, flashcard.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, question, answer, createdAt, categoryId, categoryName);
    }

    @Override
    public String toString() {
        return "Flashcard{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
