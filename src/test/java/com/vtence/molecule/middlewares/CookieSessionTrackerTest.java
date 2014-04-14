package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Session;
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

import static com.vtence.molecule.support.CookieMatchers.cookieWithValue;
import static com.vtence.molecule.support.CookieMatchers.httpOnlyCookie;
import static org.hamcrest.Matchers.equalTo;

public class CookieSessionTrackerTest {

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();

    SessionStore store = context.mock(SessionStore.class);
    CookieSessionTracker tracker = new CookieSessionTracker(store);

    MockRequest request = new MockRequest();
    MockResponse response = new MockResponse();

    @Before public void
    stubSessionStore() {
        context.checking(new Expectations() {{
            allowing(store).load("existing-session"); will(returnValue(new SessionHash("existing-session")));
            allowing(store).load("expired-session"); will(returnValue(null));
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
    createsANewCookieAndStoresNewSessionIfNewlyCreatedSessionContainsData() throws Exception {
        tracker.connectTo(incrementCounter());
        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new-session"));
        }});

        tracker.handle(request, response);
        response.assertBody("Counter: 1");
        response.assertCookie("JSESSIONID", cookieWithValue("new-session"));
        response.assertCookie("JSESSIONID", httpOnlyCookie(true));
    }

    @Test public void
    tracksExistingSessionsUsingACookieAndSavesSessionIfModified() throws Exception {
        tracker.connectTo(incrementCounter());

        Session clientSession = store.load("existing-session");
        clientSession.put("counter", 1);
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithId("existing-session")));
            will(returnValue("existing-session"));
        }});

        tracker.handle(request.withCookie("JSESSIONID", "existing-session"), response);
        response.assertBody("Counter: 2");
    }

    @Test public void
    savesExistingSessionEvenIfNotWritten() throws Exception {
        tracker.connectTo(nothing());
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithId("existing-session"))); will(returnValue("existing-session"));
        }});

        tracker.handle(request.withCookie("JSESSIONID", "existing-session"), response);
    }

    @Test public void
    createsAFreshSessionIfClientSessionHasExpired() throws Exception {
        tracker.connectTo(incrementCounter());

        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new-session"));
        }});

        tracker.handle(request.withCookie("JSESSIONID", "expired-session"), response);
        response.assertBody("Counter: 1");
        response.assertCookie("JSESSIONID", cookieWithValue("new-session"));
    }

    @Test public void
    doesNotSendTheSameSessionIdIfItDidNotChange() throws Exception {
        tracker.connectTo(nothing());
        context.checking(new Expectations() {{
            allowing(store).save(with(sessionWithId("existing-session"))); will(returnValue("existing-session"));
        }});

        tracker.handle(request.withCookie("JSESSIONID", "existing-session"), response);
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
            oneOf(store).destroy(with("existing-session"));
        }});

        tracker.handle(request.withCookie("JSESSIONID", "existing-session"), response);
        response.assertHasNoCookie("JSESSIONID");
    }

    private FeatureMatcher<Session, String> newSession() {
        return sessionWithId(null);
    }

    private FeatureMatcher<Session, String> sessionWithId(String sessionId) {
        return new FeatureMatcher<Session, String>(equalTo(sessionId), "session with id",
                "session id") {
            protected String featureValueOf(Session actual) {
                return actual.id();
            }
        };
    }

    private Incrementor incrementCounter() {
        return new Incrementor();
    }

    private Nothing nothing() {
        return new Nothing();
    }

    private Echo echoSessionId() {
        return new Echo();
    }

    private static class Incrementor implements Application {
        public void handle(Request request, Response response) throws Exception {
            Session session = request.attribute(Session.class);
            Integer counter = session.contains("counter") ? session.<Integer>get("counter") : 0;
            session.put("counter", counter++);
            response.body("Counter: " + counter);
        }
    }

    private static class Echo implements Application {
        public void handle(Request request, Response response) throws Exception {
            Session session = request.attribute(Session.class);
            response.body("Session: " + session.id());
        }
    }

    private static class Nothing implements Application {
        public void handle(Request request, Response response) throws Exception {
        }
    }
}
