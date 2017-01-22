package org.mockserver.proxy;

import com.google.common.base.Strings;
import com.google.common.net.MediaType;
import io.netty.handler.codec.http.HttpHeaders;
import org.mockserver.client.netty.NettyHttpClient;
import org.mockserver.client.serialization.ExpectationSerializer;
import org.mockserver.client.serialization.HttpRequestSerializer;
import org.mockserver.client.serialization.VerificationSequenceSerializer;
import org.mockserver.client.serialization.VerificationSerializer;
import org.mockserver.filters.*;
import org.mockserver.mappers.HttpServletRequestToMockServerRequestDecoder;
import org.mockserver.mappers.MockServerResponseToHttpServletResponseEncoder;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.streams.IOStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.mockserver.model.HttpResponse.notFoundResponse;
import static org.mockserver.model.OutboundHttpRequest.outboundRequest;

/**
 * @author jamesdbloom
 */
public class ProxyServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public RequestLogFilter requestLogFilter = new RequestLogFilter();
    public RequestResponseLogFilter requestResponseLogFilter = new RequestResponseLogFilter();
    // mockserver
    private Filters filters = new Filters();
    // http client
    private NettyHttpClient httpClient = new NettyHttpClient();
    // mappers
    private HttpServletRequestToMockServerRequestDecoder httpServletRequestToMockServerRequestDecoder = new HttpServletRequestToMockServerRequestDecoder();
    private MockServerResponseToHttpServletResponseEncoder mockServerResponseToHttpServletResponseEncoder = new MockServerResponseToHttpServletResponseEncoder();
    // serializers
    private HttpRequestSerializer httpRequestSerializer = new HttpRequestSerializer();
    private VerificationSerializer verificationSerializer = new VerificationSerializer();
    private VerificationSequenceSerializer verificationSequenceSerializer = new VerificationSequenceSerializer();

    public ProxyServlet() {
        filters.withFilter(new HttpRequest(), new HopByHopHeaderFilter());
        filters.withFilter(new HttpRequest(), requestLogFilter);
        filters.withFilter(new HttpRequest(), requestResponseLogFilter);
    }

    /**
     * Add filter for HTTP requests, each filter get called before each request is proxied, if the filter return null then the request is not proxied
     *
     * @param httpRequest the request to match against for this filter
     * @param filter the filter to execute for this request, if the filter returns null the request will not be proxied
     */
    public ProxyServlet withFilter(HttpRequest httpRequest, RequestFilter filter) {
        filters.withFilter(httpRequest, filter);
        return this;
    }

    /**
     * Add filter for HTTP response, each filter get called after each request has been proxied
     *
     * @param httpRequest the request to match against for this filter
     * @param filter the filter that is executed after this request has been proxied
     */
    public ProxyServlet withFilter(HttpRequest httpRequest, ResponseFilter filter) {
        filters.withFilter(httpRequest, filter);
        return this;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        forwardRequest(request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) {
        forwardRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        forwardRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        try {
            String requestPath = httpServletRequest.getPathInfo() != null && httpServletRequest.getContextPath() != null ? httpServletRequest.getPathInfo() : httpServletRequest.getRequestURI();
            if (requestPath.equals("/status")) {

                httpServletResponse.setStatus(HttpStatusCode.OK_200.code());

            } else if (requestPath.equals("/clear")) {

                requestLogFilter.clear(httpRequestSerializer.deserialize(IOStreamUtils.readInputStreamToString(httpServletRequest)));
                httpServletResponse.setStatus(HttpStatusCode.ACCEPTED_202.code());

            } else if (requestPath.equals("/reset")) {

                requestLogFilter.reset();
                httpServletResponse.setStatus(HttpStatusCode.ACCEPTED_202.code());

            } else if (requestPath.equals("/dumpToLog")) {

                requestResponseLogFilter.dumpToLog(httpRequestSerializer.deserialize(IOStreamUtils.readInputStreamToString(httpServletRequest)), "java".equals(httpServletRequest.getParameter("type")));
                httpServletResponse.setStatus(HttpStatusCode.ACCEPTED_202.code());

            } else if (requestPath.equals("/retrieve")) {

                HttpRequest[] requests = requestLogFilter.retrieve(httpRequestSerializer.deserialize(IOStreamUtils.readInputStreamToString(httpServletRequest)));
                httpServletResponse.setStatus(HttpStatusCode.OK_200.code());
                httpServletResponse.setHeader(CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
                IOStreamUtils.writeToOutputStream(httpRequestSerializer.serialize(requests).getBytes(), httpServletResponse);

            } else if (requestPath.equals("/verify")) {

                String result = requestLogFilter.verify(verificationSerializer.deserialize(IOStreamUtils.readInputStreamToString(httpServletRequest)));
                if (result.isEmpty()) {
                    httpServletResponse.setStatus(HttpStatusCode.ACCEPTED_202.code());
                } else {
                    httpServletResponse.setStatus(HttpStatusCode.NOT_ACCEPTABLE_406.code());
                    httpServletResponse.setHeader(CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
                    IOStreamUtils.writeToOutputStream(result.getBytes(), httpServletResponse);
                }

            } else if (requestPath.equals("/verifySequence")) {

                String result = requestLogFilter.verify(verificationSequenceSerializer.deserialize(IOStreamUtils.readInputStreamToString(httpServletRequest)));
                if (result.isEmpty()) {
                    httpServletResponse.setStatus(HttpStatusCode.ACCEPTED_202.code());
                } else {
                    httpServletResponse.setStatus(HttpStatusCode.NOT_ACCEPTABLE_406.code());
                    httpServletResponse.setHeader(CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
                    IOStreamUtils.writeToOutputStream(result.getBytes(), httpServletResponse);
                }

            } else if (requestPath.equals("/stop")) {

                httpServletResponse.setStatus(HttpStatusCode.NOT_IMPLEMENTED_501.code());

            } else {
                forwardRequest(httpServletRequest, httpServletResponse);
            }
        } catch (Exception e) {
            logger.error("Exception processing " + httpServletRequest, e);
            httpServletResponse.setStatus(HttpStatusCode.BAD_REQUEST_400.code());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        forwardRequest(request, response);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        forwardRequest(request, response);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) {
        forwardRequest(request, response);
    }

    private void forwardRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        HttpResponse httpResponse = sendRequest(filters.applyOnRequestFilters(httpServletRequestToMockServerRequestDecoder.mapHttpServletRequestToMockServerRequest(request)));
        mockServerResponseToHttpServletResponseEncoder.mapMockServerResponseToHttpServletResponse(httpResponse, httpServletResponse);
    }

    private HttpResponse sendRequest(HttpRequest httpRequest) {
        // if HttpRequest was set to null by a filter don't send request
        if (httpRequest != null) {
            String hostHeader = httpRequest.getFirstHeader("Host");
            if (!Strings.isNullOrEmpty(hostHeader)) {
                String[] hostHeaderParts = hostHeader.split(":");

                boolean isSsl = httpRequest.isSecure() != null && httpRequest.isSecure();
                Integer port = (isSsl ? 443 : 80); // default
                if (hostHeaderParts.length > 1) {
                    port = Integer.parseInt(hostHeaderParts[1]);  // non-default
                }
                HttpResponse httpResponse = filters.applyOnResponseFilters(httpRequest, httpClient.sendRequest(outboundRequest(hostHeaderParts[0], port, "", httpRequest)));
                if (httpResponse != null) {
                    return httpResponse;
                }
            } else {
                logger.error("Host header must be provided for requests being forwarded, the following request does not include the \"Host\" header:" + System.getProperty("line.separator") + httpRequest);
                throw new IllegalArgumentException("Host header must be provided for requests being forwarded");
            }
        }
        return notFoundResponse();
    }
}
