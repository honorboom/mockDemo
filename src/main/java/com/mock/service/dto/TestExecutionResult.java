package com.mock.service.dto;

import lombok.Data;
import java.util.List;

/**
 * 测试执行结果 DTO
 */
@Data
public class TestExecutionResult {
    private Long scenarioId;
    private String scenarioName;
    private Boolean success;
    private Integer totalSteps;
    private Integer passedSteps;
    private Integer failedSteps;
    private Long duration; // 总耗时（毫秒）
    private List<StepExecutionResult> stepResults;
    private String errorMessage;
}
