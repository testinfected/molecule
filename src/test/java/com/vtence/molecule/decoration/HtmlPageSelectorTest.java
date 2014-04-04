package com.vtence.molecule.decoration;

import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.support.MockResponse;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class HtmlPageSelectorTest {

    Selector selector = new HtmlPageSelector();

    MockResponse response = new MockResponse();

    @Test public void
    selectsContentOfTypeTextHtmlWhenResponseIsOk() throws IOException {
        response.withContentType("text/html; charset=iso-8859-1").withStatus(HttpStatus.OK);
        assertThat("content selection", selector.selected(response), equalTo(true));
    }

    @Test public void
    doesNotSelectContentIfNotHtml() throws IOException {
        response.withContentType("text/plain").withStatus(HttpStatus.OK);
        assertThat("content selection", selector.selected(response), equalTo(false));
    }

    @Test public void
    doesNotSelectContentWhenStatusNotOK() throws IOException {
        response.withStatus(HttpStatus.SEE_OTHER);
        assertThat("content selection", selector.selected(response), equalTo(false));
    }

    @Test public void
    doesNotSelectResponseWithoutContentType() throws IOException {
        response.withStatus(HttpStatus.OK);
        assertThat("content selection", selector.selected(response), equalTo(false));
    }
}
