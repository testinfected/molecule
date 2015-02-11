package com.vtence.molecule.test;

import com.vtence.molecule.helpers.Joiner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlForm {

    private final Map<String, String> data = new HashMap<String, String>();

    public HtmlForm set(String name, String value) {
        data.put(name, value);
        return this;
    }

    public String contentType() {
        return "application/x-www-form-urlencoded";
    }

    public String encode(Charset charset) {
        List<String> pairs = new ArrayList<String>();
        for (String name : data.keySet()) {
            pairs.add(encode(name, charset) + "=" + encode(data.get(name), charset));
        }
        return Joiner.on("&").join(pairs);
    }

    private String encode(String name, Charset charset) {
        try {
            return URLEncoder.encode(name, charset.name());
        } catch (UnsupportedEncodingException impossible) {
            // we can safely ignore this can't happen
            return null;
        }
    }
}
