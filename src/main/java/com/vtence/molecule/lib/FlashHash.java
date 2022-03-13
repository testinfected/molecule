package com.vtence.molecule.lib;

import com.vtence.molecule.Request;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FlashHash {
    private final Map<String, Object> entries = new HashMap<>();
    private final Set<String> keep = new HashSet<>();

    public FlashHash() {
        this(Collections.emptyMap());
    }

    public FlashHash(Map<String, ?> flashes) {
        this.entries.putAll(flashes);
    }

    public static FlashHash get(Request request) {
        return request.attribute(FlashHash.class);
    }

    public void bind(Request request) {
        request.attribute(FlashHash.class, this);
    }

    public static void unbind(Request request) {
        request.removeAttribute(FlashHash.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) entries.get(String.valueOf(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T put(Object key, Object value) {
        keep.add(String.valueOf(key));
        return (T) entries.put(String.valueOf(key), value);
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(Object key) {
        return (T) entries.remove(String.valueOf(key));
    }

    public boolean has(Object key) {
        return entries.containsKey(String.valueOf(key));
    }

    public boolean empty() {
        return entries.isEmpty();
    }

    public Set<String> keys() {
        return entries.keySet();
    }

    public void notice(String message) {
        put("notice", message);
    }

    public String notice() {
        return get("notice");
    }

    public void alert(String message) {
        put("alert", message);
    }

    public String alert() {
        return get("alert");
    }

    public void clear() {
        entries.clear();
    }

    public void putAll(Map<?, ?> values) {
        for (Object key : values.keySet()) {
            put(key, values.get(key));
        }
    }

    public void sweep() {
        var fresh = new HashMap<String, Object>();
        entries.keySet().stream().filter(this.keep::contains).forEach(key -> fresh.put(key, entries.get(key)));
        entries.clear();
        entries.putAll(fresh);
    }

    public Map<String, Object> toMap() {
        return new HashMap<>(entries);
    }
}