package com.mock.service.controller;

import com.mock.service.dto.Result;
import com.mock.service.dto.TestScenarioRequest;
import com.mock.service.dto.TestStepRequest;
import com.mock.service.entity.TestScenario;
import com.mock.service.entity.TestStep;
import com.mock.service.service.TestScenarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试场景管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/test-scenario")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestScenarioController {

    private final TestScenarioService scenarioService;

    /**
     * 创建测试场景
     */
    @PostMapping
    public Result<TestScenario> createScenario(@RequestBody TestScenarioRequest request) {
        try {
            TestScenario scenario = scenarioService.createScenario(request);
            return Result.success(scenario);
        } catch (Exception e) {
            log.error("创建测试场景失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新测试场景
     */
    @PutMapping("/{id}")
    public Result<TestScenario> updateScenario(
            @PathVariable Long id,
            @RequestBody TestScenarioRequest request
    ) {
        try {
            TestScenario scenario = scenarioService.updateScenario(id, request);
            return Result.success(scenario);
        } catch (Exception e) {
            log.error("更新测试场景失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除测试场景
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteScenario(@PathVariable Long id) {
        try {
            scenarioService.deleteScenario(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除测试场景失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取测试场景详情
     */
    @GetMapping("/{id}")
    public Result<TestScenario> getScenario(@PathVariable Long id) {
        try {
            TestScenario scenario = scenarioService.getScenario(id);
            return Result.success(scenario);
        } catch (Exception e) {
            log.error("获取测试场景失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有测试场景
     */
    @GetMapping
    public Result<List<TestScenario>> getAllScenarios() {
        try {
            List<TestScenario> scenarios = scenarioService.getAllScenarios();
            return Result.success(scenarios);
        } catch (Exception e) {
            log.error("获取测试场景列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加测试步骤
     */
    @PostMapping("/{scenarioId}/steps")
    public Result<TestStep> addStep(
            @PathVariable Long scenarioId,
            @RequestBody TestStepRequest request
    ) {
        try {
            request.setScenarioId(scenarioId);
            TestStep step = scenarioService.addStep(request);
            return Result.success(step);
        } catch (Exception e) {
            log.error("添加测试步骤失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新测试步骤
     */
    @PutMapping("/steps/{id}")
    public Result<TestStep> updateStep(
            @PathVariable Long id,
            @RequestBody TestStepRequest request
    ) {
        try {
            TestStep step = scenarioService.updateStep(id, request);
            return Result.success(step);
        } catch (Exception e) {
            log.error("更新测试步骤失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除测试步骤
     */
    @DeleteMapping("/steps/{id}")
    public Result<Void> deleteStep(@PathVariable Long id) {
        try {
            scenarioService.deleteStep(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除测试步骤失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取场景的所有步骤
     */
    @GetMapping("/{scenarioId}/steps")
    public Result<List<TestStep>> getScenarioSteps(@PathVariable Long scenarioId) {
        try {
            List<TestStep> steps = scenarioService.getScenarioSteps(scenarioId);
            return Result.success(steps);
        } catch (Exception e) {
            log.error("获取测试步骤列表失败", e);
            return Result.error(e.getMessage());
        }
    }
}
