package com.actigence.aal;

import com.actigence.aal.aws.SQSClient;
import com.actigence.aal.dto.ApiAccessLog;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ApiAccessLoggingFilter implements Filter
{
    private final SQSClient sqsClient;

    public ApiAccessLoggingFilter()
    {
        sqsClient = new SQSClient();
    }

    public ApiAccessLoggingFilter(SQSClient sqsClient)
    {
        this.sqsClient = sqsClient;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        HttpRequestCachingWrapper requestWrapper = new HttpRequestCachingWrapper((HttpServletRequest) request);
        HttpResponseCachingWrapper responseWrapper = new HttpResponseCachingWrapper((HttpServletResponse) response);
        try
        {
            filterChain.doFilter(requestWrapper, responseWrapper);
            response.getOutputStream().write(responseWrapper.getBytes());
        }
        finally
        {
            generateEvent(requestWrapper, responseWrapper);
        }
    }

    private void generateEvent(HttpRequestCachingWrapper requestCachingWrapper, HttpResponseCachingWrapper responseWrapper)
    {
        try
        {
            ApiAccessLog apiInvocationEvent = ApiAccessLog.builder()
                    .requestBody(requestCachingWrapper.getBody())
                    .responseBody(new String(responseWrapper.getBytes()))
                    .responseHttpStatus(responseWrapper.getStatusCode())
                    .build();
            sqsClient.publish(apiInvocationEvent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void init(FilterConfig filterConfig)
    {
        // Do nothing
    }

    @Override
    public void destroy()
    {
        // Do nothing
    }
}