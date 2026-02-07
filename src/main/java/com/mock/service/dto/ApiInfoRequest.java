package com.mock.service.dto;

import lombok.Data;

/**
 * 接口信息请求 DTO
 */
@Data
public class ApiInfoRequest {

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 接口URL
     */
    private String url;

    /**
     * HTTP方法
     */
    private String method;

    /**
     * 请求头（JSON格式）
     */
    private String headers;

    /**
     * 请求参数（JSON格式）
     */
    private String params;

    /**
     * 请求体（JSON格式）
     */
    private String body;

    /**
     * 预期状态码
     */
    private Integer expectedStatus;

    /**
     * 接口分类
     */
    private String category;

    /**
     * 标签（JSON数组格式）
     */
    private String tags;

    /**
     * 父级ID（用于树形结构）
     */
    private Long parentId;
}
