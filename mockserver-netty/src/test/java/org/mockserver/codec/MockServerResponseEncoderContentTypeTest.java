package org.mockserver.codec;

import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.mappers.ContentTypeMapper;
import org.mockserver.model.Header;
import org.mockserver.model.HttpResponse;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.JsonSchemaBody.jsonSchema;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.RegexBody.regex;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.model.XmlBody.xml;

/**
 * @author jamesdbloom
 */
public class MockServerResponseEncoderContentTypeTest {

    private List<Object> output;
    private HttpResponse httpResponse;

    @Before
    public void setupFixture() {
        output = new ArrayList<Object>();
        httpResponse = response();
    }

    @Test
    public void shouldDecodeBodyWithContentTypeAndNoCharset() {
        // given
        httpResponse.withBody("avro işarəsi: \u20AC");
        httpResponse.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, MediaType.create("text", "plain").toString()));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.content().array(), is("avro işarəsi: \u20AC".getBytes(ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET)));
    }

    @Test
    public void shouldDecodeBodyWithNoContentType() {
        // given
        httpResponse.withBody("avro işarəsi: \u20AC");

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.content().array(), is("avro işarəsi: \u20AC".getBytes(ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET)));
    }

    @Test
    public void shouldTransmitUnencodableCharacters() {
        // given
        httpResponse.withBody("avro işarəsi: \u20AC", ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET);
        httpResponse.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, MediaType.create("text", "plain").toString()));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.content().array(), is("avro işarəsi: \u20AC".getBytes(ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET)));
    }

    @Test
    public void shouldUseDefaultCharsetIfCharsetNotSupported() {
        // given
        httpResponse.withBody("avro işarəsi: \u20AC");
        httpResponse.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=invalid-charset"));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.content().array(), is("avro işarəsi: \u20AC".getBytes(ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET)));
    }

    @Test
    public void shouldDecodeBodyWithUTF8ContentType() {
        // given
        httpResponse.withBody("avro işarəsi: \u20AC", Charsets.UTF_8);
        httpResponse.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, MediaType.create("text", "plain").withCharset(Charsets.UTF_8).toString()));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(new String(fullHttpResponse.content().array(), Charsets.UTF_8), is("avro işarəsi: \u20AC"));
    }

    @Test
    public void shouldDecodeBodyWithUTF16ContentType() {
        // given
        httpResponse.withBody("我说中国话", Charsets.UTF_16);
        httpResponse.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, MediaType.create("text", "plain").withCharset(Charsets.UTF_16).toString()));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(new String(fullHttpResponse.content().array(), Charsets.UTF_16), is("我说中国话"));
    }

    @Test
    public void shouldEncodeStringBodyWithCharset() {
        // given
        httpResponse.withBody("我说中国话", Charsets.UTF_16);

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpRequest = (FullHttpResponse) output.get(0);
        assertThat(new String(fullHttpRequest.content().array(), Charsets.UTF_16), is("我说中国话"));
        assertThat(fullHttpRequest.headers().get(CONTENT_TYPE), is(MediaType.create("text", "plain").withCharset(Charsets.UTF_16).toString()));
    }

    @Test
    public void shouldEncodeUTF8JsonBodyWithContentType() {
        // given
        httpResponse.withBody("{ \"some_field\": \"我说中国话\" }").withHeader(CONTENT_TYPE, MediaType.JSON_UTF_8.toString());

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(new String(fullHttpResponse.content().array(), Charsets.UTF_8), is("{ \"some_field\": \"我说中国话\" }"));
        assertThat(fullHttpResponse.headers().get(CONTENT_TYPE), is(MediaType.JSON_UTF_8.toString()));
    }

    @Test
    public void shouldEncodeUTF8JsonBodyWithCharset() {
        // given
        httpResponse.withBody(json("{ \"some_field\": \"我说中国话\" }", Charsets.UTF_8));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(new String(fullHttpResponse.content().array(), Charsets.UTF_8), is("{ \"some_field\": \"我说中国话\" }"));
        assertThat(fullHttpResponse.headers().get(CONTENT_TYPE), is(MediaType.JSON_UTF_8.toString()));
    }

    @Test
    public void shouldPreferStringBodyCharacterSet() {
        // given
        httpResponse.withBody("avro işarəsi: \u20AC", Charsets.UTF_16);
        httpResponse.withHeader(new Header(HttpHeaders.Names.CONTENT_TYPE, MediaType.create("text", "plain").withCharset(Charsets.US_ASCII).toString()));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(new String(fullHttpResponse.content().array(), Charsets.UTF_16), is("avro işarəsi: \u20AC"));
    }

    @Test
    public void shouldReturnNoDefaultContentTypeWhenNoBodySpecified() {
        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), empty());
    }

    @Test
    public void shouldReturnContentTypeForStringBody() {
        // given - a request & response
        httpResponse.withBody("somebody");

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), empty());
    }

    @Test
    public void shouldReturnContentTypeForStringBodyWithContentType() {
        // given - a request & response
        httpResponse.withBody(exact("somebody", MediaType.PLAIN_TEXT_UTF_8));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("text/plain; charset=utf-8"));
    }

    @Test
    public void shouldReturnContentTypeForStringBodyWithCharset() {
        // given - a request & response
        httpResponse.withBody(exact("somebody", Charsets.UTF_16));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("text/plain; charset=utf-16"));
    }

    @Test
    public void shouldReturnContentTypeForJsonBody() {
        // given
        httpResponse.withBody(json("somebody"));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("application/json"));
    }

    @Test
    public void shouldReturnContentTypeForJsonBodyWithContentType() {
        // given - a request & response
        httpResponse.withBody(json("somebody", MediaType.JSON_UTF_8));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("application/json; charset=utf-8"));
    }

    @Test
    public void shouldReturnContentTypeForBinaryBody() {
        // given
        httpResponse.withBody(binary("somebody".getBytes()));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), empty());
    }

    @Test
    public void shouldReturnContentTypeForBinaryBodyWithContentType() {
        // given - a request & response
        httpResponse.withBody(binary("somebody".getBytes(), MediaType.QUICKTIME));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder(MediaType.QUICKTIME.toString()));
    }

    @Test
    public void shouldReturnContentTypeForXmlBody() {
        // given
        httpResponse.withBody(xml("somebody"));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("application/xml"));
    }

    @Test
    public void shouldReturnContentTypeForXmlBodyWithContentType() {
        // given - a request & response
        httpResponse.withBody(xml("somebody", MediaType.XML_UTF_8));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("text/xml; charset=utf-8"));
    }

    @Test
    public void shouldReturnContentTypeForJsonSchemaBody() {
        // given
        httpResponse.withBody(jsonSchema("somebody"));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("application/json"));
    }

    @Test
    public void shouldReturnContentTypeForParameterBody() {
        // given
        httpResponse.withBody(params(param("key", "value")));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("application/x-www-form-urlencoded"));
    }

    @Test
    public void shouldReturnNoContentTypeForBodyWithNoAssociatedContentType() {
        // given
        httpResponse.withBody(regex("some_value"));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), empty());
    }

    @Test
    public void shouldNotSetDefaultContentTypeWhenContentTypeExplicitlySpecified() {
        // given
        httpResponse
                .withBody(json("somebody"))
                .withHeaders(new Header("Content-Type", "some/value"));

        // when
        new MockServerResponseEncoder().encode(null, httpResponse, output);

        // then
        FullHttpResponse fullHttpResponse = (FullHttpResponse) output.get(0);
        assertThat(fullHttpResponse.headers().getAll("Content-Type"), containsInAnyOrder("some/value"));
    }
}
