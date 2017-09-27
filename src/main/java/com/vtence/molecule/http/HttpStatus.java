package com.vtence.molecule.http;

/**
 * See <a href="https://tools.ietf.org/html/rfc7231#section-6">RFC 7231 Section 6</a>.
 */
public final class HttpStatus {

    /**
     * Indicates that the initial part of a request has been received and has not yet been rejected by the server.
     */
    public static final HttpStatus CONTINUE = of(100, "Continue");

    /**
     * Indicates that the server understands and is willing for a change of protocol.
     */
    public static final HttpStatus SWITCHING_PROTOCOLS = of(101, "Switching Protocols");

    /**
     * Indicates that the request has succeeded.
     */
    public static final HttpStatus OK = of(200, "OK");

    /**
     * Indicates that the request has resulted in one or more new resources being successfully created.
     */
    public static final HttpStatus CREATED = of(201, "Created");

    /**
     * Indicates that the request has been accepted for processing, but the processing has not been completed.
     */
    public static final HttpStatus ACCEPTED = of(202, "Accepted");

    /**
     * Indicates that the request was successful but the enclosed payload has been modified by a transforming proxy.
     */
    public static final HttpStatus NON_AUTHORITATIVE_INFORMATION = of(203, "Non-Authoritative Information");

    /**
     * Indicates that the server has successfully fulfilled the request and that
     * there is no additional content to send.
     */
    public static final HttpStatus NO_CONTENT = of(204, "No Content");

    /**
     * Indicates that indicates that the server successfully fulfilled the request and
     * desires that the user agent reset the view to its original state.
     */
    public static final HttpStatus RESET_CONTENT = of(205, "Reset Content");

    /**
     * Indicates that the server is successfully fulfilling a range request for the target resource.
     */
    public static final HttpStatus PARTIAL_CONTENT = of(206, "Partial Content");

    /**
     * Indicates that the target resource has more than one representation.
     */
    public static final HttpStatus MULTIPLE_CHOICES = of(300, "Multiple Choices");

    /**
     * Indicates that the target resource has been assigned a new permanent URI.
     */
    public static final HttpStatus MOVED_PERMANENTLY = of(301, "Moved Permanently");

    /**
     * Indicates that the target resource resides temporarily under a different URI.
     */
    public static final HttpStatus FOUND = of(302, "Found");

    /**
     * Indicates that the server is redirecting the user agent to a different resource.
     */
    public static final HttpStatus SEE_OTHER = of(303, "See Other");

    /**
     * Indicates that the client already has a valid representation of the target resource.
     */
    public static final HttpStatus NOT_MODIFIED = of(304, "Not Modified");

    /**
     * This code is now deprecated.
     */
    public static final HttpStatus USE_PROXY = of(305, "Use Proxy");

    /**
     * Indicates that the target resource resides temporarily under a different URI. This is different from 302
     * in that it does not allow changing the request method (i.e. from POST to GET) when redirecting.
     */
    public static final HttpStatus TEMPORARY_REDIRECT = of(307, "Temporary Redirect");

    /**
     * Indicates that the server will not process the request due a client error (e.g. a malformed request syntax).
     */
    public static final HttpStatus BAD_REQUEST = of(400, "Bad Request");

    /**
     * Indicates that the request lacks valid authentication credentials for the target resource.
     */
    public static final HttpStatus UNAUTHORIZED = of(401, "Unauthorized");

    /**
     * This is reserved for future use.
     */
    public static final HttpStatus PAYMENT_REQUIRED = of(402, "Payment Required");

    /**
     * Indicates that the server refuses to authorize the request.
     */
    public static final HttpStatus FORBIDDEN = of(403, "Forbidden");

    /**
     * Indicates that the server did not find a representation for the target resource.
     */
    public static final HttpStatus NOT_FOUND = of(404, "Not Found");

    /**
     * Indicates that that the method is not supported by the target resource.
     */
    public static final HttpStatus METHOD_NOT_ALLOWED = of(405, "Method Not Allowed");

    /**
     * Indicates that the target resource does not have an acceptable representation
     * according to content negotiation headers.
     */
    public static final HttpStatus NOT_ACCEPTABLE = of(406, "Not Acceptable");

    /**
     * Indicates that the client needs to authenticate itself in order to use a proxy.
     */
    public static final HttpStatus PROXY_AUTHENTICATION_REQUIRED = of(407, "Proxy Authentication Required");

    /**
     * Indicates that the server did not receive a complete request message within the time that it was
     * prepared to wait.
     */
    public static final HttpStatus REQUEST_TIMEOUT = of(408, "Request Timeout");

    /**
     * Indicates that the request could not be completed due to a conflict
     * with the current state of the target resource.
     */
    public static final HttpStatus CONFLICT = of(409, "Conflict");

    /**
     * Indicates that access to the target resource is no longer available.
     */
    public static final HttpStatus GONE = of(410, "Gone");

    /**
     * Indicates that the server refuses to accept the request without a defined Content-Length.
     */
    public static final HttpStatus LENGTH_REQUIRED = of(411, "Length Required");

    /**
     * Indicates that one or more conditions given in the request header fields failed.
     */
    public static final HttpStatus PRECONDITION_FAILED = of(412, "Precondition Failed");

    /**
     * Indicates that the server is refusing to process the request because the request payload is too large.
     */
    public static final HttpStatus PAYLOAD_TOO_LARGE = of(413, "Payload Too Large");

    /**
     * Indicates that the server is refusing to service the request because the request URI is too long.
     */
    public static final HttpStatus URI_TOO_LONG = of(414, "URI Too Long");

    /**
     * Indicates that the server is refuses to service the request because the payload content type is invalid.
     */
    public static final HttpStatus UNSUPPORTED_MEDIA_TYPE = of(415, "Unsupported Media Type");

    /**
     * Indicates that the request has been rejected due to invalid ranges.
     */
    public static final HttpStatus RANGE_NOT_SATISFIABLE = of(416, "Range Not Satisfiable");

    /**
     * Indicates that the client expectations could not be met.
     */
    public static final HttpStatus EXPECTATION_FAILED = of(417, "Expectation Failed");

    /**
    * Indicates that the server refuses to perform the request using the current protocol.
    */
    public static final HttpStatus UPGRADE_REQUIRED = of(426, "Upgrade Required");

    /**
     * Indicates that the server encountered an unexpected error and could not fulfill the request.
     */
    public static final HttpStatus INTERNAL_SERVER_ERROR = of(500, "Internal Server Error");

    /**
     * Indicates that the server does not support the functionality required to fulfill the request.
     */
    public static final HttpStatus NOT_IMPLEMENTED = of(501, "Not Implemented");

    /**
     * Indicates that the gateway or proxy received an invalid response
     * from an inbound server while attempting to fulfill the request.
     */
    public static final HttpStatus BAD_GATEWAY = of(502, "Bad Gateway");

    /**
     * Indicates that the server is currently unable to handle the request.
     */
    public static final HttpStatus SERVICE_UNAVAILABLE = of(503, "Service Unavailable");

    /**
     * Indicates that the gateway or proxy did not receive a timely response from an upstream server.
     */
    public static final HttpStatus GATEWAY_TIMEOUT = of(504, "Gateway Timeout");

    /**
     * Indicates that the server does not support the major version of HTTP that was used in the request message.
     */
    public static final HttpStatus HTTP_VERSION_NOT_SUPPORTED = of(505, "HTTP Version Not Supported");

    /**
     * This is the code used in the HTTP response
     * message to tell the client what kind of response this represents.
     */
    public final int code;

    /**
     * This is the the textual description of the response state.
     */
    public final String reason;

    /**
     * Constructor for the <code>HttpStatus</code> object. This will create
     * a status object that is used to represent a response state. It
     * contains a status code and a description of that code.
     *
     * @param code this is the code that is used for this status
     * @param reason this is the description used for the status
     */
    HttpStatus(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public static HttpStatus of(int code, String reason) {
        return new HttpStatus(code, reason);
    }
}
