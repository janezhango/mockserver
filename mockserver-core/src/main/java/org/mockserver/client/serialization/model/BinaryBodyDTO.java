package org.mockserver.client.serialization.model;

import com.google.common.net.MediaType;
import org.mockserver.model.BinaryBody;

import javax.xml.bind.DatatypeConverter;

/**
 * @author jamesdbloom
 */
public class BinaryBodyDTO extends BodyDTO {

    private String value;

    public BinaryBodyDTO(BinaryBody binaryBody) {
        this(binaryBody, false);
    }

    public BinaryBodyDTO(BinaryBody binaryBody, Boolean not) {
        super(binaryBody.getType(), not, binaryBody.getContentType());
        if (binaryBody.getValue() != null && binaryBody.getValue().length > 0) {
            value = DatatypeConverter.printBase64Binary(binaryBody.getValue());
        }
    }

    protected BinaryBodyDTO() {
    }

    public String getValue() {
        return value;
    }

    public BinaryBody buildObject() {
        return new BinaryBody(DatatypeConverter.parseBase64Binary(value), (contentType != null ? MediaType.parse(contentType) : null));
    }
}
