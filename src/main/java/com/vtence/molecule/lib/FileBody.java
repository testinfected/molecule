package com.vtence.molecule.lib;

import com.vtence.molecule.Body;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class FileBody implements Body {
    private final File file;

    public FileBody(File file) {
        this.file = file;
    }

    public File file() {
        return file;
    }

    public long size(Charset charset) {
        return file.length();
    }

    public void writeTo(OutputStream out, Charset charset) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            in.transferTo(out);
        }
    }
}
