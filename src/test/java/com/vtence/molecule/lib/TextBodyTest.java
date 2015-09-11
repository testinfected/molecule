package com.vtence.molecule.lib;

import com.vtence.molecule.Response;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TextBodyTest {

    TextBody body = new TextBody();
    Response response = new Response();

    @Test public void
    appendsAndRendersText() throws IOException {
        body.append("The entire");
        body.append(" text");
        body.append(" body");
        response.body(body);
        assertThat(response).hasBodyText("The entire text body")
                            .hasBodySize(body.size(ISO_8859_1));
    }

    @Test public void
    usesSpecifiedTextEncoding() throws IOException {
        body.append("De drôles d'œufs abîmés");
        response.contentType("text/plain; charset=UTF-8");
        response.body(body);
        assertThat(response).hasBodyEncoding(UTF_8)
                            .hasBodySize(body.size(UTF_8));
    }
}