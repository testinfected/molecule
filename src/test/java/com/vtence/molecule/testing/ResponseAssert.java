package com.vtence.molecule.testing;

import com.vtence.molecule.Response;
import com.vtence.molecule.http.HeaderNames;
import com.vtence.molecule.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.Assert;

import java.nio.charset.Charset;

import static com.vtence.molecule.http.HeaderNames.CONTENT_TYPE;
import static com.vtence.molecule.testing.CharsetDetector.detectCharsetOf;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class ResponseAssert {

    private final Response response;

    protected ResponseAssert(Response response) {
        this.response = response;
    }

    public static ResponseAssert assertThat(Response response) {
        return new ResponseAssert(response);
    }

    public ResponseAssert hasStatusCode(int code) {
        hasStatusCode(is(code));
        return this;
    }

    public ResponseAssert hasStatusCode(Matcher<? super Integer> matching) {
        Assert.assertThat("response status code", response.statusCode(), matching);
        return this;
    }

    public ResponseAssert hasStatusText(String text) {
        hasStatusText(is(text));
        return this;
    }

    public ResponseAssert hasStatusText(Matcher<? super String> matching) {
        Assert.assertThat("response status text", response.statusText(), matching);
        return this;
    }

    public ResponseAssert hasStatus(HttpStatus expected) {
        hasStatusCode(expected.code);
        hasStatusText(expected.text);
        return this;
    }

    public ResponseAssert isRedirectedTo(String location) {
        isRedirectedTo(equalTo(location));
        return this;
    }

    public ResponseAssert isRedirectedTo(Matcher<? super String> matching) {
        hasHeader(HeaderNames.LOCATION, matching);
        return this;
    }

    public ResponseAssert hasHeader(String named) {
        hasHeader(named, any(String.class));
        return this;
    }

    public ResponseAssert hasHeader(String name, String value) {
        hasHeader(name, equalTo(value));
        return this;
    }

    public ResponseAssert hasHeader(String name, Matcher<? super String> matchingValue) {
        Assert.assertThat("response '" + name + "' header", response.header(name), matchingValue);
        return this;
    }

    public ResponseAssert hasHeaders(String name, Matcher<Iterable<? extends String>> matchingValues) {
        Assert.assertThat("response '" + name + "' headers", response.headers(name), matchingValues);
        return this;
    }

    public ResponseAssert hasNoHeader(String named) {
        hasHeader(named, nullValue());
        return this;
    }

    public ResponseAssert hasContentType(String contentType) {
        hasContentType(equalTo(contentType));
        return this;
    }

    public ResponseAssert hasContentType(Matcher<? super String> matching) {
        hasHeader(CONTENT_TYPE, matching);
        return this;
    }

    public ResponseAssert hasBodyText(String body) {
        return hasBodyText(equalTo(body));
    }

    public ResponseAssert hasBodyText(Matcher<? super String> matching) {
        Assert.assertThat("response body text", BodyContent.asText(response), matching);
        return this;
    }

    public ResponseAssert hasBodyContent(byte[] content) {
        Assert.assertArrayEquals("response body content", content, BodyContent.asBytes(response));
        return this;
    }

    public ResponseAssert hasBodySize(long byteCount) {
        return hasSize(is(byteCount));
    }

    public ResponseAssert hasSize(Matcher<? super Long> matching) {
        Assert.assertThat("response size", response.size(), matching);
        return this;
    }

    public ResponseAssert hasBodyEncoding(Charset charset) {
        return hasBodyEncoding(charset.name());
    }

    public ResponseAssert hasBodyEncoding(String encoding) {
        return hasBodyEncoding(equalTo(encoding));
    }

    public ResponseAssert hasBodyEncoding(Matcher<? super String> matching) {
        Assert.assertThat("response body encoding", detectCharsetOf(BodyContent.asBytes(response)), matching);
        return this;
    }

    public ResponseAssert isDone() {
        Assert.assertThat("response completed", response.isDone(), is(true));
        return this;
    }
}