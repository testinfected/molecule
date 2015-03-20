package com.vtence.molecule.http;

import com.vtence.molecule.Request;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class AcceptEncodingTest {

    @Test public void
    selectsNoEncodingWhenThereAreNoCandidates() {
        assertThat("selected", select("gzip", fromCandidates()), nullValue());
    }

    @Test public void
    selectsAcceptableEncodingWithHighestQuality() {
        assertThat("highest quality", select("compress; q=0.5, gzip", fromCandidates("compress", "gzip")), equalTo("gzip"));
        assertThat("first", select("gzip, deflate", fromCandidates("deflate", "gzip")), equalTo("gzip"));
    }

    @Test public void
    selectsNoEncodingWhenCandidatesAreNotAcceptable() {
        assertThat("selected", select("compress, deflate; q=0", fromCandidates("gzip", "deflate")), nullValue());
    }

    @Test public void
    considersIdentityEncodingAcceptableByDefault() {
        assertThat("no preference", select("", fromCandidates("gzip", "identity")), equalTo("identity"));
        assertThat("none specified supported", select("deflate, gzip", fromCandidates("identity")), equalTo("identity"));
        assertThat("candidates rejected", select("deflate; q=0, gzip; q=0", fromCandidates("gzip", "deflate", "identity")), equalTo("identity"));
        assertThat("all but identity rejected", select("*; q=0, identity; q=0.1", fromCandidates("gzip", "deflate", "identity")), equalTo("identity"));
    }

    @Test public void
    considersIdentityEncodingNoLongerAcceptableWhenExplicitlyOrImplicitlyRefused() {
        assertThat("explicitly refused", select("identity; q=0", fromCandidates("identity")), nullValue());
        assertThat("implicitly refused", select("*; q=0", fromCandidates("identity")), nullValue());
    }

    @Test public void
    selectsFirstOfHighestQualityEncodingsWhenAnyIsAcceptable() {
        assertThat("all accepted", select("*", fromCandidates("gzip", "deflate", "identity")), equalTo("gzip"));
        assertThat("all but gzip preferred", select("gzip; q=0.9, *", fromCandidates("gzip", "deflate", "compress")), equalTo("deflate"));
    }

    @Test public void
    handlesAbsenceOfAcceptEncodingHeader() {
        AcceptEncoding accept = AcceptEncoding.of(new Request());
        assertThat("encoding of missing header", accept.selectBestEncoding("gzip", "identity"), equalTo("identity"));
    }

    private String select(String header, List<String> candidates) {
        AcceptEncoding acceptEncoding = new AcceptEncoding(header);
        return acceptEncoding.selectBestEncoding(candidates);
    }

    public static List<String> fromCandidates(String... candidates) {
        return new ArrayList<String>(Arrays.asList(candidates));
    }
}