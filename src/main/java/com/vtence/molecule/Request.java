package com.vtence.molecule;

import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.helpers.Headers;
import com.vtence.molecule.helpers.Streams;
import com.vtence.molecule.http.AcceptLanguage;
import com.vtence.molecule.http.ContentType;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.http.HttpMethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LENGTH;
import static java.lang.Long.parseLong;

/**
 * Holds client HTTP request information and maintains attributes during the request lifecycle.
 *
 * Information includes among other things the body, headers, parameters, cookies and locales.
 */
public class Request {

    private final Headers headers = new Headers();
    private final Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();
    private final Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
    private final Map<Object, Object> attributes = new HashMap<Object, Object>();
    private final List<BodyPart> parts = new ArrayList<BodyPart>();

    private String uri;
    private String path;
    private String ip;
    private int port;
    private String hostName;
    private String protocol;
    private InputStream input;
    private HttpMethod method;
    private boolean secure;
    private long timestamp;

    public Request() {}

    /**
     * Reads the uri of this request.
     *
     * @return the request uri
     */
    public String uri() {
        return uri;
    }

    /**
     * Changes the uri of this request.
     *
     * @param uri the new uri
     */
    public Request uri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Reads the path of this request.
     *
     * @return the request path
     */
    public String path() {
        return path;
    }

    /**
     * Changes the path of this request.
     *
     * @param path the new path
     */
    public Request path(String path) {
        this.path = path;
        return this;
    }

    /**
     * Reads the ip of the remote client of this request.
     *
     * @return the remote client ip
     */
    public String remoteIp() {
        return ip;
    }

    /**
     * Changes the ip of the remote client of this request.
     *
     * @param ip the new ip
     */
    public Request remoteIp(String ip) {
        this.ip = ip;
        return this;
    }

    /**
     * Reads the hostname of the remote client of this request.
     *
     * @return the remote client hostname
     */
    public String remoteHost() {
        return hostName;
    }

    /**
     * Changes the hostname of the remote client of this request.
     *
     * @param hostName the new hostname
     */
    public Request remoteHost(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Reads the port of the remote client of this request.
     *
     * @return the remote client port
     */
    public int remotePort() {
        return port;
    }

    /**
     * Changes the port of the remote client of this request.
     *
     * @param port the new port
     */
    public Request remotePort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Reads the protocol of this request.
     *
     * @return the request protocol
     */
    public String protocol() {
        return protocol;
    }

    /**
     * Changes the protocol of this request.
     *
     * @param protocol the new protocol
     */
    public Request protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Indicates if this request was done over a secure connection, such as SSL.
     *
     * @return true if the request was transferred securely
     */
    public boolean secure() {
        return secure;
    }

    /**
     * Changes the secure state of this request, which indicates if the request was done over a secure channel.
     *
     * @param secure the new state
     */
    public Request secure(boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * Indicates the time in milliseconds when this request was received.
     *
     * @return the time the request arrived at
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Changes the timestamp of this request.
     *
     * @param time the new timestamp
     */
    public Request timestamp(long time) {
        timestamp = time;
        return this;
    }

    /**
     * Indicates the HTTP method with which this request was made (e.g.  GET, POST, PUT, etc.).
     *
     * @return the request HTTP method
     */
    public HttpMethod method() {
        return method;
    }

    /**
     * Changes the HTTP method of this request by name. Method name is case-insensitive and must refer to
     * one of the {@link com.vtence.molecule.http.HttpMethod}s.
     *
     * @param methodName the new method name
     */
    public Request method(String methodName) {
        return method(HttpMethod.valueOf(methodName));
    }

    /**
     * Changes the HTTP method of this request.
     *
     * @param method the new method
     */
    public Request method(HttpMethod method) {
        this.method = method;
        return this;
    }

    /**
     * Provides the text body of this request. The body is decoded using the charset of the request.
     *
     * @see Request#charset()
     * @return the text that makes up the body
     */
    public String body() throws IOException {
        return new String(bodyContent(), charset());
    }

    /**
     * Provides the body of this request as an array of bytes.
     *
     * @return the bytes content of the body
     */
    public byte[] bodyContent() throws IOException {
        return Streams.toBytes(bodyStream());
    }

    /**
     * Provides the body of this request as an {@link java.io.InputStream}.
     *
     * @return the stream of bytes that make up the body
     */
    public InputStream bodyStream() {
        return input;
    }

    /**
     * Changes the body of this request. The body is encoded using the charset of the request.
     *
     * Note that this will not affect the list of parameters that might have been sent with the original
     * body as POST parameters.
     *
     * @see Request#charset()
     * @param body the new body as a string
     */
    public Request body(String body) {
        return body(body.getBytes(charset()));
    }

    /**
     * Changes the body of this request.
     *
     * Note that this will not affect the list of parameters that might have been sent with the original
     * body as POST parameters.
     *
     * @param content the new body content as an array of bytes
     */
    public Request body(byte[] content) {
        this.input = new ByteArrayInputStream(content);
        return this;
    }

    /**
     * Changes the body of this request.
     *
     * Note that this will not affect the list of parameters that might have been sent with the original
     * body as POST parameters.
     *
     * @param input the new body content as a stream of bytes
     */
    public Request body(InputStream input) {
        this.input = input;
        return this;
    }

    /**
     * Ads a new body part to this request.
     *
     * <p>
     * Note that this will not change the body of the request, which will still contain the original multipart
     * encoded body.
     * </p>
     *
     * @param part the additional body part
     */
    public void addPart(BodyPart part) {
        parts.add(part);
    }

    /**
     * Acquires the list of <code>BodyPart</code>s with from this request,
     * provided that the request is of type <code>multipart/form-data</code>.
     * <p>
     * This is typically used in case of file uploads or multipart <code>POST</code> requests.
     * </p>
     *
     * @return the (possibly empty) list of body parts
     */
    public List<BodyPart> parts() {
        return parts;
    }

    /**
     * Acquires the <code>BodyPart</code> with the specified name from this request.
     * This is typically used in case of file uploads or multipart <code>POST</code> requests.
     *
     * @param name the name of the body part to acquire
     *
     * @return the named part or null if the part does not exist or the request
     * is not of type <code>multipart/form-data</code>
     */
    public BodyPart part(String name) {
        for (BodyPart part : parts) {
            if (part.name().equals(name)) return part;
        }
        return null;
    }

    /**
     * Provides the charset used in the body of this request.
     * The charset is read from the <code>Content-Type</code> header.
     *
     * @return the charset of this request or null if <code>Content-Type</code> is missing.
     */
    public Charset charset() {
        ContentType contentType = ContentType.of(this);
        if (contentType == null || contentType.charset() == null) {
            return Charsets.ISO_8859_1;
        }
        return contentType.charset();
    }

    /**
     * Checks for the presence of a specific HTTP message header in this request.
     *
     * @param name the name of header to check
     * @return true if the header was set
     */
    public boolean hasHeader(String name) {
        return headers.has(name);
    }

    /**
     * Gets the value of the specified header sent with this request. The name is case insensitive.
     *
     * <p>
     * In case there are multiple headers with that name, a comma separated list of values is returned.
     * </p>
     *
     * This method returns a null value if the request does not include any header of the specified name.
     *
     * @param name the name of the header to retrieve
     * @return the value of the header
     */
    public String header(String name) {
        return headers.get(name);
    }

    /**
     * Gets all the header names sent with this request. If the request has no header, the set will be empty.
     * <p>
     * Note that the names are provided as originally set on the request.
     * Modifications to the provided set are safe and will not affect the request.
     * </p>
     *
     * @return a set containing all the header names sent, which might be empty
     */
    public Set<String> headerNames() {
        return headers.names();
    }

    /**
     * Gets the list of all the values sent with this request under the specified header.
     * The name is case insensitive.
     *
     * <p>
     * Some headers can be sent by clients as several headers - each with a different value - rather than sending
     * the header as a comma separated list.
     * </p>
     *
     * If the request does not include any header of the specified name, the list is empty.
     * Modifications to the provided list are safe and will not affect the request.
     *
     * @param name the name of the header to retrieve
     * @return the list of values for that header
     */
    public List<String> headers(String name) {
        return headers.list(name);
    }

    /**
     * Adds an HTTP message header to this request. The new value will be added to the list
     * of existing values for that header name.
     *
     * @param name the name of the header to add
     * @param value the additional value for that header
     */
    public Request addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    /**
     * Sets an HTTP message header on this request. The new value will replace existing values for that header name.
     *
     * @param name the name of the header to set
     * @param value the value the header will have
     */
    public Request header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Removes the value of the specified header on this request. The name is case insensitive.
     *
     * <p>
     * In case there are multiple headers with that name, all values are removed.
     * </p>
     *
     * @param name the name of the header to remove
     */
    public Request removeHeader(String name) {
        headers.remove(name);
        return this;
    }

    /**
     * Checks if a cookie with a specific name was sent with this request.
     *
     * Note that changing the Cookie header of the request will not cause the cookies to change. To change the cookies,
     * use {@link Request#cookie(String, String)} and {@link Request#removeCookie(String)}.
     *
     * @see Request#cookie(com.vtence.molecule.http.Cookie)
     * @param name the name of the cookie to check
     * @return true if the cookie was sent
     */
    public boolean hasCookie(String name) {
        return cookies.containsKey(name);
    }

    /**
     * Retrieves the value of a cookie sent with this request under a specific name.
     *
     * If the cookie exists as an HTTP header then it's value is returned, otherwise a null value is returned.
     *
     * @param name name of the cookie to acquire
     * @return the value of the cookie with that name or null
     */
    public String cookieValue(String name) {
        Cookie cookie = cookie(name);
        return cookie != null ? cookie.value() : null;
    }

    /**
     * Retrieves a cookie sent with this request under a specific name.
     *
     * If the cookie exists as an HTTP header then it is returned
     * as a <code>Cookie</code> object - with the name, value, path as well as the optional domain part.
     *
     * @param name the name of the cookie to acquire
     * @return the cookie with that name or null
     */
    public Cookie cookie(String name) {
        return cookies.get(name);
    }

    /**
     * Retrieves the list of all cookies sent with this request.
     *
     * Note that the list is safe for modification, it will not affect the request.
     *
     * @return the list of cookies sent
     */
    public List<Cookie> cookies() {
        return new ArrayList<Cookie>(cookies.values());
    }

    /**
     * Sets a cookie with a specific name and value on this request. If a cookie with that name already exists,
     * it is replaced by the new value.
     *
     * Note that this will not affect the <code>Cookie</code> header that has been set with the request.
     *
     * @param name the name of the cookie to set
     * @param value the value of the cookie to set
     */
    public Request cookie(String name, String value) {
        return cookie(new Cookie(name, value));
    }

    /**
     * Sets a cookie on this request. If a cookie with the same name already exists, it is replaced by the new cookie.
     *
     * Note that this will not affect the <code>Cookie</code> header that has been set with the request.
     *
     * @param cookie the cookie to set
     */
    public Request cookie(Cookie cookie) {
        cookies.put(cookie.name(), cookie);
        return this;
    }

    /**
     * Removes a cookie with a specific name from this request. If no cookie with that name exists, the operation
     * does nothing.
     *
     * Note that this will not affect the Cookie header that has been set with the request.
     *
     * @param name the name of the cookie to remove
     */
    public Request removeCookie(String name) {
        cookies.remove(name);
        return this;
    }

    /**
     * Reads the value of the <code>Content-Length</code> header sent as part of this request.
     * If the header is missing, the value -1 is returned.
     *
     * @return the <code>Content-Length</code> header value as a long or -1
     */
    public long contentLength() {
        String value = header(CONTENT_LENGTH);
        return value != null ? parseLong(value) : -1;
    }

    /**
     * Reads the value of the <code>Content-Type</code> header sent as part of this request. If the
     * header is missing, a null value is returned.
     *
     * @return the <code>Content-Type</code> header value or null
     */
    public String contentType() {
        ContentType contentType = ContentType.of(this);
        return contentType != null ? contentType.mediaType() : null;
    }

    /**
     * Gets the value of a specific parameter of this request, or null if the parameter does not exist.
     *
     * If the parameter has more than one value, the first one is returned.
     *
     * <p>Request parameters are contained in the query string or posted form data.</p>
     *
     * <p>
     * Note that changing the body of the request will not cause the parameters to change. To change the request
     * parameters, use {@link Request#addParameter(String, String)} and {@link Request#removeParameter(String)}.
     * </p>
     *
     * @param name the name of the parameter
     * @return the parameter value or null
     */
    public String parameter(String name) {
        List<String> values = parameters(name);
        return values.isEmpty() ?  null : values.get(values.size() - 1);
    }

    /**
     * Gets the list of values of a specific parameter of this request. If the parameter does not exist, the list will
     * be empty. The returned list is safe for modification and will not affect the request.
     *
     * <p>Request parameters are contained in the query string or posted form data.</p>
     *
     * <p>
     * Note that changing the body of the request will not cause the parameters to change. To change the request
     * parameters, use {@link Request#addParameter(String, String)} and {@link Request#removeParameter(String)}.
     * </p>
     *
     * @param name the name of the parameter
     * @return the list of that parameter's values
     */
    public List<String> parameters(String name) {
        return parameters.containsKey(name) ? new ArrayList<String>(parameters.get(name)) : new ArrayList<String>();
    }

    /**
     * Returns the names of all the parameters contained in this request.
     * If the request has no parameter, the method returns an empty <code>Set</code>. The returned
     * set is safe for modification and will not affect the request.
     *
     * <p>
     * Parameters are taken from the query or HTTP form posting.
     * </p>
     *
     * <p>
     * Note that changing the body of the request will not cause the parameters to change. To change the request
     * parameters, use {@link Request#addParameter(String, String)} and {@link Request#removeParameter(String)}.
     * </p>
     *
     * @return the set of parameter names
     */
    public Set<String> parameterNames() {
        return new LinkedHashSet<String>(parameters.keySet());
    }

    /**
     * Returns a <code>Map</code> of all the parameters contained in this request. If the request has no parameter,
     * the map will be empty. The map is not modifiable.
     *
     * <p>
     * Parameters are taken from the query or HTTP form posting.
     * </p>
     *
     * <p>
     * Note that changing the body of the request will not cause the parameters to change. To change the request
     * parameters, use {@link Request#addParameter(String, String)} and {@link Request#removeParameter(String)}.
     * </p>
     *
     * @return a map containing all the request parameters
     */
    public Map<String, List<String>> allParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Adds a parameter value to this request. If a parameter with the same name already exists,
     * the new value is appended to this list of values for that parameter.
     *
     * @param name the parameter name
     * @param value the additional parameter value
     */
    public Request addParameter(String name, String value) {
        if (!parameters.containsKey(name)) {
            parameters.put(name, new ArrayList<String>());
        }
        parameters.get(name).add(value);
        return this;
    }

    /**
     * Removes a parameter from this request. This removes all values for that parameter from the request.
     *
     * @param name the name of the parameter to remove
     */
    public Request removeParameter(String name) {
        parameters.remove(name);
        return this;
    }

    /**
     * Gets the value of a keyed attribute of this request, or null if no attribute with the given key exists.
     *
     * <p>
     *     This method will attempt to cast the attribute value to type T,
     *     which can result in a <code>ClassCastException</code>.
     * </p>
     *
     * @see Request#attribute(Object, Object)
     * @param key the key of the attribute to retrieve
     * @return <T> the value of the attribute, or null if the attribute does not exist
     * @throws java.lang.ClassCastException if the attribute value is not an instance of the type parameter
     */
    @SuppressWarnings("unchecked")
    public <T> T attribute(Object key) {
        return (T) attributes.get(key);
    }

    /**
     * Sets an attribute on this request. Attributes make available custom information about the request.
     *
     * <p>
     * Attribute keys are unique. If an attribute exists under the same key, its value will be replaced by the new value.
     * </p>
     *
     * @param key the key of the attribute to set
     * @param value the new attribute value
     */
    public Request attribute(Object key, Object value) {
        attributes.put(key, value);
        return this;
    }

    /**
     * Gets the set of all attribute keys on this request. If no attribute exist, the set will be empty.
     *
     * <p>
     * Note that the set is a copy, it can be modified without changing the request.
     * </p>
     *
     * @return the set of attribute keys
     */
    public Set<Object> attributeKeys() {
        return new HashSet<Object>(attributes.keySet());
    }

    /**
     * Removes the attribute with the given key from this request. If no attribute exists, the method does nothing.
     *
     * @param key the key of the attribute to remove
     */
    public Request removeAttribute(Object key) {
        attributes.remove(key);
        return this;
    }

    /**
     * Gets the map of all attributes of this request. If no attribute exists, the map will be empty.
     *
     * <p>
     * Note that the map is not modifiable.
     * </p>
     *
     * @return the map of request attributes
     */
    public Map<Object, Object> attributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Acquires the preferred locale from this request <code>Accept-Language</code> header.
     * If the client accepts more than one locale, the preferred one  - i.e. the first one - is returned.
     *
     * @return the locale preferred by the client
     */
    public Locale locale() {
        List<Locale> locales = locales();
        return locales.isEmpty() ? null : locales.get(0);
    }

    /**
     * Acquires the accepted locales from this request <code>Accept-Language</code> header. The
     * locales are provided in preference order. If the header is not present, the list will be empty.
     *
     * @return all the locales accepted by the client
     */
    public List<Locale> locales() {
        return AcceptLanguage.of(this).locales();
    }
}