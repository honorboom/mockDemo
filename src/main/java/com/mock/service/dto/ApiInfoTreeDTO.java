package com.mock.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口信息树形结构DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiInfoTreeDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型：FOLDER（文件夹）或 API（接口）
     */
    private String type;

    /**
     * 接口URL（仅API类型有值）
     */
    private String url;

    /**
     * HTTP方法（仅API类型有值）
     */
    private String method;

    /**
     * 接口描述（仅API类型有值）
     */
    private String description;

    /**
     * 分类（仅API类型有值）
     */
    private String category;

    /**
     * 子节点列表
     */
    private List<ApiInfoTreeDTO> children;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
