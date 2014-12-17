package com.vtence.molecule.helpers;

import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class StreamsTest {

    @Test
    public void ignoresIOExceptionWhenClosingStreams() throws Exception {
        Streams.close(new Closeable() {
            public void close() throws IOException {
                throw new IOException("IO Error");
            }
        });
        assertTrue(true);
    }

    @Test
    public void suppressCoverageNoise() {
        new Streams();
    }
}