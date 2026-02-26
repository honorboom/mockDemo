package com.mock.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Proto 文件实体类
 * <p>
 * 存储上传的 .proto 文件内容及其编译后的 FileDescriptorSet 二进制数据。
 * 用于 Protobuf 类型的 Mock 响应配置。
 * </p>
 */
@Data
@Entity
@Table(name = "proto_file")
public class ProtoFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Proto 定义名称
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 原始文件名
     */
    @Column(length = 200)
    private String fileName;

    /**
     * .proto 文件原始内容
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String protoContent;

    /**
     * protoc 编译后的 FileDescriptorSet 字节数据
     */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] descriptorBytes;

    /**
     * 可用的 message 类型列表（JSON 数组格式，如 ["HelloRequest","HelloResponse"]）
     */
    @Column(columnDefinition = "TEXT")
    private String messageTypes;

    /**
     * 描述信息
     */
    @Column(length = 1000)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
