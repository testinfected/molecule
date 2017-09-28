package com.vtence.molecule;

import com.vtence.molecule.helpers.Headers;
import com.vtence.molecule.helpers.Streams;
import com.vtence.molecule.http.ContentType;
import com.vtence.molecule.http.Host;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.http.Scheme;
import com.vtence.molecule.http.Uri;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LENGTH;
import static com.vtence.molecule.http.HeaderNames.HOST;
import static com.vtence.molecule.http.HttpMethod.DELETE;
import static com.vtence.molecule.http.HttpMethod.HEAD;
import static com.vtence.molecule.http.HttpMethod.OPTIONS;
import static com.vtence.molecule.http.HttpMethod.PATCH;
import static com.vtence.molecule.http.HttpMethod.POST;
import static com.vtence.molecule.http.HttpMethod.PUT;
import static com.vtence.molecule.lib.EmptyInputStream.EMPTY;
import static java.lang.Long.parseLong;

/**
 * Holds client HTTP request information and maintains attributes during the request lifecycle.
 * <p>
 * Information includes among other things the body, headers, parameters, cookies and locales.
 */
public class Request {

    private final Map<String, List<String>> parameters = new LinkedHashMap<>();
    private final Map<Object, Object> attributes = new HashMap<>();
    private final List<BodyPart> parts = new ArrayList<>();

    private final Headers headers;

    private Uri uri;
    private String remoteHost;
    private String remoteIp;
    private int remotePort;
    private String protocol;
    private InputStream body;
    private HttpMethod method;
    private boolean secure;
    private long timestamp;

    public Request(String method, Uri uri) {
        this(HttpMethod.valueOf(method), uri);
    }

    public Request(HttpMethod method, Uri uri) {
        this(method, uri, new Headers());
    }

    public Request(HttpMethod method, Uri uri, Headers headers) {
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.secure = Objects.equals(Scheme.from(uri), Scheme.HTTPS);
        this.protocol = "HTTP/1.1";
        this.body = EMPTY;
        this.remotePort = -1;
        this.timestamp = -1;
    }

    /**
     * Creates an HTTP GET request with the given uri.
     *
     * @param uri the request uri
     * @return the new request
     */
    public static Request get(String uri) {
        return new Request(HttpMethod.GET, Uri.of(uri));
    }

    /**
     * Creates a HTTP POST request with the given uri.
     *
     * @param uri the request uri
     * @return the new request
     */
    public static Request post(String uri) {
        return new Request(POST, Uri.of(uri));
    }

    /**
     * Creates a HTTP PUT request with the given request uri.
     *
     * @param uri the request uri
     * @return the new request
     */
    public static Request put(String uri) {
        return new Request(PUT, Uri.of(uri));
    }

    /**
     * Creates a HTTP PATCH request with the given request uri.
     *
     * @param uri the request uri
     * @return the new request
     */
    public static Request patch(String uri) {
        return new Request(PATCH, Uri.of(uri));
    }

    /**
     * Creates a HTTP DELETE request with the given request uri.
     *
     * @param uri the request uri
     * @return the new request
     */
    public static Request delete(String uri) {
        return new Request(DELETE, Uri.of(uri));
    }

    /**
     * Creates a HTTP HEAD request with the given request uri.
     *
     * @param uri the request uri
     * @return the new request
     */
    public static Request head(String uri) {
        return new Request(HEAD, Uri.of(uri));
    }

    /**
     * Creates a HTTP OPTIONS request with the given request uri.
     *
     * @param uri the request uri
     * @return the new request
     */
    public static Request options(String uri) {
        return new Request(OPTIONS, Uri.of(uri));
    }

    /**
     *
     * Reads the URI of this request. It will be the full URL, reconstructed from the status line, server
     * host and port.
     *
     * @return the reconstructed URI
     */
    public Uri uri() {
        return uri;
    }

    /**
     * Changes the URI of this request.
     *
     * @param uri the new URI
     */
    public Request uri(Uri uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Reads the path of this request. This is the normalized path.
     *
     * @return the normalized path associated with this request's URI
     */
    public String path() {
        return uri.normalize().path();
    }

    /**
     * Changes the path of this request. This is a convenience method for changing this request's URI path.
     *
     * @param path the new request path
     */
    public Request path(String path) {
        this.uri = uri.path(path);
        return this;
    }

    /**
     * Reads the query part of this request's URI. The query string does not include the leading <code>?</code>.
     *
     * @return the query associated with this request's URI, which might be null
     */
    public String query() {
        return uri.query();
    }

    /**
     * Gets this request URI scheme. It will be one of {@code http} or {@code https}.
     *
     * @return the request URI scheme or null
     */
    public String scheme() {
        return uri.scheme();
    }

    /**
     * Reads the ip of the remote client of this request.
     *
     * @return the remote client ip
     */
    public String remoteIp() {
        return remoteIp;
    }

    /**
     * Changes the ip of the remote client of this request.
     *
     * @param ip the new ip
     */
    public Request remoteIp(String ip) {
        this.remoteIp = ip;
        return this;
    }

    /**
     * Reads the hostname of the remote client of this request.
     *
     * @return the remote client hostname
     */
    public String remoteHost() {
        return remoteHost;
    }

    /**
     * Changes the hostname of the remote client of this request.
     *
     * @param hostName the new hostname
     */
    public Request remoteHost(String hostName) {
        this.remoteHost = hostName;
        return this;
    }

    /**
     * Reads the port of the remote client of this request.
     *
     * @return the remote client port
     */
    public int remotePort() {
        return remotePort;
    }

    /**
     * Changes the port of the remote client of this request.
     *
     * @param port the new port
     */
    public Request remotePort(int port) {
        this.remotePort = port;
        return this;
    }

    /**
     * Gets the host name of the server to which this request was sent.
     * It is taken from the Host header value if any, otherwise the server hostname is used.
     *
     * @return the server host name
     */
    public String hostname() {
        return hasHeader(HOST) ? Host.parseName(header(HOST)) : uri.host();
    }

    /**
     * Reads the port of the server to which this request was sent.
     * It is taken from the Host header value if any, otherwise the server port is used.
     *
     * @return the server port
     */
    public int port() {
        int port = hasHeader(HOST) ? Host.parsePort(header(HOST)) : uri.port();

        if (port == -1) {
            port = Scheme.from(uri).defaultPort();
        }

        if (port == -1) {
            port = uri.port();
        }

        return port;
    }

    /**
     * Reconstructs the URL the client made this request to. It will use the HOST header information if present.
     *
     * @return the reconstructed URL
     */
    public URL url() {
        return uri.host(hostname()).port(port() == Scheme.from(uri).defaultPort() ? -1 : port()).toURL();
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
     * Consumes the body of this request and returns it as text. The text is decoded using the charset of the request.
     *
     * @return the text that makes up the body
     * @see Request#charset()
     */
    public String body() throws IOException {
        return new String(bodyContent(), charset());
    }

    /**
     * Consumes the body of this request and returns it as an array of bytes.
     *
     * @return the bytes content of the body
     */
    public byte[] bodyContent() throws IOException {
        return Streams.consume(bodyStream());
    }

    /**
     * Provides the body of this request as an {@link java.io.InputStream}.
     *
     * @return the stream of bytes that make up the body
     */
    public InputStream bodyStream() {
        return body;
    }

    /**
     * Changes the body of this request. The body is encoded using the charset of the request.
     * <p>
     * Note that this will not affect the list of parameters that might have been sent with the original
     * body as POST parameters.
     *
     * @param body the new body as a string
     * @see Request#charset()
     */
    public Request body(String body) {
        return body(body.getBytes(charset()));
    }

    /**
     * Changes the body of this request.
     * <p>
     * Note that this will not affect the list of parameters that might have been sent with the original
     * body as POST parameters.
     *
     * @param content the new body content as an array of bytes
     */
    public Request body(byte[] content) {
        this.body = new ByteArrayInputStream(content);
        return this;
    }

    /**
     * Changes the body of this request.
     * <p>
     * Note that this will not affect the list of parameters that might have been sent with the original
     * body as POST parameters.
     *
     * @param input the new body content as a stream of bytes
     */
    public Request body(InputStream input) {
        this.body = input;
        return this;
    }

    /**
     * Ads a new body part to this request.
     * <p>
     * <p>
     * Note that this will not change the body of the request, which will still contain the original multipart
     * encoded body.
     * </p>
     *
     * @param part the additional body part
     */
    public Request addPart(BodyPart part) {
        parts.add(part);
        return this;
    }

    /**
     * Acquires the list of <code>BodyPart</code>s with from this request,
     * provided that the request is of type <code>multipart/form-data</code>.
     * <p>
     * This is typically used in case of file uploads or multipart <code>POST</code> requests.
     * </p>
     * <p>
     * Note that the list is a copy, modifications to the returned list will not change the request.
     * </p>
     *
     * @return the (possibly empty) list of body parts
     */
    public List<BodyPart> parts() {
        return new ArrayList<>(parts);
    }

    /**
     * Acquires the <code>BodyPart</code> with the specified name from this request.
     * This is typically used in case of file uploads or multipart <code>POST</code> requests.
     *
     * @param name the name of the body part to acquire
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
     * Removes the body part with the specified name from this request.
     * <p>
     * <p>
     * In case there are multiple body parts with that name, they are all removed.
     * </p>
     *
     * @param name the name of the part(s) to remove
     */
    public Request removePart(String name) {
        BodyPart[] copy = parts.toArray(new BodyPart[parts.size()]);
        for (BodyPart part : copy) {
            if (part.name().equals(name)) removePart(part);
        }
        return this;
    }

    /**
     * Removes the given body part from this request.
     *
     * @param part the body part to remove
     */
    public Request removePart(BodyPart part) {
        parts.remove(part);
        return this;
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
            return StandardCharsets.ISO_8859_1;
        }
        return contentType.charset();
    }

    /**
     * Checks for the presence of a specific HTTP message header in this request.
     *
     * @param name the name of the header to check
     * @return true if the header was set
     */
    public boolean hasHeader(String name) {
        return headers.has(name);
    }

    /**
     * Gets the value of the specified header sent with this request. The name is case insensitive.
     * <p>
     * <p>
     * In case there are multiple headers with that name, a comma separated list of values is returned.
     * </p>
     * <p>
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
     * <p>
     * <p>
     * Some headers can be sent by clients as several headers - each with a different value - rather than sending
     * the header as a comma separated list.
     * </p>
     * <p>
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
     * @param name  the name of the header to add
     * @param value the additional value for that header
     */
    public Request addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    /**
     * Sets an HTTP message header on this request. The new value will replace existing values for that header name.
     *
     * @param name  the name of the header to set
     * @param value the value the header will have
     */
    public Request header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Removes the value of the specified header on this request. The name is case insensitive.
     * <p>
     * <p>
     * In case there are multiple headers with that name, all values are removed.
     * </p>
     *
     * @param name the name of the header(s) to remove
     */
    public Request removeHeader(String name) {
        headers.remove(name);
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
     * Checks for the presence of a specific parameter in this request.
     *
     * @param name the name of the parameter
     * @return true if the parameter is present, false otherwise
     */
    public boolean hasParameter(String name) {
        return !parameters(name).isEmpty();
    }

    /**
     * Gets the value of a specific parameter of this request, or null if the parameter does not exist.
     * <p>
     * If the parameter has more than one value, the last one is returned.
     * <p>
     * <p>Request parameters are contained in the query string or posted form data.</p>
     * <p>
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
        return values.isEmpty() ? null : values.get(values.size() - 1);
    }

    /**
     * Gets the list of values of a specific parameter of this request. If the parameter does not exist, the list will
     * be empty. The returned list is safe for modification and will not affect the request.
     * <p>
     * <p>Request parameters are contained in the query string or posted form data.</p>
     * <p>
     * <p>
     * Note that changing the body of the request will not cause the parameters to change. To change the request
     * parameters, use {@link Request#addParameter(String, String)} and {@link Request#removeParameter(String)}.
     * </p>
     *
     * @param name the name of the parameter
     * @return the list of that parameter's values
     */
    public List<String> parameters(String name) {
        return parameters.containsKey(name) ? new ArrayList<>(parameters.get(name)) : new ArrayList<>();
    }

    /**
     * Returns the names of all the parameters contained in this request.
     * If the request has no parameter, the method returns an empty <code>Set</code>. The returned
     * set is safe for modification and will not affect the request.
     * <p>
     * <p>
     * Parameters are taken from the query or HTTP form posting.
     * </p>
     * <p>
     * <p>
     * Note that changing the body of the request will not cause the parameters to change. To change the request
     * parameters, use {@link Request#addParameter(String, String)} and {@link Request#removeParameter(String)}.
     * </p>
     *
     * @return the set of parameter names
     */
    public Set<String> parameterNames() {
        return new LinkedHashSet<>(parameters.keySet());
    }

    /**
     * Returns a <code>Map</code> of all the parameters contained in this request. If the request has no parameter,
     * the map will be empty. The map is not modifiable.
     * <p>
     * <p>
     * Parameters are taken from the query or HTTP form posting.
     * </p>
     * <p>
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
     * @param name  the parameter name
     * @param value the additional parameter value
     */
    public Request addParameter(String name, String value) {
        if (!parameters.containsKey(name)) {
            parameters.put(name, new ArrayList<>());
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
     * <p>
     * <p>
     * This method will attempt to cast the attribute value to type T,
     * which can result in a <code>ClassCastException</code>.
     * </p>
     *
     * @param key the key of the attribute to retrieve
     * @return the value of the attribute, or null if the attribute does not exist
     * @throws java.lang.ClassCastException if the attribute value is not an instance of the type parameter
     * @see Request#attribute(Object, Object)
     */
    @SuppressWarnings("unchecked")
    public <T> T attribute(Object key) {
        return (T) attributes.get(key);
    }

    /**
     * Sets an attribute on this request. Attributes make available custom information about the request.
     * <p>
     * <p>
     * Attribute keys are unique. If an attribute exists under the same key, its value will be replaced by the new value.
     * </p>
     *
     * @param key   the key of the attribute to set
     * @param value the new attribute value
     */
    public Request attribute(Object key, Object value) {
        attributes.put(key, value);
        return this;
    }

    /**
     * Gets the set of all attribute keys on this request. If no attribute exist, the set will be empty.
     * <p>
     * <p>
     * Note that the set is a copy, it can be modified without changing the request.
     * </p>
     *
     * @return the set of attribute keys
     */
    public Set<Object> attributeKeys() {
        return new HashSet<>(attributes.keySet());
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
     * <p>
     * <p>
     * Note that the map is not modifiable.
     * </p>
     *
     * @return the map of request attributes
     */
    public Map<Object, Object> attributes() {
        return Collections.unmodifiableMap(attributes);
    }
}