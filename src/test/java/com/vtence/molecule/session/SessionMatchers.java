package com.vtence.molecule.session;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.vtence.molecule.support.HasMethodWithValue.hasMethod;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;

public class SessionMatchers {
    public static Matcher<Session> sameSessionDataAs(Session data) {
        List<Matcher<? super Session>> matchers = new ArrayList<>();

        matchers.add(sessionWithId(data.id()));
        matchers.add(sessionCreatedAt(data.createdAt()));
        matchers.add(sessionUpdatedAt(data.updatedAt()));
        matchers.add(sessionWithMaxAge(data.maxAge()));

        matchers.addAll(data.keys().stream().map(key -> sessionWithSameAttributeAs(data, key)).collect(toList()));

        return new AllOf<>(matchers);
    }

    public static Matcher<Session> sessionWithId(String id) {
        return hasMethod("id", id);
    }

    public static Matcher<Session> sessionCreatedAt(Instant value) {
        return hasMethod("createdAt", value);
    }

    public static Matcher<Session> sessionUpdatedAt(Instant value) {
        return hasMethod("updatedAt", value);
    }

    public static Matcher<Session> sessionWithMaxAge(int maxAge) {
        return hasMethod("maxAge", maxAge);
    }

    public static FeatureMatcher<Session, ?> sessionWithSameAttributeAs(final Session data, final String key) {
        return new FeatureMatcher<Session, Object>(equalTo(data.get(key)), "session with attribute " + key, key) {
            protected Object featureValueOf(Session actual) {
                return actual.get(key);
            }
        };
    }
}
