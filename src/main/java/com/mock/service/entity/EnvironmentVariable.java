package com.mock.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 环境变量实体
 */
@Data
@Entity
@Table(name = "environment_variable")
public class EnvironmentVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 环境ID
     */
    @Column(nullable = false)
    private Long environmentId;

    /**
     * 变量名
     */
    @Column(name = "`key`", nullable = false, length = 100)
    private String key;

    /**
     * 变量值
     */
    @Column(name = "`value`", columnDefinition = "TEXT")
    private String value;

    /**
     * 是否为敏感信息（用于前端显示控制）
     */
    @Column(nullable = false)
    private Boolean isSecret = false;

    /**
     * 描述
     */
    @Column(length = 500)
    private String description;

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
