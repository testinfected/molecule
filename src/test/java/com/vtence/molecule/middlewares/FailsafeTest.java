package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Before;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static com.vtence.molecule.support.ResponseAssertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class FailsafeTest {
    Failsafe failsafe = new Failsafe();

    String errorMessage = "An error occurred!";
    Error error = new Error(errorMessage) {{
        setStackTrace(new StackTraceElement[] {
                      new StackTraceElement("stack", "trace", "line", 1),
                      new StackTraceElement("stack", "trace", "line", 2)
        });
    }};

    Request request = new Request();
    Response response = new Response();

    @Before public void
    handleRequest() throws Exception {
        failsafe.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                throw error;
            }
        });
        failsafe.handle(request, response);
    }

    @Test public void
    setsStatusToInternalServerError() {
        assertThat(response).hasStatus(INTERNAL_SERVER_ERROR);
    }

    @Test public void
    rendersErrorTemplate() {
        assertThat(response).hasBodyText(containsString(errorMessage))
                            .hasBodyText(containsString("stack.trace(line:1)"))
                            .hasBodyText(containsString("stack.trace(line:2)"));
    }

    @Test public void
    respondsWithHtmlContentUtf8Encoded() {
        assertThat(response).hasContentType("text/html; charset=utf-8");
    }
}