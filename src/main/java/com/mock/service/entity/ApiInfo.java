package com.mock.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 接口信息实体
 */
@Data
@Entity
@Table(name = "api_info")
public class ApiInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 接口名称
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 接口描述
     */
    @Column(length = 1000)
    private String description;

    /**
     * 接口URL
     */
    @Column(length = 500)
    private String url;

    /**
     * HTTP方法
     */
    @Column(length = 10)
    private String method;

    /**
     * 请求头（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String headers;

    /**
     * 请求参数（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String params;

    /**
     * 请求体（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String body;

    /**
     * 预期状态码
     */
    private Integer expectedStatus = 200;

    /**
     * 接口分类
     */
    @Column(length = 100)
    private String category;

    /**
     * 标签（JSON数组格式）
     */
    @Column(length = 500)
    private String tags;

    /**
     * 父级ID（用于实现树形结构，NULL表示根级）
     */
    @Column(nullable = true)
    private Long parentId;

    /**
     * 类型：FOLDER（文件夹）或 API（接口）
     */
    @Column(nullable = false, length = 10)
    private String type = "API";

    /**
     * 显示顺序
     */
    @Column(nullable = false)
    private Integer displayOrder = 0;

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
