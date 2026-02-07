package com.mock.service.controller;

import com.mock.service.dto.Result;
import com.mock.service.entity.Environment;
import com.mock.service.entity.EnvironmentVariable;
import com.mock.service.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 环境配置管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/environment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    /**
     * 创建环境
     */
    @PostMapping
    public Result<Environment> createEnvironment(@RequestBody Environment environment) {
        try {
            Environment created = environmentService.createEnvironment(environment);
            return Result.success(created);
        } catch (Exception e) {
            log.error("创建环境失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新环境
     */
    @PutMapping("/{id}")
    public Result<Environment> updateEnvironment(
            @PathVariable Long id,
            @RequestBody Environment environment
    ) {
        try {
            Environment updated = environmentService.updateEnvironment(id, environment);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("更新环境失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除环境
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteEnvironment(@PathVariable Long id) {
        try {
            environmentService.deleteEnvironment(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除环境失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有环境
     */
    @GetMapping
    public Result<List<Environment>> getAllEnvironments() {
        try {
            List<Environment> environments = environmentService.getAllEnvironments();
            return Result.success(environments);
        } catch (Exception e) {
            log.error("获取环境列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取环境详情
     */
    @GetMapping("/{id}")
    public Result<Environment> getEnvironmentById(@PathVariable Long id) {
        try {
            Environment environment = environmentService.getEnvironmentById(id);
            return Result.success(environment);
        } catch (Exception e) {
            log.error("获取环境详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取默认环境
     */
    @GetMapping("/default")
    public Result<Environment> getDefaultEnvironment() {
        try {
            Environment environment = environmentService.getDefaultEnvironment();
            return Result.success(environment);
        } catch (Exception e) {
            log.error("获取默认环境失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置默认环境
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultEnvironment(@PathVariable Long id) {
        try {
            environmentService.setDefaultEnvironment(id);
            return Result.success();
        } catch (Exception e) {
            log.error("设置默认环境失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建环境变量
     */
    @PostMapping("/{environmentId}/variables")
    public Result<EnvironmentVariable> createVariable(
            @PathVariable Long environmentId,
            @RequestBody EnvironmentVariable variable
    ) {
        try {
            variable.setEnvironmentId(environmentId);
            EnvironmentVariable created = environmentService.createVariable(variable);
            return Result.success(created);
        } catch (Exception e) {
            log.error("创建环境变量失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新环境变量
     */
    @PutMapping("/variables/{id}")
    public Result<EnvironmentVariable> updateVariable(
            @PathVariable Long id,
            @RequestBody EnvironmentVariable variable
    ) {
        try {
            EnvironmentVariable updated = environmentService.updateVariable(id, variable);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("更新环境变量失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除环境变量
     */
    @DeleteMapping("/variables/{id}")
    public Result<Void> deleteVariable(@PathVariable Long id) {
        try {
            environmentService.deleteVariable(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除环境变量失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取环境的所有变量
     */
    @GetMapping("/{environmentId}/variables")
    public Result<List<EnvironmentVariable>> getVariables(@PathVariable Long environmentId) {
        try {
            List<EnvironmentVariable> variables = environmentService.getVariablesByEnvironmentId(environmentId);
            return Result.success(variables);
        } catch (Exception e) {
            log.error("获取环境变量列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取环境的所有变量（Map形式）
     */
    @GetMapping("/{environmentId}/variables/map")
    public Result<Map<String, String>> getVariablesMap(@PathVariable Long environmentId) {
        try {
            Map<String, String> variables = environmentService.getVariablesMapByEnvironmentId(environmentId);
            return Result.success(variables);
        } catch (Exception e) {
            log.error("获取环境变量Map失败", e);
            return Result.error(e.getMessage());
        }
    }
}
