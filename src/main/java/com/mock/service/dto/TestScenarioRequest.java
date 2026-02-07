package com.mock.service.dto;

import lombok.Data;

/**
 * 测试场景请求 DTO
 */
@Data
public class TestScenarioRequest {
    private String name;
    private String description;
    private Boolean enabled;
}
