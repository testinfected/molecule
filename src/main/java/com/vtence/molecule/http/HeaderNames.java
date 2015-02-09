package com.vtence.molecule.http;

/**
 * HTTP Request and Response Headers (see <a href="https://tools.ietf.org/html/rfc7231">RFC 7231</a>).
 */
public interface HeaderNames {

    static final String CONTENT_TYPE = "Content-Type";
    static final String CONTENT_ENCODING = "Content-Encoding";
    static final String CONTENT_LANGUAGE = "Content-Language";
    static final String CONTENT_LOCATION = "Content-Location";

    static final String CONTENT_LENGTH = "Content-Length";
    static final String CONTENT_RANGE = "Content-Range";
    static final String TRAILER = "Trailer";
    static final String TRANSFER_ENCODING = "Transfer-Encoding";

    static final String CACHE_CONTROL = "Cache-Control";
    static final String EXPECT = "Expect";
    static final String HOST = "Host";
    static final String MAX_FORWARDS = "Max-Forwards";
    static final String PRAGMA = "Pragma";
    static final String RANGE = "RANGE";
    static final String TE = "TE";

    static final String IF_MATCH = "If-Match";
    static final String IF_NONE_MATCH = "If-None-Match";
    static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    static final String IF_RANGE = "If-Range";

    static final String ACCEPT = "Accept";
    static final String ACCEPT_CHARSET = "Accept-Charset";
    static final String ACCEPT_ENCODING = "Accept-Encoding";
    static final String ACCEPT_LANGUAGE = "Accept-Language";

    static final String AUTHORIZATION = "Authorization";
    static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

    static final String FROM = "From";
    static final String REFERER = "Referer";
    static final String USER_AGENT = "User-Agent";

    static final String AGE = "Age";
    static final String EXPIRES = "Expires";
    static final String DATE = "Date";
    static final String LOCATION = "Location";
    static final String RETRY_AFTER = "Retry-After";
    static final String VARY = "Vary";
    static final String WARNING = "Warning";

    static final String ETAG = "ETag";
    static final String LAST_MODIFIED = "Last-Modified";

    static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

    static final String ACCEPT_RANGES = "Accept-Ranges";
    static final String ALLOW = "Allow";
    static final String SERVER = "Server";

    static final String SET_COOKIE = "Set-Cookie";
}