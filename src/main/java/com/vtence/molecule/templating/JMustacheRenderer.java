package com.vtence.molecule.templating;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.vtence.molecule.templating.JMustacheRenderer.ClasspathTemplateLoader.classpath;
import static com.vtence.molecule.templating.JMustacheRenderer.FileTemplateLoader.dir;

public class JMustacheRenderer implements RenderingEngine {

    private Mustache.Compiler mustache;

    public static JMustacheRenderer from(Mustache.TemplateLoader loader) {
        return new JMustacheRenderer(Mustache.compiler().withLoader(loader));
    }

    public JMustacheRenderer(Mustache.Compiler compiler) {
        this.mustache = compiler;
    }

    public static JMustacheRenderer fromClasspath() {
        return from(classpath());
    }

    public static JMustacheRenderer fromClasspath(String root) {
        return from(classpath(root));
    }

    public static JMustacheRenderer fromDir(File root) {
        return from(dir(root));
    }

    public JMustacheRenderer defaultValue(String defaultValue) {
        mustache = mustache.defaultValue(defaultValue);
        return this;
    }

    public JMustacheRenderer nullValue(String nullValue) {
        mustache = mustache.nullValue(nullValue);
        return this;
    }

    public void render(Writer out, String templateName, Object context) throws IOException {
        try (Reader source = load(templateName)) {
            Template template = mustache.compile(source);
            template.execute(context, out);
        }
    }

    private Reader load(String templateName) throws IOException {
        try {
            return mustache.loader.getTemplate(templateName);
        } catch (Exception e) {
            throw new IOException("loading template `" + templateName + "`", e);
        }
    }

    public static abstract class AbstractTemplateLoader implements Mustache.TemplateLoader {

        protected Charset encoding = StandardCharsets.UTF_8;
        protected String extension = "mustache";

        public AbstractTemplateLoader usingEncoding(String charsetName) {
            return usingEncoding(Charset.forName(charsetName));
        }

        public AbstractTemplateLoader usingEncoding(Charset charset) {
            this.encoding = charset;
            return this;
        }

        public AbstractTemplateLoader usingExtension(String ext) {
            this.extension = ext;
            return this;
        }
    }

    public static class ClasspathTemplateLoader extends AbstractTemplateLoader {

        private final String root;
        private ClassLoader classLoader;

        public ClasspathTemplateLoader(String root) {
            this.root = root;
            usingClassLoader(Thread.currentThread().getContextClassLoader());
        }

        public static ClasspathTemplateLoader classpath() {
            return classpath("");
        }

        public static ClasspathTemplateLoader classpath(String root) {
            return new ClasspathTemplateLoader(root);
        }

        public ClasspathTemplateLoader usingClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public Reader getTemplate(String name) throws IOException {
            String resource = (root.equals("") ? "" : root + "/") + name + "." + extension;
            URL location = classLoader.getResource(resource);
            if (location == null) throw new FileNotFoundException("classpath:" + resource);
            return new InputStreamReader(location.openStream(), encoding);
        }
    }

    public static class FileTemplateLoader extends AbstractTemplateLoader {
        private final File root;

        public FileTemplateLoader(File root) {
            this.root = root;
        }

        public static FileTemplateLoader dir(File root) {
            return new FileTemplateLoader(root);
        }

        public Reader getTemplate(String name) throws IOException {
            return new InputStreamReader(new FileInputStream(templateFile(name)), encoding);
        }

        private File templateFile(String name) {
            return new File(root, name + "." + extension);
        }
    }
}