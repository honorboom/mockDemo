package com.mock.service.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ProxyRequest {
    private String url;
    private String method;
    private Map<String, String> params;
    private Map<String, String> headers;
    private Object body;
}
