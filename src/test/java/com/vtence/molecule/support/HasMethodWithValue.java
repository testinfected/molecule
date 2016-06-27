package com.vtence.molecule.support;

import org.hamcrest.Condition;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.lang.reflect.Method;

import static org.hamcrest.Condition.matched;
import static org.hamcrest.Condition.notMatched;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.beans.PropertyUtil.NO_ARGUMENTS;

public class HasMethodWithValue<T> extends TypeSafeDiagnosingMatcher<T> {
    private static final Condition.Step<Method, Method> WITH_READABLE_METHOD = readableMethod();
    private final String methodName;
    private final Matcher<Object> valueMatcher;

    public HasMethodWithValue(String methodName, Matcher<?> valueMatcher) {
        this.methodName = methodName;
        this.valueMatcher = nastyGenericsWorkaround(valueMatcher);
    }

    public boolean matchesSafely(T target, Description mismatch) {
        return methodOn(target, mismatch)
                .and(WITH_READABLE_METHOD)
                .and(withReturnValue(target))
                .matching(valueMatcher, "method " + methodName + " ");
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("has method ").appendValue(methodName).appendText(" with value ")
                   .appendDescriptionOf(valueMatcher);
    }

    private Method getMethod(Class<?> clazz, String name) {
        // first check up the superclass chain
        for (Class<?> each = clazz; each != null && each != Object.class; each = each.getSuperclass()) {
            Method candidate = getMethodOn(each, name);
            if (candidate != null) return candidate;
        }

        return null;
    }

    private Method getMethodOn(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredMethod(name);
        } catch (Exception notFound) {
        }

        return null;
    }

    private Condition<Method> methodOn(T target, Description mismatch) {
        Method method = getMethod(target.getClass(), methodName);
        if (method == null) {
            mismatch.appendText("No method \"" + methodName + "\"");
            return notMatched();
        }

        return matched(method, mismatch);
    }

    private static Condition.Step<Method, Method> readableMethod() {
        return (method, mismatch) -> {
            if (method.getReturnType().equals(void.class)) {
                mismatch.appendText("method \"" + method.getName() + "\" is not readable");
                return notMatched();
            }

            return matched(method, mismatch);
        };
    }

    private Condition.Step<Method, Object> withReturnValue(final T value) {
        return (method, mismatch) -> {
            try {
                return matched(method.invoke(value, NO_ARGUMENTS), mismatch);
            } catch (Exception e) {
                mismatch.appendText(e.getMessage());
                return notMatched();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static Matcher<Object> nastyGenericsWorkaround(Matcher<?> valueMatcher) {
        return (Matcher<Object>) valueMatcher;
    }

    public static <T> Matcher<T> hasMethod(String methodName, Object value) {
        return hasMethod(methodName, equalTo(value));
    }

    /**
     * Creates a matcher that matches when the examined object has a method
     * with the specified name whose value satisfies the specified matcher.
     *
     * @param methodName the name of the method to look for
     * @param valueMatcher a matcher for the return value of the specified method
     */
    public static <T> Matcher<T> hasMethod(String methodName, Matcher<?> valueMatcher) {
        return new HasMethodWithValue<T>(methodName, valueMatcher);
    }
}
