package com.vtence.molecule.support;

import com.vtence.molecule.Session;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MockSession implements Session {

    private final Map<Object, Object> attributes = new HashMap<Object, Object>();
    private final String id;
    private int maxAge;

    public MockSession() {
        this("mock session");
    }

    public MockSession(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    @Override
    public boolean exists() {
        return false;
    }

    public boolean contains(Object key) {
        return attributes.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) attributes.get(key);
    }

    public void put(Object key, Object value) {
        attributes.put(key, value);
    }

    public Set<?> keys() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Collection<?> values() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void clear() {

    }

    public Date createdAt() {
        return null;
    }

    public void createdAt(Date time) {

    }

    public boolean expiredAt(Date time) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int maxAge() {
        return 0;
    }

    public void maxAge(int seconds) {
    }

    public Date expirationTime() {
        return null;
    }

    public void invalidate() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean invalid() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public void merge(Session other) {

    }

    public Date updatedAt() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void updatedAt(Date time) {

    }

    public int size() {
        return 0;
    }

    public String toString() {
        return id() + ": " + attributes.toString();
    }
}
