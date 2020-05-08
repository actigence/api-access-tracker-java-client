package com.actigence.aal.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
@Builder
@ToString
public class ApiAccessLog {
    //Unique ID for this log message
    private String logId;

    //Client Identifier as configured in ApiAccessLoggingFilter class.
    private String clientId;

    //The URL for this request.
    private String requestUri;

    //Query portion of the request URL
    private String requestQueryString;

    //List of all request headers
    private List<NameValue> requestHeaders;

    //Http Method used for this request.
    private String requestHttpMethod;

    //Body of this request
    private String requestBody;

    //HTTP response status
    private int responseHttpStatus;

    //Response body
    private String responseBody;

    //Server start time of this request
    private ZonedDateTime requestStartTime;

    //Server end time fo this request
    private ZonedDateTime requestEndTime;

    private void addHeader(String name, String value) {
        if (requestHeaders == null) {
            requestHeaders = new LinkedList<>();
        }
        requestHeaders.add(new NameValue(name, value));
    }
}
