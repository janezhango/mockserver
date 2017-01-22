package org.mockserver.client.serialization;

import org.junit.Test;
import org.mockserver.client.serialization.model.*;
import org.mockserver.model.*;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.JsonSchemaBody.jsonSchema;
import static org.mockserver.model.NottableString.string;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.RegexBody.regex;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.model.XPathBody.xpath;
import static org.mockserver.model.XmlBody.xml;

/**
 * @author jamesdbloom
 */
public class HttpRequestSerializerIntegrationTest {


    @Test
    public void shouldIgnoreExtraFields() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "    \"path\": \"somePath\"," + System.getProperty("line.separator") +
                "    \"extra_field\": \"extra_value\"" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setPath(string("somePath"))
                .buildObject(), httpRequest);
    }

    @Test
    public void shouldDeserializeCompleteObject() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "  \"method\" : \"someMethod\"," + System.getProperty("line.separator") +
                "  \"keepAlive\" : false," + System.getProperty("line.separator") +
                "  \"queryStringParameters\" : [ {" + System.getProperty("line.separator") +
                "    \"name\" : \"queryParameterName\"," + System.getProperty("line.separator") +
                "    \"values\" : [ \"queryParameterValue\" ]" + System.getProperty("line.separator") +
                "  } ]," + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"STRING\"," + System.getProperty("line.separator") +
                "    \"value\" : \"somebody\"" + System.getProperty("line.separator") +
                "  }," + System.getProperty("line.separator") +
                "  \"cookies\" : [ {" + System.getProperty("line.separator") +
                "    \"name\" : \"someCookieName\"," + System.getProperty("line.separator") +
                "    \"value\" : \"someCookieValue\"" + System.getProperty("line.separator") +
                "  } ]," + System.getProperty("line.separator") +
                "  \"headers\" : [ {" + System.getProperty("line.separator") +
                "    \"name\" : \"someHeaderName\"," + System.getProperty("line.separator") +
                "    \"values\" : [ \"someHeaderValue\" ]" + System.getProperty("line.separator") +
                "  } ]," + System.getProperty("line.separator") +
                "  \"path\" : \"somePath\"," + System.getProperty("line.separator") +
                "  \"secure\" : true" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setMethod(string("someMethod"))
                .setPath(string("somePath"))
                .setQueryStringParameters(Arrays.<ParameterDTO>asList((ParameterDTO) new ParameterDTO(new Parameter("queryParameterName", Arrays.asList("queryParameterValue")))))
                .setBody(BodyDTO.createDTO(new StringBody("somebody")))
                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                .setSecure(true)
                .setKeepAlive(false)
                .buildObject(), httpRequest);
    }

    @Test
    public void shouldDeserializeStringBodyShorthand() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "  \"body\" : \"somebody\"" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(exact("somebody")))
                .buildObject(), httpRequest);
    }

    @Test
    public void shouldDeserializeStringBodyWithType() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"STRING\"," + System.getProperty("line.separator") +
                "    \"value\" : \"somebody\"" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(exact("somebody")))
                .buildObject(), httpRequest);
    }

    @Test
    public void shouldDeserializeJsonBody() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"JSON\"," + System.getProperty("line.separator") +
                "    \"value\" : \"{ \\\"key\\\": \\\"value\\\" }\"" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        HttpRequest expected = new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(json("{ \"key\": \"value\" }")))
                .buildObject();
        assertEquals(expected, httpRequest);
    }

    @Test
    public void shouldDeserializeJsonSchemaBody() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"JSON_SCHEMA\"," + System.getProperty("line.separator") +
                "    \"value\" : \"{ \\\"key\\\": \\\"value\\\" }\"" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(jsonSchema("{ \"key\": \"value\" }")))
                .buildObject(), httpRequest);
    }

    @Test
    public void shouldDeserializeRegexBody() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"REGEX\"," + System.getProperty("line.separator") +
                "    \"value\" : \"some[a-z]{3}\"" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(regex("some[a-z]{3}")))
                .buildObject(), httpRequest);
    }

    @Test
    public void shouldDeserializeXpathBody() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"XPATH\"," + System.getProperty("line.separator") +
                "    \"value\" : \"/element[key = 'some_key' and value = 'some_value']\"" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(xpath("/element[key = 'some_key' and value = 'some_value']")))
                .buildObject(), httpRequest);
    }

    @Test
    public void shouldDeserializeParameterBody() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"PARAMETERS\"," + System.getProperty("line.separator") +
                "    \"parameters\" : [ {" + System.getProperty("line.separator") +
                "      \"name\" : \"nameOne\"," + System.getProperty("line.separator") +
                "      \"values\" : [ \"valueOne\" ]" + System.getProperty("line.separator") +
                "    }, {" + System.getProperty("line.separator") +
                "      \"name\" : \"nameTwo\"," + System.getProperty("line.separator") +
                "      \"values\" : [ \"valueTwo_One\", \"valueTwo_Two\" ]" + System.getProperty("line.separator") +
                "    } ]" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(params(
                        new Parameter("nameOne", "valueOne"),
                        new Parameter("nameTwo", "valueTwo_One", "valueTwo_Two")
                )))
                .buildObject(), httpRequest);
    }

    @Test
    public void shouldDeserializePartialObject() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "    \"path\": \"somePath\"" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setPath(string("somePath"))
                .buildObject(), httpRequest);
    }

    @Test
    public void shouldDeserializeAsHttpRequestField() throws IOException {
        // given
        String requestBytes = "{" + System.getProperty("line.separator") +
                "    \"httpRequest\": {" + System.getProperty("line.separator") +
                "        \"path\": \"somePath\"," + System.getProperty("line.separator") +
                "        \"queryStringParameters\" : [ {" + System.getProperty("line.separator") +
                "            \"name\" : \"queryParameterName\"," + System.getProperty("line.separator") +
                "            \"values\" : [ \"queryParameterValue\" ]" + System.getProperty("line.separator") +
                "        } ]" + System.getProperty("line.separator") +
                "    }" + System.getProperty("line.separator") +
                "}";

        // when
        HttpRequest httpRequest = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setPath(string("somePath"))
                .setQueryStringParameters(Arrays.<ParameterDTO>asList((ParameterDTO) new ParameterDTO(new Parameter("queryParameterName", Arrays.asList("queryParameterValue")))))
                .buildObject(), httpRequest);
    }

    @Test
    public void shouldSerializeCompleteObject() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setMethod(string("someMethod"))
                        .setPath(string("somePath"))
                        .setQueryStringParameters(Arrays.<ParameterDTO>asList((ParameterDTO) new ParameterDTO(new Parameter("queryParameterName", Arrays.asList("queryParameterValue")))))
                        .setBody(BodyDTO.createDTO(new StringBody("somebody")))
                        .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                        .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                        .buildObject()
        );

        // then
        assertEquals("{" + System.getProperty("line.separator") +
                "  \"method\" : \"someMethod\"," + System.getProperty("line.separator") +
                "  \"path\" : \"somePath\"," + System.getProperty("line.separator") +
                "  \"queryStringParameters\" : [ {" + System.getProperty("line.separator") +
                "    \"name\" : \"queryParameterName\"," + System.getProperty("line.separator") +
                "    \"values\" : [ \"queryParameterValue\" ]" + System.getProperty("line.separator") +
                "  } ]," + System.getProperty("line.separator") +
                "  \"headers\" : [ {" + System.getProperty("line.separator") +
                "    \"name\" : \"someHeaderName\"," + System.getProperty("line.separator") +
                "    \"values\" : [ \"someHeaderValue\" ]" + System.getProperty("line.separator") +
                "  } ]," + System.getProperty("line.separator") +
                "  \"cookies\" : [ {" + System.getProperty("line.separator") +
                "    \"name\" : \"someCookieName\"," + System.getProperty("line.separator") +
                "    \"value\" : \"someCookieValue\"" + System.getProperty("line.separator") +
                "  } ]," + System.getProperty("line.separator") +
                "  \"body\" : \"somebody\"" + System.getProperty("line.separator") +
                "}", jsonHttpRequest);
    }

    @Test
    public void shouldSerializeArray() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(
                new HttpRequest[]{
                        new HttpRequestDTO()
                                .setMethod(string("some_method_one"))
                                .setPath(string("some_path_one"))
                                .setBody(BodyDTO.createDTO(new StringBody("some_body_one")))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("some_header_name_one", Arrays.asList("some_header_value_one")))))
                                .buildObject(),
                        new HttpRequestDTO()
                                .setMethod(string("some_method_two"))
                                .setPath(string("some_path_two"))
                                .setBody(BodyDTO.createDTO(new StringBody("some_body_two")))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("some_header_name_two", Arrays.asList("some_header_value_two")))))
                                .buildObject()
                }
        );

        // then
        assertEquals("[ {" + System.getProperty("line.separator") +
                "  \"method\" : \"some_method_one\"," + System.getProperty("line.separator") +
                "  \"path\" : \"some_path_one\"," + System.getProperty("line.separator") +
                "  \"headers\" : [ {" + System.getProperty("line.separator") +
                "    \"name\" : \"some_header_name_one\"," + System.getProperty("line.separator") +
                "    \"values\" : [ \"some_header_value_one\" ]" + System.getProperty("line.separator") +
                "  } ]," + System.getProperty("line.separator") +
                "  \"body\" : \"some_body_one\"" + System.getProperty("line.separator") +
                "}, {" + System.getProperty("line.separator") +
                "  \"method\" : \"some_method_two\"," + System.getProperty("line.separator") +
                "  \"path\" : \"some_path_two\"," + System.getProperty("line.separator") +
                "  \"headers\" : [ {" + System.getProperty("line.separator") +
                "    \"name\" : \"some_header_name_two\"," + System.getProperty("line.separator") +
                "    \"values\" : [ \"some_header_value_two\" ]" + System.getProperty("line.separator") +
                "  } ]," + System.getProperty("line.separator") +
                "  \"body\" : \"some_body_two\"" + System.getProperty("line.separator") +
                "} ]", jsonHttpRequest);
    }

    @Test
    public void shouldSerializeList() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(
                Arrays.asList(
                        new HttpRequestDTO()
                                .setMethod(string("some_method_one"))
                                .setPath(string("some_path_one"))
                                .setBody(BodyDTO.createDTO(new StringBody("some_body_one")))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("some_header_name_one", Arrays.asList("some_header_value_one")))))
                                .buildObject(),
                        new HttpRequestDTO()
                                .setMethod(string("some_method_two"))
                                .setPath(string("some_path_two"))
                                .setBody(BodyDTO.createDTO(new StringBody("some_body_two")))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("some_header_name_two", Arrays.asList("some_header_value_two")))))
                                .buildObject()
                )
        );

        // then
        assertEquals("[ {" + System.getProperty("line.separator") +
                "  \"method\" : \"some_method_one\"," + System.getProperty("line.separator") +
                "  \"path\" : \"some_path_one\"," + System.getProperty("line.separator") +
                "  \"headers\" : [ {" + System.getProperty("line.separator") +
                "    \"name\" : \"some_header_name_one\"," + System.getProperty("line.separator") +
                "    \"values\" : [ \"some_header_value_one\" ]" + System.getProperty("line.separator") +
                "  } ]," + System.getProperty("line.separator") +
                "  \"body\" : \"some_body_one\"" + System.getProperty("line.separator") +
                "}, {" + System.getProperty("line.separator") +
                "  \"method\" : \"some_method_two\"," + System.getProperty("line.separator") +
                "  \"path\" : \"some_path_two\"," + System.getProperty("line.separator") +
                "  \"headers\" : [ {" + System.getProperty("line.separator") +
                "    \"name\" : \"some_header_name_two\"," + System.getProperty("line.separator") +
                "    \"values\" : [ \"some_header_value_two\" ]" + System.getProperty("line.separator") +
                "  } ]," + System.getProperty("line.separator") +
                "  \"body\" : \"some_body_two\"" + System.getProperty("line.separator") +
                "} ]", jsonHttpRequest);
    }

    @Test
    public void shouldSerializeStringBody() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(exact("somebody")))
                        .buildObject()
        );

        // then
        assertEquals("{" + System.getProperty("line.separator") +
                "  \"body\" : \"somebody\"" + System.getProperty("line.separator") +
                "}", jsonHttpRequest);
    }

    @Test
    public void shouldSerializeJsonBody() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(json("{ \"key\": \"value\" }")))
                        .buildObject()
        );

        // then
        assertEquals("{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"JSON\"," + System.getProperty("line.separator") +
                "    \"json\" : \"{ \\\"key\\\": \\\"value\\\" }\"" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}", jsonHttpRequest);
    }

    @Test
    public void shouldSerializeRegexBody() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(regex("some[a-z]{3}")))
                        .buildObject()
        );

        // then
        assertEquals("{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"REGEX\"," + System.getProperty("line.separator") +
                "    \"regex\" : \"some[a-z]{3}\"" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}", jsonHttpRequest);
    }

    @Test
    public void shouldSerializeXpathBody() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(xpath("/element[key = 'some_key' and value = 'some_value']")))
                        .buildObject()
        );

        // then
        assertEquals("{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"XPATH\"," + System.getProperty("line.separator") +
                "    \"xpath\" : \"/element[key = 'some_key' and value = 'some_value']\"" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}", jsonHttpRequest);
    }

    @Test
    public void shouldSerializeXmlBody() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(xml("<some><xml></xml></some>")))
                        .buildObject()
        );

        // then
        assertEquals("{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"XML\"," + System.getProperty("line.separator") +
                "    \"xml\" : \"<some><xml></xml></some>\"" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}", jsonHttpRequest);
    }

    @Test
    public void shouldSerializeParameterBody() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(params(
                                new Parameter("nameOne", "valueOne"),
                                new Parameter("nameTwo", "valueTwo_One", "valueTwo_Two")
                        )))
                        .buildObject()
        );

        // then
        assertEquals("{" + System.getProperty("line.separator") +
                "  \"body\" : {" + System.getProperty("line.separator") +
                "    \"type\" : \"PARAMETERS\"," + System.getProperty("line.separator") +
                "    \"parameters\" : [ {" + System.getProperty("line.separator") +
                "      \"name\" : \"nameOne\"," + System.getProperty("line.separator") +
                "      \"values\" : [ \"valueOne\" ]" + System.getProperty("line.separator") +
                "    }, {" + System.getProperty("line.separator") +
                "      \"name\" : \"nameTwo\"," + System.getProperty("line.separator") +
                "      \"values\" : [ \"valueTwo_One\", \"valueTwo_Two\" ]" + System.getProperty("line.separator") +
                "    } ]" + System.getProperty("line.separator") +
                "  }" + System.getProperty("line.separator") +
                "}", jsonHttpRequest);
    }

    @Test
    public void shouldSerializePartialRequestAndResponse() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(new HttpRequestDTO()
                        .setPath(string("somePath"))
                        .buildObject()
        );

        // then
        assertEquals("{" + System.getProperty("line.separator") +
                "  \"path\" : \"somePath\"" + System.getProperty("line.separator") +
                "}", jsonHttpRequest);
    }

    @Test
    public void shouldSerializePartialHttpRequest() throws IOException {
        // when
        String jsonHttpRequest = new HttpRequestSerializer().serialize(new HttpRequestDTO()
                        .setPath(string("somePath"))
                        .buildObject()
        );

        // then
        assertEquals("{" + System.getProperty("line.separator") +
                "  \"path\" : \"somePath\"" + System.getProperty("line.separator") +
                "}", jsonHttpRequest);
    }
}
