package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class URLMapTest {

    URLMap map = new URLMap(new NotFound());
    Request request = new Request();
    Response response = new Response();

    @Test
    public void fallsBackToDefaultApplicationForUnmappedPaths() throws Exception {
        request.path("/unmapped");

        map.handle(request, response);
        response.done();

        assertThat(response).hasStatus(NOT_FOUND)
                            .hasBodyText("Not found: /unmapped");
    }

    @Test
    public void dispatchesBasedOnRequestPath() throws Exception {
        map.mount("/foo", this::describeMount)
           .mount("/baz", this::describeMount);

        map.handle(request.path("/baz/quux"), response);

        assertThat(response).hasStatus(OK).hasBodyText("/baz at /quux (/baz/quux)");
    }

    @Test
    public void matchesMountPointsAsWords() throws Exception {
        map.mount("/foo", (request, response) -> response.done("mounted!?!"));

        map.handle(request.path("/foobar"), response);

        assertThat(response).hasStatus(NOT_FOUND);
    }

    @Test
    public void dispatchesToServerRootCorrectly() throws Exception {
        map.mount("/", this::describeMount);

        map.handle(request.path("/"), response);
        assertThat(response).hasStatus(OK).hasBodyText("/ at / (/)");

        map.handle(request.path("/foo"), response);
        assertThat(response).hasStatus(OK).hasBodyText("/ at /foo (/foo)");
    }

    @Test
    public void dispatchesToImplicitMountRootCorrectly() throws Exception {
        map.mount("/foo", this::describeMount);

        map.handle(request.path("/foo"), response);

        assertThat(response).hasStatus(OK).hasBodyText("/foo at / (/foo)");
    }

    @Test
    public void dispatchesToExplicitMountRootCorrectly() throws Exception {
        map.mount("/foo", this::describeMount);

        map.handle(request.path("/foo/"), response);

        assertThat(response).hasStatus(OK).hasBodyText("/foo at / (/foo)");
    }

    @Test
    public void dispatchesToMostSpecificPath() throws Exception {
        map.mount("/foo", this::describeMount)
           .mount("/foo/bar", this::describeMount);

        map.handle(request.path("/foo/bar/quux"), response);

        assertThat(response).hasStatus(OK).hasBodyText("/foo/bar at /quux (/foo/bar/quux)");
    }

    public void describeMount(Request request, Response response) {
        URLMap.MountPoint mountPoint = request.attribute(URLMap.MountPoint.class);
        response.done(String.format("%s at %s (%s)", mountPoint.app(), request.path(), mountPoint.uri(request.path())));
    }
}
