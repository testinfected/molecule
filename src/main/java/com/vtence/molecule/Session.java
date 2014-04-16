package com.vtence.molecule;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

public interface Session {

    String id();

    boolean exists();

    Date createdAt();

    void createdAt(Date time);

    Date updatedAt();

    void updatedAt(Date time);

    int maxAge();

    void maxAge(int seconds);

    Date expirationTime();

    int size();

    boolean isEmpty();

    boolean contains(Object key);

    <T> T get(Object key);

    void put(Object key, Object value);

    Set<?> keys();

    Collection<?> values();

    void clear();

    void merge(Session other);

    void invalidate();

    boolean invalid();
}