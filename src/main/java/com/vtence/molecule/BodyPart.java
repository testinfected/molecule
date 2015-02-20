package com.vtence.molecule;

import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.helpers.Streams;
import com.vtence.molecule.http.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Represents a body part or form item that was received within a <code>multipart/form-data</code> <code>POST</code> request.
 * <br/>
 * Typically a part represents either a text parameter or a file.
 * The contents of the part can be acquired either as an <code>InputStream</code>, a byte array or as a
 * string encoded in the encoding specified with the <code>Content-Type</code> header or in <code>UTF-8</code>.
 */
public class BodyPart {
    private final InputStream input;

    private String name;
    private String filename;
    private String contentType;

    public BodyPart(InputStream input) {
        this.input = input;
    }

    /**
     * Sets the name of this part.
     *
     * @param name the new part name
     */
    public BodyPart name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the name of this part. Typically this is used when the part represents a text parameter rather
     * than a file. However, file parts can also have a name.
     *
     * @return the part name or null if this part has no associated name
     */
    public String name() {
        return name;
    }

    /**
     * Sets the file name of this part.
     *
     * @param filename the new file name
     */
    public BodyPart filename(String filename) {
        this.filename = filename;
        return this;
    }

    /**
     * Gets the file name of this part. Typically this is used when the part represents a file.
     *
     * @return the file name of the part or null if this part has no associated file name
     */
    public String filename() {
        return filename;
    }

    /**
     * Sets the content type of this part.
     *
     * @param contentType the new content type
     */
    public BodyPart contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets the content type of this part.
     *
     * @return the part content type or null if the part has no associated content type
     */
    public String contentType() {
        return contentType;
    }

    /**
     * Gets the content of the part as a string. The encoding of the string is taken from the content type.
     * If no content type is sent the content is decoded in UTF-8.
     *
     * @return the text representation of the content
     * @throws IOException thrown if the content cannot be accessed
     */
    public String text() throws IOException {
        return new String(content(), charset());
    }

    /**
     * Gets the content of the part as a byte array.
     *
     * @return the binary representation of the part
     * @throws IOException thrown if the content can not be accessed
     */
    public byte[] content() throws IOException {
        return Streams.toBytes(input());
    }

    /**
     * Gets the content of the part as an input stream.
     *
     * @return an input stream giving access to the part content
     * @throws IOException thrown if the content cannot be accessed
     */
    public InputStream input() throws IOException {
        return input;
    }

    private Charset charset() {
        ContentType contentType = ContentType.parse(contentType());
        if (contentType == null || contentType.charset() == null) {
            return Charsets.UTF_8;
        }
        return contentType.charset();
    }
}
