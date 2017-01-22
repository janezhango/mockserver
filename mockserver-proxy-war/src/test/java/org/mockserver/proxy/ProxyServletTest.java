package org.mockserver.proxy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockserver.client.netty.NettyHttpClient;
import org.mockserver.client.serialization.ExpectationSerializer;
import org.mockserver.client.serialization.HttpRequestSerializer;
import org.mockserver.client.serialization.VerificationSequenceSerializer;
import org.mockserver.client.serialization.VerificationSerializer;
import org.mockserver.filters.RequestFilter;
import org.mockserver.filters.RequestResponseLogFilter;
import org.mockserver.filters.ResponseFilter;
import org.mockserver.mappers.HttpServletRequestToMockServerRequestDecoder;
import org.mockserver.mappers.MockServerResponseToHttpServletResponseEncoder;
import org.mockserver.model.*;
import org.mockserver.filters.RequestLogFilter;
import org.mockserver.verify.Verification;
import org.mockserver.verify.VerificationSequence;
import org.mockserver.verify.VerificationTimes;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.OutboundHttpRequest.outboundRequest;

public class ProxyServletTest {

    @Mock
    private HttpServletRequestToMockServerRequestDecoder mockHttpServletRequestToMockServerRequestDecoder;
    @Mock
    private MockServerResponseToHttpServletResponseEncoder mockServerResponseToHttpServletResponseEncoder;
    @Mock
    private NettyHttpClient mockNettyHttpClient;
    @Mock
    private RequestLogFilter mockRequestLogFilter;
    @Mock
    private RequestResponseLogFilter mockRequestResponseLogFilter;
    @Mock
    private HttpRequestSerializer mockHttpRequestSerializer;
    @Mock
    private ExpectationSerializer mockExpectationSerializer;
    @Mock
    private VerificationSerializer mockVerificationSerializer;
    @Mock
    private VerificationSequenceSerializer mockVerificationSequenceSerializer;
    @InjectMocks
    private ProxyServlet proxyServlet;
    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;
    private OutboundHttpRequest outboundHttpRequest;
    private HttpResponse httpResponse;
    private ArgumentCaptor<OutboundHttpRequest> httpRequestArgumentCaptor;

    @Before
    public void setupMocks() {
        // create mocks
        proxyServlet = new ProxyServlet();
        initMocks(this);

        // additional mock objects
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();
        outboundHttpRequest = outboundRequest("localhost", 80, "", request().withHeader(header("Host", "localhost")).withPath("some_path"));
        httpResponse = new HttpResponse();

        // mappers
        when(mockHttpServletRequestToMockServerRequestDecoder.mapHttpServletRequestToMockServerRequest(any(MockHttpServletRequest.class))).thenReturn(outboundHttpRequest);
        httpRequestArgumentCaptor = ArgumentCaptor.forClass(OutboundHttpRequest.class);
        when(mockNettyHttpClient.sendRequest(httpRequestArgumentCaptor.capture())).thenReturn(httpResponse);
    }

    @Test
    public void shouldProxyGETRequest() {
        // when
        proxyServlet.doGet(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockHttpServletRequestToMockServerRequestDecoder).mapHttpServletRequestToMockServerRequest(same(mockHttpServletRequest));
        verify(mockServerResponseToHttpServletResponseEncoder).mapMockServerResponseToHttpServletResponse(same(httpResponse), same(mockHttpServletResponse));
        verify(mockNettyHttpClient).sendRequest(outboundHttpRequest);
    }

    @Test
    public void shouldProxyHEADRequest() {
        // when
        proxyServlet.doHead(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockHttpServletRequestToMockServerRequestDecoder).mapHttpServletRequestToMockServerRequest(same(mockHttpServletRequest));
        verify(mockServerResponseToHttpServletResponseEncoder).mapMockServerResponseToHttpServletResponse(same(httpResponse), same(mockHttpServletResponse));
        verify(mockNettyHttpClient).sendRequest(outboundHttpRequest);
    }

    @Test
    public void shouldProxyPOSTRequest() {
        // when
        proxyServlet.doPost(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockHttpServletRequestToMockServerRequestDecoder).mapHttpServletRequestToMockServerRequest(same(mockHttpServletRequest));
        verify(mockServerResponseToHttpServletResponseEncoder).mapMockServerResponseToHttpServletResponse(same(httpResponse), same(mockHttpServletResponse));
        verify(mockNettyHttpClient).sendRequest(outboundHttpRequest);
    }

    @Test
    public void shouldProxyPUTRequest() {
        // when
        proxyServlet.doPut(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockHttpServletRequestToMockServerRequestDecoder).mapHttpServletRequestToMockServerRequest(same(mockHttpServletRequest));
        verify(mockServerResponseToHttpServletResponseEncoder).mapMockServerResponseToHttpServletResponse(same(httpResponse), same(mockHttpServletResponse));
        verify(mockNettyHttpClient).sendRequest(outboundHttpRequest);
    }

    @Test
    public void shouldProxyDELETERequest() {
        // when
        proxyServlet.doDelete(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockHttpServletRequestToMockServerRequestDecoder).mapHttpServletRequestToMockServerRequest(same(mockHttpServletRequest));
        verify(mockServerResponseToHttpServletResponseEncoder).mapMockServerResponseToHttpServletResponse(same(httpResponse), same(mockHttpServletResponse));
        verify(mockNettyHttpClient).sendRequest(outboundHttpRequest);
    }

    @Test
    public void shouldProxyOPTIONSRequest() {
        // when
        proxyServlet.doOptions(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockHttpServletRequestToMockServerRequestDecoder).mapHttpServletRequestToMockServerRequest(same(mockHttpServletRequest));
        verify(mockServerResponseToHttpServletResponseEncoder).mapMockServerResponseToHttpServletResponse(same(httpResponse), same(mockHttpServletResponse));
        verify(mockNettyHttpClient).sendRequest(outboundHttpRequest);
    }

    @Test
    public void shouldProxyTRACERequest() {
        // when
        proxyServlet.doTrace(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockHttpServletRequestToMockServerRequestDecoder).mapHttpServletRequestToMockServerRequest(same(mockHttpServletRequest));
        verify(mockServerResponseToHttpServletResponseEncoder).mapMockServerResponseToHttpServletResponse(same(httpResponse), same(mockHttpServletResponse));
        verify(mockNettyHttpClient).sendRequest(outboundHttpRequest);
    }

    @Test
    public void shouldCallMatchingFiltersBeforeForwardingRequest() {
        // given
        // - add first filter
        RequestFilter filter = mock(RequestFilter.class);
        proxyServlet.withFilter(outboundHttpRequest, filter);
        // - add first filter with other request
        HttpRequest someOtherRequest = request().withPath("some_other_path");
        proxyServlet.withFilter(someOtherRequest, filter);
        // - add second filter
        RequestFilter someOtherFilter = mock(RequestFilter.class);
        proxyServlet.withFilter(someOtherRequest, someOtherFilter);

        // when
        proxyServlet.doGet(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(filter, times(1)).onRequest(same(outboundHttpRequest));
        verify(filter, times(0)).onRequest(same(someOtherRequest));
        verifyZeroInteractions(someOtherFilter);
    }

    @Test
    public void shouldApplyFiltersBeforeAndAfterRequest() {
        // given
        // - add first filter
        ResponseFilter filter = mock(ResponseFilter.class);
        when(filter.onResponse(any(HttpRequest.class), any(HttpResponse.class))).thenReturn(new HttpResponse());
        proxyServlet.withFilter(outboundHttpRequest, filter);
        // - add first filter with other request
        HttpRequest someOtherRequest = request().withPath("some_other_path");
        proxyServlet.withFilter(someOtherRequest, filter);
        // - add second filter
        ResponseFilter someOtherFilter = mock(ResponseFilter.class);
        proxyServlet.withFilter(someOtherRequest, someOtherFilter);

        // when
        proxyServlet.doGet(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(filter, times(1)).onResponse(same(outboundHttpRequest), same(httpResponse));
        verify(filter, times(0)).onResponse(same(someOtherRequest), same(httpResponse));
        verifyZeroInteractions(someOtherFilter);
    }

    @Test
    public void shouldNotForwardHopByHopHeaders() {
        // given
        outboundHttpRequest.withHeaders(
                header("host", "localhost"),
                header("some_other_header"),
                header("proxy-connection"),
                header("connection"),
                header("keep-alive"),
                header("transfer-encoding"),
                header("te"),
                header("trailer"),
                header("proxy-authorization"),
                header("proxy-authenticate"),
                header("upgrade")
        );

        // when
        proxyServlet.doGet(mockHttpServletRequest, mockHttpServletResponse);

        // then
        HttpRequest actual = httpRequestArgumentCaptor.getValue();
        assertEquals(actual.getHeaders().size(), 2);
    }

    @Test
    public void shouldDumpToLogAsJSON() {
        // given
        mockHttpServletRequest.setRequestURI("/dumpToLog");
        mockHttpServletRequest.setContent("body".getBytes());
        when(mockHttpRequestSerializer.deserialize("body")).thenReturn(outboundHttpRequest);

        // when
        proxyServlet.doPut(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockRequestResponseLogFilter).dumpToLog(outboundHttpRequest, false);
        assertEquals(HttpStatusCode.ACCEPTED_202.code(), mockHttpServletResponse.getStatus());
    }

    @Test
    public void shouldDumpToLogAsJava() {
        // given
        mockHttpServletRequest.setRequestURI("/dumpToLog");
        mockHttpServletRequest.setContent("body".getBytes());
        mockHttpServletRequest.addParameter("type", "java");
        when(mockHttpRequestSerializer.deserialize("body")).thenReturn(outboundHttpRequest);

        // when
        proxyServlet.doPut(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockRequestResponseLogFilter).dumpToLog(outboundHttpRequest, true);
        assertEquals(HttpStatusCode.ACCEPTED_202.code(), mockHttpServletResponse.getStatus());
    }

    @Test
    public void shouldReturnRecordedRequests() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/retrieve");
        httpServletRequest.setContent("requestBytes".getBytes());

        // and - a request matcher
        HttpRequest request = new HttpRequest();
        when(mockHttpRequestSerializer.deserialize(anyString())).thenReturn(request);

        // and - a set of requests retrieved from the log
        HttpRequest[] httpRequests = {request, request};
        when(mockRequestLogFilter.retrieve(any(HttpRequest.class))).thenReturn(httpRequests);
        when(mockHttpRequestSerializer.serialize(httpRequests)).thenReturn("request_response");

        // when
        proxyServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockRequestLogFilter).retrieve(request);
        assertThat(httpServletResponse.getContentAsByteArray(), is("request_response".getBytes()));
        assertThat(httpServletResponse.getStatus(), is(HttpStatusCode.OK_200.code()));
        verifyNoMoreInteractions(mockHttpServletRequestToMockServerRequestDecoder);
    }

    @Test
    public void shouldVerifyRequestNotMatching() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/verify");
        Verification verification = new Verification().withRequest(request()).withTimes(VerificationTimes.once());

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockVerificationSerializer.deserialize(requestBytes)).thenReturn(verification);
        when(mockRequestLogFilter.verify(verification)).thenReturn("verification_error");

        // when
        proxyServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockRequestLogFilter).verify(verification);
        assertThat(httpServletResponse.getContentAsString(), is("verification_error"));
        assertThat(httpServletResponse.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE_406.code()));
        verifyNoMoreInteractions(mockHttpServletRequestToMockServerRequestDecoder);
    }

    @Test
    public void shouldVerifyRequestMatching() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/verify");
        Verification verification = new Verification().withRequest(request()).withTimes(VerificationTimes.once());

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockVerificationSerializer.deserialize(requestBytes)).thenReturn(verification);
        when(mockRequestLogFilter.verify(verification)).thenReturn("");

        // when
        proxyServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockRequestLogFilter).verify(verification);
        assertThat(httpServletResponse.getContentAsString(), is(""));
        assertThat(httpServletResponse.getStatus(), is(HttpStatusCode.ACCEPTED_202.code()));
        verifyNoMoreInteractions(mockHttpServletRequestToMockServerRequestDecoder);
    }

    @Test
    public void shouldVerifySequenceRequestNotMatching() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/verifySequence");
        VerificationSequence verification = new VerificationSequence().withRequests(request("one"), request("two"));

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockVerificationSequenceSerializer.deserialize(requestBytes)).thenReturn(verification);
        when(mockRequestLogFilter.verify(verification)).thenReturn("verification_error");

        // when
        proxyServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockRequestLogFilter).verify(verification);
        assertThat(httpServletResponse.getContentAsString(), is("verification_error"));
        assertThat(httpServletResponse.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE_406.code()));
        verifyNoMoreInteractions(mockHttpServletRequestToMockServerRequestDecoder);
    }

    @Test
    public void shouldVerifySequenceRequestMatching() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/verifySequence");
        VerificationSequence verification = new VerificationSequence().withRequests(request("one"), request("two"));

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockVerificationSequenceSerializer.deserialize(requestBytes)).thenReturn(verification);
        when(mockRequestLogFilter.verify(verification)).thenReturn("");

        // when
        proxyServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockRequestLogFilter).verify(verification);
        assertThat(httpServletResponse.getContentAsString(), is(""));
        assertThat(httpServletResponse.getStatus(), is(HttpStatusCode.ACCEPTED_202.code()));
        verifyNoMoreInteractions(mockHttpServletRequestToMockServerRequestDecoder);
    }

    @Test
    public void shouldReset() {
        // given
        mockHttpServletRequest.setRequestURI("/reset");

        // when
        proxyServlet.doPut(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockRequestLogFilter).reset();
        assertEquals(HttpStatusCode.ACCEPTED_202.code(), mockHttpServletResponse.getStatus());
    }

    @Test
    public void shouldClear() {
        // given
        mockHttpServletRequest.setRequestURI("/clear");
        mockHttpServletRequest.setContent("body".getBytes());
        when(mockHttpRequestSerializer.deserialize("body")).thenReturn(outboundHttpRequest);

        // when
        proxyServlet.doPut(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockRequestLogFilter).clear(outboundHttpRequest);
        assertEquals(HttpStatusCode.ACCEPTED_202.code(), mockHttpServletResponse.getStatus());
    }

    @Test
    public void shouldMapPathWhenContextSet() {
        // given
        mockHttpServletRequest.setPathInfo("/clear");
        mockHttpServletRequest.setContextPath("/mockserver");
        mockHttpServletRequest.setRequestURI("/mockserver/clear");
        mockHttpServletRequest.setContent("body".getBytes());
        when(mockHttpRequestSerializer.deserialize("body")).thenReturn(outboundHttpRequest);

        // when
        proxyServlet.doPut(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockRequestLogFilter).clear(outboundHttpRequest);
        assertEquals(HttpStatusCode.ACCEPTED_202.code(), mockHttpServletResponse.getStatus());
    }

    @Test
    public void shouldMapPathWhenContextSetButPathNull() {
        // given
        mockHttpServletRequest.setPathInfo(null);
        mockHttpServletRequest.setContextPath("/mockserver");
        mockHttpServletRequest.setRequestURI("/clear");
        mockHttpServletRequest.setContent("body".getBytes());
        when(mockHttpRequestSerializer.deserialize("body")).thenReturn(outboundHttpRequest);

        // when
        proxyServlet.doPut(mockHttpServletRequest, mockHttpServletResponse);

        // then
        verify(mockRequestLogFilter).clear(outboundHttpRequest);
        assertEquals(HttpStatusCode.ACCEPTED_202.code(), mockHttpServletResponse.getStatus());
    }
}
