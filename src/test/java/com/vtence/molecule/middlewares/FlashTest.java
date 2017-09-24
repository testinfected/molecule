package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.FlashHash;
import com.vtence.molecule.session.Session;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class FlashTest {

    Flash flash = new Flash();
    Session session = new Session();

    @Rule
    public ExpectedException error = ExpectedException.none();

    public Request withEmptyFlash(Request request) {
        session.put(FlashHash.class, new HashMap<>());
        session.bind(request);
        return request;
    }

    @Test
    public void createsFlashHashFromSessionValues() throws Throwable {
        Request request = withEmptyFlash(Request.get("/"));

        flashes().put("greeting", "Hello");
        flashes().put("farewell", "Goodbye");

        Response response = flash.then(this::printFlashContent)
                                 .handle(request);

        assertNoError(response);
        assertThat(response).hasBodyText("{greeting=Hello, farewell=Goodbye}");
    }

    @Test
    public void complainsIfNoSessionFound() throws Throwable {
        error.expect(IllegalStateException.class);
        flash.then(this::printFlashContent)
             .handle(Request.get("/"));
    }

    @Test
    public void createsFreshFlashHashWhenNoneExists() throws Throwable {
        Request request = withEmptyFlash(Request.get("/"));
        session.clear();

        Response response = flash.then(this::printFlashContent)
                                 .handle(request);

        assertNoError(response);
        assertThat(response).hasBodyText("{}");
    }

    @Test
    public void storesFreshFlashEntriesInSessionWhenDoneAndForgetAboutOldEntries() throws Throwable {
        Request request = withEmptyFlash(Request.get("/"));
        flashes().put("greeting", "Hello");

        Map<String, String> freshValues = new HashMap<>();
        freshValues.put("farewell", "Goodbye");

        Response response = flash.then(writeToFlash(freshValues))
                                 .handle(request);

        assertThat("old flashes", flashes(), nullValue());

        response.done();
        assertNoError(response);

        assertThat("fresh flashes count", flashes(), aMapWithSize(1));
        assertThat("fresh flashes", flashes(), hasEntry("farewell", "Goodbye"));
    }

    @Test
    public void doesNotWriteEmptyFlashToSession() throws Throwable {
        Request request = withEmptyFlash(Request.get("/"));

        Response response = flash.then(writeToFlash(emptyMap()))
                                 .handle(request);

        response.done();
        assertNoError(response);
        assertThat("done flashes", flashes(), nullValue());
    }

    @Test
    public void unbindsFlashFromRequestOnceDone() throws Throwable {
        Request request = withEmptyFlash(Request.get("/"));

        Response response = flash.then(this::printFlashContent)
                                 .handle(request);

        assertNoError(response);
        assertThat("flash", FlashHash.get(request), nullValue());
    }

    @Test
    public void unbindsFlashFromRequestWhenExceptionOccurs() throws Throwable {
        error.expectMessage("Internal error!");

        Request request = withEmptyFlash(Request.get("/"));
        try {
            flash.then(crashWith(new Exception("Internal error!")))
                 .handle(request);
        } finally {
            assertThat("flash", FlashHash.get(request), nullValue());
        }
    }

    @Test
    public void unbindsFlashFromRequestInCaseOfDeferredErrorAsWell() throws Throwable {
        Request request = withEmptyFlash(Request.get("/"));
        Response response = flash.then(this::printFlashContent)
                                 .handle(request);

        response.done(new Exception("Internal error!"));

        assertThat("flash", FlashHash.get(request), nullValue());
    }

    @Test
    public void complainsIfSessionRemovedDuringProcessing() throws Throwable {
        error.expect(IllegalStateException.class);

        Request request = withEmptyFlash(Request.get("/"));
        Response response = flash.then(r -> {
            Session.unbind(request);
            return Response.ok().done();
        }).handle(request);

        assertNoError(response);
    }

    private Application crashWith(Exception error) {
        return Application.of(request -> {
            throw error;
        });
    }

    private Application writeToFlash(Map<String, String> values) {
        return Application.of(request -> {
            FlashHash flash = FlashHash.get(request);
            assertThat("request flash", flash, notNullValue());

            flash.putAll(values);
            return Response.ok();
        });
    }

    private Response printFlashContent(Request request) throws Exception {
        FlashHash flash = FlashHash.get(request);
        assertThat("request flash", flash, notNullValue());
        return Response.ok().done(flash.toMap().toString());
    }

    private Map<String, String> flashes() {
        return session.get(FlashHash.class);
    }

    private void assertNoError(Response response) throws Throwable {
        awaitCompletion(response);
    }

    private void awaitCompletion(Response response) throws Throwable {
        try {
            response.await();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
}
