package com.mock.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 测试执行历史实体
 */
@Data
@Entity
@Table(name = "test_execution_history")
public class TestExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 场景ID
     */
    @Column(nullable = false)
    private Long scenarioId;

    /**
     * 场景名称
     */
    @Column(length = 200)
    private String scenarioName;

    /**
     * 环境ID
     */
    private Long environmentId;

    /**
     * 环境名称
     */
    @Column(length = 100)
    private String environmentName;

    /**
     * 是否成功
     */
    @Column(nullable = false)
    private Boolean success;

    /**
     * 总步骤数
     */
    private Integer totalSteps;

    /**
     * 通过步骤数
     */
    private Integer passedSteps;

    /**
     * 失败步骤数
     */
    private Integer failedSteps;

    /**
     * 总耗时（毫秒）
     */
    private Long duration;

    /**
     * 执行结果详情（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String resultDetail;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 执行时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime executedAt;
}
