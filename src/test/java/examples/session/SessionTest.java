package examples.session;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import com.vtence.molecule.support.StackTrace;
import com.vtence.molecule.util.Delorean;
import com.vtence.molecule.FailureReporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

public class SessionTest {

    Delorean delorean = new Delorean();
    SessionExample sessions = new SessionExample(delorean);
    WebServer server = WebServer.create(9999);

    Throwable error;
    String SESSION_COOKIE = "JSESSIONID"; // The default session cookie name is the standard servlet cookie
    int FIVE_MIN = 300;

    HttpRequest request = new HttpRequest(9999).followRedirects(false);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        sessions.run(server);
        server.failureReporter(new FailureReporter() {
            public void errorOccurred(Throwable e) {
                error = e;
            }
        });
    }

    @After
    public void stopServer() throws IOException {
        delorean.back();
        server.stop();
    }

    @Test
    public void readingButNotCreatingASession() throws IOException {
        response = request.get("/");
        assertNoError();
        response.assertHasNoCookie(SESSION_COOKIE);
        response.assertHasContent("Hello, Guest");
    }

    @Test
    public void creatingAPersistentSession() throws IOException {
        response = request.withParameter("username", "Vincent").post("/login");
        assertNoError();
        response.assertHasCookie(SESSION_COOKIE, not(containsString("max-age")));
        response.assertHasCookie(SESSION_COOKIE, not(containsString("expire-after")));
    }

    @Test
    public void noteThatPersistentSessionCookiesAreNotRefreshed() throws IOException {
        response = request.withParameter("username", "Vincent").post("/login");
        assertNoError();
        // Play the same request again
        response = request.send();
        assertNoError();
        // Cookie needs no refresh
        response.assertHasNoCookie(SESSION_COOKIE);
    }

    @Test
    public void creatingASessionWhichExpires() throws IOException {
        sessions.expireAfter(FIVE_MIN);

        response = request.withParameter("username", "Vincent").post("/login");
        assertNoError();
        response.assertHasCookie(SESSION_COOKIE, containsString("max-age=" + FIVE_MIN));

        // Play the same request again...
        response = request.send();
        assertNoError();
        // ... cookie will be refreshed
        response.assertHasCookie(SESSION_COOKIE, containsString("max-age=" + FIVE_MIN));
    }

    @Test
    public void trackingASessionAcrossRequests() throws IOException {
        response = request.but().withParameter("username", "Vincent").post("/login");
        assertNoError();

        response = request.but().get("/");
        assertNoError();
        response.assertHasContent("Hello, Vincent");
    }

    @Test
    public void deletingASession() throws IOException {
        response = request.but().withParameter("username", "Vincent").post("/login");
        assertNoError();
        response.assertHasCookie(SESSION_COOKIE);

        response = request.but().delete("/logout");
        assertNoError();
        // Session cookie should be expired
        response.assertHasCookie(SESSION_COOKIE, containsString("max-age=0"));

        response = request.but().get("/");
        assertNoError();
        response.assertHasContent("Hello, Guest");
    }

    @Test public void
    attemptingToUseAnExpiredSession() throws Exception {
        sessions.expireAfter(FIVE_MIN);

        response = request.but().withParameter("username", "Vincent").post("/login");
        assertNoError();

        delorean.travelInTime(SECONDS.toMillis(FIVE_MIN));
        response = request.but().get("/");
        assertNoError();
        response.assertHasNoCookie(SESSION_COOKIE);
        response.assertHasContent("Hello, Guest");
    }

    private void assertNoError() {
        if (error != null) fail(StackTrace.of(error));
    }
}
