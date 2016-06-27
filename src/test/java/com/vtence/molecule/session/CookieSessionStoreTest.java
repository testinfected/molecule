package com.vtence.molecule.session;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.vtence.molecule.support.HasMethodWithValue.hasMethod;
import static java.lang.String.valueOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class CookieSessionStoreTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    SessionCookieEncoder encoder = context.mock(SessionCookieEncoder.class);
    Sequence counter = new Sequence();
    CookieSessionStore store = new CookieSessionStore(counter, encoder);

    @Test
    public void
    encodesSessionDataInCookie() {
        Session data = new Session("42");
        data.put("username", "Gorion");
        data.put("race", "Human");
        data.maxAge(1800);

        context.checking(new Expectations() {{
            allowing(encoder).encode(with(sameSessionDataAs(data))); will(returnValue("<encoded session>"));
        }});

        String cookie = store.save(data);
        assertThat("session cookie", cookie, equalTo("<encoded session>"));
    }

    @Test
    public void
    generatesIdsForNewSessions() {
        Session data = new Session();
        counter.expect(data);

        context.checking(new Expectations() {{
            allowing(encoder).encode(with(sessionWithId(counter.nextId())));
        }});

        store.save(data);
    }

    @Test
    public void
    decodesSessionFromCookieValue() {
        Session data = new Session() {
            public String toString() {
                return "decoded session";
            }
        };

        context.checking(new Expectations() {{
            allowing(encoder).decode("<encoded session>"); will(returnValue(data));
        }});

        Session session = store.load("<encoded session>");
        assertThat("session", session, sameInstance(data));
    }

    private Matcher<Session> sameSessionDataAs(Session data) {
        List<Matcher<? super Session>> matchers = new ArrayList<>();

        matchers.add(sessionWithId(data.id()));
        matchers.add(hasMethod("createdAt", data.createdAt()));
        matchers.add(hasMethod("updatedAt", data.updatedAt()));
        matchers.add(hasMethod("maxAge", data.maxAge()));

        matchers.addAll(data.keys().stream().map(key -> sessionWithSameAttributeAs(data, key)).collect(Collectors.toList()));

        return new AllOf<>(matchers);
    }

    private Matcher<Session> sessionWithId(String id) {
        return hasMethod("id", id);
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
