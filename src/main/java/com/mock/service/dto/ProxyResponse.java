package com.mock.service.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ProxyResponse {
    private int status;
    private String statusText;
    private Map<String, String> headers;
    private Object data;
}
