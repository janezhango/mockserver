package org.mockserver.model;

import com.google.common.net.MediaType;
import org.mockserver.client.serialization.Base64Converter;

/**
 * @author jamesdbloom
 */
public class BinaryBody extends Body<byte[]> {

    private final byte[] bytes;

    public BinaryBody(byte[] bytes) {
        this(bytes, null);
    }

    public BinaryBody(byte[] bytes, MediaType contentType) {
        super(Type.BINARY, contentType);
        this.bytes = bytes;
    }

    public static BinaryBody binary(byte[] body) {
        return new BinaryBody(body);
    }

    public static BinaryBody binary(byte[] body, MediaType contentType) {
        return new BinaryBody(body, contentType);
    }

    public byte[] getValue() {
        return bytes;
    }

    public byte[] getRawBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return bytes != null ? Base64Converter.bytesToBase64String(bytes) : null;
    }
}
