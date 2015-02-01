package com.vtence.molecule.support;

import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.http.HttpStatus;
import com.vtence.molecule.Response;
import org.hamcrest.Matcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.vtence.molecule.support.CharsetDetector.detectedCharset;
import static com.vtence.molecule.support.ResponseAssertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class MockResponse extends Response {

    /**
     * @see ResponseAssertions#hasStatusCode(int)
     */
    @Deprecated
    public void assertStatusCode(int code) {
        assertThat(this).hasStatusCode(code);
    }

    /**
     * @see ResponseAssertions#hasStatusText(String)
     */
    @Deprecated
    public void assertStatusText(String text) {
        assertThat(this).hasStatusText(text);
    }

    /**
     * @see ResponseAssertions#hasStatus(com.vtence.molecule.http.HttpStatus)
     */
    @Deprecated
    public void assertStatus(HttpStatus expected) {
        assertThat(this).hasStatus(expected);
    }

    /**
     * @see ResponseAssertions#isRedirectedTo(String)
     */
    @Deprecated
    public void assertRedirectedTo(String location) {
        assertThat(this).isRedirectedTo(location);
    }

    /**
     * @see ResponseAssertions#hasHeader(String, String)
     */
    @Deprecated
    public void assertHeader(String name, String value) {
        assertThat(this).hasHeader(name, value);
    }

    /**
     * @see ResponseAssertions#hasHeader(String, org.hamcrest.Matcher)
     */
    @Deprecated
    public void assertHeader(String name, Matcher<? super String> valueMatching) {
        assertThat(this).hasHeader(name, valueMatching);
    }

    /**
     * @see ResponseAssertions#hasNoHeader(String)
     */
    @Deprecated
    public void assertNoHeader(String name) {
        assertThat(this).hasNoHeader(name);
    }

    /**
     * @see ResponseAssertions#hasContentType(String)
     */
    @Deprecated
    public void assertContentType(String contentType) {
        assertThat(this).hasContentType(contentType);
    }

    /**
     * @see ResponseAssertions#hasContentType(org.hamcrest.Matcher)
     */
    @Deprecated
    public void assertContentType(Matcher<? super String> matching) {
        assertThat(this).hasContentType(matching);
    }

    /**
     * @see ResponseAssertions#hasCookie(String)
     */
    @Deprecated
    public void assertHasCookie(String name) {
        assertThat(this).hasCookie(name);
    }

    /**
     * @see ResponseAssertions#hasNoCookie(String)
     */
    @Deprecated
    public void assertHasNoCookie(String name) {
        assertThat(this).hasNoCookie(name);
    }

    /**
     * @see ResponseAssertions#hasCookie(String)
     */
    @Deprecated
    public void assertCookie(String name, Matcher<? super Cookie> matching) {
        assertThat(name, cookie(name), matching);
    }

    public void assertBody(String body) {
        assertBody(equalTo(body));
    }

    public void assertBody(Matcher<? super String> matching) {
        assertThat("body", text(), matching);
    }

    public void assertContent(byte[] content) {
        assertArrayEquals("content", content, content());
    }

    public void assertContentSize(long size) {
        assertThat("content size", contentSize(), is(size));
    }

    public void assertContentEncodedAs(String encoding) throws IOException {
        assertThat("content encoding", detectedCharset(content()).toLowerCase(), containsString(encoding.toLowerCase()));
    }

    public byte[] content() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            body().writeTo(out, charset());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return out.toByteArray();
    }

    public InputStream stream() {
        return new ByteArrayInputStream(content());
    }

    public String text() {
        return new String(content(), charset());
    }

    public long contentSize() {
        return content().length;
    }

    public String toString() {
        // todo add headers and status as well
        return text();
    }
}