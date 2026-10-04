package com.example.myquiz;

import java.io.Serializable;
import java.util.Objects;

/**
 * Data Model representing a Quiz Category.
 */
public class Category implements Serializable {

    private long id;
    private String name;
    private boolean isEnabled;
    private int cardCount;

    public Category() {
    }

    public Category(long id, String name, boolean isEnabled) {
        this.id = id;
        this.name = name;
        this.isEnabled = isEnabled;
    }

    public Category(long id, String name, boolean isEnabled, int cardCount) {
        this.id = id;
        this.name = name;
        this.isEnabled = isEnabled;
        this.cardCount = cardCount;
    }

    public Category(String name, boolean isEnabled) {
        this.name = name;
        this.isEnabled = isEnabled;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id &&
                isEnabled == category.isEnabled &&
                cardCount == category.cardCount &&
                Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, isEnabled, cardCount);
    }

    @Override
    public String toString() {
        return name;
    }
}
