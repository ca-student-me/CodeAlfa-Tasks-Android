package com.example.myqoutes;

import java.util.Objects;

public class Quote {
    private int id;
    private String quoteText;
    private String author;
    private boolean isCustom;

    public Quote(int id, String quoteText, String author, boolean isCustom) {
        this.id = id;
        this.quoteText = quoteText;
        this.author = author;
        this.isCustom = isCustom;
    }

    public Quote(String quoteText, String author, boolean isCustom) {
        this.quoteText = quoteText;
        this.author = author;
        this.isCustom = isCustom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quote quote = (Quote) o;
        return id == quote.id && isCustom == quote.isCustom &&
                Objects.equals(quoteText, quote.quoteText) &&
                Objects.equals(author, quote.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quoteText, author, isCustom);
    }
}
