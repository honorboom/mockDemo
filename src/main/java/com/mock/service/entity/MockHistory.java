package com.mock.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Mock 调用历史记录实体
 */
@Data
@Entity
@Table(name = "mock_history", indexes = {
    @Index(name = "idx_mock_config_id", columnList = "mockConfigId"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class MockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的 Mock 配置 ID
     */
    @Column(nullable = false)
    private Long mockConfigId;

    /**
     * Mock 名称（冗余字段，方便查询）
     */
    @Column(length = 200)
    private String mockName;

    /**
     * 请求路径
     */
    @Column(nullable = false, length = 500)
    private String requestPath;

    /**
     * 请求方法
     */
    @Column(nullable = false, length = 10)
    private String requestMethod;

    /**
     * 请求参数
     */
    @Column(columnDefinition = "TEXT")
    private String requestParams;

    /**
     * 请求头
     */
    @Column(columnDefinition = "TEXT")
    private String requestHeaders;

    /**
     * 请求体
     */
    @Column(columnDefinition = "TEXT")
    private String requestBody;

    /**
     * 响应状态码
     */
    @Column(nullable = false)
    private Integer responseStatus;

    /**
     * 响应体
     */
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    /**
     * 响应时间（毫秒）
     */
    @Column
    private Long responseTime;

    /**
     * 客户端 IP
     */
    @Column(length = 50)
    private String clientIp;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
