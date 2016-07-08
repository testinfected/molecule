package com.vtence.molecule.http;

import com.vtence.molecule.lib.MimeEncoder;
import org.junit.Test;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class BasicCredentialsTest {

    MimeEncoder packer = MimeEncoder.inUtf8();

    @Test
    public void unpacksCredentialsAsUTF8() {
        BasicCredentials auth = BasicCredentials.decode(packer.encode("œufs:abîmés"));

        assertThat("username", auth.username(), equalTo("œufs"));
        assertThat("missing password", auth.password(), equalTo("abîmés"));
    }

    @Test
    public void considersMissingPasswordAsEmpty() {
        BasicCredentials auth = new BasicCredentials("username");

        assertThat("username", auth.username(), equalTo("username"));
        assertThat("missing password", auth.password(), emptyString());
    }

    @Test
    public void considersMissingUsernameAsEmpty() {
        BasicCredentials auth = new BasicCredentials();

        assertThat("username", auth.username(), emptyString());
        assertThat("password", auth.password(), emptyString());
    }
}