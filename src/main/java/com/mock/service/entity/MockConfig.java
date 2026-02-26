package com.mock.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Mock配置实体类
 * <p>
 * 存储Mock接口的完整配置信息，包括路径、方法、响应内容等。
 * 用于动态Mock服务，根据请求匹配并返回预设的响应。
 * </p>
 * <p>
 * 数据库表名：mock_config
 * </p>
 * <p>
 * 主要特性：
 * <ul>
 *   <li>唯一性约束：path字段具有唯一约束，同一路径和方法组合不能重复</li>
 *   <li>自动时间戳：createdAt和updatedAt由Hibernate自动管理</li>
 *   <li>启用/禁用：支持通过enabled字段控制Mock是否生效</li>
 *   <li>响应延迟：支持模拟网络延迟</li>
 * </ul>
 * </p>
 *
 * @author Mock Service Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Entity
@Table(name = "mock_config")
public class MockConfig {

    /**
     * 主键ID，自增长
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 显示序号
     * <p>
     * 用于前端列表展示的连续序号，从1开始。
     * 创建时自动分配为当前最大序号+1，删除后会重新排序。
     * </p>
     */
    @Column
    private Integer displayOrder;

    /**
     * Mock名称
     * <p>
     * 用于标识Mock配置的友好名称，便于管理和识别。
     * 例如："获取用户信息"、"登录接口"等
     * </p>
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Mock路径
     * <p>
     * 请求路径，不包含域名和端口，以/开头。
     * 例如：/api/user/info, /api/v1/login
     * </p>
     * <p>
     * 注意：具有唯一约束，同一路径和方法组合不能重复配置
     * </p>
     */
    @Column(nullable = false, unique = true, length = 500)
    private String path;

    /**
     * HTTP方法
     * <p>
     * 支持的HTTP方法：GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD
     * 存储时会自动转换为大写
     * </p>
     */
    @Column(nullable = false, length = 10)
    private String method;

    /**
     * 响应HTTP状态码
     * <p>
     * HTTP标准状态码，例如：
     * <ul>
     *   <li>200: OK - 请求成功</li>
     *   <li>201: Created - 资源创建成功</li>
     *   <li>400: Bad Request - 请求错误</li>
     *   <li>401: Unauthorized - 未授权</li>
     *   <li>404: Not Found - 资源不存在</li>
     *   <li>500: Internal Server Error - 服务器错误</li>
     * </ul>
     * 默认值：200
     * </p>
     */
    @Column(nullable = false)
    private Integer statusCode = 200;

    /**
     * 响应内容类型（Content-Type）
     * <p>
     * 常用值：
     * <ul>
     *   <li>application/json - JSON格式（默认）</li>
     *   <li>application/xml - XML格式</li>
     *   <li>text/plain - 纯文本</li>
     *   <li>text/html - HTML</li>
     * </ul>
     * 默认值：application/json
     * </p>
     */
    @Column(length = 100)
    private String contentType = "application/json";

    /**
     * 请求体匹配内容
     * <p>
     * 可选字段，用于更精确的Mock匹配。
     * 如果配置了此字段，只有当请求体与此字段匹配时才返回该Mock响应。
     * 通常存储为JSON格式。
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    private String requestBody;

    /**
     * 响应体内容
     * <p>
     * Mock返回的响应内容，通常为JSON格式。
     * 支持复杂的JSON结构，可以包含数组、嵌套对象等。
     * </p>
     * <p>
     * 示例：
     * <pre>
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": {
     *     "userId": 12345,
     *     "username": "test"
     *   }
     * }
     * </pre>
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    /**
     * 响应延迟时间（毫秒）
     * <p>
     * 用于模拟网络延迟或慢速接口。
     * Mock服务会在返回响应前等待指定的毫秒数。
     * 默认值：0（无延迟）
     * </p>
     * <p>
     * 使用场景：
     * <ul>
     *   <li>测试前端加载状态</li>
     *   <li>模拟慢速网络环境</li>
     *   <li>测试超时处理逻辑</li>
     * </ul>
     * </p>
     */
    @Column
    private Integer delay = 0;

    /**
     * 启用状态
     * <p>
     * 控制Mock是否生效：
     * <ul>
     *   <li>true: 启用，请求会匹配并返回Mock响应</li>
     *   <li>false: 禁用，请求不会匹配此Mock配置</li>
     * </ul>
     * 默认值：true
     * </p>
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 描述信息
     * <p>
     * 对Mock配置的详细说明，便于团队协作和维护。
     * 可以包含使用场景、注意事项等信息。
     * </p>
     */
    @Column(length = 1000)
    private String description;

    /**
     * 响应类型：JSON（默认）或 PROTOBUF
     */
    @Column(length = 20)
    private String responseType = "JSON";

    /**
     * 关联的 Proto 文件 ID（仅 PROTOBUF 类型使用）
     */
    private Long protoFileId;

    /**
     * Proto message 类型名称（如 "MyResponse"，仅 PROTOBUF 类型使用）
     */
    @Column(length = 200)
    private String protoMessageType;

    /**
     * 创建时间
     * <p>
     * 记录自动创建时间，由Hibernate的@CreationTimestamp注解自动管理。
     * 创建后不可修改（updatable = false）
     * </p>
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     * <p>
     * 记录最后修改时间，由Hibernate的@UpdateTimestamp注解自动管理。
     * 每次更新实体时自动更新为当前时间。
     * </p>
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
