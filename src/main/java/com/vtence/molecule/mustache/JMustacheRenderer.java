package com.vtence.molecule.mustache;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import com.vtence.molecule.templating.RenderingEngine;
import com.vtence.molecule.util.Charsets;
import com.vtence.molecule.util.Streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

public class JMustacheRenderer implements RenderingEngine {

    private File path = new File(".");
    private Mustache.Compiler mustache;
    private Charset encoding = Charsets.UTF_8;
    private String extension = "mustache";

    public static JMustacheRenderer templatesIn(File folder) {
        return new JMustacheRenderer().templateDir(folder);
    }

    public JMustacheRenderer() {
        this(Mustache.compiler());
    }

    public JMustacheRenderer(Mustache.Compiler compiler) {
        this.mustache = compiler.withLoader(new Mustache.TemplateLoader() {
            public Reader getTemplate(String name) throws Exception {
                return load(name);
            }
        });
    }

    public JMustacheRenderer templateDir(File dir) {
        this.path = dir;
        return this;
    }

    public JMustacheRenderer extension(String ext) {
        this.extension = ext;
        return this;
    }

    public JMustacheRenderer defaultValue(String defaultValue) {
        mustache = mustache.defaultValue(defaultValue);
        return this;
    }

    public JMustacheRenderer nullValue(String nullValue) {
        mustache = mustache.nullValue(nullValue);
        return this;
    }

    public JMustacheRenderer encoding(String charsetName) {
        encoding(Charset.forName(charsetName));
        return this;
    }

    public JMustacheRenderer encoding(Charset charset) {
        this.encoding = charset;
        return this;
    }

    public void render(Writer out, String view, Object context) throws IOException {
        Reader source = null;
        try {
            source = load(view);
            Template template = mustache.compile(source);
            template.execute(context, out);
        } finally {
            Streams.close(source);
        }
    }

    private Reader load(String name) throws IOException {
        return new InputStreamReader(new FileInputStream(templateFile(name)), encoding);
    }

    private File templateFile(String name) {
        return new File(path, name + "." + extension);
    }
}