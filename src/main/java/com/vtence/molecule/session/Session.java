package com.vtence.molecule.session;

import com.vtence.molecule.Request;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Session implements Serializable {

    private final String id;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private boolean invalid;
    private int maxAge = -1;

    public Session() {
        this(null);
    }

    public Session(String id) {
        this.id = id;
    }

    public static Session get(Request request) {
        return request.attribute(Session.class);
    }

    public void bind(Request request) {
        request.attribute(Session.class, this);
    }

    public static void unbind(Request request) {
        request.removeAttribute(Session.class);
    }

    public String id() {
        return id;
    }

    public boolean fresh() {
        return id == null;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public void createdAt(Instant pointInTime) {
        createdAt = pointInTime;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public void updatedAt(Instant pointInTime) {
        updatedAt = pointInTime;
    }

    public int maxAge() {
        return maxAge;
    }

    public void maxAge(int seconds) {
        maxAge = seconds;
    }

    public Instant expirationTime() {
        return expires() ? updatedAt.plusSeconds(maxAge) : Instant.MAX;
    }

    public boolean expires() {
        return maxAge >= 0;
    }

    public boolean expired(Instant instant) {
        return expires() && !instant.isBefore(expirationTime());
    }

    public int size() {
        return attributes.size();
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public boolean contains(Object key) {
        return attributes.containsKey(String.valueOf(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) attributes.get(String.valueOf(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T put(Object key, Object value) {
        checkValid();
        return (T) attributes.put(String.valueOf(key), value);
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(Object key) {
        checkValid();
        return (T) attributes.remove(String.valueOf(key));
    }

    private void checkValid() {
        if (invalid) throw new IllegalStateException("Session invalidated");
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(attributes.keySet());
    }

    public Collection<Object> values() {
        return Collections.unmodifiableCollection(attributes.values());
    }

    public void clear() {
        this.attributes.clear();
    }

    public void merge(Session other) {
        for (Object key : other.keys()) {
            put(key, other.get(key));
        }
    }

    public void invalidate() {
        clear();
        invalid = true;
    }

    public boolean invalid() {
        return invalid;
    }
}
