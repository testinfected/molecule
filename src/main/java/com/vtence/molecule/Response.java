package com.vtence.molecule;

import com.vtence.molecule.helpers.Headers;
import com.vtence.molecule.http.ContentType;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.http.HttpStatus;
import com.vtence.molecule.lib.BinaryBody;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LENGTH;
import static com.vtence.molecule.http.HeaderNames.CONTENT_TYPE;
import static com.vtence.molecule.http.HeaderNames.LOCATION;
import static com.vtence.molecule.http.HttpDate.httpDate;
import static com.vtence.molecule.http.HttpStatus.SEE_OTHER;
import static com.vtence.molecule.lib.BinaryBody.bytes;
import static com.vtence.molecule.lib.TextBody.text;
import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * The HTTP response to write back to the client.
 */
public class Response {
    private final CompletableFuture<Response> done = new CompletableFuture<>();
    private final Headers headers = new Headers();
    private final List<Cookie> cookies = new ArrayList<>();

    private CompletableFuture<Response> postProcessing = done;
    private int statusCode = HttpStatus.OK.code;
    private String statusText = HttpStatus.OK.text;
    private Body body = BinaryBody.empty();

    public Response() {}

    /**
     * Sets the HTTP status for this response. This will set both the status code and the status text.
     *
     * <p>
     * The status is set to 200 OK by default.
     * </p>
     *
     * @param status the HTTP status to set
     */
    public Response status(HttpStatus status) {
        statusCode(status.code);
        statusText(status.text);
        return this;
    }

    /**
     * Sets the status code for this response. It is usually preferable to set the status and text
     * together with Response#status(com.vtence.molecule.http.HttpStatus).
     *
     * @see Response#status(com.vtence.molecule.http.HttpStatus)
     * @param code the status code to set
     */
    public Response statusCode(int code) {
        statusCode = code;
        return this;
    }

    /**
     * Gets the status code set on this response.
     *
     * @return the response status code
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Sets the status text for this response. It is usually preferable to set the status and text
     * together with Response#status(com.vtence.molecule.http.HttpStatus).
     *
     * @see Response#status(com.vtence.molecule.http.HttpStatus)
     * @param text the status text to set
     */
    public Response statusText(String text) {
        statusText = text;
        return this;
    }

    /**
     * Gets the status text of this response.
     *
     * @return the response status text
     */
    public String statusText() {
        return statusText;
    }

    /**
     * Sends a SEE OTHER (303) redirect response to the client using the specified redirect location.
     *
     * @param location the url of the other location
     */
    public Response redirectTo(String location) {
        status(SEE_OTHER);
        header(LOCATION, location);
        return this;
    }

    /**
     * Checks if this response has a header of the specified name.
     *
     * @param name the header name
     * @return true is the header was found, false otherwise
     */
    public boolean hasHeader(String name) {
        return headers.has(name);
    }

    /**
     * Gets the names of all the headers to be sent with this response.
     * If the response has no header, the set will be empty.
     * <p>
     * Modifications to the returned set will not modify the response.
     * </p>
     * @return a (possibly empty) <code>Set</code> of header names to send
     */
    public Set<String> headerNames() {
        return headers.names();
    }

    /**
     * Gets the list of values of the specified header of this response. The name is case insensitive.
     *
     * @param name the name of the header to retrieve
     * @return the (possibly empty) list of header values
     */
    public List<String> headers(String name) {
        return headers.list(name);
    }

    /**
     * Gets the value of the specified header of this response. The name is case insensitive.
     *
     * <p>
     * In case there are multiple headers with that name, a comma separated list of values is returned.
     * </p>
     *
     * This method returns a null value if the response does not include any header of the specified name.
     *
     * @param name the name of the header to retrieve
     * @return the header value or null
     */
    public String header(String name) {
        return headers.get(name);
    }

    /**
     * Gets the value of the specified header as a <code>long</code>. The name is case insensitive.
     *
     * <p>
     * In case there are multiple headers with that name, a comma separated list of values is returned.
     * </p>
     *
     * This method returns -1 if the response does not include any header of the specified name.
     *
     * @param name the name of the header to retrieve
     * @return the long header value or -1
     * @throws java.lang.NumberFormatException is the header value cannot be converted to a <code>long</code>
     */
    public long headerAsLong(String name) {
        String value = header(name);
        return value != null ? parseLong(value) : -1;
    }

    /**
     * Adds a response header with the given name and value to this response.
     * <p>
     * This method allows response headers to have multiple values. The new value will be added to the list
     * of existing values for that header name.
     * </p>
     *
     * @param name the name of the header to send
     * @param value the additional value for that header
     */
    public Response addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    /**
     * Sets a header with the given name and value to be sent with this response.
     * If the header has already been set, the new value overwrites the previous one.
     * <p>
     * The {@link Response#hasHeader(String)} method can be used to test for the presence of the header before setting its value.
     * </p>
     *
     * @param name the name of the header to send
     * @param value the new value for that header
     */
    public Response header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Sets a header with the given name and date value to be sent with this response.
     * If the header has already been set, the new value overwrites the previous one.
     * <p>
     * The {@link Response#hasHeader(String)} method can be used to test for the presence of the header before setting its value.
     * </p>
     *
     * @param name the name of the header to send
     * @param value the new date value for that header
     */
    public Response header(String name, Instant value) {
        return header(name, httpDate(value));
    }

    /**
     * Sets a header with the given name and value to be sent with this response.
     * If the header has already been set, the new value overwrites the previous one.
     * <p>
     * The {@link Response#hasHeader(String)} method can be used to test for the presence of the header before setting its value.
     * </p>
     *
     * @param name the name of the header to send
     * @param value the new value for that header
     */
    public Response header(String name, Object value) {
        return header(name, valueOf(value));
    }

    /**
     * Removes the value of the specified header on this response. The name is case insensitive.
     *
     * <p>
     * In case there are multiple headers with that name, all values are removed.
     * </p>
     *
     * @param name the name of the header to remove
     */
    public Response removeHeader(String name) {
        headers.remove(name);
        return this;
    }

    /**
     * Adds a cookie to the response.
     *
     * @param cookie the cookie to add to the response
     */
    public Response addCookie(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    /**
     * Gets the list of cookies for this response
     * @return the (possibly empty) list of cookies
     */
    public List<Cookie> cookies() {
        return new ArrayList<>(cookies);
    }

    /**
     * Gets the content type of this response.
     *
     * <p>Note that getting the content type can also be done explicitly using
     * {@link Response#header}. This is a convenient method for doing so.</p>
     *
     * @return the content type header value or null
     */
    public String contentType() {
        return header(CONTENT_TYPE);
    }

    /**
     * Sets the content type for this response. If the content type specifies a charset, this charset will
     * be used to encode text based responses.
     *
     * <p>Note that setting the content type can also be done explicitly using {@link Response#header}.
     *  This is a convenient method for doing so.</p>
     *
     * @see Response#charset(String)
     * @param contentType the new content type value to set
     */
    public Response contentType(String contentType) {
        header(CONTENT_TYPE, contentType);
        return this;
    }

    /**
     * Gets the content length of this response.
     *
     * <p>Note that getting the content length can also be done explicitly using
     * {@link Response#header}. This is a convenient method for doing so.</p>
     *
     * @return the content length header value or null
     */
    public long contentLength() {
        return headerAsLong(CONTENT_LENGTH);
    }

    /**
     * Sets the content length for this response.
     *
     * <p>Note that setting the content length can also be done explicitly using {@link Response#header}.
     *  This is a convenient method for doing so</p>
     *
     * @param length the new content length value to set
     */
    public Response contentLength(long length) {
        header(CONTENT_LENGTH, length);
        return this;
    }

    /**
     * Replaces the MIME charset of the content type of this response. If no charset is set, <code>ISO-8859-1</code>
     * is assumed. This method has no effect if no content type has been set on the response.
     *
     * Calling {@link Response#contentType(String)} with the value of <code>text/html</code> then
     * calling this method with the value <code>UTF-8</code> is equivalent to calling <code>contentType</code>
     * with the value <code>text/html; charset=UTF-8</code>
     * <p>
     * Note that the charset will be used for encoding character based bodies.
     * </p>
     *
     * @param charsetName the name of the character encoding to use
     */
    public Response charset(String charsetName) {
        ContentType contentType = ContentType.of(this);
        if (contentType == null) return this;
        contentType(new ContentType(contentType.type(), contentType.subType(), charsetName).toString());
        return this;
    }

    /**
     * Reads the MIME charset of this response. The charset is read from the content-type header. A default charset
     * of <code> ISO-8859-1</code> is assumed.
     *
     * @return the charset set in the content type header or <code> ISO-8859-1</code>
     */
    public Charset charset() {
        ContentType contentType = ContentType.of(this);
        if (contentType == null || contentType.charset() == null) {
            return ISO_8859_1;
        }
        return contentType.charset();
    }

    /**
     * Sets the text content to write back to the client as the body of this response. The text content will be encoded
     * using the charset of the response.
     *
     * @see Response#charset(String)
     * @see Response#contentType(String)
     * @param text the text of the body to write back to the client
     */
    public Response body(String text) {
        return body(text(text));
    }

    /**
     * Sets the binary content to write back to the client as the body of this response.
     *
     * @param content the binary of the body to write back to the client
     */
    public Response body(byte[] content) {
        return body(bytes(content));
    }

    /**
     * Sets the body to write back to the client with this response. Character bodies will be encoded
     * using the charset of the response.
     *
     * @see Response#charset(String)
     * @see Response#contentType(String)
     * @param body the body to write back to the client
     */
    public Response body(Body body) {
        this.body = body;
        return this;
    }

    /**
     * Gets the body to write back to the client with this response.
     *
     * @return the body to send to the client
     */
    public Body body() {
        return body;
    }

    /**
     * Gets the size of the body of this response.
     *
     * @return the size of the body as a long
     */
    public long size() {
        return body.size(charset());
    }

    /**
     * Checks whether the body of this response is empty, i.e. has a size of <code>0</code>.
     *
     * @return true is the body is empty, false otherwise
     */
    public boolean empty() {
        return size() == 0;
    }

    /**
     * Sets the text content to write back to the client as the body of this response
     * then triggers a normal (i.e successful) completion of this response if not already completed.
     * The text content will be encoded using the charset of the response.
     * <p>
     * A call to <code>done</code> has no effect if this response has already completed, whether normally or
     * abnormally.
     * </p>
     * @param text the body text to write back to the client
     **/
    public void done(String text) {
        done(text(text));
    }

    /**
     * Sets the body to write back to the client then triggers a normal (i.e successful) completion of this response
     * if not already completed.
     * <p>
     * A call to <code>done</code> has no effect if this response has already completed, whether normally or
     * abnormally.
     * </p>
     * @param body the body to write back to the client
     **/
    public void done(Body body) {
        body(body).done();
    }

    /**
     * If not already completed, triggers a normal (i.e successful) completion of this response.
     * <p>
     * A call to <code>done</code> has no effect if this response has already completed, whether normally or
     * abnormally.
     * </p>
     **/
    public void done() {
        done.complete(this);
    }

    /**
     * If not already completed, triggers an abnormal (i.e failed) completion of this response with the
     * given exception.
     *
     * <p>
     * A call to <code>done</code> has no effect if this response has already completed, whether normally or
     * abnormally.
     * </p>
     **/
    public void done(Throwable error) {
        done.completeExceptionally(error);
    }

    /**
     * When this response completes normally (i.e. without an exception),
     * executes the given action, with this response.
     * <p>
     * Actions supplied will be executed in the order they are registered on this response.
     * <br>
     * To trigger normal completion of this response, call {@link #done()} .
     * </p>
     *
     * @param action the action to perform when this response completes successfully
     */
    public Response whenSuccessful(Consumer<Response> action) {
        postProcessing = postProcessing.whenComplete((response, error) -> {
            if (response != null) action.accept(response);
        });
        return this;
    }

    /**
     * When this response completes abnormally (i.e. with an exception),
     * executes the given action, with this response.
     * <p>
     * Actions supplied will be executed in the order they are registered on this response.
     * <br>
     * To trigger abnormal completion of this response, call {@link #done(Throwable)} .
     * </p>
     *
     * @param action the action to perform when this response completes abnormally
     */
    public Response whenFailed(BiConsumer<Response, Throwable> action) {
        postProcessing = postProcessing.whenComplete((response, error) -> {
            if (error != null) action.accept(Response.this, unwrap(error));
        });
        return this;
    }

    /**
     * When this response completes either normally or abnormally (i.e. with or without an exception),
     * executes the given action with this response and the
     * exception (or {@code null} if the response completed normally).
     * <p>
     * Note that this method allows injection of an action regardless of outcome,
     * otherwise preserving the outcome in its completion, unlike {@link #rescue}.
     * <br>
     * Actions supplied will be executed in the order they are registered on this response.
     * </p>
     * <p>
     * To trigger completion of this response, call either forms of {@link #done}.
     * </p>
     * @param action the action to perform when this response completes
     */
    public Response whenComplete(BiConsumer<Response, Throwable> action) {
        postProcessing = postProcessing.whenComplete((response, error) -> action.accept(Response.this, unwrap(error)));
        return this;
    }

    /**
     * When this response completes abnormally (i.e. with an exception),
     * executes the given action with this response and the exception, then continue processing this response
     * normally.
     * <p>
     * Note that this method replaces the failed result with this response before triggering the next action,
     * as if the response had completed normally.
     * <br>
     * Actions supplied will be executed in the order they are registered on this response.
     * </p>
     * @param action the action to perform when this response completes abnormally
     */
    public Response rescue(BiConsumer<Response, Throwable> action) {
        postProcessing = postProcessing.handle((response, error) -> {
            if (error != null) action.accept(Response.this, unwrap(error));
            return this;
        });
        return this;
    }

    /**
     * Returns {@code true} if this response completed.
     *
     * Completion may be due to normal termination or an exception -- in all of these cases, this method will return
     * {@code true}.
     *
     * @return {@code true} if this response completed, false otherwise
     */
    public boolean isDone() {
        return done.isDone();
    }

    /**
     * Waits if necessary for this response to complete, and then
     * retrieves its result.
     *
     * @return this response if it completed normally
     * @throws ExecutionException if this response completed with an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public Response await() throws ExecutionException, InterruptedException {
        return postProcessing.get();
    }

    /**
     * Waits if necessary for at most the given time for this response
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return this response if it completed normally
     * @throws ExecutionException if this response completed with an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws TimeoutException if the wait timed out
     */
    public Response await(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        return postProcessing.get(timeout, unit);
    }

    private Throwable unwrap(Throwable error) {
        return error instanceof CompletionException ? error.getCause() : error;
    }
}