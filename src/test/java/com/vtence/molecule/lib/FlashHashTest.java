package com.vtence.molecule.lib;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class FlashHashTest {

    @Test
    public void isInitiallyEmpty() {
        FlashHash flash = new FlashHash();

        assertThat("keys", flash.keys(), emptyIterable());
        assertThat("is empty", flash.empty(), is(true));
    }

    @Test
    public void copiesEntriesDefensively() {
        Map<String, String> entries = new HashMap<>();
        entries.put("greeting", "Hello");

        FlashHash flash = new FlashHash(entries);
        entries.clear();

        assertThat("keys", flash.keys(), contains("greeting"));
    }

    @Test
    public void storesMultipleEntries() {
        FlashHash flash = new FlashHash();
        flash.put("greeting", "Hello");
        flash.put("farewell", "Goodbye");

        assertThat("is empty", flash.empty(), is(false));
        assertThat("greeting", flash.get("greeting"), equalTo("Hello"));
        assertThat("farewell", flash.get("farewell"), equalTo("Goodbye"));
    }

    @Test
    public void maintainsASetOfKeys() {
        FlashHash flash = new FlashHash();
        flash.put("greeting", "Hello");
        flash.put("farewell", "Goodbye");

        assertThat("greeting?", flash.has("greeting"), is(true));
        assertThat("farewell?", flash.has("farewell"), is(true));
        assertThat("other?", flash.has("other"), is(false));
        assertThat("keys", flash.keys(), containsInAnyOrder("greeting", "farewell"));
    }

    @Test
    public void clearsAllEntries() {
        FlashHash flash = new FlashHash();
        flash.put("greeting", "Hello");
        flash.put("farewell", "Goodbye");

        flash.clear();
        assertThat("empty", flash.empty(), is(true));
    }

    @Test
    public void updatesEntries() {
        FlashHash flash = new FlashHash();
        flash.put("greeting", "Goodbye");

        String oldGreeting = flash.put("greeting", "Hello");

        assertThat("old greeting", oldGreeting, equalTo("Goodbye"));
        assertThat("updated greeting", flash.get("greeting"), equalTo("Hello"));
    }

    @Test
    public void removesEntries() {
        FlashHash flash = new FlashHash();
        flash.put("greeting", "Hello");
        String pastGreeting = flash.remove("greeting");

        assertThat("past greeting", pastGreeting, equalTo("Hello"));
        assertThat("removed greeting", flash.get("greeting"), nullValue());
    }

    @Test
    public void sweepsOldEntries() {
        Map<String, String> oldEntries = new HashMap<>();
        oldEntries.put("greeting", "Hello");
        FlashHash flash = new FlashHash(oldEntries);

        flash.put("farewell", "Goodbye");
        flash.sweep();

        assertThat("fresh count", flash.toMap(), aMapWithSize(1));
        assertThat("fresh values", flash.toMap(), hasKey("farewell"));
    }
}
