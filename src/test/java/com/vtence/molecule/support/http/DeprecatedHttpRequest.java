package com.vtence.molecule.support.http;

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeprecatedHttpRequest {

    private final WebClient client;
    private final String domain = "localhost";

    private final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    private final Map<String, String> headers = new HashMap<String, String>();
    private final Map<String, String> cookies = new HashMap<String, String>();

    private HttpMethod method = HttpMethod.GET;
    private String path = "/";
    private int port;
    private int timeoutInMillis = 5000;
    private boolean followRedirects = true;
    private boolean applyCookies = true;
    private String body;
    private String encodingType;
    private boolean secure = false;

    public DeprecatedHttpRequest(int port) {
        this(new WebClient(), port);
    }

    public DeprecatedHttpRequest(WebClient client, int port) {
        this.client = client;
        this.port = port;
    }

    public DeprecatedHttpRequest but() {
        DeprecatedHttpRequest other = new DeprecatedHttpRequest(client, port).
                withTimeout(timeoutInMillis).
                usingMethod(method).
                on(path).
                applyCookies(applyCookies).
                followRedirects(followRedirects).
                withEncodingType(encodingType).
                useSSL(secure).
                withBody(body);

        for (String header: headers.keySet()) {
            other.withHeader(header, headers.get(header));
        }
        for (String cookie: cookies.keySet()) {
            other.withCookie(cookie, cookies.get(cookie));
        }
        for (String name: parameters.keySet()) {
            other.withParameters(name, parameters(name));
        }

        return other;
    }

    private String[] parameters(String name) {
        List<String> values = parameters.get(name);
        return values.toArray(new String[values.size()]);
    }

    public DeprecatedHttpRequest applyCookies(boolean apply) {
        this.applyCookies = apply;
        return this;
    }

    public DeprecatedHttpRequest onPort(int port) {
        this.port = port;
        return this;
    }

    public DeprecatedHttpRequest on(String path) {
        this.path = path;
        return this;
    }

    public DeprecatedHttpRequest useSSL() {
        return useSSL(true);
    }

    public DeprecatedHttpRequest useSSL(boolean secure)  {
        this.secure = secure;
        return this;
    }

    public DeprecatedHttpRequest followRedirects(boolean follow) {
        this.followRedirects = follow;
        return this;
    }

    public DeprecatedHttpRequest withHeader(String header, String value) {
        this.headers.put(header, value);
        return this;
    }

    public DeprecatedHttpRequest withParameter(String name, String value) {
        return withParameters(name, value);
    }

    public DeprecatedHttpRequest withParameters(String name, String... value) {
        parameters.put(name, Arrays.asList(value));
        return this;
    }

    public DeprecatedHttpRequest withBody(String body) {
        this.body = body;
        return this;
    }

    public DeprecatedHttpRequest withEncodingType(String type) {
        this.encodingType = type;
        return this;
    }

    public DeprecatedHttpRequest withCookie(String name, String value) {
        this.cookies.put(name, value);
        return this;
    }

    public DeprecatedHttpResponse send() throws IOException {
        client.getOptions().setTimeout(timeoutInMillis);
        client.getOptions().setUseInsecureSSL(true);
        for (String cookie: cookies.keySet()) {
            client.getCookieManager().addCookie(new Cookie(domain, cookie, cookies.get(cookie)));
        }
        client.getCookieManager().setCookiesEnabled(applyCookies);
        client.getOptions().setRedirectEnabled(followRedirects);
        WebRequest request = new WebRequest(requestUrl(), method);
        request.setRequestParameters(requestParameters());
        if (body != null) request.setRequestBody(body);
        if (encodingType != null) request.setEncodingType(FormEncodingType.getInstance(encodingType));
        request.setAdditionalHeaders(headers);
        // Clear HtmlUnit internal cache to make sure requests are actually sent
        client.getCache().clear();

        return new DeprecatedHttpResponse(client.loadWebResponse(request));
    }

    public DeprecatedHttpResponse get(String path) throws IOException {
        return usingMethod(HttpMethod.GET).on(path).send();
    }

    public DeprecatedHttpResponse post(String path) throws IOException {
        return usingMethod(HttpMethod.POST).on(path).send();
    }

    public DeprecatedHttpResponse put(String path) throws IOException {
        return usingMethod(HttpMethod.PUT).on(path).send();
    }

    public DeprecatedHttpResponse delete(String path) throws IOException {
        return usingMethod(HttpMethod.DELETE).on(path).send();
    }

    public DeprecatedHttpRequest withTimeout(int timeoutInMillis) {
        this.timeoutInMillis = timeoutInMillis;
        return this;
    }

    public DeprecatedHttpRequest usingMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public DeprecatedHttpRequest removeParameters() {
        parameters.clear();
        return this;
    }

    private List<NameValuePair> requestParameters() {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (String name: parameters.keySet()) {
            for (String value : parameters(name)) {
                pairs.add(new NameValuePair(name, value));
            }
        }
        return pairs;
    }

    private URL requestUrl() throws MalformedURLException {
        return new URL((secure ? "https" : "http") + "://" + domain + ":" + port + path);
    }
}