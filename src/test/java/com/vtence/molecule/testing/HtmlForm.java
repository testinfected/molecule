package com.vtence.molecule.testing;

import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.helpers.Joiner;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HtmlForm {

    private final List<Field> fields = new ArrayList<Field>();
    private Charset charset = Charsets.UTF_8;

    public String contentType() {
        return "application/x-www-form-urlencoded";
    }

    public HtmlForm charset(String charsetName) {
        return charset(Charset.forName(charsetName));
    }

    public HtmlForm charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public HtmlForm addField(String name, String value) {
        fields.add(new Field(name, value));
        return this;
    }

    public String encode() {
        List<String> pairs = new ArrayList<String>();
        for (Field field : fields) {
            pairs.add(field.encode(charset));
        }
        return Joiner.on("&").join(pairs);
    }

    static class Field {
        private final String name;
        private final String value;

        public Field(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String encode(Charset charset) {
            URLEscaper escaper = URLEscaper.to(charset);
            return escaper.escape(name) + "=" + escaper.escape(value);
        }
    }
}
