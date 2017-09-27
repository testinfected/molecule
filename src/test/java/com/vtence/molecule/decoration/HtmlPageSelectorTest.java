package com.vtence.molecule.decoration;

import com.vtence.molecule.Response;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.http.HttpStatus.SEE_OTHER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class HtmlPageSelectorTest {

    Selector selector = new HtmlPageSelector();

    @Test public void
    selectsContentOfTypeTextHtmlWhenResponseIsOk() throws IOException {
        Response response = Response.ok()
                                    .contentType("text/html; charset=iso-8859-1");
        assertThat("content selection", selector.selected(response), equalTo(true));
    }

    @Test public void
    doesNotSelectContentIfNotHtml() throws IOException {
        Response response = Response.ok()
                                    .contentType("text/plain");
        assertThat("content selection", selector.selected(response), equalTo(false));
    }

    @Test public void
    doesNotSelectContentWhenStatusNotOK() throws IOException {
        Response response = Response.of(SEE_OTHER);
        assertThat("content selection", selector.selected(response), equalTo(false));
    }

    @Test public void
    doesNotSelectResponseWithoutContentType() throws IOException {
        Response response = Response.ok();
        assertThat("content selection", selector.selected(response), equalTo(false));
    }
}