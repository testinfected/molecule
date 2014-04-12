package com.vtence.molecule;

import com.vtence.molecule.util.Clock;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

public interface Session {

    String id();

    boolean isNew();

    Date createdAt();

    Date lastAccessedAt();

    boolean contains(Object key);

    <T> T get(Object key);

    void put(Object key, Object value);

    Set<?> keys();

    Collection<?> values();

    long timeout();

    void timeout(long inSeconds);

    // todo expiredAt(Date time)
    boolean expired(Clock clock);

    // todo lastAccessedAt(Date time)
    void touch(Clock clock);

    void invalidate();

    boolean invalid();

    boolean isEmpty();
}