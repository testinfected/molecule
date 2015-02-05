package com.vtence.molecule.helpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.runners.Parameterized.Parameter;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class HexTest {

    @Parameter(0)
    public byte[] bytes;
    @Parameter(1)
    public String hex;

    @Parameters(name = "0x{1}")
    public static Iterable<Object[]> data() {
        return asList(new Object[][] {
                { new byte[] {}, "" },
                { new byte[] { 0x65, (byte) 0xa3, 0x02, 0x70, (byte) 0xcf, 0x4b }, "65a30270cf4b" },
                { new byte[] { (byte) 0xff, (byte) 0xea, 0x00, 0x74, (byte) 0x9c, 0x2b,
                               0x54, 0x29, 0x48, (byte) 0xc7, (byte) 0xd3, (byte) 0xaa}, "ffea00749c2b542948c7d3aa" },
        });
    }

    @Test public void
    convertsBytesToHexRepresentation() {
        assertThat("hex representation", Hex.from(bytes), equalTo(hex));
    }

    @Test public void
    suppressCoverageNoise() {
        new Hex();
    }
}