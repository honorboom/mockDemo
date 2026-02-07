package com.mock.service.controller;

import com.mock.service.dto.Result;
import com.mock.service.dto.TestExecutionResult;
import com.mock.service.service.TestExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 测试执行 Controller
 */
@Slf4j
@RestController
@RequestMapping("/test-execution")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestExecutionController {

    private final TestExecutionService executionService;

    /**
     * 执行测试场景
     */
    @PostMapping("/run/{scenarioId}")
    public Result<TestExecutionResult> executeScenario(
            @PathVariable Long scenarioId,
            @RequestParam(required = false) Long environmentId
    ) {
        try {
            log.info("开始执行测试场景: {}, 环境ID: {}", scenarioId, environmentId);
            TestExecutionResult result = executionService.executeScenario(scenarioId, environmentId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("执行测试场景失败", e);
            return Result.error(e.getMessage());
        }
    }
}
