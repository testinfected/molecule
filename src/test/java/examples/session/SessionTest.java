package examples.session;

import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import com.vtence.molecule.support.StackTrace;
import com.vtence.molecule.util.Delorean;
import com.vtence.molecule.util.FailureReporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.CombinableMatcher.both;
import static org.junit.Assert.fail;

public class SessionTest {

    String SESSION_COOKIE = "JSESSIONID";

    SimpleServer server = new SimpleServer(9999);
    Delorean delorean = new Delorean();
    Exception error;

    HttpRequest request = new HttpRequest().onPort(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        new SessionExample(delorean).run(server);
        server.reportErrorsTo(new FailureReporter() {
            public void errorOccurred(Exception e) {
                error = e;
            }
        });
    }

    @After
    public void stopServer() throws IOException {
        delorean.back();
        server.shutdown();
    }

    @Test public void
    onlyCreatesSessionsWhenWritten() throws IOException {
        response = request.get("/");
        assertNoError();
        response.assertHasNoCookie(SESSION_COOKIE);
        response.assertHasContent("Hello, Guest");
    }

    @Test public void
    maintainsSessionsAcrossRequestsUsingCookies() throws IOException {
        response = request.but().withParameter("username", "Vincent").followRedirects(false).post("/login");
        assertNoError();
        // Session cookie should expire in 300s
        response.assertHasCookie(both(containsString(SESSION_COOKIE)).and(containsString("max-age=300")));

        response = request.but().get("/");
        assertNoError();
        // Session cookie should have been refreshed
        response.assertHasCookie(both(containsString(SESSION_COOKIE)).and(containsString("max-age=300")));
        response.assertHasContent("Hello, Vincent");

        response = request.but().followRedirects(false).delete("/logout");
        assertNoError();
        // Session cookie should be expired
        response.assertHasCookie(both(containsString(SESSION_COOKIE)).and(containsString("max-age=0")));

        response = request.get("/");
        assertNoError();
        response.assertHasNoCookie(SESSION_COOKIE);
        response.assertHasContent("Hello, Guest");
    }

    @Test public void
    expiresSessionsAfterTimeout() throws Exception {
        response = request.withParameter("username", "Vincent").post("/login");
        assertNoError();

        delorean.travelInTime(TimeUnit.SECONDS.toMillis(300));
        response = request.but().removeParameters().get("/");
        assertNoError();
        response.assertHasNoCookie(SESSION_COOKIE);
        response.assertHasContent("Hello, Guest");
    }

    private void assertNoError() {
        if (error != null) fail(StackTrace.of(error));
    }
}
