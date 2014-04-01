package com.vtence.molecule;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileBody implements Body {
    private static final int SIZE_8K = 8 * 1024;

    private final File file;
    private final int chunkSize;

    public FileBody(File file) {
        this(file, SIZE_8K);
    }

    public FileBody(File file, int chunkSize) {
        this.file = file;
        this.chunkSize = chunkSize;
    }

    public long size() {
        return file.length();
    }

    public void writeTo(OutputStream out) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            copy(out, in);
        } finally {
            close(in);
        }
    }

    private void copy(OutputStream out, InputStream in) throws IOException {
        byte[] buffer = new byte[chunkSize];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    public void close() throws IOException {
    }
}
