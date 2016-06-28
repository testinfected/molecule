package com.vtence.molecule.session;

import com.vtence.molecule.support.Delorean;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static com.vtence.molecule.session.SessionMatchers.sameSessionDataAs;
import static com.vtence.molecule.session.SessionMatchers.sessionCreatedAt;
import static com.vtence.molecule.session.SessionMatchers.sessionUpdatedAt;
import static com.vtence.molecule.session.SessionMatchers.sessionWithId;
import static java.lang.String.valueOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class CookieSessionStoreTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    SessionEncoder encoder = context.mock(SessionEncoder.class);
    Delorean delorean = new Delorean();
    Sequence counter = new Sequence();
    CookieSessionStore cookies = new CookieSessionStore(counter, encoder).usingClock(delorean);

    int maxAge = (int) TimeUnit.MINUTES.toSeconds(30);
    int timeToLive = (int) TimeUnit.DAYS.toSeconds(2);

    @Test public void
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

    @Test public void
    generatesIdsForNewSessions() throws Exception {
        Session data = new Session();
        counter.expect(data);

        context.checking(new Expectations() {{
            allowing(encoder).encode(with(sessionWithId(counter.nextId()))); will(returnValue("<...>"));
        }});

        cookies.save(data);
    }

    @Test public void
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

    @Test public void
    canRenewExistingSessionsIdsOnSave() throws Exception {
        Session data = new Session("42");

        context.checking(new Expectations() {{
            allowing(encoder).encode(with(sessionWithId(counter.nextId()))); will(returnValue("<with renewed id>"));
        }});

        cookies.renewIds();
        String renewed = cookies.save(data);

        assertThat("renewed id", renewed, equalTo("<with renewed id>"));
    }

    @Test public void
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

    @Test public void
    discardsExpiredSessions() throws Exception {
        Session expired = new Session();
        expired.maxAge(maxAge);

        context.checking(new Expectations() {{
            allowing(encoder).decode("expired"); will(returnValue(expired));
        }});

        delorean.travelInTime(timeJump(maxAge));

        assertThat("expired session", cookies.load("expired"), nullValue());
    }

    @Test public void
    discardsStaleSessions() throws Exception {
        cookies.idleTimeout(maxAge);
        Session staleSession = new Session();

        context.checking(new Expectations() {{
            allowing(encoder).decode("stale"); will(returnValue(staleSession));
        }});

        delorean.travelInTime(timeJump(maxAge));
        assertThat("stale session", cookies.load("stale"), nullValue());
    }

    @Test public void
    limitsSessionsLifetime() throws Exception {
        cookies.timeToLive(timeToLive);
        Session dead = new Session();

        context.checking(new Expectations() {{
            allowing(encoder).decode("dead"); will(returnValue(dead));
        }});

        delorean.travelInTime(timeJump(timeToLive));
        assertThat("dead session", cookies.load("dead"), nullValue());
    }

    @Test public void
    ignoresCorruptedSessions() throws Exception {
        context.checking(new Expectations() {{
            allowing(encoder).decode("corrupted"); will(returnValue(null));
        }});

        assertThat("corrupted session", cookies.load("corrupted"), nullValue());
    }

    private long timeJump(int seconds) {
        return TimeUnit.SECONDS.toMillis(seconds);
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
