package com.mock.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 环境配置实体
 */
@Data
@Entity
@Table(name = "environment")
public class Environment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 环境名称（如：开发环境、测试环境等）
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 基础URL
     */
    @Column(length = 500)
    private String baseUrl;

    /**
     * 描述
     */
    @Column(length = 1000)
    private String description;

    /**
     * 是否为默认环境
     */
    @Column(nullable = false)
    private Boolean isDefault = false;

    /**
     * 显示顺序
     */
    @Column
    private Integer displayOrder;

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
