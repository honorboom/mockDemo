package com.mock.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Proto 文件请求 DTO
 */
@Data
public class ProtoFileRequest {

    @NotBlank(message = "Proto 名称不能为空")
    private String name;

    private String fileName;

    @NotBlank(message = "Proto 内容不能为空")
    private String protoContent;

    private String description;
}
