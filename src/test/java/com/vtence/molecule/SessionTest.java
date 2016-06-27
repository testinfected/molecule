package com.vtence.molecule;

import com.vtence.molecule.session.Session;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class SessionTest {

    int timeoutInSeconds = (int) TimeUnit.MINUTES.toSeconds(30);
    Instant updateTime = Instant.now();

    @Test
    public void
    isFreshWithoutAnId() {
        Session newSession = new Session();
        assertThat("new is fresh?", newSession.fresh(), equalTo(true));
        assertThat("new id", newSession.id(), nullValue());

        Session oldSession = new Session("old");
        assertThat("old is fresh?", oldSession.fresh(), equalTo(false));
        assertThat("old id", oldSession.id(), equalTo("old"));
    }

    @Test
    public void
    isInitiallyEmptyAndValid() {
        Session session = new Session();
        assertThat("initially valid?", !session.invalid(), equalTo(true));
        assertThat("initial keys", session.keys(), emptyIterable());
        assertThat("initial values", session.values(), emptyIterable());
    }

    @Test
    public void
    storesMultipleAttributes() {
        Session session = new Session();
        session.put("A", "Alice");
        session.put("B", "Bob");
        session.put("C", "Chris");
        assertThat("A(lice)", session.<String>get("A"), equalTo("Alice"));
        assertThat("B(ob)", session.<String>get("B"), equalTo("Bob"));
        assertThat("C(hris)", session.<String>get("C"), equalTo("Chris"));
    }

    @Test
    public void
    allowsOverridingAttributes() {
        Session session = new Session();
        session.put("key", "original");
        session.put("key", "override");
        assertThat("overridden value", session.<String>get("key"), equalTo("override"));
    }

    @Test
    public void
    allowsRemovingAttributes() {
        Session session = new Session();
        session.put("A", "Alice");
        session.put("B", "Bob");
        session.put("C", "Chris");
        session.remove("B");
        assertThat("values", session.values(), containsItems("Alice", "Chris"));
    }

    @Test
    public void
    knowsItsContent() {
        Session session = new Session();
        session.put("A", "Alice");
        session.put("B", "Bob");
        session.put("C", "Chris");

        assertThat("knows A?", session.contains("A"), equalTo(true));
        assertThat("knows Z?", session.contains("Z"), equalTo(false));
        assertThat("known size", session.size(), equalTo(3));
        assertThat("known keys", session.keys(), containsItems("A", "B", "C"));
        assertThat("known values", session.values(), containsItems("Alice", "Bob", "Chris"));
    }

    @Test
    public void
    updatesContentFromOtherSession() {
        Session session = new Session();
        session.put("A", "Albert");
        session.put("C", "Chris");
        Session other = new Session();
        other.put("A", "Alice");
        other.put("B", "Bob");

        session.merge(other);
        assertThat("merged keys", session.keys(), containsItems("A", "B", "C"));
        assertThat("merged values", session.values(), containsItems("Alice", "Bob", "Chris"));
    }

    @Test
    public void
    dropsContentWhenInvalidated() {
        Session session = new Session();

        session.put("A", "Alice");
        session.put("B", "Bob");
        session.put("C", "Chris");

        session.invalidate();

        assertThat("invalid?", session.invalid(), equalTo(true));
        assertThat("keys once invalid", session.keys(), emptyIterable());
        assertThat("values once invalid", session.values(), emptyIterable());
    }

    @Test
    public void
    isInitiallySetToNeverExpires() {
        Session session = new Session();
        assertThat("expires", session.expires(), equalTo(false));
        assertThat("default max age", session.maxAge(), equalTo(-1));
        assertThat("default expiration time", session.expirationTime(), equalTo(Instant.MAX));
        assertThat("has expired", !session.expired(Instant.MAX));
    }

    @Test
    public void
    expiresAtSpecifiedTime() {
        Session session = new Session();
        session.updatedAt(updateTime);
        session.maxAge(timeoutInSeconds);
        assertThat("expires", session.expires(), equalTo(true));
        assertThat("expiration time", session.expirationTime(), equalTo(whenTimeoutOccurs(updateTime)));
        assertThat("expired too early", !session.expired(justBefore(whenTimeoutOccurs(updateTime))));
        assertThat("expired too late", session.expired(whenTimeoutOccurs(updateTime)));
    }

    @Test(expected = IllegalStateException.class)
    public void
    canNoLongerBeWrittenOnceInvalidated() {
        Session session = new Session();
        session.invalidate();
        session.put("A", "Alice");
    }

    private Instant whenTimeoutOccurs(Instant pointInTime) {
        return pointInTime.plus(timeoutInSeconds, ChronoUnit.SECONDS);
    }

    private Instant justBefore(Instant pointInTime) {
        return pointInTime.minusNanos(1);
    }

    private Matcher<Iterable<?>> containsItems(Object... items) {
        return containsInAnyOrder(items);
    }
}
