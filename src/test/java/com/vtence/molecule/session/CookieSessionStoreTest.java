package com.vtence.molecule.session;

import com.vtence.molecule.support.Delorean;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.vtence.molecule.support.HasMethodWithValue.hasMethod;
import static java.lang.String.valueOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class CookieSessionStoreTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    SessionCookieEncoder encoder = context.mock(SessionCookieEncoder.class);
    Delorean delorean = new Delorean();
    Sequence counter = new Sequence();
    CookieSessionStore cookies = new CookieSessionStore(counter, encoder, delorean);

    int maxAge = (int) TimeUnit.MINUTES.toSeconds(30);
    int timeToLive = (int) TimeUnit.DAYS.toSeconds(2);

    @Test
    public void
    encodesSessionDataInCookie() throws Exception {
        Session data = new Session("42");
        Instant now = delorean.freeze();
        data.updatedAt(now);
        data.createdAt(now);
        data.put("username", "Gorion");
        data.put("race", "Human");
        data.maxAge(1800);

        context.checking(new Expectations() {{
            allowing(encoder).encode(with(sameSessionDataAs(data))); will(returnValue("<encoded session>"));
        }});

        String cookie = cookies.save(data);
        assertThat("session cookie", cookie, equalTo("<encoded session>"));
    }

    @Test
    public void
    generatesIdsForNewSessions() throws Exception {
        Session data = new Session();
        counter.expect(data);

        context.checking(new Expectations() {{
            allowing(encoder).encode(with(sessionWithId(counter.nextId()))); will(returnValue("<...>"));
        }});

        cookies.save(data);
    }

    @Test
    public void
    decodesSessionFromCookieValue() throws Exception {
        Session data = new Session() {
            public String toString() {
                return "decoded session";
            }
        };

        context.checking(new Expectations() {{
            allowing(encoder).decode("<encoded session>"); will(returnValue(data));
        }});

        Session session = cookies.load("<encoded session>");
        assertThat("session", session, sameInstance(data));
    }

    @Test
    public void
    canRenewExistingSessionsIdsOnSave() throws Exception {
        Session data = new Session("42");

        context.checking(new Expectations() {{
            allowing(encoder).encode(with(sessionWithId(counter.nextId()))); will(returnValue("<with renewed id>"));
        }});

        cookies.renewIds();
        String renewed = cookies.save(data);

        assertThat("renewed id", renewed, equalTo("<with renewed id>"));
    }

    @Test
    public void
    marksSessionUpdateTime() throws Exception {
        Session data = new Session();
        delorean.travelInTime(50);
        Instant updateTime = delorean.freeze();

        context.checking(new Expectations() {{
            allowing(encoder).encode(with(sessionUpdatedAt(updateTime)));
        }});

        cookies.save(data);
    }

    @Test
    public void
    marksSessionCreationTimeForNewSessions() throws Exception {
        Session data = new Session();
        delorean.travelInTime(50);
        Instant creationTime = delorean.freeze();

        context.checking(new Expectations() {{
            allowing(encoder).encode(with(sessionCreatedAt(creationTime)));
        }});

        cookies.save(data);
    }

    @Test
    public void
    discardsExpiredSessions() throws Exception {
        Session expired = new Session();
        expired.maxAge(maxAge);

        context.checking(new Expectations() {{
            allowing(encoder).decode("expired"); will(returnValue(expired));
        }});

        delorean.travelInTime(timeJump(maxAge));

        assertThat("expired session", cookies.load("expired"), nullValue());
    }

    @Test
    public void
    discardsStaleSessions() throws Exception {
        cookies.idleTimeout(maxAge);
        Session staleSession = new Session();

        context.checking(new Expectations() {{
            allowing(encoder).decode("stale"); will(returnValue(staleSession));
        }});

        delorean.travelInTime(timeJump(maxAge));
        assertThat("stale session", cookies.load("stale"), nullValue());
    }

    @Test
    public void
    limitsSessionsLifetime() throws Exception {
        cookies.timeToLive(timeToLive);
        Session dead = new Session();

        context.checking(new Expectations() {{
            allowing(encoder).decode("dead"); will(returnValue(dead));
        }});

        delorean.travelInTime(timeJump(timeToLive));
        assertThat("dead session", cookies.load("dead"), nullValue());
    }

    private long timeJump(int seconds) {
        return TimeUnit.SECONDS.toMillis(seconds);
    }

    private Matcher<Session> sameSessionDataAs(Session data) {
        List<Matcher<? super Session>> matchers = new ArrayList<>();

        matchers.add(sessionWithId(data.id()));
        matchers.add(sessionCreatedAt(data.createdAt()));
        matchers.add(sessionUpdatedAt(data.updatedAt()));
        matchers.add(sessionWithMaxAge(data.maxAge()));

        matchers.addAll(data.keys().stream().map(key -> sessionWithSameAttributeAs(data, key)).collect(Collectors.toList()));

        return new AllOf<>(matchers);
    }

    private Matcher<Session> sessionWithId(String id) {
        return hasMethod("id", id);
    }

    private Matcher<Session> sessionCreatedAt(Instant value) {
        return hasMethod("createdAt", value);
    }

    private Matcher<Session> sessionUpdatedAt(Instant value) {
        return hasMethod("updatedAt", value);
    }

    private Matcher<Session> sessionWithMaxAge(int maxAge) {
        return hasMethod("maxAge", maxAge);
    }

    private FeatureMatcher<Session, ?> sessionWithSameAttributeAs(final Session data, final String key) {
        return new FeatureMatcher<Session, Object>(equalTo(data.get(key)), "session with attribute " + key, key) {
            protected Object featureValueOf(Session actual) {
                return actual.get(key);
            }
        };
    }

    private class Sequence implements SessionIdentifierPolicy {
        private int nextId;
        private Matcher<Session> session = notNullValue(Session.class);

        private Sequence() {
            this(1);
        }

        public Sequence(int seed) {
            this.nextId = seed;
        }

        public void expect(Session session) {
            expect(equalTo(session));
        }

        public void expect(Matcher<Session> matching) {
            this.session = matching;
        }

        public String generateId(Session data) {
            assertThat("session data", data, session);
            return valueOf(nextId++);
        }

        public String nextId() {
            return valueOf(nextId);
        }
    }
}
