package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class URLMapTest {

    URLMap map = new URLMap();

    @Test
    public void fallsBackToApplicationForUnmappedPaths() throws Exception {
        Response response = map.then(new NotFound())
                               .handle(Request.get("/unmapped"));

        assertThat(response).hasStatus(NOT_FOUND)
                            .hasBodyText("Not found: /unmapped");
    }

    @Test
    public void dispatchesBasedOnRequestPath() throws Exception {
        map.mount("/foo", describeMount())
           .mount("/baz", describeMount());

        Response response = map.then(ok())
                               .handle(Request.get("/baz/quux"));

        assertThat(response).hasStatus(OK)
                            .hasBodyText("/baz at /quux (/baz/quux)");
    }

    @Test
    public void matchesMountPointsAsWords() throws Exception {
        map.mount("/foo", Application.of(request -> Response.ok().done("mounted!?!")));

        Response response = map.then(new NotFound())
                               .handle(Request.get("/foobar"));

        assertThat(response).hasStatus(NOT_FOUND);
    }

    @Test
    public void dispatchesToServerRootCorrectly() throws Exception {
        map.mount("/", describeMount());

        Response response = map.then(ok())
                               .handle(Request.get("/"));
        assertThat(response).hasStatus(OK)
                            .hasBodyText("/ at / (/)");

        response = map.then(ok())
                      .handle(Request.get("/foo"));
        assertThat(response).hasStatus(OK)
                            .hasBodyText("/ at /foo (/foo)");
    }

    @Test
    public void dispatchesToImplicitMountRootCorrectly() throws Exception {
        map.mount("/foo", describeMount());

        Response response = map.then(ok())
                               .handle(Request.get("/foo"));

        assertThat(response).hasStatus(OK)
                            .hasBodyText("/foo at / (/foo)");
    }

    @Test
    public void dispatchesToExplicitMountRootCorrectly() throws Exception {
        map.mount("/foo", describeMount());

        Response response = map.then(ok())
                               .handle(Request.get("/foo/"));

        assertThat(response).hasStatus(OK)
                            .hasBodyText("/foo at / (/foo)");
    }

    @Test
    public void dispatchesToMostSpecificPath() throws Exception {
        map.mount("/foo", describeMount())
           .mount("/foo/bar", describeMount());

        Response response = map.then(ok())
                               .handle(Request.get("/foo/bar/quux"));

        assertThat(response).hasStatus(OK)
                            .hasBodyText("/foo/bar at /quux (/foo/bar/quux)");
    }

    private Application ok() {
        return request -> Response.ok().done();
    }

    public Application describeMount() {
        return Application.of(request -> {
            URLMap.MountPoint mountPoint = request.attribute(URLMap.MountPoint.class);
            return Response.ok()
                           .done(String.format("%s at %s (%s)",
                                               mountPoint.app(),
                                               request.path(),
                                               mountPoint.uri(request.path())));
        });
    }
}
