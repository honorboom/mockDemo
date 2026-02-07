package com.mock.service.service;

import com.mock.service.dto.TestScenarioRequest;
import com.mock.service.dto.TestStepRequest;
import com.mock.service.entity.TestScenario;
import com.mock.service.entity.TestStep;
import com.mock.service.repository.TestScenarioRepository;
import com.mock.service.repository.TestStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 测试场景服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestScenarioService {

    private final TestScenarioRepository scenarioRepository;
    private final TestStepRepository stepRepository;

    /**
     * 创建测试场景
     */
    @Transactional
    public TestScenario createScenario(TestScenarioRequest request) {
        TestScenario scenario = new TestScenario();
        scenario.setName(request.getName());
        scenario.setDescription(request.getDescription());
        scenario.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        return scenarioRepository.save(scenario);
    }

    /**
     * 更新测试场景
     */
    @Transactional
    public TestScenario updateScenario(Long id, TestScenarioRequest request) {
        TestScenario scenario = scenarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("测试场景不存在"));

        scenario.setName(request.getName());
        scenario.setDescription(request.getDescription());
        scenario.setEnabled(request.getEnabled());

        return scenarioRepository.save(scenario);
    }

    /**
     * 删除测试场景
     */
    @Transactional
    public void deleteScenario(Long id) {
        // 先删除所有步骤
        stepRepository.deleteByScenarioId(id);
        // 再删除场景
        scenarioRepository.deleteById(id);
    }

    /**
     * 获取测试场景详情
     */
    public TestScenario getScenario(Long id) {
        return scenarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("测试场景不存在"));
    }

    /**
     * 获取所有测试场景
     */
    public List<TestScenario> getAllScenarios() {
        return scenarioRepository.findAll();
    }

    /**
     * 获取所有启用的测试场景
     */
    public List<TestScenario> getEnabledScenarios() {
        return scenarioRepository.findByEnabledTrue();
    }

    /**
     * 添加测试步骤
     */
    @Transactional
    public TestStep addStep(TestStepRequest request) {
        TestStep step = new TestStep();
        step.setScenarioId(request.getScenarioId());
        step.setName(request.getName());
        step.setStepOrder(request.getStepOrder());
        step.setUrl(request.getUrl());
        step.setMethod(request.getMethod());
        step.setHeaders(request.getHeaders());
        step.setParams(request.getParams());
        step.setBody(request.getBody());
        step.setExpectedStatus(request.getExpectedStatus() != null ? request.getExpectedStatus() : 200);
        step.setAssertions(request.getAssertions());
        step.setDelay(request.getDelay() != null ? request.getDelay() : 0);

        return stepRepository.save(step);
    }

    /**
     * 更新测试步骤
     */
    @Transactional
    public TestStep updateStep(Long id, TestStepRequest request) {
        TestStep step = stepRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("测试步骤不存在"));

        step.setName(request.getName());
        step.setStepOrder(request.getStepOrder());
        step.setUrl(request.getUrl());
        step.setMethod(request.getMethod());
        step.setHeaders(request.getHeaders());
        step.setParams(request.getParams());
        step.setBody(request.getBody());
        step.setExpectedStatus(request.getExpectedStatus());
        step.setAssertions(request.getAssertions());
        step.setDelay(request.getDelay());

        return stepRepository.save(step);
    }

    /**
     * 删除测试步骤
     */
    @Transactional
    public void deleteStep(Long id) {
        stepRepository.deleteById(id);
    }

    /**
     * 获取场景的所有步骤
     */
    public List<TestStep> getScenarioSteps(Long scenarioId) {
        return stepRepository.findByScenarioIdOrderByStepOrderAsc(scenarioId);
    }
}
