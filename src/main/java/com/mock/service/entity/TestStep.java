package com.mock.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 测试步骤实体
 */
@Data
@Entity
@Table(name = "test_step")
public class TestStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属场景ID
     */
    @Column(nullable = false)
    private Long scenarioId;

    /**
     * 步骤名称
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 步骤顺序
     */
    @Column(nullable = false)
    private Integer stepOrder;

    /**
     * 请求URL
     */
    @Column(nullable = false, length = 500)
    private String url;

    /**
     * 请求方法
     */
    @Column(nullable = false, length = 10)
    private String method;

    /**
     * 请求头 (JSON格式)
     */
    @Column(columnDefinition = "TEXT")
    private String headers;

    /**
     * 请求参数 (JSON格式)
     */
    @Column(columnDefinition = "TEXT")
    private String params;

    /**
     * 请求体 (JSON格式)
     */
    @Column(columnDefinition = "TEXT")
    private String body;

    /**
     * 预期状态码
     */
    @Column
    private Integer expectedStatus;

    /**
     * 响应断言 (JSON格式)
     */
    @Column(columnDefinition = "TEXT")
    private String assertions;

    /**
     * 延迟时间(毫秒)
     */
    @Column
    private Integer delay = 0;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
