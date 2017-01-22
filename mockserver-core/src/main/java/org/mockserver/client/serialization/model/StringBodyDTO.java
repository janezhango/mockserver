package org.mockserver.client.serialization.model;

import com.google.common.net.MediaType;
import org.mockserver.model.StringBody;

import java.nio.charset.Charset;

/**
 * @author jamesdbloom
 */
public class StringBodyDTO extends BodyDTO {

    private String string;

    public StringBodyDTO(StringBody stringBody) {
        this(stringBody, stringBody.getNot());
    }

    public StringBodyDTO(StringBody stringBody, Boolean not) {
        super(stringBody.getType(), not, stringBody.getContentType());
        string = stringBody.getValue();
    }

    protected StringBodyDTO() {
    }

    public String getString() {
        return string;
    }

    public StringBody buildObject() {
        return new StringBody(string, (contentType != null ? MediaType.parse(contentType) : null));
    }
}
