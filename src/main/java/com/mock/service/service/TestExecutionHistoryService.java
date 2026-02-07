package com.mock.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mock.service.dto.TestExecutionResult;
import com.mock.service.entity.TestExecutionHistory;
import com.mock.service.repository.TestExecutionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 测试执行历史服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestExecutionHistoryService {

    private final TestExecutionHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    /**
     * 保存测试执行历史
     */
    @Transactional
    public TestExecutionHistory saveHistory(TestExecutionResult result, Long environmentId, String environmentName) {
        try {
            TestExecutionHistory history = new TestExecutionHistory();
            history.setScenarioId(result.getScenarioId());
            history.setScenarioName(result.getScenarioName());
            history.setEnvironmentId(environmentId);
            history.setEnvironmentName(environmentName);
            history.setSuccess(result.getSuccess());
            history.setTotalSteps(result.getTotalSteps());
            history.setPassedSteps(result.getPassedSteps());
            history.setFailedSteps(result.getFailedSteps());
            history.setDuration(result.getDuration());
            history.setErrorMessage(result.getErrorMessage());

            // 将详细结果转换为JSON保存
            String resultDetail = objectMapper.writeValueAsString(result);
            history.setResultDetail(resultDetail);

            return historyRepository.save(history);
        } catch (Exception e) {
            log.error("保存测试执行历史失败", e);
            throw new RuntimeException("保存测试执行历史失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询所有历史记录
     */
    public Page<TestExecutionHistory> getHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return historyRepository.findAllByOrderByExecutedAtDesc(pageable);
    }

    /**
     * 根据场景ID查询历史记录
     */
    public Page<TestExecutionHistory> getHistoryByScenarioId(Long scenarioId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return historyRepository.findByScenarioIdOrderByExecutedAtDesc(scenarioId, pageable);
    }

    /**
     * 根据成功状态查询
     */
    public Page<TestExecutionHistory> getHistoryBySuccess(Boolean success, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return historyRepository.findBySuccessOrderByExecutedAtDesc(success, pageable);
    }

    /**
     * 获取历史详情
     */
    public TestExecutionHistory getHistoryById(Long id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("历史记录不存在"));
    }

    /**
     * 删除历史记录
     */
    @Transactional
    public void deleteHistory(Long id) {
        historyRepository.deleteById(id);
    }

    /**
     * 删除指定场景的所有历史记录
     */
    @Transactional
    public void deleteHistoryByScenarioId(Long scenarioId) {
        historyRepository.deleteByScenarioId(scenarioId);
    }

    /**
     * 清空所有历史记录
     */
    @Transactional
    public void clearAllHistory() {
        historyRepository.deleteAll();
    }

    /**
     * 获取执行结果详情
     */
    public TestExecutionResult getExecutionResult(Long id) {
        try {
            TestExecutionHistory history = getHistoryById(id);
            return objectMapper.readValue(history.getResultDetail(), TestExecutionResult.class);
        } catch (Exception e) {
            log.error("解析执行结果失败", e);
            throw new RuntimeException("解析执行结果失败: " + e.getMessage());
        }
    }
}
