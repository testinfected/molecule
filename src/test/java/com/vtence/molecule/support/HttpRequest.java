package com.vtence.molecule.support;

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {

    private final WebClient client;

    private final Map<String, String> parameters = new HashMap<String, String>();
    private final Map<String, String> headers = new HashMap<String, String>();

    private HttpMethod method = HttpMethod.GET;
    private String path = "/";
    private int port;
    private int timeoutInMillis = 5000;
    private boolean followRedirects = true;
    private boolean applyCookies = true;
    private String body;
    private String encodingType;

    public static HttpRequest aRequest() {
        return new HttpRequest();
    }

    public HttpRequest() {
        this(new WebClient());
    }

    public HttpRequest(WebClient client) {
        this.client = client;
    }

    public HttpRequest but() {
        HttpRequest other = new HttpRequest(client);
        other.withTimeout(timeoutInMillis).onPort(port).usingMethod(method).on(path).
                applyCookies(applyCookies).followRedirects(followRedirects).
                withEncodingType(encodingType).withBody(body);
        for (String header : headers.keySet()) {
            other.withHeader(header, headers.get(header));
        }
        for (String name : parameters.keySet()) {
            other.withParameter(name, parameters.get(name));
        }
        return other;
    }

    public HttpRequest applyCookies(boolean apply) {
        this.applyCookies = apply;
        return this;
    }

    public HttpRequest onPort(int port) {
        this.port = port;
        return this;
    }

    public HttpRequest on(String path) {
        this.path = path;
        return this;
    }

    public HttpRequest followRedirects(boolean follow) {
        this.followRedirects = follow;
        return this;
    }

    public HttpRequest withHeader(String header, String value) {
        this.headers.put(header, value);
        return this;
    }

    public HttpRequest withParameter(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    public HttpRequest withBody(String body) {
        this.body = body;
        return this;
    }

    public HttpRequest withEncodingType(String type) {
        this.encodingType = type;
        return this;
    }

    public HttpResponse send() throws IOException {
        client.getOptions().setTimeout(timeoutInMillis);
        if (!applyCookies) client.getCookieManager().clearCookies();
        client.getOptions().setRedirectEnabled(followRedirects);
        WebRequest request = new WebRequest(requestUrl(), method);
        request.setRequestParameters(requestParameters());
        if (body != null) request.setRequestBody(body);
        if (encodingType != null) request.setEncodingType(FormEncodingType.getInstance(encodingType));
        request.setAdditionalHeaders(headers);

        return new HttpResponse(client.loadWebResponse(request));
    }

    public HttpResponse get(String path) throws IOException {
        return usingMethod(HttpMethod.GET).on(path).send();
    }

    public HttpResponse post(String path) throws IOException {
        return usingMethod(HttpMethod.POST).on(path).send();
    }

    public HttpResponse delete(String path) throws IOException {
        return usingMethod(HttpMethod.DELETE).on(path).send();
    }

    public HttpRequest withTimeout(int timeoutInMillis) {
        this.timeoutInMillis = timeoutInMillis;
        return this;
    }

    public HttpRequest usingMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpRequest removeParameters() {
        parameters.clear();
        return this;
    }

    private List<NameValuePair> requestParameters() {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (String name : parameters.keySet()) {
            pairs.add(new NameValuePair(name, parameters.get(name)));
        }
        return pairs;
    }

    private URL requestUrl() throws MalformedURLException {
        return new URL("http://localhost:" + port + path);
    }
}