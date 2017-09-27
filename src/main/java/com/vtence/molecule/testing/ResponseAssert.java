package com.vtence.molecule.testing;

import com.vtence.molecule.Response;
import com.vtence.molecule.http.HeaderNames;
import com.vtence.molecule.http.HttpStatus;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;

import java.nio.charset.Charset;
import java.util.Arrays;

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
        MatcherAssert.assertThat("response status code", response.statusCode(), matching);
        return this;
    }

    public ResponseAssert hasStatusDescription(String text) {
        hasStatusDescription(is(text));
        return this;
    }

    public ResponseAssert hasStatusDescription(Matcher<? super String> matching) {
        MatcherAssert.assertThat("response status description", response.statusDescription(), matching);
        return this;
    }

    public ResponseAssert hasStatus(HttpStatus expected) {
        hasStatusCode(expected.code);
        hasStatusDescription(expected.reason);
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
        MatcherAssert.assertThat("response '" + name + "' header", response.header(name), matchingValue);
        return this;
    }

    public ResponseAssert hasHeaders(String name, Matcher<Iterable<? extends String>> matchingValues) {
        MatcherAssert.assertThat("response '" + name + "' headers", response.headers(name), matchingValues);
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
        MatcherAssert.assertThat("response body text", BodyContent.asText(response), matching);
        return this;
    }

    public ResponseAssert hasBodyContent(byte[] content) {
        byte[] actual = BodyContent.asBytes(response);
        // Yep, I know, but it's good enough
        MatcherAssert.assertThat("response body byte stream", actual, new IsEqual<byte[]>(content) {
            public void describeTo(Description description) {
                description.appendValue(Arrays.toString(content));
            }

            public void describeMismatch(Object item, Description description) {
                description.appendText("was ").appendValue(Arrays.toString((byte[]) item));
            }
        });
        return this;
    }

    public ResponseAssert isEmpty() {
        return hasBodySize(0);
    }

    public ResponseAssert hasBodySize(long byteCount) {
        return hasSize(is(byteCount));
    }

    public ResponseAssert hasSize(Matcher<? super Long> matching) {
        MatcherAssert.assertThat("response size", response.size(), matching);
        return this;
    }

    public ResponseAssert hasBodyEncoding(Charset charset) {
        return hasBodyEncoding(charset.name());
    }

    public ResponseAssert hasBodyEncoding(String encoding) {
        return hasBodyEncoding(equalTo(encoding));
    }

    public ResponseAssert hasBodyEncoding(Matcher<? super String> matching) {
        MatcherAssert.assertThat("response body encoding", detectCharsetOf(BodyContent.asBytes(response)), matching);
        return this;
    }

    public ResponseAssert isDone() {
        MatcherAssert.assertThat("response completed", response.isDone(), is(true));
        return this;
    }
}