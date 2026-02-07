package com.mock.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Mock 配置请求 DTO
 */
@Data
public class MockConfigRequest {

    @NotBlank(message = "Mock 名称不能为空")
    private String name;

    @NotBlank(message = "Mock 路径不能为空")
    private String path;

    @NotBlank(message = "HTTP 方法不能为空")
    private String method;

    @NotNull(message = "响应状态码不能为空")
    private Integer statusCode = 200;

    private String contentType = "application/json";

    private String requestBody;

    private String responseBody;

    private Integer delay = 0;

    private Boolean enabled = true;

    private String description;
}
