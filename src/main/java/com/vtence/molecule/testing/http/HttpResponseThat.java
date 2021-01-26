package com.vtence.molecule.testing.http;

import com.vtence.molecule.http.ContentType;
import com.vtence.molecule.testing.CharsetDetector;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;

public class HttpResponseThat {
    public static Matcher<HttpResponse<String>> contentEncodedAs(Charset charset) {
        return contentEncodedWithCharset(equalTo(charset));
    }
    public static Matcher<HttpResponse<String>> contentEncodedWithCharset(Matcher<Charset> matching) {
        return new FeatureMatcher<>(matching, "content encoded as", "encoding") {
            @Override
            protected Charset featureValueOf(HttpResponse<String> actual) {
                return Charset.forName(CharsetDetector.detectCharsetOf(actual.body().getBytes(encodingOf(actual))));
            }

            private Charset encodingOf(HttpResponse<?> response) {
                return contentType(response).map(ContentType::parse)
                                            .map(ContentType::charset)
                                            .orElse(StandardCharsets.ISO_8859_1);
            }

            private Optional<String> contentType(HttpResponse<?> response) {
                return response.headers().firstValue("Content-Type");
            }
        };
    }
}
