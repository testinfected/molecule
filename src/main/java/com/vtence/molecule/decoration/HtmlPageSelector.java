package com.vtence.molecule.decoration;

import com.vtence.molecule.Response;

import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.http.MimeTypes.HTML;

public class HtmlPageSelector implements Selector {

    public boolean selected(Response response) {
        return isOk(response.statusCode()) && isHtml(response.contentType());
    }

    private boolean isOk(int code) {
        return code == OK.code;
    }

    private boolean isHtml(String contentType) {
        return contentType != null && contentType.startsWith(HTML);
    }
}