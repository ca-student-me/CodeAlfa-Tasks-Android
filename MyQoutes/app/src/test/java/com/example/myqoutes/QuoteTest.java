package com.example.myqoutes;

import org.junit.Test;

import static org.junit.Assert.*;

public class QuoteTest {

    @Test
    public void testQuoteCreationAndGetters() {
        Quote quote = new Quote(1, "Life is what happens when you're busy making other plans.", "John Lennon", false);
        assertEquals(1, quote.getId());
        assertEquals("Life is what happens when you're busy making other plans.", quote.getQuoteText());
        assertEquals("John Lennon", quote.getAuthor());
        assertFalse(quote.isCustom());
    }

    @Test
    public void testCustomQuoteCreation() {
        Quote quote = new Quote("Be yourself; everyone else is already taken.", "Oscar Wilde", true);
        assertEquals("Be yourself; everyone else is already taken.", quote.getQuoteText());
        assertEquals("Oscar Wilde", quote.getAuthor());
        assertTrue(quote.isCustom());
    }

    @Test
    public void testQuoteEqualsAndHashCode() {
        Quote q1 = new Quote(10, "Test Quote", "Test Author", true);
        Quote q2 = new Quote(10, "Test Quote", "Test Author", true);
        Quote q3 = new Quote(11, "Different Quote", "Test Author", false);

        assertEquals(q1, q2);
        assertEquals(q1.hashCode(), q2.hashCode());
        assertNotEquals(q1, q3);
    }
}
