package com.vtence.molecule.lib;

import com.vtence.molecule.Response;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static com.vtence.molecule.testing.ResourceLocator.onClasspath;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileBodyTest {

    File base = onClasspath().locate("assets/images");
    File file = new File(base, "sample.png");

    @Test public void
    rendersFileContent() throws Exception {
        FileBody body = new FileBody(file);
        assertThat("file", body.file(), sameInstance(file));
        Response response = Response.ok().body(body);
        assertThat(response).hasBodySize(file.length())
                            .hasBodyContent(Files.readAllBytes(file.toPath()));
    }

}