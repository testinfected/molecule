package com.vtence.molecule.helpers;

import org.junit.Test;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JoinerTest {

    @Test
    public void ignoresEmptySequences() {
        assertThat("joined", Joiner.on(", ").join(emptyList()), equalTo(""));
    }
}