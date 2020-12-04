package com.vtence.molecule.helpers;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;

public class HexTest {

    HexEncoder codec = new HexEncoder();

    @Test public void
    roundTripping() {
        assertThat("round trip", codec.toHex(codec.fromHex("01ac3f4e418d6a5b19ed")), equalToIgnoringCase("01ac3f4e418d6a5b19ed"));
    }
}
