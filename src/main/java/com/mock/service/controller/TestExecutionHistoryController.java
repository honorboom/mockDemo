package com.mock.service.controller;

import com.mock.service.dto.Result;
import com.mock.service.dto.TestExecutionResult;
import com.mock.service.entity.TestExecutionHistory;
import com.mock.service.service.TestExecutionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 测试执行历史控制器
 */
@Slf4j
@RestController
@RequestMapping("/test-execution-history")
@RequiredArgsConstructor
public class TestExecutionHistoryController {

    private final TestExecutionHistoryService historyService;

    /**
     * 分页查询测试执行历史
     */
    @GetMapping
    public Result<Page<TestExecutionHistory>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long scenarioId,
            @RequestParam(required = false) Boolean success
    ) {
        try {
            Page<TestExecutionHistory> historyPage;

            if (scenarioId != null) {
                historyPage = historyService.getHistoryByScenarioId(scenarioId, page, size);
            } else if (success != null) {
                historyPage = historyService.getHistoryBySuccess(success, page, size);
            } else {
                historyPage = historyService.getHistory(page, size);
            }

            return Result.success(historyPage);
        } catch (Exception e) {
            log.error("查询测试执行历史失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取历史详情
     */
    @GetMapping("/{id}")
    public Result<TestExecutionHistory> getHistoryDetail(@PathVariable Long id) {
        try {
            TestExecutionHistory history = historyService.getHistoryById(id);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取历史详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取执行结果详情
     */
    @GetMapping("/{id}/result")
    public Result<TestExecutionResult> getExecutionResult(@PathVariable Long id) {
        try {
            TestExecutionResult result = historyService.getExecutionResult(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取执行结果详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除历史记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        try {
            historyService.deleteHistory(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除历史记录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 清空所有历史记录
     */
    @DeleteMapping("/clear")
    public Result<Void> clearAllHistory() {
        try {
            historyService.clearAllHistory();
            return Result.success();
        } catch (Exception e) {
            log.error("清空历史记录失败", e);
            return Result.error(e.getMessage());
        }
    }
}
