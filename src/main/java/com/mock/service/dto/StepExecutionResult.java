package com.mock.service.dto;

import lombok.Data;
import java.util.Map;

/**
 * 步骤执行结果 DTO
 */
@Data
public class StepExecutionResult {
    private Long stepId;
    private String stepName;
    private Integer stepOrder;
    private Boolean success;

    // 请求信息
    private String requestUrl;
    private String requestMethod;
    private Map<String, String> requestHeaders;
    private String requestParams;
    private String requestBody;

    // 响应信息
    private Integer statusCode;
    private Integer expectedStatus;
    private String responseBody;
    private Map<String, String> responseHeaders;

    // 其他信息
    private Long duration; // 耗时（毫秒）
    private String errorMessage;
    private String assertionResult; // 断言结果描述
}
