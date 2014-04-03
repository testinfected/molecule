package com.vtence.molecule.support;

import com.vtence.molecule.BinaryBody;
import com.vtence.molecule.Body;
import com.vtence.molecule.Cookie;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.TextBody;
import com.vtence.molecule.simple.SimpleResponse;
import com.vtence.molecule.util.Charsets;
import com.vtence.molecule.util.HttpDate;
import org.hamcrest.Matcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class MockResponse extends SimpleResponse {

    private final Map<String, String> headers = new HashMap<String, String>();
    private final Map<String, Cookie> cookies = new HashMap<String, Cookie>();

    private Body body = BinaryBody.empty();

    public MockResponse() {
        super(null);
    }

    public static MockResponse aResponse() {
        return new MockResponse();
    }

    public MockResponse withStatus(HttpStatus status) {
        status(status);
        return this;
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

    public void redirectTo(String location) {
        header("Location", location);
    }

    public void assertRedirectedTo(String expectedLocation) {
        assertThat("redirection", header("Location"), equalTo(expectedLocation));
    }

    public boolean hasHeader(String name) {
        return header(name) != null;
    }

    public void header(String name, String value) {
        headers.put(name, value);
    }

    public void headerDate(String name, long date) {
        header(name, HttpDate.format(date));
    }

    public String header(String name) {
        return headers.get(name);
    }

    public void removeHeader(String name) {
        headers.remove(name);
    }

    public void cookie(Cookie cookie) {
        cookies.put(cookie.name(), cookie);
    }

    public void assertNoHeader(String name) {
        assertHeader(name, nullValue());
    }

    public void assertHeader(String name, String value) {
        assertHeader(name, equalTo(value));
    }

    public void assertHeader(String name, Matcher<? super String> valueMatcher) {
        assertThat(name, header(name), valueMatcher);
    }

    public void contentType(String contentType) {
        header("Content-Type", contentType);
    }

    public String contentType() {
        return header("Content-Type");
    }

    public void assertContentType(String contentType) {
        assertContentType(equalTo(contentType));
    }

    public void assertContentType(Matcher<? super String> contentTypeMatcher) {
        assertHeader("Content-Type", contentTypeMatcher);
    }

    public long contentLength() {
        // todo should return -1 if content length unknown
        return header("Content-Length") != null ? parseLong(header("Content-Length")) : 0;
    }

    public void contentLength(long length) {
        header("Content-Length", valueOf(length));
    }

    public Charset charset() {
        if (contentType() == null) return Charsets.ISO_8859_1;
        Charset charset = parseCharset(contentType());
        return charset != null ? charset : Charsets.ISO_8859_1;
    }

    public void body(String text) throws IOException {
        body(TextBody.text(text));
    }

    public void body(Body body) throws IOException {
        this.body = body;
    }

    public Body body() {
        return body;
    }

    public long size() {
        return body.size(charset());
    }

    public boolean empty() {
        return size() == 0;
    }

    public String text() {
        return new String(content(), charset());
    }

    public void assertBody(String body) {
        assertBody(equalTo(body));
    }

    public void assertBody(Matcher<? super String> bodyMatcher) {
        assertThat("body", text(), bodyMatcher);
    }

    public byte[] content() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            body.writeTo(out, charset());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return out.toByteArray();
    }

    public void assertContent(byte[] content) {
        assertArrayEquals("content", content, content());
    }

    public InputStream stream() {
        return new ByteArrayInputStream(content());
    }

    public long contentSize() {
        return content().length;
    }

    public void assertContentSize(long size) {
        assertThat("content size", contentSize(), is(size));
    }

    public void assertContentEncodedAs(String encoding) throws IOException {
        assertThat("content encoding", CharsetDetector.detectedCharset(content()).toLowerCase(), containsString(encoding.toLowerCase()));
    }

    public <T> T unwrap(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public MockResponse withContentType(String contentType) {
        contentType(contentType);
        return this;
    }

    public Cookie cookie(String name) {
        return cookies.get(name);
    }

    public void assertHasCookie(String name) {
        assertCookie(name, notNullValue());
    }

    public void assertCookie(String name, Matcher<? super Cookie> matches) {
        assertThat("cookies ", cookies, hasKey(name));
        assertThat(name, cookie(name), matches);
    }

    public String toString() {
        // todo add headers and status as well
        return text();
    }

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