package examples.session;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.Delorean;
import com.vtence.molecule.support.StackTrace;
import com.vtence.molecule.testing.http.Form;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SessionTest {

    Delorean delorean = new Delorean();
    SessionExample sessions = new SessionExample(delorean);
    WebServer server = WebServer.create(9999);

    Throwable error;
    // The default session cookie name, it can be set to something different
    String SESSION_COOKIE = "molecule.session";
    int FIVE_MIN = 300;

    CookieManager cookies = new CookieManager();
    HttpClient client = HttpClient.newBuilder().cookieHandler(cookies).build();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

    @Before
    public void startServer() throws IOException {
        sessions.run(server);
        server.failureReporter(e -> error = e);
    }

    @After
    public void stopServer() throws IOException {
        delorean.back();
        server.stop();
    }

    @Test
    public void accessingTheSessionWithoutCreatingANewSession() throws Exception {
        var response = client.send(request.GET().build(), ofString());
        assertNoError();
        assertThat(response).hasNoCookie(SESSION_COOKIE)
                            .hasBody("Hello, Guest");
    }

    @Test
    public void trackingASessionAcrossRequests() throws Exception {
        var ok = client.send(request.copy().uri(server.uri().resolve("/login"))
                                       .header("Content-Type", Form.urlEncoded().contentType())
                                       .POST(Form.urlEncoded().addField("username", "Vincent"))
                                       .build(), ofString());

        assertNoError();

        var response = client.send(request.copy().GET().build(), ofString());
        assertNoError();
        assertThat(response).hasBody("Hello, Vincent");
    }

    @Test
    public void creatingATransientSessionCookie() throws Exception {
        var response = client.send(request.copy().uri(server.uri().resolve("/login"))
                                          .header("Content-Type", Form.urlEncoded().contentType())
                                          .POST(Form.urlEncoded().addField("username", "Vincent"))
                                          .build(), ofString());
        assertNoError();
        assertThat(response).hasCookie(SESSION_COOKIE).hasMaxAge(-1);
    }

    @Test
    public void creatingAPersistentSessionCookieWhichExpiresAfterFiveMinutes() throws Exception {
        var response = client.send(request.copy().uri(server.uri().resolve("/login"))
                                          .header("Content-Type", Form.urlEncoded().contentType())
                                          .POST(Form.urlEncoded()
                                                    .addField("username", "Vincent")
                                                    .addField("remember_me", "true"))
                                          .build(), ofString());

        assertNoError();
        assertThat(response).hasCookie(SESSION_COOKIE).hasMaxAge(FIVE_MIN);

        // Play the same request again and include the cookie
        var again = client.send(request.copy().build(), ofString());
        assertNoError();
        // ... the cookie will have been refreshed
        assertThat(again).hasCookie(SESSION_COOKIE).hasMaxAge(FIVE_MIN);
    }

    @Test
    public void deletingASession() throws Exception {
        var response = client.send(request.copy().uri(server.uri().resolve("/login"))
                                          .header("Content-Type", Form.urlEncoded().contentType())
                                          .POST(Form.urlEncoded()
                                                    .addField("username", "Vincent")
                                                    .addField("remember_me", "true"))
                                          .build(), ofString());
        assertNoError();
        assertThat(response).hasCookie(SESSION_COOKIE);

        var delete = client.send(request.copy().uri(server.uri().resolve("/logout")).DELETE().build(), ofString());
        assertNoError();
        // Session cookie should be expired
        assertThat(delete).hasCookie(SESSION_COOKIE).hasMaxAge(0);

        var get = client.send(request.copy().build(), ofString());
        assertNoError();
        // Back to being a guest
        assertThat(get).hasBody("Hello, Guest");
    }

    @Test
    public void
    attemptingToUseAnExpiredSession() throws Exception {
        client.send(request.copy().uri(server.uri().resolve("/login"))
                           .header("Content-Type", Form.urlEncoded().contentType())
                           .POST(Form.urlEncoded()
                                     .addField("username", "Vincent")
                                     .addField("remember_me", "true"))
                           .build(), ofString());
        assertNoError();

        delorean.travelInTime(SECONDS.toMillis(FIVE_MIN));

        var response = client.send(request.copy().build(), ofString());
        assertNoError();
        // Back to being a guest
        assertThat(response).hasNoCookie(SESSION_COOKIE)
                       .hasBody("Hello, Guest");
    }

    @Test
    public void
    renewingASessionToAvoidSessionFixation() throws Exception {
        var attack = client.send(request.copy().uri(server.uri().resolve("/login"))
                           .header("Content-Type", Form.urlEncoded().contentType())
                           .POST(Form.urlEncoded()
                                     .addField("username", "attacker"))
                           .build(), ofString());
        assertNoError();
        String fixatedSession = readSessionCookie();

        var response = client.send(request.copy().uri(server.uri().resolve("/login"))
                                          .header("Content-Type", Form.urlEncoded().contentType())
                                          .POST(Form.urlEncoded()
                                                    .addField("username", "Vincent")
                                                    .addField("remember_me", "true"))
                                          .build(), ofString());
        assertNoError();
        assertThat(response).hasCookie(SESSION_COOKIE);

        String sessionId = readSessionCookie();
        assertThat("new session id", sessionId, not(equalTo(fixatedSession)));
    }

    private String readSessionCookie() {
        var sessionId = cookies.getCookieStore().get(server.uri()).stream()
                       .filter(it -> it.getName().equals(SESSION_COOKIE))
                       .map(HttpCookie::getValue)
                       .findFirst()
                       .orElse(null);
        assertThat("session cookie", sessionId, notNullValue());
        return sessionId;
    }

    private void assertNoError() {
        if (error != null) throw new AssertionError(StackTrace.of(error));
    }
}
