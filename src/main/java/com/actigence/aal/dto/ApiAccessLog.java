package com.actigence.aal.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
public class ApiAccessLog
{
    private String logId;
    private String clientId;
    private String requestUrl;
    private String requestHttpMethod;
    private String requestBody;
    private int responseHttpStatus;
    private String responseBody;
}
