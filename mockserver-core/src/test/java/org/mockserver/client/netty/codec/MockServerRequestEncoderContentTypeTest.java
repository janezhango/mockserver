package org.mockserver.client.netty.codec;

import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.mappers.ContentTypeMapper;
import org.mockserver.model.Header;
import org.mockserver.model.OutboundHttpRequest;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.JsonSchemaBody.jsonSchema;
import static org.mockserver.model.OutboundHttpRequest.outboundRequest;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.RegexBody.regex;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.model.XmlBody.xml;

/**
 * @author jamesdbloom
 */
public class MockServerRequestEncoderContentTypeTest {

    private List<Object> output;
    private OutboundHttpRequest httpRequest;

    @Before
    public void setupFixture() {
        output = new ArrayList<Object>();
        httpRequest = outboundRequest("localhost", 80, "", request());
    }

    @Test
    public void shouldDecodeBodyWithContentTypeAndNoCharset() {
        // given
        httpRequest.withBody("A normal string with ASCII characters");
        httpRequest.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, MediaType.create("text", "plain").toString()));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpRequest = (FullHttpRequest) output.get(0);
        assertThat(fullHttpRequest.content().toString(ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET), is("A normal string with ASCII characters"));
    }

    @Test
    public void shouldDecodeBodyWithNoContentType() {
        // given
        httpRequest.withBody("A normal string with ASCII characters");

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpRequest = (FullHttpRequest) output.get(0);
        assertThat(fullHttpRequest.content().toString(ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET), is("A normal string with ASCII characters"));
    }

    @Test
    public void shouldTransmitUnencodableCharacters() {
        // given
        httpRequest.withBody("Euro sign: \u20AC", ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET);
        httpRequest.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, MediaType.create("text", "plain").toString()));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpRequest = (FullHttpRequest) output.get(0);
        assertThat(fullHttpRequest.content().toString(ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET), is(new String("Euro sign: \u20AC".getBytes(ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET), ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET)));
    }

    @Test
    public void shouldUseDefaultCharsetIfCharsetNotSupported() {
        // given
        httpRequest.withBody("A normal string with ASCII characters");
        httpRequest.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=invalid-charset"));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpRequest = (FullHttpRequest) output.get(0);
        assertThat(fullHttpRequest.content().toString(ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET), is("A normal string with ASCII characters"));
    }

    @Test
    public void shouldDecodeBodyWithUTF8ContentType() {
        // given
        httpRequest.withBody("avro işarəsi: \u20AC", Charsets.UTF_8);
        httpRequest.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, MediaType.create("text", "plain").withCharset(Charsets.UTF_8).toString()));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpRequest = (FullHttpRequest) output.get(0);
        assertThat(fullHttpRequest.content().toString(Charsets.UTF_8), is("avro işarəsi: \u20AC"));
    }

    @Test
    public void shouldDecodeBodyWithUTF16ContentType() {
        // given
        httpRequest.withBody("我说中国话", Charsets.UTF_16);
        httpRequest.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, MediaType.create("text", "plain").withCharset(Charsets.UTF_16).toString()));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpRequest = (FullHttpRequest) output.get(0);
        assertThat(fullHttpRequest.content().toString(Charsets.UTF_16), is("我说中国话"));
    }

    @Test
    public void shouldEncodeStringBodyWithCharset() {
        // given
        httpRequest.withBody("我说中国话", Charsets.UTF_16);

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpRequest = (FullHttpRequest) output.get(0);
        assertThat(fullHttpRequest.content().toString(Charsets.UTF_16), is("我说中国话"));
        assertThat(fullHttpRequest.headers().get(CONTENT_TYPE), is(MediaType.create("text", "plain").withCharset(Charsets.UTF_16).toString()));
    }

    @Test
    public void shouldEncodeUTF8JsonBodyWithContentType() {
        // given
        httpRequest.withBody("{ \"some_field\": \"我说中国话\" }").withHeader(CONTENT_TYPE, MediaType.JSON_UTF_8.toString());

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(new String(fullHttpResponse.content().array(), Charsets.UTF_8), is("{ \"some_field\": \"我说中国话\" }"));
        assertThat(fullHttpResponse.headers().get(CONTENT_TYPE), is(MediaType.JSON_UTF_8.toString()));
    }

    @Test
    public void shouldEncodeUTF8JsonBodyWithCharset() {
        // given
        httpRequest.withBody(json("{ \"some_field\": \"我说中国话\" }", Charsets.UTF_8));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(new String(fullHttpResponse.content().array(), Charsets.UTF_8), is("{ \"some_field\": \"我说中国话\" }"));
        assertThat(fullHttpResponse.headers().get(CONTENT_TYPE), is(MediaType.JSON_UTF_8.toString()));
    }

    @Test
    public void shouldPreferStringBodyCharacterSet() {
        // given
        httpRequest.withBody("avro işarəsi: \u20AC", Charsets.UTF_16);
        httpRequest.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, MediaType.create("text", "plain").withCharset(Charsets.US_ASCII).toString()));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpRequest = (FullHttpRequest) output.get(0);
        assertThat(fullHttpRequest.content().toString(Charsets.UTF_16), is("avro işarəsi: \u20AC"));
    }

    @Test
    public void shouldReturnNoDefaultContentTypeWhenNoBodySpecified() {
        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), empty());
    }

    @Test
    public void shouldReturnContentTypeForStringBody() {
        // given - a request & response
        httpRequest.withBody("somebody");

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), empty());
    }

    @Test
    public void shouldReturnContentTypeForStringBodyWithContentType() {
        // given - a request & response
        httpRequest.withBody(exact("somebody", MediaType.PLAIN_TEXT_UTF_8));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("text/plain; charset=utf-8"));
    }

    @Test
    public void shouldReturnContentTypeForStringBodyWithCharset() {
        // given - a request & response
        httpRequest.withBody(exact("somebody", Charsets.UTF_16));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("text/plain; charset=utf-16"));
    }

    @Test
    public void shouldReturnContentTypeForJsonBody() {
        // given
        httpRequest.withBody(json("somebody"));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("application/json"));
    }

    @Test
    public void shouldReturnContentTypeForJsonBodyWithContentType() {
        // given - a request & response
        httpRequest.withBody(json("somebody", MediaType.JSON_UTF_8));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("application/json; charset=utf-8"));
    }

    @Test
    public void shouldReturnContentTypeForBinaryBody() {
        // given
        httpRequest.withBody(binary("somebody".getBytes()));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), empty());
    }

    @Test
    public void shouldReturnContentTypeForBinaryBodyWithContentType() {
        // given - a request & response
        httpRequest.withBody(binary("somebody".getBytes(), MediaType.QUICKTIME));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder(MediaType.QUICKTIME.toString()));
    }

    @Test
    public void shouldReturnContentTypeForXmlBody() {
        // given
        httpRequest.withBody(xml("somebody"));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("application/xml"));
    }

    @Test
    public void shouldReturnContentTypeForXmlBodyWithContentType() {
        // given - a request & response
        httpRequest.withBody(xml("somebody", MediaType.XML_UTF_8));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("text/xml; charset=utf-8"));
    }

    @Test
    public void shouldReturnContentTypeForJsonSchemaBody() {
        // given
        httpRequest.withBody(jsonSchema("somebody"));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("application/json"));
    }

    @Test
    public void shouldReturnContentTypeForParameterBody() {
        // given
        httpRequest.withBody(params(param("key", "value")));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("application/x-www-form-urlencoded"));
    }

    @Test
    public void shouldReturnNoContentTypeForBodyWithNoAssociatedContentType() {
        // given
        httpRequest.withBody(regex("some_value"));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), empty());
    }

    @Test
    public void shouldNotSetDefaultContentTypeWhenContentTypeExplicitlySpecified() {
        // given
        httpRequest
                .withBody(json("somebody"))
                .withHeaders(new Header("Content-Type", "some/value"));

        // when
        new MockServerRequestEncoder().encode(null, httpRequest, output);

        // then
        FullHttpRequest fullHttpResponse = (FullHttpRequest) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("some/value"));
    }
}
