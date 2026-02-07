package com.mock.service.dto;

import lombok.Data;

/**
 * 测试步骤请求 DTO
 */
@Data
public class TestStepRequest {
    private Long scenarioId;
    private String name;
    private Integer stepOrder;
    private String url;
    private String method;
    private String headers;
    private String params;
    private String body;
    private Integer expectedStatus;
    private String assertions;
    private Integer delay;
}
