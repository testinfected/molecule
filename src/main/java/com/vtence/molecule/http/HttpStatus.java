package com.vtence.molecule.http;

/**
 * See <a href="https://tools.ietf.org/html/rfc7231#section-6">RFC 7231 Section 6</a>.
 */
public enum HttpStatus {

    /**
     * Indicates that the initial part of a request has been received and has not yet been rejected by the server.
     */
    CONTINUE(100, "Continue"),

    /**
     * Indicates that the server understands and is willing for a change of protocol.
     */
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),

    /**
     * Indicates that the request has succeeded.
     */
    OK(200, "OK"),

    /**
     * Indicates that the request has resulted in one or more new resources being successfully created.
     */
    CREATED(201, "Created"),

    /**
     * Indicates that the request has been accepted for processing, but the processing has not been completed.
     */
    ACCEPTED(202, "Accepted"),

    /**
     * Indicates that the request was successful but the enclosed payload has been modified by a transforming proxy.
     */
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),

    /**
     * Indicates that the server has successfully fulfilled the request and that
     * there is no additional content to send.
     */
    NO_CONTENT(204, "No Content"),

    /**
     * Indicates that indicates that the server successfully fulfilled the request and
     * desires that the user agent reset the view to its original state.
     */
    RESET_CONTENT(205, "Reset Content"),

    /**
     * Indicates that the server is successfully fulfilling a range request for the target resource.
     */
    PARTIAL_CONTENT(206, "Partial Content"),

    /**
     * Indicates that the target resource has more than one representation.
     */
    MULTIPLE_CHOICES(300, "Multiple Choices"),

    /**
     * Indicates that the target resource has been assigned a new permanent URI.
     */
    MOVED_PERMANENTLY(301, "Moved Permanently"),

    /**
     * Indicates that the target resource resides temporarily under a different URI.
     */
    FOUND(302, "Found"),

    /**
     * Indicates that the server is redirecting the user agent to a different resource.
     */
    SEE_OTHER(303, "See Other"),

    /**
     * Indicates that the client already has a valid representation of the target resource.
     */
    NOT_MODIFIED(304, "Not Modified"),

    /**
     * This code is now deprecated.
     */
    USE_PROXY(305, "Use Proxy"),

    /**
     * Indicates that the target resource resides temporarily under a different URI. This is different from 302
     * in that it does not allow changing the request method (i.e. from POST to GET) when redirecting.
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),

    /**
     * Indicates that the server will not process the request due a client error (e.g. a malformed request syntax)
     */
    BAD_REQUEST(400, "Bad Request"),

    /**
     * Indicates that the request lacks valid authentication credentials for the target resource.
     */
    UNAUTHORIZED(401, "Unauthorized"),

    /**
     * This is reserved for future use.
     */
    PAYMENT_REQUIRED(402, "Payment Required"),

    /**
     * Indicates that the server refuses to authorize the request.
     */
    FORBIDDEN(403, "Forbidden"),

    /**
     * Indicates that the server did not find a representation for the target resource.
     */
    NOT_FOUND(404, "Not Found"),

    /**
     * Indicates that that the method is not supported by the target resource.
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),

    /**
     * Indicates that the target resource does not have an acceptable representation
     * according to content negotiation headers.
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),

    /**
     * Indicates that the client needs to authenticate itself in order to use a proxy.
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),

    /**
     * Indicates that the server did not receive a complete request message within the time that it was
     * prepared to wait.
     */
    REQUEST_TIMEOUT(408, "Request Timeout"),

    /**
     * Indicates that the request could not be completed due to a conflict
     * with the current state of the target resource.
     */
    CONFLICT(409, "Conflict"),

    /**
     * Indicates that access to the target resource is no longer available.
     */
    GONE(410, "Gone"),

    /**
     * Indicates that the server refuses to accept the request without a defined Content-Length.
     */
    LENGTH_REQUIRED(411, "Length Required"),

    /**
     * Indicates that one or more conditions given in the request header fields failed.
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),

    /**
     * Indicates that the server is refusing to process the request because the request payload is too large.
     */
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),

    /**
     * Indicates that the server is refusing to service the request because the request URI is too long.
     */
    URI_TOO_LONG(414, "URI Too Long"),

    /**
     * Indicates that the server is refuses to service the request because the payload content type is invalid.
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),

    /**
     * Indicates that the request has been rejected due to invalid ranges.
     */
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),

    /**
     * Indicates that the client expectations could not be met.
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),

    /**
    * Indicates that the server refuses to perform the request using the current protocol.
    */
    UPGRADE_REQUIRED(426, "Upgrade Required"),

    /**
     * Indicates that the server encountered an unexpected error and could not fulfill the request.
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    /**
     * Indicates that the server does not support the functionality required to fulfill the request.
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),

    /**
     * Indicates that the gateway or proxy received an invalid response
     * from an inbound server while attempting to fulfill the request.
     */
    BAD_GATEWAY(502, "Bad Gateway"),

    /**
     * Indicates that the server is currently unable to handle the request.
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),

    /**
     * Indicates that the gateway or proxy did not receive a timely response from an upstream server.
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),

    /**
     * Indicates that the server does not support the major version of HTTP that was used in the request message.
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");

    /**
     * This is the the textual description of the response state.
     */
    public final String text;

    /**
     * This is the code used in the HTTP response
     * message to tell the client what kind of response this represents.
     */
    public final int code;

    /**
     * Constructor for the <code>HttpStatus</code> object. This will create
     * a status object that is used to represent a response state. It
     * contains a status code and a description of that code.
     *
     * @param code this is the code that is used for this status
     * @param reason this is the description used for the status
     */
    private HttpStatus(int code, String reason) {
        this.text = reason;
        this.code = code;
    }
}
