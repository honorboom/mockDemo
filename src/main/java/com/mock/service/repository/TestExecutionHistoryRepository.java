package com.mock.service.repository;

import com.mock.service.entity.TestExecutionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 测试执行历史Repository
 */
@Repository
public interface TestExecutionHistoryRepository extends JpaRepository<TestExecutionHistory, Long> {

    /**
     * 分页查询所有测试执行历史
     */
    Page<TestExecutionHistory> findAllByOrderByExecutedAtDesc(Pageable pageable);

    /**
     * 根据场景ID查询测试执行历史
     */
    Page<TestExecutionHistory> findByScenarioIdOrderByExecutedAtDesc(Long scenarioId, Pageable pageable);

    /**
     * 根据成功状态查询
     */
    Page<TestExecutionHistory> findBySuccessOrderByExecutedAtDesc(Boolean success, Pageable pageable);

    /**
     * 删除指定场景的所有历史记录
     */
    void deleteByScenarioId(Long scenarioId);
}
