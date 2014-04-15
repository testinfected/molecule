package com.vtence.molecule.middlewares;

import com.vtence.molecule.*;
import com.vtence.molecule.simple.session.SessionHash;
import com.vtence.molecule.simple.session.SessionStore;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.hamcrest.FeatureMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.vtence.molecule.support.Cookies.cookieWithMaxAge;
import static com.vtence.molecule.support.Cookies.cookieWithValue;
import static com.vtence.molecule.support.Cookies.httpOnlyCookie;
import static org.hamcrest.Matchers.equalTo;

public class CookieSessionTrackerTest {

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();

    SessionStore store = context.mock(SessionStore.class);
    int timeout = (int) TimeUnit.MINUTES.toSeconds(30);
    CookieSessionTracker tracker = new CookieSessionTracker(store);

    MockRequest request = new MockRequest();
    MockResponse response = new MockResponse();

    @Before public void
    stubSessionStore() {
        context.checking(new Expectations() {{
            allowing(store).load("existing"); will(returnValue(new SessionHash("existing")));
            allowing(store).load("expired"); will(returnValue(null));
        }});
    }

    @Test public void
    createsSessionsForNewClientsButDoesNotCommitEmptySessions() throws Exception {
        tracker.connectTo(echoSessionId());
        context.checking(new Expectations() {{
            never(store).save(with(any(Session.class)));
        }});

        tracker.handle(request, response);
        response.assertBody("Session: null");
        response.assertHasNoCookie("JSESSIONID");
    }

    @Test public void
    createsANewCookieAndStoresNewSessionIfItContainsData() throws Exception {
        tracker.connectTo(incrementCounter());
        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new"));
        }});

        tracker.handle(request, response);
        response.assertBody("Counter: 1");
        response.assertCookie("JSESSIONID", cookieWithValue("new"));
        response.assertCookie("JSESSIONID", httpOnlyCookie(true));
    }

    @Test public void
    tracksExistingSessionsUsingACookieAndSavesSessionIfModified() throws Exception {
        tracker.connectTo(incrementCounter());

        Session clientSession = store.load("existing");
        clientSession.put("counter", 1);
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        tracker.handle(request.withCookie("JSESSIONID", "existing"), response);
        response.assertBody("Counter: 2");
    }

    @Test public void
    savesExistingSessionEvenIfNotWritten() throws Exception {
        tracker.connectTo(nothing());
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        tracker.handle(request.withCookie("JSESSIONID", "existing"), response);
    }

    @Test public void
    createsAFreshSessionIfClientSessionHasExpired() throws Exception {
        tracker.connectTo(incrementCounter());

        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new"));
        }});

        tracker.handle(request.withCookie("JSESSIONID", "expired"), response);
        response.assertBody("Counter: 1");
        response.assertCookie("JSESSIONID", cookieWithValue("new"));
    }

    @Test public void
    doesNotSendTheSameSessionIdIfItDidNotChange() throws Exception {
        tracker.connectTo(nothing());
        context.checking(new Expectations() {{
            allowing(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        tracker.handle(request.withCookie("JSESSIONID", "existing"), response);
        response.assertHasNoCookie("JSESSIONID");
    }

    @Test public void
    destroysInvalidSessions() throws Exception {
        tracker.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                Session session = request.attribute(Session.class);
                session.invalidate();
            }
        });
        context.checking(new Expectations() {{
            oneOf(store).destroy(with("existing"));
        }});

        tracker.handle(request.withCookie("JSESSIONID", "existing"), response);
        response.assertHasNoCookie("JSESSIONID");
    }

    @Test public void
    usesPersistentSessionsByDefault() throws Exception {
        tracker.connectTo(incrementCounter());
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithMaxAge(-1))); will(returnValue("persistent"));
        }});
        tracker.handle(request, response);
        response.assertCookie("JSESSIONID", cookieWithMaxAge(-1));
    }

    @Test public void
    setsSessionsToExpireAfterMaxAge() throws Exception {
        tracker.connectTo(expireSessionAfter(timeout));
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithMaxAge(timeout))); will(returnValue("expires"));
        }});
        tracker.handle(request, response);
        response.assertCookie("JSESSIONID", cookieWithMaxAge(timeout));
    }

    @Test public void
    setsNewSessionsToExpiresIfMaxAgeSpecified() throws Exception {
        tracker.expireAfter(timeout);
        tracker.connectTo(incrementCounter());
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithMaxAge(timeout))); will(returnValue("expires"));
        }});
        tracker.handle(request, response);
    }

    @Test public void
    setsCookieEvenOnExistingSessionsIfMaxAgeSpecified() throws Exception {
        tracker.connectTo(expireSessionAfter(timeout));
        context.checking(new Expectations() {{
            allowing(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});
        tracker.handle(request.withCookie("JSESSIONID", "existing"), response);
        response.assertCookie("JSESSIONID", cookieWithMaxAge(timeout));
    }

    private FeatureMatcher<Session, String> newSession() {
        return sessionWithId(null);
    }

    private FeatureMatcher<Session, String> sessionWithId(String sessionId) {
        return new FeatureMatcher<Session, String>(equalTo(sessionId), "session with id", "session id") {
            protected String featureValueOf(Session actual) {
                return actual.id();
            }
        };
    }

    private FeatureMatcher<Session, Integer> sessionWithMaxAge(final int maxAge) {
        return new FeatureMatcher<Session, Integer>(equalTo(maxAge), "session with max age", "max age") {
            protected Integer featureValueOf(Session actual) {
                return actual.maxAge();
            }
        };
    }

    private Application echoSessionId() {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
                Session session = request.attribute(Session.class);
                response.body("Session: " + session.id());
            }
        };
    }

    private Application incrementCounter() {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
                Session session = request.attribute(Session.class);
                Integer counter = session.contains("counter") ? session.<Integer>get("counter") : 0;
                session.put("counter", counter++);
                response.body("Counter: " + counter);
            }
        };
    }

    private Application nothing() {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
            }
        };
    }

    private Application expireSessionAfter(final int timeout) {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
                Session session = request.attribute(Session.class);
                session.put("written", true);
                session.maxAge(timeout);
            }
        };
    }
}
