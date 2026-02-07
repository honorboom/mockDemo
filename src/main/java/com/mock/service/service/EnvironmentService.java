package com.mock.service.service;

import com.mock.service.entity.Environment;
import com.mock.service.entity.EnvironmentVariable;
import com.mock.service.repository.EnvironmentRepository;
import com.mock.service.repository.EnvironmentVariableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 环境配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;
    private final EnvironmentVariableRepository variableRepository;

    /**
     * 创建环境
     */
    @Transactional
    public Environment createEnvironment(Environment environment) {
        // 如果设置为默认环境，取消其他环境的默认状态
        if (Boolean.TRUE.equals(environment.getIsDefault())) {
            environmentRepository.findByIsDefaultTrue().ifPresent(env -> {
                env.setIsDefault(false);
                environmentRepository.save(env);
            });
        }

        // 计算显示顺序
        if (environment.getDisplayOrder() == null) {
            Integer maxOrder = environmentRepository.findAll().stream()
                .map(Environment::getDisplayOrder)
                .filter(order -> order != null)
                .max(Integer::compareTo)
                .orElse(0);
            environment.setDisplayOrder(maxOrder + 1);
        }

        return environmentRepository.save(environment);
    }

    /**
     * 更新环境
     */
    @Transactional
    public Environment updateEnvironment(Long id, Environment request) {
        Environment environment = environmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("环境不存在"));

        // 如果设置为默认环境，取消其他环境的默认状态
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(environment.getIsDefault())) {
            environmentRepository.findByIsDefaultTrue().ifPresent(env -> {
                if (!env.getId().equals(id)) {
                    env.setIsDefault(false);
                    environmentRepository.save(env);
                }
            });
        }

        environment.setName(request.getName());
        environment.setBaseUrl(request.getBaseUrl());
        environment.setDescription(request.getDescription());
        environment.setIsDefault(request.getIsDefault());
        environment.setDisplayOrder(request.getDisplayOrder());

        return environmentRepository.save(environment);
    }

    /**
     * 删除环境
     */
    @Transactional
    public void deleteEnvironment(Long id) {
        Environment environment = environmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("环境不存在"));

        // 删除关联的环境变量
        variableRepository.deleteByEnvironmentId(id);

        // 删除环境
        environmentRepository.delete(environment);

        log.info("已删除环境: id={}", id);
    }

    /**
     * 获取所有环境
     */
    public List<Environment> getAllEnvironments() {
        return environmentRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * 根据ID获取环境
     */
    public Environment getEnvironmentById(Long id) {
        return environmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("环境不存在"));
    }

    /**
     * 获取默认环境
     */
    public Environment getDefaultEnvironment() {
        return environmentRepository.findByIsDefaultTrue()
            .orElse(null);
    }

    /**
     * 设置默认环境
     */
    @Transactional
    public void setDefaultEnvironment(Long id) {
        Environment environment = environmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("环境不存在"));

        // 取消其他环境的默认状态
        environmentRepository.findByIsDefaultTrue().ifPresent(env -> {
            env.setIsDefault(false);
            environmentRepository.save(env);
        });

        // 设置为默认环境
        environment.setIsDefault(true);
        environmentRepository.save(environment);
    }

    /**
     * 创建环境变量
     */
    @Transactional
    public EnvironmentVariable createVariable(EnvironmentVariable variable) {
        // 检查环境是否存在
        if (!environmentRepository.existsById(variable.getEnvironmentId())) {
            throw new RuntimeException("环境不存在");
        }

        // 检查变量名是否已存在
        if (variableRepository.existsByEnvironmentIdAndKey(variable.getEnvironmentId(), variable.getKey())) {
            throw new RuntimeException("该环境变量名已存在");
        }

        return variableRepository.save(variable);
    }

    /**
     * 更新环境变量
     */
    @Transactional
    public EnvironmentVariable updateVariable(Long id, EnvironmentVariable request) {
        EnvironmentVariable variable = variableRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("环境变量不存在"));

        // 检查变量名是否与其他记录冲突
        if (variableRepository.existsByEnvironmentIdAndKeyAndIdNot(
                request.getEnvironmentId(), request.getKey(), id)) {
            throw new RuntimeException("该环境变量名已被使用");
        }

        variable.setKey(request.getKey());
        variable.setValue(request.getValue());
        variable.setIsSecret(request.getIsSecret());
        variable.setDescription(request.getDescription());

        return variableRepository.save(variable);
    }

    /**
     * 删除环境变量
     */
    @Transactional
    public void deleteVariable(Long id) {
        if (!variableRepository.existsById(id)) {
            throw new RuntimeException("环境变量不存在");
        }
        variableRepository.deleteById(id);
    }

    /**
     * 获取环境的所有变量
     */
    public List<EnvironmentVariable> getVariablesByEnvironmentId(Long environmentId) {
        return variableRepository.findByEnvironmentId(environmentId);
    }

    /**
     * 获取环境的所有变量（以Map形式）
     */
    public Map<String, String> getVariablesMapByEnvironmentId(Long environmentId) {
        List<EnvironmentVariable> variables = variableRepository.findByEnvironmentId(environmentId);
        Map<String, String> map = new HashMap<>();
        for (EnvironmentVariable variable : variables) {
            map.put(variable.getKey(), variable.getValue());
        }
        return map;
    }

    /**
     * 替换字符串中的环境变量
     * 格式：${variableName}
     */
    public String replaceVariables(String text, Long environmentId) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Map<String, String> variables = getVariablesMapByEnvironmentId(environmentId);
        String result = text;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue());
        }

        return result;
    }
}
