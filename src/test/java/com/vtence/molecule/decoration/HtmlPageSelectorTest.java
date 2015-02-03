package com.vtence.molecule.decoration;

import com.vtence.molecule.Response;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.http.HttpStatus.SEE_OTHER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class HtmlPageSelectorTest {

    Selector selector = new HtmlPageSelector();

    Response response = new Response();

    @Test public void
    selectsContentOfTypeTextHtmlWhenResponseIsOk() throws IOException {
        response.contentType("text/html; charset=iso-8859-1").status(OK);
        assertThat("content selection", selector.selected(response), equalTo(true));
    }

    @Test public void
    doesNotSelectContentIfNotHtml() throws IOException {
        response.contentType("text/plain").status(OK);
        assertThat("content selection", selector.selected(response), equalTo(false));
    }

    @Test public void
    doesNotSelectContentWhenStatusNotOK() throws IOException {
        response.status(SEE_OTHER);
        assertThat("content selection", selector.selected(response), equalTo(false));
    }

    @Test public void
    doesNotSelectResponseWithoutContentType() throws IOException {
        response.status(OK);
        assertThat("content selection", selector.selected(response), equalTo(false));
    }
}