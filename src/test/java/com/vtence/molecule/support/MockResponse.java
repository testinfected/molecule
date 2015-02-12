package com.vtence.molecule.support;

import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.http.HttpStatus;
import com.vtence.molecule.Response;
import com.vtence.molecule.testing.ResponseAssert;
import org.hamcrest.Matcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@Deprecated
public class MockResponse extends Response {

    /**
     * @see ResponseAssert#hasStatusCode(int)
     */
    @Deprecated
    public void assertStatusCode(int code) {
        assertThat(this).hasStatusCode(code);
    }

    /**
     * @see ResponseAssert#hasStatusText(String)
     */
    @Deprecated
    public void assertStatusText(String text) {
        assertThat(this).hasStatusText(text);
    }

    /**
     * @see ResponseAssert#hasStatus(com.vtence.molecule.http.HttpStatus)
     */
    @Deprecated
    public void assertStatus(HttpStatus expected) {
        assertThat(this).hasStatus(expected);
    }

    /**
     * @see ResponseAssert#isRedirectedTo(String)
     */
    @Deprecated
    public void assertRedirectedTo(String location) {
        assertThat(this).isRedirectedTo(location);
    }

    /**
     * @see ResponseAssert#hasHeader(String, String)
     */
    @Deprecated
    public void assertHeader(String name, String value) {
        assertThat(this).hasHeader(name, value);
    }

    /**
     * @see ResponseAssert#hasHeader(String, org.hamcrest.Matcher)
     */
    @Deprecated
    public void assertHeader(String name, Matcher<? super String> valueMatching) {
        assertThat(this).hasHeader(name, valueMatching);
    }

    /**
     * @see ResponseAssert#hasNoHeader(String)
     */
    @Deprecated
    public void assertNoHeader(String name) {
        assertThat(this).hasNoHeader(name);
    }

    /**
     * @see ResponseAssert#hasContentType(String)
     */
    @Deprecated
    public void assertContentType(String contentType) {
        assertThat(this).hasContentType(contentType);
    }

    /**
     * @see ResponseAssert#hasContentType(org.hamcrest.Matcher)
     */
    @Deprecated
    public void assertContentType(Matcher<? super String> matching) {
        assertThat(this).hasContentType(matching);
    }

    /**
     * @see ResponseAssert#hasCookie(String)
     */
    @Deprecated
    public void assertHasCookie(String name) {
        assertThat(this).hasCookie(name);
    }

    /**
     * @see ResponseAssert#hasNoCookie(String)
     */
    @Deprecated
    public void assertHasNoCookie(String name) {
        assertThat(this).hasNoCookie(name);
    }

    /**
     * @see ResponseAssert#hasCookie(String)
     */
    @Deprecated
    public void assertCookie(String name, Matcher<? super Cookie> matching) {
        assertThat(name, cookie(name), matching);
    }

    /**
     * @see ResponseAssert#hasBodyText(String)
     */
    @Deprecated
    public void assertBody(String body) {
        assertThat(this).hasBodyText(body);
    }

    /**
     * @see ResponseAssert#hasBodyText(org.hamcrest.Matcher)
     */
    @Deprecated
    public void assertBody(Matcher<? super String> matching) {
        assertThat(this).hasBodyText(matching);
    }

    /**
     * @see ResponseAssert#hasBodyContent(byte[])
     */
    @Deprecated
    public void assertContent(byte[] content) {
        assertThat(this).hasBodyContent(content);
    }

    /**
     * @see ResponseAssert#hasBodySize(long)
     */
    @Deprecated
    public void assertContentSize(long byteCount) {
        assertThat(this).hasBodySize(byteCount);
    }

    /**
     * @see ResponseAssert#hasBodyEncoding(java.nio.charset.Charset)
     */
    @Deprecated
    public void assertContentEncodedAs(String encoding) throws IOException {
        assertThat(this).hasBodyEncoding(containsString(encoding.toUpperCase()));
    }

    @Deprecated
    /**
     * @see BodyContent#asBytes(com.vtence.molecule.Response)
     */
    public byte[] content() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            body().writeTo(out, charset());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return out.toByteArray();
    }

    /**
     * @see BodyContent#asStream(com.vtence.molecule.Response)
     */
    @Deprecated
    public InputStream stream() {
        return new ByteArrayInputStream(content());
    }

    public String text() {
        return new String(content(), charset());
    }

    @Deprecated
    public long contentSize() {
        return content().length;
    }

    public String toString() {
        // todo add headers and status as well
        return text();
    }
}