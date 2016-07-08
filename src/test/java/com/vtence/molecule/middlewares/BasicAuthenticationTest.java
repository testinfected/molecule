package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.Authenticator;
import com.vtence.molecule.lib.MimeEncoder;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.BAD_REQUEST;
import static com.vtence.molecule.http.HttpStatus.UNAUTHORIZED;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static java.util.Optional.empty;

public class BasicAuthenticationTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    Authenticator authenticator = context.mock(Authenticator.class);

    BasicAuthentication authentication = new BasicAuthentication("WallyWorld", authenticator);
    MimeEncoder mime = MimeEncoder.inUtf8();

    Request request = new Request();
    Response response = new Response();

    @Test
    public void
    issuesAChallengeWhenNoCredentialsAreSpecified() throws Exception {
        authentication.handle(request, response);

        assertUnauthorized();
    }

    @Test
    public void
    rejectsUnsupportedAuthenticationSchemes() throws Exception {
        authentication.handle(request.header("Authorization", "Unsupported Scheme"), response);

        assertThat(response).hasStatus(BAD_REQUEST).isDone();
    }

    @Test
    public void rejectsInvalidCredentials() throws Exception {
        context.checking(new Expectations() {{
            oneOf(authenticator).authenticate("email", "bad secret"); will(returnValue(empty()));
        }});

        authentication.handle(request.header("Authorization", "Basic " + mime.encode("email:bad secret")), response);

        assertUnauthorized();
    }

    private void assertUnauthorized() {
        assertThat(response).hasStatus(UNAUTHORIZED)
                            .hasHeader("WWW-Authenticate", "Basic realm=\"WallyWorld\"")
                            .hasContentType("text/plain")
                            .isEmpty()
                            .isDone();
    }
}