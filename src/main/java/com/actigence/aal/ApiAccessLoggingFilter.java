package com.actigence.aal;

import com.actigence.aal.aws.SQSClient;
import com.actigence.aal.dto.ApiAccessLog;
import com.actigence.aal.dto.NameValue;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Slf4j
public class ApiAccessLoggingFilter implements Filter {
    private final SQSClient sqsClient;

    //(Optional) Unique ID to identify this service on logs.
    private String clientId;

    public ApiAccessLoggingFilter() {
        sqsClient = new SQSClient();
    }

    public ApiAccessLoggingFilter(SQSClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        ZonedDateTime requestStartTime = ZonedDateTime.now();
        HttpRequestCachingWrapper requestWrapper = new HttpRequestCachingWrapper((HttpServletRequest) request);
        HttpResponseCachingWrapper responseWrapper = new HttpResponseCachingWrapper((HttpServletResponse) response);
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
            response.getOutputStream().write(responseWrapper.getBytes());
        } finally {
            generateEvent(requestWrapper, responseWrapper, requestStartTime);
        }
    }

    private void generateEvent(HttpRequestCachingWrapper requestWrapper,
                               HttpResponseCachingWrapper responseWrapper,
                               ZonedDateTime startTime) {
        try {
            ApiAccessLog apiInvocationEvent = ApiAccessLog.builder()
                    .logId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .requestStartTime(startTime)
                    .requestEndTime(ZonedDateTime.now())
                    .requestUri(requestWrapper.getRequestURI())
                    .requestQueryString(requestWrapper.getQueryString())
                    .requestBody(requestWrapper.getBody())
                    .requestHeaders(requestHeaders(requestWrapper))
                    .responseBody(new String(responseWrapper.getBytes()))
                    .responseHttpStatus(responseWrapper.getStatusCode())
                    .build();
            sqsClient.publish(apiInvocationEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<NameValue> requestHeaders(HttpRequestCachingWrapper requestCachingWrapper) {
        return Collections.list(requestCachingWrapper.getHeaderNames())
                .stream()
                .map(it -> new NameValue(it, requestCachingWrapper.getHeader(it)))
                .collect(toList());
    }

    @Override
    public void init(FilterConfig filterConfig) {
        try {
            clientId = filterConfig.getInitParameter("clientId");
        } catch (Exception e) {
            //Unable to find clientId.
            clientId = null;
        }
    }

    @Override
    public void destroy() {
        // Do nothing
    }
}