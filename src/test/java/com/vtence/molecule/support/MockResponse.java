package com.vtence.molecule.support;

import com.vtence.molecule.Cookie;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.simple.SimpleResponse;
import com.vtence.molecule.util.Charsets;
import org.hamcrest.Matcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.vtence.molecule.HttpHeaders.LOCATION;
import static com.vtence.molecule.support.CharsetDetector.detectedCharset;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class MockResponse extends SimpleResponse {

    private final Map<String, Cookie> cookies = new HashMap<String, Cookie>();

    public MockResponse() {
        super(null);
    }

    public void assertStatusCode(int code) {
        assertThat("status code", statusCode(), equalTo(code));
    }

    public void assertStatusText(String text) {
        assertThat("status text", statusText(), equalTo(text));
    }

    public void assertStatus(HttpStatus expected) {
        assertStatusCode(expected.code);
        assertStatusText(expected.text);
    }

    public void assertRedirectedTo(String expectedLocation) {
        assertThat("redirection", get(LOCATION), equalTo(expectedLocation));
    }

    public void assertHeader(String name, String value) {
        assertHeader(name, equalTo(value));
    }

    public void assertHeader(String name, Matcher<? super String> valueMatcher) {
        assertThat(name, get(name), valueMatcher);
    }

    public void assertNoHeader(String name) {
        assertHeader(name, nullValue());
    }

    public void assertContentType(String contentType) {
        assertContentType(equalTo(contentType));
    }

    public void assertContentType(Matcher<? super String> contentTypeMatcher) {
        assertHeader("Content-Type", contentTypeMatcher);
    }

    public void assertHasCookie(String name) {
        assertCookie(name, notNullValue());
    }

    public void assertCookie(String name, Matcher<? super Cookie> matches) {
        assertThat("cookies ", cookies, hasKey(name));
        assertThat(name, cookie(name), matches);
    }

    public void assertBody(String body) {
        assertBody(equalTo(body));
    }

    public void assertBody(Matcher<? super String> bodyMatcher) {
        assertThat("body", text(), bodyMatcher);
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

    public Cookie cookie(String name) {
        return cookies.get(name);
    }

    public void cookie(Cookie cookie) {
        cookies.put(cookie.name(), cookie);
    }

    public Charset charset() {
        if (contentType() == null) return Charsets.ISO_8859_1;
        Charset charset = parseCharset(contentType());
        return charset != null ? charset : Charsets.ISO_8859_1;
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

    public <T> T unwrap(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public MockResponse withContentType(String contentType) {
        contentType(contentType);
        return this;
    }

    public MockResponse withStatus(HttpStatus status) {
        status(status);
        return this;
    }

    public String toString() {
        // todo add headers and status as well
        return text();
    }

    // todo Extract a MediaType class
    private static final String TYPE = "[^/]+";
    private static final String SUBTYPE = "[^;]+";
    private static final String CHARSET = "charset=([^;]+)";
    private static final Pattern CONTENT_TYPE_FORMAT = Pattern.compile(String.format("%s/%s(?:;\\s*%s)+", TYPE, SUBTYPE, CHARSET));

    private static final int ENCODING = 1;

    private static Charset parseCharset(String contentType) {
        java.util.regex.Matcher matcher = CONTENT_TYPE_FORMAT.matcher(contentType);
        if (!matcher.matches()) return null;
        return Charset.forName(matcher.group(ENCODING));
    }
}