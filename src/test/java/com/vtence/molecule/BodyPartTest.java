package com.vtence.molecule;

import com.vtence.molecule.helpers.Charsets;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.vtence.molecule.helpers.Charsets.UTF_16;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class BodyPartTest {

    @Test
    public void encodesTextContentAccordingToContentTypeCharset() throws IOException {
        String originalText = "Les naïfs ægithales hâtifs pondant à Noël où il gèle...";

        BodyPart part = new BodyPart(asUTF16(originalText));
        part.contentType("text/plain; charset=utf-16");

        assertThat("decoded text", part.text(), equalTo(originalText));
    }

    @Test
    public void defaultsToISO88591EncodingWhenNoContentTypeIsSpecified() throws IOException {
        String originalText = "sont sûrs d'être déçus...";

        BodyPart part = new BodyPart(asISO88591(originalText));

        assertThat("decoded text", part.text(), equalTo(originalText));
    }

    @Test
    public void defaultsToISO88591EncodingWhenNoCharsetIsSpecified() throws IOException {
        String originalText = "en voyant leurs drôles d'oeufs abîmés.";

        BodyPart part = new BodyPart(asISO88591(originalText));
        part.contentType("text/plain");

        assertThat("decoded text", part.text(), equalTo(originalText));
    }

    private ByteArrayInputStream asUTF16(String originalText) {
        return new ByteArrayInputStream(originalText.getBytes(UTF_16));
    }

    private ByteArrayInputStream asISO88591(String originalText) {
        return new ByteArrayInputStream(originalText.getBytes(Charsets.ISO_8859_1));
    }
}