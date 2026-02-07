package com.mock.service.controller;

import com.mock.service.dto.MockConfigRequest;
import com.mock.service.dto.Result;
import com.mock.service.entity.MockConfig;
import com.mock.service.service.MockConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Mock 配置管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/mock-config")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MockConfigController {

    private final MockConfigService mockConfigService;

    /**
     * 创建 Mock 配置
     */
    @PostMapping
    public Result<MockConfig> createMock(@Valid @RequestBody MockConfigRequest request) {
        try {
            MockConfig config = mockConfigService.createMock(request);
            return Result.success(config);
        } catch (Exception e) {
            log.error("创建 Mock 配置失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新 Mock 配置
     */
    @PutMapping("/{id}")
    public Result<MockConfig> updateMock(
        @PathVariable Long id,
        @Valid @RequestBody MockConfigRequest request
    ) {
        try {
            MockConfig config = mockConfigService.updateMock(id, request);
            return Result.success(config);
        } catch (Exception e) {
            log.error("更新 Mock 配置失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除 Mock 配置
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMock(@PathVariable Long id) {
        try {
            mockConfigService.deleteMock(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除 Mock 配置失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取 Mock 配置详情
     */
    @GetMapping("/{id}")
    public Result<MockConfig> getMock(@PathVariable Long id) {
        try {
            MockConfig config = mockConfigService.getMock(id);
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取 Mock 配置失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有 Mock 配置（支持分页）
     */
    @GetMapping
    public Result<?> getAllMocks(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        try {
            // 如果提供了分页参数，返回分页结果
            if (page != null && size != null) {
                return Result.success(mockConfigService.getMocksByPage(page, size));
            }
            // 否则返回所有数据
            List<MockConfig> configs = mockConfigService.getAllMocks();
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取 Mock 配置列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 切换 Mock 启用状态
     */
    @PutMapping("/{id}/toggle")
    public Result<MockConfig> toggleEnabled(@PathVariable Long id) {
        try {
            MockConfig config = mockConfigService.toggleEnabled(id);
            return Result.success(config);
        } catch (Exception e) {
            log.error("切换 Mock 状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量导入 Mock 配置
     */
    @PostMapping("/import")
    public Result<List<MockConfig>> importMocks(@RequestBody List<MockConfigRequest> requests) {
        try {
            log.info("开始批量导入 Mock 配置，数量: {}", requests.size());
            List<MockConfig> configs = mockConfigService.importMocks(requests);
            return Result.success(configs);
        } catch (Exception e) {
            log.error("批量导入 Mock 配置失败", e);
            return Result.error(e.getMessage());
        }
    }
}
