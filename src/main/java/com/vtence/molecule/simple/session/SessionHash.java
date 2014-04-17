package com.vtence.molecule.simple.session;

import com.vtence.molecule.Session;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SessionHash implements Session {

    private final String id;
    private final Map<Object, Object> attributes = new ConcurrentHashMap<Object, Object>();

    private Date createdAt;
    private Date updatedAt;
    private boolean invalid;
    private int maxAge = -1;

    public SessionHash() {
        this(null);
    }

    public SessionHash(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean exists() {
        return id != null;
    }

    public Date createdAt() {
        return createdAt;
    }

    public void createdAt(Date time) {
        createdAt = time;
    }

    public Date updatedAt() {
        return updatedAt;
    }

    public void updatedAt(Date time) {
        updatedAt = time;
    }

    public int maxAge() {
        return maxAge;
    }

    public void maxAge(int seconds) {
        maxAge = seconds;
    }

    public Date expirationTime() {
        return expires() ? new Date(updatedAt.getTime() + TimeUnit.SECONDS.toMillis(maxAge)) : null;
    }

    private boolean expires() {
        return maxAge >= 0 && updatedAt != null;
    }

    public int size() {
        return attributes.size();
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public boolean contains(Object key) {
        return attributes.containsKey(key);
    }

    public void put(Object key, Object value) {
        checkValid();
        attributes.put(key, value);
    }

    private void checkValid() {
        if (invalid) throw new IllegalStateException("Session invalidated");
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) attributes.get(key);
    }

    public Set<Object> keys() {
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

    public String toString() {
        return id + ": " + attributes.toString();
    }
}