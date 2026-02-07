package com.mock.service.controller;

import com.mock.service.dto.Result;
import com.mock.service.entity.MockHistory;
import com.mock.service.service.MockHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mock 历史记录 Controller
 */
@Slf4j
@RestController
@RequestMapping("/mock-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MockHistoryController {

    private final MockHistoryService mockHistoryService;

    /**
     * 分页查询历史记录
     */
    @GetMapping
    public Result<Page<MockHistory>> getHistoryPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Page<MockHistory> historyPage = mockHistoryService.getHistoryPage(page, size);
            return Result.success(historyPage);
        } catch (Exception e) {
            log.error("查询历史记录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据 Mock ID 查询历史记录
     */
    @GetMapping("/mock/{mockConfigId}")
    public Result<Page<MockHistory>> getHistoryByMockId(
        @PathVariable Long mockConfigId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Page<MockHistory> historyPage = mockHistoryService.getHistoryByMockId(mockConfigId, page, size);
            return Result.success(historyPage);
        } catch (Exception e) {
            log.error("查询 Mock 历史记录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据时间范围查询历史记录
     */
    @GetMapping("/time-range")
    public Result<Page<MockHistory>> getHistoryByTimeRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Page<MockHistory> historyPage = mockHistoryService.getHistoryByTimeRange(
                startTime, endTime, page, size
            );
            return Result.success(historyPage);
        } catch (Exception e) {
            log.error("按时间范围查询历史记录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取最近的历史记录
     */
    @GetMapping("/recent")
    public Result<List<MockHistory>> getRecentHistory() {
        try {
            List<MockHistory> history = mockHistoryService.getRecentHistory();
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取最近历史记录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 统计 Mock 调用次数
     */
    @GetMapping("/count/{mockConfigId}")
    public Result<Long> countByMockId(@PathVariable Long mockConfigId) {
        try {
            long count = mockHistoryService.countByMockId(mockConfigId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("统计调用次数失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除所有历史记录
     */
    @DeleteMapping("/all")
    public Result<Void> deleteAllHistory() {
        try {
            mockHistoryService.deleteAllHistory();
            return Result.success();
        } catch (Exception e) {
            log.error("删除所有历史记录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除指定 Mock 的历史记录
     */
    @DeleteMapping("/mock/{mockConfigId}")
    public Result<Void> deleteHistoryByMockId(@PathVariable Long mockConfigId) {
        try {
            mockHistoryService.deleteHistoryByMockId(mockConfigId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除 Mock 历史记录失败", e);
            return Result.error(e.getMessage());
        }
    }
}
