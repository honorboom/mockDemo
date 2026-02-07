package com.mock.service.repository;

import com.mock.service.entity.MockHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mock 历史记录数据访问层
 */
@Repository
public interface MockHistoryRepository extends JpaRepository<MockHistory, Long> {

    /**
     * 根据 Mock 配置 ID 分页查询历史记录
     */
    Page<MockHistory> findByMockConfigIdOrderByCreatedAtDesc(Long mockConfigId, Pageable pageable);

    /**
     * 根据时间范围查询历史记录
     */
    Page<MockHistory> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Pageable pageable
    );

    /**
     * 查询最近的历史记录
     */
    List<MockHistory> findTop100ByOrderByCreatedAtDesc();

    /**
     * 统计某个 Mock 的调用次数
     */
    long countByMockConfigId(Long mockConfigId);

    /**
     * 删除指定 Mock 的所有历史记录
     */
    void deleteByMockConfigId(Long mockConfigId);
}
